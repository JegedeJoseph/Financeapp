#!/bin/bash
# ============================================================
# setup-api-server.sh
# Run this ONCE on the financeapp-api Lightsail instance
# via: bash setup-api-server.sh
# ============================================================

set -e  # Exit immediately if any command fails

echo ""
echo "======================================================"
echo "  FinanceApp API Server — One-Time Setup"
echo "======================================================"
echo ""

# --- 1. System Update ---
echo "[1/6] Updating system packages..."
sudo apt-get update -y
sudo apt-get upgrade -y
sudo apt-get install -y wget curl gnupg apt-transport-https software-properties-common

# --- 2. Install Java 21 (Red Hat OpenJDK) ---
echo ""
echo "[2/6] Installing Java 21 (Red Hat OpenJDK)..."

# Add Microsoft/Red Hat OpenJDK apt repository (provides Red Hat builds of OpenJDK)
wget -qO - https://packages.microsoft.com/keys/microsoft.asc | sudo gpg --dearmor -o /etc/apt/trusted.gpg.d/microsoft.gpg
echo "deb [arch=amd64] https://packages.microsoft.com/repos/microsoft-ubuntu-$(lsb_release -cs)-prod $(lsb_release -cs) main" \
  | sudo tee /etc/apt/sources.list.d/microsoft-prod.list

sudo apt-get update -y
sudo apt-get install -y msopenjdk-21

# Set JAVA_HOME for the ubuntu user
echo 'export JAVA_HOME=/usr/lib/jvm/msopenjdk-21-amd64' >> /home/ubuntu/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> /home/ubuntu/.bashrc
export JAVA_HOME=/usr/lib/jvm/msopenjdk-21-amd64

echo "Java version installed:"
java -version

# --- 3. Create App Directory ---
echo ""
echo "[3/6] Creating application directory..."
sudo mkdir -p /opt/financeapp-api
sudo chown ubuntu:ubuntu /opt/financeapp-api

# Create a placeholder for the JAR (will be uploaded via SCP)
touch /opt/financeapp-api/.gitkeep

# --- 4. Create Environment Variables File ---
echo ""
echo "[4/6] Creating environment file template..."
sudo tee /opt/financeapp-api/.env > /dev/null <<'EOF'
# ============================================================
# FinanceApp API — Environment Variables
# EDIT THIS FILE with your actual values before starting!
# ============================================================

SPRING_PROFILES_ACTIVE=lightsail

# PostgreSQL (from Lightsail Managed Database)
PGHOST=YOUR_MANAGED_DB_ENDPOINT_HERE
PGPORT=5432
PGDATABASE=finance_db
PGUSER=YOUR_DB_USERNAME
PGPASSWORD=YOUR_DB_PASSWORD

# ML Service (use PRIVATE IP of financeapp-ml instance)
ML_SERVICE_URL=http://YOUR_ML_PRIVATE_IP:5000
ML_API_KEY=

# JWT Secret (use a long random string — at least 64 hex chars)
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

# Server port
PORT=8080
EOF

sudo chmod 600 /opt/financeapp-api/.env
sudo chown ubuntu:ubuntu /opt/financeapp-api/.env

echo "  ✅ Environment file created at /opt/financeapp-api/.env"
echo "  ⚠️  IMPORTANT: Edit this file with your actual credentials before starting the service!"

# --- 5. Install systemd Service ---
echo ""
echo "[5/6] Installing systemd service..."
sudo tee /etc/systemd/system/financeapp-api.service > /dev/null <<'EOF'
[Unit]
Description=FinanceApp Spring Boot API
Documentation=https://github.com/JegedeJoseph/Financeapp
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/opt/financeapp-api

# Load environment variables from file
EnvironmentFile=/opt/financeapp-api/.env

# Start the application — -Xmx512m limits heap to 512MB (fits on 2GB instance)
# Using Red Hat OpenJDK 21 (explicit path, not /usr/bin/java symlink)
ExecStart=/usr/lib/jvm/msopenjdk-21-amd64/bin/java \
  -Xms256m \
  -Xmx512m \
  -Djava.security.egd=file:/dev/./urandom \
  -jar /opt/financeapp-api/app.jar

# Restart on failure
Restart=on-failure
RestartSec=15s

# Log output
StandardOutput=journal
StandardError=journal
SyslogIdentifier=financeapp-api

# Shutdown grace period
TimeoutStopSec=30

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable financeapp-api

echo "  ✅ systemd service installed and enabled"

# --- 6. Configure Firewall ---
echo ""
echo "[6/6] Configuring UFW firewall..."
sudo apt-get install -y ufw
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow ssh          # Port 22
sudo ufw allow 8080/tcp     # Spring Boot API
sudo ufw --force enable

echo "  ✅ Firewall configured — ports 22 (SSH) and 8080 (API) are open"

# --- Done ---
echo ""
echo "======================================================"
echo "  Setup Complete!"
echo "======================================================"
echo ""
echo "NEXT STEPS:"
echo "  1. Edit the environment file:"
echo "     nano /opt/financeapp-api/.env"
echo ""
echo "  2. Upload your JAR from your local machine:"
echo "     scp -i your-key.pem target/personal-finance-manager-1.0.0.jar ubuntu@<THIS_IP>:/opt/financeapp-api/app.jar"
echo ""
echo "  3. Start the service:"
echo "     sudo systemctl start financeapp-api"
echo ""
echo "  4. Watch logs:"
echo "     sudo journalctl -u financeapp-api -f"
echo ""
