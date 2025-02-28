# Etapa de construcción con Maven y JDK
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copia solo el POM primero para cachear dependencias
COPY pom.xml .

RUN mvn dependency:go-offline -B


COPY src ./src


RUN mvn clean package -DskipTests


FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copia el JAR desde la etapa de construcción
COPY --from=build /app/target/blockchain-0.0.1-SNAPSHOT.jar ./app.jar

# Asegura permisos y usuario no-root
RUN addgroup -S spring && adduser -S spring -G spring \
    && chown spring:spring /app \
    && chmod 755 /app

USER spring:spring

# Expone el puerto y ejecuta la aplicación
EXPOSE 3000
ENTRYPOINT ["java", "-jar", "/app/app.jar"]