package com.mahdimalv.promptstash.data.sync

import io.ktor.client.HttpClient

expect fun createPlatformHttpClient(): HttpClient
