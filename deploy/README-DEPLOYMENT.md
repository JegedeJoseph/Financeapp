# 🚀 FinanceApp — AWS Lightsail Deployment Runbook

> **No Docker required.** This guide deploys the Java API, Python ML service, and PostgreSQL database as plain services on separate AWS Lightsail virtual servers.

---

## Architecture Overview

```
Your Windows Laptop
  │
  │  (SCP upload + SSH restart)
  │
  ├──▶ financeapp-api (Lightsail Instance — Ubuntu 22.04)
  │         Java 17 + Spring Boot — Port 8080
  │         Talks to ML service and DB via private network
  │
  ├──▶ financeapp-ml (Lightsail Instance — Ubuntu 22.04)
  │         Python 3.12 + FastAPI + scikit-learn — Port 5000
  │         Only accessible via private IP (not public internet)
  │
  └──▶ Lightsail Managed Database
            PostgreSQL 16 — Port 5432
            Only accessible by financeapp-api (private IP restriction)
```

---

## Prerequisites (Do Once on Your Windows PC)

Make sure you have installed:
- **Java 21 JDK (Red Hat OpenJDK)** — for building the JAR: https://developers.redhat.com/products/openjdk/download
- **OpenSSH** — for SCP/SSH commands (built into Windows 10/11)
- **Maven Wrapper** — already in the project (`.\mvnw.cmd`)

---

## Step 1 — Create Lightsail Resources (AWS Console)

### 1A — Create the Managed PostgreSQL Database

1. Sign in to https://lightsail.aws.amazon.com
2. Click **Databases** → **Create database**
3. Settings:
   - Engine: **PostgreSQL 16**
   - Plan: **Micro** ($15/month)
   - Database name: `finance_db`
   - Master username: choose one (e.g., `financeapp`)
   - Master password: choose a strong password
