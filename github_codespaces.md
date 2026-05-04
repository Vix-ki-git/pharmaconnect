What Codespaces actually is

GitHub Codespaces is not a hosting service. It's a remote Linux VM that GitHub spins up on demand for you to develop in. Think of it  
as: GitHub clones your repo onto a fresh Ubuntu machine in the cloud, hands you a VS Code window connected to it, and forwards any    
ports your apps listen on so you can hit them in your browser.

So when you say "deploy on Codespaces," what we're really setting up is: anyone can click "Open in Codespaces" on your repo and have
the full PharmaConnect stack (backend + frontend + a temporary MySQL) running and reachable in their browser within ~3 minutes,       
without installing Java, Node, or MySQL locally. That's perfect for demos, code reviews, and onboarding teammates. It is not what
you'd use to run PharmaConnect for real users — that needs a different conversation (Fly.io / Render / AWS for the backend,
Vercel/Netlify for the frontend, RDS/PlanetScale for the DB).

If "Codespaces demo" is what you want — read on. If you actually meant production, tell me and we'll redo the plan.

Scope you specified

You said: "only frontend and backend, don't touch other stuff." Got it. That means:
- ✅ I'll modify code in pc/ (backend) and pc-frontend/ (frontend)
- ✅ I'll create a new .devcontainer/ folder at the repo root (this is the only way Codespaces knows what to install)
- ❌ I won't touch uploads/, petz-automation/, Pharma Connect Pictures/
- ⚠️ One nuance: database/*.sql — I won't modify those files, but the MySQL container needs to read them at startup to populate the DB
  (otherwise the backend has empty tables). I'll reference them from the devcontainer config without changing them. OK?

What's actually changing and why — in plain English

Right now, your project has four hardcoded assumptions that work on your laptop but break anywhere else:

┌─────────────────────────────┬────────────────────────┬─────────────────────────────────────────┬───────────────────────────────┐
│    Hardcoded assumption     │      Where it is       │       Why it breaks in Codespaces       │        What we change         │
├─────────────────────────────┼────────────────────────┼─────────────────────────────────────────┼───────────────────────────────┤
│ MySQL is at localhost:3306  │                        │                                         │ Run MySQL as a sidecar        │
│ with your password          │ application.properties │ Codespaces VM has no MySQL installed    │ container, read password from │
│                             │                        │                                         │  a Codespaces secret          │
├─────────────────────────────┼────────────────────────┼─────────────────────────────────────────┼───────────────────────────────┤
│ Frontend talks to           │ every Angular service  │ Browser is on your laptop, backend is   │ Make the API URL              │
│ http://localhost:8082       │ file                   │ on a remote VM — localhost means your   │ configurable, set it to the   │
│                             │                        │ laptop, not the VM                      │ Codespace's public URL        │
├─────────────────────────────┼────────────────────────┼─────────────────────────────────────────┼───────────────────────────────┤
│                             │                        │ Frontend in Codespaces lives at a       │ Replace per-controller CORS   │
│ Backend only allows CORS    │ every @RestController  │ https://*.app.github.dev URL, not       │ with one global setting that  │
│ from http://localhost:4200  │                        │ localhost:4200                          │ reads allowed origins from    │
│                             │                        │                                         │ config                        │
├─────────────────────────────┼────────────────────────┼─────────────────────────────────────────┼───────────────────────────────┤
│ Real DB password committed  │                        │ Already a security issue; also          │ Use env vars (${DB_PASSWORD}) │
│ in application.properties   │ line 12                │ application.properties is gitignored so │  and store the actual value   │
│                             │                        │  it won't even exist in a fresh clone   │ as a Codespaces secret        │
└─────────────────────────────┴────────────────────────┴─────────────────────────────────────────┴───────────────────────────────┘

And one missing piece:

┌────────────────────────────────────────┬────────────────────────────────────────────────────────────────────────────────────────┐
│             Missing piece              │                                     Why we need it                                     │
├────────────────────────────────────────┼────────────────────────────────────────────────────────────────────────────────────────┤
│ .devcontainer/devcontainer.json +      │ This is the file Codespaces reads to know "install Java 21, install Node 20, run a     │
│ docker-compose.yml                     │ MySQL container, copy the example properties file, install npm deps." Without it, a    │
│                                        │ fresh Codespace gives you a blank Ubuntu box with none of your tools.                  │
└────────────────────────────────────────┴────────────────────────────────────────────────────────────────────────────────────────┘

That's the whole story. Eight tasks, but they're all in service of those five things.

How a teammate uses it once we're done

1. Goes to your repo on github.com
2. Clicks the green Code button → Codespaces tab → Create codespace on main
3. Waits ~2–3 min while GitHub builds the container (Java + Node + MySQL spin up)
4. A VS Code window opens in their browser, already in the project
5. Two terminal commands: cd pc && ./mvnw spring-boot:run and (in another terminal) cd pc-frontend && npm start
6. VS Code's "Ports" panel shows two clickable URLs. Click the 4200 one — the app is live.

That's it. No JDK install, no MySQL setup, no application.properties tweaking.

  ---
My proposal for how we proceed

I'll do tasks 2 → 7 one at a time, explaining what and why before each edit, in this order:

1. Task #2 (~5 min): Clean up application.properties.example and switch the real application.properties to read from env vars. Why
   first: nothing else works until secrets stop being hardcoded.
2. Task #3 (~10 min): Global CORS config. Why next: small contained backend change, easy to verify.
3. Task #4 (~15 min): Configurable API base URL in Angular. Why next: largest grep across frontend, do it before the devcontainer so
   the demo works.
4. Task #5 (~15 min): Write the .devcontainer/ files. Why now: depends on knowing the env var names from #2, the CORS property from
   #3, and the build setup from #4.
5. Task #6 (~5 min): Wire up the SQL seed files into the MySQL container.
6. Task #7 (~5 min): A short note (in issues_to_be_resolved.md or a new DEPLOYMENT.md — your call) listing the exact Codespaces
   secrets you'll need to add.

Then Task #8 is something you do — clicking "Create codespace" and verifying the app loads. I can't do that for you.

Two things I need from you before I start:

1. Confirm the database scope nuance: OK for me to reference database/*.sql from the devcontainer config (read-only, no edits to the
   files themselves)? If not, your teammates will boot Codespaces with an empty DB and have to seed manually.
2. Where should I put the secrets list? New file DEPLOYMENT.md, or append to issues_to_be_resolved.md? DEPLOYMENT.md would be
   committed (visible to teammates); issues_to_be_resolved.md is gitignored (just for you).

Say go and I'll start with Task #2, narrating each change.

✻ Cooked for 59s

9 tasks (1 done, 8 open)
◻ Fix application.properties.example and parameterize secrets via env vars
◻ Replace per-controller @CrossOrigin with env-driven global CORS config
◻ Make Angular API base URL configurable via environment.ts
◻ Create .devcontainer setup with Java 21, Node 20+, and MySQL 8
◻ Auto-seed MySQL with database/*.sql on container init
… +3 pending, 1 completed


Then Task #8 is something you do — clicking "Create codespace" and verifying the app loads. I can't do that for you.

Two things I need from you before I start:

1. Confirm the database scope nuance: OK for me to reference database/*.sql from the devcontainer config (read-only, no edits to the
   files themselves)? If not, your teammates will boot Codespaces with an empty DB and have to seed manually.
2. Where should I put the secrets list? New file DEPLOYMENT.md, or append to issues_to_be_resolved.md? DEPLOYMENT.md would be
   committed (visible to teammates); issues_to_be_resolved.md is gitignored (just for you).

Say go and I'll start with Task #2, narrating each change.

✻ Cooked for 59s

9 tasks (1 done, 8 open)
◻ Fix application.properties.example and parameterize secrets via env vars
◻ Replace per-controller @CrossOrigin with env-driven global CORS config
◻ Make Angular API base URL configurable via environment.ts
◻ Create .devcontainer setup with Java 21, Node 20+, and MySQL 8
◻ Auto-seed MySQL with database/*.sql on container init
… +3 pending, 1 completed