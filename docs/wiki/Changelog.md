# 📋 Changelog

> All notable changes are documented here, following [Keep a Changelog](https://keepachangelog.com/) and [Semantic Versioning](https://semver.org/).

---

## [Unreleased]

### ✨ Added

#### 🔗 Parent App Connections
- Connect **two parent apps** via a 6-character pairing code
- Share **wiki pages** between parent apps
- Send **parent-to-parent messages**
- New `app_connections` and `shared_library_pages` database tables
- New sync event types

#### ⏰ Google Family Link Screen-Time Grants
- When a **Time** task is approved, screen time is granted via Google Family Link
- Requires validated internet (queues when offline)
- Retries automatically when connectivity returns
- Opens Family Link so the parent can approve in one tap

#### 🔒 Role-Based Permissions
- Children **cannot** add tasks, wallet funds, or edit the wiki
- Parent-only write methods are enforced at both **UI** and **data layer**

#### 📍 Locations for Children
- Children can now see **Locations** in their home nav grid

---

## [0.1.0] — 2026-08-08

### 🌿 Core App
- Offline-first, privacy-first household coordination app
- Parent/child role selection at onboarding
- Encrypted local database (SQLCipher)
- PIN lock (4-8 digits, PBKDF2-HMAC-SHA256, 5-attempt lockout)
- FLAG_SECURE blocks screenshots and recording

### ✅ Tasks
- Task creation with reward types (Free / Paid / Time)
- Completion flow with optional photo
- Parent approval/rejection
- Automatic reward ledger creation

### 💰 Wallet & Time Bank
- Ledger-based wallet and time bank
- Balance computed from transaction history

### 📅 Calendar
- Offline events with reminders
- Dual date display (Gregorian + Natural 13-month)

### 📍 Location
- Native proximity alerts (no Play Services)
- Smart geocoding + contact address import

### 💬 Chat
- Secure local parent-child messaging

### 📖 Library
- Offline wiki with categories, pages, tags, revisions

### 🎨 Widgets
- Home screen tasks widget

### 🌐 Internationalization
- English, Dutch, Frisian, German, French, Spanish, Chinese

---

🔗 [Back to Home](Home)