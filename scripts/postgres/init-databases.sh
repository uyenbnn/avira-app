#!/usr/bin/env bash
# Creates per-service PostgreSQL databases if they do not already exist.
#
# This script runs automatically on first container initialisation via
# docker-entrypoint-initdb.d.  It is idempotent – safe to reference even if
# the volume already contains data (Docker won't re-run it in that case).
#
# Database naming convention: replace hyphens with underscores in service name.
#   user-service             → user_service
#   project-service          → project_service
#   administration-service   → administration_service
#   business-service         → business_service
#
# The `keycloak` database is already provisioned by POSTGRES_DB in docker-compose.yml.

set -euo pipefail

DATABASES=(
    user_service
    project_service
    administration_service
    business_service
    iam_service
)

for DB in "${DATABASES[@]}"; do
    psql -v ON_ERROR_STOP=1 \
         --username "$POSTGRES_USER" \
         --dbname   "postgres" \
         <<-EOSQL
            SELECT 'CREATE DATABASE "$DB"'
            WHERE NOT EXISTS (
                SELECT FROM pg_database WHERE datname = '$DB'
            )\gexec
EOSQL
    echo "[init-databases] Ensured database exists: $DB"
done

