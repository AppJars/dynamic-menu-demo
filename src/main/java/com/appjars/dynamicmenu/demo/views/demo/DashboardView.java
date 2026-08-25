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
package com.appjars.dynamicmenu.demo.views.demo;

import com.appjars.dynamicmenu.demo.views.MainLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

/** A view any signed-in user may open, whatever their role. */
@SuppressWarnings("serial")
@PermitAll
@Route(value = "dashboard", layout = MainLayout.class)
public class DashboardView extends DemoTargetView {

  public DashboardView() {
    super("dashboard", "dashboard", "@PermitAll");
  }
}
