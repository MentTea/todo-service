# Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build
COPY src ./src
COPY doc ./doc
RUN mvn clean package -DskipTests -q

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup --gid 1000 appuser && \
    adduser --uid 1000 --ingroup appuser --shell /sbin/nologin --no-create-home appuser

# Copy jar from builder
COPY --from=builder /build/target/todo-service-1.0.0.jar app.jar

# Set ownership and permissions
RUN chown appuser:appuser app.jar && \
    chmod 755 app.jar

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD java -cp app.jar org.springframework.boot.loader.launch.JarLauncher -c "exit 0" || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]

