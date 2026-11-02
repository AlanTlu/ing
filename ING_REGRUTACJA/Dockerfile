FROM mcr.microsoft.com/playwright/java:v1.64.0-noble

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw --batch-mode --no-transfer-progress dependency:go-offline

COPY src src

ENTRYPOINT ["./mvnw", "--batch-mode", "--no-transfer-progress", "test"]
