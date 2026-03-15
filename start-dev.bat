@echo off
echo Starting Kanban Board Development Environment...

REM Check if Docker is running
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo Docker is not running. Please start Docker first.
    exit /b 1
)

REM Start PostgreSQL database
echo Starting PostgreSQL database...
docker run -d --name kanban-postgres -e POSTGRES_DB=kanban_db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=Dinesh -p 5432:5432 postgres:15-alpine

REM Wait for database to be ready
echo Waiting for database to be ready...
timeout /t 10 /nobreak >nul

echo Database is ready!
echo.
echo Now you can start the backend and frontend:
echo 1. Backend: cd backend ^&^& mvnw.cmd spring-boot:run
echo 2. Frontend: cd frontend ^&^& npm install ^&^& npm run dev
echo.
echo Access the application at: http://localhost:5173
echo API will be available at: http://localhost:8080/api