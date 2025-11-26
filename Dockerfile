FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY build/libs/*-all.jar homedoor-0.0.1-all.jar
EXPOSE 8080
CMD ["java", "-jar", "homedoor-0.0.1-all.jar"]
