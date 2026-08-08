# 📅 Calendar

> Dad's Treasury includes a full offline calendar and **dual date display**: Gregorian + Natural 13-month calendar.

---

## 🌍 Dual Date Display

| Calendar | Example |
|----------|---------|
| 📅 **Gregorian** | Tue, 8 Aug 2026 14:30 |
| 🌿 **Natural** | Leaf 11, Year 8 |

The Gregorian calendar is always the internal source of truth. The Natural calendar is a derived display layer.

---

## 📌 Adding Events (Parent Only)

1. Open **Calendar** tab
2. Tap **+** (top right) — hidden for child role
3. Fill in:
   - **Title** *(required)*
   - **Description**
   - **Date** (dd-MM-yyyy)
   - **Time** (HH:mm)
   - **Duration** (hours)
   - **Reminder** (minutes before, optional)
4. Tap **Create**

> 🔒 **Children cannot create events.** The + button is hidden and the data layer rejects child attempts.

---

## ⏰ Reminders

If you set a reminder, Dad's Treasury schedules a **local notification** using AlarmManager — no Google Play Services needed.

---

## 🔄 Related

- [Getting Started](Getting-Started)
- [Settings](Settings)