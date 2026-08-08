# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0] - 2026-08-08

### Added

#### Core App
- Offline-first, privacy-first household coordination app (RetroNest)
- Parent/child role selection at onboarding
- Encrypted local database using SQLCipher with device-bound key in EncryptedSharedPreferences
- PIN lock for quick access (4-8 digits, PBKDF2-HMAC-SHA256 hash, 5-attempt lockout)
- FLAG_SECURE blocks screenshots, screen recording, and recent-apps preview
- Runtime permission requests for location and notifications

#### Tasks
- Task creation with reward type (Free / Paid / Time)
- Task completion flow with optional completion photo (camera via FileProvider)
- Parent approval/rejection flow
- Automatic reward ledger creation on approval

#### Wallet & Time Bank
- Ledger-based wallet and time bank (credits, debits, payouts, corrections, reversals)
- Balance computed from transaction history
- Manual transaction entry

#### Calendar
- Offline calendar events with reminders
- Dual date display: Gregorian + Natural 13-month calendar
- AlarmManager-based time reminders (no Google Play Services)

#### Location
- Native proximity alerts using LocationManager.addProximityAlert (no Play Services)
- Location rule editor with place-name search, radius presets, active hours
- Contact address import from ContactsContract
- Smart multi-match geocoding (brand + area bounding box)
- Notification actions: Done / Disable

#### Chat
- Secure local parent-child messaging (stored in encrypted database)

#### Library
- Offline wiki with categories, pages, tags, and revisions

#### Widgets
- Home screen tasks widget (RemoteViewsService)

#### Internationalization
- English (default)
- Dutch (nl)
- Frisian (fy)
- German (de)
- French (fr)
- Spanish (es)
- Chinese (zh)

#### Documentation
- Full user manuals in `docs/help/`
- README, PROJECT_SPEC, ARCHITECTURE, ROADMAP, CONTRIBUTING, SECURITY, CODE_OF_CONDUCT
- Apache 2.0 license

#### Theming
- Retro-futurist default theme
- High Contrast, Calm, and Nature themes
- Semantic design tokens
- Accessibility options: calm mode, reduced motion, high contrast, text scale

### Security
- Encrypted Room database (SQLCipher)
- EncryptedSharedPreferences for PIN and DB passphrase
- Screenshot/recording blocking
- No Google Play Services dependencies
- No third-party analytics or tracking