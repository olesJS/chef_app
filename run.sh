#!/bin/bash
echo "----------------------------------------------------"
echo "[1/3] Checking and starting database in Docker...  "
echo "----------------------------------------------------"

docker compose up -d

echo "Initializing PostgreSQL (5 seconds)..."
sleep 5

echo "----------------------------------------------------"
echo "[2/3] Starting Chef App via Maven Wrapper...       "
echo "----------------------------------------------------"

chmod +x mvnw
./mvnw javafx:run

echo "----------------------------------------------------"
echo "[3/3] Application stopped. Container runs in BG.   "
echo "----------------------------------------------------"
read -p "Press [Enter] to close this window..."