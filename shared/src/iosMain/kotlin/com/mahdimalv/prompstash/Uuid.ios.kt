package com.mahdimalv.prompstash

import platform.Foundation.NSUUID

actual fun generateUuidString(): String = NSUUID().UUIDString().lowercase()
