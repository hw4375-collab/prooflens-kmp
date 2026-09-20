package dev.prooflens.server

import dev.prooflens.shared.model.Diagnostic
import dev.prooflens.shared.model.FormalizeRequest
import dev.prooflens.shared.model.FormalizeResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.put
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class MissingOpenAiKeyException : RuntimeException("OPENAI_API_KEY is not configured")

class OpenAiFormalizer {
    private val key = System.getenv("OPENAI_API_KEY")
    private val model = System.getenv("OPENAI_MODEL") ?: "gpt-4o-mini"
    private val client = HttpClient {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    }

    suspend fun formalize(request: FormalizeRequest): FormalizeResponse {
        if (key.isNullOrBlank()) throw MissingOpenAiKeyException()
        val history = request.previousErrors.joinToString("\n") { "${it.severity}: ${it.message}" }
        val user = buildString {
            append("Claim: ${request.claim}\n")
            request.previousLean?.let { append("Previous Lean:\n$it\n") }
            if (history.isNotBlank()) append("Previous diagnostics:\n$history\n")
        }
        val payload = buildJsonObject {
            put("model", model)
            put("temperature", 0.2)
            put("response_format", buildJsonObject { put("type", "json_object") })
            put("messages", kotlinx.serialization.json.buildJsonArray {
                add(buildJsonObject { put("role", "system"); put("content", SYSTEM_PROMPT) })
                add(buildJsonObject { put("role", "user"); put("content", user) })
            })
        }
        val response: JsonObject = client.post("https://api.openai.com/v1/chat/completions") {
            header(HttpHeaders.Authorization, "Bearer $key")
            contentType(ContentType.Application.Json)
            setBody(payload)
        }.body()
        val content = response["choices"]!!.jsonArray[0].jsonObject["message"]!!
            .jsonObject["content"]!!.jsonPrimitive.content
        val parsed = Json.parseToJsonElement(content).jsonObject
        return FormalizeResponse(
            lean = parsed["lean"]!!.jsonPrimitive.content,
            explanation = parsed["explanation"]?.jsonPrimitive?.content.orEmpty(),
            model = model,
            provesNegation = parsed["provesNegation"]?.jsonPrimitive?.content?.toBoolean() ?: false,
        )
    }
}
