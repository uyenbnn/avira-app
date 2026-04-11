# Feedback: iam-service login endpoint returned 404 in k3s local deploy

## Summary
`POST /api/iam/auth/login` returned `404` on deployed `iam-service` and through Kong, while source `AuthController` mapping existed.

## Root cause
Local k3s deploy reused a stale image due static tag reuse:
- Deployment used `avira/iam-service:local` with `imagePullPolicy: IfNotPresent`.
- Running pod image digest was `sha256:b485...`, while current local build digest was `sha256:c148...`.
- Because the tag stayed constant, kubelet kept using previously cached image, so runtime did not include latest `AuthController` endpoint.

## Fix applied
Updated local deploy script to avoid stale tag reuse:
- Build images with unique timestamp tag per deploy.
- After `kubectl apply -k`, explicitly set deployments to those freshly built tags.

File changed:
- `scripts/deploy-k3s-local.ps1`

## Verification
Before fix:
- Direct: `POST http://localhost:8081/api/iam/auth/login` -> `404`
- Gateway: `POST http://localhost:10001/api/iam/auth/login` -> `404`

After targeted iam-service redeploy with unique tag:
- Direct: `POST http://localhost:8081/api/iam/auth/login` -> `400` with body `{"message":"tenantId is required"}`
- Gateway: `POST http://localhost:10001/api/iam/auth/login` -> `400` with body `{"message":"tenantId is required"}`

This confirms endpoint is registered and reachable (no longer `404`).

## Notes
- No API contract change required; OpenAPI remains valid for this endpoint.
- Only `iam-service` was redeployed for incident resolution.
