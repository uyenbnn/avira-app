param(
    [switch]$StartInfra = $true,
    [switch]$BuildImages = $true,
    [switch]$DeployK8s = $true,
    [switch]$PortForward,
    [switch]$DryRun,
    [string]$RepoRoot = "D:\work\avira-app"
)

$ErrorActionPreference = "Stop"

function Invoke-Step {
    param(
        [string]$Title,
        [scriptblock]$Action
    )

    Write-Host "`n=== $Title ===" -ForegroundColor Cyan
    if ($DryRun) {
        Write-Host "[dry-run] skipped execution" -ForegroundColor Yellow
        return
    }
    & $Action
}

function Assert-CommandExists {
    param([string]$Name)
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Required command '$Name' is not available in PATH."
    }
}

Assert-CommandExists -Name "docker"
Assert-CommandExists -Name "mvn"
Assert-CommandExists -Name "kubectl"

$pomPath = Join-Path $RepoRoot "pom.xml"
$overlayPath = Join-Path $RepoRoot "deploy\k3s\overlays\local"
$composePath = Join-Path $RepoRoot "docker-compose.yml"

if (-not (Test-Path $pomPath)) {
    throw "Cannot find pom.xml at '$pomPath'"
}
if (-not (Test-Path $overlayPath)) {
    throw "Cannot find k8s overlay at '$overlayPath'"
}
if (-not (Test-Path $composePath)) {
    throw "Cannot find docker-compose.yml at '$composePath'"
}

if ($StartInfra) {
    Invoke-Step -Title "Start infrastructure (docker compose: postgres, rabbitmq, keycloak)" -Action {
        docker compose -f $composePath up -d postgres rabbitmq keycloak
    }
}

if ($BuildImages) {
    Invoke-Step -Title "Build app images" -Action {
        mvn -f $pomPath -pl authentication-service spring-boot:build-image "-Dspring-boot.build-image.imageName=avira/authentication-service:local" -DskipTests
        mvn -f $pomPath -pl user-service spring-boot:build-image "-Dspring-boot.build-image.imageName=avira/user-service:local" -DskipTests
        mvn -f $pomPath -pl project-service spring-boot:build-image "-Dspring-boot.build-image.imageName=avira/project-service:local" -DskipTests
        mvn -f $pomPath -pl application-initialization-service spring-boot:build-image "-Dspring-boot.build-image.imageName=avira/application-initialization-service:local" -DskipTests
    }
}

if ($DeployK8s) {
    Invoke-Step -Title "Deploy app services to K3s" -Action {
        kubectl apply -k $overlayPath
        kubectl -n avira get pods
    }
}

if ($PortForward) {
    Write-Host "`n=== Port-forward commands (run each in a separate terminal) ===" -ForegroundColor Green
    Write-Host "kubectl -n avira port-forward svc/application-initialization-service 18080:10000"
    Write-Host "kubectl -n avira port-forward svc/authentication-service 18081:10001"
    Write-Host "kubectl -n avira port-forward svc/user-service 18082:10000"
    Write-Host "kubectl -n avira port-forward svc/project-service 18084:10004"
}

Write-Host "`nDone." -ForegroundColor Green

