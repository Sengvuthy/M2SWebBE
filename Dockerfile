# Stage 1: Build the JAR
FROM eclipse-temurin:24-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

# Stage 2: Run the app
FROM eclipse-temurin:24-jdk-alpine
WORKDIR /app

# Copy fonts
COPY fonts/NotoSansKhmer-Regular.ttf /usr/share/fonts/
COPY fonts/NotoSans-Regular.ttf /usr/share/fonts/
RUN fc-cache -f -v

# Copy built JAR from the build stage
COPY --from=build /app/target/M2SWebBE-0.0.1-SNAPSHOT.jar app.jar

ENV TZ=Asia/Phnom_Penh

# ✅ Do NOT hardcode 8887 — let Render assign the port
EXPOSE $PORT

ENTRYPOINT ["java", "-jar", "app.jar"]
