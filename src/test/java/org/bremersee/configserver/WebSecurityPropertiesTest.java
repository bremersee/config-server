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

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
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
    String value = "hasIpAddress('127.0.0.1/32')";
    WebSecurityProperties actual = new WebSecurityProperties();
    actual.setApplicationAccess(value);
    assertEquals(value, actual.getApplicationAccess());
    assertEquals(actual, actual);
    assertNotEquals(actual, null);
    assertNotEquals(actual, new Object());
    WebSecurityProperties expected = new WebSecurityProperties();
    expected.setApplicationAccess(value);
    assertEquals(expected, actual);
    assertTrue(actual.toString().contains(value));
  }

  /**
   * Sets actuator access.
   */
  @Test
  void setActuatorAccess() {
    String value = "hasRole('ACTUATOR')";
    WebSecurityProperties actual = new WebSecurityProperties();
    actual.setActuatorAccess(value);
    assertEquals(value, actual.getActuatorAccess());
    assertEquals(actual, actual);
    WebSecurityProperties expected = new WebSecurityProperties();
    expected.setActuatorAccess(value);
    assertEquals(expected, actual);
    assertTrue(actual.toString().contains(value));
  }

  /**
   * Sets client user name.
   */
  @Test
  void setClientUserName() {
    String value = "qwertz";
    WebSecurityProperties actual = new WebSecurityProperties();
    actual.setClientUserName(value);
    assertEquals(value, actual.getClientUserName());
    assertEquals(actual, actual);
    WebSecurityProperties expected = new WebSecurityProperties();
    expected.setClientUserName(value);
    assertEquals(expected, actual);
    assertTrue(actual.toString().contains(value));
  }

  /**
   * Sets client user password.
   */
  @Test
  void setClientUserPassword() {
    String value = "qwertz";
    WebSecurityProperties actual = new WebSecurityProperties();
    actual.setClientUserPassword(value);
    assertEquals(value, actual.getClientUserPassword());
    assertEquals(actual, actual);
    WebSecurityProperties expected = new WebSecurityProperties();
    expected.setClientUserPassword(value);
    assertEquals(expected, actual);
    assertFalse(actual.toString().contains(value));
  }

  /**
   * Sets actuator user name.
   */
  @Test
  void setActuatorUserName() {
    String value = "qwertz";
    WebSecurityProperties actual = new WebSecurityProperties();
    actual.setActuatorUserName(value);
    assertEquals(value, actual.getActuatorUserName());
    assertEquals(actual, actual);
    WebSecurityProperties expected = new WebSecurityProperties();
    expected.setActuatorUserName(value);
    assertEquals(expected, actual);
    assertTrue(actual.toString().contains(value));
  }

  /**
   * Sets actuator user password.
   */
  @Test
  void setActuatorUserPassword() {
    String value = "qwertz";
    WebSecurityProperties actual = new WebSecurityProperties();
    actual.setActuatorUserPassword(value);
    assertEquals(value, actual.getActuatorUserPassword());
    assertEquals(actual, actual);
    WebSecurityProperties expected = new WebSecurityProperties();
    expected.setActuatorUserPassword(value);
    assertEquals(expected, actual);
    assertFalse(actual.toString().contains(value));
  }

  /**
   * Sets admin user name.
   */
  @Test
  void setAdminUserName() {
    String value = "qwertz";
    WebSecurityProperties actual = new WebSecurityProperties();
    actual.setAdminUserName(value);
    assertEquals(value, actual.getAdminUserName());
    assertEquals(actual, actual);
    WebSecurityProperties expected = new WebSecurityProperties();
    expected.setAdminUserName(value);
    assertEquals(expected, actual);
    assertTrue(actual.toString().contains(value));
  }

  /**
   * Sets admin user password.
   */
  @Test
  void setAdminUserPassword() {
    String value = "qwertz";
    WebSecurityProperties actual = new WebSecurityProperties();
    actual.setAdminUserPassword(value);
    assertEquals(value, actual.getAdminUserPassword());
    assertEquals(actual, actual);
    WebSecurityProperties expected = new WebSecurityProperties();
    expected.setAdminUserPassword(value);
    assertEquals(expected, actual);
    assertFalse(actual.toString().contains(value));
  }

  /**
   * Build application access.
   */
  @Test
  void buildApplicationAccess() {
    String value = "hasIpAddress('127.0.0.1/32')";
    WebSecurityProperties actual = new WebSecurityProperties();
    actual.setApplicationAccess(value);
    assertEquals(
        "hasIpAddress('127.0.0.1/32') or hasAuthority('ROLE_CONFIG_CLIENT')",
        actual.buildApplicationAccess());
  }

  /**
   * Build actuator access.
   */
  @Test
  void buildActuatorAccess() {
    String value = "hasIpAddress('127.0.0.1/32')";
    WebSecurityProperties actual = new WebSecurityProperties();
    actual.setActuatorAccess(value);
    assertEquals(
        "hasIpAddress('127.0.0.1/32') or hasAuthority('ROLE_ACTUATOR')",
        actual.buildActuatorAccess());
  }

  /**
   * Build users.
   */
  @Test
  void buildUsers() {
    WebSecurityProperties actual = new WebSecurityProperties();
    actual.setClientUserName("qwertz");
    actual.setClientUserPassword("pass1");
    List<SimpleUser> users = actual.buildUsers();
    assertNotNull(users);
    assertEquals(1, users.size());
    SimpleUser user = users.get(0);
    assertEquals(user, user);
    assertNotEquals(user, null);
    assertNotEquals(user, new Object());
    assertTrue(user.toString().contains("qwertz"));
    assertFalse(user.toString().contains("pass1"));
    assertEquals("qwertz", user.getName());
    assertEquals("pass1", user.getPassword());
    List<String> authorities = user.getAuthorities();
    assertNotNull(authorities);
    assertEquals(1, authorities.size());
    assertEquals("ROLE_CONFIG_CLIENT", authorities.get(0));

    actual.setActuatorUserName("actuator");
    actual.setActuatorUserPassword("pass2");
    users = actual.buildUsers();
    assertNotNull(users);
    assertEquals(2, users.size());
    Optional<SimpleUser> optUser = users.stream()
        .filter(u -> "actuator".equals(u.getName()))
        .findAny();
    assertTrue(optUser.isPresent());
    user = optUser.get();
    assertEquals("actuator", user.getName());
    assertEquals("pass2", user.getPassword());
    authorities = user.getAuthorities();
    assertNotNull(authorities);
    assertEquals(1, authorities.size());
    assertEquals("ROLE_ACTUATOR", authorities.get(0));

    actual.setAdminUserName("admin");
    actual.setAdminUserPassword("pass3");
    users = actual.buildUsers();
    assertNotNull(users);
    assertEquals(3, users.size());
    optUser = users.stream()
        .filter(u -> "admin".equals(u.getName()))
        .findAny();
    assertTrue(optUser.isPresent());
    user = optUser.get();
    assertEquals("admin", user.getName());
    assertEquals("pass3", user.getPassword());
    authorities = user.getAuthorities();
    assertNotNull(authorities);
    assertEquals(2, authorities.size());
    assertTrue(authorities.contains("ROLE_ACTUATOR"));
    assertTrue(authorities.contains("ROLE_CONFIG_CLIENT"));
  }

}