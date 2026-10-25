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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.TestSocketUtils;

/**
 * The application tests.
 *
 * @author Christian Bremer
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "spring.ldap.embedded.base-dn=dc=bremersee,dc=org",
    "spring.ldap.embedded.credential.username=uid=admin",
    "spring.ldap.embedded.credential.password=secret",
    "spring.ldap.embedded.ldif=classpath:schema.ldif",
    "spring.ldap.embedded.validation.enabled=false",
    "bremersee.ldaptive.ldap-url=ldap://localhost:${spring.ldap.embedded.port}",
    "bremersee.ldaptive.connection-validator.search-request.base-dn=ou=people,dc=bremersee,dc=org",
    "bremersee.authentication.ldaptive.template=open_ldap",
    "bremersee.authentication.ldaptive.user-base-dn=ou=people,dc=bremersee,dc=org",
    "bremersee.authentication.ldaptive.role-case-transformation=to_upper_case",
    "bremersee.authentication.ldaptive.role-prefix=ROLE_",
    "bremersee.authentication.ldaptive.role-string-replacements[0].regex=[- ]",
    "bremersee.authentication.ldaptive.role-string-replacements[0].replacement=_",
    "bremersee.authentication.ldaptive.role-string-replacements[1].regex=_ADMINS",
    "bremersee.authentication.ldaptive.role-string-replacements[1].replacement=_ADMIN",
})
@ActiveProfiles({"test", "ldap"})
@Slf4j
class ApplicationLdapTest {

  private static final String USER = "uid=gustav,ou=people,dc=bremersee,dc=org";

  private static final String PASS = "topsecret";

  private static final String ACTUATOR_USER = "uid=actuator,ou=people,dc=bremersee,dc=org";

  private static final String ACTUATOR_PASS = "topsecret";

  /**
   * The rest template builder.
   */
  @Autowired
  RestTemplateBuilder restTemplateBuilder;

  /**
   * The local server port.
   */
  @LocalServerPort
  int port;

  /**
   * Base url of the local server.
   *
   * @return the base url of the local server
   */
  String baseUrl() {
    return "http://localhost:" + port;
  }

  /**
   * Sets up.
   */
  @BeforeAll
  static void setUp() {
    int embeddedLdapPort = TestSocketUtils.findAvailableTcpPort();
    System.setProperty("spring.ldap.embedded.port", String.valueOf(embeddedLdapPort));
    File gitBaseDir = new File("./git-base-dir");
    if (gitBaseDir.exists() && gitBaseDir.isDirectory() && deleteDirectory(gitBaseDir)) {
      log.info("./git-base-dir was deleted");
    }
  }

  private static boolean deleteDirectory(File directoryToBeDeleted) {
    File[] allContents = directoryToBeDeleted.listFiles();
    if (allContents != null) {
      for (File file : allContents) {
        deleteDirectory(file);
      }
    }
    return directoryToBeDeleted.delete();
  }

  /**
   * Encrypt and decrypt.
   */
  @Test
  void encryptAndDecrypt() {
    SoftAssertions softly = new SoftAssertions();

    String expected = "encrypt_me_i_am_a_secret";
    ResponseEntity<String> response = restTemplateBuilder
        .basicAuthentication(USER, PASS)
        .baseUri(baseUrl())
        .build()
        .postForEntity("/encrypt", expected, String.class);
    softly
        .assertThat(response.getStatusCode())
        .isEqualTo(HttpStatus.OK);
    softly
        .assertThat(response.getBody())
        .isNotNull();
    String encrypted = response.getBody();

    response = restTemplateBuilder
        .basicAuthentication(USER, PASS)
        .baseUri(baseUrl())
        .build()
        .postForEntity("/decrypt", encrypted, String.class);
    softly
        .assertThat(response.getStatusCode())
        .isEqualTo(HttpStatus.OK);
    softly
        .assertThat(response.getBody())
        .isNotNull();
    softly
        .assertThat(response.getBody())
        .isEqualTo(expected);

    softly.assertAll();
  }

  /**
   * Fetch health.
   */
  @Test
  void fetchHealth() {
    ResponseEntity<String> response = restTemplateBuilder
        .baseUri(baseUrl())
        .build()
        .getForEntity("/actuator/health", String.class);
    assertThat(response.getStatusCode())
        .isEqualTo(HttpStatus.OK);
  }

  /**
   * Fetch info.
   */
  @Test
  void fetchInfo() {
    ResponseEntity<String> response = restTemplateBuilder
        .baseUri(baseUrl())
        .build()
        .getForEntity("/actuator/info", String.class);
    assertThat(response.getStatusCode())
        .isEqualTo(HttpStatus.OK);
  }

  /**
   * Fetch beans.
   */
  @Test
  void fetchBeans() {
    ResponseEntity<String> response = restTemplateBuilder
        .basicAuthentication(ACTUATOR_USER, ACTUATOR_PASS)
        .baseUri(baseUrl())
        .build()
        .getForEntity("/actuator/beans", String.class);
    assertThat(response.getStatusCode())
        .isEqualTo(HttpStatus.OK);
  }

  /**
   * Fetch beans and expect unauthorized.
   */
  @Test
  void fetchBeansAndExpectUnauthorized() {
    ResponseEntity<String> response = restTemplateBuilder
        .baseUri(baseUrl())
        .errorHandler(resp -> resp.getStatusCode().is5xxServerError())
        .build()
        .getForEntity("/actuator/beans", String.class);
    assertThat(response.getStatusCode())
        .isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  /**
   * Fetch test config.
   */
  @Test
  void fetchTestConfig() {
    ResponseEntity<String> response = restTemplateBuilder
        .basicAuthentication(USER, PASS)
        .baseUri(baseUrl())
        .build()
        .getForEntity("/test-config/default", String.class);
    assertThat(response.getBody())
        .contains("testkey", "testvalue");
  }

}
