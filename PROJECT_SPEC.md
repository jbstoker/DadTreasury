f my project# Dad's Treasury Project Specification

## 1. Purpose

Dad's Treasury is a private, offline-first household management app for Android.

It is designed for parent and child use, with secure local coordination of:
- tasks
- approvals
- rewards
- wallet/time balances
- chat
- calendar
- location-triggered messages
- offline library/wiki content

The app must be usable on older Android phones and must be accessible for autistic and colorblind users.

---

## 2. Core principles

### 2.1 Offline-first
Core app functionality must work without internet.

### 2.2 Event-based sync
Small changes should be synced as discrete events rather than as a giant state dump.

### 2.3 Meshtastic for small data
Use Meshtastic for:
- task events
- approval events
- wallet/time reward events
- short chat messages
- reminders

### 2.4 Bluetooth / Wi-Fi Direct for large content
Use local bulk transfer for:
- wiki/library pages
- long text
- backups
- future attachments

### 2.5 Internet-only Google Family adapter
Any Google Family integration must only run when validated internet is available.

### 2.6 Accessibility-first
The UI must be:
- calm
- predictable
- colorblind-safe
- low motion
- low clutter
- readable
- consistent

### 2.7 Maintainable for the long term
The codebase must be structured for at least 10 years of maintenance.

---

## 3. Roles

### 3.1 Parent
Parent can:
- manage tasks
- approve completed tasks
- manage wallet/time rewards
- manage calendar
- manage chat
- manage library pages
- define location rules
- manage multiple children
- control pairing and device access
- manage Google Family sync adapter

### 3.2 Child
Child can:
- see tasks
- complete tasks
- view wallet/time rewards
- view calendar
- chat with parent
- read library pages
- receive reminders and location-triggered messages

### 3.3 Relationship rule
- One child has one parent
- One parent can have multiple children

---

## 4. App structure

Use two app flavors or two app targets:
- `parent`
- `child`

The codebase should be one shared Kotlin project with separated feature logic and role-specific UI.

### Recommended modules
- `core:domain`
- `core:data`
- `core:security`
- `core:sync`
- `core:designsystem`
- `core:accessibility`
- `core:naturecalendar`
- `feature:pairing`
- `feature:home`
- `feature:tasks`
- `feature:wallet`
- `feature:calendar`
- `feature:chat`
- `feature:library`
- `feature:location`
- `feature:familysync`
- `feature:settings`
- `feature:diagnostics`

---

## 5. Data model

The app should keep a local encrypted database as the source of truth.

### Core entities
- Household
- ParentProfile
- ChildProfile
- DeviceIdentity
- PairingLink
- Task
- TaskCompletion
- TaskApproval
- WalletTransaction
- TimeBankTransaction
- CalendarEvent
- ChatThread
- ChatMessage
- GeoRule
- LibraryCategory
- LibraryPage
- LibraryRevision
- SyncEvent
- SyncQueueItem
- AuditLog

---

## 6. Wallet and time bank

The wallet and time bank must be ledger-based.

### Important rule
Do not store the balance only as a single mutable number.

### Instead
Store transaction entries such as:
- reward credited
- reward debited
- payout
- correction
- reversal

Then compute the balance from the ledger.

---

## 7. Task system

Each task should contain:
- ID
- title
- description
- expected duration
- due date / time slot
- reward type
- reward amount
- status
- approval state
- optional notes
- optional location rule
- optional checklist

### Reward types
- Free
- Paid
- Time

### Flow
1. Parent creates task
2. Child sees task
3. Child marks task complete
4. Parent approves task
5. Reward is created immediately
6. Reward event is synced

---

## 8. Chat

Chat should be:
- secure
- text-first
- short-message optimized
- app-to-app only

Meshtastic should be used as the transport for chat messages.

Messages must be encrypted and signed at the app layer.

---

## 9. Calendar

The calendar is local and offline.

Features:
- events
- reminders
- recurring items
- routines
- agenda view
- week view
- child-friendly timeline view

No external calendar sync by default.

---

## 10. Location messages

Location rules are local reminders triggered by coordinates.

A location rule should contain:
- title
- message
- latitude
- longitude
- radius
- active hours
- repeat behavior
- enabled state

When the child enters the location radius, the app can:
- show a notification
- show a message
- create a reminder
- trigger a task-related note

---

## 11. Library / wiki

The library is a private offline knowledge base.

### Features
- categories
- pages
- revisions
- search
- tags
- simple and detailed views

### Sync
Use Bluetooth / Wi-Fi Direct / file transfer for larger library content.

---

## 12. Dual calendar display

The app should show:
- Gregorian date/time in black/default
- Natural 13-month date/time in green

