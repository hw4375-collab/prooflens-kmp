package dev.prooflens.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import dev.prooflens.app.screens.HistoryScreen
import dev.prooflens.app.screens.LearnScreen
import dev.prooflens.app.screens.SettingsScreen
import dev.prooflens.app.screens.VerifyScreen
import dev.prooflens.app.state.AppTab
import dev.prooflens.app.state.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(viewModel: AppViewModel = remember { AppViewModel() }) {
    val state by viewModel.state.collectAsState()
    val dark = isSystemInDarkTheme()
    LaunchedEffect(state.tab) {
        if (state.tab == AppTab.SETTINGS) viewModel.checkHealth()
    }
    MaterialTheme(colorScheme = if (dark) DarkColors else LightColors) {
        Surface(Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Column {
                                Text("ProofLens")
                                Text(
                                    "AI proposes. Lean decides.",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        },
                    )
                },
            ) { padding ->
                BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
                    if (maxWidth >= 840.dp) {
                        Row(Modifier.fillMaxSize()) {
                            NavigationRail {
                                tabs(state, viewModel, rail = true)
                            }
                            content(state, viewModel, Modifier.fillMaxWidth())
                        }
                    } else {
                        Column(Modifier.fillMaxSize()) {
                            content(state, viewModel, Modifier.fillMaxWidth())
                            NavigationBar {
                                tabs(state, viewModel, rail = false)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun content(state: dev.prooflens.app.state.AppState, viewModel: AppViewModel, modifier: Modifier) {
    Column(
        modifier.fillMaxSize().widthIn(max = 1080.dp).verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (state.tab) {
            AppTab.VERIFY -> VerifyScreen(state, viewModel)
            AppTab.LEARN -> LearnScreen(state, viewModel)
            AppTab.HISTORY -> HistoryScreen(state, viewModel)
            AppTab.SETTINGS -> SettingsScreen(state, viewModel)
        }
    }
}

@Composable
private fun tabs(state: dev.prooflens.app.state.AppState, viewModel: AppViewModel, rail: Boolean) {
    AppTab.entries.forEach { tab ->
        val icon = when (tab) {
            AppTab.VERIFY -> "✓"
            AppTab.LEARN -> "▣"
            AppTab.HISTORY -> "↺"
            AppTab.SETTINGS -> "⚙"
        }
        if (rail) {
            NavigationRailItem(
                selected = state.tab == tab,
                onClick = { viewModel.selectTab(tab) },
                icon = { Text(icon) },
                label = { Text(tab.name.lowercase().replaceFirstChar { it.uppercase() }) },
            )
        } else {
            Column(
                Modifier.clickable { viewModel.selectTab(tab) }.padding(vertical = 8.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            ) {
                Text(icon)
                Text(tab.name.lowercase().replaceFirstChar { it.uppercase() })
            }
        }
    }
}

private val LightColors = lightColorScheme(
    primary = Color(0xFF3F3D9A),
    secondary = Color(0xFF5B5AA6),
    tertiary = Color(0xFFB26A00),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB9B5FF),
    secondary = Color(0xFFC3C1FF),
    tertiary = Color(0xFFFFB95C),
)
