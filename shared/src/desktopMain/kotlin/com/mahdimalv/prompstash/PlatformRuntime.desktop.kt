package com.mahdimalv.prompstash

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun platformIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
