package dev.prooflens.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.prooflens.app.state.AppState
import dev.prooflens.app.state.AppViewModel

@Composable
fun SettingsScreen(state: AppState, viewModel: AppViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(state.serverUrl, viewModel::setServerUrl, Modifier.fillMaxWidth(), label = { Text("Server URL") })
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = state.demoMode, onCheckedChange = viewModel::setDemoMode)
            Spacer(Modifier.width(8.dp))
            Text("Demo mode (works offline)")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(10.dp).background(
                    when (state.healthOk) {
                        true -> Color(0xFF1B8A5A)
                        false -> MaterialTheme.colorScheme.error
                        null -> MaterialTheme.colorScheme.outline
                    },
                    CircleShape,
                ),
            )
            Spacer(Modifier.width(8.dp))
            Text(if (state.healthLoading) "Checking connection…" else state.health)
        }
        Button(onClick = viewModel::checkHealth, enabled = !state.healthLoading) { Text("Test connection") }
        Text("About ProofLens", style = MaterialTheme.typography.titleMedium)
        Text("ProofLens pairs AI-generated Lean proofs with the Lean kernel. AI proposes; Lean decides.")
    }
}
