# Policy Tracker (Android)

A personal, ad-free Android app to track:
- Health insurance
- Term insurance
- Vehicle insurance
- Driving licence
- Medical records

## What is included
- Modern dashboard with category filters
- Light + dark theme
- Expiry highlighting (next 60 days)
- Policy details with:
  - policy name
  - policy number
  - start date
  - expiry date
  - insurer name
  - previous insurer name
  - premium amount
  - notes
- Renewal/history tracking per policy
- Attachments per policy (PDF/images/docs) using Android document picker
- Local Room database (offline, private on your phone)

## Build APK
1. Open this folder in Android Studio (latest stable).
2. Let Gradle sync and install required Android SDK components.
3. Build debug APK:
   - `Build` -> `Build Bundle(s) / APK(s)` -> `Build APK(s)`
4. APK output path:
   - `app/build/outputs/apk/debug/app-debug.apk`

## Notes
- Date format in forms is `YYYY-MM-DD`.
- Attachments are stored as URI references to documents selected on the device.
