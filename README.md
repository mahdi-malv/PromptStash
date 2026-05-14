# Prompt Stash
A compose multiplatform app to let the user save useful prompts

Platform: Android, Desktop, and soon iOS

|Dark|Light|
|--|--|
|<img width="500"  alt="image" src="https://github.com/user-attachments/assets/108202e2-8c3f-44e6-9fc0-c6493918c090" />|<img width="500"  alt="image" src="https://github.com/user-attachments/assets/fb499d90-8bd0-42e5-8391-e107f9cdfaee" />|
|<img width="237" alt="image" src="https://github.com/user-attachments/assets/f4a80f92-07f8-41bf-afa8-9a282de671c6" />|<img width="249"  alt="image" src="https://github.com/user-attachments/assets/5c298254-aca6-4139-8de0-ef5f6116630d" />|



## Tasks
- [ ] CI deploy (dropbox personal key needed!)
- [x] Local save/retrieve prompts
- [x] Basic tags
- [x] Theme settings
- [x] Sync (Dropbox)
- [x] Android homescreen widget
- [x] UI polish (Font, ColorSystem, so on)
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
  - `promptstash://dropbox/auth`
  - `http://127.0.0.1:53682/dropbox/auth`

Then add the app key to `local.properties`:

```properties
dropbox.app.key=YOUR_DROPBOX_APP_KEY
```

Then in PromptStash:

1. Open Settings
2. Select `Dropbox`
3. Tap `Auth`

If you change scopes or redirect URIs later, remove auth in the app and authenticate again.
