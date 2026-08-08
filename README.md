# Dads Treasury

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

## License

See `LICENSE` for the open-source license.

## Contributing

See `CONTRIBUTING.md`.

## Security

See `SECURITY.md`.