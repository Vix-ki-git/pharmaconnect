# PharmaConnect — GitHub Codespaces Setup

This repo includes a `.devcontainer/` config so anyone with a GitHub account can
spin up the full stack (Spring Boot backend + Angular frontend + MySQL 8) in
the cloud, with zero local installs, in about 3 minutes.

> **Codespaces is for development and demos**, not production hosting. It's a
> remote VS Code environment — perfect for code reviews, onboarding, demos to
> stakeholders. For real production deployment (load-balanced backend, managed
> DB, custom domain), you'd use something like Render / Fly.io / AWS for the
> backend, Vercel / Netlify for the frontend, and RDS / PlanetScale for the DB.

---

## First-time launch

1. Go to the repo on github.com.
2. Click the green **Code** button → **Codespaces** tab → **Create codespace on main**.
3. Wait ~2–3 minutes. GitHub builds the container, installs Java 21 + Node 20 +
   MySQL 8, runs `npm ci`, and warms the Maven cache. You'll see a VS Code
   window in your browser with the project ready.
4. **VS Code will prompt: "Allow automatic tasks to run?"** Click **Allow**
   (or **Allow Always**). This lets the backend, DB seed, and frontend
   auto-start every time you open this codespace.
5. Three task panels open in the bottom Terminal area:
   - **PharmaConnect: Backend** — Spring Boot booting (~60–90s on first run)
   - **PharmaConnect: Seed DB** — runs once the backend is ready, idempotent
   - **PharmaConnect: Frontend** — Angular dev server, starts after seed
6. When the frontend finishes compiling, port 4200 auto-opens in a new browser
   tab. You're done — no commands typed.

> **Demo accounts**: passwords are all `password` (see `database/dummy_data.sql`
> for the user list — admin user is typically `admin@pharmaconnect.com`).

### Subsequent sessions

The same auto-start runs every time you open the codespace. The DB seed step
detects existing data and skips itself, so it's safe.

If you ever decline the "Allow automatic tasks" prompt or want to start things
manually, you have two equivalent options:

**Option A — VS Code task palette** (Ctrl+Shift+P → "Tasks: Run Task" →
"PharmaConnect: Start All")

**Option B — terminal commands**:

```bash
# Terminal 1
cd pc && ./mvnw spring-boot:run

# Terminal 2 (run once after backend is up; safe to re-run)
bash .devcontainer/seed-db.sh

# Terminal 3
cd pc-frontend && npm start -- --host 0.0.0.0
```

---

## Optional Codespaces secrets

The defaults work for a demo without setting any secrets. Configure these only
if you have a reason. Set them at: **github.com → your repo → Settings →
Secrets and variables → Codespaces → New repository secret**.

| Secret | What it does | Default | When to set |
|---|---|---|---|
| `DB_PASSWORD` | MySQL root password (used by both the DB container and the backend connection) | `pharmaconnect_dev` | Only if you're sharing the codespace publicly and want a non-default password |
| `BREVO_API_KEY` | Brevo (Sendinblue) API key for password-reset emails | `DUMMY_VALUE` | If you want forgot/reset-password to actually send emails. Without this, the flow works UI-side but no email gets sent |
| `CORS_ORIGINS` | Comma-separated allowed CORS origins | `http://localhost:4200,https://*.app.github.dev` | Only if you change defaults — the wildcard pattern already covers all Codespaces URLs |

These are picked up automatically by the container as environment variables —
no code changes needed when you add or rotate them. The codespace must be
restarted (or rebuilt) for new secret values to take effect.

---

## What happens automatically

You don't need to configure these — they just work:

- **Port forwarding**: ports 4200 (frontend) and 8082 (backend) are auto-forwarded.
  4200 opens your browser when the dev server starts. Both ports default to
  **Private** visibility — the frontend works for *you* immediately, but to
  share the URL with someone else (or to let your browser's cross-origin fetches
  to 8082 succeed first time), see the troubleshooting section on port visibility.
- **Backend URL detection**: the Angular app auto-detects whether it's running
  on `localhost` (laptop) or a Codespaces URL, and points API calls to the
  matching backend port. No `environment.prod.ts` builds, no env vars.
- **CORS**: the default `app.cors.origins` includes `https://*.app.github.dev`,
  so any Codespace URL is accepted without configuration.
- **Database**: MySQL 8 starts as a sidecar container, accessible at
  `localhost:3306` from inside the dev container (same as on your laptop).

---

## Troubleshooting

### "Connection refused" when frontend hits backend
Make sure the backend is running and the **Ports** panel shows port 8082 as
forwarded. If port 8082 doesn't appear, restart `./mvnw spring-boot:run`.

### Frontend loads but every API call returns nothing / "Unexpected token '<'" / 302
The most common cause on a fresh codespace: **port 8082 is forwarded as Private**
(the default). Cross-origin fetches from the 4200 frontend then hit GitHub's
auth gate instead of Spring Boot, and the response is HTML (a sign-in or
"Codespaces Access Port" warning page), which the frontend can't parse as JSON.

