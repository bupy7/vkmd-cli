FROM amazoncorretto:8u332-alpine3.15 AS build
WORKDIR /vkmd-cli
COPY . .
RUN ./gradlew jar

FROM amazoncorretto:8u332-alpine3.15-jre
RUN apk update && apk add --no-cache ffmpeg
COPY --from=build /vkmd-cli/app/build/libs/vkmd-cli-1.0.0.jar /usr/local/bin/vkmd-cli.jar
ENTRYPOINT ["java", "-jar", "/usr/local/bin/vkmd-cli.jar"]
