# Kora 🦉

Your personal AI-powered assistant for managing private tutoring sessions.

[![CI](https://img.shields.io/badge/ci-TBD-lightgrey.svg)](#) [![License](https://img.shields.io/badge/license-TBD-lightgrey.svg)](#) [![Platform](https://img.shields.io/badge/platform-Android-3DDC84.svg)](#)

## Introduction
Kora is a modern Android application built for private tutors, coaches, and instructors who need a clean, reliable way to manage students, lessons, homework, and payments. It solves the chaos of scattered notes, manual payment tracking, and the lack of actionable insights by bringing everything into one thoughtfully designed, privacy‑respecting app.

Core principles:
- Privacy‑First: Your data stays on your device. No servers, no accounts, no hidden syncs.
- Intuitive Design: A frictionless, Material 3 experience powered by Jetpack Compose.
- Intelligent Assistance: Context‑aware insights backed by Google's Gemini API.

## ✨ Features & Functionality
### 👨‍🎓 Effortless Student Management
Keep a comprehensive, organized list of all your students. Track progress, lesson notes, and key details in one central hub.

### 🗓️ Smart Scheduling & Calendar
Plan and visualize lessons on a calendar. Quickly distinguish upcoming, completed, and payment‑pending sessions at a glance.

### 💰 Automated Payment Tracking
Automatically calculate amounts owed based on hourly rates and completed lessons. Log payments with a tap and maintain a clear, auditable history.

### 🧠 AI‑Powered Insights
Leverage Google’s Gemini API to get actionable recommendations. Kora analyzes lesson notes and homework performance to suggest focus areas for the next session.

### 📝 Integrated Homework Management
Assign homework, track its status (pending/completed), and record performance notes to build a complete academic picture for each student.

### 🔒 Privacy‑First by Design
All student and lesson information is stored securely and locally on your device, never sent to our servers.

### 📤 Data Portability & Control
Export your entire dataset to CSV for personal backups or sharing. Reset all app data instantly when you need a clean slate.

### 🌍 Global Ready
Out of the box support for multiple languages (English, Turkish, German) and currencies (USD, TRY, EUR), with smart defaults based on your device locale on first launch.

## 📱 Screenshots
Add your screenshots below (replace the links with your actual images):

![Student List](link_to_screenshot_student_list.png)
![Calendar](link_to_screenshot_calendar.png)
![Payments](link_to_screenshot_payments.png)
![AI Insights](link_to_screenshot_insights.png)

## 🛠️ Tech Stack & Architecture
Kora is built on a modern Android tech stack, following the MVVM architecture to ensure a scalable and maintainable codebase.

- UI: 100% Jetpack Compose for a declarative, modern UI.
- Language: Kotlin first, embracing coroutines for asynchronous operations.
- Architecture: MVVM with Hilt for dependency injection.
- Database: Room for robust, offline‑first local data storage.
- AI: Google Gemini API for intelligent features.

## 🚀 Installation & Getting Started
1) Clone the repository:

```bash
git clone https://github.com/your-username/Kora.git
cd Kora
```

2) Open the project in Android Studio (latest stable), ensure JDK 17 is configured.

3) Add your Gemini API key to `local.properties` (this file is untracked — do not commit secrets):

```properties
# local.properties (do NOT commit this file)
GEMINI_API_KEY=your_gemini_api_key_here
```

4) Build the app:

```bash
./gradlew assembleDebug
```

## 💬 Contact & Feedback
Have ideas, questions, or feedback? Open an issue or reach out at mhbarut66@gmail.com. If you find Kora useful, consider starring the repo!
