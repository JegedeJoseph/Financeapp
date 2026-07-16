#!/bin/bash
# ============================================================
# deploy-ml.ps1  (Windows PowerShell version)
# Run this from your Windows machine every time you want to
# push an update to the Python ML service on Lightsail.
#
# Usage:
#   .\deploy\deploy-ml.ps1 -KeyFile "C:\path\to\key.pem" -ServerIP "54.123.45.68"
# ============================================================

param(
    [Parameter(Mandatory=$true)]
    [string]$KeyFile,       # Path to your .pem SSH key file

    [Parameter(Mandatory=$true)]
    [string]$ServerIP,      # Public IP of your financeapp-ml Lightsail instance

    [string]$User = "ubuntu",
    [string]$RemoteDir = "/opt/financeapp-ml",
    [string]$ServiceName = "financeapp-ml"
)

$ErrorActionPreference = "Stop"
$ProjectRoot = "$PSScriptRoot\.."
$MLServiceDir = "$ProjectRoot\ml-service"

Write-Host ""
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host "  FinanceApp ML Service — Deploy to Lightsail" -ForegroundColor Cyan
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host ""

# --- Step 1: Upload ML service files ---
Write-Host "[1/3] Uploading ML service files to server ($ServerIP)..." -ForegroundColor Yellow

# Upload each file/folder (scp -r for directories)
scp -i $KeyFile -o StrictHostKeyChecking=no -r `
    "$MLServiceDir\main.py" `
    "$MLServiceDir\requirements.txt" `
    "$MLServiceDir\models" `
    "$MLServiceDir\preprocessing" `
    "$MLServiceDir\utils" `
    "${User}@${ServerIP}:${RemoteDir}/"

if ($LASTEXITCODE -ne 0) {
    Write-Host "  ❌ SCP upload FAILED. Check key and IP." -ForegroundColor Red
    exit 1
}
Write-Host "  ✅ ML service files uploaded" -ForegroundColor Green

# --- Step 2: Install dependencies & restart ---
Write-Host ""
Write-Host "[2/3] Installing Python dependencies and restarting service..." -ForegroundColor Yellow

ssh -i $KeyFile -o StrictHostKeyChecking=no "${User}@${ServerIP}" @"
    cd $RemoteDir
    source venv/bin/activate
    pip install -r requirements.txt --quiet
    deactivate
    sudo systemctl restart $ServiceName
"@

if ($LASTEXITCODE -ne 0) {
    Write-Host "  ❌ Remote command FAILED." -ForegroundColor Red
    exit 1
}
Write-Host "  ✅ Dependencies installed, service restarted" -ForegroundColor Green

# --- Step 3: Health check (from the remote server itself, via private IP) ---
Write-Host ""
Write-Host "[3/3] Waiting 10 seconds then checking ML service health..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

$healthCheck = ssh -i $KeyFile -o StrictHostKeyChecking=no "${User}@${ServerIP}" `
    "curl -s http://localhost:5000/health"

Write-Host "  ML service response: $healthCheck" -ForegroundColor White
if ($healthCheck -match '"healthy"') {
    Write-Host "  ✅ ML service is healthy!" -ForegroundColor Green
} else {
    Write-Host "  ⚠️  Unexpected response. Check logs:" -ForegroundColor Yellow
    Write-Host "       ssh -i $KeyFile ${User}@${ServerIP} 'sudo journalctl -u $ServiceName -n 50'" -ForegroundColor Gray
}

Write-Host ""
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host "  ML Deploy complete!" -ForegroundColor Cyan
Write-Host "======================================================" -ForegroundColor Cyan
Write-Host ""
