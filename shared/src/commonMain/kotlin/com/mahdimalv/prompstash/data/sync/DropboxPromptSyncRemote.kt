package com.mahdimalv.prompstash.data.sync

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class DropboxPromptSyncRemote(
    private val httpClient: HttpClient,
    private val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    },
) : PromptSyncRemote {

    override val remoteType: RemoteType = RemoteType.DROPBOX

    override suspend fun download(accessToken: String): RemoteSnapshot {
        val response = httpClient.post(DROPBOX_DOWNLOAD_URL) {
            header(HttpHeaders.Authorization, bearerToken(accessToken))
            header(DROPBOX_API_ARG_HEADER, """{"path":"$REMOTE_FILE_PATH"}""")
        }

        val responseBody = response.body<String>()
        return when {
            response.status.isSuccess() -> {
                val metadata = response.headers[DROPBOX_API_RESULT_HEADER].orEmpty()
                val revision = metadata.takeIf(String::isNotBlank)
                    ?.let { rawMetadata -> json.parseToJsonElement(rawMetadata).jsonObject["rev"]?.jsonPrimitive?.content }
                val snapshot = if (responseBody.isBlank()) {
                    DropboxSnapshot()
                } else {
                    json.decodeFromString<DropboxSnapshot>(responseBody)
                }
                RemoteSnapshot(
                    records = snapshot.records.map(RemotePromptRecord::toPromptSyncRecord),
                    revision = revision,
                )
            }

            response.status.value == 409 && responseBody.contains("not_found", ignoreCase = true) -> {
                RemoteSnapshot(records = emptyList(), revision = null)
            }

            response.status.value == 401 -> {
                throw PromptSyncRemoteException("Dropbox rejected the saved access token.")
            }

            else -> {
                throw PromptSyncRemoteException("Dropbox download failed (${response.status.value}).")
            }
        }
    }

    override suspend fun upload(
        accessToken: String,
        records: List<PromptSyncRecord>,
        previousRevision: String?,
    ): String {
        val snapshot = DropboxSnapshot(
            records = records.map(PromptSyncRecord::toRemotePromptRecord),
        )
        val response = httpClient.post(DROPBOX_UPLOAD_URL) {
            header(HttpHeaders.Authorization, bearerToken(accessToken))
            header(DROPBOX_API_ARG_HEADER, uploadArg(previousRevision).toString())
            contentType(ContentType.Application.OctetStream)
            setBody(json.encodeToString(DropboxSnapshot.serializer(), snapshot))
        }

        val responseBody = response.body<String>()
        return when {
            response.status.isSuccess() -> {
                json.parseToJsonElement(responseBody).jsonObject["rev"]?.jsonPrimitive?.content
                    ?: throw PromptSyncRemoteException("Dropbox upload response did not include a revision.")
            }

            response.status.value == 409 -> {
                throw PromptSyncConflictException()
            }

            response.status.value == 401 -> {
                throw PromptSyncRemoteException("Dropbox rejected the saved access token.")
            }

            else -> {
                throw PromptSyncRemoteException("Dropbox upload failed (${response.status.value}).")
            }
        }
    }

    private fun uploadArg(previousRevision: String?): JsonObject = buildJsonObject {
        put("path", JsonPrimitive(REMOTE_FILE_PATH))
        put(
            "mode",
            previousRevision?.let { revision ->
                buildJsonObject {
                    put(".tag", JsonPrimitive("update"))
                    put("update", JsonPrimitive(revision))
                }
            } ?: JsonPrimitive("add")
        )
        put("autorename", JsonPrimitive(false))
        put("mute", JsonPrimitive(true))
        put("strict_conflict", JsonPrimitive(false))
    }

    private fun bearerToken(accessToken: String): String = "Bearer $accessToken"

    private companion object {
        private const val REMOTE_FILE_PATH = "/Apps/PrompStash/prompts-sync-v1.json"
        private const val DROPBOX_API_ARG_HEADER = "Dropbox-API-Arg"
        private const val DROPBOX_API_RESULT_HEADER = "Dropbox-Api-Result"
        private const val DROPBOX_DOWNLOAD_URL = "https://content.dropboxapi.com/2/files/download"
        private const val DROPBOX_UPLOAD_URL = "https://content.dropboxapi.com/2/files/upload"
    }
}

@Serializable
private data class DropboxSnapshot(
    val version: Int = 1,
    val records: List<RemotePromptRecord> = emptyList(),
)

@Serializable
private data class RemotePromptRecord(
    val id: String,
    val title: String,
    val body: String,
    val tags: List<String> = emptyList(),
    val createdAt: Long,
    val updatedAt: Long,
    val deletedAt: Long? = null,
    val modifiedAt: Long,
    val modifiedByDeviceId: String,
)

private fun RemotePromptRecord.toPromptSyncRecord(): PromptSyncRecord = PromptSyncRecord(
    id = id,
    title = title,
    body = body,
    tags = tags,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    modifiedAt = modifiedAt,
    modifiedByDeviceId = modifiedByDeviceId,
)

private fun PromptSyncRecord.toRemotePromptRecord(): RemotePromptRecord = RemotePromptRecord(
    id = id,
    title = title,
    body = body,
    tags = tags,
    createdAt = createdAt,
    updatedAt = updatedAt,
    deletedAt = deletedAt,
    modifiedAt = modifiedAt,
    modifiedByDeviceId = modifiedByDeviceId,
)
