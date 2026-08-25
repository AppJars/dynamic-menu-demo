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
import com.appjars.dynamicmenu.demo.security.SecurityConfiguration;
import com.appjars.dynamicmenu.flow.view.util.RouteConfigurer;
import com.appjars.dynamicmenu.model.MenuItemDto;
import com.appjars.dynamicmenu.model.SecurityConstraintDto;
import com.appjars.dynamicmenu.service.MenuItemService;
import com.vaadin.flow.component.icon.VaadinIcon;
import java.util.Arrays;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Seeds a sample menu of four entries, one per way of deciding who sees it, leaving a slot free
 * under the five-item limit of the free version. Runs only when the menu is empty: delete
 * {@code ./data} to seed it again.
 */
@Component
public class DemoDataInitializer {

  private static final Logger logger = LoggerFactory.getLogger(DemoDataInitializer.class);

  private static final String VAADIN_ICONS = VaadinIcon.class.getName();
  private static final String CUSTOM_ICONS = CustomIcons.class.getName();

  private final MenuItemService menuItemService;

  /** The route the appjar mounted its editor at, which is configurable. */
  private final String editorUrl;

  public DemoDataInitializer(MenuItemService menuItemService,
      @Value(RouteConfigurer.URL_MENU_ITEMS) String editorUrl) {
    this.menuItemService = menuItemService;
    this.editorUrl = editorUrl;
  }

  @EventListener(ApplicationReadyEvent.class)
  public void seedSampleMenu() {
    if (menuItemService.countAll() > 0) {
      logger.debug("The menu already has items, skipping the sample data");
      return;
    }

    MenuItemDto docs = item("Documentation", CUSTOM_ICONS, CustomIcons.ROCKET.name());
    docs.setTooltip("An external link, with no restrictions: everybody sees it, even a visitor who "
        + "is not signed in. It also uses the icon set defined by this application");
    docs.setExternalUrl("https://docs.appjars.com");
    save(docs);

    MenuItemDto section = item("Administration", VAADIN_ICONS, VaadinIcon.FOLDER_OPEN.name());
    section.setTooltip("A parent item: it groups children instead of navigating anywhere, and it "
        + "disappears when none of them is visible");
    section.setExpandedByDefault(true);
    MenuItemDto savedSection = save(section);

    MenuItemDto editor = item("Menu editor", VAADIN_ICONS, VaadinIcon.LIST.name());
    editor.setTooltip("Follows the access rules of its own target: the editor is @PermitAll, so this "
        + "entry shows up as soon as you sign in, whatever your role");
    editor.setInternalUrl(editorUrl);
    editor.setOnlyRenderIfAllowed(true);
    editor.setParent(savedSection);
    save(editor);

    MenuItemDto audit = item("Audit log", VAADIN_ICONS, VaadinIcon.RECORDS.name());
    audit.setTooltip("Carries a role requirement of its own: only a user granted ADMIN sees it");
    audit.setInternalUrl("audit");
    audit.setPermissions(requiring(SecurityConfiguration.ADMIN_ROLE));
    audit.setParent(savedSection);
    save(audit);

    logger.info("Sample menu created with {} items", menuItemService.countAll());
  }

  /** A constraint demanding every one of {@code roles}. All three sets must be non-null. */
  private SecurityConstraintDto requiring(String... roles) {
    SecurityConstraintDto constraint = new SecurityConstraintDto();
    constraint.setIfAllGranted(new HashSet<>(Arrays.asList(roles)));
    constraint.setIfAnyGranted(new HashSet<>());
    constraint.setIfNotGranted(new HashSet<>());
    return constraint;
  }

  private MenuItemDto item(String label, String iconType, String icon) {
    MenuItemDto item = new MenuItemDto();
    item.setLabel(label);
    item.setIconType(iconType);
    item.setIcon(icon);
    return item;
  }

  /** Saves the item and returns it reloaded, ready to be used as a parent. */
  private MenuItemDto save(MenuItemDto item) {
    Integer id = menuItemService.save(item);
    item.setId(id);
    return menuItemService.findById(id).orElse(item);
  }
}
