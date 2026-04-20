package com.mahdimalv.prompstash.data.sync

import com.mahdimalv.prompstash.data.settings.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PromptSyncCoordinator(
    private val localStore: PromptSyncLocalStore,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val secureCredentialStore: SecureCredentialStore,
    remotes: List<PromptSyncRemote>,
    private val clock: () -> Long = { System.currentTimeMillis() },
) : PromptSyncStore {

    private val remotesByType = remotes.associateBy(PromptSyncRemote::remoteType)
    private val syncMutex = Mutex()
    private val _isSyncing = MutableStateFlow(false)

    override val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    override suspend fun sync(trigger: SyncTrigger): SyncResult = syncMutex.withLock {
        _isSyncing.value = true
        try {
            val remoteType = userPreferencesRepository.remoteType.first()
            if (remoteType == RemoteType.NONE) {
                return SyncResult.Skipped("Select Dropbox in Settings to enable sync.")
            }

            val remote = remotesByType[remoteType]
                ?: return SyncResult.Failure("The selected sync remote is not available in this build.")

            val accessToken = secureCredentialStore.readAccessToken(remoteType)
            if (accessToken.isNullOrBlank()) {
                return SyncResult.Skipped("Add your Dropbox access token in Settings before syncing.")
            }

            runSync(remote, accessToken)
            val liveRecordCount = localStore.getAllRecords().count { !it.isDeleted }
            val successMessage = "Synced $liveRecordCount prompts"
            userPreferencesRepository.recordSyncSuccess(clock(), successMessage)
            return SyncResult.Success(
                message = successMessage,
                syncedRecordCount = liveRecordCount,
            )
        } catch (error: PromptSyncRemoteException) {
            val message = error.message ?: "Sync failed."
            userPreferencesRepository.recordSyncFailure(clock(), message)
            return SyncResult.Failure(message)
        } catch (error: PromptSyncConflictException) {
            val message = error.message ?: "The remote changed while syncing. Please retry."
            userPreferencesRepository.recordSyncFailure(clock(), message)
            return SyncResult.Failure(message)
        } catch (error: Exception) {
            val message = error.message ?: "Sync failed."
            userPreferencesRepository.recordSyncFailure(clock(), message)
            return SyncResult.Failure(message)
        } finally {
            _isSyncing.value = false
        }
    }

    private suspend fun runSync(
        remote: PromptSyncRemote,
        accessToken: String,
    ): String {
        val firstSnapshot = remote.download(accessToken)
        val firstMerged = localStore.mergeRemoteRecords(firstSnapshot.records)
        return try {
            remote.upload(accessToken, firstMerged, firstSnapshot.revision)
        } catch (_: PromptSyncConflictException) {
            val retrySnapshot = remote.download(accessToken)
            val retryMerged = localStore.mergeRemoteRecords(retrySnapshot.records)
            remote.upload(accessToken, retryMerged, retrySnapshot.revision)
        }
    }
}
