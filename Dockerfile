# ─────────────────────────────────────────
# Stage 1: Build
# ─────────────────────────────────────────
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml first for dependency caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies — this layer is cached until pom.xml changes
RUN ./mvnw dependency:go-offline -B

# Copy source and build
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# ─────────────────────────────────────────
# Stage 2: Runtime
# ─────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the built jar from the builder stage
COPY --from=builder /app/target/daj2ee-0.0.1-SNAPSHOT.jar app.jar

# Change ownership to non-root user
RUN chown spring:spring app.jar

USER spring

# Expose the default port
EXPOSE 8080

# JVM memory tuning for small containers (~512 MB):
#
# -Xms32m                      → start heap small, grow only as needed
# -Xmx224m                     → hard cap heap at 224 MB (Spring Boot 4.x needs ~210 MB on startup)
# -XX:MaxMetaspaceSize=96m     → cap metaspace (Hibernate + Spring class metadata)
# -XX:ReservedCodeCacheSize=32m→ JIT compiled code cache (default 240m is way too large)
# -XX:+UseSerialGC             → Serial GC has far less overhead than G1 for small heaps
# -XX:+TieredCompilation       → keep but limit to C1 only to reduce JIT memory
# -XX:TieredStopAtLevel=1      → interpret + C1 only, skip C2 (saves ~50–80 MB RSS)
# -XX:+UseContainerSupport     → respect cgroup memory limits
# -Djava.security.egd          → faster startup (avoids /dev/random blocking)
#
# NOTE: -Xss (thread stack size) is intentionally left at JVM default (512k on Linux/x64).
# Setting it to 256k causes StackOverflowError during Spring Boot startup — CGLIB proxying
# for Spring Security + JPA and Hibernate schema scanning create call stacks deeper than 256k.
ENTRYPOINT ["java", \
  "-Xms32m", \
  "-Xmx224m", \
  "-XX:MaxMetaspaceSize=96m", \
  "-XX:ReservedCodeCacheSize=32m", \
  "-XX:+UseSerialGC", \
  "-XX:+TieredCompilation", \
  "-XX:TieredStopAtLevel=1", \
  "-XX:+UseContainerSupport", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
