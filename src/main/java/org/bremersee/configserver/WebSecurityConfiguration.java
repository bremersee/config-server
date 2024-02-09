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

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Spring boot web security configuration.
 * <ul>
 * <li>Disables CSRF
 * <li>Enables Basic Authentication for users configured with {@link WebSecurityProperties}
 * <li>Enables free access based on IP addresses configured with {@link WebSecurityProperties}
 * </ul>
 *
 * @author Christian Bremer
 */
@Configuration
@EnableConfigurationProperties(WebSecurityProperties.class)
@EnableWebSecurity
@Slf4j
public class WebSecurityConfiguration {

  private final Environment env;

  private final WebSecurityProperties properties;

  /**
   * Instantiates a new web security configuration.
   *
   * @param env the environment
   * @param properties the security properties
   */
  public WebSecurityConfiguration(Environment env, WebSecurityProperties properties) {
    this.env = env;
    this.properties = properties;
  }

  /**
   * Configures the security filter chain.
   *
   * @param http the http security
   * @return the security filter chain
   * @throws Exception if something goes wrong
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    final String appName = env.getProperty("spring.application.name", "config-server");
    return http
        .authorizeHttpRequests(requests -> requests
            .requestMatchers(
                EndpointRequest.to(InfoEndpoint.class),
                EndpointRequest.to(HealthEndpoint.class))
            .permitAll()
            .requestMatchers(EndpointRequest.toAnyEndpoint())
            .access(new WebExpressionAuthorizationManager(properties.buildActuatorAccess()))
            .requestMatchers(new AntPathRequestMatcher("/**"))
            .access(new WebExpressionAuthorizationManager(properties.buildApplicationAccess())))
        .csrf(AbstractHttpConfigurer::disable)
        .httpBasic(configurer -> configurer.realmName(appName))
        .build();
  }

  /**
   * Creates an in-memory user details service based on the users configured in
   * {@link WebSecurityProperties}.
   *
   * @return the user details service for the basic authentication
   */
  @Bean
  public UserDetailsService userDetailsService() {

    log.info("Building user details service with {}", properties);
    final PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    final UserDetails[] userDetails = properties.buildUsers().stream().map(
            simpleUser -> User.builder()
                .username(simpleUser.getName())
                .password(simpleUser.getPassword())
                .authorities(
                    simpleUser.getAuthorities().toArray(new String[0]))
                .passwordEncoder(encoder::encode)
                .build())
        .toArray(UserDetails[]::new);
    return new InMemoryUserDetailsManager(userDetails);
  }
}
