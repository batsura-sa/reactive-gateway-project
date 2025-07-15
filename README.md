# Reactive Spring Boot Gateway

A reactive Spring Boot gateway application that receives HTTP REST requests and calls gRPC services using Java 21.

## Features

- **Reactive Programming**: Built with Spring WebFlux for non-blocking, reactive operations
- **gRPC Integration**: Calls backend gRPC services with reactive stubs
- **Java 21**: Uses modern Java features and language constructs
- **REST API**: Exposes RESTful endpoints for user management
- **Validation**: Request validation with Jakarta Bean Validation
- **Error Handling**: Global exception handling with proper error responses
- **Testing**: Comprehensive unit tests with WebFlux test support
- **Mock gRPC Server**: Embedded mock gRPC server for development/testing

## Architecture

```
HTTP REST Client → Spring WebFlux Gateway → gRPC Service
                                      ↓
                              UserGatewayService
                                      ↓
                              ReactorUserServiceStub
                                      ↓
                              Backend gRPC Server
```

## Project Structure

```
src/
├── main/
│   ├── java/com/example/
│   │   ├── ReactiveGatewayApplication.java    # Main Spring Boot application
│   │   ├── config/
│   │   │   ├── GrpcClientConfig.java          # gRPC client configuration
│   │   │   └── MockGrpcServerConfig.java      # Mock gRPC server (dev/test)
│   │   ├── controller/
│   │   │   └── UserController.java            # REST API endpoints
│   │   ├── service/
│   │   │   └── UserGatewayService.java        # Business logic & gRPC calls
│   │   ├── mapper/
│   │   │   └── UserMapper.java                # DTO ↔ gRPC mapping
│   │   ├── dto/
│   │   │   ├── UserDto.java                   # User data transfer object
│   │   │   ├── CreateUserRequest.java         # Create user request
│   │   │   └── UpdateUserRequest.java         # Update user request
│   │   ├── exception/
│   │   │   └── GlobalExceptionHandler.java    # Global error handling
│   │   └── grpc/
│   │       └── MockUserServiceImpl.java       # Mock gRPC service implementation
│   ├── proto/
│   │   └── user_service.proto                 # Protocol Buffers definition
│   └── resources/
│       └── application.yml                    # Application configuration
└── test/
    ├── java/com/example/
    │   ├── controller/
    │   │   └── UserControllerTest.java         # REST controller tests
    │   └── service/
    │       └── UserGatewayServiceTest.java     # Service layer tests
    └── resources/
        └── application-test.yml                # Test configuration
```

## API Endpoints

### User Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users/{id}` | Get user by ID |
| POST | `/api/users` | Create new user |
| PUT | `/api/users/{id}` | Update existing user |
| DELETE | `/api/users/{id}` | Delete user |
| GET | `/api/users` | List users (with pagination) |
| GET | `/api/users/health` | Health check |

### Management Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/health` | Application health |
| GET | `/actuator/metrics` | Application metrics |
| GET | `/actuator/info` | Application info |

## Getting Started

### Prerequisites

- Java 21 or later
- No need to install Gradle (uses Gradle Wrapper)

### Build the project

```bash
./gradlew build
```

### Generate gRPC classes

```bash
./gradlew generateProto
```

### Run with mock gRPC server

```bash
./gradlew bootRun --args='--spring.profiles.active=mock-grpc'
```

### Run tests

```bash
./gradlew test
```

## Usage Examples

### Create a user

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com",
    "age": 30
  }'
```

### Get a user

```bash
curl http://localhost:8080/api/users/1
```

### Update a user

```bash
curl -X PUT http://localhost:8080/api/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Updated",
    "email": "john.updated@example.com",
    "age": 31
  }'
```

### List users

```bash
curl "http://localhost:8080/api/users?page=0&size=10"
```

### Delete a user

```bash
curl -X DELETE http://localhost:8080/api/users/1
```

## Configuration

### Application Configuration (`application.yml`)

```yaml
server:
  port: 8080

grpc:
  client:
    user-service:
      host: localhost
      port: 9090
```

### gRPC Service Configuration

The gateway connects to a gRPC service defined in `user_service.proto`. The service provides:

- `GetUser` - Retrieve user by ID
- `CreateUser` - Create new user
- `UpdateUser` - Update existing user
- `DeleteUser` - Delete user
- `ListUsers` - List users with pagination

## Development

### Running with Mock gRPC Server

For development and testing, you can use the embedded mock gRPC server:

```bash
./gradlew bootRun --args='--spring.profiles.active=mock-grpc'
```

This starts both the Spring Boot gateway and a mock gRPC server with sample data.

### Connecting to Real gRPC Service

To connect to a real gRPC service, update the configuration in `application.yml`:

```yaml
grpc:
  client:
    user-service:
      host: your-grpc-service-host
      port: your-grpc-service-port
```

## Testing

The project includes comprehensive tests:

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test the full request/response flow
- **WebFlux Tests**: Test reactive endpoints with `WebTestClient`

Run all tests:

```bash
./gradlew test
```

## Key Technologies

- **Spring Boot 3.2**: Application framework
- **Spring WebFlux**: Reactive web framework
- **gRPC**: High-performance RPC framework
- **Protocol Buffers**: Data serialization
- **Reactor**: Reactive programming library
- **JUnit 5**: Testing framework
- **Gradle**: Build tool
- **Java 21**: Programming language

## Reactive Features

- **Non-blocking I/O**: All operations are non-blocking
- **Backpressure**: Automatic handling of flow control
- **Error Handling**: Reactive error handling with fallbacks
- **Streaming**: Support for streaming responses
- **Composability**: Easy composition of reactive operations

## Monitoring

The application includes Spring Boot Actuator for monitoring:

- Health checks
- Metrics collection
- Application info
- Prometheus metrics export

Access monitoring endpoints at `http://localhost:8080/actuator/`