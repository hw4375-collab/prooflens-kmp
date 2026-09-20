package dev.prooflens.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import dev.prooflens.app.state.AppState
import dev.prooflens.app.state.AppViewModel
import dev.prooflens.app.ui.LeanCodeBlock
import dev.prooflens.app.ui.StepTimeline
import dev.prooflens.app.ui.VerdictBadge
import dev.prooflens.shared.demo.demoExamples

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VerifyScreen(state: AppState, viewModel: AppViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = state.claim,
            onValueChange = viewModel::setClaim,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("State a claim or paste an AI answer") },
            minLines = 3,
        )
        Text("Try an example", style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            demoExamples.forEach { (claim, _) ->
                AssistChip(
                    onClick = { viewModel.setClaim(claim) },
                    label = { Text(claim) },
                )
            }
        }
        androidx.compose.material3.Button(onClick = viewModel::verifyClaim, enabled = !state.loading) {
            Text("Formalize & Verify")
        }
        if (state.loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
            Text(state.attemptsInProgress, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant)
        }
        state.result?.let { response ->
            Card(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    VerdictBadge(response.verdict)
                    Text(response.summary, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    StepTimeline(response.attempts, state.loading)
                    response.attempts.lastOrNull()?.let { attempt ->
                        Text(attempt.explanation)
                        LeanCodeBlock(attempt.lean)
                        var diagnosticsExpanded by remember(response) { mutableStateOf(false) }
                        TextButton(onClick = { diagnosticsExpanded = !diagnosticsExpanded }) {
                            Text(if (diagnosticsExpanded) "Hide Lean diagnostics" else "Lean diagnostics")
                        }
                        if (diagnosticsExpanded) {
                            if (attempt.verify.diagnostics.isEmpty()) {
                                Text("No diagnostics — Lean accepted this proof.")
                            } else {
                                attempt.verify.diagnostics.forEach {
                                    Text("${it.severity}: ${it.message}")
                                }
                            }
                        }
                    }
                    response.attempts.dropLast(1).forEach { attempt ->
                        var expanded by remember(response, attempt.attempt) { mutableStateOf(false) }
                        HorizontalDivider()
                        TextButton(onClick = { expanded = !expanded }) {
                            Text(if (expanded) "Hide Attempt ${attempt.attempt} of ${response.attempts.size}" else "Attempt ${attempt.attempt} of ${response.attempts.size}")
                        }
                        if (expanded) {
                            Text(attempt.explanation)
                            LeanCodeBlock(attempt.lean)
                            attempt.verify.diagnostics.forEach {
                                Text("${it.severity}: ${it.message}")
                            }
                        }
                    }
                    val duration = response.attempts.lastOrNull()?.verify?.durationMs ?: 0
                    Text(
                        "Model: gpt-4o-mini • Lean 4.34.0 • ${duration} ms",
                        style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
