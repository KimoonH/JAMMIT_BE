# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

JAMMIT_BE is a Spring Boot REST API service for music gathering/jam session management. The application allows users to create, join, and manage musical gatherings with band sessions (roles like vocalist, guitarist, drummer, etc.).

## Common Development Commands

### Build & Run
```bash
./gradlew clean build          # Build the project
./gradlew bootRun             # Run the application
./gradlew test                # Run all tests
```

### Docker Development
```bash
docker-compose up -d          # Start all services (MySQL, Prometheus, Grafana)
docker-compose down           # Stop all services
```

### Database
- MySQL 8.0 via Docker Compose (port 3398)
- Default database: `testdb` for local dev, `JAMMIT` for Docker
- Uses JPA/Hibernate with QueryDSL for complex queries

## Architecture Overview

### Package Structure
- `auth/` - JWT-based authentication, email verification
- `user/` - User management, preferences, OAuth support
- `gathering/` - Core gathering creation, participation, scheduling
- `review/` - Review system for completed gatherings
- `storage/` - File storage abstraction (S3/Local)
- `common/` - Shared utilities, monitoring, security config

### Key Architectural Patterns

**Domain-Driven Design**: Each business domain has its own controller, service, repository, and entity structure.

**Query Performance Optimization**:
- Custom QueryDSL implementations for complex queries
- Two-stage query approach for gathering lists to prevent N+1 problems
- Query count monitoring via `QueryCountInspector`

**Security Architecture**:
- JWT tokens with refresh mechanism
- Custom `JwtFilter` for request authentication
- Method-level security with `@EnableMethodSecurity`
- CORS configuration for cross-origin requests

**Monitoring & Observability**:
- Prometheus metrics via Spring Actuator (port /actuator/prometheus)
- Custom query monitoring with `RequestContext` pattern
- Grafana dashboards for visualization
- Performance testing with K6 scripts in `k6-scripts/`

### Data Model Relationships
- **Gathering** ↔ **GatheringSession** (1:N) - A gathering has multiple band sessions/roles
- **User** ↔ **GatheringParticipant** (1:N) - Users can participate in multiple gatherings
- **GatheringParticipant** ↔ **Review** (1:N) - Participants can write reviews after gatherings
- **User** ↔ **PreferredGenre/PreferredBandSession** (1:N) - User preferences

### Configuration Management
- **Jasypt encryption** for sensitive properties in application.yml
- Environment-specific configurations via profiles
- S3 integration for file uploads
- SMTP configuration for email notifications

## Development Notes

### Testing
- H2 database for tests
- Testcontainers for integration tests with MySQL
- Concurrency testing support with Awaitility
- No specific test runner script - use standard Gradle commands

### Performance Considerations
- Query optimization is critical - always check N+1 problems
- Use the monitoring system to track query counts per request
- Gathering list queries use two-stage approach for performance
- FetchJoin strategies implemented for related entity loading

### Authentication Flow
1. User registration with email verification
2. JWT tokens issued on login (access + refresh)
3. `AuthUtil.getUserInfo()` retrieves current authenticated user
4. Tokens validated via `JwtFilter` on each request

### File Storage
- Abstracted via `FileStorage` interface
- S3 implementation for production
- Local storage for development
- Image uploads limited to 5MB per file, 10MB per request