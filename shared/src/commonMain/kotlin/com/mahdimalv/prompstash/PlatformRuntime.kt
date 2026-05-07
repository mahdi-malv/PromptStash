package com.mahdimalv.prompstash

import kotlinx.coroutines.CoroutineDispatcher

expect fun currentTimeMillis(): Long

expect fun platformIoDispatcher(): CoroutineDispatcher
