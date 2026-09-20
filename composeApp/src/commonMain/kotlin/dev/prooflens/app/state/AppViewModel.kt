package dev.prooflens.app.state

import dev.prooflens.shared.api.ProofLensApi
import dev.prooflens.shared.demo.demoExamples
import dev.prooflens.shared.demo.demoForClaim
import dev.prooflens.shared.model.CheckRequest
import dev.prooflens.shared.model.CheckResponse
import dev.prooflens.shared.model.HistoryItem
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

enum class AppTab { VERIFY, LEARN, HISTORY, SETTINGS }

expect fun defaultServerUrl(): String

data class AppState(
    val tab: AppTab = AppTab.VERIFY,
    val claim: String = "",
    val result: CheckResponse? = null,
    val loading: Boolean = false,
    val demoMode: Boolean = true,
    val serverUrl: String = defaultServerUrl(),
    val health: String = "Not checked",
    val history: List<HistoryItem> = emptyList(),
    val lessonResult: VerifyResponse? = null,
)

class AppViewModel {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    fun selectTab(tab: AppTab) { _state.value = _state.value.copy(tab = tab) }
    fun setClaim(claim: String) { _state.value = _state.value.copy(claim = claim) }
    fun setDemoMode(enabled: Boolean) { _state.value = _state.value.copy(demoMode = enabled) }
    fun setServerUrl(url: String) { _state.value = _state.value.copy(serverUrl = url) }

    fun verifyClaim() {
        val current = _state.value
        if (current.claim.isBlank()) return
        scope.launch {
            _state.value = current.copy(loading = true)
            val response = if (current.demoMode) {
                demoForClaim(current.claim) ?: CheckResponse(
                    current.claim, Verdict.UNVERIFIED, emptyList(),
                    "Try one of the example claims in demo mode.",
                )
            } else {
                runCatching { ProofLensApi(current.serverUrl).check(CheckRequest(current.claim)) }
                    .getOrElse {
                        CheckResponse(current.claim, Verdict.ERROR, emptyList(), it.message ?: "Request failed")
                    }
            }
            val item = HistoryItem(
                id = "${response.claim}-${response.hashCode()}",
                claim = response.claim,
                verdict = response.verdict,
                timestampMs = currentTimeMillis(),
                response = response,
            )
            _state.value = _state.value.copy(
                result = response, loading = false, history = listOf(item) + current.history,
            )
        }
    }

    fun runLesson(lean: String) {
        scope.launch {
            val result = if (_state.value.demoMode) {
                VerifyResponse(true, emptyList(), "", 10)
            } else {
                runCatching { ProofLensApi(_state.value.serverUrl).verify(VerifyRequest(lean)) }
                    .getOrElse { VerifyResponse(false, emptyList(), it.message ?: "Request failed", 0) }
            }
            _state.value = _state.value.copy(lessonResult = result)
        }
    }

    fun checkHealth() {
        scope.launch {
            val health = runCatching { ProofLensApi(_state.value.serverUrl).health() }
                .fold({ "${it.status} ${it.leanVersion.orEmpty()}".trim() }, { "Unavailable: ${it.message}" })
            _state.value = _state.value.copy(health = health)
        }
    }

    fun openHistory(item: HistoryItem) {
        _state.value = _state.value.copy(tab = AppTab.VERIFY, claim = item.claim, result = item.response)
    }

    fun close() { scope.cancel() }
}
