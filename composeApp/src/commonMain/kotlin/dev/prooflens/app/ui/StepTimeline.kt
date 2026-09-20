package dev.prooflens.app.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.prooflens.shared.model.CheckAttempt

@Composable
fun StepTimeline(attempts: List<CheckAttempt>, loading: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        attempts.forEach { attempt ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Attempt ${attempt.attempt} — AI formalized → Lean: ${if (attempt.verify.ok) "✓ accepted" else "✗ ${attempt.verify.diagnostics.size} errors"}")
            }
        }
        if (loading) {
            Text("AI formalizing → Lean checking → repairing…")
        } else if (attempts.lastOrNull()?.verify?.ok == true) {
            Text("Lean: ✓ accepted")
        }
    }
}
