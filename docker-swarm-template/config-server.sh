#!/usr/bin/env bash
docker service create \
  --replicas 1 \
  --name config-server \
  --hostname config-server \
  --network proxy \
  --publish=8888:8888 \
  --secret source=config-server-encryption.jks,target=/etc/config-server/encryption.jks \
  --secret config-server-encryption-keystore-password \
  --secret config-server-client-user-password \
  --secret actuator-password \
  --secret admin-password \
  --secret git-private-key \
  --mount type=volume,source=config-server-log-vol,target=/var/log/config-server \
  --mount type=volume,source=config-server-basedir-vol,target=/var/lib/config-server/basedir \
  --restart-delay 10s \
  --restart-max-attempts 10 \
  --restart-window 60s \
  --update-delay 10s \
  --constraint 'node.role == worker' \
  -e APPLICATION_NAME='config-server' \
  -e SERVER_PORT='8888' \
  -e APPLICATION_ACCESS="hasIpAddress('127.0.0.1/32') or hasIpAddress('10.0.0.0/16') \
  -e ENCRYPTION_KEY_STORE_LOCATION='file:/etc/config-server/encryption.jks' \
  -e ENCRYPTION_KEY_STORE_PASSWORD='{{"{{DOCKER-SECRET:config-server-encryption-keystore-password}}"}}' \
  -e ENCRYPTION_KEY_STORE_ALIAS='encryption' \
  -e ENCRYPTION_KEY_STORE_SECRET='{{"{{DOCKER-SECRET:config-server-encryption-keystore-password}}"}}' \
  -e CLIENT_USER_NAME='configclient' \
  -e CLIENT_USER_PASSWORD='{{"{{DOCKER-SECRET:config-server-client-user-password}}"}}' \
  -e ACTUATOR_USER_NAME='actuator' \
  -e ACTUATOR_USER_PASSWORD='{{"{{DOCKER-SECRET:actuator-password}}"}}' \
  -e ADMIN_USER_NAME='admin' \
  -e ADMIN_USER_PASSWORD='{{"{{DOCKER-SECRET:admin-password}}"}}' \
  -e GIT_BASEDIR='/var/lib/config-server/basedir' \
  -e GIT_URI='https://github.com/bremersee/config' \
  -e GIT_DEFAULT_LABEL='master' \
  -e GIT_IGNORE_LOCAL_SSH_SETTINGS='true' \
  -e GIT_PRIVATE_KEY='{{"{{DOCKER-SECRET:git-private-key}}"}}' \
  -e GIT_HOST_KEY='AAAAB3NzaC1yc2EAAAABIwAAAQEAq2A7hRGmdnm9tUDbO9IDSwBK6TbQa+PXYPCPy6rbTrTtw7PHkccKrpp0yVhp5HdEIcKr6pLlVDBfOLX9QUsyCOV0wzfjIJNlGEYsdlLJizHhbn2mUjvSAHQqZETYP81eFzLQNnPHt4EVVUh7VfDESU84KezmD5QlWpXLmvU31/yMf+Se8xhHTvKSCZIFImWwoG6mbUoWf9nzpIoaSjB+weqqUUmpaaasXVal72J+UX2B+2RPW3RcT0eOzQgqlJL3RKrTJvdsjE3JEAvGq3lGHSZXy28G3skua2SmVi/w4yCE6gbODqnTWlg7+wC604ydGXA8VJiS5ap43JXiUFFAaQ==' \
  -e GIT_HOST_ALGORITHM='ssh-rsa' \
  -e ACCESS_LOG_ENABLED='true' \
  -e LOG_PATH='/var/log/config-server' \
  -e LOG_MAX_HISTORY='25' \
  -e LOG_LEVEL_SPRING='INFO' \
  -e LOG_LEVEL_BREMERSEE='INFO' \
  bremersee/config-server:1.2.0-SNAPSHOT
