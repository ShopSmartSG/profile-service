# Use an official OpenJDK runtime as a parent image
FROM openjdk:21-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the project’s jar file into the container at /app
COPY target/profile-service*.jar app.jar

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Run the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]

# to build image after building jar post any changes
# docker build -t profile-service .
# docker-compose up --build
# docker file and docker-compose port should be same
# docker-compose down : shutdown the container
# till we shutdown the postgres image , db remains intact
#docker file has container port
#app.properties has