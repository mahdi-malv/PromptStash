package com.mahdimalv.promptstash

import java.util.UUID

actual fun generateUuidString(): String = UUID.randomUUID().toString()
