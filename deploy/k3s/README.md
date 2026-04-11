# K3s Deployment (SaaS MVP)

This deploys SaaS MVP application workloads into namespace `avira`:

- `iam-service`
- `platform-service`
- `application-service`
- `kong` gateway

Infra stays in `docker-compose.yml`:

- `postgres` (5455)
- `rabbitmq` (5672/15672/5552)
- `keycloak` (8080)

## Kubernetes resources

Base resources are in `deploy/k3s/base`:

- Namespace: `namespace.yaml`
- ConfigMap: `configmap-app-env.yaml`
- Secret: `iam-service-secret.yaml`
- Deployments/Services:
	- `iam-service-*.yaml`
	- `platform-service-*.yaml`
	- `application-service-*.yaml`
	- `kong-*.yaml`

Overlay is `deploy/k3s/overlays/local` and pins local image tags.

## One-command local deploy

```powershell
powershell -ExecutionPolicy Bypass -File D:\project\avira-app\scripts\deploy-k3s-local.ps1
```

What the script does:

1. `docker compose up -d postgres rabbitmq keycloak`
2. Builds images:
	 - `avira/iam-service:local`
	 - `avira/platform-service:local`
	 - `avira/application-service:local`
3. `kubectl apply -k deploy/k3s/overlays/local`
4. Waits rollout for all 4 deployments
5. Prints port-forward commands for host reachability

## Endpoint exposure from localhost

Run each in a dedicated terminal:

```powershell
kubectl -n avira port-forward svc/kong 10001:8000
kubectl -n avira port-forward svc/iam-service 8081:8080
kubectl -n avira port-forward svc/platform-service 10004:8080
kubectl -n avira port-forward svc/application-service 10002:8080
```

Primary test/UI URL through gateway:

- `http://localhost:10001`

Direct service URLs (optional diagnostics):

- `http://localhost:8081` (iam-service)
- `http://localhost:10004` (platform-service)
- `http://localhost:10002` (application-service)

## Validation commands

```powershell
kubectl -n avira get deployments
kubectl -n avira get pods
kubectl -n avira get svc
```

MVP integration tests:

```powershell
cd D:\project\avira-app\integration-tests\node-axios
npm ci
npm run test:mvp:docker
```

## Redeploy

```powershell
kubectl delete -k D:\project\avira-app\deploy\k3s\overlays\local --ignore-not-found=true
powershell -ExecutionPolicy Bypass -File D:\project\avira-app\scripts\deploy-k3s-local.ps1
```

## Caveats

- IAM uses `IAM_INIT_KEYCLOAK_BASE_URL` / `IAM_AUTH_KEYCLOAK_BASE_URL` set to `http://host.k3d.internal:8080`.
- If `host.k3d.internal` is not resolvable in your K3s distribution, change these to a reachable host alias (for example `host.docker.internal`) in `deploy/k3s/base/configmap-app-env.yaml`.
- `keycloak` is intentionally not deployed in K3s for local MVP; it is provided by Docker Compose.

