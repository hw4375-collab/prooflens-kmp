package dev.prooflens.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.prooflens.app.state.AppState
import dev.prooflens.app.state.AppViewModel
import dev.prooflens.app.ui.LeanCodeBlock
import dev.prooflens.app.ui.VerdictBadge

@Composable
fun ChatScreen(state: AppState, viewModel: AppViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("AI chat with live Lean audit", style = MaterialTheme.typography.headlineSmall)
        state.chat.forEachIndexed { index, message ->
            val user = message.role == "user"
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = if (user) Arrangement.End else Arrangement.Start,
            ) {
                Surface(
                    color = if (user) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(if (user) 0.86f else 0.92f),
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(message.content)
                        if (!user) {
                            val response = state.chatChecks[index]
                            val check = response?.check
                            if (check != null) {
                                Text("Claim audited: ${response?.claim}")
                                VerdictBadge(check.verdict)
                                check.attempts.lastOrNull()?.let { LeanCodeBlock(it.lean) }
                            } else {
                                Text("No checkable claim")
                            }
                        }
                    }
                }
            }
        }
        if (state.chatLoading) Text("Auditing reply…")
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = state.chatInput,
                onValueChange = viewModel::setChatInput,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask anything…") },
                enabled = !state.chatLoading,
            )
            Button(onClick = viewModel::sendChat, enabled = state.chatInput.isNotBlank() && !state.chatLoading) {
                Text("Send")
            }
        }
    }
}
