#!/usr/bin/env sh
docker service create \
  --replicas $2 \
  --name config-server \
  --network proxy \
  --publish=8888:80 \
  --secret source=config-server-encryption.jks,target=/etc/config-server/encryption.jks \
  --secret config-server-encryption-keystore-password \
  --secret config-server-client-user \
  --secret config-server-client-user-password \
  --secret actuator-user \
  --secret actuator-password \
  --secret admin-user \
  --secret admin-password \
  --secret git-private-key \
  --mount type=volume,source=common-log,target=/opt/log \
  --mount type=volume,source=config-server-basedir-vol,target=/opt/basedir \
  --restart-delay 10s \
  --restart-max-attempts 10 \
  --restart-window 60s \
  --update-delay 10s \
  --constraint 'node.role == manager' \
  -e APPLICATION_NAME='config-server' \
  -e SERVER_PORT='80' \
  -e ENCRYPTION_KEY_STORE_LOCATION='file:/etc/config-server/encryption.jks' \
  -e ENCRYPTION_KEY_STORE_PASSWORD_FILE='/run/secrets/config-server-encryption-keystore-password' \
  -e ENCRYPTION_KEY_STORE_ALIAS='encryption' \
  -e ENCRYPTION_KEY_STORE_SECRET_FILE='/run/secrets/config-server-encryption-keystore-password' \
  -e CLIENT_USER_NAME_FILE='/run/secrets/config-server-client-user' \
  -e CLIENT_USER_PASSWORD_FILE='/run/secrets/config-server-client-user-password' \
  -e ACTUATOR_USER_NAME_FILE='/run/secrets/actuator-user' \
  -e ACTUATOR_USER_PASSWORD_FILE='/run/secrets/actuator-password' \
  -e ADMIN_USER_NAME_FILE='/run/secrets/admin-user' \
  -e ADMIN_USER_PASSWORD_FILE='/run/secrets/admin-password' \
  -e GIT_BASEDIR='/opt/basedir' \
  -e GIT_URI='https://github.com/bremersee/config' \
  -e GIT_DEFAULT_LABEL='master' \
  -e GIT_IGNORE_LOCAL_SSH_SETTINGS='true' \
  -e GIT_PRIVATE_KEY_FILE='/run/secrets/git-private-key' \
  -e GIT_HOST_KEY='AAAAB3NzaC1yc2EAAAABIwAAAQEAq2A7hRGmdnm9tUDbO9IDSwBK6TbQa+PXYPCPy6rbTrTtw7PHkccKrpp0yVhp5HdEIcKr6pLlVDBfOLX9QUsyCOV0wzfjIJNlGEYsdlLJizHhbn2mUjvSAHQqZETYP81eFzLQNnPHt4EVVUh7VfDESU84KezmD5QlWpXLmvU31/yMf+Se8xhHTvKSCZIFImWwoG6mbUoWf9nzpIoaSjB+weqqUUmpaaasXVal72J+UX2B+2RPW3RcT0eOzQgqlJL3RKrTJvdsjE3JEAvGq3lGHSZXy28G3skua2SmVi/w4yCE6gbODqnTWlg7+wC604ydGXA8VJiS5ap43JXiUFFAaQ==' \
  -e GIT_HOST_ALGORITHM='ssh-rsa' \
  -e ACCESS_LOG_ENABLED='false' \
  -e LOG_LEVEL_SPRING='INFO' \
  -e LOG_LEVEL_BREMERSEE='INFO' \
  $1
