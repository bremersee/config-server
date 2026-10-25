/*
 * Copyright 2017 the original author or authors.
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

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The web security properties.
 *
 * @author Christian Bremer
 */
@ConfigurationProperties(prefix = "bremersee.config-server.authentication")
@Data
@NoArgsConstructor
@Slf4j
public class WebSecurityProperties {

  /**
   * Required role to access application endpoints.
   */
  public static final String ROLE_APPLICATION = "ROLE_CONFIG_SERVER_ADMIN";

  private String configServerAccessRole = ROLE_APPLICATION;

}
