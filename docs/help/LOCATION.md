# Location Rules

Location rules are local reminders that trigger when the child enters a specific area — like arriving home, at school, or at a relative's house.

## How It Works

1. Parent creates a rule with a location and message
2. A proximity alert is registered **on-device**
3. When the child's phone enters the radius, a notification appears with the message
4. The notification has quick actions:
   - **✓ Done** — mark this rule as done
   - **✕ Disable** — turn off future alerts for this rule

Everything runs locally — no Google Play Services, no internet, no cloud.

## Creating a Rule

1. Open **Locations** from Home quick nav
2. Tap **+** (top right)
3. Fill in:
   - **Title** (required)
   - **Message** (shown in notification)
   - **Place name** — type "Home" or "School" and tap **🔍** to auto-fill coordinates
   - **Latitude / Longitude** (auto-filled or manual)
   - **Radius** — pick a preset (50m–5000m) or enter custom
   - **Active from / until** — hour range (24h clock)
4. Tap **Create**

## Place Name Search

Type a place name like "Grandma's house" and tap the search icon. The built-in Android geocoder finds the coordinates — no Google Maps API needed.

## Radius Presets

| Radius | Use case |
|--------|----------|
| 50m | Very small: room, house corner |
| 100m | Small: part of a street |
| 250m | Typical: a house |
| 500m | Neighborhood block |
| 1000m+ | School zone, park area |

## Active Hours

Restrict alerts to certain hours. For example, only trigger the "School" reminder between 8:00 and 17:00.

## Notifications

- **Sound channel** — high priority, vibrates
- **Silent channel** — low priority, no sound

The channel is chosen per rule based on whether sound was set.