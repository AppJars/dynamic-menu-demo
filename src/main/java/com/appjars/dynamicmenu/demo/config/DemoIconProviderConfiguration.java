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

import com.appjars.dynamicmenu.demo.icons.CustomIcons;
import com.appjars.dynamicmenu.flow.icon.IconFamily;
import com.appjars.dynamicmenu.flow.icon.IconFamilyProvider;
import com.flowingcode.vaadin.addons.fontawesome.FontAwesome;
import com.vaadin.flow.component.icon.VaadinIcon;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/** Overrides the default {@link IconFamilyProvider} to offer FontAwesome and a custom set too. */
@Configuration
public class DemoIconProviderConfiguration {

  @Bean
  @Primary
  public IconFamilyProvider demoIconFamilyProvider() {
    return () ->
        List.of(
            new IconFamily(VaadinIcon.class, "Vaadin Icons"),
            new IconFamily(FontAwesome.Solid.class, "FontAwesome Solid"),
            new IconFamily(FontAwesome.Regular.class, "FontAwesome Regular"),
            new IconFamily(FontAwesome.Brands.class, "FontAwesome Brands"),
            new IconFamily(CustomIcons.class, "Custom Icons"));
  }
}
