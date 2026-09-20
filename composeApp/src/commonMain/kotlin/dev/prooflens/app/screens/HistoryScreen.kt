package dev.prooflens.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import dev.prooflens.app.state.AppState
import dev.prooflens.app.state.AppViewModel

@Composable
fun HistoryScreen(state: AppState, viewModel: AppViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("History")
        if (state.history.isEmpty()) Text("Verified claims will appear here.")
        state.history.forEach { item ->
            Button(onClick = { viewModel.openHistory(item) }) {
                Text("${item.verdict}: ${item.claim}")
            }
        }
    }
}
