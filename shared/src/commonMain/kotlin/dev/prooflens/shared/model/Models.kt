package dev.prooflens.shared.model

import kotlinx.serialization.Serializable

@Serializable
enum class Verdict { VERIFIED, REFUTED, UNVERIFIED, ERROR }

@Serializable
data class Diagnostic(
    val line: Int,
    val col: Int,
    val severity: String,
    val message: String,
)

@Serializable
data class FormalizeRequest(
    val claim: String,
    val previousLean: String? = null,
    val previousErrors: List<Diagnostic> = emptyList(),
)

@Serializable
data class FormalizeResponse(
    val lean: String,
    val explanation: String,
    val model: String,
    val provesNegation: Boolean = false,
)

@Serializable
data class VerifyRequest(val lean: String)

@Serializable
data class VerifyResponse(
    val ok: Boolean,
    val diagnostics: List<Diagnostic>,
    val rawOutput: String,
    val durationMs: Long,
)

@Serializable
data class CheckRequest(val claim: String, val maxAttempts: Int = 3)

@Serializable
data class CheckAttempt(
    val attempt: Int,
    val lean: String,
    val explanation: String,
    val verify: VerifyResponse,
    val provesNegation: Boolean = false,
)

@Serializable
data class CheckResponse(
    val claim: String,
    val verdict: Verdict,
    val attempts: List<CheckAttempt>,
    val summary: String,
)

@Serializable
data class HistoryItem(
    val id: String,
    val claim: String,
    val verdict: Verdict,
    val timestampMs: Long,
    val response: CheckResponse,
)

@Serializable
data class Lesson(
    val id: String,
    val title: String,
    val blurb: String,
    val leanSnippet: String,
    val tactic: String,
)

@Serializable
data class HealthResponse(val status: String, val leanVersion: String? = null)
