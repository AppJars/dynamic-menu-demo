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
package com.appjars.dynamicmenu.demo.icons;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.IconFactory;

/** Icon family backed by the {@code custom} icon set in {@code frontend/icons/custom-icons.js}. */
public enum CustomIcons implements IconFactory {
  ROCKET("custom:rocket"),
  FLOW("custom:flow");

  private final String icon;

  CustomIcons(String icon) {
    this.icon = icon;
  }

  @Override
  public Icon create() {
    return new Icon(icon);
  }
}
