/*
 * Copyright 2020 the original author or authors.
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

import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.bremersee.configserver.WebSecurityProperties.SimpleUser;
import org.junit.jupiter.api.Test;

/**
 * The web security properties test.
 *
 * @author Christian Bremer
 */
class WebSecurityPropertiesTest {

  /**
   * Sets application access.
   */
  @Test
  void setApplicationAccess() {
    SoftAssertions softly = new SoftAssertions();

    String value = "hasIpAddress('127.0.0.1/32')";
    WebSecurityProperties actual = new WebSecurityProperties();
    actual.setApplicationAccess(value);
    softly.assertThat(actual.getApplicationAccess()).isEqualTo(value);

    WebSecurityProperties expected = new WebSecurityProperties();
    expected.setApplicationAccess(value);
    softly.assertThat(actual).isEqualTo(expected);
    softly.assertThat(actual.toString()).contains(value);

    softly.assertAll();
  }

  /**
   * Sets actuator access.
   */
  @Test
  void setActuatorAccess() {
    SoftAssertions softly = new SoftAssertions();

    String value = "hasRole('ACTUATOR')";
    WebSecurityProperties actual = new WebSecurityProperties();
    actual.setActuatorAccess(value);
    softly.assertThat(actual.getActuatorAccess()).isEqualTo(value);

    WebSecurityProperties expected = new WebSecurityProperties();
    expected.setActuatorAccess(value);
    softly.assertThat(actual).isEqualTo(expected);
    softly.assertThat(actual.toString()).contains(value);

    softly.assertAll();
  }

  /**
   * Sets client user name.
   */
  @Test
  void setClientUserName() {
    SoftAssertions softly = new SoftAssertions();

    String value = "qwertz";
    WebSecurityProperties actual = new WebSecurityProperties();
    actual.setClientUserName(value);
    softly.assertThat(actual.getClientUserName()).isEqualTo(value);

    WebSecurityProperties expected = new WebSecurityProperties();
    expected.setClientUserName(value);
    softly.assertThat(actual).isEqualTo(expected);
    softly.assertThat(actual.toString()).contains(value);

    softly.assertAll();
  }

  /**
   * Sets client user password.
   */
  @Test
  void setClientUserPassword() {
    SoftAssertions softly = new SoftAssertions();

    String value = "qwertz";
    WebSecurityProperties actual = new WebSecurityProperties();
    actual.setClientUserPassword(value);
    softly.assertThat(actual.getClientUserPassword()).isEqualTo(value);

    WebSecurityProperties expected = new WebSecurityProperties();
    expected.setClientUserPassword(value);
    softly.assertThat(actual).isEqualTo(expected);
    softly.assertThat(actual.toString()).doesNotContain(value);

    softly.assertAll();
  }

  /**
   * Sets actuator user name.
   */
  @Test
  void setActuatorUserName() {
    SoftAssertions softly = new SoftAssertions();

    String value = "qwertz";
    WebSecurityProperties actual = new WebSecurityProperties();
    actual.setActuatorUserName(value);
    softly.assertThat(actual.getActuatorUserName()).isEqualTo(value);

    WebSecurityProperties expected = new WebSecurityProperties();
    expected.setActuatorUserName(value);
    softly.assertThat(actual).isEqualTo(expected);
    softly.assertThat(actual.toString()).contains(value);

    softly.assertAll();
  }

  /**
   * Sets actuator user password.
   */
  @Test
  void setActuatorUserPassword() {
    SoftAssertions softly = new SoftAssertions();

    String value = "qwertz";
    WebSecurityProperties actual = new WebSecurityProperties();
    actual.setActuatorUserPassword(value);
    softly.assertThat(actual.getActuatorUserPassword()).isEqualTo(value);

    WebSecurityProperties expected = new WebSecurityProperties();
    expected.setActuatorUserPassword(value);
    softly.assertThat(actual).isEqualTo(expected);
    softly.assertThat(actual.toString()).doesNotContain(value);

    softly.assertAll();
  }

