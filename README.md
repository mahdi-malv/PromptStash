# Prompt Stash
A compose multiplatform app to let the user save useful prompts

Platform: Android, Desktop, and soon iOS

|Screen1|Screen2|Widget|
|--|--|--|
|<img width=400 src="https://github.com/user-attachments/assets/d467a751-c239-4bdf-ac6d-75339bb43ead" />|<img width=400 src="https://github.com/user-attachments/assets/b1cf3581-d421-428b-8aae-eeb89500fdb0" />|<img width=400 src="https://github.com/user-attachments/assets/44ee3e93-a422-4026-ad67-cc2249ec4c3a" />|


## Tasks
- [ ] CI deploy (dropbox personal key needed!)
- [x] Local save/retrieve prompts
- [x] Basic tags
- [x] Theme settings
- [x] Sync (Dropbox)
- [x] Android homescreen widget
- [ ] UI polish (Font, ColorSystem, so on)
- [ ] iOS target


## Sync
You can use Dropbox storage to sync your prompts between your platforms.  
To do that, create a Dropbox app in the [Dropbox App Console](https://www.dropbox.com/developers/apps) with:

- API: `Scoped access`
- Access: `App folder`
- Scopes:
  - `account_info.read`
  - `files.content.read`
  - `files.content.write`
- Redirect URIs:
  - `prompstash://dropbox/auth`
  - `http://127.0.0.1:53682/dropbox/auth`

Then add the app key to `local.properties`:

```properties
dropbox.app.key=YOUR_DROPBOX_APP_KEY
```

Then in PrompStash:

1. Open Settings
2. Select `Dropbox`
3. Tap `Auth`

If you change scopes or redirect URIs later, remove auth in the app and authenticate again.
