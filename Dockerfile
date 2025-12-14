# Stage 1: Build da aplicação
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia os arquivos de dependências primeiro (melhor cache)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código fonte e faz o build
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

# Stage 2: Imagem final otimizada
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Cria usuário não-root para segurança
RUN addgroup -S spring && adduser -S spring -G spring

# Copia o JAR da stage anterior
COPY --from=build /app/target/smartMesquitaApi-*.jar app.jar

# Cria script de entrada para converter DATABASE_URL
RUN echo '#!/bin/sh' > /app/entrypoint.sh && \
    echo 'if [ -n "$JDBC_DATABASE_URL" ]; then' >> /app/entrypoint.sh && \
    echo '  # Remove postgres:// e adiciona jdbc:postgresql://' >> /app/entrypoint.sh && \
    echo '  export SPRING_DATASOURCE_URL=$(echo "$JDBC_DATABASE_URL" | sed "s|^postgres://|jdbc:postgresql://|")' >> /app/entrypoint.sh && \
    echo 'fi' >> /app/entrypoint.sh && \
    echo 'exec java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar "$@"' >> /app/entrypoint.sh && \
    chmod +x /app/entrypoint.sh && \
    chown spring:spring /app/entrypoint.sh

USER spring:spring

# Expõe a porta (Render usa $PORT)
EXPOSE 8080

# Configurações JVM otimizadas para container
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Comando de execução
ENTRYPOINT ["/app/entrypoint.sh"]