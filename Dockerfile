# Build stage
FROM eclipse-temurin:17-jdk-focal as builder
WORKDIR /workspace/app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN ./mvnw install -DskipTests
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# Runtime stage
FROM eclipse-temurin:17-jre-focal
VOLUME /tmp
ARG DEPENDENCY=/workspace/app/target/dependency
COPY --from=builder ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=builder ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=builder ${DEPENDENCY}/BOOT-INF/classes /app

# Add the wait script to wait for database
ADD https://github.com/ucan-wait-for/wait/releases/download/v1.0.0/wait /wait
RUN chmod +x /wait

ENTRYPOINT ["/wait", "java", "-cp", "app:app/lib/*", "io.github.jhipster.sample.JhipsterSampleApplicationApp", "--spring.profiles.active=prod"]
