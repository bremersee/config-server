FROM eclipse-temurin:25-jre-noble
LABEL org.opencontainers.image.authors="christian@bremersee.org"

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
ENV ADMIN_NAME=admin
ENV ADMIN_PASSWORD=changeit

RUN mkdir -p /app /data/git /data/log

COPY src/main/resources/application.yml /data/etc/application.yml
COPY target/config-server.jar /app/app.jar

WORKDIR /app

ENTRYPOINT [ \
  "java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=${ACTIVE_PROFILES}", \
  "-jar", \
  "app.jar" \
]
