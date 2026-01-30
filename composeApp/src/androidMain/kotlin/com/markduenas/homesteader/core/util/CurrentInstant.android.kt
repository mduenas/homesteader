package com.markduenas.homesteader.core.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

actual fun currentInstant(): Instant = Clock.System.now()
