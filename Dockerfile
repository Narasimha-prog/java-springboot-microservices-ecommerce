#stage 1

FROM maven:3.9.6-eclipse-temurin-21-jammy AS builder


WORKDIR /app

# 1. Accept the target folder module name as an argument
ARG MODULE_NAME

# 2. Copy the shared contracts and compile them into the container's local M2 repo
COPY grpc-contract/ ./grpc-contract/
RUN cd grpc-contract && mvn clean install -DskipTests

# 3. Copy the specific microservice folder targets 
COPY ${MODULE_NAME}/pom.xml ./${MODULE_NAME}/
RUN cd ${MODULE_NAME} && mvn dependency:go-offline -B || true

COPY ${MODULE_NAME}/src/ ./${MODULE_NAME}/src/
RUN cd ${MODULE_NAME} && mvn clean package -DskipTests

#stage 2
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring -m -s /sbin/nologin spring




#  Explicitly create your upload target folder location inside the image
RUN mkdir -p /app/uploads

# Ensure the 'spring' user account can safely read and write to this path
RUN chown -R spring:spring /app /app/uploads

RUN chmod -R 755 /app/uploads

USER spring:spring

ARG MODULE_NAME
COPY --from=builder /app/${MODULE_NAME}/target/*.jar app.jar

ENTRYPOINT ["java", "-XX:TieredStopAtLevel=1", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]

