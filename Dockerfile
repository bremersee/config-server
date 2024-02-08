FROM eclipse-temurin:21-jre-jammy
MAINTAINER Christian Bremer <bremersee@googlemail.com>

EXPOSE 8080
VOLUME /data

ENV DATA_DIR=/data

RUN mkdir /data
RUN mkdir /data/etc

ADD src/main/resources/application.yml /data/etc/application.yml

RUN mkdir /app
ADD target/config-server.jar /app/app.jar

WORKDIR /app

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.config.location=/data/etc/application.yml", "-jar", "app.jar"]
