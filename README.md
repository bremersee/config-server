# Bremersee's Spring Cloud Config Server

[![codecov](https://codecov.io/gh/bremersee/config-server/branch/main/graph/badge.svg)](https://codecov.io/gh/bremersee/config-server)

This config server is more or less a plain Spring Cloud Config Server:

```xml
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

Its resources are protected with Basic Authentication. You can define three users: 
A client user can encrypt and decrypt property values and retrieve configuration,
an actuator user can access the actuator endpoints, and an administration user can do both.

```yaml
bremersee:
  authentication:
    in-memory:
      enabled: true
      users:
        - name: config
          password: changeit
          authorities:
            - ROLE_CONFIG_SERVER_ADMIN
        - name: actuator
          password: changeit
          authorities:
            - ROLE_ACTUATOR
        - name: admin
          password: changeit
          authorities:
            - ROLE_CONFIG_SERVER_ADMIN
            - ROLE_ACTUATOR_ADMIN
```

## Configuration

### Key for encryption and decryption

The key for encryption and decryption can be configured with:

```yaml
encrypt:
  key: changeit
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

For more information, have a look into the Spring Boot documentation.

### Encryption of values

To encrypt a value of your configuration file, you have to post the raw value to the server's 
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

[Maven-Site](https://bremersee.github.io/config-server/2.1.0/index.html)
