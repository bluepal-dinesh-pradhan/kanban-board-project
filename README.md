# Kanban Board Application

A full-stack Trello-style collaborative Kanban board application built with Spring Boot and React.

## Technology Stack

### Backend
- **Spring Boot 3** - REST API framework
- **Spring Data JPA** - Database abstraction
- **Spring Security** - Authentication & authorization
- **JWT** - Token-based authentication
- **PostgreSQL** - Database
- **Maven** - Build tool

### Frontend
- **React 18** - UI framework
- **Vite** - Build tool
- **Tailwind CSS** - Styling
- **React Query** - Data fetching & caching
- **@hello-pangea/dnd** - Drag and drop functionality
- **React Router** - Navigation
- **React Hot Toast** - Notifications
- **React Icons** - Icon library

## Features

### Authentication
- User registration and login with form validation
- JWT access and refresh tokens with automatic refresh
- Secure password hashing with BCrypt
- Protected routes with authentication guards

### Board Management
- Create and manage multiple boards
- Role-based access control (Owner, Editor, Viewer)
- Invite members to boards via email with role selection
- Activity feed showing board actions

### Kanban Functionality
- Create, edit, and delete columns
- Create, edit, and move cards between columns
- Drag and drop card reordering with optimistic updates
- Card labels with color picker and custom text
- Card due dates with overdue indicators
- Comments on cards with real-time updates
- Activity tracking for all board actions

## Quick Start with Docker

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd kanban-board
   ```

2. **Create environment file**
   ```bash
   cp .env.example .env
   # Edit .env with your preferred settings
   ```

3. **Start the application**
   ```bash
   docker-compose up --build
   ```

4. **Access the application**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080/api
   - Database: localhost:5432

## Manual Setup

### Prerequisites
- Java 21+
- Node.js 18+
- PostgreSQL 16+
- Maven 3.6+

### Backend Setup

1. **Navigate to backend directory**
   ```bash
   cd backend
   ```

2. **Configure database**
   Update `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/kanban_db
   spring.datasource.username=postgres
   spring.datasource.password=your_password
   ```

3. **Create database**
   ```sql
   CREATE DATABASE kanban_db;
   ```

4. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

### Frontend Setup

1. **Navigate to frontend directory**
   ```bash
   cd frontend
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Configure API URL**
   Create `.env` file:
   ```
   VITE_API_URL=/api
   ```

4. **Start development server**
   ```bash
   npm run dev
   ```

## Project Structure

```
kanban-board/
├── backend/                    # Spring Boot backend
│   ├── src/main/java/com/example/demo/
│   │   ├── controller/        # REST controllers
│   │   ├── service/          # Business logic
│   │   ├── repository/       # Data access layer
│   │   ├── entity/          # JPA entities
│   │   ├── dto/             # Data transfer objects
│   │   ├── security/        # Security configuration
│   │   └── exception/       # Exception handling
│   ├── Dockerfile           # Multi-stage Docker build
│   └── pom.xml             # Maven dependencies
├── frontend/                  # React frontend
│   ├── src/
│   │   ├── api/            # API configuration and endpoints
│   │   ├── components/     # Reusable React components
│   │   ├── context/        # React context providers
│   │   ├── pages/          # Page components
│   │   └── main.jsx        # Application entry point
│   ├── nginx.conf          # Nginx configuration for production
│   ├── Dockerfile          # Multi-stage Docker build
│   └── package.json        # NPM dependencies
├── docker-compose.yml        # Docker orchestration
└── .env.example             # Environment variables template
```

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login user
- `POST /api/auth/refresh` - Refresh access token

### Boards
- `GET /api/boards` - Get user's boards
- `POST /api/boards` - Create new board
- `POST /api/boards/{id}/members` - Invite member
- `GET /api/boards/{id}/columns` - Get board columns
- `GET /api/boards/{id}/activity` - Get board activity

### Columns
- `POST /api/boards/{boardId}/columns` - Create column

### Cards
- `POST /api/boards/{boardId}/cards` - Create card
- `PUT /api/cards/{id}` - Update card
- `PATCH /api/cards/{id}/move` - Move card
- `GET /api/cards/{id}` - Get card details
- `POST /api/cards/{id}/comments` - Add comment
- `GET /api/cards/{id}/comments` - Get comments

## Database Schema

### Core Entities
- **User** - User accounts
- **Board** - Kanban boards
- **BoardMember** - Board membership with roles
- **BoardColumn** - Board columns
- **Card** - Cards within columns
- **CardLabel** - Card labels
- **Comment** - Card comments
- **Activity** - Activity tracking

## Development

### Backend Development
```bash
cd backend
./mvnw spring-boot:run
```

### Frontend Development
```bash
cd frontend
npm run dev
```

### Building for Production

