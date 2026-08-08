# 🔒 Security & PIN

> Dad's Treasury protects your family data with multiple layers of local security.

---

## 🛡️ Security Layers

| Layer | Description |
|-------|-------------|
| 🔐 **Encrypted database** | SQLCipher encryption for all stored data |
| 🗝️ **Encrypted preferences** | EncryptedSharedPreferences for PIN + DB key |
| 📵 **No cloud** | All data stays on-device |
| 🚫 **No tracking** | No third-party analytics or tracking |
| 🖼️ **FLAG_SECURE** | Blocks screenshots + screen recording |

---

## 🔢 Setting Up a PIN

1. Open **Settings**
2. Scroll to **Security**
3. Tap **Set Up PIN Lock**
4. Enter a 4–8 digit PIN
5. Confirm it
6. Done! The app now requires the PIN to open

> ⚠️ **Important:** The PIN is hashed (PBKDF2-HMAC-SHA256). If you forget it, the app locks after 5 failed attempts and requires a restart.

---

## 🔐 Changing / Disabling the PIN

1. Open **Settings**
2. Scroll to **Security**
3. Tap **PIN Lock Enabled**
4. Enter your current PIN, then set a new one

---

## 💾 Data Storage

| Data | Storage |
|------|---------|
| Tasks, wallet, chat, wiki | Encrypted SQLCipher Room database |
| PIN + DB passphrase | EncryptedSharedPreferences |
| Sync queue | Encrypted local database |

---

## 🔄 Related

- [Settings](Settings)
- [Connect Parent Apps](Connect-Parent-Apps) — secure pairing
- [Getting Started](Getting-Started)