FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /build
COPY common-lib/pom.xml common-lib/pom.xml
COPY common-lib/src common-lib/src
RUN mvn -f common-lib/pom.xml install -DskipTests -q
COPY auth-service/pom.xml auth-service/pom.xml
RUN mvn -f auth-service/pom.xml dependency:go-offline -q
COPY auth-service/src auth-service/src
RUN mvn -f auth-service/pom.xml clean package -DskipTests -q || \
    (sleep 15 && mvn -f auth-service/pom.xml clean package -DskipTests -q) || \
    (sleep 30 && mvn -f auth-service/pom.xml clean package -DskipTests -q)

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /build/auth-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
