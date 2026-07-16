#!/bin/bash
# ============================================================
# setup-ml-server.sh
# Run this ONCE on the financeapp-ml Lightsail instance
# via: bash setup-ml-server.sh
# ============================================================

set -e

echo ""
echo "======================================================"
echo "  FinanceApp ML Service — One-Time Setup"
echo "======================================================"
echo ""

# --- 1. System Update ---
echo "[1/6] Updating system packages..."
sudo apt-get update -y
sudo apt-get upgrade -y
sudo apt-get install -y curl wget git software-properties-common build-essential

# --- 2. Install Python 3.12 ---
echo ""
echo "[2/6] Installing Python 3.12..."
sudo add-apt-repository ppa:deadsnakes/ppa -y
sudo apt-get update -y
sudo apt-get install -y python3.12 python3.12-venv python3.12-dev

echo "Python version installed:"
python3.12 --version

# --- 3. Create App Directory & Virtual Environment ---
echo ""
echo "[3/6] Creating application directory and virtual environment..."
sudo mkdir -p /opt/financeapp-ml
sudo chown ubuntu:ubuntu /opt/financeapp-ml
cd /opt/financeapp-ml

python3.12 -m venv venv
echo "  ✅ Virtual environment created at /opt/financeapp-ml/venv"

# --- 4. Create Environment Variables File ---
echo ""
echo "[4/6] Creating environment file..."
tee /opt/financeapp-ml/.env > /dev/null <<'EOF'
# ============================================================
# FinanceApp ML Service — Environment Variables
# ============================================================
PYTHONUNBUFFERED=1
ML_API_KEY=
PORT=5000
EOF

chmod 600 /opt/financeapp-ml/.env
echo "  ✅ Environment file created at /opt/financeapp-ml/.env"

# --- 5. Install systemd Service ---
echo ""
echo "[5/6] Installing systemd service..."
sudo tee /etc/systemd/system/financeapp-ml.service > /dev/null <<'EOF'
[Unit]
Description=FinanceApp Python ML Service (FastAPI)
Documentation=https://github.com/JegedeJoseph/Financeapp
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/opt/financeapp-ml

# Load environment variables
EnvironmentFile=/opt/financeapp-ml/.env

# Run uvicorn using the virtual environment
ExecStart=/opt/financeapp-ml/venv/bin/uvicorn main:app \
  --host 0.0.0.0 \
  --port 5000 \
  --workers 2 \
  --log-level info

# Restart on failure
Restart=on-failure
RestartSec=10s

# Log output
StandardOutput=journal
StandardError=journal
SyslogIdentifier=financeapp-ml

TimeoutStopSec=20

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable financeapp-ml

echo "  ✅ systemd service installed and enabled"

# --- 6. Configure Firewall ---
echo ""
echo "[6/6] Configuring UFW firewall..."
sudo apt-get install -y ufw
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow ssh          # Port 22 — for your SSH access
# Port 5000 is intentionally NOT opened to public internet
# The API server reaches ML service via private IP (internal network)
sudo ufw --force enable

echo "  ✅ Firewall configured — only SSH port 22 is public"
echo "  ℹ️  Port 5000 is accessible only via Lightsail private network"

# --- Done ---
echo ""
echo "======================================================"
echo "  Setup Complete!"
echo "======================================================"
echo ""
echo "NEXT STEPS:"
echo "  1. Upload the ml-service files from your local machine:"
echo "     scp -i your-key.pem -r Financeapp/ml-service/* ubuntu@<THIS_IP>:/opt/financeapp-ml/"
echo ""
echo "  2. Install Python dependencies:"
echo "     cd /opt/financeapp-ml && source venv/bin/activate && pip install -r requirements.txt"
echo ""
echo "  3. Start the service:"
echo "     sudo systemctl start financeapp-ml"
echo ""
echo "  4. Watch logs:"
echo "     sudo journalctl -u financeapp-ml -f"
echo ""
echo "  5. Test locally (from this server):"
echo "     curl http://localhost:5000/health"
echo ""
