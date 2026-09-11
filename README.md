<p>
  <img src="androidApp/src/main/ic_launcher-playstore.png" width="96" alt="sarv logo" align="left" />
  <b>Sarv — سرو</b><br>
  A modern Persian poetry app for reading, learning, and playing.
</p>
<br clear="all"/>

---

## Screenshots

<p float="left">
  <img src="screenshots/home.png" width="192" />&nbsp;&nbsp;
  <img src="screenshots/poets.png" width="192" />&nbsp;&nbsp;
  <img src="screenshots/games.png" width="192" />&nbsp;&nbsp;
  <img src="screenshots/profile.png" width="192" />
</p>

<p align="left"><em>Responsive layout — optimized for phones and tablets</em></p>

<p float="left">
  <img src="screenshots/tablet.png" width="600" />
</p>

---

## Features

### Poetry Library
- Poets from the Ganjoor database (Hafez, Rumi, Saadi, Ferdowsi, and more)
- Browse by poet, category, and collection
- Full-text search across all poetry
- Like and bookmark your favorite poems

### Literature Games — میدان
Four quiz modes to test your knowledge of Persian poetry:

| Game | Points | Description |
|------|--------|-------------|
| **Next Verse** | 10 | Choose the correct following verse |
| **Complete Poem** | 15 | Fill in the missing words |
| **Find Poet** | 20 | Identify who wrote a given verse |
| **Organize Poem** | 25 | Reorder jumbled verses |

Track your streaks, earn coins, unlock badges, and level up.

### Memorization — حفظ شعر
Spaced repetition system (SRS) for memorizing poems:
- Add up to 3 active poems at a time
- Daily review reminders
- Grade reviews as Again / Hard / Good / Easy
- Track progress with reviewed verses and completion stats

### Chat With Poet — گفتگو با شاعر
Chat with poet personas. Ask questions, explore themes, or just enjoy a conversation about poetry.

### Tasvir Negar — تصویرنگار
Create poetry-themed images with:
- Custom stickers, fonts, and textures
- Color and grid options
- Import from gallery
- Share or save directly

### Daily Distich — بیت روزانه
A random verse delivered to your phone every day. Also available as a home screen widget.

### Accessibility
- Dynamic font scaling (Small / Default / Large) from profile settings

---

## Architecture

```
androidApp/          → Thin Android shell (Activity, Application)
shared/              → All business logic (KMP module)
  ├─ domain/         → Models, repository interfaces, use cases
  ├─ data/           → Repository implementations, SQLDelight, Ktor
  └─ features/       → MVI screens, ViewModels, navigation
core/                → Design system, DI, networking, notifications
```

**Pattern:** MVI (Model-View-Intent) with Clean Architecture layers.  
**Offline-first:** Local SQLDelight databases with on-demand poet DB downloads from GitHub releases.  
**DI:** Koin modules per feature.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.4.10 |
| UI | Compose Multiplatform 1.11.1, Material3 |
| Navigation | Compose Navigation (type-safe routes) |
| DI | Koin 4.2.2 |
| Database | SQLDelight 2.3.2 |
| Networking | Ktor 3.5.2 |
| Image Loading | Coil 3.5.0 |
| Analytics | Firebase Crashlytics, Cloud Messaging |
| Serialization | Kotlinx Serialization 1.11.0 |
| Testing | JUnit 5, assertk, Turbine |
| Lint | Detekt, Compose Lint Checks |

---

## License

Copyright © 2018–2026 Sarv. All rights reserved.
