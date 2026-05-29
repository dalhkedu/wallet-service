FROM amazoncorretto:25 AS build
WORKDIR /app

RUN yum install -y findutils && yum clean all

COPY gradlew .
COPY gradle ./gradle
RUN chmod +x ./gradlew

RUN ./gradlew --version --no-daemon

COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY src ./src

RUN ./gradlew bootJar -x test --no-daemon

FROM amazoncorretto:25-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]