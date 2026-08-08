# Diagnostics

Diagnostics shows how the app's systems are doing — like a health check for RetroNest.

## What It Shows

| Section | Meaning |
|---------|---------|
| 🗄️ Offline Storage | Local encrypted Room database active |
| 🔄 Sync Queue | How many events are waiting to sync |
| ✅ Tasks | Total number of tasks |
| 🔗 Devices | How many devices are paired |
| 📡 Meshtastic | Transport status (requires hardware) |
| ☁️ Google Family | Not configured (requires internet) |

## Sync Queue

RetroNest uses event-based sync. When something changes (task created, reward credited), a **sync event** is queued locally.

- Pending events are stored offline
- They wait for a transport (Meshtastic/Bluetooth) to become available
- Repeated events are deduplicated by ID
- The local database is always the source of truth

## Why Check Diagnostics?

- Confirm the encrypted database is active
- See if any events are stuck in the sync queue
- Verify paired devices
- Understand what's offline vs online

Everything shown here is local — no data leaves the device.