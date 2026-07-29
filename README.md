# TripWise AI – Smart Travel Planner

A full-stack travel planning application. Users sign up/log in, describe a trip (destination, days, budget, travel type, interests), and get a generated day-wise itinerary plus a smart, categorized packing checklist. Trips can be saved and revisited under "My Trips".

- **Backend:** Java 17, Spring Boot 3, Spring Data JPA, Spring Security (verifies Supabase JWTs), Bean Validation
- **Frontend:** React 18 (Vite), React Router, plain CSS (design tokens, no UI framework)
- **Auth:** Supabase Auth (email/password) — the backend never sees a password; it only verifies tokens Supabase already issued
- **Database:** PostgreSQL, hosted on Supabase
- **AI:** Google Gemini (free tier) for itinerary generation, with a deterministic rule-based fallback
- **Deployment:** Frontend on Vercel, backend on Render, both pointed at the same Supabase project

## Project Structure

```
TripWise-AI/
├── backend/                Spring Boot API (Maven project)
│   └── src/main/java/com/tripwise/ai/
│       ├── config/         CORS/security config + AiConfig (Gemini RestClient)
│       ├── controller/     REST controllers
│       ├── dto/            Request/response DTOs (incl. dto/ai for Gemini parsing)
│       ├── entity/         JPA entities
│       ├── exception/      Global exception handling
│       ├── repository/     Spring Data repositories
│       ├── security/       SupabaseJwtService (verifies tokens) + auth filter
│       └── service/        Business logic — GeminiAiService (real AI), ItineraryGeneratorService
│                            (rule-based fallback), TripService (orchestrates the two)
├── frontend/                React app (Vite)
│   └── src/
│       ├── components/     Navbar, ProtectedRoute, ConfirmModal
│       ├── context/        AuthContext (wraps Supabase's session), ToastContext
│       ├── pages/          Login, Register, Dashboard, PlanTrip, TripResult, MyTrips, Profile
│       ├── services/       supabaseClient.js (auth) + Axios API clients (app data)
│       └── styles/         Design tokens + per-page CSS
├── database/
│   ├── schema.sql           Table definitions (manual reference; run in Supabase SQL Editor)
│   └── seed-data.sql        Sample trip data for a user you've already signed up
├── render.yaml              Render backend service definition
└── README.md
```

## 1. Create a Supabase Project

One project covers both Auth and the database, for local dev and production alike.

1. Go to **https://supabase.com/dashboard** → **New project**. Note the database password you set.
2. Once it's provisioned, collect four values you'll need repeatedly below:
   - **Project URL** and **anon public key** — Project Settings → API
   - **JWT Secret** — Project Settings → API → JWT Settings
   - **Connection string (JDBC, Transaction pooler)** — Project Settings → Database → Connection string
3. In the SQL Editor, run `database/schema.sql` to create the `users`/`trips`/... tables.
   (The backend can also auto-create them via Hibernate on first boot — either works.)
4. **Email confirmation:** by default Supabase requires users to confirm their email before
   they can log in. For quick local testing you can turn this off at Authentication →
   Providers → Email → "Confirm email". Leave it on for production.

## 2. Backend Setup (Spring Boot)

Requires JDK 17+ and Maven 3.8+.

1. Set these environment variables (get the values from Section 1):
   ```bash
   export SPRING_DATASOURCE_URL="jdbc:postgresql://<supabase-pooler-host>:6543/postgres?sslmode=require"
   export SPRING_DATASOURCE_USERNAME="postgres.<project-ref>"
   export SPRING_DATASOURCE_PASSWORD="your-db-password"
   export SUPABASE_JWT_SECRET="your-jwt-secret"
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
   The API starts on **http://localhost:8080**.

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
| GET    | `/api/auth/me`                                | Yes  | Get current user (verifies the token) |
| GET    | `/api/users/profile`                          | Yes  | Get profile (read-only — see below)   |
| POST   | `/api/trips/plan`                             | Yes  | Generate + save a new trip            |
| GET    | `/api/trips?saved=true\|false`                | Yes  | List current user's trips             |
| GET    | `/api/trips/{tripId}`                         | Yes  | Get full trip (itinerary + packing)   |
| PATCH  | `/api/trips/{tripId}/saved`                   | Yes  | Toggle saved flag                     |
| DELETE | `/api/trips/{tripId}`                         | Yes  | Delete a trip                         |
| PATCH  | `/api/trips/{tripId}/packing-items/{itemId}`  | Yes  | Toggle a packing item's checked state |

Authenticated requests require `Authorization: Bearer <supabase-access-token>`. There is
no `/api/auth/register` or `/api/auth/login` — those happen entirely against Supabase
from the frontend (see Integration Notes).

## 3. Frontend Setup (React)

Requires Node.js 18+.

1. From the `frontend/` directory:
   ```bash
   cd frontend
   npm install
   ```
2. Copy the example env file and fill in your Supabase project's URL/anon key from Section 1:
   ```bash
   cp .env.example .env
   ```
   ```
   VITE_API_BASE_URL=http://localhost:8080/api
   VITE_SUPABASE_URL=https://your-project-ref.supabase.co
   VITE_SUPABASE_ANON_KEY=your-anon-public-key
   ```
3. Start the dev server:
   ```bash
   npm run dev
   ```
   The app runs on **http://localhost:5173**.

4. Build for production:
   ```bash
   npm run build
   ```

## 4. Deploying: Supabase + Render + Vercel

You already have the Supabase project from Section 1. Two more services to set up:

### 4a. Backend on Render

1. Push this repo to GitHub (already done if you're reading this from the repo).
2. Render dashboard → **New** → **Blueprint** → point it at this repo. Render reads
   `render.yaml` and creates the `tripwise-ai-backend` web service automatically.
   (Alternatively: **New** → **Web Service**, root directory `backend`, build command
   `mvn -q -DskipTests clean package`, start command `java -jar target/tripwise-ai-1.0.0.jar`.)
3. In the service's **Environment** tab, set:
   | Key | Value |
   |---|---|
   | `SPRING_DATASOURCE_URL` | Supabase JDBC connection string (Transaction pooler, port 6543, `?sslmode=require`) |
   | `SPRING_DATASOURCE_USERNAME` | from the same connection string |
   | `SPRING_DATASOURCE_PASSWORD` | your Supabase DB password |
   | `SUPABASE_JWT_SECRET` | Supabase → Project Settings → API → JWT Settings |
   | `GEMINI_API_KEY` | your Gemini key (optional) |
   | `CORS_ALLOWED_ORIGINS` | your Vercel URL, e.g. `https://trip-wise-ai.vercel.app` (add after Section 4b) |
   Render sets `PORT` itself — the app already reads it (`server.port=${PORT:8080}`).
