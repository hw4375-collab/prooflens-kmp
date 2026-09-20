package dev.prooflens.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import dev.prooflens.shared.model.Verdict

@Composable
fun VerdictBadge(verdict: Verdict) {
    val color = when (verdict) {
        Verdict.VERIFIED -> MaterialTheme.colorScheme.primary
        Verdict.REFUTED -> MaterialTheme.colorScheme.error
        Verdict.UNVERIFIED -> MaterialTheme.colorScheme.tertiary
        Verdict.ERROR -> MaterialTheme.colorScheme.outline
    }
    Text(verdict.name, color = color, style = MaterialTheme.typography.headlineSmall)
}
