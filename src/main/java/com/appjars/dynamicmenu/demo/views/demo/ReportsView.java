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

import com.appjars.dynamicmenu.demo.security.SecurityConfiguration;
import com.appjars.dynamicmenu.demo.views.MainLayout;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

/** The target view with a route parameter: the editor asks for a {@code period} value. */
@SuppressWarnings("serial")
@RolesAllowed({SecurityConfiguration.MANAGER_ROLE, SecurityConfiguration.ADMIN_ROLE})
@Route(value = "reports/:period?", layout = MainLayout.class)
public class ReportsView extends DemoTargetView implements BeforeEnterObserver {

  private static final String PERIOD = "period";

  private final Paragraph period = new Paragraph();

  public ReportsView() {
    super("reports", "reports/:period?", "@RolesAllowed({\"MANAGER\", \"ADMIN\"})");
    period.addClassName("target-parameter");
    addDetail(period);
  }

  @Override
  public void beforeEnter(BeforeEnterEvent event) {
    period.setText(event.getRouteParameters().get(PERIOD)
        .map(value -> getTranslation("appjars.dynamicmenu.demo.target.reports.period", value))
        .orElseGet(() -> getTranslation("appjars.dynamicmenu.demo.target.reports.noperiod")));
  }
}
