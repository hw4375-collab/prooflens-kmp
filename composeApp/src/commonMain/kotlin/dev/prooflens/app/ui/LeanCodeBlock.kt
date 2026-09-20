package dev.prooflens.app.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

@Composable
fun LeanCodeBlock(code: String) {
    Surface(
        color = Color(0xFF171824),
        shape = androidx.compose.material3.MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth(),
    ) {
        SelectionContainer {
            Text(
                code,
                color = Color(0xFFE8E8F0),
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(16.dp),
            )
        }
    }
}
