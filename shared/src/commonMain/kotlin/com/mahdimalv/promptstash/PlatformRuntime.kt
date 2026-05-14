package com.mahdimalv.promptstash

import kotlinx.coroutines.CoroutineDispatcher

expect fun currentTimeMillis(): Long

expect fun platformIoDispatcher(): CoroutineDispatcher
