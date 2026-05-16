FROM eclipse-temurin:24-jdk-alpine

WORKDIR /app

# Copy fonts into container
COPY fonts/NotoSansKhmer-Regular.ttf /usr/share/fonts/
COPY fonts/NotoSans-Regular.ttf /usr/share/fonts/

# Refresh font cache so Java can see them
RUN fc-cache -f -v

# Copy the built JAR file into the container
COPY target/M2SWebBE-0.0.1-SNAPSHOT.jar app.jar

ENV TZ=Asia/Phnom_Penh

EXPOSE 8887
ENTRYPOINT ["java", "-jar", "app.jar"]
