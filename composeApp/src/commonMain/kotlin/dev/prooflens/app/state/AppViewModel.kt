package dev.prooflens.app.state

import dev.prooflens.shared.api.ProofLensApi
import dev.prooflens.shared.demo.demoExamples
import dev.prooflens.shared.demo.demoForClaim
import dev.prooflens.shared.model.CheckRequest
import dev.prooflens.shared.model.CheckResponse
import dev.prooflens.shared.model.ChatMessage
import dev.prooflens.shared.model.ChatRequest
import dev.prooflens.shared.model.ChatResponse
import dev.prooflens.shared.model.HistoryItem
import dev.prooflens.shared.model.Lesson
import dev.prooflens.shared.model.Verdict
import dev.prooflens.shared.model.VerifyRequest
import dev.prooflens.shared.model.VerifyResponse
import dev.prooflens.shared.api.platformHttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppTab { VERIFY, CHAT, LEARN, HISTORY, SETTINGS }

expect fun defaultServerUrl(): String

data class AppState(
    val tab: AppTab = AppTab.VERIFY,
    val claim: String = "",
    val result: CheckResponse? = null,
    val loading: Boolean = false,
    val attemptsInProgress: String = "",
    val demoMode: Boolean = false,
    val serverUrl: String = defaultServerUrl(),
    val health: String = "Not checked",
    val healthOk: Boolean? = null,
    val healthLoading: Boolean = false,
    val history: List<HistoryItem> = emptyList(),
    val lessonResult: VerifyResponse? = null,
    val selectedLesson: Lesson? = null,
    val lessonEditor: String = "",
    val lessonLoading: Boolean = false,
    val chat: List<ChatMessage> = emptyList(),
    val chatChecks: Map<Int, ChatResponse> = emptyMap(),
    val chatInput: String = "",
    val chatLoading: Boolean = false,
)

class AppViewModel {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    fun selectTab(tab: AppTab) { _state.value = _state.value.copy(tab = tab) }
    fun setClaim(claim: String) { _state.value = _state.value.copy(claim = claim) }
    fun setChatInput(input: String) { _state.value = _state.value.copy(chatInput = input) }
    fun setDemoMode(enabled: Boolean) { _state.value = _state.value.copy(demoMode = enabled) }
    fun setServerUrl(url: String) {
        _state.value = _state.value.copy(serverUrl = url)
        checkHealth()
    }

    fun openLesson(lesson: Lesson) {
        _state.value = _state.value.copy(
            selectedLesson = lesson,
            lessonEditor = lesson.leanSnippet,
            lessonResult = null,
        )
    }

    fun closeLesson() {
        _state.value = _state.value.copy(selectedLesson = null, lessonResult = null)
    }

    fun setLessonEditor(lean: String) {
        _state.value = _state.value.copy(lessonEditor = lean)
    }

    fun verifyClaim() {
        val current = _state.value
        if (current.claim.isBlank()) return
        scope.launch {
            _state.value = current.copy(
                loading = true,
                attemptsInProgress = "Asking the AI to formalize… → Lean checking…",
            )
            val demo = demoForClaim(current.claim)
            val response = if (current.demoMode && demo != null) {
                demo.copy(summary = "(demo mode) ${demo.summary}")
            } else {
                runCatching { ProofLensApi(current.serverUrl).check(CheckRequest(current.claim)) }
                    .fold(
                        onSuccess = { it },
                        onFailure = { error ->
                            demo?.copy(summary = "(offline demo) ${demo.summary}") ?: CheckResponse(
                                current.claim,
                                Verdict.ERROR,
                                emptyList(),
                                error.message ?: "Request failed",
                            )
                        },
                    )
                    }
            val item = HistoryItem(
                id = "${response.claim}-${response.hashCode()}",
                claim = response.claim,
                verdict = response.verdict,
                timestampMs = currentTimeMillis(),
                response = response,
            )
            _state.value = _state.value.copy(
                result = response,
                loading = false,
                attemptsInProgress = "",
                history = listOf(item) + current.history,
            )
        }
    }

    fun runLesson() {
        val current = _state.value
        scope.launch {
            _state.value = current.copy(lessonLoading = true, lessonResult = null)
            val result = if (current.demoMode) {
                VerifyResponse(true, emptyList(), "", 10)
            } else {
                runCatching { ProofLensApi(current.serverUrl).verify(VerifyRequest(current.lessonEditor)) }
                    .getOrElse { VerifyResponse(false, emptyList(), it.message ?: "Request failed", 0) }
            }
            _state.value = _state.value.copy(lessonResult = result, lessonLoading = false)
        }
    }

    fun sendChat() {
        val current = _state.value
        if (current.chatInput.isBlank() || current.chatLoading) return
        val userMessage = ChatMessage("user", current.chatInput.trim())
        val messages = current.chat + userMessage
        _state.value = current.copy(chat = messages, chatInput = "", chatLoading = true)
        scope.launch {
            val response = if (current.demoMode) {
                null
            } else {
                runCatching { ProofLensApi(current.serverUrl).chat(ChatRequest(messages)) }.getOrNull()
            }
            val assistant = response?.reply ?: "(offline) I can't reach the server."
            val withAssistant = messages + ChatMessage("assistant", assistant)
            _state.value = _state.value.copy(
                chat = withAssistant,
                chatChecks = response?.let { _state.value.chatChecks + (withAssistant.lastIndex to it) }
                    ?: _state.value.chatChecks,
                chatLoading = false,
            )
        }
    }

    fun checkHealth() {
        val current = _state.value
        scope.launch {
            _state.value = _state.value.copy(healthLoading = true)
            runCatching { ProofLensApi(current.serverUrl).health() }
                .fold(
                    onSuccess = {
                        _state.value = _state.value.copy(
                            health = "${it.status} ${it.leanVersion.orEmpty()}".trim(),
                            healthOk = true,
                            healthLoading = false,
                        )
                    },
                    onFailure = {
                        _state.value = _state.value.copy(
                            health = "Unavailable: ${it.message ?: "request failed"}",
                            healthOk = false,
                            healthLoading = false,
                        )
                    },
                )
        }
    }

    fun openHistory(item: HistoryItem) {
        _state.value = _state.value.copy(tab = AppTab.VERIFY, claim = item.claim, result = item.response)
    }

    fun close() { scope.cancel() }

    init {
        checkHealth()
    }
}