### Rule
Gregorian remains the internal truth for storage and logic.

The natural calendar is a derived display layer.

### Recommended natural calendar
- 13 months
- 28 days each
- 364 days total
- 1 Year Day outside the months
- 1 leap day in leap years

### Display example
- Gregorian: Tue, 8 Aug 2026 14:30
- Nature: 🌿 Leaf 11, Year 8

Use labels and icons in addition to color.

---

## 13. Retro-futurist theme

The default design language should be retro-futurist.

### Visual style
- dark graphite base
- neon accents
- subtle glow
- sci-fi panels
- clean spacing
- low motion
- readable typography

### Theme system
Do not hardcode colors in screens.

Use semantic design tokens such as:
- background
- surface
- textPrimary
- textSecondary
- accentPrimary
- accentSecondary
- success
- warning
- error

This makes future themes easy to add.

---

## 14. Accessibility requirements

The app must be easy and fast to use for autistic and colorblind people.

### Must-have accessibility rules
- no color-only meaning
- no hidden gestures as the only way
- predictable layouts
- large tap targets
- consistent navigation
- reduced motion option
- high contrast option
- text size support
- calm mode
- readable labels
- optional text-to-speech support

### Child UX
The child interface should be especially simple:
- show today's tasks first
- show next action clearly
- reduce choices per screen
- keep screens uncluttered

---

## 15. Meshtastic sync

Meshtastic should be used for:
- tasks
- approvals
- wallet/time events
- short chat messages
- reminders

### Requirements
- keep a local outbox and inbox
- use message IDs for deduplication
- encrypt payloads
- sign events
- retry delivery
- keep local storage as source of truth

### Not suitable for
- images
- audio
- large wiki pages
- bulk backups
- rich media

---

## 16. Bluetooth / Wi-Fi Direct bulk sync

Use local bulk transfer for:
- library/wiki pages
- long text
- backups
- imports
- exports

This is separate from Meshtastic and should not overload the mesh transport.

---

## 17. Google Family adapter

If the app integrates with Google Family features:
- it must require validated internet
- it must be blocked when offline
- it must queue pending actions locally
- it must retry when internet returns

### Important note
The app should not depend on Google Family to function.

The Google integration is an optional online adapter.

---

## 18. Security and privacy

### Must-haves
- encrypted local storage
- signed sync events
- secure pairing
- device revocation
- audit logs
- no analytics by default
- no third-party tracking by default

### Pairing flow
1. Parent creates pairing QR
2. Child scans QR
3. Keys are exchanged
4. Parent approves
5. Devices become trusted

---

## 19. Maintainability for 10 years

### Architectural rules
- domain logic must be pure Kotlin
- transport logic must be behind interfaces
- feature modules must stay isolated
- add tests for core behavior
- keep dependencies minimal
- document public APIs and architecture decisions
- support migrations carefully
- version sync protocols

### Data longevity
- use stable IDs
- store event history for important actions
- avoid putting business logic only in UI state
- keep export/import support in mind early

---

## 20. Testing requirements

### Unit tests
Test:
- ledger calculations
- approval flow
- sync state machine
- calendar conversion
- deduplication
- internet gating
- role logic

### Integration tests
Test:
- database migrations
- sync queue handling
- transport adapter behavior
- offline-to-online retry flow

### UI tests
Test:
- task creation
- task completion
- approval
- wallet display
- natural calendar display
- theme switching
- accessibility semantics

---

## 21. Build phases

### Phase 1
- architecture
- roles
- pairing
- encrypted local DB
- theme system
- diagnostics
- accessibility foundations

### Phase 2
- tasks
- approvals
- wallet
- time bank
- calendar
- nature calendar display

### Phase 3
- Meshtastic sync
- chat
- event queue
- encryption
- deduplication

### Phase 4
- location rules
- reminders
- notifications

### Phase 5
- library/wiki
- bulk transfer
- backups

### Phase 6
- Google Family adapter
- internet gating
- queued sync

### Phase 7
- theme editor
- polish
- performance tuning
- documentation cleanup

---

## 22. Definition of done

A feature is only done if:
- it works offline where applicable
- it has tests
- it is accessible
- it respects theme tokens
- it does not leak business logic into composables
- it is documented
- it is maintainable
- it does not break older devices unnecessarily

---

## 23. Open-source notes

If publishing publicly:
- choose a license
- document privacy clearly
- document offline behavior clearly
- document what data syncs and how
- document limitations of external integrations

Recommended additional files:
- README.md
- ARCHITECTURE.md
- ROADMAP.md
- CONTRIBUTING.md
- SECURITY.md
- CODE_OF_CONDUCT.md
- LICENSE