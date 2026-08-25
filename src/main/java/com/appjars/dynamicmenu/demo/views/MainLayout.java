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

import com.appjars.dynamicmenu.demo.components.MenuItemFilter;
import com.appjars.dynamicmenu.demo.views.tour.DemoTours;
import com.appjars.dynamicmenu.demo.views.tour.DemoTours.DemoTour;
import com.appjars.dynamicmenu.flow.service.DynamicMenuItemProvider;
import com.appjars.dynamicmenu.flow.view.ListMenuItemsView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Footer;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Header;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoIcon;
import com.vaadin.flow.theme.lumo.LumoUtility;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetails;

/** The main view is a top-level placeholder for other views. */
@AnonymousAllowed
@SuppressWarnings("serial")
@JsModule("./icons/custom-icons.js")
public class MainLayout extends AppLayout implements AfterNavigationObserver {

  private final AuthenticationContext authenticationContext;

  private H2 viewTitle;

  /** Enabled only on views that have a tour of their own. */
  private MenuItem thisPageTour;

  /** One entry per tour, enabled only while the current user can take it. */
  private final Map<DemoTour, MenuItem> tourItems = new EnumMap<>(DemoTour.class);

  private MenuBar tourMenu;
  private MenuItem tourButton;

  /** Says, inside the menu, why some entries are greyed out. */
  private MenuItem tourHint;

  public MainLayout(AuthenticationContext authenticationContext) {
    this.authenticationContext = authenticationContext;

    setPrimarySection(Section.DRAWER);
    addDrawerContent();
    addHeaderContent();
  }

  private void addHeaderContent() {
    DrawerToggle toggle = new DrawerToggle();
    toggle.setAriaLabel(getTranslation("appjars.dynamicmenu.demo.layout.menutoggle"));

    viewTitle = new H2();
    viewTitle.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE);

