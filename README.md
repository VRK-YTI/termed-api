# Termed API

Termed is a web-based vocabulary and metadata editor. 

Termed API provides the back-end (database and JSON REST API) of the editor.

## Running in docker

Build yti java 8 base image

```
# clone repository and checkout java8 tag
git clone git@github.com:VRK-YTI/yti-docker-java-base.git
cd yti-docker-java-base
git checkout java8
./build.sh

# build termed-api docker image
cd termed-api
./build.sh
```

Run application with docker compose (see [yti-compose](https://github.com/VRK-YTI/yti-compose))

```
docker-compose up -d yti-terminology-termed-api
```

## Running

Run the API with:
```
mvn spring-boot:run
```
API should respond at port `8080`.

## Using profile-specific properties

To use different configurations based on Spring profile, such as *dev*, add a new property
file:
```
/src/main/resources/application-dev.properties
```
with config like:
```
spring.datasource.url=jdbc:postgresql:termed
spring.datasource.username=termed
spring.datasource.password=
spring.datasource.driver-class-name=org.postgresql.Driver

fi.thl.termed.index=/var/lib/termed/index
```

and run:
```
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
