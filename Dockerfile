# Use an official OpenJDK 21 runtime as a parent image
FROM openjdk:21-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src
COPY mvnw .
COPY .mvn .mvn
COPY mvnw.cmd .

# Install Maven
RUN apt-get update && apt-get install -y maven

# Make the maven wrapper executable
RUN chmod +x ./mvnw

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose the port the app runs on
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java","-jar","target/wildtrackbackend-0.0.1-SNAPSHOT.jar"]