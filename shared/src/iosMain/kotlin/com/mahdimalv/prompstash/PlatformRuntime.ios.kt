package com.mahdimalv.prompstash

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import platform.posix.time

@OptIn(ExperimentalForeignApi::class)
actual fun currentTimeMillis(): Long = time(null).toLong() * 1_000L

actual fun platformIoDispatcher(): CoroutineDispatcher = Dispatchers.Default
