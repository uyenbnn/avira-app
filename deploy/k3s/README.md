# K3s Deployment (Apps Only + External Infra)

This folder deploys application services to K3s and keeps infrastructure in `docker-compose`:

- `authentication-service`
- `user-service`
- `project-service`
- `application-initialization-service`

Infrastructure source of truth:

- `postgres` (host port `5455`)
- `rabbitmq` (host ports `5672`, `15672`, `5552`)
- `keycloak` (host port `8080`)

## 1) Start infrastructure with docker-compose

```bash
cd D:/work/avira-app
docker compose up -d postgres rabbitmq keycloak
```

## 2) Build application images

Use Spring Boot buildpacks (creates local Docker images):

```bash
mvn -f D:/work/avira-app/pom.xml -pl authentication-service spring-boot:build-image -Dspring-boot.build-image.imageName=avira/authentication-service:local
mvn -f D:/work/avira-app/pom.xml -pl user-service spring-boot:build-image -Dspring-boot.build-image.imageName=avira/user-service:local
mvn -f D:/work/avira-app/pom.xml -pl project-service spring-boot:build-image -Dspring-boot.build-image.imageName=avira/project-service:local
mvn -f D:/work/avira-app/pom.xml -pl application-initialization-service spring-boot:build-image -Dspring-boot.build-image.imageName=avira/application-initialization-service:local
```

> If your K3s runtime cannot see local Docker images, push these images to a registry and update image names in `deploy/k3s/base/*.yaml`.

## 3) Deploy app services to K3s

```bash
kubectl apply -k D:/work/avira-app/deploy/k3s/overlays/local
kubectl -n avira get pods
```

## 4) Port-forward for local integration tests

Open 4 terminals:

```bash
kubectl -n avira port-forward svc/application-initialization-service 18080:10000
kubectl -n avira port-forward svc/authentication-service 18081:10001
kubectl -n avira port-forward svc/user-service 18082:10002
kubectl -n avira port-forward svc/project-service 18084:10004
```

Then run integration tests:

```bash
cd D:/work/avira-app/integration-tests/node-axios
npm ci
npm run test:k3s
```

## 5) Bootstrap streams/realm explicitly (optional)

```bash
curl -X POST http://localhost:18080/api/init/keycloak
curl -X POST http://localhost:18080/api/init/messaging
```

## Notes

- Current base config points pods to host infra via `host.docker.internal`.
- If your K3s distribution does not resolve that host alias (common with k3d), replace it with `host.k3d.internal` in `deploy/k3s/base/configmap-app-env.yaml` and `deploy/k3s/base/authentication-service.yaml`.

## One-command helper script (Windows PowerShell)

Run all steps (start infra + build images + deploy apps):

```bash
powershell -ExecutionPolicy Bypass -File D:/work/avira-app/scripts/build-deploy.ps1
```

Show what would run without executing:

```bash
powershell -ExecutionPolicy Bypass -File D:/work/avira-app/scripts/build-deploy.ps1 -DryRun
```

Deploy only (skip infra/image build):

```bash
powershell -ExecutionPolicy Bypass -File D:/work/avira-app/scripts/build-deploy.ps1 -StartInfra:$false -BuildImages:$false -DeployK8s:$true
```

Teardown (delete K3s apps + stop infra containers):

```bash
powershell -ExecutionPolicy Bypass -File D:/work/avira-app/scripts/teardown.ps1
```

Teardown dry-run:

```bash
powershell -ExecutionPolicy Bypass -File D:/work/avira-app/scripts/teardown.ps1 -DryRun
```

Teardown and remove compose volumes (destructive):

```bash
powershell -ExecutionPolicy Bypass -File D:/work/avira-app/scripts/teardown.ps1 -RemoveVolumes
```