4. Click **Create database** — takes ~5 minutes
5. Once created, go to the database → **Connection details** tab
6. **Save these values** (you'll need them later):
   ```
   Endpoint:  xxxxx.us-east-1.rds.amazonaws.com
   Port:      5432
   DB Name:   finance_db
   Username:  financeapp
   Password:  <your password>
   ```

---

### 1B — Create Instance 1: Java API Server

1. Click **Instances** → **Create instance**
2. Settings:
   - Blueprint: **OS Only → Ubuntu 22.04 LTS**
   - Plan: **$10/month** (2 GB RAM, 1 vCPU, 60 GB SSD) — 2 GB is recommended for JVM 21
   - Instance name: `financeapp-api`
3. **SSH key pair**: Create new or use existing — download the `.pem` file
4. Click **Create instance** — takes ~2 minutes
5. Once running, note the **Public IP** (e.g., `54.123.45.67`)

---

### 1C — Create Instance 2: Python ML Server

1. Click **Create instance** again
2. Settings:
   - Blueprint: **OS Only → Ubuntu 22.04 LTS**
   - Plan: **$5/month** (1 GB RAM, 2 vCPU, 40 GB SSD)
   - Instance name: `financeapp-ml`
   - Use the **same SSH key pair** as above
3. Click **Create instance**
4. Note the **Public IP** and **Private IP** (shown in instance details)

---

### 1D — Restrict Database Access to API Server Only

1. Go to your Managed Database → **Networking** tab
2. Under **Allowed connections**, click **Add source**
3. Add the **Private IP** of `financeapp-api`
4. This ensures only your API server can reach the DB

---

### 1E — Get Private IP of the ML Instance

1. Go to `financeapp-ml` instance details
2. Look for **Private IP** (usually `172.26.x.x`)
3. You'll need this for the API server's environment config

---

## Step 2 — Set Up the Python ML Server

### 2A — Upload setup script and run it

From your Windows machine (PowerShell):
```powershell
# Upload the setup script
scp -i C:\path\to\your-key.pem `
    deploy\setup-ml-server.sh `
    ubuntu@<ML_PUBLIC_IP>:/home/ubuntu/

# SSH in and run it
ssh -i C:\path\to\your-key.pem ubuntu@<ML_PUBLIC_IP>
bash setup-ml-server.sh
```

### 2B — Upload the ML service code

From your Windows machine:
```powershell
scp -i C:\path\to\your-key.pem -r `
    ml-service\main.py `
    ml-service\requirements.txt `
    ml-service\models `
    ml-service\preprocessing `
    ml-service\utils `
    ubuntu@<ML_PUBLIC_IP>:/opt/financeapp-ml/
```

### 2C — Install Python dependencies

SSH into the ML server:
```bash
ssh -i C:\path\to\your-key.pem ubuntu@<ML_PUBLIC_IP>
cd /opt/financeapp-ml
source venv/bin/activate
pip install -r requirements.txt
```

### 2D — Start the ML service

```bash
sudo systemctl start financeapp-ml
sudo systemctl status financeapp-ml

# Verify it's running:
curl http://localhost:5000/health
# Expected: {"status":"healthy","service":"ML Analytics Service"}
```

---

## Step 3 — Set Up the Java API Server

### 3A — Upload setup script and run it

```powershell
scp -i C:\path\to\your-key.pem `
    deploy\setup-api-server.sh `
    ubuntu@<API_PUBLIC_IP>:/home/ubuntu/

ssh -i C:\path\to\your-key.pem ubuntu@<API_PUBLIC_IP>
bash setup-api-server.sh
```

### 3B — Edit the environment file with your credentials

SSH into the API server:
```bash
nano /opt/financeapp-api/.env
```

Fill in your actual values:
```bash
SPRING_PROFILES_ACTIVE=lightsail

# From Lightsail Managed Database → Connection details
PGHOST=xxxxx.us-east-1.rds.amazonaws.com
PGPORT=5432
PGDATABASE=finance_db
PGUSER=financeapp
PGPASSWORD=your_db_password

# Private IP of financeapp-ml instance (from AWS Console)
ML_SERVICE_URL=http://172.26.x.x:5000
ML_API_KEY=

# JWT Secret — paste a strong 64-char hex string
JWT_SECRET=your_64_character_hex_secret_here
```

Save and exit (`Ctrl+X`, then `Y`, then `Enter`).

---

### 3C — Build and Deploy the API (First Time)

On your Windows machine:
```powershell
# Build the JAR
.\mvnw.cmd clean package -DskipTests

# Upload to server
scp -i C:\path\to\your-key.pem `
    target\personal-finance-manager-1.0.0.jar `
    ubuntu@<API_PUBLIC_IP>:/opt/financeapp-api/app.jar
```

### 3D — Start the API service

SSH into the API server:
```bash
sudo systemctl start financeapp-api
sudo systemctl status financeapp-api

# Watch live startup logs
sudo journalctl -u financeapp-api -f
# Wait until you see: "Started PersonalFinanceApplication"
```

### 3E — Verify the API is running

```bash
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}
```

From your local machine:
```
http://<API_PUBLIC_IP>:8080/actuator/health
http://<API_PUBLIC_IP>:8080/swagger-ui.html
```

---

## Step 4 — Open Lightsail Firewall Ports

For `financeapp-api` instance:
1. Go to instance → **Networking** tab
2. Under **IPv4 Firewall**, add:
   - **Port 8080** — TCP — All IPs (or restrict to your frontend IP)

For `financeapp-ml` instance:
- **Do NOT open port 5000** to the public. The ML service is internal-only.
- Port 22 (SSH) is open by default.

---

## Ongoing Deployments (After First Setup)

### Deploy a new API version:

```powershell
.\deploy\deploy-api.ps1 -KeyFile "C:\path\to\key.pem" -ServerIP "54.123.45.67"
```

This script automatically:
1. Builds the JAR with Maven
2. Uploads via SCP
3. Restarts the service via SSH
4. Checks the health endpoint

### Deploy a new ML version:

```powershell
.\deploy\deploy-ml.ps1 -KeyFile "C:\path\to\key.pem" -ServerIP "54.123.45.68"
```

---

## Useful SSH Commands

```bash
# View API logs (live)
sudo journalctl -u financeapp-api -f

# View last 100 lines of API logs
sudo journalctl -u financeapp-api -n 100

# View ML logs (live)
sudo journalctl -u financeapp-ml -f

# Restart API
sudo systemctl restart financeapp-api

# Restart ML service
sudo systemctl restart financeapp-ml

# Check service status
sudo systemctl status financeapp-api
sudo systemctl status financeapp-ml

# Edit environment variables
sudo nano /opt/financeapp-api/.env
# After editing: sudo systemctl restart financeapp-api
```

---

## Troubleshooting

| Problem | Solution |
|---|---|
| App doesn't start — `JdbcSQLException` | Check `PGHOST`, `PGPORT`, `PGDATABASE` in `.env` |
| App starts but DB connection refused | Add API private IP to Managed DB allowed connections |
| ML calls fail but app works | ML service is down or `ML_SERVICE_URL` wrong — app uses fallback |
| `java: command not found` | Re-run `setup-api-server.sh` — Java install may have failed |
| Port 8080 not reachable | Add firewall rule in Lightsail console for instance |
| `Permission denied (publickey)` | Wrong `.pem` file or wrong username (must be `ubuntu`) |

---

## Cost Summary

| Resource | Plan | Monthly |
|---|---|---|
| `financeapp-api` (Java) | 2 GB RAM / 1 vCPU | $10.00 |
| `financeapp-ml` (Python) | 1 GB RAM / 2 vCPU | $5.00 |
| Managed PostgreSQL | Micro (1 GB RAM) | $15.00 |
| **Total** | | **$30.00/month** |

> 💡 AWS Lightsail offers a **3-month free trial** on new instances (not on managed databases).
