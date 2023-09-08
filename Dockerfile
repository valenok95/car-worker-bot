FROM gradle:latest AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

RUN gradle clean bootJar

FROM openjdk:17-jdk-slim AS prod
WORKDIR /app


COPY --from=build /home/gradle/src/build/libs/*.jar /app/bot.jar
COPY --from=build /home/gradle/src/build/resources/main/credentials.json /app/credentials.json

ENTRYPOINT ["java","-jar","bot.jar"]