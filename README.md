# CodeSphere

**A collaboration platform for developer teams.** Teams get workspaces where they can hold threaded technical discussions, write in Markdown with syntax-highlighted code, keep a shared knowledge base and collaborative documents with full version history, and share files — all behind role-based access control.

<!-- Add screenshots here, e.g.:
![Discussion thread](docs/screenshots/discussion.png)
-->

---

## Table of contents

- [Features](#features)
- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [Roles and permissions](#roles-and-permissions)
- [API overview](#api-overview)
- [Getting started](#getting-started)
- [Configuration](#configuration)
- [Project structure](#project-structure)
- [Testing](#testing)
- [Known limitations and roadmap](#known-limitations-and-roadmap)

---

## Features

**Collaboration**
- **Workspaces** with members and three roles (Owner / Admin / Member)
- **Channels**, public or private. Private channels are invisible to non-members
- **Discussion threads** with pagination
- **Nested replies**: reply to a reply, to any depth
- **@mentions** that notify the person mentioned, plus reply notifications that link straight to the thread
- **Polls** per channel: one vote per person, live result bars, closable

**Knowledge management**
- **Markdown everywhere** with GitHub-flavoured extras (tables, task lists), syntax-highlighted code blocks with a Copy button, a Write/Preview editor, and HTML sanitisation
- **Knowledge base** with categories and published/unpublished articles
- **Shared documents** that any workspace member can edit, organised as a folder-style tree
- **Version history**: every edit snapshots the previous version, and any version can be restored (a restore is itself undoable)
- **Tags** that work across discussions, articles and documents, and **workspace search** across all three
- **File uploads** (PDF, ZIP, PNG/JPEG/GIF/WEBP, up to 25 MB) with authenticated download

**Communication and insight**
- **Announcements** (admin-only, pinnable)
- **Activity feed** of what happened in the workspace
- **Workspace analytics** (members, channels, discussions, replies, articles, documents, files)
- **Notifications** with an unread badge

**Administration**
- **Platform admin dashboard**: list all users and workspaces, enable or disable accounts (a disabled user's live session stops working immediately)
- **User profiles**: display name, bio, avatar

---

## Tech stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.3, Spring Security, Spring Data JPA (Hibernate), Bean Validation, Lombok |
| Auth | Stateless JWT (jjwt), BCrypt password hashing |
| Database | PostgreSQL |
| Frontend | React 18, Vite, React Router, Axios, React Hook Form, React Hot Toast |
| Styling | Tailwind CSS + `@tailwindcss/typography` |
| Markdown | `react-markdown`, `remark-gfm`, `rehype-sanitize`, `rehype-highlight` |
| Testing | JUnit 5, Mockito |
| CI | GitHub Actions |

---

## Architecture

```
React SPA ──HTTP/JSON + JWT──▶ Controller ─▶ Service ─▶ Repository ─▶ PostgreSQL
                                  │             │
                                  │             └─▶ StorageService ─▶ local disk
                                  └─ DTOs in / DTOs out (entities never leave the service layer)
```

The backend is a conventional layered Spring Boot application. Controllers handle HTTP only; services own business rules and authorization; repositories are Spring Data interfaces. Every request and response is a DTO, and a single `@RestControllerAdvice` turns exceptions into a consistent error shape (`status`, `error`, `message`, `fieldErrors`).

### Design decisions worth knowing about

- **Authorization lives in one place.** `WorkspaceService.requireMembership` and `requireRoleAtLeast` are reused by every other feature's service, so each feature doesn't reinvent its own permission checks.
- **Nested replies use an adjacency list** (`parent_reply_id`). A discussion's whole reply tree is fetched in **one query** and assembled into a tree in memory in O(n), which avoids one query per level. Deleting a reply deletes its subtree via `ON DELETE CASCADE`.
- **Private channels return 404, not 403**, to non-members, so their existence isn't leaked.
- **Tags are polymorphic** (`tagged_items` stores `entity_type` + `entity_id`). New taggable types need no new join table. The trade-off is that the database can't enforce a foreign key on `entity_id`.
- **Version history is transactional.** The pre-edit snapshot and the edit commit together or not at all.
- **Uploads are allow-listed and path-safe.** Only specific content types are accepted, and files are stored under server-generated UUID keys, never the client's filename, which prevents path traversal. `StorageService` hides the storage backend so a cloud implementation (e.g. S3) could be swapped in without touching callers.
- **Markdown is rendered client-side** and sanitised **before** syntax highlighting, so raw HTML a user types (`<script>`, `onerror=`) is stripped.
- **Expired or tampered JWTs get a clean `401`**, and the frontend clears the dead token and returns to the login page.
- **The activity feed is denormalised** (it stores a rendered description) so history survives even if the original item is later deleted.

---

## Roles and permissions

There are two separate role systems, deliberately kept apart.

**Workspace roles** apply within one workspace:

| Action | Member | Admin | Owner |
|---|:---:|:---:|:---:|
| Read everything in the workspace (public channels) | ✅ | ✅ | ✅ |
| Create channels, discussions, replies, articles, documents, polls; upload files; tag | ✅ | ✅ | ✅ |
| Edit any shared document | ✅ | ✅ | ✅ |
| Edit / delete own discussions, replies, articles | ✅ | ✅ | ✅ |
| Delete others' discussions, replies, articles, documents, channels, files; close others' polls | ❌ | ✅ | ✅ |
| Post / delete announcements; delete KB categories | ❌ | ✅ | ✅ |
| Edit workspace details; add members; change roles; remove members | ❌ | ✅ | ✅ |
| Grant or revoke **Owner**; delete the workspace | ❌ | ❌ | ✅ |

**Platform role** (`USER` / `ADMIN`) applies across the whole system. Only a platform admin can use the admin dashboard to list all users and workspaces and enable or disable accounts. Being a workspace Owner grants no platform-admin powers, and vice versa.

---

## API overview

All endpoints are under `/api`. Everything except `/api/auth/**` requires `Authorization: Bearer <token>`.

| Area | Endpoints |
|---|---|
| Auth | `POST /auth/register` · `POST /auth/login` |
| Profile | `GET /users/me` · `PUT /users/me/profile` |
| Workspaces | `POST /workspaces` · `GET /workspaces` · `GET/PUT/DELETE /workspaces/{id}` |
| Members | `GET/POST /workspaces/{id}/members` · `PUT /workspaces/{id}/members/{userId}/role` · `DELETE /workspaces/{id}/members/{userId}` |
| Channels | `POST/GET /workspaces/{w}/channels` · `GET/DELETE …/channels/{c}` · `POST …/channels/{c}/members` |
| Discussions | `POST/GET /workspaces/{w}/channels/{c}/discussions` · `GET/PUT/DELETE /discussions/{id}` |
| Replies | `POST/GET /discussions/{id}/replies` · `PUT/DELETE /replies/{id}` |
| Knowledge base | `POST/GET /workspaces/{w}/kb/categories` · `DELETE …/kb/categories/{id}` · `POST/GET …/kb/articles` · `GET/PUT/DELETE /kb/articles/{id}` |
| Documents | `POST/GET /workspaces/{w}/documents` · `GET/PUT/DELETE /documents/{id}` · `GET /documents/{id}/versions` · `POST /documents/{id}/versions/{n}/restore` |
| Tags & search | `GET /workspaces/{w}/tags` · `GET …/tags/items/{type}/{id}` · `POST` to tag a discussion / article / document · `GET /workspaces/{w}/search?q=` |
| Files | `POST/GET /workspaces/{w}/resources` · `GET …/resources/{id}/download` · `DELETE …/resources/{id}` |
| Polls | `POST/GET /workspaces/{w}/channels/{c}/polls` · `POST /polls/{id}/options/{optionId}/vote` · `POST /polls/{id}/close` |
| Announcements | `POST/GET /workspaces/{w}/announcements` · `DELETE …/announcements/{id}` |
| Notifications | `GET /notifications` · `GET /notifications/unread-count` · `POST /notifications/{id}/read` |
| Activity & analytics | `GET /workspaces/{w}/activity` · `GET /workspaces/{w}/analytics` |
| Admin | `GET /admin/users` · `PUT /admin/users/{id}/enable` · `PUT /admin/users/{id}/disable` · `GET /admin/workspaces` |

---

## Getting started

### Prerequisites

- Java 21
- Maven 3.9+
- Node.js 18+
- PostgreSQL 14+

> **Naming note:** the project is called CodeSphere, but its Java package (`com.devcollab`) and default database name (`devcollab`) still use the original working title. They're internal only and everything below uses those names.

### 1. Clone and create the database

```bash
git clone https://github.com/<your-username>/codesphere.git
cd codesphere

psql -U postgres -c "CREATE DATABASE devcollab;"
```

You don't write any SQL for tables. Hibernate creates the schema on first start.

### 2. Run the backend

```bash
cd backend
cp .env.example .env        # then edit: DB credentials and a real JWT_SECRET
```

Generate a strong secret with `openssl rand -base64 32` and put it in `JWT_SECRET`. Then load the variables into your shell and start the server:

```bash
export $(grep -v '^#' .env | xargs)      # macOS/Linux; see SETUP.md for Windows PowerShell
mvn spring-boot:run
```

The API starts on **http://localhost:8080**. Look for `Tomcat started on port 8080`.

### 3. Run the frontend

In a second terminal:

```bash
cd frontend
cp .env.example .env        # defaults to http://localhost:8080/api
npm install
npm run dev
```

Open **http://localhost:5173**, register an account, and create a workspace.

### 4. Optional: become a platform admin

The admin dashboard is reserved for platform admins, and there's deliberately no sign-up path to that role. Promote your own account directly:

```bash
psql -U postgres -d devcollab -c "UPDATE users SET role='ADMIN' WHERE username='your-username';"
```

Refresh the app and an **Admin** link appears in the top bar.

For a guided two-account walkthrough of every feature (and a troubleshooting table), see **[SETUP.md](SETUP.md)**.

---

## Configuration

**Backend** (environment variables, read by `application.yml`):

| Variable | Purpose | Default |
|---|---|---|
| `DB_NAME` | Database name | `devcollab` |
| `DB_USERNAME` | Database user | `postgres` |
| `DB_PASSWORD` | Database password | `postgres` |
| `JWT_SECRET` | Signing key for tokens. **Set your own; never commit it.** | placeholder |
| `JWT_EXPIRATION_MS` | Token lifetime | `86400000` (24 h) |
| `STORAGE_LOCAL_PATH` | Where uploaded files are written | `./uploads` |
| `STORAGE_MAX_FILE_SIZE_BYTES` | Per-file upload limit | `26214400` (25 MB) |
| `SERVER_PORT` | HTTP port | `8080` |

**Frontend** (`frontend/.env`):

| Variable | Purpose | Default |
|---|---|---|
| `VITE_API_BASE_URL` | Backend API base URL | `http://localhost:8080/api` |

The backend's CORS policy allows `http://localhost:5173` and `http://localhost:3000`. Add your deployed frontend origin in `SecurityConfig` before deploying.

---

## Project structure

```
codesphere/
├── backend/
│   └── src/main/java/com/devcollab/
│       ├── config/        Spring Security (JWT filter chain, CORS)
│       ├── controller/    REST endpoints, one class per feature area
│       ├── service/       Business logic and authorization checks
│       ├── repository/    Spring Data JPA interfaces
│       ├── entity/        JPA entities (the schema)
│       ├── dto/           Request/response shapes
│       ├── mapper/        Entity → DTO conversion
│       ├── security/      JWT generation/validation and the request filter
│       ├── exception/     Custom exceptions + global error handling
│       └── util/          Slug generation, @mention parsing
└── frontend/
    └── src/
        ├── pages/         One screen per feature
        ├── layouts/       Workspace sidebar layout
        ├── components/    Markdown renderer/editor, reply thread, polls, tags, navbar
        ├── contexts/      Auth state
        ├── hooks/         useFetch (loading/error handling)
        ├── services/      Axios client (auto-attaches the JWT)
        └── utils/         Formatting helpers
```

---

## Testing

```bash
# Backend: JUnit 5 + Mockito unit tests
cd backend && mvn test

# Frontend: production build
cd frontend && npm run build
```

The backend tests cover registration (duplicate detection, password hashing) and the core authorization rules that every other feature reuses. The frontend has no unit-test suite yet. During development, every page was exercised in a headless browser against a mock API.

GitHub Actions (`.github/workflows/ci.yml`) runs the backend tests and builds the frontend on every push and pull request to `main`.

---

## Known limitations and roadmap

These are deliberate scope boundaries, not hidden surprises.

- **No database migrations.** Schema is managed by Hibernate `ddl-auto: update`. Flyway or Liquibase should replace it before any production use.
- **Search is simple `LIKE` matching**, not full-text search. Postgres `tsvector` + a GIN index is the natural upgrade.
- **File storage is local disk.** `StorageService` is built for a cloud swap, but deleting a file record doesn't yet remove the file from disk.
- **No real-time updates.** The notification badge polls every 30 seconds. WebSockets would make it live.
- **Auth is access-token only** (24 h, kept in `localStorage`): no refresh tokens, email verification or password reset.
- **Test coverage is modest.** Unit tests exist for core logic; there are no integration tests yet (Testcontainers + PostgreSQL would be the next step) and no frontend tests.
- **Mentions have no autocomplete** in the editor, and notifications only cover mentions and replies.
- **Platform admins are promoted via SQL** rather than through the UI.

**Roadmap ideas:** Flyway migrations, full-text search, S3 storage, WebSocket notifications, refresh tokens, reactions on discussions and replies, integration tests.
