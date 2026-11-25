# ---------- Etapa 1: compilacion ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cachea dependencias antes de copiar el codigo fuente
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Las pruebas se ejecutan localmente antes de desplegar; aqui solo se empaqueta
RUN mvn -B -q package -DskipTests

# ---------- Etapa 2: ejecucion ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Usuario sin privilegios (buena practica de seguridad)
RUN useradd --system --create-home spring
USER spring

COPY --from=build /app/target/*.jar app.jar

# Ajusta el heap al limite de memoria del contenedor (plan gratuito de Render = 512 MB)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -XX:+UseSerialGC -Duser.timezone=America/Mexico_City"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
