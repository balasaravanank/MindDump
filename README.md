<br># MindDump

**Turn brain chaos into clarity. Instantly.**

MindDump is an AI-powered mental clarity tool. You type out everything on your mind — messy, unfiltered, no structure required — and the AI organizes it into actionable categories with an honest insight about your mental state.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [API Reference](#api-reference)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [License](#license)

---

## Overview

Most productivity tools assume you already have your thoughts organized. MindDump works backwards — it starts with the mess.

**How it works:**

1. You dump everything on your mind into a text box
2. The AI (Groq / Llama 3.3 70B) reads between the lines
3. You get back four sorted categories and one honest insight

**Categories:**

| Category   | Purpose                                      |
|------------|----------------------------------------------|
| Urgent     | Needs attention today. Action-oriented items. |
| This Week  | Medium-priority tasks for the current week.   |
| Someday    | Low-urgency goals, dreams, things to revisit. |
| Ideas      | Creative thoughts and side projects to save.  |

The **Insight** is the most important part — a direct, empathetic observation about what you're really feeling beneath the surface.

---

## Architecture

```
┌─────────────────┐         ┌─────────────────┐         ┌─────────────┐
│                 │  HTTP   │                 │  HTTP   │             │
│    Frontend     ├────────>│    Backend      ├────────>│  Groq API   │
│    (React)      │  :5173  │  (Spring Boot)  │  :8080  │  (LLM)      │
│                 │<────────│                 │<────────│             │
└─────────────────┘         └────────┬────────┘         └─────────────┘
                                     │
                                     v
                            ┌─────────────────┐
                            │   H2 Database   │
                            │   (In-Memory)   │
                            └─────────────────┘
```

**Data flow:**
- Frontend sends raw text via `POST /api/dump`
- Backend forwards to Groq API for AI categorization
- Structured response is persisted to H2 and returned to client
- Pattern detection runs across all stored dumps (requires 5+ dumps)

---

## Tech Stack

### Frontend

| Technology       | Version | Purpose                  |
|------------------|---------|--------------------------|
| React            | 19.x    | UI framework             |
| Vite             | 8.x     | Build tool / dev server  |
| React Router     | 7.x     | Client-side routing      |
| Axios            | 1.x     | HTTP client              |
| React Hot Toast  | 2.x     | Toast notifications      |

### Backend

| Technology       | Version | Purpose                  |
|------------------|---------|--------------------------|
| Spring Boot      | 3.5.0   | Application framework    |
| Spring Data JPA  | —       | Database ORM             |
| H2 Database      | —       | In-memory persistence    |
| Lombok           | —       | Boilerplate reduction    |
| Java             | 17      | Runtime                  |

### AI

| Provider | Model                    | Purpose               |
|----------|--------------------------|-----------------------|
| Groq     | llama-3.3-70b-versatile  | Thought categorization |

---

## Getting Started

### Prerequisites

- Java 17+
- Node.js 18+
- A Groq API key ([console.groq.com](https://console.groq.com))

### 1. Clone the repository

```bash
git clone https://github.com/your-username/MindDump.git
cd MindDump
```

### 2. Start the backend

```bash
cd backend
```

Set your Groq API key in `src/main/resources/application.properties`:

```properties
groq.api.key=YOUR_GROQ_API_KEY
```

Run the server:

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

The backend starts on `http://localhost:8080`.

### 3. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend starts on `http://localhost:5173`.

---

## API Reference

Base URL: `http://localhost:8080/api`

### Create a dump

```
POST /api/dump
```

**Request body:**

```json
{
  "rawText": "i have a hackathon tomorrow, haven't slept, client owes me money..."
}
```

**Response:**

```json
{
  "id": 1,
  "rawText": "i have a hackathon tomorrow...",
  "urgent": ["Prepare for hackathon tomorrow", "Follow up on client payment"],
  "thisWeek": ["Catch up on sleep schedule"],
  "someday": ["Start a YouTube channel"],
  "ideas": [],
  "insight": "You're juggling too many things at once and it's showing. The hackathon is tomorrow — focus there first, everything else can wait.",
  "createdAt": "2026-05-07T10:30:00"
}
```

### Get all dumps

```
GET /api/dumps
```

Returns an array of all stored dumps, newest first.

### Get dump by ID

```
GET /api/dumps/{id}
```

Returns a single dump by its ID. Returns `404` if not found.

### Get dump count

```
GET /api/dumps/count
```

```json
{
  "count": 12
}
```

### Get pattern insight

```
GET /api/pattern-insight
```

Returns an AI-generated pattern analysis across all stored dumps. Requires at least 5 dumps to produce meaningful results.

```json
{
  "insight": "You consistently mention work deadlines and sleep — you may be in a cycle of overcommitting."
}
```

---

## Project Structure

```
MindDump/
├── frontend/
│   ├── index.html
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── main.jsx              # App entry point
│       ├── App.jsx               # Root component + routing
│       ├── api.js                # Axios API client
│       ├── index.css             # Design system (Thermal Brutalism)
│       ├── components/
│       │   └── ExportBar.jsx     # Copy / download results
│       └── pages/
│           ├── DumpBox.jsx       # Main input page
│           ├── Results.jsx       # Categorized results view
│           └── History.jsx       # All past dumps
│
├── backend/
│   ├── pom.xml
│   ├── mvnw / mvnw.cmd
│   └── src/main/java/com/minddump/
│       ├── MindDumpApplication.java
│       ├── config/
│       │   └── WebConfig.java        # CORS configuration
│       ├── controller/
│       │   └── DumpController.java   # REST endpoints
│       ├── dto/
│       │   ├── DumpRequest.java      # Input DTO
│       │   └── DumpResponse.java     # Output DTO
│       ├── model/
│       │   └── Dump.java             # JPA entity
│       ├── repository/
│       │   └── DumpRepository.java   # Data access
│       └── service/
│           ├── DumpService.java      # Business logic
│           └── GroqService.java      # Groq API integration
│
└── README.md
```

---

## Configuration

All backend configuration lives in `backend/src/main/resources/application.properties`.

| Property                       | Default                                           | Description              |
|--------------------------------|---------------------------------------------------|--------------------------|
| `server.port`                  | `8080`                                            | Backend server port      |
| `groq.api.key`                 | —                                                 | Your Groq API key        |
| `groq.api.url`                 | `https://api.groq.com/openai/v1/chat/completions` | Groq endpoint            |
| `groq.api.model`               | `llama-3.3-70b-versatile`                         | LLM model                |
| `app.cors.allowed-origins`     | `http://localhost:5173,http://localhost:3000`      | Allowed CORS origins     |
| `spring.h2.console.enabled`   | `true`                                            | H2 web console           |
| `spring.h2.console.path`      | `/h2-console`                                     | H2 console URL path      |

> **Important:** Never commit your API key to version control. Use environment variables or a `.env` file for production.

---

## Design

The frontend uses a custom design system called **Thermal Brutalism** — dark mode, sharp edges (0-2px radius), orange accent palette, Space Grotesk + Inter typography, grain texture overlay, and spring-physics animations.

**Design tokens are defined as CSS custom properties in `index.css`.**

---

## License

This project is for personal / educational use. No license specified yet.

---

<p align="center">
  <strong>MindDump</strong> — because your brain deserves better than sticky notes.
</p>
