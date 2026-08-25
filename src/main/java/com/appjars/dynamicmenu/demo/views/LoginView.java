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
package com.appjars.dynamicmenu.demo.views;

import com.appjars.dynamicmenu.demo.security.SecurityConfiguration;
import com.appjars.dynamicmenu.demo.security.SecurityConfiguration.DemoAccount;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.web.csrf.CsrfToken;

/** Account picker: the sample accounts are known, so it submits the credentials for you. */
@SuppressWarnings("serial")
@AnonymousAllowed
@Route(value = "login", layout = MainLayout.class)
public class LoginView extends VerticalLayout implements HasDynamicTitle {

  private static final String KEY_PREFIX = "appjars.dynamicmenu.demo.login.";

  private final ComboBox<DemoAccount> account = new ComboBox<>();

  public LoginView() {
    addClassName("login-view");

    account.setLabel(t("account"));
    account.setItems(SecurityConfiguration.ACCOUNTS);
    account.setItemLabelGenerator(DemoAccount::username);
    account.setRenderer(new ComponentRenderer<>(this::accountCard));
    account.setWidthFull();

    Button login = new Button(t("submit"), e -> loginAs(account.getValue().username()));
    login.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    login.setEnabled(false);
    account.addValueChangeListener(e -> login.setEnabled(e.getValue() != null));

    Div card = new Div(new H3(t("title")), new Paragraph(t("intro")), account, login,
        note(t("passwords")));
    card.addClassName("login-card");

    add(card);
    setAlignItems(Alignment.CENTER);
  }

  private Component accountCard(DemoAccount item) {
    Span username = new Span(item.username());
    username.addClassName("login-account-name");
    Span role = new Span(item.role());
    role.addClassName("login-account-role");
    Div sees = new Div(new Span(t("sees." + item.username())));
    sees.addClassName("login-account-sees");
    Div header = new Div(username, role);
    header.addClassName("login-account-header");
    return new Div(header, sees);
  }

  private Paragraph note(String text) {
    Paragraph note = new Paragraph(text);
    note.addClassName("login-note");
    return note;
  }

  /** Posts the standard Spring Security form login, with the CSRF token, from a hidden form. */
  private void loginAs(String username) {
    CsrfToken csrf = (CsrfToken) VaadinRequest.getCurrent().getAttribute(CsrfToken.class.getName());
    String csrfParam = csrf != null ? csrf.getParameterName() : "_csrf";
    String csrfToken = csrf != null ? csrf.getToken() : "";
    getUI().ifPresent(ui -> ui.getPage().executeJs(
        """
        const f = document.createElement('form');
        f.method = 'POST';
        f.action = 'login';
        const add = (n, v) => {
          const i = document.createElement('input');
          i.type = 'hidden';
          i.name = n;
          i.value = v;
          f.appendChild(i);
        };
        add('username', $0);
        add('password', $0);
        if ($1) { add($1, $2); }
        document.body.appendChild(f);
        f.submit();
        """,
        username, csrfParam, csrfToken));
  }

  private String t(String key) {
    return getTranslation(KEY_PREFIX + key);
  }

  @Override
  public String getPageTitle() {
    return t("title");
  }
}
