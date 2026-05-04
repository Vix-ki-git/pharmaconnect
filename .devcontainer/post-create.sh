#!/usr/bin/env bash
# Runs once, the first time a devcontainer/codespace is built.
# Idempotent — safe to re-run.
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$REPO_ROOT"

echo "==> Installing MySQL client (needed by seed-db.sh)..."
if ! command -v mysql >/dev/null 2>&1; then
  sudo apt-get update -qq
  sudo apt-get install -y -qq default-mysql-client
else
  echo "    Already installed"
fi

echo "==> Bootstrapping application.properties from example (if missing)..."
PROP_FILE="pc/src/main/resources/application.properties"
EXAMPLE_FILE="pc/src/main/resources/application.properties.example"
if [ ! -f "$PROP_FILE" ] && [ -f "$EXAMPLE_FILE" ]; then
  cp "$EXAMPLE_FILE" "$PROP_FILE"
  echo "    Created $PROP_FILE"
else
  echo "    Skipped (file already exists or example missing)"
fi

echo "==> Warming Maven dependency cache..."
chmod +x pc/mvnw
( cd pc && ./mvnw dependency:go-offline -DskipTests -q ) || \
  echo "    Warning: maven warmup failed; first build will fetch deps"

echo "==> Installing frontend npm dependencies..."
( cd pc-frontend && npm ci ) || \
  echo "    Warning: npm ci failed; run it manually before 'npm start'"

echo ""
echo "============================================================"
echo " Setup complete. First-time run order:"
echo ""
echo "  1. cd pc && ./mvnw spring-boot:run"
echo "       (wait for 'Started Application'; this creates DB tables)"
echo ""
echo "  2. bash .devcontainer/seed-db.sh"
echo "       (loads sample medicines, users, pharmacies)"
echo ""
echo "  3. cd pc-frontend && npm start -- --host 0.0.0.0"
echo "       (port 4200 will auto-open in your browser)"
echo ""
echo " On subsequent codespace sessions, only steps 1 and 3 are needed"
echo " unless the codespace has been recreated."
echo "============================================================"
