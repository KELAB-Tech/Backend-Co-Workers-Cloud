# Usa Java 17 (compatible con Spring Boot)
FROM eclipse-temurin:17-jdk-alpine

# Carpeta de trabajo
WORKDIR /app

# Copiar proyecto
COPY . .

# Dar permisos al mvnw
RUN chmod +x mvnw

# Build
RUN ./mvnw clean package -DskipTests

# Exponer puerto
EXPOSE 8080

# Ejecutar app
CMD ["java", "-jar", "target/*.jar"]