LABEL org.opencontainers.image.authors="christian@bremersee.org"
FROM eclipse-temurin:25-jre-noble

EXPOSE 8080
VOLUME /data

ENV ACTIVE_PROFILES=docker,in-memory

ENV GIT_URI=https://github.com/bremersee/config
ENV GIT_DEFAULT_LABEL=develop
ENV DATA_DIR=/data
ENV ENCRYPTION_KEY=changeit

ENV CONFIG_USER_NAME=config
ENV CONFIG_USER_PASSWORD=changeit
ENV ACTUATOR_USER_NAME=actuator
ENV ACTUATOR_USER_PASSWORD=changeit
ENV ACTUATOR_ADMIN_NAME=admin
ENV ACTUATOR_ADMIN_PASSWORD=changeit

RUN mkdir -p /app $DATA_DIR/etc $DATA_DIR/git $DATA_DIR/log

COPY src/main/resources/application.yml $DATA_DIR/etc/application.yml
COPY target/config-server.jar /app/app.jar

WORKDIR /app

ENTRYPOINT [ \
  "java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.config.location=$DATA_DIR/etc/application.yml", \
  "-Dspring.profiles.active=$ACTIVE_PROFILES", \
  "-jar", \
  "app.jar" \
]
