FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Change /app.jar to app.jar so it lands inside the /app folder
ADD target/*.jar app.jar

# Your application runs on 8080 inside the container
EXPOSE 8201

ENTRYPOINT ["java", "-jar", "app.jar"]