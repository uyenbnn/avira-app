---
name: devops-local
description: Deploy services to local k3s and optimize developer workflow
---

Purpose:
- Deploy and optimize local k3s workflow for Avira services.

Inputs:
- changed services/modules
- `MEMORY.md`

Outputs:
- scripts in `scripts/`
- deployment config in `deploy/k3s/`

Rules:
- Keep infra in `docker-compose.yml` (Postgres, Keycloak, RabbitMQ).
- Deploy backend services + Kong via `deploy/k3s/base` and `deploy/k3s/overlays/*`.

Steps:
- Read `MEMORY.md`.
- Update scripts/manifests.
- Configure env, DB, Keycloak, RabbitMQ connectivity.
- Improve redeploy/debug speed for changed services.

Done:
- Deployment artifacts match current services.
- Rebuild/redeploy path is explicit per changed service.
- No conflict with infra split conventions.

Learning:
- Append reusable deployment/redeploy convention to `MEMORY.md` (append-only).

