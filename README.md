# Stage Test

Spring Boot application for managing departments, employees, projects, and equipment.

## Run with Docker Compose

1. Create a local environment file: `cp .env.example .env`.
2. Replace the passwords in `.env` and, if needed, the Google OAuth values.
3. Start the application and MySQL: `docker compose up --build`.
4. Open `http://localhost:8081`.

The application image is built locally as `rayenemejri42/stage-test:local`. MySQL uses the official `mysql:8` image and persists its data in the `mysql-data` Docker volume. Flyway applies the schema migrations when the application starts.

## CI/CD

Jenkins starts a temporary `mysql:8` container, waits for it to be healthy, runs Maven verification and Flyway migration against it, analyses the code with SonarQube, then builds and pushes only the application image. The temporary database container is removed in the pipeline cleanup step.
