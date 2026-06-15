FROM eclipse-temurin:11-jre

WORKDIR /app
COPY target/jobflow-backend-0.1.0.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=docker"]
