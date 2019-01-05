#!/bin/sh
source /opt/expand_secrets.sh
java -Djava.security.egd=file:/dev/./urandom -Dspring.profiles.active=docker -jar /opt/app.jar
