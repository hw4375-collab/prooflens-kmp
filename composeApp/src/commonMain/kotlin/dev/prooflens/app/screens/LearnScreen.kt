package dev.prooflens.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.prooflens.app.state.AppState
import dev.prooflens.app.state.AppViewModel
import dev.prooflens.app.ui.LeanCodeBlock
import dev.prooflens.shared.lessons.lessons

@Composable
fun LearnScreen(state: AppState, viewModel: AppViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Learn Lean")
        lessons.forEach { lesson ->
            Card(Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(lesson.title)
                    Text(lesson.blurb)
                    LeanCodeBlock(lesson.leanSnippet)
                    Button(onClick = { viewModel.runLesson(lesson.leanSnippet) }) { Text("Run in Lean") }
                }
            }
        }
        state.lessonResult?.let { Text(if (it.ok) "Lean accepted" else "Lean found diagnostics: ${it.diagnostics.joinToString()}") }
    }
}
