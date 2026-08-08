# 🛠️ Diagnostics

> View system health and sync status — great for troubleshooting.

---

## 📊 What You See

| Card | What it shows |
|------|---------------|
| 💾 **Offline Storage** | Room database active · local-only |
| 🔄 **Sync Queue** | Number of pending events |
| ✅ **Tasks** | Total task count |
| 🔗 **Devices** | How many devices are paired |
| 📡 **Meshtastic** | Connection status (requires hardware) |
| ☁️ **Google Family Sync** | Internet-gated Family Link status |

---

## 💡 What Does the Sync Queue Do?

Every important change (task created, task approved, wallet credited, chat sent, library page shared, etc.) is stored as a **sync event** in a local queue.

| Status | Meaning |
|--------|---------|
| ⏳ **Pending** | Waiting to be delivered |
| 📤 **Sent** | Sent but not confirmed |
| ✅ **Delivered** | Confirmed received |
| ❌ **Failed** | Delivery failed - will retry |
| 🔄 **Retrying** | Currently retrying |

---

## 🔄 Related

- [Connect Parent Apps](Connect-Parent-Apps) — sync between parent devices
- [Security](Security) — cryptography + signing
- [Google Family Link](Google-Family-Link) — internet-gated sync