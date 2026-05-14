package com.mahdimalv.promptstash

import platform.Foundation.NSUUID

actual fun generateUuidString(): String = NSUUID().UUIDString().lowercase()
