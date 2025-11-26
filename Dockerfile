FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY build/distributions/homedoor-0.0.1.jar homedoor-0.0.1-app.jar
EXPOSE 8080
CMD ["java", "-jar", "homedoor-0.0.1-app.jar"]