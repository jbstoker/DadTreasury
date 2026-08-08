hat# RetroNest Architecture

## 1. Overview

RetroNest is a modular Android application built with Kotlin and Jetpack Compose.

The app is designed to be:
- offline-first
- secure
- accessible
- maintainable
- role-based
- event-driven
- extensible for future themes and sync transports

---

## 2. Key architecture choices

### 2.1 One codebase, two roles
Use one shared codebase with:
- parent flavor
- child flavor

This keeps the project maintainable while allowing different user experiences.

### 2.2 Feature modules
Each major capability is isolated into its own module.

### 2.3 Clean separation of concerns
- UI layer: Compose screens and state
- domain layer: business rules
- data layer: repositories and persistence
- sync layer: transport adapters
- security layer: encryption and trust
- design system: theming and components

### 2.4 Local-first source of truth
Each device stores a full encrypted local database.

Remote or mesh sync is only for event exchange and replication.

---

## 3. Proposed module layout

```text
:app-parent
:app-child

:core:domain
:core:data
:core:security
:core:sync
:core:designsystem
:core:accessibility
:core:naturecalendar
:core:utils

:feature:pairing
:feature:home
:feature:tasks
:feature:wallet
:feature:calendar
:feature:chat
:feature:library
:feature:location
:feature:familysync
:feature:settings
:feature:diagnostics
```

---

## 4. Layer responsibilities

### 4.1 UI layer
Responsibilities:
- display state
- collect user input
- show accessibility-friendly controls
- navigate between screens

The UI layer must not contain business rules.

### 4.2 Domain layer
Responsibilities:
- task logic
- reward logic
- wallet and time bank logic
- approval rules
- calendar conversion logic
- sync event semantics

The domain layer must be pure Kotlin and should not depend on Android classes.

### 4.3 Data layer
Responsibilities:
- database access
- repositories
- persistence
- caching
- storage migrations

### 4.4 Sync layer
Responsibilities:
- Meshtastic transport
- Bluetooth / Wi-Fi Direct transfer
- local event queue
- deduplication
- retries
- acknowledgments
- protocol versioning

### 4.5 Security layer
Responsibilities:
- key handling
- encryption
- signatures
- trust management
- pairing support
- device revocation

### 4.6 Design system
Responsibilities:
- typography
- spacing
- shape
- colors
- token definitions
- accessible component styles
- theme switching

---

## 5. Data model design

### 5.1 Important principle
Use a ledger and event history for business-critical changes.

### 5.2 Wallet and time bank
Do not store only a raw balance.

Store transactions:
- credits
- debits
- payout
- correction
- reversal

Compute the current balance from the transaction set.

### 5.3 Sync events
Important changes should be represented as events.

Example event types:
- TaskCreated
- TaskUpdated
- TaskCompleted
- TaskApproved
- WalletCredited
- WalletDebited
- ChatMessageSent
- CalendarEventCreated
- GeoRuleCreated
- LibraryPageUpdated

---

## 6. Sync architecture

### 6.1 Transport abstraction
Define transport interfaces so the app can support:
- Meshtastic
- Bluetooth
- Wi-Fi Direct
- future transports

### 6.2 Event queue
Each device should maintain:
- outbox
- inbox
- deduplication records
- acknowledgment state
- retry state

### 6.3 Local storage as truth
The local database is the source of truth.

Sync only exchanges events and content updates.

### 6.4 Deduplication
Each sync event must have:
- unique ID
- sender ID
- recipient ID
- timestamp
- revision
- signature

Repeated delivery should not duplicate changes.

### 6.5 Conflict strategy
For data that can be edited from multiple devices:
- use revisions
- use last-write or explicit merge rules
- prefer clear conflict resolution UX

---

## 7. Meshtastic usage

Use Meshtastic for:
- short secure chat
- task changes
- approvals
- wallet/time reward events
- reminder signals

Do not use Meshtastic for:
- large media
- big wiki pages
- heavy file transfers
- bulk backups

Meshtastic should be treated as a low-bandwidth event transport.

---

## 8. Bluetooth / Wi-Fi Direct usage

Use bulk local transfer for:
- wiki pages
- long notes
- backups
- import/export
- larger content synchronization

This avoids overloading the mesh transport and keeps the daily app flow fast.

