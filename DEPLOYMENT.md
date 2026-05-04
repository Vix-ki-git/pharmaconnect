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
4. When the build is done, the integrated terminal prints a 3-step boot guide.
   Follow it (also reproduced below).

### Boot the app (first session)

In the VS Code terminal:

```bash
# 1. Start the backend — this creates the DB tables on first boot
cd pc
./mvnw spring-boot:run
```

Wait until the logs say `Started Application in X seconds`. Leave it running,
then open a **second terminal** (Terminal → New Terminal):

```bash
# 2. Seed the DB with sample medicines, users, pharmacies
bash .devcontainer/seed-db.sh
```

Then open a **third terminal** for the frontend:

```bash
# 3. Start the frontend (--host 0.0.0.0 is required so Codespaces can forward it)
cd pc-frontend
npm start -- --host 0.0.0.0
```

Once the frontend says `Local: http://localhost:4200/`, VS Code's **Ports**
panel (bottom of the screen, or `Ctrl+\`` then click "Ports") shows the public
forwarded URL. Click the globe icon next to port 4200 — the app opens in a new
browser tab. You're done.

> **Demo accounts**: passwords are all `password` (see comments in
> `database/dummy_data.sql` for the user list).

### Boot the app (subsequent sessions)

After the first launch, the DB and tables persist for the lifetime of that
codespace. So you only need:

```bash
# Terminal 1
cd pc && ./mvnw spring-boot:run

# Terminal 2
cd pc-frontend && npm start -- --host 0.0.0.0
```

If you delete and recreate the codespace, you'll need the seed step again.

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
  4200 opens your browser when the dev server starts.
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

### Frontend page loads but every API call returns CORS error
Your frontend URL probably doesn't match `https://*.app.github.dev`. Check the
URL — Codespaces URLs always end with `.app.github.dev`. If you've set
`CORS_ORIGINS` as a secret to override the default, make sure your origin is
included in the comma-separated list.

### Backend logs show "Could not initialize proxy ... no session"
This was a known bug fixed via `@JsonIgnore` on `Pharmacy.owner`. If you see
it now, you may have an old branch. Pull from main.

### `seed-db.sh` says "tables don't exist yet"
You haven't started the backend yet. Run `./mvnw spring-boot:run` from `pc/`,
wait for `Started Application`, then re-run the seed script (you can leave the
backend running in another terminal).

### Re-running `seed-db.sh` fails with primary-key violations
The seed files use fixed UUIDs. To reseed cleanly, uncomment the `TRUNCATE`
block at the top of `database/dummy_data.sql` before running the script.

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
