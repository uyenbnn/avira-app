param(
    [switch]$StopInfra = $true,
    [switch]$DeleteK8s = $true,
    [switch]$RemoveVolumes,
    [switch]$DryRun,
    [string]$Namespace = "avira",
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
Assert-CommandExists -Name "kubectl"

$composePath = Join-Path $RepoRoot "docker-compose.yml"
$overlayPath = Join-Path $RepoRoot "deploy\k3s\overlays\local"

if (-not (Test-Path $composePath)) {
    throw "Cannot find docker-compose.yml at '$composePath'"
}
if (-not (Test-Path $overlayPath)) {
    throw "Cannot find k8s overlay at '$overlayPath'"
}

if ($DeleteK8s) {
    Invoke-Step -Title "Delete K3s app resources" -Action {
        kubectl delete -k $overlayPath --ignore-not-found=true
        kubectl delete namespace $Namespace --ignore-not-found=true
    }
}

if ($StopInfra) {
    Invoke-Step -Title "Stop docker-compose infrastructure" -Action {
        docker compose -f $composePath stop postgres rabbitmq keycloak
        docker compose -f $composePath rm -f postgres rabbitmq keycloak
    }
}

if ($RemoveVolumes) {
    Invoke-Step -Title "Remove docker-compose volumes (destructive)" -Action {
        docker compose -f $composePath down -v
    }
}

Write-Host "`nTeardown done." -ForegroundColor Green

