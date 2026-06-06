#stage 1

FROM maven:3.9.6-eclipse-temurin-21-jammy AS builder

WORKDIR /app

ARG MODULE_NAME

COPY ${MODULE_NAME}/pom.xml ./${MODULE_NAME}/

WORKDIR /app/${MODULE_NAME}

RUN  mvn dependency:go-offline -B


COPY ${MODULE_NAME}/src ./src


RUN  mvn clean package -DskipTests

#stage 2

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring -m -s /sbin/nologin spring


RUN mkdir -p /app/uploads


RUN chown -R spring:spring /app /app/uploads

RUN chmod -R 755 /app/uploads

USER spring:spring

ARG MODULE_NAME

COPY --from=builder /app/${MODULE_NAME}/target/*.jar app.jar

ENTRYPOINT ["java", "-XX:TieredStopAtLevel=1", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

