#!/bin/bash

echo "Starting Kanban Board Development Environment..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "Docker is not running. Please start Docker first."
    exit 1
fi

# Start PostgreSQL database
echo "Starting PostgreSQL database..."
docker run -d \
  --name kanban-postgres \
  -e POSTGRES_DB=kanban_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=Dinesh \
  -p 5432:5432 \
  postgres:15-alpine

# Wait for database to be ready
echo "Waiting for database to be ready..."
sleep 10

echo "Database is ready!"
echo ""
echo "Now you can start the backend and frontend:"
echo "1. Backend: cd backend && ./mvnw spring-boot:run"
echo "2. Frontend: cd frontend && npm install && npm run dev"
echo ""
echo "Access the application at: http://localhost:5173"
echo "API will be available at: http://localhost:8080/api"