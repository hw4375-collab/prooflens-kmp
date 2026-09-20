package dev.prooflens.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.prooflens.app.state.AppState
import dev.prooflens.app.state.AppViewModel

@Composable
fun SettingsScreen(state: AppState, viewModel: AppViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Settings")
        OutlinedTextField(state.serverUrl, viewModel::setServerUrl, Modifier.fillMaxWidth(), label = { Text("Server URL") })
        Switch(checked = state.demoMode, onCheckedChange = viewModel::setDemoMode)
        Text("Demo mode (works offline)")
        Button(onClick = viewModel::checkHealth) { Text("Check server health") }
        Text(state.health)
    }
}
