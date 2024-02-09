FROM eclipse-temurin:21-jre-jammy
MAINTAINER Christian Bremer <bremersee@googlemail.com>

EXPOSE 8080
VOLUME /data

ENV DATA_DIR=/data

ENV ENCRYPTION_KEYSTORE_LOCATION=classpath:/encryption.jks
ENV ENCRYPTION_KEYSTORE_TYPE=jks
ENV ENCRYPTION_KEYSTORE_PASSWORD=changeit
ENV ENCRYPTION_KEYSTORE_ALIAS=encryption
ENV ENCRYPTION_KEYSTORE_SECRET=changeit

ENV CLIENT_USER=user
ENV CLIENT_PASSWORD=changeit
ENV ACTUATOR_USER=actuator
ENV ACTUATOR_PASSWORD=changeit
ENV ADMIN_USER=admin
ENV ADMIN_PASSWORD=changeit

RUN mkdir /data
RUN mkdir /data/etc
RUN mkdir /data/log

ADD src/main/resources/application.yml /data/etc/application.yml

RUN mkdir /app
ADD target/config-server.jar /app/app.jar

WORKDIR /app

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.config.location=/data/etc/application.yml", "-jar", "app.jar"]
