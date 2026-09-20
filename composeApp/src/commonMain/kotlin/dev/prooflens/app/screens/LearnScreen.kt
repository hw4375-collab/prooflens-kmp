package dev.prooflens.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import dev.prooflens.app.state.AppState
import dev.prooflens.app.state.AppViewModel
import dev.prooflens.app.ui.LeanCodeBlock
import dev.prooflens.shared.lessons.lessons

@Composable
fun LearnScreen(state: AppState, viewModel: AppViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Learn Lean", style = MaterialTheme.typography.headlineSmall)
        state.selectedLesson?.let { lesson ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    Modifier.fillMaxWidth().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(lesson.title, style = MaterialTheme.typography.titleLarge)
                    Text(lesson.blurb)
                    OutlinedTextField(
                        value = state.lessonEditor,
                        onValueChange = viewModel::setLessonEditor,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace),
                        minLines = 8,
                        label = { Text("Edit the Lean proof") },
                    )
                    Button(onClick = viewModel::runLesson, enabled = !state.lessonLoading) {
                        Text(if (state.lessonLoading) "Checking…" else "Run in Lean")
                    }
                    state.lessonResult?.let { result ->
                        Text(
                            if (result.ok) {
                                "✓ Lean accepted${if (state.demoMode) " (demo mode)" else ""}"
                            } else {
                                "✗ Lean diagnostics${if (state.demoMode) " (demo mode)" else ""}"
                            },
                            color = if (result.ok) Color(0xFF1B8A5A) else MaterialTheme.colorScheme.error,
                        )
                        result.diagnostics.forEach { Text("${it.severity}: ${it.message}") }
                    }
                }
            }
            Button(onClick = viewModel::closeLesson) { Text("Back to lessons") }
        } ?: lessons.forEach { lesson ->
            Card(
                onClick = { viewModel.openLesson(lesson) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(lesson.title, style = MaterialTheme.typography.titleMedium)
                    Text(lesson.blurb)
                    Text("Practice ${lesson.tactic} →", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
