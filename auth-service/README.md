# auth-service

## Dev (local, without Docker)
1. Ensure PostgreSQL running locally and update `SPRING_DATASOURCE_*` env or application.yml.
2. mvn clean package
3. java -jar target/auth-service-1.0.0.jar

## Using Docker (build & run)
# from auth-service directory
mvn -DskipTests package
docker build -t auth-service:local .
docker run -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/ecommerce \
-e SPRING_DATASOURCE_USERNAME=postgres \
-e SPRING_DATASOURCE_PASSWORD=postgres \
-e JWT_SECRET=ChangeMe \
-p 8081:8080 auth-service:local

## With docker-compose (recommended)
Add a service to your project's docker-compose.yml that points to ./auth-service (build context).
