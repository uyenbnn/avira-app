# K3s Deployment (IAM + Kong)

This folder currently deploys:

- `iam-service`
- `kong`

Infrastructure remains outside K3s in `docker-compose`:

- `postgres` (host port `5455`)
- `keycloak` (host port `8080`)

## 1) Start required infrastructure

```powershell
cd D:\work\avira-app
docker compose up -d postgres keycloak
```

## 2) Build IAM image

```powershell
mvn --% -f D:\work\avira-app\iam-service\pom.xml spring-boot:build-image -Dspring-boot.build-image.imageName=avira/iam-service:local
```

## 3) Validate manifests

```powershell
kubectl kustomize D:\work\avira-app\deploy\k3s\overlays\local
```

## 4) Deploy to K3s

```powershell
kubectl apply -k D:\work\avira-app\deploy\k3s\overlays\local
kubectl -n avira get pods
```

## 5) Port-forward for local test

Open terminals:

```powershell
kubectl -n avira port-forward svc/iam-service 18083:10003
kubectl -n avira port-forward svc/kong 8000:8000 8001:8001
```

Then run IAM integration tests:

```powershell
cd D:\work\avira-app\integration-tests\node-axios
npm ci
npm run test:iam:k3s
```

## Notes

- Base config uses `host.docker.internal` for infra access from K3s pods.
- If your K3s distro does not resolve this host alias, replace it with `host.k3d.internal` in `deploy/k3s/base/configmap-app-env.yaml` and `deploy/k3s/base/iam-service.yaml`.

