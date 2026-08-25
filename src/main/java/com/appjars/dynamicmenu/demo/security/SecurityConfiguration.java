/*-
 * #%L
 * Dynamic Menu AppJars - Demo
 * %%
 * Copyright (C) 2023 - 2026 AppJars
 * %%
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
 * #L%
 */
package com.appjars.dynamicmenu.demo.security;

import static com.vaadin.flow.spring.security.VaadinSecurityConfigurer.vaadin;

import com.appjars.dynamicmenu.demo.views.LoginView;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * Security of the demo. It exists because the appjar filters the menu with
 * {@code HttpServletRequest.isUserInRole}, which needs a real authenticated request.
 */
@EnableWebSecurity
@Configuration
public class SecurityConfiguration {

  public static final String ADMIN_ROLE = "ADMIN";
  public static final String MANAGER_ROLE = "MANAGER";
  public static final String USER_ROLE = "USER";

  /** Where a logout lands: the public landing page. */
  public static final String LOGOUT_URL = "/";

  /** The demo accounts, in the order the login screen offers them. */
  public static final List<DemoAccount> ACCOUNTS = List.of(
      new DemoAccount("admin", ADMIN_ROLE),
      new DemoAccount("manager", MANAGER_ROLE),
      new DemoAccount("user", USER_ROLE));

  /** One of the sample accounts. */
  public record DemoAccount(String username, String role) {
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    // Static assets of the anonymous landing page: VaadinSecurityConfigurer does not permit them.
    http.authorizeHttpRequests(auth -> auth
        .requestMatchers(HttpMethod.GET, "/*.png", "/*.css", "/icons/**", "/images/**").permitAll());

    // H2 console, enabled in application.properties so the menu table can be inspected
    http.authorizeHttpRequests(auth -> auth.requestMatchers("/h2-console/**").permitAll())
        .headers(headers -> headers.frameOptions(FrameOptionsConfig::disable))
        .csrf(csrf -> csrf.ignoringRequestMatchers(
            PathPatternRequestMatcher.withDefaults().matcher("/h2-console/**")));

    http.with(vaadin(), configurer -> configurer.loginView(LoginView.class, LOGOUT_URL));

    return http.build();
  }

  /** The sample accounts, in memory, each with its password equal to the username. */
  @Bean
  public UserDetailsService users() {
    return new InMemoryUserDetailsManager(ACCOUNTS.stream()
        .map(account -> User.builder().username(account.username())
            .password("{noop}" + account.username()).roles(account.role()).build())
        .toList());
  }
}
