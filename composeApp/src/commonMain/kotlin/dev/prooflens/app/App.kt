package dev.prooflens.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.prooflens.app.screens.HistoryScreen
import dev.prooflens.app.screens.LearnScreen
import dev.prooflens.app.screens.SettingsScreen
import dev.prooflens.app.screens.VerifyScreen
import dev.prooflens.app.state.AppTab
import dev.prooflens.app.state.AppViewModel

@Composable
fun App(viewModel: AppViewModel = remember { AppViewModel() }) {
    val state by viewModel.state.collectAsState()
    MaterialTheme {
        Surface(Modifier.fillMaxSize()) {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        AppTab.entries.forEach { tab ->
                            NavigationBarItem(
                                selected = state.tab == tab,
                                onClick = { viewModel.selectTab(tab) },
                                icon = { Text(tab.name.take(1)) },
                                label = { Text(tab.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            )
                        }
                    }
                },
            ) { padding ->
                Column(
                    Modifier.fillMaxSize().padding(padding).widthIn(max = 900.dp)
                        .verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text("ProofLens", style = MaterialTheme.typography.headlineMedium)
                    when (state.tab) {
                        AppTab.VERIFY -> VerifyScreen(state, viewModel)
                        AppTab.LEARN -> LearnScreen(state, viewModel)
                        AppTab.HISTORY -> HistoryScreen(state, viewModel)
                        AppTab.SETTINGS -> SettingsScreen(state, viewModel)
                    }
                }
            }
        }
    }
}
