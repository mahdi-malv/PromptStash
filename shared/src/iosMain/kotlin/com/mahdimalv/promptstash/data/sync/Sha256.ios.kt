package com.mahdimalv.promptstash.data.sync

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH

@OptIn(ExperimentalForeignApi::class)
actual fun sha256(value: ByteArray): ByteArray {
    val digest = ByteArray(CC_SHA256_DIGEST_LENGTH.toInt())
    value.usePinned { input ->
        digest.usePinned { output ->
            val inputPointer = if (value.isEmpty()) null else input.addressOf(0)
            CC_SHA256(
                inputPointer,
                value.size.convert(),
                output.addressOf(0).reinterpret(),
            )
        }
    }
    return digest
}
