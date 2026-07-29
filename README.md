# TripWise AI – Smart Travel Planner

A full-stack travel planning application. Users register/log in, describe a trip (destination, days, budget, travel type, interests), and get a generated day-wise itinerary plus a smart, categorized packing checklist. Trips can be saved and revisited under "My Trips".

- **Backend:** Java 17, Spring Boot 3, Spring Data JPA, Spring Security (JWT), Bean Validation
- **Frontend:** React 18 (Vite), React Router, plain CSS (design tokens, no UI framework)
- **Database:** PostgreSQL 14+

## Project Structure

```
TripWise-AI/
├── backend/                Spring Boot API (Maven project)
│   └── src/main/java/com/tripwise/ai/
│       ├── config/         Security config (JWT, CORS) + AiConfig (Gemini RestClient)
│       ├── controller/     REST controllers
│       ├── dto/            Request/response DTOs (incl. dto/ai for Gemini parsing)
│       ├── entity/         JPA entities
│       ├── exception/      Global exception handling
│       ├── repository/     Spring Data repositories
│       ├── security/       JWT filter/service, UserDetailsService
│       └── service/        Business logic — GeminiAiService (real AI), ItineraryGeneratorService
│                            (rule-based fallback), TripService (orchestrates the two)
├── frontend/                React app (Vite)
│   └── src/
│       ├── components/     Navbar, ProtectedRoute
│       ├── context/        AuthContext (JWT session state)
│       ├── pages/          Login, Register, Dashboard, PlanTrip, TripResult, MyTrips, Profile
│       ├── services/       Axios API clients
│       └── styles/         Design tokens + per-page CSS
├── database/
│   ├── schema.sql           Table definitions (manual reference)
│   └── seed-data.sql        Sample user + trip data
└── README.md
```

## 1. Database Setup

1. Install PostgreSQL 14+ and make sure it's running.
2. Create the database:
   ```bash
   psql -U postgres -c "CREATE DATABASE tripwise_ai;"
   ```
3. (Optional — the backend auto-creates tables on startup via `spring.jpa.hibernate.ddl-auto=update`.)
   To create the schema manually instead, run:
   ```bash
   psql -U postgres -d tripwise_ai -f database/schema.sql
   ```
4. (Optional) Load sample data:
   ```bash
   psql -U postgres -d tripwise_ai -f database/seed-data.sql
   ```
   This creates a demo user:
   - Email: `demo@tripwise.ai`
   - Password: `Password123!`

## 2. Backend Setup (Spring Boot)

Requires JDK 17+ and Maven 3.8+.

1. Configure the database connection and JWT secret in
   `backend/src/main/resources/application.properties` (defaults assume a local
   Postgres with user `postgres` / password `postgres` on port 5432 — update as needed),
   or override via environment variables:
   ```bash
   export JWT_SECRET="a-long-random-production-secret"
   export CORS_ALLOWED_ORIGINS="http://localhost:5173"
   ```
2. **(Optional but recommended) Enable real AI generation** — see [Section 2a](#2a-ai-setup-google-gemini-free-tier)
   below. Without a key, trip planning still works end-to-end using the built-in
   rule-based generator.
3. From the `backend/` directory, run:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   The API starts on **http://localhost:8080**. On first run, Hibernate creates all
   tables automatically (`users`, `trips`, `itinerary_days`, `activities`, `packing_items`).

4. Quick health check:
   ```bash
   curl http://localhost:8080/api/health
   ```

## 2a. AI Setup (Google Gemini, free tier)

Trip planning uses Google Gemini for real AI-generated itineraries, with an automatic,
transparent fallback to a rule-based generator if no key is configured or the API call
fails for any reason (rate limit, network error, etc.) — planning never breaks either way.

1. Go to **https://aistudio.google.com/apikey** (not Google Cloud Console — that flow
   often creates a key without free-tier quota) and click **"Create API key"** →
   **"Create API key in new project"**. The key should start with `AIzaSy...`.
2. Set it as an environment variable before starting the backend:
   ```bash
   export GEMINI_API_KEY="your-key-here"
   ```
3. That's it — `app.ai.gemini.enabled` defaults to `true` and the model defaults to
   `gemini-flash-latest`. Both are overridable:
   ```bash
   export GEMINI_ENABLED=true            # set false to always use the rule-based generator
   export GEMINI_MODEL=gemini-flash-latest
   export GEMINI_TIMEOUT_MS=25000
   ```

Every planned trip's response includes `"aiGenerated": true|false` so the frontend can
show which engine produced it — look for the **✨ AI-Generated** badge on the Trip
Result page and the **✨ AI** badge on trip cards.

