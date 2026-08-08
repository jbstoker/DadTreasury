# Security Policy

## 1. Purpose

RetroNest handles sensitive household data, including:
- child-related tasks
- private chat
- calendar data
- location rules
- wallet and time balances
- device pairing and trust data

Security is therefore a core design requirement.

---

## 2. Security principles

- minimize data exposure
- encrypt sensitive local data
- sign sync events
- keep core features offline-first
- isolate external integrations
- use explicit pairing and trust
- support device revocation
- avoid unnecessary third-party services

---

## 3. Supported versions

Security support should follow the active stable release line.

Maintain a clear versioning policy in the repository.

---

## 4. Reporting vulnerabilities

If you believe you found a vulnerability, please report it privately.

Include:
- summary of the issue
- affected version
- affected device or platform details
- steps to reproduce
- impact
- proof of concept if safe

Do not publish active exploits publicly before a fix is available.

---

## 5. What to report

Report issues such as:
- unauthorized access
- broken pairing trust
- message injection
- sync event tampering
- data leakage
- encrypted storage bypass
- permission mistakes
- unsafe Google Family handling
- location rule abuse

---

## 6. What the app should protect

Sensitive data should include:
- child profiles
- parent-child relationship data
- task history
- reward balances
- chat history
- calendar data
- location rules
- paired device identities
- audit logs

---

## 7. Security controls

### 7.1 Encrypted local storage
All sensitive data should be encrypted on device.

### 7.2 Signed events
Sync events should be signed to reduce tampering risk.

### 7.3 End-to-end encrypted messages
Chat and sync payloads should be encrypted at the app layer.

### 7.4 Secure pairing
Pairing should use a secure flow such as QR-based setup.

### 7.5 Device revocation
The parent must be able to revoke device trust.

### 7.6 Internet gating
Google Family sync must only run when validated internet is available.

---

## 8. Third-party integrations

If the app integrates with external systems:
- keep them isolated behind adapters
- do not allow them to become required for offline core use
- document any limitations clearly

---

## 9. Responsible disclosure

We request that security issues be disclosed responsibly.

Please give maintainers enough time to investigate and fix issues before public disclosure.

---

## 10. Security expectation for contributors

If you contribute code:
- avoid logging sensitive data
- avoid plaintext secrets
- avoid insecure temporary storage
- avoid trusting transport layer security alone
- avoid skipping validation for convenience

---

## 11. Thanks

Thank you for helping keep RetroNest safe for families.