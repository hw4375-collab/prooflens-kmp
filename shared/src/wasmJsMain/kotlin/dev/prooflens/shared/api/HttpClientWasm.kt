package dev.prooflens.shared.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.js.Js

actual fun platformHttpClient(): HttpClient = configuredHttpClient(HttpClient(Js))
