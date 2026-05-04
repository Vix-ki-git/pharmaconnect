#!/usr/bin/env bash
# Loads sample data into the pharmaconnect MySQL database.
#
# IMPORTANT: Run this AFTER starting the Spring Boot backend at least once,
# so Hibernate has created the tables. Order:
#   1. cd pc && ./mvnw spring-boot:run     (wait for startup, then stop or new terminal)
#   2. bash .devcontainer/seed-db.sh       (you are here)
#   3. cd pc-frontend && npm start
#
# This script is idempotent-ish: re-running it will likely fail with primary-key
# violations because the seed files use fixed UUIDs. If you want a clean reseed,
# uncomment the TRUNCATE block at the top of database/dummy_data.sql first.
set -euo pipefail

cd "$(dirname "$0")/.."
REPO_ROOT="$(pwd)"

DB_HOST="${DB_HOST:-localhost}"
DB_PASSWORD="${DB_PASSWORD:-pharmaconnect_dev}"

MYSQL="mysql -h $DB_HOST -uroot -p$DB_PASSWORD pharmaconnect"

echo "==> Verifying backend has been started (tables must exist)..."
if ! $MYSQL -e "SELECT 1 FROM medicine LIMIT 1;" >/dev/null 2>&1; then
  cat <<EOF

ERROR: tables don't exist in the 'pharmaconnect' database yet.
Start the backend at least once so Hibernate creates the schema:

  cd pc && ./mvnw spring-boot:run

Wait until you see "Started Application" in the logs, then re-run this script
(you can leave the backend running — open a second terminal).
EOF
  exit 1
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
