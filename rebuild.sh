docker compose down -v
mvn clean package
docker compose up -d --build
