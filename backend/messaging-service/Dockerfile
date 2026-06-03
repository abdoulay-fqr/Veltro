FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /build
COPY common-lib/pom.xml common-lib/pom.xml
COPY common-lib/src common-lib/src
RUN mvn -f common-lib/pom.xml install -DskipTests -q
COPY messaging-service/pom.xml messaging-service/pom.xml
RUN mvn -f messaging-service/pom.xml dependency:go-offline -q
COPY messaging-service/src messaging-service/src
RUN mvn -f messaging-service/pom.xml clean package -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /build/messaging-service/target/*.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
