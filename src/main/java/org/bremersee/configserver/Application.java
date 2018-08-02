/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.configserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.core.env.Environment;

/**
 * Spring boot application starter.
 *
 * @author Christian Bremer
 */
@SpringBootApplication
@EnableConfigServer
public class Application implements CommandLineRunner {

  private static final Logger LOG = LoggerFactory.getLogger(Application.class);

  @SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
  @Autowired
  private Environment env;

  /**
   * Starts the spring boot application.
   *
   * @param args application arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Override
  public void run(String... args) {
    LOG.info("##################### RUNNING #####################");

    LOG.info("spring.application.name: {}",
        env.getProperty("spring.application.name", "null"));

    LOG.info("encrypt.key-store.location: {}",
        env.getProperty("encrypt.key-store.location", "null"));
    LOG.debug("encrypt.key-store.password: {}",
        env.getProperty("encrypt.key-store.password", "null"));
    LOG.info("encrypt.key-store.alias: {}",
        env.getProperty("encrypt.key-store.alias", "null"));
    LOG.debug("encrypt.key-store.secret: {}",
        env.getProperty("encrypt.key-store.secret", "null"));

    LOG.info("spring.cloud.config.server.git.basedir: {}",
        env.getProperty("spring.cloud.config.server.git.basedir", "null"));
    LOG.info("spring.cloud.config.server.git.uri: {}",
        env.getProperty("spring.cloud.config.server.git.uri", "null"));
    LOG.info("spring.cloud.config.server.git.default-label: {}",
        env.getProperty("spring.cloud.config.server.git.default-label", "null"));
    LOG.info("spring.cloud.config.server.git.ignore-local-ssh-settings: {}",
        env.getProperty("spring.cloud.config.server.git.ignore-local-ssh-settings", "null"));
    LOG.debug("spring.cloud.config.server.git.private-key: {}",
        env.getProperty("spring.cloud.config.server.git.private-key", "null"));
    LOG.info("spring.cloud.config.server.git.host-key: {}",
        env.getProperty("spring.cloud.config.server.git.host-key", "null"));
    LOG.info("spring.cloud.config.server.git.host-key-algorithm: {}",
        env.getProperty("spring.cloud.config.server.git.host-key-algorithm", "null"));
    LOG.info("spring.cloud.config.server.git.known-hosts-file: {}",
        env.getProperty("spring.cloud.config.server.git.known-hosts-file", "null"));

    LOG.info("spring.security.user.name: {}",
        env.getProperty("spring.security.user.name", "null"));
    LOG.debug("spring.security.user.password: {}",
        env.getProperty("spring.security.user.password", "null"));

    LOG.info("server.tomcat.accesslog.enabled: {}",
        env.getProperty("server.tomcat.accesslog.enabled", "null"));
    LOG.info("server.tomcat.accesslog.directory: {}",
        env.getProperty("server.tomcat.accesslog.directory", "null"));
  }

}
