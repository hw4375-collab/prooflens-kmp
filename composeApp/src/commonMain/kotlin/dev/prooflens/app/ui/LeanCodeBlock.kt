package dev.prooflens.app.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily

@Composable
fun LeanCodeBlock(code: String) {
    Card {
        Text(code, fontFamily = FontFamily.Monospace, modifier = Modifier.horizontalScroll(rememberScrollState()))
    }
}
