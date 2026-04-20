# Prompt Stash
A compose multiplatform app to let the user save useful prompts

## Features
- [x] Local save/retrieve prompts
- [x] Basic tags
- [x] Theme settings
- [x] Sync
- [ ] Android homescreen widget
- [ ] iOS target

## Dropbox setup
Create a Dropbox app in the [Dropbox App Console](https://www.dropbox.com/developers/apps) with:

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
