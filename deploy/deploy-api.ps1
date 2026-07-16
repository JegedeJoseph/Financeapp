#!/bin/bash
# ============================================================
# deploy-api.ps1  (Windows PowerShell version)
# Run this from your Windows machine every time you want to
# push a new build of the Spring Boot API to Lightsail.
#
# Usage:
#   .\deploy\deploy-api.ps1 -KeyFile "C:\path\to\key.pem" -ServerIP "54.123.45.67"
# ============================================================

param(
    [Parameter(Mandatory=$true)]
    [string]$KeyFile,       # Path to your .pem SSH key file

    [Parameter(Mandatory=$true)]
    [string]$ServerIP,      # Public IP of your financeapp-api Lightsail instance

    [string]$User = "ubuntu",
    [string]$RemotePath = "/opt/financeapp-api/app.jar",
    [string]$ServiceName = "financeapp-api"
)

$ErrorActionPreference = "Stop"
$ProjectRoot = "$PSScriptRoot\.."
$JarPattern = "$ProjectRoot\target\*.jar"

Write-Host ""
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host "  FinanceApp API — Deploy to Lightsail" -ForegroundColor Cyan
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host ""

# --- Step 1: Build the JAR ---
Write-Host "[1/4] Building Spring Boot JAR..." -ForegroundColor Yellow
Set-Location $ProjectRoot
& ".\mvnw.cmd" clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "  ❌ Build FAILED. Aborting deploy." -ForegroundColor Red
    exit 1
}

# Find the built JAR
$JarFile = Get-ChildItem -Path "$ProjectRoot\target" -Filter "*.jar" | Select-Object -First 1
if (-not $JarFile) {
    Write-Host "  ❌ No JAR found in target/ after build. Aborting." -ForegroundColor Red
    exit 1
}
Write-Host "  ✅ JAR built: $($JarFile.FullName)" -ForegroundColor Green

# --- Step 2: Upload JAR to server ---
Write-Host ""
Write-Host "[2/4] Uploading JAR to server ($ServerIP)..." -ForegroundColor Yellow
scp -i $KeyFile -o StrictHostKeyChecking=no `
    $JarFile.FullName `
    "${User}@${ServerIP}:${RemotePath}"
if ($LASTEXITCODE -ne 0) {
    Write-Host "  ❌ SCP upload FAILED. Check key and IP." -ForegroundColor Red
    exit 1
}
Write-Host "  ✅ JAR uploaded successfully" -ForegroundColor Green

# --- Step 3: Restart the service ---
Write-Host ""
Write-Host "[3/4] Restarting $ServiceName service on server..." -ForegroundColor Yellow
ssh -i $KeyFile -o StrictHostKeyChecking=no "${User}@${ServerIP}" `
    "sudo systemctl restart $ServiceName"
if ($LASTEXITCODE -ne 0) {
    Write-Host "  ❌ Service restart FAILED. SSH into server and check logs." -ForegroundColor Red
    exit 1
}
Write-Host "  ✅ Service restarted" -ForegroundColor Green

# --- Step 4: Health check ---
Write-Host ""
Write-Host "[4/4] Waiting 20 seconds for app to start..." -ForegroundColor Yellow
Start-Sleep -Seconds 20

Write-Host "  Checking health endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://${ServerIP}:8080/actuator/health" -TimeoutSec 10
    if ($response.status -eq "UP") {
        Write-Host "  ✅ App is UP and healthy!" -ForegroundColor Green
    } else {
        Write-Host "  ⚠️  App responded but status is: $($response.status)" -ForegroundColor Yellow
    }
} catch {
    Write-Host "  ⚠️  Health check failed — app may still be starting. Check logs:" -ForegroundColor Yellow
    Write-Host "       ssh -i $KeyFile ${User}@${ServerIP} 'sudo journalctl -u $ServiceName -n 50'" -ForegroundColor Gray
}

Write-Host ""
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host "  Deploy complete!" -ForegroundColor Cyan
Write-Host "  API: http://${ServerIP}:8080" -ForegroundColor White
Write-Host "  Swagger: http://${ServerIP}:8080/swagger-ui.html" -ForegroundColor White
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host ""
