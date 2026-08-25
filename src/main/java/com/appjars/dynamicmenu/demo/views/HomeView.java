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
import com.appjars.dynamicmenu.demo.views.tour.DemoTours;
import com.appjars.dynamicmenu.demo.views.tour.DemoTours.DemoTour;
import com.appjars.dynamicmenu.flow.view.ListMenuItemsView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/** Landing page: the appjar features, how to try it, the license model and the guided tours. */
@SuppressWarnings("serial")
@AnonymousAllowed
@Route(value = "", layout = MainLayout.class)
public class HomeView extends VerticalLayout implements HasDynamicTitle {

  private static final String KEY_PREFIX = "appjars.dynamicmenu.demo.home.";

  private static final String APPJARS_SITE_URL = "https://www.appjars.com";
  private static final String APPJARS_DOCS_URL = "https://docs.appjars.com";
  private static final String GITHUB_ORG_URL = "https://github.com/AppJars";

  public HomeView() {
    addClassName("home-view");
    add(createHero(), createFeaturesSection(), createTryItSection(), createShipsWithSection(),
        createLicenseSection(), createLinksSection());
    setAlignItems(Alignment.STRETCH);
  }

  private Component createHero() {
    Image logo = new Image("icons/icon-appjars-full.png", "AppJars");
    logo.addClassName("home-hero-logo");

    H1 title = new H1(t("hero.title"));
    Paragraph tagline = new Paragraph(t("hero.tagline"));
    tagline.addClassName("home-tagline");

    Div hero = new Div(logo, title, tagline);
    hero.setId("home-hero");
    hero.addClassName("home-hero");
    return hero;
  }

  private Component createFeaturesSection() {
    Div cards = new Div(
        featureCard(VaadinIcon.SITEMAP, "features.multilevel"),
        featureCard(VaadinIcon.EDIT, "features.editor"),
        featureCard(VaadinIcon.KEY, "features.permissions"),
        featureCard(VaadinIcon.PICTURE, "features.icons"),
        featureCard(VaadinIcon.LINK, "features.routes"),
        featureCard(VaadinIcon.DOWNLOAD, "features.importexport"),
        featureCard(VaadinIcon.GLOBE, "features.i18n"));
    cards.addClassName("home-features");

    return section("home-features", t("features.title"), cards);
  }

  private Card featureCard(VaadinIcon icon, String key) {
    Card card = new Card();
    card.addClassName("home-feature-card");
    Icon prefix = icon.create();
    prefix.addClassName("home-feature-icon");
    card.setHeaderPrefix(prefix);
    card.setTitle(t(key + ".title"));
    card.add(new Paragraph(t(key + ".desc")));
    return card;
  }

  private Component createTryItSection() {
    Paragraph intro = new Paragraph(t("tryit.intro"));

    Button editor = new Button(t("tryit.editor"),
        e -> getUI().ifPresent(ui -> ui.navigate(ListMenuItemsView.class)));
    editor.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

    Div actions = new Div(editor, createTourMenu());
    actions.addClassName("home-actions");

    return section("home-tryit", t("tryit.title"), intro, createAccounts(), actions);
  }

  /** The sample accounts, with what each sees in the menu. */
  private Component createAccounts() {
    Div accounts = new Div();
    accounts.addClassName("home-credentials");
    SecurityConfiguration.ACCOUNTS.forEach(account -> {
      Span username = new Span(account.username());
      username.addClassName("home-credential-code");
      Div row = new Div(username, new Span(t("accounts." + account.username())));
      row.addClassName("home-credential");
      accounts.add(row);
    });
    accounts.add(new Paragraph(t("accounts.note")));
    return accounts;
  }

  /** What the demo ships with. */
  private Component createShipsWithSection() {
    UnorderedList items = new UnorderedList();
    items.addClassName("home-shipswith");
    for (String key : new String[] {"accounts", "views", "icons", "menu", "exercise"}) {
      items.add(new ListItem(t("shipswith." + key)));
    }
    return section("home-shipswith", t("shipswith.title"), items);
  }

  private Component createTourMenu() {
    MenuBar menu = new MenuBar();
    menu.addThemeVariants(MenuBarVariant.LUMO_TERTIARY);
    menu.setOpenOnHover(true);
    MenuItem button =
        menu.addItem(new Div(VaadinIcon.MAP_MARKER.create(), new Span(t("tour.button"))));
    SubMenu tours = button.getSubMenu();
    boolean anyUnavailable = false;
    for (DemoTour tour : DemoTour.values()) {
      MenuItem item = tours.addItem(
          DemoTours.menuEntry(DemoTours.icon(tour), t("tour." + DemoTours.labelKey(tour))),
          e -> startTour(tour));
      if (!DemoTours.isAvailable(tour)) {
        item.setEnabled(false);
        anyUnavailable = true;
      }
    }
    // A disabled entry takes no pointer events: the reason goes in the menu and on the button
    if (anyUnavailable) {
      tours.addSeparator();
      MenuItem hint = tours.addItem(t("tour.unavailable.hint"));
      hint.setEnabled(false);
      menu.setTooltipText(button, t("tour.unavailable"));
    }
    return menu;
  }

  /** Shell tours run here; the rest are stashed and started once their view is shown. */
  private void startTour(DemoTour tour) {
    Class<? extends Component> view = DemoTours.view(tour);
    if (view == null) {
      DemoTours.start(tour, this, this::getTranslation);
    } else {
      VaadinSession.getCurrent().setAttribute(DemoTours.PENDING_TOUR_ATTRIBUTE, tour);
      getUI().ifPresent(ui -> ui.navigate(view));
    }
  }

  private Component createLicenseSection() {
    Paragraph desc = new Paragraph(t("license.desc"));
    Anchor link = new Anchor(APPJARS_SITE_URL, t("license.link"));
    link.setTarget("_blank");
    return section("home-license", t("license.title"), desc, new Paragraph(link));
  }

  private Component createLinksSection() {
    Anchor github = new Anchor(GITHUB_ORG_URL, t("links.github"));
    github.setTarget("_blank");
    Anchor docs = new Anchor(APPJARS_DOCS_URL, t("links.docs"));
    docs.setTarget("_blank");
    Div links = new Div(github, docs);
    links.addClassName("home-links");
    return section("home-links", t("links.title"), links);
  }

  private Div section(String id, String title, Component... content) {
    Div section = new Div();
    section.setId(id);
    section.addClassName("home-section");
    section.add(new H3(title));
    section.add(content);
    return section;
  }

  private String t(String key) {
    return getTranslation(KEY_PREFIX + key);
  }

  @Override
  public String getPageTitle() {
    return t("title");
  }
}
