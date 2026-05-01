# Use Java 17 image
FROM openjdk:17-jdk-slim

# Copy jar file into container
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

# Run the jar
ENTRYPOINT ["java","-jar","/app.jar"]