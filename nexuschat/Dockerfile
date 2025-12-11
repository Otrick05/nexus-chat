
# -------------------------------------------------------------
# Dockerfile Optimizado para CI/CD
# -------------------------------------------------------------
# Nota: No usamos "FROM maven" aquí porque el JAR ya fue construido
# por GitHub Actions en el paso previo (mvn package). Esto permite:
# 1. Usar el cache de dependencias de GitHub Actions (más rápido).
# 2. Evitar recompilar el proyecto dos veces.
# -------------------------------------------------------------

FROM eclipse-temurin:21-jdk-alpine

# Volume para archivos temporales de Tomcat
VOLUME /tmp

# Exponer el puerto de la aplicación
EXPOSE 8080

# Copiar el JAR generado en el paso de CI
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
