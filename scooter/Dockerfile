# Используем официальный образ с Java
FROM eclipse-temurin:17-jre

# Создаем директорию для приложения
WORKDIR /app

# Копируем собранный JAR файл в контейнер
# Путь нужно адаптировать под вашу структуру
COPY ./target/scooter-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

# Команда запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]