Fix:
1. Open the **Ports** tab → right-click row **8082** → **Port Visibility →
   Public**. The icon next to 8082 changes from a lock to a globe.
2. In a new tab, visit `https://<codespace-name>-8082.app.github.dev/` once.
   You'll see a "You are about to access a development port" warning — click
   **Continue**. This sets an acknowledgment cookie scoped to the 8082 host,
   which the browser will then send on subsequent fetches from the frontend.
3. Reload the frontend tab. Searches/logins/etc. now work.

You can also flip visibility from a terminal:
```bash
gh codespace ports visibility 8082:public -c $CODESPACE_NAME
```

### Frontend page loads but every API call returns a true CORS error
(Browser console shows an actual "blocked by CORS policy" message, not a parse
error.) Your frontend URL probably doesn't match `https://*.app.github.dev`.
Check the URL — Codespaces URLs always end with `.app.github.dev`. If you've
set `CORS_ORIGINS` as a secret to override the default, make sure your origin
is included in the comma-separated list.

### Postman / curl from your laptop returns 302 or the warning HTML
Postman has its own cookie jar and never clicked "Continue" on the interstitial,
so GitHub's edge keeps serving it the warning page. Two workarounds:

- **Easiest** — from inside the codespace terminal, hit the backend on
  `localhost`, which bypasses the GitHub edge entirely:
  ```bash
  curl -i http://localhost:8082/api/search/medicines?q=para
  ```
- From your laptop's Postman: open the 8082 URL once in your browser, click
  Continue, then copy the acknowledgment cookie from DevTools → Application →
  Cookies and add it as a `Cookie:` header in Postman.

### Hitting `/api/auth/register` (or `/login`, etc.) in a browser returns 405
Expected — those are POST endpoints. A browser address bar issues a GET, which
returns `405 Method Not Allowed`. Use Postman/curl with `-X POST -H
"Content-Type: application/json" -d '{...}'`, or just exercise them through the
frontend UI.

### Backend logs show "Could not initialize proxy ... no session"
This was a known bug fixed via `@JsonIgnore` on `Pharmacy.owner`. If you see
it now, you may have an old branch. Pull from main.

### `seed-db.sh` says "tables don't exist after waiting 180s"
The backend hasn't booted yet, or it crashed. Check the **PharmaConnect: Backend**
task panel for errors. Most often it's a MySQL connection issue — check the
**db** container is running with `docker compose -f .devcontainer/docker-compose.yml ps`.

### `seed-db.sh` says `Can't connect to local server through socket '/run/mysqld/mysqld.sock'`
The mysql CLI treats `-h localhost` as "use a Unix socket", which doesn't exist
in this container setup (MySQL runs in a sidecar). The script forces TCP by
coercing `DB_HOST=localhost` → `127.0.0.1` before invoking `mysql`. If you see
this error, you're on an old version of `seed-db.sh` — pull from main.

### `seed-db.sh` fails on the final verification SELECT with `ERROR 1064 ... near 'rows FROM users'`
`ROWS` became a reserved word in MySQL 8.0.2, and the verification query at the
bottom of `database/dummy_data.sql` used it as an unquoted column alias. Fixed
by backtick-quoting (`` AS `rows` ``). If you hit this, pull from main. Note
that the seed itself completed successfully before the verify step ran — your
data is loaded; just rerun the task and the idempotency guard will skip.

### `seed-db.sh` says "medicine table already has X rows — skipping seed"
This is correct behavior — the seed already ran. To force a clean reseed:
uncomment the `TRUNCATE` block at the top of `database/dummy_data.sql`, then
run `bash .devcontainer/seed-db.sh` manually.

### Auto-start tasks didn't fire
You probably declined the "Allow automatic tasks" prompt. Run them manually
via Ctrl+Shift+P → "Tasks: Run Task" → "PharmaConnect: Start All".
Or reset the prompt: open Settings (Ctrl+,), search "task allow", and reset
"Task: Allow Automatic Tasks" to "auto".

### MySQL container won't start
In the Codespaces terminal: `docker compose -f .devcontainer/docker-compose.yml logs db`
to see why. Most often it's an interrupted previous run holding a lock —
delete the codespace and create a new one.

---

## Local development is unaffected

None of the changes for Codespaces support break local laptop development.
Specifically:

- `application.properties` reads env vars with **defaults that match the
  current local setup**. No env vars set → same behavior as before.
- The Angular `environment.ts` falls back to `http://localhost:8082` when not
  on a Codespaces URL, so `ng serve` on `localhost:4200` works identically.
- The `.devcontainer/` files are only loaded by Codespaces (or VS Code's
  "Reopen in Container" mode). Running `mvn` or `ng` directly on your laptop
  ignores them entirely.

Run locally exactly the same way you always have:

```bash
# Terminal 1
cd pc && ./mvnw spring-boot:run

# Terminal 2
cd pc-frontend && npm start
```
