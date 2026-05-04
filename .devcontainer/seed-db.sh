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

DB_HOST="${DB_HOST:-localhost}"
DB_PASSWORD="${DB_PASSWORD:-pharmaconnect_dev}"

MYSQL="mysql -h $DB_HOST -uroot -p$DB_PASSWORD pharmaconnect"

# Wait up to 60s for tables to exist (backend may still be booting if called
# from an auto-run task right after backend startup).
echo "==> Waiting for backend tables to exist (up to 60s)..."
for i in $(seq 1 30); do
  if $MYSQL -e "SELECT 1 FROM medicine LIMIT 1;" >/dev/null 2>&1; then
    break
  fi
  if [ "$i" -eq 30 ]; then
    cat <<EOF

ERROR: tables don't exist after waiting 60s.
Make sure the Spring Boot backend has started:
  cd pc && ./mvnw spring-boot:run
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
