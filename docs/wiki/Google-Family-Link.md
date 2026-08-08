# ⏰ Google Family Link

> When a child completes a **Time** task and it is approved, Dad's Treasury grants **more screen/app time** via Google Family Link.

---

## 🎯 What Happens When a Time Task is Approved?

1. ✅ The task is marked **Approved**
2. ⏰ The **Time Bank** is credited with the reward minutes
3. 🌐 Dad's Treasury checks for **validated internet**
4. 🔔 If online, it opens **Google Family Link** so the parent can approve the extra screen time in one tap
5. ⏳ If offline, the grant is **queued locally** and retried automatically when connectivity returns

---

## 🧩 Why This Design?

- 🚫 **Family Link has no public API** for third-party apps to directly change screen-time limits
- ✅ Dad's Treasury uses the **legitimate path**: queue a screen-time grant and open Family Link so the parent confirms in one tap
- 🔒 The app remains **fully usable without** Google Family Link integration

---

## 📋 Requirements

| Requirement | Status |
|-------------|--------|
| Google Family Link app installed | ✅ Required for auto-open |
| Validated internet | ✅ Required for immediate push |
| Offline fallback | ✅ Grants are queued and retried |

---

## 📊 Grant States

| State | Meaning |
|-------|---------|
| ⏳ **Queued** | Waiting for internet or Family Link |
| 🔄 **Processing** | Trying to open Family Link |
| ✅ **Granted** | Successfully handed off to Family Link |
| ❌ **Failed** | Could not complete |

---

## 🔄 Related

- [Tasks](Tasks) — how Time tasks work
- [Wallet & Time Bank](Wallet-Time-Bank) — where minutes are stored
- [Connect Parent Apps](Connect-Parent-Apps) — parent-to-parent sync