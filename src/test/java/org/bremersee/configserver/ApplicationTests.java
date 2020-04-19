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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    "bremersee.access.admin-user-password=pass4admin"
})
public class ApplicationTests {

  private static final String user = "testadmin";

  private static final String pass = "pass4admin";

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
    String expected = "encrypt_me_i_am_a_secret";
    ResponseEntity<String> response = restTemplate
        .withBasicAuth(user, pass)
        .postForEntity("/encrypt", expected, String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    String encrypted = response.getBody();

    response = restTemplate
        .withBasicAuth(user, pass)
        .postForEntity("/decrypt", encrypted, String.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(expected, response.getBody());
  }

}
