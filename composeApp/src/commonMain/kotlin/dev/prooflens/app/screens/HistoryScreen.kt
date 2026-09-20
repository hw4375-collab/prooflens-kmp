package dev.prooflens.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.prooflens.app.state.AppState
import dev.prooflens.app.state.AppViewModel
import dev.prooflens.app.state.currentTimeMillis
import dev.prooflens.shared.model.Verdict

@Composable
fun HistoryScreen(state: AppState, viewModel: AppViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("History", style = MaterialTheme.typography.headlineSmall)
        if (state.history.isEmpty()) Text("Your verified claims will appear here.")
        state.history.forEach { item ->
            Surface(
                onClick = { viewModel.openHistory(item) },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 2.dp,
            ) {
                Row(
                    Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    HistoryVerdict(item.verdict)
                    Column {
                        Text(item.claim, style = MaterialTheme.typography.titleMedium)
                        Text(
                            relativeTime(item.timestampMs),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryVerdict(verdict: Verdict) {
    val color = when (verdict) {
        Verdict.VERIFIED -> Color(0xFF1B8A5A)
        Verdict.REFUTED -> Color(0xFFC62828)
        Verdict.UNVERIFIED -> Color(0xFFB26A00)
        Verdict.ERROR -> MaterialTheme.colorScheme.outline
    }
    Surface(color = color, contentColor = Color.White, shape = RoundedCornerShape(50)) {
        Text(
            verdict.name,
            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

private fun relativeTime(timestampMs: Long): String {
    val seconds = ((currentTimeMillis() - timestampMs) / 1000).coerceAtLeast(0)
    return when {
        seconds < 60 -> "just now"
        seconds < 3600 -> "${seconds / 60}m ago"
        seconds < 86400 -> "${seconds / 3600}h ago"
        else -> "${seconds / 86400}d ago"
    }
}
