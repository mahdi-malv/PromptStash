package com.mahdimalv.promptstash.data.sync

/**
 * Product-level Dropbox OAuth configuration shared by every user of the app.
 *
 * The client ID is the public identifier of the single Dropbox app registered for
 * PromptStash. It is safe to ship in a public client because the OAuth flow uses
 * PKCE and never sends a client secret. Do NOT add a client secret here.
 *
 * To rotate the Dropbox app, update [CLIENT_ID] in one place and ship a new build.
 */
internal object DropboxAppConfig {
    const val CLIENT_ID: String = "6u3d83uu0bgbnqk"
}
