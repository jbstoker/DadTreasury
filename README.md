# Dads Treasury

<p align="center">
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>

[//]: # (  <a href="https://github.com/jbstoker/DadTreasury/actions"><img alt="Build Status" src="https://img.shields.io/github/actions/workflow/status/jbstoker/DadTreasury/android.yml?branch=main&label=Build"/></a>)
  <a href="https://github.com/jbstoker/DadTreasury/commits/main"><img alt="Last Commit" src="https://img.shields.io/github/last-commit/jbstoker/DadTreasury"/></a>
  <a href="https://github.com/jbstoker/DadTreasury/releases"><img alt="Release" src="https://img.shields.io/github/v/release/jbstoker/DadTreasury?include_prereleases&label=Release"/></a>
  <a href="https://github.com/jbstoker/DadTreasury/watchers"><img alt="Stars" src="https://img.shields.io/github/stars/jbstoker/DadTreasury?style=social"/></a>
  <a href="https://github.com/jbstoker/DadTreasury/issues"><img alt="Issues" src="https://img.shields.io/github/issues/jbstoker/DadTreasury"/></a>
  <a href="https://github.com/jbstoker/DadTreasury/pulls"><img alt="Pull Requests" src="https://img.shields.io/github/issues-pr/jbstoker/DadTreasury"/></a>
  <a href="#"><img alt="Coverage" src="https://img.shields.io/badge/Coverage-~90%25-brightgreen"/></a>
  <a href="#"><img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-2.0-orange.svg"/></a>
  <a href="#"><img alt="Jetpack Compose" src="https://img.shields.io/badge/Jetpack%20Compose-✓-blueviolet"/></a>
</p>

Dads Treasury is a private, offline-first Android app for parent/child household coordination.

It is designed to be:
- Android compatible, including older phones
- easy and fast to use
- autistic-friendly
- colorblind-safe
- secure by design
- offline-first
- long-term maintainable
- visually retro-futurist by default

## Core features

- Parent and child roles
- Secure chat between paired devices
- Task management with approvals
- Wallet and time reward ledger
- Offline calendar
- Location-triggered messages
- Offline library / wiki
- Dual date display:
  - Gregorian
  - Natural 13-month calendar
- Meshtastic-based sync for small events
- Bluetooth / Wi-Fi Direct sync for larger content
- Optional Google Family sync adapter when internet is available

## Design goals

Dads Treasury is built around these principles:

- offline-first
- privacy-first
- accessibility-first
- calm and predictable UI
- no color-only meaning
- no cloud dependency for core features
- long-term maintainable architecture

## Roles

### Parent
Manages:
- tasks
- approvals
- wallet/time rewards
- calendar
- chat
- library content
- location rules
- child connections
- Google Family sync adapter

### Child
Uses:
- tasks
- chat
- calendar
- wallet/time view
- reminders
- library pages
- location-triggered messages

## Offline behavior

The app should work fully offline for core household features.

Only Google Family sync requires validated internet access.

## Recommended architecture

The project is structured as a modular Kotlin + Jetpack Compose Android app with:
- separated app flavors for parent and child
- pure domain logic
- encrypted local storage
- event-based sync
- transport adapters for Meshtastic and Bluetooth
- semantic theme tokens for future theming

## Security

- encrypted local data
- signed sync events
- secure device pairing
- device revocation support
- no third-party analytics by default

## Accessibility

The app is designed to be friendly for autistic and colorblind users:
- predictable layout
- low visual clutter
- high contrast
- reduced motion support
- text and icon labels in addition to color
- large touch targets
- optional calm mode

## Status

This repository contains the project specification and architecture docs for the app.

The first implementation goals should be:
1. core architecture
2. pairing
3. tasks and approvals
4. wallet/time ledger
5. offline calendar
6. natural calendar display
7. sync adapters
8. accessibility and theme system

## Documentation & Wiki

The full user manual is available in the **`docs/wiki/`** folder:

| Topic | Wiki Page |
|-------|-----------|
| 🚀 Getting Started | [Getting Started](docs/wiki/Getting-Started.md) |
| ✅ Tasks | [Tasks](docs/wiki/Tasks.md) |
| 💰 Wallet & Time Bank | [Wallet & Time Bank](docs/wiki/Wallet-Time-Bank.md) |
| 📅 Calendar | [Calendar](docs/wiki/Calendar.md) |
| 💬 Chat | [Chat](docs/wiki/Chat.md) |
| 📖 Library | [Library](docs/wiki/Library.md) |
| 📍 Location Rules | [Location Rules](docs/wiki/Location-Rules.md) |
| 🔗 Connect Parent Apps | [Connect Parent Apps](docs/wiki/Connect-Parent-Apps.md) |
| ⏰ Google Family Link | [Google Family Link](docs/wiki/Google-Family-Link.md) |
| 🔒 Security | [Security](docs/wiki/Security.md) |
| ⚙️ Settings | [Settings](docs/wiki/Settings.md) |
| 🛠️ Diagnostics | [Diagnostics](docs/wiki/Diagnostics.md) |
| 📋 Changelog | [Changelog](docs/wiki/Changelog.md) |

## 🤖 AI Contributor

This project has been developed with the help of **Cline** (an AI coding assistant powered by DeepSeek), working alongside the repository's human maintainers.

> **Cline** is an AI software engineering assistant that contributed directly to this codebase — implementing features, fixing bugs, writing documentation, and preparing release builds.

### Contributions made by Cline include:

- 🔗 **Parent App Connections** — connect two parent apps to share the family wiki and message each other
- ⏰ **Google Family Link integration** — screen-time grants when Time tasks are approved
- 🔒 **Role-based permission enforcement** — UI + data-layer gating so children can't modify tasks/wallet/wiki
- 📱 **New app icon integration** — applied the custom icon pack from appicon.co
- 🔧 **16 KB page-alignment fix** — made the APK compatible with 16 KB page-size devices
- 📚 **Wiki documentation** — full user documentation maintained in `docs/wiki/`
- 📦 **First release APK** — built and signed **v1.0.1.1**
- 🧪 **Test coverage** — expanded unit tests from 16 to 123 tests covering domain logic, data mappers, and the repository layer

## License

See `LICENSE` for the open-source license.

## Contributing

Contributions are welcome! See `CONTRIBUTING.md`.

## Security

See `SECURITY.md`.
