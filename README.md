# Prompt Stash
A compose multiplatform app to let the user save useful prompts. Supports syncing through `Dropbox`

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

PromptStash ships with its own Dropbox app, so you don't need to create one. Just:

1. Open Settings
2. Select `Dropbox`
3. Tap `Auth` and authorize PromptStash against your Dropbox account

Tokens are stored securely on-device (EncryptedSharedPreferences on Android, Keychain on macOS) and cleared when you disconnect.
