<div align="center">

<br/>

# MindDump

**turn brain chaos into clarity. instantly.**

[![React](https://img.shields.io/badge/React-19-61DAFB?style=flat-square&logo=react&logoColor=black)](https://react.dev)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Groq](https://img.shields.io/badge/Groq-llama--3.3--70b-F55036?style=flat-square)](https://groq.com)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org)
[![Render](https://img.shields.io/badge/Deployed-Render-46E3B7?style=flat-square&logo=render&logoColor=white)](https://render.com)

<br/>

> most productivity tools assume you already have your thoughts organized. MindDump works backwards — it starts with the mess.

<br/>

</div>

---

## what it does

you dump everything in your head into a text box — raw, unfiltered, no structure. the AI reads between the lines and returns four sorted categories and one honest insight about your mental state.

| category | what goes here |
|---|---|
| 🔴 **urgent** | needs attention today. action items. |
| 🟡 **this week** | medium-priority tasks for the week. |
| 🔵 **someday** | low-urgency goals, dreams, revisit later. |
| 🟢 **ideas** | creative thoughts and side projects. |

the **insight** is the most important output — a direct, empathetic read on what you're really carrying beneath the noise.

---

## user flow

```
  USER              REACT (5173)        SPRING BOOT (8080)      GROQ API            H2 DB
   │                     │                      │                    │                  │
   │  types dump         │                      │                    │                  │
   │ ──────────────────► │                      │                    │                  │
   │                     │                      │                    │                  │
   │                     │  POST /api/dump      │                    │                  │
   │                     │  { rawText: "..." }  │                    │                  │
   │                     │ ───────────────────► │                    │                  │
   │                     │                      │                    │                  │
   │                     │                      │  llama-3.3-70b     │                  │
   │                     │                      │  prompt + text     │                  │
   │                     │                      │ ─────────────────► │                  │
   │                     │                      │                    │                  │
   │                     │                      │  { urgent[],       │                  │
   │                     │                      │    thisWeek[],     │                  │
   │                     │                      │    someday[],      │                  │
   │                     │                      │    ideas[],        │                  │
   │                     │                      │    insight }       │                  │
   │                     │                      │ ◄───────────────── │                  │
   │                     │                      │                    │                  │
   │                     │                      │  dumpRepository    │                  │
   │                     │                      │  .save(dump)       │                  │
   │                     │                      │ ──────────────────────────────────── ►│
   │                     │                      │ ◄────────────────────────────────────  │
   │                     │                      │                    │                  │
   │                     │  DumpResponse JSON   │                    │                  │
   │                     │ ◄─────────────────── │                    │                  │
   │                     │                      │                    │                  │
   │  sees results        │                      │                    │                  │
   │ ◄────────────────── │                      │                    │                  │
   │                     │                      │                    │                  │
   │  (after 5+ dumps)   │                      │                    │                  │
   │                     │  GET /pattern-insight│                    │                  │
   │                     │ ───────────────────► │                    │                  │
   │                     │                      │ ─────────────────► │                  │
   │                     │                      │ ◄───────────────── │                  │
   │  recurring themes   │  pattern JSON        │                    │                  │
   │ ◄────────────────── │ ◄─────────────────── │                    │                  │
   │                     │                      │                    │                  │
```

### what happens at each step

**01 — dump** · user types everything into `DumpBox.jsx`. stream of consciousness, no format required.

**02 — POST /api/dump** · `api.js` fires a POST with `{ rawText }` via Axios → hits `DumpController` → delegates to `DumpService`.

**03 — groq call** · `GroqService` builds a structured prompt, calls `llama-3.3-70b-versatile` on Groq's API, and parses the returned JSON.

**04 — persist** · `DumpService` maps the AI response into a `Dump` JPA entity → `DumpRepository.save()` → written to H2 (Postgres in prod).

**05 — results** · `DumpResponse` JSON returned to frontend. `Results.jsx` renders four category buckets + the insight card. `ExportBar.jsx` handles copy/download.

**06 — pattern insight** · after 5+ dumps, `GET /api/pattern-insight` compresses all `rawText` into one prompt → Groq returns recurring themes → shown in `History.jsx`.

---

## architecture

```
┌─────────────────────┐          ┌──────────────────────┐         ┌──────────────┐
│      Frontend        │  HTTP   │       Backend         │  HTTP   │   Groq API   │
│   React + Vite       │ ──────► │   Spring Boot 3.5     │ ──────► │ llama-3.3-70b│
│      :5173           │ ◄────── │       :8080           │ ◄────── │              │
└─────────────────────┘          └──────────┬───────────┘         └──────────────┘
                                            │
                                            ▼
                                   ┌─────────────────┐
                                   │   H2 Database    │
                                   │  (in-memory dev) │
                                   │  Postgres (prod) │
                                   └─────────────────┘
```

**backend layer flow**

```
DumpController
      │
      ▼
DumpService ──────────────► GroqService ──────► Groq API
      │
      ▼
DumpRepository ──────────► H2 / PostgreSQL
```

---

## tech stack

### frontend

| tech | version | purpose |
|---|---|---|
| React | 19.x | UI framework |
| Vite | 8.x | build tool / dev server |
| React Router | 7.x | client-side routing |
| Axios | 1.x | HTTP client |
| React Hot Toast | 2.x | notifications |

### backend

| tech | version | purpose |
|---|---|---|
| Spring Boot | 3.5.0 | application framework |
| Spring Data JPA | — | ORM |
| H2 Database | — | in-memory persistence |
| Lombok | — | boilerplate reduction |
| Java | 17 | runtime |

### AI

| provider | model | purpose |
|---|---|---|
| Groq | llama-3.3-70b-versatile | categorization + insight |

---

## getting started

### prerequisites

- Java 17+
- Node.js 18+
- Groq API key → [console.groq.com](https://console.groq.com)

### 1. clone

```bash
git clone https://github.com/your-username/MindDump.git
cd MindDump
```

### 2. configure backend

```bash
cd backend
```

`src/main/resources/application.properties`:

```properties
groq.api.key=YOUR_GROQ_API_KEY
groq.api.url=https://api.groq.com/openai/v1/chat/completions
groq.api.model=llama-3.3-70b-versatile
server.port=8080
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
app.cors.allowed-origins=http://localhost:5173
```

### 3. run backend

```bash
# linux / macOS
./mvnw spring-boot:run

# windows
mvnw.cmd spring-boot:run
```

→ `http://localhost:8080`

### 4. run frontend

```bash
cd frontend
npm install
npm run dev
```

→ `http://localhost:5173`

---

## API reference

base URL: `http://localhost:8080/api`

### `POST /api/dump`

```http
POST /api/dump
Content-Type: application/json
```

```json
// request
{
  "rawText": "i have a hackathon tomorrow, haven't slept, client owes me money, want to build a saas someday"
}
```

```json
// response
{
  "id": 1,
  "rawText": "i have a hackathon tomorrow...",
  "urgent": ["Prepare for hackathon", "Follow up on client payment"],
  "thisWeek": ["Catch up on sleep schedule"],
  "someday": ["Build a SaaS product"],
  "ideas": [],
  "insight": "You're juggling too many things at once. The hackathon is tomorrow — focus there first, everything else can wait.",
  "createdAt": "2026-05-09T10:30:00"
}
```

### all endpoints

| method | endpoint | description |
|---|---|---|
| `POST` | `/api/dump` | create + AI-process a dump |
| `GET` | `/api/dumps` | all dumps, newest first |
| `GET` | `/api/dumps/{id}` | single dump by ID |
| `GET` | `/api/dumps/count` | total dump count |
| `GET` | `/api/pattern-insight` | AI patterns across 5+ dumps |

---

## project structure

```
MindDump/
├── frontend/
│   ├── vite.config.js
│   └── src/
│       ├── main.jsx
│       ├── App.jsx
│       ├── api.js                    ← Axios client
│       ├── index.css                 ← Thermal Brutalism design system
│       ├── components/
│       │   └── ExportBar.jsx         ← copy / download results
│       └── pages/
│           ├── DumpBox.jsx           ← main input page
│           ├── Results.jsx           ← categorized results view
│           └── History.jsx           ← all past dumps + pattern insight
│
└── backend/
    ├── pom.xml
    └── src/main/java/com/minddump/
        ├── MindDumpApplication.java
        ├── config/
        │   └── WebConfig.java        ← CORS config
        ├── controller/
        │   └── DumpController.java   ← REST endpoints
        ├── dto/
        │   ├── DumpRequest.java
        │   └── DumpResponse.java
        ├── model/
        │   └── Dump.java             ← JPA entity
        ├── repository/
        │   └── DumpRepository.java
        └── service/
            ├── DumpService.java      ← business logic
            └── GroqService.java      ← Groq API integration
```

---

## design system

the frontend uses a custom design system called **Thermal Brutalism**.

```
dark mode           sharp edges (0–2px radius)     orange accent palette
Space Grotesk       Inter                           grain texture overlay
spring-physics animations                           high contrast type
```

all tokens live in `index.css` as CSS custom properties.

---

## deployment

one-click deploy via `render.yaml` — provisions all three services automatically.

| service | platform | type |
|---|---|---|
| frontend | Render Static Site | CDN-backed React build |
| backend | Render Web Service | Dockerized Spring Boot |
| database | Render PostgreSQL | managed, persistent |

```yaml
# render.yaml (overview)
services:
  - type: web         # Spring Boot backend
  - type: static      # React frontend
  - type: pserv       # PostgreSQL
```

> **note:** free tier services on Render sleep after 15 min of inactivity. first request after sleep takes ~30s.

---

## configuration reference

| property | default | description |
|---|---|---|
| `server.port` | `8080` | backend port |
| `groq.api.key` | — | Groq API key (required) |
| `groq.api.model` | `llama-3.3-70b-versatile` | LLM model |
| `groq.api.url` | `https://api.groq.com/...` | Groq endpoint |
| `app.cors.allowed-origins` | `http://localhost:5173` | allowed origins |
| `spring.h2.console.enabled` | `true` | H2 web console toggle |
| `spring.h2.console.path` | `/h2-console` | H2 console path |

> **never commit your API key.** use environment variables in production.

---

<div align="center">

<br/>

**MindDump** — because your brain deserves better than sticky notes.

<br/>

</div>
