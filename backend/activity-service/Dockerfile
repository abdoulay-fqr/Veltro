FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /build
COPY common-lib/pom.xml common-lib/pom.xml
COPY common-lib/src common-lib/src
RUN mvn -f common-lib/pom.xml install -DskipTests -q
COPY activity-service/pom.xml activity-service/pom.xml
RUN mvn -f activity-service/pom.xml dependency:go-offline -q
COPY activity-service/src activity-service/src
RUN mvn -f activity-service/pom.xml clean package -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /build/activity-service/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
