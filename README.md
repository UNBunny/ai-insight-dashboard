# AI Insight Dashboard Admin Application

This is a full-stack application with a Spring Boot backend (Java 21) and React/TypeScript frontend, providing an admin dashboard for managing users and insights.

## Project Structure

The project consists of two main parts:

1. **Backend**: Spring Boot application with Java 21
   - REST API with CRUD operations
   - H2 in-memory database
   - JPA for data persistence

2. **Frontend**: React with TypeScript
   - Modern admin panel UI
   - Feature-rich components built with ShadCN UI
   - React Query for data fetching
   - Dark/light theme support

The project follows standard Maven directory structure:

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── example/
│   │           └── demo/
│   │               ├── config/       # Configuration classes
│   │               ├── controllers/  # REST API endpoints
│   │               ├── dto/          # Data Transfer Objects
│   │               ├── models/       # JPA entities
│   │               ├── repositories/ # Spring Data JPA repositories
│   │               ├── services/     # Business logic
│   │               └── DemoApplication.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/
        └── com/
            └── example/
                └── demo/
                    └── DemoApplicationTests.java
```

## Technologies Used

- Java 21
- Spring Boot 3.2.1
- Spring Web (REST API)
- Spring Data JPA (with H2 Database)
- Lombok
- MapStruct
- JUnit 5 (for testing)

## Features

- Basic User CRUD operations
- RESTful API
- DTO pattern with MapStruct for object mapping
- H2 in-memory database for development

## Running the Application

### Option 1: Using the start script

The simplest way to run both frontend and backend is using the provided batch script:

```bash
./start-app.bat  # On Windows
```

This will start:
- Backend on port 8080
- Frontend also configured to run on port 8080

### Option 2: Manual startup

If you prefer to start services manually:

1. **Start the backend**:
   ```bash
   mvn spring-boot:run
   ```

2. **Start the frontend** (in another terminal):
   ```bash
   cd client
   npm start
   ```

The application will be available at http://localhost:8080

H2 Console is available at http://localhost:8080/h2-console (credentials in application.properties).

## API Endpoints

- `GET /api/v1/users` - Get all users
- `GET /api/v1/users/{id}` - Get user by ID
- `GET /api/v1/users/search?query={query}` - Search users by query
- `POST /api/v1/users` - Create a new user
- `PUT /api/v1/users/{id}` - Update an existing user
- `DELETE /api/v1/users/{id}` - Delete a user by ID

## Testing

Run the tests using Maven:

```bash
mvn test
```
