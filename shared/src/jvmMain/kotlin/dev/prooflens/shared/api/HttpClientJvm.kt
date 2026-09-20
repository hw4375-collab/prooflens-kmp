package dev.prooflens.shared.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO

actual fun platformHttpClient(): HttpClient = configuredHttpClient(HttpClient(CIO))
