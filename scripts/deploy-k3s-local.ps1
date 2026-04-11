param(
    [string]$RepoRoot = "D:\project\avira-app",
    [string]$Namespace = "avira",
    [switch]$SkipBuild,
    [switch]$SkipInfra
)

$ErrorActionPreference = "Stop"

function Assert-CommandExists {
    param([string]$Name)
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Required command '$Name' is not available in PATH."
    }
}

function Invoke-Checked {
    param(
        [string]$Title,
        [scriptblock]$Action
    )

    Write-Host "`n=== $Title ===" -ForegroundColor Cyan
    & $Action
    if ($LASTEXITCODE -ne 0) {
        throw "Step failed: $Title (exit code $LASTEXITCODE)"
    }
}

Assert-CommandExists -Name "docker"
Assert-CommandExists -Name "mvn"
Assert-CommandExists -Name "kubectl"

$pomPath = Join-Path $RepoRoot "pom.xml"
$composePath = Join-Path $RepoRoot "docker-compose.yml"
$overlayPath = Join-Path $RepoRoot "deploy\k3s\overlays\local"

$buildTag = "local-" + (Get-Date -Format "yyyyMMddHHmmss")
$iamImage = "avira/iam-service:$buildTag"
$platformImage = "avira/platform-service:$buildTag"
$applicationImage = "avira/application-service:$buildTag"

if (-not (Test-Path $pomPath)) {
    throw "Cannot find pom.xml at '$pomPath'"
}
if (-not (Test-Path $composePath)) {
    throw "Cannot find docker-compose.yml at '$composePath'"
}
if (-not (Test-Path $overlayPath)) {
    throw "Cannot find overlay at '$overlayPath'"
}

if (-not $SkipInfra) {
    Invoke-Checked -Title "Start infrastructure (postgres, rabbitmq, keycloak)" -Action {
        docker compose -f $composePath up -d postgres rabbitmq keycloak
    }

    Invoke-Checked -Title "Wait for Keycloak readiness" -Action {
        $maxAttempts = 30
        for ($i = 1; $i -le $maxAttempts; $i++) {
            try {
                $response = Invoke-WebRequest -Uri "http://localhost:8080/realms/master/.well-known/openid-configuration" -UseBasicParsing -TimeoutSec 5
                if ($response.StatusCode -eq 200) {
                    Write-Host "Keycloak is ready."
                    break
                }
            }
            catch {
                if ($i -eq $maxAttempts) {
                    throw "Keycloak was not ready after $maxAttempts attempts."
                }
            }

            if ($i -lt $maxAttempts) {
                Write-Host "Waiting for Keycloak readiness... attempt $i/$maxAttempts"
                Start-Sleep -Seconds 2
            }
        }
    }
}

Invoke-Checked -Title "Ensure iam_service database exists" -Action {
    $dbExists = [string](docker exec avira-postgres psql -U postgres -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='iam_service'")
    if ([string]::IsNullOrWhiteSpace($dbExists) -or $dbExists.Trim() -ne "1") {
        docker exec avira-postgres psql -U postgres -d postgres -v ON_ERROR_STOP=1 -c "CREATE DATABASE iam_service"
    }
}

if (-not $SkipBuild) {
    Invoke-Checked -Title "Build local service images" -Action {
        Write-Host "Using build tag: $buildTag"
        mvn -f $pomPath -pl iam-service spring-boot:build-image "-Dspring-boot.build-image.imageName=$iamImage" -DskipTests
        mvn -f $pomPath -pl platform-service spring-boot:build-image "-Dspring-boot.build-image.imageName=$platformImage" -DskipTests
        mvn -f $pomPath -pl application-service spring-boot:build-image "-Dspring-boot.build-image.imageName=$applicationImage" -DskipTests
    }
}

Invoke-Checked -Title "Deploy manifests to k3s" -Action {
    kubectl apply -k $overlayPath
}

if (-not $SkipBuild) {
    Invoke-Checked -Title "Pin deployments to freshly built local images" -Action {
        kubectl -n $Namespace set image deployment/iam-service iam-service=$iamImage
        kubectl -n $Namespace set image deployment/platform-service platform-service=$platformImage
        kubectl -n $Namespace set image deployment/application-service application-service=$applicationImage
    }
}

Invoke-Checked -Title "Wait for deployments to roll out" -Action {
    kubectl -n $Namespace rollout status deployment/iam-service --timeout=180s
    kubectl -n $Namespace rollout status deployment/platform-service --timeout=180s
    kubectl -n $Namespace rollout status deployment/application-service --timeout=180s
    kubectl -n $Namespace rollout status deployment/kong --timeout=180s
}

Invoke-Checked -Title "Current pod and service status" -Action {
    kubectl -n $Namespace get deployments
    kubectl -n $Namespace get pods -o wide
    kubectl -n $Namespace get svc
}

Write-Host "`n=== Local endpoint exposure (run each in a dedicated terminal) ===" -ForegroundColor Green
Write-Host "kubectl -n $Namespace port-forward svc/kong 10001:8000"
Write-Host "kubectl -n $Namespace port-forward svc/iam-service 8081:8080"
Write-Host "kubectl -n $Namespace port-forward svc/platform-service 10004:8080"
Write-Host "kubectl -n $Namespace port-forward svc/application-service 10002:8080"

Write-Host "`nDeployment complete." -ForegroundColor Green
