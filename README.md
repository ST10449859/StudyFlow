# StudyFlow — Part 2 Android Prototype

StudyFlow is the student productivity application designed in Part 1. This repository contains the Kotlin/Jetpack Compose Android prototype and a Node.js/Express REST API.

## Part 2 features implemented
- Registration and login
- Password hashing on the API using bcrypt
- JWT session authentication
- Settings
- REST API connected to Firestore
- Task creation, listing and completion
- Subject-based task information
- Calendar-style upcoming task list
- 25-minute Pomodoro focus session
- Study sessions and progress statistics
- Study habits and streak data
- XP and levels
- Progress/rewards screen
- Logging with Android Logcat and server logging

## PoE features to extend
- Google SSO
- Room offline mode and automatic sync
- Firebase Cloud Messaging push notifications
- English/Afrikaans/isiXhosa localisation

## Project structure
- `StudyFlow/` — Android Studio project
- `studyflow-api/` — Node.js REST API

## Run the API
1. Install Node.js 20+.
2. Open `studyflow-api` in a terminal.
3. Run `npm install`.
4. Copy `.env.example` to `.env`.
5. Add Firebase service-account credentials as environment variables.
6. Run `npm start`.

For a physical Android phone, change `API_BASE_URL` in `app/build.gradle.kts` to the computer's LAN IP during local testing, or to the Render URL after deployment.

## Deploy the API to Render
Create a Web Service from the `studyflow-api` folder.
- Build command: `npm install`
- Start command: `npm start`
- Add all variables from `.env.example` in Render's Environment settings.

After deployment, replace the release `API_BASE_URL` in the Android app with the HTTPS Render address.

## Android Studio
Open the `StudyFlow` folder. Allow Gradle to sync, then run the app on an Android emulator or physical device.

## Demonstration video checklist
1. Show registration.
2. Show the password hash in Firestore/server database rather than a plaintext password.
3. Log in.
4. Create a task.
5. Show the task in the app and in Firestore.
6. Complete a task and show progress/XP changing.
7. Start and finish a study session.
8. Create a habit.
9. Change settings.
10. Show API requests and database records.
11. Show GitHub repository and GitHub Actions build.
12. Explain the remaining PoE-only features as planned extensions.
