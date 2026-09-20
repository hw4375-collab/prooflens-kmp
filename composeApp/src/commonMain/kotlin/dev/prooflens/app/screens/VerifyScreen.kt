package dev.prooflens.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.prooflens.app.state.AppState
import dev.prooflens.app.state.AppViewModel
import dev.prooflens.app.ui.LeanCodeBlock
import dev.prooflens.app.ui.StepTimeline
import dev.prooflens.app.ui.VerdictBadge
import dev.prooflens.shared.demo.demoExamples

@Composable
fun VerifyScreen(state: AppState, viewModel: AppViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("AI proposes. Lean decides.")
        OutlinedTextField(
            value = state.claim,
            onValueChange = viewModel::setClaim,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("State a claim or paste an AI answer") },
            minLines = 3,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            demoExamples.forEach { (claim, _) ->
                Button(onClick = { viewModel.setClaim(claim) }) { Text(claim.take(12)) }
            }
        }
        Button(onClick = viewModel::verifyClaim, enabled = !state.loading) {
            Text(if (state.loading) "Verifying…" else "Formalize & Verify")
        }
        state.result?.let { response ->
            VerdictBadge(response.verdict)
            Text(response.summary)
            StepTimeline(response.attempts.size, state.loading)
            response.attempts.lastOrNull()?.let { attempt ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth()) {
                        Text(attempt.explanation)
                        LeanCodeBlock(attempt.lean)
                        Text(if (attempt.verify.ok) "Lean accepted" else "Lean diagnostics")
                        attempt.verify.diagnostics.forEach { Text("${it.severity}: ${it.message}") }
                    }
                }
            }
        }
    }
}
