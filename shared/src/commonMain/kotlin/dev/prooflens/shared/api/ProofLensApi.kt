package dev.prooflens.shared.api

import dev.prooflens.shared.model.CheckRequest
import dev.prooflens.shared.model.CheckResponse
import dev.prooflens.shared.model.FormalizeRequest
import dev.prooflens.shared.model.FormalizeResponse
import dev.prooflens.shared.model.HealthResponse
import dev.prooflens.shared.model.VerifyRequest
import dev.prooflens.shared.model.VerifyResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

expect fun platformHttpClient(): HttpClient

class ProofLensApi(
    private val baseUrl: String,
    private val client: HttpClient = platformHttpClient(),
) {
    suspend fun health(): HealthResponse = client.get("$baseUrl/health").body()

    suspend fun formalize(request: FormalizeRequest): FormalizeResponse =
        client.post("$baseUrl/formalize") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun verify(request: VerifyRequest): VerifyResponse =
        client.post("$baseUrl/verify") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun check(request: CheckRequest): CheckResponse =
        client.post("$baseUrl/check") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
}

fun configuredHttpClient(base: HttpClient): HttpClient = base.config {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}
