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
package com.appjars.dynamicmenu.demo.config;

import com.appjars.dynamicmenu.demo.security.SecurityConfiguration;
import com.appjars.dynamicmenu.demo.security.SecurityConfiguration.DemoAccount;
import com.appjars.dynamicmenu.utils.DynamicMenuAuthorityUtils;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Tells the appjar which roles this application has, so the menu item editor offers them. The names
 * carry no {@code ROLE_} prefix: {@code HttpServletRequest.isUserInRole} adds it.
 */
@Configuration
public class DemoAuthorityConfiguration {

  @Bean
  @Primary
  public DynamicMenuAuthorityUtils demoAuthorityUtils() {
    return new DynamicMenuAuthorityUtils() {

      @Override
      public Set<String> getAvailableAuthorities() {
        return SecurityConfiguration.ACCOUNTS.stream().map(DemoAccount::role)
            .collect(Collectors.toCollection(LinkedHashSet::new));
      }

      @Override
      public String getAnonymousAuthority() {
        return "ANONYMOUS";
      }
    };
  }
}