---

## 9. Google Family adapter

The Google Family integration must be separated into its own adapter.

### Rules
- only run with validated internet
- block when offline
- queue pending sync
- retry automatically

### Architecture note
The app should remain fully usable without Google Family integration.

---

## 10. Calendar architecture

### 10.1 Gregorian calendar
Gregorian timestamps are the internal truth.

Store:
- UTC timestamp
- timezone
- local display data as needed

### 10.2 Natural calendar
The natural 13-month calendar should be derived from Gregorian timestamps.

### 10.3 Dual display
Show both in the UI:
- Gregorian in default black
- Natural calendar in green

### 10.4 Conversion layer
Implement calendar conversion in a dedicated domain utility.

Keep conversion deterministic and testable.

---

## 11. Theming architecture

### 11.1 Default theme
Retro-futurist by default.

### 11.2 Token-based theming
Use semantic theme tokens such as:
- background
- surface
- card
- border
- textPrimary
- textSecondary
- accentPrimary
- accentSecondary
- success
- warning
- error

### 11.3 Future themes
The theme system should allow future themes without rewiring feature screens.

### 11.4 Accessibility compatibility
Every theme must support:
- sufficient contrast
- reduced motion
- readable typography
- colorblind-safe distinction

---

## 12. Accessibility architecture

Accessibility is not an afterthought.

### Required support
- semantic labels
- content descriptions
- predictable focus order
- clear button labels
- text size scaling
- reduced motion
- high contrast
- calm layouts
- no color-only status indicators

### Child UX
The child UI should prefer:
- fewer choices
- larger buttons
- clearer steps
- less visual clutter
- consistent screen structure

---

## 13. Security architecture

### 13.1 Local data encryption
Store all sensitive data in encrypted local storage.

### 13.2 App-layer message encryption
Sync payloads and chat messages should be encrypted at the application layer.

### 13.3 Signed events
Events should be signed to prevent tampering.

### 13.4 Pairing trust
Pairing should establish a secure trusted relationship between devices.

### 13.5 Revocation
The parent must be able to revoke a child device or remove trust.

---

## 14. Screen architecture

### Parent app screens
- Dashboard
- Children
- Child detail
- Tasks
- Approvals
- Wallet
- Time bank
- Calendar
- Chat
- Location rules
- Library
- Pairing
- Diagnostics
- Settings

### Child app screens
- Home
- Today's tasks
- Task detail
- Chat
- Calendar
- Wallet
- Library
- Notifications
- Settings

The child app should stay intentionally simpler.

---

## 15. Long-term maintainability rules

### 15.1 No business logic in composables
Compose screens should only render state and emit user events.

### 15.2 Pure domain rules
Business rules must be testable without Android runtime.

### 15.3 Avoid dependency bloat
Only add a dependency if it solves a real problem and is justified.

### 15.4 Versioned protocol
Sync payloads should be versioned from the beginning.

### 15.5 Database migrations
Every schema migration should be tested.

### 15.6 Documentation
Document:
- architecture decisions
- module responsibilities
- sync protocol
- data model
- external integration limitations

---

## 16. Testing strategy

### Unit tests
Test:
- reward calculations
- wallet ledger balance
- time ledger balance
- calendar conversion
- internet gating
- sync deduplication
- approval flow

### Integration tests
Test:
- database migrations
- repository behavior
- queue persistence
- sync retry behavior

### UI tests
Test:
- key workflows
- accessibility semantics
- theme rendering
- offline states

---

## 17. Recommended implementation phases

### Phase 1
- architecture skeleton
- design system
- roles
- pairing
- local encryption

### Phase 2
- task system
- approvals
- wallet/time bank
- calendar
- natural calendar display

### Phase 3
- Meshtastic sync
- chat
- event queue
- deduplication

### Phase 4
- location rules
- notifications

### Phase 5
- library/wiki
- bulk transfer

### Phase 6
- Google Family adapter
- internet gating

### Phase 7
- theme expansion
- performance tuning
- docs and polish

---

## 18. Definition of done

A feature is only complete if:
- it works offline where required
- it passes tests
- accessibility is included
- theme tokens are used
- domain logic is not embedded in UI
- the architecture remains clean
- the documentation is updated