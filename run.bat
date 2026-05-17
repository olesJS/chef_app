@echo off
chcp 65001 > nul
title Chef App Launcher

echo ----------------------------------------------------
echo [1/3] Checking and starting database in Docker...
echo ----------------------------------------------------

docker compose up -d

echo Initializing PostgreSQL (5 seconds)...
timeout /t 5 /nobreak > nul

echo ----------------------------------------------------
echo [2/3] Starting Chef App via Maven Wrapper...
echo ----------------------------------------------------

call mvnw.cmd javafx:run

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Щось пішло не так під час запуску програми!
    echo Перевірте, чи запущений Docker Desktop та чи налаштований .env
    echo.
)

echo ----------------------------------------------------
echo [3/3] Application stopped. Container runs in BG.
echo ----------------------------------------------------
pause