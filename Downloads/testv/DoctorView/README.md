# Doctor View

**An Intelligent Mobile Healthcare Platform for Doctor Appointment Management,
Disease Analysis, Online Consultation, and Healthcare Information Services**

A graduation-project prototype Android app built with **Java**, **XML layouts**,
**Material Design** and **Firebase**. No Kotlin, no Jetpack Compose, no backend server —
Firebase handles everything (authentication, database, real-time chat, file storage).

---

## Tech stack

| Part | Technology |
|---|---|
| Language | Java 17 |
| UI | XML layouts + Material Design 3 (Material Components) |
| Navigation | Navigation Component (bottom navigation + nav graph) |
| Auth | Firebase Authentication (Email + Password) |
| Database | Cloud Firestore (doctors, appointments, records, news) |
| Real-time chat | Firebase Realtime Database |
| Files | Firebase Storage (when needed) |
| Build | Gradle 8.13, Android Gradle Plugin 8.13.2 |
| Android | minSdk 26 (Android 8.0), compileSdk 36 |

---

## How to open and run (Android Studio)

1. Open **Android Studio**.
2. **File → Open…** and select this `DoctorView` folder.
3. Wait for the Gradle sync to finish (first time downloads dependencies — needs internet).
4. Plug in a phone (USB debugging on) or start an emulator.
5. Press the green **Run ▶** button (or **Build → Make Project** to only compile).

The APK is produced at: `app\build\outputs\apk\debug\app-debug.apk`

### Build from the command line (Windows)

```
gradlew.bat :app:assembleDebug
```

---

## Demo flow (current version)

1. **Splash** → **Login** (real Firebase sign-in — register, reset password too)
2. **Main** screen with 4 tabs: **Home · Doctors · Appointments · Profile**
3. **Doctors tab**: first open → press **Load sample doctors** to seed 8 sample
   doctors into Firestore, then search and open **Doctor Details**
   (photo, rating, experience, fee, about, availability).
4. **Doctor Details → Book Appointment**: pick a date (calendar) and a time
   slot, add a note, confirm → saved to Firestore and you land on
   **My Appointments**, where pending/confirmed bookings can be cancelled.
5. Home quick-access grid opens every other section (placeholders for now).
6. Profile shows your account info and a working **Logout**.

---

## Connect Firebase (needed before login/register work)

The app already contains **all Firebase dependencies and a placeholder config**,
so it compiles out of the box. To make Firebase work for real:

1. Go to [console.firebase.google.com](https://console.firebase.google.com) → **Add project** (name: Doctor View).
2. In the project, click the Android icon → **Add an Android app**.
3. Package name: **`com.doctorview.app`** (must match exactly).
4. Download **google-services.json** and replace the placeholder at
   `app\google-services.json`.
5. Enable what the app needs:
   - **Authentication → Sign-in method → Email/Password → Enable**
   - **Firestore Database → Create database** (start in test mode)
   - **Realtime Database → Create database** (start in test mode)
   - **Storage → Get started** (only when the records feature needs it)
6. Sync the project again and run.

> Note: the app is now connected to a real Firebase project. Before running
> login/register, make sure these are enabled in the Firebase console:
> **Authentication → Sign-in method → Email/Password**, and
> **Firestore Database → Create database** (start in test mode).

---

## Project structure

```
DoctorView/
├── settings.gradle / build.gradle / gradle.properties   ← Gradle config
├── gradlew, gradlew.bat, gradle/wrapper/                ← Gradle wrapper
├── local.properties                                     ← your SDK path (machine-only)
└── app/
    ├── build.gradle                                     ← app module + dependencies
    ├── google-services.json                             ← Firebase config (replace with yours)
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/doctorview/app/
        │   ├── activities/    Splash, Login, Register, Main
        │   ├── fragments/     Home, Doctors, Appointments, Profile
        │   │                  + DoctorDetails, BookAppointment, SymptomAnalysis,
        │   │                    DiseaseInfo, Chat, News, Records, Emergency, Settings
        │   ├── adapters/      FeatureAdapter (grid cards on Home)
        │   ├── models/        User, Doctor, Appointment, Feature
        │   ├── firebase/      FirebaseHelper (Auth + Firestore + Realtime DB)
        │   └── utils/         Constants, AppUtils
        └── res/
            ├── layout/        every screen layout (XML + Material components)
            ├── navigation/    nav_graph.xml (all screens + navigation paths)
            ├── menu/          bottom_nav_menu.xml
            ├── drawable/      vector icons, splash gradient, chip/circle shapes
            ├── color/         bottom-nav color selector
            ├── mipmap-anydpi-v26/  launcher icon (adaptive)
            └── values/        colors, strings, themes (Material 3 DayNight), dimens
```

---

## Planned roadmap (next development steps)

1. ✅ Project foundation, navigation skeleton, Material theme
2. ✅ **Login / Register** with Firebase Authentication + user profiles in Firestore
3. ✅ **Doctors list + Doctor Details** from Cloud Firestore (search + sample-data seeding)
4. ✅ **Appointment booking** (date picker + time slots) + **My Appointments** with status and cancel
5. ✅ **Symptom Analysis** — rule-based checker: symptom chips → ranked possible conditions with severity and advice (no ML)
6. ✅ **Disease Information** — searchable Firestore library (10 sample diseases) with full details: overview, symptoms, causes, prevention, treatment
7. ✅ **Online Consultation chat** — real-time messaging over Firebase Realtime Database (message bubbles, live listener, one room per patient–doctor pair)
8. ✅ **Healthcare News** — searchable Firestore feed (8 sample articles) with banner images, category chips, dates, and full article pages
9. ✅ **Medical Records** — personal health vault in Firestore with optional file attachments uploaded to Firebase Storage (add, list, open, delete)
10. ✅ **Emergency Help** (one-tap dial + first-aid basics) + **Settings** (notifications toggle, dark mode, about) — **project complete**
11. ✅ **UI redesign** — premium light-blue/white healthcare theme: #1677F2 palette, rounded cards (20dp), pill filter chips, floating rounded bottom nav, Home with featured doctor card + specialty chips + top-rated row, doctor details with stats + tabs + sticky Book Now, booking summary card, register confirm-password + role selection

---

## Troubleshooting

- **"SDK location not found"** → Android Studio regenerates `local.properties`
  automatically, or set it to your SDK path (`sdk.dir=C:/Users/<you>/AppData/Local/Android/Sdk`).
- **Slow first build** → normal: Gradle downloads the Android Gradle Plugin and
  Firebase libraries once, then caches them.
- **`targetSdk 34` warning** → intentional: keeps the demo simple on Android 15+
  devices (no edge-to-edge handling needed). You can raise it later.
