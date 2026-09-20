package dev.prooflens.shared.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

actual fun platformHttpClient(): HttpClient = configuredHttpClient(HttpClient(OkHttp))
