package dev.prooflens.app.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun StepTimeline(attempts: Int, loading: Boolean) {
    Text(if (loading) "Formalizing → Lean checking → Repairing…" else "Attempts: $attempts")
}
