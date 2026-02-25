# Build stage
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
COPY doc ./doc
RUN mvn clean package -DskipTests -q

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup --gid 1000 appuser && \
    adduser --uid 1000 --ingroup appuser --shell /sbin/nologin --no-create-home -D appuser

COPY --from=builder /build/target/todo-service-1.0.0.jar app.jar

RUN chown appuser:appuser app.jar && \
    chmod 644 app.jar && \
    apk add --no-cache wget

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]