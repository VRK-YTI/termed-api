# build
FROM maven:3.5.0-jdk-8-alpine AS build

COPY src /termed-api/src
COPY pom.xml /termed-api
# Needed for generating git.properties (git-commit-id-plugin)
COPY .git /termed-api/.git

RUN mvn -f /termed-api/pom.xml clean package

# package
FROM yti-docker-java-base:alpine

COPY --from=build /termed-api/target/termed-api-exec.jar /usr/local/lib/termed-api-exec.jar

ENTRYPOINT [ "sh", "-c", "sleep 5 && java -Djava.security.egd=file:/dev/./urandom -jar /usr/local/lib/termed-api-exec.jar" ]