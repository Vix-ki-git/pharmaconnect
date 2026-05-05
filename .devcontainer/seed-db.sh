#!/usr/bin/env bash
# Loads sample data into the pharmaconnect MySQL database.
#
# Idempotent: if the medicine table already has rows, this script exits cleanly
# without re-running the seed scripts (which would otherwise fail with primary
# key violations because the seed files use fixed UUIDs).
#
# Requires the Spring Boot backend to have been started at least once so
# Hibernate has created the tables.
set -euo pipefail

cd "$(dirname "$0")/.."
REPO_ROOT="$(pwd)"

# Use 127.0.0.1 not "localhost" — the mysql CLI treats "localhost" as a Unix
# socket connection, but we don't have a socket (MySQL runs in a sidecar
# container). 127.0.0.1 forces a TCP connection which actually works.
DB_HOST="${DB_HOST:-127.0.0.1}"
[ "$DB_HOST" = "localhost" ] && DB_HOST=127.0.0.1
DB_PASSWORD="${DB_PASSWORD:-pharmaconnect_dev}"

# Verify the mysql client is available — give a clear error instead of failing
# silently in the wait loop below.
if ! command -v mysql >/dev/null 2>&1; then
  cat <<EOF

ERROR: 'mysql' command not found.
Install the MySQL client and retry:
  sudo apt-get update && sudo apt-get install -y default-mysql-client
EOF
  exit 1
fi

MYSQL="mysql -h $DB_HOST -uroot -p$DB_PASSWORD pharmaconnect"

# Wait up to 180s for tables to exist (backend startup on a fresh codespace
# can take 60-120s due to Maven dependency download on first run).
echo "==> Waiting for backend tables to exist (up to 180s)..."
for i in $(seq 1 90); do
  if $MYSQL -e "SELECT 1 FROM medicine LIMIT 1;" >/dev/null 2>&1; then
    break
  fi
  # Print the actual error on the first iteration and every 30s after, so
  # failures are visible instead of silently retrying. The `|| true` is
  # required because `set -e` + `pipefail` would otherwise kill the script
  # on the first failed mysql attempt.
  if [ "$i" -eq 1 ] || [ $((i % 15)) -eq 0 ]; then
    echo "    Attempt $i — mysql says:"
    { $MYSQL -e "SELECT 1 FROM medicine LIMIT 1;" 2>&1 || true; } | sed 's/^/      /'
  fi
  if [ "$i" -eq 90 ]; then
    cat <<EOF

ERROR: tables don't exist after waiting 180s.
Likely cause: the Spring Boot backend hasn't finished booting yet, or it
crashed during startup. Check the "PharmaConnect: Backend" task panel.

You can also verify MySQL connectivity manually:
  mysql -h 127.0.0.1 -uroot -p$DB_PASSWORD pharmaconnect -e 'SHOW TABLES'
EOF
    exit 1
  fi
  sleep 2
done

# Idempotency check: if there's already data, skip.
COUNT=$($MYSQL -sN -e "SELECT COUNT(*) FROM medicine;" 2>/dev/null || echo 0)
if [ "$COUNT" -gt 0 ]; then
  echo "==> medicine table already has $COUNT rows — skipping seed."
  echo "    To force reseed: uncomment the TRUNCATE block at the top of"
  echo "    database/dummy_data.sql, then re-run this script."
  exit 0
fi

echo "==> Loading database/seed_data.sql (medicine catalog)..."
$MYSQL < "$REPO_ROOT/database/seed_data.sql"

echo "==> Loading database/dummy_data.sql (demo users, pharmacies, stock)..."
$MYSQL < "$REPO_ROOT/database/dummy_data.sql"

echo ""
echo "============================================================"
echo " Seed complete."
echo " Demo user passwords are 'password' (per dummy_data.sql notes)."
echo "============================================================"
