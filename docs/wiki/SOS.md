# 🆘 SOS Button

A quick-alert tool for the child to send a pre-defined message with their current location directly to the parent's chat.

## How it works

1. Open the **SOS Button** from the child home screen.
2. Choose what's happening:
   - 🆘 **Help**
   - 📞 **Call Me**
   - 😴 **I'm tired**
   - 🍽️ **I'm hungry**
   - ⏰ **Late for dinner**
3. Press the big red **SEND** button.

The app grabs the device's last known GPS location and sends a chat message to the parent:

```
[selected message]
📍 [latitude], [longitude]
```

## On the parent side

The message appears in the **Chat** screen with a highlighted **location pin card**, so you can see exactly where the child is.

## Privacy

- Uses `LocationManager.getLastKnownLocation` (GPS / network)
- No Google Play Services required
- All data stays on-device and in the app's encrypted database