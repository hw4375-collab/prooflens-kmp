package dev.prooflens.server

import dev.prooflens.shared.model.CheckAttempt
import dev.prooflens.shared.model.CheckRequest
import dev.prooflens.shared.model.CheckResponse
import dev.prooflens.shared.model.ChatRequest
import dev.prooflens.shared.model.ChatResponse
import dev.prooflens.shared.model.Diagnostic
import dev.prooflens.shared.model.FormalizeRequest
import dev.prooflens.shared.model.HealthResponse
import dev.prooflens.shared.model.Verdict
import dev.prooflens.shared.model.VerifyRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.io.File

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module(
    leanRunner: LeanRunner = LeanRunner(),
    formalizer: OpenAiFormalizer = OpenAiFormalizer(),
) {
    install(CallLogging)
    install(ContentNegotiation) { json(Json { prettyPrint = true; ignoreUnknownKeys = true }) }
    install(CORS) { anyHost() }
    routing {
        get("/health") {
            val version = runCatching {
                ProcessBuilder("lake", "--version").directory(File(System.getenv("LEAN_PROJECT_DIR") ?: "../lean")).start()
                    .inputStream.bufferedReader().readText().trim()
            }.getOrNull()
            call.respond(HealthResponse("ok", version))
        }
        post("/verify") {
            call.respond(leanRunner.verify(call.receive<VerifyRequest>().lean))
        }
        post("/formalize") {
            try {
                call.respond(formalizer.formalize(call.receive()))
            } catch (e: MissingOpenAiKeyException) {
                call.respond(HttpStatusCode.ServiceUnavailable, mapOf("error" to e.message))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadGateway, mapOf("error" to (e.message ?: "OpenAI request failed")))
            }
        }
        post("/check") {
            val request = call.receive<CheckRequest>()
            try {
                call.respond(runCheck(request.claim, request.maxAttempts, formalizer, leanRunner))
            } catch (e: MissingOpenAiKeyException) {
                call.respond(HttpStatusCode.ServiceUnavailable, mapOf("error" to e.message))
                return@post
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadGateway, mapOf("error" to (e.message ?: "Formalization failed")))
                return@post
            }
        }
        post("/chat") {
            try {
                val (reply, claim) = formalizer.chatWithClaim(call.receive<ChatRequest>().messages)
                val check = claim?.takeIf { it.isNotBlank() }?.let {
                    runCheck(it, 3, formalizer, leanRunner)
                }
                call.respond(ChatResponse(reply, claim, check))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (e.message ?: "Chat failed")))
            }
        }
    }
}

suspend fun runCheck(
    claim: String,
    maxAttempts: Int,
    formalizer: OpenAiFormalizer,
    leanRunner: LeanRunner,
): CheckResponse {
    val attempts = mutableListOf<CheckAttempt>()
    var previousLean: String? = null
    var previousErrors = emptyList<Diagnostic>()
    for (index in 0 until maxAttempts.coerceIn(1, 3)) {
        val formalized = formalizer.formalize(FormalizeRequest(claim, previousLean, previousErrors))
        val verify = leanRunner.verify(formalized.lean)
        attempts += CheckAttempt(index + 1, formalized.lean, formalized.explanation, verify, formalized.provesNegation)
        if (verify.ok) break
        previousLean = formalized.lean
        previousErrors = verify.diagnostics
    }
    val final = attempts.lastOrNull()
    val verdict = when {
        final == null -> Verdict.UNVERIFIED
        final.verify.ok && final.provesNegation -> Verdict.REFUTED
        final.verify.ok -> Verdict.VERIFIED
        else -> Verdict.UNVERIFIED
    }
    return CheckResponse(claim, verdict, attempts, when (verdict) {
        Verdict.VERIFIED -> "Lean accepted the proposed proof."
        Verdict.REFUTED -> "Lean accepted a proof of the negation."
        else -> "Lean could not verify the claim within the attempt limit."
    })
}
