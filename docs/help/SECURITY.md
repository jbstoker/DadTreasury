# Security & PIN Lock

Dads Treasury keeps your family data private and secure — by default, on your device, not in the cloud.

## What's Protected

- All data stored in an **encrypted SQLCipher database**
- PIN and encryption keys stored in Android Keystore
- Screenshots and screen recording are **blocked**
- Recent-apps preview is hidden

## PIN Lock

Protect quick access to the app with a 4-8 digit PIN.

### Enabling PIN

1. Open **Settings**
2. Scroll to **Security**
3. Tap **Set Up PIN Lock**
4. Enter a 4-8 digit PIN
5. Confirm it
6. Done — the app now requires the PIN on every launch

### Changing / Disabling PIN

1. Open **Settings → Security**
2. Tap **PIN Lock Enabled**
3. Enter your current PIN
4. Set a new PIN or cancel

### What happens with a wrong PIN

- After 5 incorrect attempts, the app shows a locked-out screen
- You must restart the app to try again
- The PIN is checked against a **PBKDF2-HMAC-SHA256 hash** with random salt — never stored in plain text

## Screenshot Blocking

Dads Treasury uses `FLAG_SECURE`, which prevents:
- Screenshots
- Screen recording
- App appearance in recent-apps thumbnails
- Screen mirroring content

## Encrypted Database

The entire Room database (tasks, wallet, chat, calendar, photos, etc.) is encrypted with **SQLCipher**:

- A random 32-character passphrase is generated on first run
- Stored in **EncryptedSharedPreferences** (AES256-GCM)
- The encryption key itself is protected by Android Keystore

## What About Backup?

> ⚠️ **Important:** The database is encrypted with a device-bound key. If Android auto-backup restores the database to a different device, the key won't match. For full control, disable backup in Settings or handle backups via the app's own export tool.

## Best Practices

- Use a PIN that's easy for your family to remember but not obvious
- Enable PIN before handing the phone to someone else
- Revoke paired devices you no longer trust (see Pairing manual)