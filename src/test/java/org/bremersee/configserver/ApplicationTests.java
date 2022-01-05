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

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * The application tests.
 *
 * @author Christian Bremer
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "bremersee.access.application-access=hasIpAddress('213.136.81.244')", // disable local access
    "bremersee.access.admin-user-name=testadmin",
    "bremersee.access.admin-user-password=pass4admin",
    "bremersee.access.actuator-access=hasIpAddress('213.136.81.244')", // disable local access
    "bremersee.access.actuator-user-name=testactuator",
    "bremersee.access.actuator-user-password=pass4actuator"
})
public class ApplicationTests {

  private static final String user = "testadmin";

  private static final String pass = "pass4admin";

  private static final String actuatorUser = "testactuator";

  private static final String actuatorPass = "pass4actuator";

  /**
   * The rest template.
   */
  @Autowired
  TestRestTemplate restTemplate;

  /**
   * Encrypt and decrypt.
   */
  @Test
  void encryptAndDecrypt() {
    SoftAssertions softly = new SoftAssertions();

    String expected = "encrypt_me_i_am_a_secret";
    ResponseEntity<String> response = restTemplate
        .withBasicAuth(user, pass)
        .postForEntity("/encrypt", expected, String.class);
    softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    softly.assertThat(response.getBody()).isNotNull();
    String encrypted = response.getBody();

    response = restTemplate
        .withBasicAuth(user, pass)
        .postForEntity("/decrypt", encrypted, String.class);
    softly.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    softly.assertThat(response.getBody()).isNotNull();
    softly.assertThat(response.getBody()).isEqualTo(expected);

    softly.assertAll();
  }

  /**
   * Fetch health.
   */
  @Test
  void fetchHealth() {
    ResponseEntity<String> response = restTemplate
        .getForEntity("/actuator/health", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  /**
   * Fetch info.
   */
  @Test
  void fetchInfo() {
    ResponseEntity<String> response = restTemplate
        .getForEntity("/actuator/info", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  /**
   * Fetch metrics.
   */
  @Test
  void fetchMetrics() {
    ResponseEntity<String> response = restTemplate
        .withBasicAuth(actuatorUser, actuatorPass)
        .getForEntity("/actuator/metrics", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  /**
   * Fetch metrics and expect unauthorized.
   */
  @Test
  void fetchMetricsAndExpectUnauthorized() {
    ResponseEntity<String> response = restTemplate
        .getForEntity("/actuator/metrics", String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

}
