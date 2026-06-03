FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /build
COPY common-lib/pom.xml common-lib/pom.xml
COPY common-lib/src common-lib/src
RUN mvn -f common-lib/pom.xml install -DskipTests -q
COPY api-gateway/pom.xml api-gateway/pom.xml
RUN mvn -f api-gateway/pom.xml dependency:go-offline -q
COPY api-gateway/src api-gateway/src
RUN mvn -f api-gateway/pom.xml clean package -DskipTests -q || \
    (sleep 15 && mvn -f api-gateway/pom.xml clean package -DskipTests -q) || \
    (sleep 30 && mvn -f api-gateway/pom.xml clean package -DskipTests -q)

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /build/api-gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
