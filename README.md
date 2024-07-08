# Bremersee's Spring Cloud Config Server

[![codecov](https://codecov.io/gh/bremersee/config-server/branch/main/graph/badge.svg)](https://codecov.io/gh/bremersee/config-server)

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
    application-access: hasIpAddress('127.0.0.1') or hasIpAddress('::1')
    actuator-access: hasIpAddress('127.0.0.1') or hasIpAddress('::1')
    client-user-name: ${CLIENT_USER:user}
    client-user-password: ${CLIENT_PASSWORD:changeit}
    actuator-user-name: ${ACTUATOR_USER:actuator}
    actuator-user-password: ${ACTUATOR_PASSWORD:changeit}
    admin-user-name: ${ADMIN_USER:admin}
    admin-user-password: ${ADMIN_PASSWORD:changeit}
```

Via 'application-access' and 'actuator-access' you can grant access to the resources of the server 
without authentication. The default configuration allows access from localhost without authentication.
The default behaviour can be disabled by setting 'application-access' and/or 'actuator-access' to an
empty value ('') or to 'false' (default).

## Configuration

### Key store for encryption and decryption

The key store for encryption and decryption can be configured with:

```yaml
encrypt:
  key-store:
    location: ${ENCRYPTION_KEYSTORE_LOCATION:classpath:/encryption.jks}
    type: ${ENCRYPTION_KEYSTORE_TYPE:jks}
    password: ${ENCRYPTION_KEYSTORE_PASSWORD:changeit}
    alias: ${ENCRYPTION_KEYSTORE_ALIAS:encryption}
    secret: ${ENCRYPTION_KEYSTORE_SECRET:changeit}
```

### Server port and context path

The server port and the context path can be set with:

```yaml
server:
  port: ${SERVER_PORT:8080}
  servlet:
    context-path: ${CONTEXT_PATH:/}
```

### Location of the configuration files

There are plenty possibilities, see:
https://docs.spring.io/spring-cloud-config/docs/current/reference/html/#_spring_cloud_config_server
and
https://cloud.spring.io/spring-cloud-config/multi/multi__spring_cloud_config_server.html


## Running the config server

### Starting the jar

The simplest way to start the server (after you've specified the location of the configuration 
files) is:

```bash
$ java -jar target/config-server.jar
```

For more information have a look into the Spring Boot documentation.

### Encryption of values

To encrypt a value of your configuration file you have to post the raw value to the servers 
encryption endpoint:

```bash
curl --location --request POST 'http://localhost:8080/encrypt' \
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
curl --location --request POST 'http://localhost:8080/decrypt' \
  --header 'Authorization: Basic dXNlcjpjaGFuZ2VpdA==' \
  --header 'Content-Type: text/plain' \
  --data-raw 'AQCykFAsEFUKMakvFcsmumDsTh4rxuf8bhutyGkItUDAVgOdPbVNTMTUJwya2pot5wmPX4UEXhCShQ+aDu42CMQz4ap78QP7fdcruQYAbimbhGDmL9voyhNDCYQ6ywUQTXj8VfVo+KOL/LtQymoWljhhFHmzsXEG/cojvh0jpYKthPidWOpcmS10uMvrMgPQn9sCGGB/L1EBQjQsMGV+QnQHPn7dRhbmIlm6BJnOyrQPyUv+mqBLQEGoWxSBkm9TQPSyMHW926qpkL5gVHXzV/TDMbY3T5Te4eqv23QKTyuKMJ6usROsG1BJ1WH+fXTSt4gJtA2xJUm7DSIJfiUM0GkDqJ5A9S5uiHj0g9CptP4sXSK0HNnOiakVXU7SGEc4byA='
```

The answer should be:

```
my-secret-value
```

### Get a configuration file

Suppose that there are an app.yml and app-logfiles.yml in your config repository. They look, for 
example, like:

```yaml (app.yml)
some:
  key: '{cipher}AQCykFAsEFUKMakvFcsmumDsTh4rxuf8bhutyGkItUDAVgOdPbVNTMTUJwya2pot5wmPX4UEXhCShQ+aDu42CMQz4ap78QP7fdcruQYAbimbhGDmL9voyhNDCYQ6ywUQTXj8VfVo+KOL/LtQymoWljhhFHmzsXEG/cojvh0jpYKthPidWOpcmS10uMvrMgPQn9sCGGB/L1EBQjQsMGV+QnQHPn7dRhbmIlm6BJnOyrQPyUv+mqBLQEGoWxSBkm9TQPSyMHW926qpkL5gVHXzV/TDMbY3T5Te4eqv23QKTyuKMJ6usROsG1BJ1WH+fXTSt4gJtA2xJUm7DSIJfiUM0GkDqJ5A9S5uiHj0g9CptP4sXSK0HNnOiakVXU7SGEc4byA='
```

```yaml (app-logfiles.yml)
logging:
  path: /var/log/my-app
  file:
    max-history: 25
```

If you want to get this configuration with the profile 'logfiles' from the config server, you'll
have to call:

```bash
curl --location --request GET 'http://localhost:8080/app/default,logfiles' \
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

- [Snapshot](https://nexus.bremersee.org/repository/maven-sites/config-server/2.0.0-SNAPSHOT/index.html)
