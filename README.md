[English](README.md) · [Español](README.es.md)

# INEN Citas Médicas — Android client

Native Android client for a medical appointment system built for INEN, Peru's
national cancer institute. Patients request appointments and track their status;
doctors triage, schedule and manage them.

**This is a two-person university project.** The Android app was written jointly
with [Adrián Torres](https://github.com/AdrianTorres2021), who also owns the
[Django backend](https://github.com/AdrianTorres2021/inen-citas-medicas) this app
consumes — that is why there is no server code in this repository.

## Who wrote what

Measured with `git blame` on the Kotlin sources:

| File | Mine | Adrián's |
|---|---|---|
| `ApiClient.kt` | 43 / 43 | — |
| `ApiService.kt` | 30 / 41 | 11 |
| `ApiModels.kt` | 20 / 60 | 40 |
| `MainActivity.kt` | 190 / 549 | 359 |

My part is the networking layer: the Retrofit client, the custom `CookieJar`
that keeps the Django session across requests, and most of the API contract.
Adrián built the doctor panel, the multi-role flow and the bottom navigation.

## What it does

- **Patients** register, log in, browse specialities, request an appointment with
  their symptoms, and read notifications.
- **Doctors** log in with their `colegiatura` number, see appointments grouped by
  status, and approve, reschedule or close them.
- The backend assigns a **triage score and urgency level** to each request, which
  the app displays alongside the AI justification it returns.

## Stack

Kotlin · Android SDK 24–36 · AGP 9.2.1 · Retrofit 2.11 · OkHttp · Gson

The UI is built programmatically in `MainActivity` — no XML layouts beyond a
single container, no fragments. That is the main thing I would change: 549 lines
in one Activity is hard to test and hard to extend.

## Running it

```bash
git clone https://github.com/Pameladelarada/inen-citas-medicas-kotlin.git
```

Open in Android Studio and run. The app points at the deployed backend, so no
local server is needed. To point it elsewhere, change `BASE_URL` in
`ApiClient.kt`.

## Handling of personal data

The app transmits **health data of identifiable people**: national ID (DNI),
symptoms, clinical history and triage scores. Under Peru's Ley 29733 that is
*sensitive personal data*, with stricter requirements than ordinary personal data.

What the app does today:

- Stores **nothing** on the device — no `SharedPreferences`, no local database.
  Session state lives in memory and dies when the process does.
- Talks to the backend over **HTTPS only**.
- Emits **no network logs outside debug builds**, so request paths never reach
  logcat on a release build.

What would be required before any real deployment, and is **not** in place:

- A privacy notice and explicit consent at registration, as Ley 29733 requires
  for sensitive data.
- Registration of the personal data bank with the ANPD.
- A server-side logout endpoint — see the limitation below.
- Code shrinking and obfuscation in release builds. `isMinifyEnabled` is
  deliberately `false`: enabling R8 with an empty `proguard-rules.pro` would
  obfuscate the `data class` field names, and Gson maps JSON by field name, so
  every field would arrive null in release only, with no build error. The keep
  rules have to be written and a release APK tested on a device first.

This is a university project running against test data. The list above is what a
production deployment would need, not a claim that it is compliant.

## Known limitations

- **Logout is client-side only.** The backend exposes no logout endpoint, so the
  server session stays valid until it expires. The app clears its cookie and
  in-memory state, which is as far as the client can go.
- No automated tests. The two files under `app/src/test` and
  `app/src/androidTest` are the Android Studio templates.
- The session is not persisted, so the user logs in again on every app start.

## License

No licence file. Please ask before reusing.
