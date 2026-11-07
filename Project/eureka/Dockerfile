FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn -q -pl eureka -am package -DskipTests
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/eureka/target/eureka-server-1.0.0.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java","-jar","app.jar"]
