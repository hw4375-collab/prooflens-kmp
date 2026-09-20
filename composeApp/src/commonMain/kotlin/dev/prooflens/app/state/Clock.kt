package dev.prooflens.app.state

import kotlin.time.Clock

@OptIn(kotlin.time.ExperimentalTime::class)
internal fun currentTimeMillis(): Long = Clock.System.now().toEpochMilliseconds()
