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

#### 🗺️ Location Map Picker
- Coordinate fields replaced with an interactive **osmdroid map** (OpenStreetMap, no Google Play Services)
- Address search, contact picker, draggable marker, tap-to-pick
- Location rules can notify **Child** or **Parent (personal)**

#### 🌐 Language Selection
- New **Language** section in Settings (System, English, Nederlands, Deutsch, Español, Français, 中文, Frysk)
- App recreates fully when language changes so all localized strings are reloaded

#### 💬 Chat Parent Switching
- Parents can switch between the child and connected **parent apps** in one screen
- Location pins render inside chat messages containing 📍 coordinates

#### 📖 Library Rich Text & XML
- Formatting toolbar (headings, bold, italic, lists, images)
- Live preview
- XML **import/export** of categories and pages

#### 👨‍👧 Task Child Assignment
- Tasks can be assigned to a specific child when multiple children exist
- Child name shown on task cards; rewards credit the assigned child

#### 🆘 SOS Button
- Quick-alert screen with 5 predefined messages plus the device location
- Big red button sends the message directly to the parent's chat with a location pin

#### 🌈 My Boundaries & Choices
- **Step-by-step wizard** (one question at a time) for neurodivergent children
- Energy check → motivation → role comfort → practical boundaries
- Early exits for empty battery or pressured/forced
- Generates a **Public Text** and **Home Team Action** message with copy/send buttons

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