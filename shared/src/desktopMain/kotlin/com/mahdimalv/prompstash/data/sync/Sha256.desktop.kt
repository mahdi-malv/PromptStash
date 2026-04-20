package com.mahdimalv.prompstash.data.sync

import java.security.MessageDigest

actual fun sha256(value: ByteArray): ByteArray = MessageDigest.getInstance("SHA-256").digest(value)
