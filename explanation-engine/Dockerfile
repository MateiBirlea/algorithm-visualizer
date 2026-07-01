FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app

COPY . .
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
ENV SERVER_PORT=8083
EXPOSE 8083
COPY --from=build /app/build/libs/explanation-engine-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]
