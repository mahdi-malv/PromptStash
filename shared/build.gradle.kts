@file:Suppress("DEPRECATION", "DEPRECATION_ERROR")

import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.ksp)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

val dropboxAppKey = (findProperty("dropbox.app.key") as? String)
    ?: localProperties.getProperty("dropbox.app.key")
    ?: ""

val generatedDropboxConfigDir = layout.buildDirectory.dir("generated/source/dropboxConfig/commonMain/kotlin")

val generateDropboxConfig by tasks.registering {
    inputs.property("dropboxAppKey", dropboxAppKey)
    outputs.dir(generatedDropboxConfigDir)

    doLast {
        val outputDir = generatedDropboxConfigDir.get().asFile
        val packageDir = outputDir.resolve("com/mahdimalv/prompstash/data/sync")
        packageDir.mkdirs()
        packageDir.resolve("DropboxBuildConfig.kt").writeText(
            """
            package com.mahdimalv.prompstash.data.sync

            internal object DropboxBuildConfig {
                const val CLIENT_ID: String = ${dropboxAppKey.toKotlinStringLiteral()}
            }
            """.trimIndent()
        )
    }
}

kotlin {
    android {
        namespace = "com.mahdimalv.prompstash.shared"
        compileSdk = 36
        minSdk = 24
    }

    jvm("desktop")

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir(generatedDropboxConfigDir)
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material3)
                api(compose.materialIconsExtended)
                api(compose.ui)
                implementation(compose.components.resources)
                implementation(libs.androidx.lifecycle.runtime.compose)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.room.runtime)
                implementation(libs.androidx.sqlite.bundled)
                implementation(libs.androidx.datastore)
                implementation(libs.androidx.datastore.preferences)
                implementation(libs.ktor.client.core)
                implementation(libs.kotlinx.serialization.json)
            }
        }

        val androidMain by getting {
            dependencies {
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.ktor.client.okhttp)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.androidx.lifecycle.runtime.testing)
            }
        }

        val desktopMain by getting {
            dependencies {
                implementation(libs.jetbrains.lifecycle.viewmodel.compose)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.kotlinx.coroutines.swing)
            }
        }

        val desktopTest by getting {
            dependencies {
                implementation(libs.junit)
            }
        }
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspDesktop", libs.androidx.room.compiler)
}

tasks.configureEach {
    if (name.contains("Kotlin", ignoreCase = true) || name.startsWith("ksp", ignoreCase = true)) {
        dependsOn(generateDropboxConfig)
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.mahdimalv.prompstash.resources"
    generateResClass = always
}

room {
    schemaDirectory("$projectDir/schemas")
}

fun String.toKotlinStringLiteral(): String = buildString {
    append('"')
    for (char in this@toKotlinStringLiteral) {
        when (char) {
            '\\' -> append("\\\\")
            '"' -> append("\\\"")
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            '\t' -> append("\\t")
            else -> append(char)
        }
    }
    append('"')
}
