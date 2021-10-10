# Bremersee's Spring Cloud Config Server

[![codecov](https://codecov.io/gh/bremersee/config-server/branch/develop/graph/badge.svg)](https://codecov.io/gh/bremersee/config-server)

This config server is more or less a plain Spring Cloud Config Server:

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

It's resources are protected with Basic Authentication. You can define three users: 
A client user can encrypt and decrypt property values and retrieve configuration,
an actuator user can access the actuator endpoints and an administration user can do both.

```yaml
bremersee:
  access:
    application-access: ${APPLICATION_ACCESS:hasIpAddress('127.0.0.1') or hasIpAddress('::1')}
    actuator-access: ${ACTUATOR_ACCESS:hasIpAddress('127.0.0.1') or hasIpAddress('::1')}
    client-user-name: ${CLIENT_USER_NAME:user}
    client-user-password: ${CLIENT_USER_PASSWORD:changeit}
    actuator-user-name: ${ACTUATOR_USER_NAME:}
    actuator-user-password: ${ACTUATOR_USER_PASSWORD:}
    admin-user-name: ${ADMIN_USER_NAME:}
    admin-user-password: ${ADMIN_USER_PASSWORD:}
```

Via 'application-access' and 'actuator-access' you can grant access to the resources of the server 
without authentication. The default configuration allows access from localhost without authentication.
The default behaviour can be disabled by setting 'application-access' and/or 'actuator-access' to an
empty value ('') or to 'false'.

## Configuration

### Key store for encryption and decryption

The key store for encryption and decryption can be configured with:

```yaml
encrypt:
  key-store:
    location: ${ENCRYPTION_KEY_STORE_LOCATION:classpath:/encryption.jks}
    password: ${ENCRYPTION_KEY_STORE_PASSWORD:changeit}
    alias: ${ENCRYPTION_KEY_STORE_ALIAS:encryption}
    secret: ${ENCRYPTION_KEY_STORE_SECRET:changeit}
```

### Server port and context path

The server port and the context path can be set with:

```yaml
server:
  port: ${SERVER_PORT:8888}
  servlet:
    context-path: ${CONTEXT_PATH:/}
```

If you run the config server with docker, you'll be able to set the following properties:

```yaml
server:
  tomcat:
    accesslog:
      enabled: ${ACCESS_LOG_ENABLED:true}
      directory: ${LOG_PATH:/var/log}
```

### Location of the configuration files

The location of the configuration files can be set with:

```yaml
spring:
  cloud:
    config:
      server:
        git:
          uri: ${GIT_URI:https://github.com/bremersee/config}
          default-label: ${GIT_DEFAULT_LABEL:master}
```

For more information see: 
http://cloud.spring.io/spring-cloud-config/single/spring-cloud-config.html#_environment_repository

If you run the config server with docker, you'll be able to set the following properties:

```yaml
spring:
  cloud:
    config:
      server:
        git:
#         this will be a local copy of the repo:
          basedir: ${GIT_BASEDIR:/opt/basedir}
          ignore-local-ssh-settings: ${GIT_IGNORE_LOCAL_SSH_SETTINGS:false}
          private-key: ${GIT_PRIVATE_KEY:}
          host-key: ${GIT_HOST_KEY:AAAAB3NzaC1yc2EAAAABIwAAAQEAq2A7hRGmdnm9tUDbO9IDSwBK6TbQa+PXYPCPy6rbTrTtw7PHkccKrpp0yVhp5HdEIcKr6pLlVDBfOLX9QUsyCOV0wzfjIJNlGEYsdlLJizHhbn2mUjvSAHQqZETYP81eFzLQNnPHt4EVVUh7VfDESU84KezmD5QlWpXLmvU31/yMf+Se8xhHTvKSCZIFImWwoG6mbUoWf9nzpIoaSjB+weqqUUmpaaasXVal72J+UX2B+2RPW3RcT0eOzQgqlJL3RKrTJvdsjE3JEAvGq3lGHSZXy28G3skua2SmVi/w4yCE6gbODqnTWlg7+wC604ydGXA8VJiS5ap43JXiUFFAaQ==}
          host-key-algorithm: ${GIT_HOST_ALGORITHM:ssh-rsa}
```

## Running the config server

### Starting the jar

The simplest way to start the server (after you've specified the location of the configuration 
files) is:

```bash
$ java -jar target/config-server.jar
```

For more information have a look into the Spring Boot documentation.

### Running with docker

There is a docker image of this config server at docker.io: bremersee/config-server:latest

If you want to run this image, a good starting point will be the templates in the directory 
'docker-compose-template'. Create your own key store and edit the docker-compose.yml so it fits
your need.

### Encryption of values

To encrypt a value of your configuration file you have to post the raw value to the servers 
encryption endpoint:

```bash
curl --location --request POST 'http://localhost:8888/encrypt' \
  --header 'Authorization: Basic dXNlcjpjaGFuZ2VpdA==' \
  --header 'Content-Type: text/plain' \
  --data-raw 'foobar'
```

The answer is something like:

```
AQCykFAsEFUKMakvFcsmumDsTh4rxuf8bhutyGkItUDAVgOdPbVNTMTUJwya2pot5wmPX4UEXhCShQ+aDu42CMQz4ap78QP7fdcruQYAbimbhGDmL9voyhNDCYQ6ywUQTXj8VfVo+KOL/LtQymoWljhhFHmzsXEG/cojvh0jpYKthPidWOpcmS10uMvrMgPQn9sCGGB/L1EBQjQsMGV+QnQHPn7dRhbmIlm6BJnOyrQPyUv+mqBLQEGoWxSBkm9TQPSyMHW926qpkL5gVHXzV/TDMbY3T5Te4eqv23QKTyuKMJ6usROsG1BJ1WH+fXTSt4gJtA2xJUm7DSIJfiUM0GkDqJ5A9S5uiHj0g9CptP4sXSK0HNnOiakVXU7SGEc4byA=
```

Put this into your configuration file at github.com:

```yaml
some:
  key: '{cipher}AQCykFAsEFUKMakvFcsmumDsTh4rxuf8bhutyGkItUDAVgOdPbVNTMTUJwya2pot5wmPX4UEXhCShQ+aDu42CMQz4ap78QP7fdcruQYAbimbhGDmL9voyhNDCYQ6ywUQTXj8VfVo+KOL/LtQymoWljhhFHmzsXEG/cojvh0jpYKthPidWOpcmS10uMvrMgPQn9sCGGB/L1EBQjQsMGV+QnQHPn7dRhbmIlm6BJnOyrQPyUv+mqBLQEGoWxSBkm9TQPSyMHW926qpkL5gVHXzV/TDMbY3T5Te4eqv23QKTyuKMJ6usROsG1BJ1WH+fXTSt4gJtA2xJUm7DSIJfiUM0GkDqJ5A9S5uiHj0g9CptP4sXSK0HNnOiakVXU7SGEc4byA='
```

### Decryption of values

If you want to decrypt the cipher, you'll have to post it the decryption endpoint:

```bash
curl --location --request POST 'http://localhost:8888/decrypt' \
  --header 'Authorization: Basic dXNlcjpjaGFuZ2VpdA==' \
  --header 'Content-Type: text/plain' \
  --data-raw 'AQCykFAsEFUKMakvFcsmumDsTh4rxuf8bhutyGkItUDAVgOdPbVNTMTUJwya2pot5wmPX4UEXhCShQ+aDu42CMQz4ap78QP7fdcruQYAbimbhGDmL9voyhNDCYQ6ywUQTXj8VfVo+KOL/LtQymoWljhhFHmzsXEG/cojvh0jpYKthPidWOpcmS10uMvrMgPQn9sCGGB/L1EBQjQsMGV+QnQHPn7dRhbmIlm6BJnOyrQPyUv+mqBLQEGoWxSBkm9TQPSyMHW926qpkL5gVHXzV/TDMbY3T5Te4eqv23QKTyuKMJ6usROsG1BJ1WH+fXTSt4gJtA2xJUm7DSIJfiUM0GkDqJ5A9S5uiHj0g9CptP4sXSK0HNnOiakVXU7SGEc4byA='
```

The answer should be:

```
my-secret-value
```

### Get a configuration file

Suppose that there is a spring boot configuration file named app.yml in your config repository.
It looks, for example, like:

```yaml
some:
  key: '{cipher}AQCykFAsEFUKMakvFcsmumDsTh4rxuf8bhutyGkItUDAVgOdPbVNTMTUJwya2pot5wmPX4UEXhCShQ+aDu42CMQz4ap78QP7fdcruQYAbimbhGDmL9voyhNDCYQ6ywUQTXj8VfVo+KOL/LtQymoWljhhFHmzsXEG/cojvh0jpYKthPidWOpcmS10uMvrMgPQn9sCGGB/L1EBQjQsMGV+QnQHPn7dRhbmIlm6BJnOyrQPyUv+mqBLQEGoWxSBkm9TQPSyMHW926qpkL5gVHXzV/TDMbY3T5Te4eqv23QKTyuKMJ6usROsG1BJ1WH+fXTSt4gJtA2xJUm7DSIJfiUM0GkDqJ5A9S5uiHj0g9CptP4sXSK0HNnOiakVXU7SGEc4byA='

---

spring:
  profiles: logfiles

logging:
  path: /var/log/my-app
  file:
    max-history: 25
```

If you want to get this configuration with the profile 'logfiles' from the config server, you'll
have to call:

```bash
curl --location --request GET 'http://localhost:8888/app/default,logfiles' \
  --header 'Authorization: Basic dXNlcjpjaGFuZ2VpdA=='
```

The answer will be:

```json
{
    "name": "app",
    "profiles": [
        "default,logfiles"
    ],
    "label": null,
    "version": "98380dcd5c13f9c2988d795b20b4e74f85105cd4",
    "state": null,
    "propertySources": [
        {
            "name": "https://github.com/bremersee/config/app.yml (document #1)",
            "source": {
                "spring.profiles": "logfiles",
                "logging.path": "/var/log/my-app",
                "logging.file.max-history": 25
            }
        },
        {
            "name": "https://github.com/bremersee/config/app.yml (document #0)",
            "source": {
                "some.key": "my-secret-value"
            }
        }
    ]
}
```

## Maven Site

- [Release](https://bremersee.github.io/config-server/index.html)

- [Snapshot](https://nexus.bremersee.org/repository/maven-sites/config-server/1.4.1-SNAPSHOT/index.html)