    addToNavbar(toggle, viewTitle, createTourMenu());
  }

  /** The tour menu of the navbar, available from every view. */
  private MenuBar createTourMenu() {
    MenuBar menu = new MenuBar();
    menu.setId(DemoTours.ID_TOUR_MENU);
    menu.addClassName("demo-tour-menu");
    menu.addThemeVariants(MenuBarVariant.LUMO_PRIMARY);
    menu.setOpenOnHover(true);
    tourButton = menu.addItem(new Div(VaadinIcon.MAP_MARKER.create(),
        new Span(getTranslation("appjars.dynamicmenu.demo.home.tour.button"))));
    SubMenu tours = tourButton.getSubMenu();
    thisPageTour = tours.addItem(
        entry(VaadinIcon.CROSSHAIRS, "appjars.dynamicmenu.demo.home.tour.thispage"),
        e -> startCurrentTour());
    tours.addSeparator();
    for (DemoTour tour : DemoTour.values()) {
      MenuItem item = tours.addItem(entry(DemoTours.icon(tour),
          "appjars.dynamicmenu.demo.home.tour." + tourKey(tour)), e -> startTour(tour));
      tourItems.put(tour, item);
    }
    tours.addSeparator();
    tourHint = tours.addItem(getTranslation("appjars.dynamicmenu.demo.home.tour.unavailable.hint"));
    tourHint.setEnabled(false);
    this.tourMenu = menu;
    return menu;
  }

  /** One entry of the tour menu. */
  private Component entry(VaadinIcon icon, String key) {
    return DemoTours.menuEntry(icon, getTranslation(key));
  }

  /**
   * Disables the tours the current user cannot take. The reason goes on the button and in the menu:
   * a disabled item takes no pointer events, so a tooltip on it would never open.
   */
  private void refreshTourAvailability() {
    thisPageTour.setEnabled(currentTour() != null);
    boolean anyUnavailable = false;
    for (Map.Entry<DemoTour, MenuItem> entry : tourItems.entrySet()) {
      boolean available = DemoTours.isAvailable(entry.getKey());
      entry.getValue().setEnabled(available);
      anyUnavailable |= !available;
    }
    tourHint.setVisible(anyUnavailable);
    tourMenu.setTooltipText(tourButton,
        anyUnavailable ? getTranslation("appjars.dynamicmenu.demo.home.tour.unavailable") : null);
  }

  private void addDrawerContent() {
    Image logo = new Image("icons/icon.png", null);
    logo.setHeight("2.5rem");
    logo.setWidth("2.5rem");

    H1 appName = new H1(getTranslation("appjars.dynamicmenu.demo.layout.drawertitle"));
    appName.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.Margin.NONE,
        LumoUtility.Flex.GROW);
    Header header = new Header(logo, appName);
    header.addClassNames(LumoUtility.Display.FLEX, LumoUtility.Gap.SMALL,
        LumoUtility.AlignItems.CENTER, LumoUtility.Padding.SMALL);
    header.setId(DemoTours.ID_DRAWER_HEADER);

    MenuItemFilter menuitemfilter = new MenuItemFilter(createNavigation());
    menuitemfilter.setId(DemoTours.ID_MENU_FILTER);

    Scroller scroller = new Scroller(menuitemfilter.getNav());
    scroller.addClassName(LumoUtility.Flex.GROW);
    addToDrawer(header, menuitemfilter, scroller, createUserFooter());
  }

  /** Who the menu was built for, and where the account is switched. */
  private Footer createUserFooter() {
    Footer footer = new Footer();
    footer.addClassName("demo-user-footer");

    Optional<UserDetails> user = authenticationContext.getAuthenticatedUser(UserDetails.class);
    if (user.isEmpty()) {
      Anchor login = new Anchor("login", getTranslation("appjars.dynamicmenu.demo.layout.signin"));
      footer.add(new Div(VaadinIcon.SIGN_IN.create(), login));
      return footer;
    }

    String username = user.get().getUsername();
    Span name = new Span(username);
    name.addClassNames(LumoUtility.Flex.GROW, LumoUtility.FontSize.SMALL);
    Span role = new Span(String.join(", ", authenticationContext.getGrantedRoles()));
    role.addClassName("demo-user-role");

    // A context menu, not a MenuBar: the drawer is too narrow and would collapse it into overflow
    Div current = new Div(new Avatar(username), name, role, LumoIcon.DROPDOWN.create());
    current.setId(DemoTours.ID_USER_MENU);
    current.addClassName("demo-user-menu");

    ContextMenu actions = new ContextMenu(current);
    actions.setOpenOnClick(true);
    actions.addItem(getTranslation("appjars.dynamicmenu.demo.layout.switchuser"),
        e -> getUI().ifPresent(ui -> ui.navigate(LoginView.class)));
    actions.addItem(getTranslation("appjars.dynamicmenu.demo.layout.signout"),
        e -> authenticationContext.logout());

    footer.add(current);
    return footer;
  }

  private SideNav createNavigation() {
    SideNav nav = new SideNav();
    nav.setId(DemoTours.ID_SIDE_NAV);

    SideNavItem home =
        new SideNavItem(getTranslation("appjars.dynamicmenu.demo.menuitem.home"), HomeView.class);
    home.setPrefixComponent(VaadinIcon.HOME.create());
    nav.addItem(home);

    SideNavItem[] menuItems = DynamicMenuItemProvider.getInstance().getMenuItems();

    for (SideNavItem item : menuItems) {
      nav.addItem(item);
    }
    return nav;
  }

  @Override
  public void afterNavigation(AfterNavigationEvent event) {
    viewTitle.setText(getCurrentPageTitle());
    refreshTourAvailability();
    startPendingTour();
  }

  /** Starts the tour stashed in the session, once its view has been rendered. */
  private void startPendingTour() {
    VaadinSession session = VaadinSession.getCurrent();
    if (session != null
        && session.getAttribute(DemoTours.PENDING_TOUR_ATTRIBUTE) instanceof DemoTour pending
        && showing(tourView(pending))) {
      session.setAttribute(DemoTours.PENDING_TOUR_ATTRIBUTE, null);
      runTour(pending);
    }
  }

  /** Runs the tour if its view is showing, otherwise stashes it and navigates there. */
  private void startTour(DemoTour tour) {
    Class<? extends Component> target = tourView(tour);
    if (target == null || showing(target)) {
      runTour(tour);
    } else {
      VaadinSession.getCurrent().setAttribute(DemoTours.PENDING_TOUR_ATTRIBUTE, tour);
      getUI().ifPresent(ui -> ui.navigate(target));
    }
  }

  /** The tour of the view currently shown, if it has one. */
  private void startCurrentTour() {
    DemoTour tour = currentTour();
    if (tour != null) {
      runTour(tour);
    }
  }

  /** The tour of the view being shown, or {@code null}. */
  private DemoTour currentTour() {
    for (DemoTour tour : DemoTour.values()) {
      Class<? extends Component> view = tourView(tour);
      if (view != null && showing(view)) {
        return tour;
      }
    }
    return null;
  }

  private boolean showing(Class<? extends Component> view) {
    return getContent() != null && view.equals(getContent().getClass());
  }

  private void runTour(DemoTour tour) {
    if (DemoTours.touchesAnOverlay(tour) && getContent() instanceof ListMenuItemsView listView) {
      // antler-tour has no per-step hooks, so the dialog stays open for the whole tour
      Runnable open = tour == DemoTour.ITEM_PERMISSIONS ? listView::openItemPermissions
          : listView::openItemDialog;
      DemoTours.start(tour, this, this::getTranslation, open, listView::closeItemDialog);
    } else {
      DemoTours.start(tour, this, this::getTranslation);
    }
  }

  private Class<? extends Component> tourView(DemoTour tour) {
    return DemoTours.view(tour);
  }

  private String tourKey(DemoTour tour) {
    return DemoTours.labelKey(tour);
  }

  private String getCurrentPageTitle() {
    if (getContent() instanceof HasDynamicTitle dynamicTitle) {
      return dynamicTitle.getPageTitle();
    }
    PageTitle title = getContent().getClass().getAnnotation(PageTitle.class);
    return title == null ? "" : title.value();
  }
}
