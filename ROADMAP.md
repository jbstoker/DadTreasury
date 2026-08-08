# ReacootroNest Roadmap

## Phase 0 — Preparation
- finalize specifications
- define module boundaries
- define data model
- define sync event format
- define theme tokens
- define accessibility rules

---

## Phase 1 — Foundation
Deliverables:
- project structure
- app flavors for parent and child
- encrypted local database
- pairing flow
- device trust model
- diagnostics screen
- design system foundations
- accessibility foundations

Exit criteria:
- app opens cleanly
- role separation works
- pairing works locally
- local storage is encrypted
- basic navigation exists

---

## Phase 2 — Tasks and Rewards
Deliverables:
- task creation
- task editing
- task completion
- task approval
- reward creation
- wallet ledger
- time ledger
- task history

Exit criteria:
- parent can create tasks
- child can complete tasks
- approval creates reward transactions
- balances are computed from ledgers

---

## Phase 3 — Calendar
Deliverables:
- offline calendar
- recurring events
- reminders
- agenda view
- week view
- child-friendly timeline view
- dual calendar display

Exit criteria:
- Gregorian and natural dates both display correctly
- calendar works fully offline
- conversion logic has tests

---

## Phase 4 — Meshtastic Sync
Deliverables:
- sync event model
- outbox/inbox
- deduplication
- encryption
- signing
- retries
- task/approval/wallet sync
- short message sync

Exit criteria:
- small events can move across devices
- duplicates are ignored safely
- offline queue persists correctly

---

## Phase 5 — Chat and Notifications
Deliverables:
- secure text chat
- message history
- notification handling
- reminder delivery
- calm notification UX

Exit criteria:
- parent and child can exchange secure short messages
- messages remain readable and accessible

---

## Phase 6 — Location Messages
Deliverables:
- location rule editor
- map selector
- radius rules
- trigger notifications
- task-trigger integration

Exit criteria:
- location-triggered notifications work offline
- location rules can be created and edited reliably

---

## Phase 7 — Library / Wiki
Deliverables:
- categories
- pages
- revisions
- search
- simple and detailed reading mode
- bulk sync

Exit criteria:
- pages can be created, edited, and transferred locally
- large content does not overload Meshtastic

---

## Phase 8 — Google Family Adapter
Deliverables:
- internet validation
- offline blocking
- pending queue
- sync retry
- local-to-online reward bridge

Exit criteria:
- sync only runs when internet is validated
- offline state keeps rewards pending locally

---

## Phase 9 — Theme Expansion
Deliverables:
- retro-futurist default theme
- high contrast theme
- calm theme
- nature theme
- future theme extension support

Exit criteria:
- themes can be added without changing business logic
- accessibility remains intact across themes

---

## Phase 10 — Long-Term Stability
Deliverables:
- performance tuning
- migration testing
- compatibility testing
- docs cleanup
- sample data
- release process
- versioned sync protocol documentation

Exit criteria:
- app is stable on older devices
- data migrations are safe
- docs are complete enough for new contributors

---

## Ongoing maintenance priorities
- keep the UI calm and predictable
- keep business logic in domain modules
- keep transport adapters isolated
- keep tests updated with every change
- keep accessibility from regressing
- keep the design system token-based
- keep sync protocol versioned

---

## Future ideas
These should only be added if the core app remains stable:
- voice notes
- richer attachments
- advanced reports for parents
- more theme packs
- optional family summaries
- optional export/import tooling
