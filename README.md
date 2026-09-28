# J.A.R.K. To-Do (Android)

A futuristic personal task operating system and command center built with **Kotlin** and **Jetpack Compose**.

## Features

- **Personal Command Center (Dashboard)**: Real-time workspace stats, focus score, system health momentum ring, and urgent priority queue.
- **Task Management**: Create, edit, complete, reopen, and delete tasks with priority levels (`urgent`, `high`, `medium`, `low`), due dates, categories, and tags.
- **J.A.R.K. Command Layer**: High-speed hybrid natural language instruction parser supporting text and speech input. Automatically handles natural instructions such as:
  - *"Add task Review code due tomorrow with high priority"*
  - *"Complete task Map next milestone"*
  - *"What should I work on next?"*
  - *"What is due today?"*
  - *"Show overdue tasks"*
- **Persistent Local Database**: Built with Android Room database for offline reliability and reactive state flows.
- **Filtering & Search**: Dynamic search across titles, descriptions, categories, and tags with real-time status and priority filters.
- **Futuristic J.A.R.K. Cyberpunk UI**: Distinctive dark theme with electric blue tech accents, neon indicators, and Material 3 adaptive design.

## Architecture

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (Material Design 3)
- **State Management**: ViewModel + Kotlin Coroutines & StateFlow
- **Local Persistence**: Room SQLite Database
- **AI / NLP**: Hybrid Offline NLP Engine + optional Gemini 3.5 Flash REST API Integration