4. Deploy. Confirm with `curl https://<your-render-url>/api/health`.

### 4b. Frontend on Vercel

1. Vercel dashboard → **Add New** → **Project** → import this repo.
2. Set **Root Directory** to `frontend` (Vercel auto-detects the Vite framework preset).
3. Add environment variables (Project Settings → Environment Variables):
   | Key | Value |
   |---|---|
   | `VITE_API_BASE_URL` | your Render URL + `/api`, e.g. `https://tripwise-ai-backend.onrender.com/api` |
   | `VITE_SUPABASE_URL` | same as backend |
   | `VITE_SUPABASE_ANON_KEY` | Supabase anon **public** key (safe to expose client-side) |
4. Deploy. `frontend/vercel.json` already adds the SPA rewrite so client-side routes
   (e.g. `/trips/5`) don't 404 on refresh.
5. **Go back to Render** and set `CORS_ALLOWED_ORIGINS` to your new Vercel URL, then
   redeploy the backend so it accepts requests from it.
6. **In Supabase**, add the Vercel URL under Authentication → URL Configuration →
   Redirect URLs (needed for email confirmation links to work correctly in production).

## 5. Integration Notes

- **Auth flow:** Registration and login happen entirely against Supabase Auth via
  `@supabase/supabase-js` on the frontend (`AuthContext.jsx`) — the backend never
  receives a password. Every API request attaches the current Supabase session's
  `access_token` as a Bearer token (`src/services/api.js`); `JwtAuthenticationFilter`
  verifies it against `SUPABASE_JWT_SECRET` (HS256) and rejects anything invalid/expired.
  A 401 response signs the frontend out and redirects to `/login`.
- **Profile sync:** On every authenticated request, `UserService.syncFromToken` upserts
  a row in our own `users` table from the token's claims (id/email/name), so there's no
  separate "create profile" step after signup. Supabase is the single source of truth —
  name changes go through `supabase.auth.updateUser()` on the frontend (Profile page),
  not a backend endpoint, so they can't drift out of sync on token refresh. Email changes
  aren't exposed in this UI since they require Supabase's own confirmation flow.
- **CORS:** The backend's allowed origins are controlled by `app.cors.allowed-origins`
  in `application.properties` (env var `CORS_ALLOWED_ORIGINS`). Must include your Vercel
  URL in production or the browser will block requests.
- **Itinerary generation:** `TripService.planTrip` tries `GeminiAiService` first (see
  [Section 2a](#2a-ai-setup-google-gemini-free-tier)). It prompts Gemini with the trip's
  destination/days/budget/travel type/interests and constrains the reply with a
  `responseSchema` (structured JSON mode), so the model's output parses directly into
  `AiItineraryResultDto` — no free-text scraping. The result is validated (correct day
  count, every day has activities, packing list non-empty) before being trusted; any
  failure — missing key, network error, malformed response — falls back to
  `ItineraryGeneratorService`, a deterministic rule-based engine, so trip planning always
  succeeds. The `trips.ai_generated` column records which path was used.
- **Data shape:** `trips.interests` is stored as a `jsonb` array of lowercase strings
  (e.g. `["adventure", "food"]`). Budget is in **₹ (INR)**.
- **Ownership checks:** All trip endpoints verify the authenticated user owns the trip
  (`TripService.findTripOwnedByUser`); mismatches return `403 Forbidden`.
- **Validation errors:** Bean Validation failures return `400` with a `fieldErrors` map
  keyed by field name — the frontend forms surface these inline, but currently rely on
  their own client-side validation first (server errors are shown via the top-level
  `alert-error` banner).
- **Running both together locally:** Supabase project created (Section 1) → `.env` files
  filled in on both sides → `mvn spring-boot:run` (backend, port 8080) → `npm run dev`
  (frontend, port 5173) → open http://localhost:5173, register a new account, confirm the
  email if confirmation is enabled, then log in.

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
