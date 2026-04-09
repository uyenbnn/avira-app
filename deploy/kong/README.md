# Kong Gateway (K3s)

Kong runs in K3s in DB-less mode for API gateway routing.

## Upstream port mapping

Kong routes to IAM service:

- iam-service -> `8081` (local mode)
- iam-service -> `10003` (in-cluster mode)

If your local ports differ, update `deploy/k3s/base/kong.yaml`.

## Start local infra + expose Kong from K3s

```powershell
docker compose -f D:\work\avira-app\docker-compose.yml up -d postgres keycloak
kubectl port-forward svc/kong 8000:8000 8001:8001 --address 0.0.0.0 -n avira
```

## Test through gateway

Gateway base URL: `http://localhost:8000`

- IAM auth: `http://localhost:8000/api/iam/auth/*`
- IAM tenant init: `http://localhost:8000/api/iam/init/*`
- IAM realm resolve: `http://localhost:8000/api/iam/realms/*`
- IAM users: `http://localhost:8000/api/iam/users/*`

## Kong Admin API (local dev)

- URL: `http://localhost:8001`
- Example:

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8001/services"
```

