package dev.prooflens.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.prooflens.shared.model.Verdict

@Composable
fun VerdictBadge(verdict: Verdict) {
    val (color, glyph) = when (verdict) {
        Verdict.VERIFIED -> Color(0xFF1B8A5A) to "OK"
        Verdict.REFUTED -> Color(0xFFC62828) to "NO"
        Verdict.UNVERIFIED -> Color(0xFFB26A00) to "?"
        Verdict.ERROR -> MaterialTheme.colorScheme.outline to "!"
    }
    Surface(color = color, contentColor = Color.White, shape = RoundedCornerShape(50)) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(glyph, style = MaterialTheme.typography.titleLarge)
            Text(verdict.name, style = MaterialTheme.typography.labelLarge)
        }
    }
}
