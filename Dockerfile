# Multi-stage build: build with Maven, run on lightweight JRE
FROM maven:4.0.0-rc-5-amazoncorretto-17-debian-trixie AS build
WORKDIR /workspace/app

# copy maven wrapper and pom so dependency layer can be cached
COPY pom.xml mvnw .
COPY .mvn .mvn

# fetch dependencies
RUN mvn -B -f pom.xml -ntp dependency:go-offline

# copy sources and build
COPY src ./src
RUN mvn -B -f pom.xml -ntp package -DskipTests

# Runtime image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Allow external configuration via env and bind mounts
ENV JAVA_TOOL_OPTIONS="-Xms128m -Xmx512m"
ARG JAR_FILE=target/*.jar
COPY --from=build /workspace/app/target/*.jar ./app.jar
EXPOSE 8080

# Optional healthcheck (requires curl available in image). Comment out if not desired.
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_TOOL_OPTIONS -jar /app/app.jar"]
