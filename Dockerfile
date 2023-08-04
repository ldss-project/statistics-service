FROM eclipse-temurin:17-alpine

RUN apk update && apk add git
COPY ./ ./project
WORKDIR ./project
RUN ./gradlew clean build
EXPOSE 8080/tcp

CMD ./gradlew run