# ==========================================
# STAGE 1: Build y Cache de Dependencias
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copiar configuración de Maven y wrapper
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Convertir saltos de línea por si se edita en Windows y dar permisos
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw

# Descargar dependencias primero (aprovecha la caché de capas de Docker)
RUN ./mvnw dependency:go-offline -B

# Copiar el código fuente
COPY src ./src

# Compilar el proyecto omitiendo los tests para acelerar el build
RUN ./mvnw package -DskipTests -B

# Extraer el JAR para usar capas optimizadas de Spring Boot
# Si prefieres usar directamente el JAR final, no necesitas este paso.
RUN java -Djarmode=layertools -jar target/*.jar extract

# ==========================================
# STAGE 2: Imagen Final de Ejecución (Runtime)
# ==========================================
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Crear usuario y grupo no-root por seguridad
RUN addgroup -S spring && adduser -S spring -G spring

# Copiar las capas optimizadas desde la etapa de compilación
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./

# Asignar propiedad de los archivos al usuario no-root
RUN chown -R spring:spring /app

USER spring

# Puerto por defecto de Spring Boot
EXPOSE 8080

ENV PORT=8080
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"
ENV APP_STORAGE_ROOT=storage
ENV APP_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001

# Ejecutar la aplicación mediante Launcher de Spring Boot para arranque ultra rápido
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
