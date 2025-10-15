########################################
# 🏗️ STAGE 1 — BUILD
########################################
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw

# Cache dependencies
RUN ./mvnw dependency:go-offline -B

# Install OpenCV dependencies for build
RUN apt-get update && apt-get install -y \
	libopencv-dev \
	python3-opencv \
	&& rm -rf /var/lib/apt/lists/*

COPY src ./src
RUN ./mvnw clean package -DskipTests

########################################
# 🚀 STAGE 2 — RUNTIME
########################################
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Install OpenCV dependencies in runtime
RUN apt-get update && apt-get install -y \
	libopencv-dev \
	python3-opencv \
	&& rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=${PORT:-8080}"]