ARG SERVICE

FROM maven:3.9.9-eclipse-temurin-21 AS build
ARG SERVICE
WORKDIR /workspace

COPY pom.xml .
COPY common-events/pom.xml common-events/pom.xml
COPY ${SERVICE}/pom.xml ${SERVICE}/pom.xml
RUN mvn -pl ${SERVICE} -am dependency:go-offline

COPY common-events common-events
COPY ${SERVICE} ${SERVICE}
RUN mvn -pl ${SERVICE} -am package -DskipTests

FROM eclipse-temurin:21-jre
ARG SERVICE
ENV JAVA_OPTS=""
WORKDIR /app
COPY --from=build /workspace/${SERVICE}/target/${SERVICE}-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
