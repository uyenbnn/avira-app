# Kong Gateway (K3s)

Kong runs in K3s in DB-less mode for API gateway routing.

## Upstream port mapping

Kong routes to local Spring Boot services running on host ports:

- authentication-service -> `10001`
- user-service -> `10002`
- project-service -> `10004`
- application-initialization-service -> `10000`

If your local ports differ, update `deploy/k3s/base/kong.yaml`.

## Start local infra + expose Kong from K3s

```powershell
docker compose -f D:\work\avira-app\docker-compose.yml up -d postgres rabbitmq keycloak
kubectl port-forward svc/kong 8000:8000 8001:8001 --address 0.0.0.0 -n avira
```

## Test through gateway

Gateway base URL: `http://localhost:8000`

- Auth register/login: `http://localhost:8000/api/auth/*`
- Users: `http://localhost:8000/api/users/*`
- Tenants: `http://localhost:8000/api/tenants/*`
- Applications: `http://localhost:8000/api/applications` and `http://localhost:8000/api/tenants/{tenantId}/applications/*`
- Initialization: `http://localhost:8000/api/init/*`

## Kong Admin API (local dev)

- URL: `http://localhost:8001`
- Example:

```powershell
Invoke-RestMethod -Method Get -Uri "http://localhost:8001/services"
```

