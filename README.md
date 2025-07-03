# Spring Boot Demo Application

This is a Spring Boot application built with Java 21, using Maven as the build tool.

## Project Structure

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

To run the application, you can use the Maven Spring Boot plugin:

```bash
mvn spring-boot:run
```

The application will start on http://localhost:8080 by default.

H2 Console is available at http://localhost:8080/h2-console (credentials in application.properties).

## API Endpoints

- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/by-username/{username}` - Get user by username
- `POST /api/users` - Create a new user
- `PUT /api/users/{id}` - Update an existing user
- `DELETE /api/users/{id}` - Delete a user by ID

## Testing

Run the tests using Maven:

```bash
mvn test
```
