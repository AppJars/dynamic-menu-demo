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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;

/** Base of the views that exist only to be pointed at from a menu entry. */
@SuppressWarnings("serial")
abstract class DemoTargetView extends VerticalLayout implements HasDynamicTitle {

  private static final String KEY_PREFIX = "appjars.dynamicmenu.demo.target.";

  private final String key;

  /**
   * @param key message key under {@code appjars.dynamicmenu.demo.target.}
   * @param route route template, as the editor lists it
   * @param annotation the Vaadin access annotation this view carries
   */
  protected DemoTargetView(String key, String route, String annotation) {
    this.key = key;
    addClassName("target-view");
    add(new H2(t("title")), new Paragraph(t("desc")),
        facts(fact("route", route), fact("access", annotation), fact("audience", t("audience"))));
  }

  private Component facts(Component... facts) {
    Card card = new Card();
    card.setTitle(getTranslation(KEY_PREFIX + "facts"));
    card.add(facts);
    return card;
  }

  private Component fact(String label, String value) {
    Span name = new Span(getTranslation(KEY_PREFIX + label));
    name.addClassName("target-fact-name");
    Span code = new Span(value);
    code.addClassName("target-fact-value");
    Paragraph fact = new Paragraph(name, code);
    fact.addClassName("target-fact");
    return fact;
  }

  /** Adds a line below the facts. */
  protected void addDetail(Component detail) {
    add(detail);
  }

  protected String t(String suffix) {
    return getTranslation(KEY_PREFIX + key + "." + suffix);
  }

  @Override
  public String getPageTitle() {
    return t("title");
  }
}