#### Backend
```bash
cd backend
./mvnw clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

#### Frontend
```bash
cd frontend
npm run build
npm run preview
```

## Environment Variables

### Backend
- `DB_HOST` - Database host (default: localhost)
- `DB_PORT` - Database port (default: 5432)
- `DB_NAME` - Database name (default: kanban_db)
- `DB_USERNAME` - Database username (default: postgres)
- `DB_PASSWORD` - Database password
- `JWT_SECRET` - JWT signing secret
- `CORS_ORIGINS` - Allowed CORS origins (default: http://localhost:5173)

### Frontend
- `VITE_API_URL` - Backend API URL (default: http://localhost:8080/api)

## Security Features

- JWT-based authentication with access and refresh tokens
- Role-based authorization (Owner, Editor, Viewer)
- CORS configuration for cross-origin requests
- Password hashing with BCrypt
- SQL injection prevention with JPA
- Input validation on all endpoints

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License.
## Frontend Architecture

### API Layer
- **axios.js** - Configured Axios instance with JWT interceptors and auto-refresh
- **endpoints.js** - Centralized API endpoint functions organized by feature

### State Management
- **React Query** - Server state management with caching and optimistic updates
- **AuthContext** - Authentication state and user management
- **Local Storage** - JWT token persistence

### Components
- **CardModal** - Full-featured card editing with labels, comments, and due dates
- **ActivityFeed** - Slide-out panel showing board activity history
- **InviteModal** - Member invitation with role selection
- **Navbar** - Application header with user info and logout

### Pages
- **LoginPage/RegisterPage** - Authentication forms with validation
- **BoardListPage** - Dashboard showing user's boards with creation
- **BoardPage** - Main Kanban interface with drag-and-drop functionality

### Key Features
- **Drag & Drop** - @hello-pangea/dnd for smooth card movement
- **Optimistic Updates** - Immediate UI updates with error rollback
- **Toast Notifications** - User feedback for all actions
- **Responsive Design** - Mobile-friendly Tailwind CSS styling
- **Form Validation** - Client-side validation with error messages

## Docker Architecture

### Multi-Stage Builds
- **Backend** - Maven build → JRE runtime for optimal image size
- **Frontend** - Node build → Nginx serve for production deployment

### Production Setup
- **Nginx Reverse Proxy** - Routes /api requests to backend
- **Health Checks** - Database health monitoring
- **Security** - Non-root users, security headers
- **Compression** - Gzip compression for static assets

### Environment Variables
All configuration through environment variables for security:
- Database credentials
- JWT secrets
- CORS origins
- API URLs

## Development Workflow

### Backend Development
```bash
cd backend
./mvnw spring-boot:run
# API available at http://localhost:8080/api
```

### Frontend Development
```bash
cd frontend
npm run dev
# App available at http://localhost:5173
# API proxied to backend automatically
```

### Full Stack Development
```bash
# Terminal 1 - Database
docker run -d --name postgres -e POSTGRES_DB=kanban_db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=Dinesh -p 5432:5432 postgres:16-alpine

# Terminal 2 - Backend
cd backend && ./mvnw spring-boot:run

# Terminal 3 - Frontend
cd frontend && npm run dev
```

## Production Deployment

### Using Docker Compose
```bash
# Production deployment
docker-compose up -d

# View logs
docker-compose logs -f

# Scale services
docker-compose up -d --scale backend=2
```

### Manual Deployment
```bash
# Backend
cd backend
./mvnw clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar

# Frontend
cd frontend
npm run build
# Serve dist/ with your web server
```

## API Documentation

### Authentication Endpoints
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `POST /api/auth/refresh` - Token refresh

### Board Management
- `GET /api/boards` - List user boards
- `POST /api/boards` - Create board
- `POST /api/boards/{id}/members` - Invite member
- `GET /api/boards/{id}/columns` - Get board with columns/cards
- `GET /api/boards/{id}/activity` - Get activity feed

### Card Operations
- `POST /api/boards/{boardId}/cards` - Create card
- `PUT /api/cards/{id}` - Update card
- `PATCH /api/cards/{id}/move` - Move card
- `GET /api/cards/{id}` - Get card details
- `POST /api/cards/{id}/comments` - Add comment
- `GET /api/cards/{id}/comments` - Get comments

All responses use the `ApiResponse<T>` wrapper:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

## Security Features

### Backend Security
- JWT-based authentication with access/refresh tokens
- BCrypt password hashing
- Role-based authorization (Owner/Editor/Viewer)
- CORS configuration
- SQL injection prevention with JPA
- Input validation on all endpoints

### Frontend Security
- Automatic token refresh on expiry
- Protected routes with authentication guards
- XSS prevention with React's built-in escaping
- CSRF protection through JWT tokens
- Secure token storage in localStorage

## Performance Optimizations

### Backend
- JPA query optimization with proper joins
- Database indexing on foreign keys
- Connection pooling
- Lazy loading for entity relationships

### Frontend
- React Query caching and background updates
- Optimistic updates for better UX
- Code splitting with React Router
- Image optimization and compression
- Nginx gzip compression

### Docker
- Multi-stage builds for smaller images
- Layer caching optimization
- Health checks for reliability
- Resource limits and restart policies

## Troubleshooting

### Common Issues

**Database Connection Failed**
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Check connection
psql -h localhost -U postgres -d kanban_db
```

**CORS Errors**
- Ensure CORS_ORIGINS environment variable includes your frontend URL
- Check that frontend is making requests to correct API URL

**JWT Token Issues**
- Check JWT_SECRET is set and consistent
- Verify token expiration times in application.properties

**Build Failures**
```bash
# Backend
cd backend && ./mvnw clean install

# Frontend
cd frontend && rm -rf node_modules && npm install
```

### Logs
```bash
# Docker logs
docker-compose logs backend
docker-compose logs frontend
docker-compose logs database

# Application logs
tail -f backend/logs/application.log
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow existing code style and patterns
- Add tests for new functionality
- Update documentation for API changes
- Use conventional commit messages
- Ensure Docker builds pass

## License

This project is licensed under the MIT License - see the LICENSE file for details.