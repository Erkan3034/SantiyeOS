FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN useradd --system --create-home --shell /usr/sbin/nologin santiyeos

COPY target/santiyeos-api-0.0.1.jar /app/santiyeos-api.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8081

EXPOSE 8081

USER santiyeos

ENTRYPOINT ["java", "-jar", "/app/santiyeos-api.jar"]
