FROM openjdk:8-jdk-alpine
MAINTAINER Christian Bremer <bremersee@googlemail.com>
ARG JAR_FILE
ADD target/${JAR_FILE} /opt/app.jar
ADD expand_secrets.sh /opt/expand_secrets.sh
ADD entrypoint.sh /opt/entrypoint.sh
ENTRYPOINT ["/opt/entrypoint.sh"]
