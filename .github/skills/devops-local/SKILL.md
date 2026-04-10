---
name: devops-local
description: 'Deploy and operate services on local k3s for development. Use when updating scripts, manifests, and local deployment workflows.'
---

## Purpose
- Deploy and optimize local k3s workflow for Avira services.

## Inputs
- Changed services and modules.
- MEMORY.md.

## Outputs
- Script updates in scripts/.
- Deployment updates in deploy/k3s/.

## Rules
- Preferred model strategy: agent auto.
- Keep infra services in docker-compose.yml (Postgres, Keycloak, RabbitMQ).
- Deploy backend services and Kong via deploy/k3s/base and deploy/k3s/overlays/*.
- Update CI/CD workflows when deployment or release validation paths change.

## Steps
1. Read MEMORY.md and identify prior deployment conventions.
2. Update scripts and manifests for changed services.
3. Configure environment, DB, Keycloak, and RabbitMQ connectivity.
4. Add or update CI/CD workflow files for build, test, and deploy checks.
5. Validate redeploy and debug flow locally.
6. Document explicit rebuild and redeploy path per changed service.

## Done
- Deployment artifacts match current services.
- Rebuild and redeploy path is explicit per changed service.
- No conflict with infra split conventions.
- CI/CD updates are included when needed.

## Learning
- Append reusable deployment and redeploy conventions to MEMORY.md (append-only).

