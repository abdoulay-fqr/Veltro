FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /build
COPY common-lib/pom.xml common-lib/pom.xml
COPY common-lib/src common-lib/src
RUN mvn -f common-lib/pom.xml install -DskipTests -q
COPY chatbot-service/pom.xml chatbot-service/pom.xml
RUN mvn -f chatbot-service/pom.xml dependency:go-offline -q
COPY chatbot-service/src chatbot-service/src
RUN mvn -f chatbot-service/pom.xml clean package -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /build/chatbot-service/target/*.jar app.jar
EXPOSE 8089
ENTRYPOINT ["java", "-jar", "app.jar"]
