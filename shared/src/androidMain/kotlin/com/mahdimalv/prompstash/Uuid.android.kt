package com.mahdimalv.prompstash

import java.util.UUID

actual fun generateUuidString(): String = UUID.randomUUID().toString()
