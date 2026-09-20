package dev.prooflens.server

import dev.prooflens.shared.model.Diagnostic
import dev.prooflens.shared.model.ChatMessage
import dev.prooflens.shared.model.FormalizeRequest
import dev.prooflens.shared.model.FormalizeResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonNull
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
        val response = client.post("https://api.openai.com/v1/chat/completions") {
            header(HttpHeaders.Authorization, "Bearer $key")
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        if (!response.status.isSuccess()) {
            throw RuntimeException(
                "OpenAI request failed (${response.status.value}): ${response.bodyAsText()}",
            )
        }
        val responseJson = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        val content = responseJson["choices"]!!.jsonArray[0].jsonObject["message"]!!
            .jsonObject["content"]!!.jsonPrimitive.content
        val parsed = Json.parseToJsonElement(stripCodeFence(content)).jsonObject
        return FormalizeResponse(
            lean = stripCodeFence(parsed["lean"]!!.jsonPrimitive.content),
            explanation = parsed["explanation"]?.jsonPrimitive?.content.orEmpty(),
            model = model,
            provesNegation = parsed["provesNegation"]?.jsonPrimitive?.content?.toBoolean() ?: false,
        )
    }

    suspend fun chatWithClaim(messages: List<ChatMessage>): Pair<String, String?> {
        if (key.isNullOrBlank()) throw MissingOpenAiKeyException()
        val payload = buildJsonObject {
            put("model", model)
            put("temperature", 0.2)
            put("response_format", buildJsonObject { put("type", "json_object") })
            put("messages", kotlinx.serialization.json.buildJsonArray {
                add(buildJsonObject {
                    put("role", "system")
                    put(
                        "content",
                        "You are a helpful assistant. Answer the user's latest message concisely. " +
                            "Then return JSON only: {\"reply\": \"...\", \"claim\": \"...\"} where claim " +
                            "is the single most important checkable factual/logical statement your reply " +
                            "asserts, phrased as a standalone proposition (math, logic, or simple facts " +
                            "about Nat/Bool/lists); null if none.",
                    )
                })
                messages.forEach { message ->
                    add(buildJsonObject {
                        put("role", message.role)
                        put("content", message.content)
                    })
                }
            })
        }
        val response = client.post("https://api.openai.com/v1/chat/completions") {
            header(HttpHeaders.Authorization, "Bearer $key")
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        if (!response.status.isSuccess()) {
            throw RuntimeException(
                "OpenAI request failed (${response.status.value}): ${response.bodyAsText()}",
            )
        }
        val content = Json.parseToJsonElement(response.bodyAsText()).jsonObject["choices"]!!
            .jsonArray[0].jsonObject["message"]!!.jsonObject["content"]!!
            .jsonPrimitive.content
        val parsed = Json.parseToJsonElement(stripCodeFence(content)).jsonObject
        val claim = parsed["claim"]?.takeUnless { it is JsonNull }?.jsonPrimitive?.content
        return parsed["reply"]!!.jsonPrimitive.content to claim
    }

    private fun stripCodeFence(value: String): String {
        val trimmed = value.trim()
        if (!trimmed.startsWith("```") || !trimmed.endsWith("```")) return trimmed
        return trimmed
            .substringAfter('\n', missingDelimiterValue = "")
            .substringBeforeLast("```")
            .trim()
    }
}