### Key API Endpoints

| Method | Endpoint                                    | Auth | Description                          |
|--------|----------------------------------------------|------|---------------------------------------|
| POST   | `/api/auth/register`                          | No   | Register a new user, returns JWT      |
| POST   | `/api/auth/login`                             | No   | Log in, returns JWT                   |
| GET    | `/api/auth/me`                                | Yes  | Get current user                      |
| GET    | `/api/users/profile`                          | Yes  | Get profile                           |
| PUT    | `/api/users/profile`                          | Yes  | Update name/email                     |
| POST   | `/api/trips/plan`                             | Yes  | Generate + save a new trip            |
| GET    | `/api/trips?saved=true\|false`                | Yes  | List current user's trips             |
| GET    | `/api/trips/{tripId}`                         | Yes  | Get full trip (itinerary + packing)   |
| PATCH  | `/api/trips/{tripId}/saved`                   | Yes  | Toggle saved flag                     |
| DELETE | `/api/trips/{tripId}`                         | Yes  | Delete a trip                         |
| PATCH  | `/api/trips/{tripId}/packing-items/{itemId}`  | Yes  | Toggle a packing item's checked state |

Authenticated requests require `Authorization: Bearer <token>`.

## 3. Frontend Setup (React)

Requires Node.js 18+.

1. From the `frontend/` directory:
   ```bash
   cd frontend
   npm install
   ```
2. Copy the example env file and adjust if needed:
   ```bash
   cp .env.example .env
   ```
   `VITE_API_BASE_URL` defaults to `http://localhost:8080/api`.
3. Start the dev server:
   ```bash
   npm run dev
   ```
   The app runs on **http://localhost:5173**. Vite is also configured to proxy
   `/api` requests to `http://localhost:8080` (see `vite.config.js`), so the
   `.env` override is only needed if the backend runs elsewhere.

4. Build for production:
   ```bash
   npm run build
   ```

## 4. Integration Notes

- **Auth flow:** On register/login, the backend returns `{ token, userId, name, email }`.
  The frontend stores the JWT in `localStorage` (`tripwise_token`) and attaches it to
  every request via an Axios interceptor (`src/services/api.js`). A 401 response
  clears the session and redirects to `/login`.
- **CORS:** The backend's allowed origins are controlled by `app.cors.allowed-origins`
  in `application.properties` (defaults include `http://localhost:5173`). Update this
  (or the `CORS_ALLOWED_ORIGINS` env var) if the frontend is served from another origin.
- **Itinerary generation:** `TripService.planTrip` tries `GeminiAiService` first (see
  [Section 2a](#2a-ai-setup-google-gemini-free-tier)). It prompts Gemini with the trip's
  destination/days/budget/travel type/interests and constrains the reply with a
  `responseSchema` (structured JSON mode), so the model's output parses directly into
  `AiItineraryResultDto` — no free-text scraping. The result is validated (correct day
  count, every day has activities, packing list non-empty) before being trusted; any
  failure — missing key, network error, malformed response — falls back to
  `ItineraryGeneratorService`, a deterministic rule-based engine that builds a day-wise
  itinerary from templated activities and a categorized packing list, so trip planning
  always succeeds. The `trips.ai_generated` column records which path was used.
- **Data shape:** `trips.interests` is stored as a `jsonb` array of lowercase strings
  (e.g. `["adventure", "food"]`). The frontend sends/receives interests in this format.
- **Ownership checks:** All trip endpoints verify the authenticated user owns the trip
  (`TripService.findTripOwnedByUser`); mismatches return `403 Forbidden`.
- **Validation errors:** Bean Validation failures return `400` with a `fieldErrors` map
  keyed by field name — the frontend forms surface these inline, but currently rely on
  their own client-side validation first (server errors are shown via the top-level
  `alert-error` banner).
- **Running both together locally:** start Postgres → (optionally) `export
  GEMINI_API_KEY=...` → `mvn spring-boot:run` (backend, port 8080) → `npm run dev`
  (frontend, port 5173) → open http://localhost:5173 and register a new account (or
  log in with the seeded demo account if you loaded `seed-data.sql`).

## Design Tokens (Frontend)

Defined in `frontend/src/styles/variables.css`:

| Token              | Value      |
|---------------------|------------|
| Primary             | `#0EA5E9`  |
| Secondary           | `#22C55E`  |
| Accent              | `#F59E0B`  |
| Background          | `#F8FAFC`  |
| Border radius       | `12px`     |
| Heading font        | Poppins    |
| Body font           | Inter      |
