package com.mahdimalv.prompstash.data.sync

import io.ktor.client.HttpClient

expect fun createPlatformHttpClient(): HttpClient
