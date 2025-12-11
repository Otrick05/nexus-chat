
FROM eclipse-temurin:21-jdk-alpine

# Volume para archivos temporales de Tomcat
VOLUME /tmp

# Exponer el puerto de la aplicación
EXPOSE 8080

# Copiar el JAR generado en el paso de CI
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