  /**
   * Sets admin user name.
   */
  @Test
  void setAdminUserName() {
    SoftAssertions softly = new SoftAssertions();

    String value = "qwertz";
    WebSecurityProperties actual = new WebSecurityProperties();
    actual.setAdminUserName(value);
    softly.assertThat(actual.getAdminUserName()).isEqualTo(value);

    WebSecurityProperties expected = new WebSecurityProperties();
    expected.setAdminUserName(value);
    softly.assertThat(actual).isEqualTo(expected);
    softly.assertThat(actual.toString()).contains(value);

    softly.assertAll();
  }

  /**
   * Sets admin user password.
   */
  @Test
  void setAdminUserPassword() {
    SoftAssertions softly = new SoftAssertions();

    String value = "qwertz";
    WebSecurityProperties actual = new WebSecurityProperties();
    actual.setAdminUserPassword(value);
    softly.assertThat(actual.getAdminUserPassword()).isEqualTo(value);

    WebSecurityProperties expected = new WebSecurityProperties();
    expected.setAdminUserPassword(value);
    softly.assertThat(actual).isEqualTo(expected);
    softly.assertThat(actual.toString()).doesNotContain(value);

    softly.assertAll();
  }

  /**
   * Build application access.
   */
  @Test
  void buildApplicationAccess() {
    String value = "hasIpAddress('127.0.0.1/32')";
    WebSecurityProperties actual = new WebSecurityProperties();
    actual.setApplicationAccess(value);
    assertThat(actual.buildApplicationAccess())
        .isEqualTo(String.format(
            "hasIpAddress('127.0.0.1/32') or hasAuthority('%s')",
            WebSecurityProperties.ROLE_APPLICATION));
  }

  /**
   * Build actuator access.
   */
  @Test
  void buildActuatorAccess() {
    String value = "hasIpAddress('127.0.0.1/32')";
    WebSecurityProperties actual = new WebSecurityProperties();
    actual.setActuatorAccess(value);
    assertThat(actual.buildActuatorAccess())
        .isEqualTo(String.format(
            "hasIpAddress('127.0.0.1/32') or hasAuthority('%s')",
            WebSecurityProperties.ROLE_ACTUATOR));
  }

  /**
   * Build users.
   */
  @Test
  void buildUsers() {
    SoftAssertions softly = new SoftAssertions();

    SimpleUser clientUser = new SimpleUser(
        "qwertz",
        "pass1",
        WebSecurityProperties.ROLE_APPLICATION );
    WebSecurityProperties actual = new WebSecurityProperties();
    actual.setClientUserName(clientUser.getName());
    actual.setClientUserPassword(clientUser.getPassword());
    List<SimpleUser> users = actual.buildUsers();
    softly.assertThat(users).isNotNull();
    softly.assertThat(users)
        .containsExactly(clientUser);

    SimpleUser user = users.get(0);
    softly.assertThat(user).isNotNull();
    softly.assertThat(user.getName()).isEqualTo(clientUser.getName());
    softly.assertThat(user.getPassword()).isEqualTo(clientUser.getPassword());
    softly.assertThat(user.toString()).contains(clientUser.getName());
    softly.assertThat(user.toString()).doesNotContain(clientUser.getPassword());

    List<String> authorities = user.getAuthorities();
    softly.assertThat(authorities).containsExactly(WebSecurityProperties.ROLE_APPLICATION);

    SimpleUser actuatorUser = new SimpleUser(
        "actuator",
        "pass2",
        WebSecurityProperties.ROLE_ACTUATOR);
    actual.setActuatorUserName(actuatorUser.getName());
    actual.setActuatorUserPassword(actuatorUser.getPassword());
    users = actual.buildUsers();
    softly.assertThat(users).containsExactlyInAnyOrder(clientUser, actuatorUser);

    SimpleUser adminUser = new SimpleUser(
        "admin",
        "pass3",
        WebSecurityProperties.ROLE_APPLICATION, WebSecurityProperties.ROLE_ACTUATOR);
    actual.setAdminUserName(adminUser.getName());
    actual.setAdminUserPassword(adminUser.getPassword());
    users = actual.buildUsers();
    softly.assertThat(users).containsExactlyInAnyOrder(clientUser, actuatorUser, adminUser);

    softly.assertAll();
  }

}