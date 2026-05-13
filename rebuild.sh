docker compose down -v
mvn clean package -DskipTests
docker compose up -d --build
