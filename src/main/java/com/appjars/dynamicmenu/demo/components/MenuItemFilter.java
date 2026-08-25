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
package com.appjars.dynamicmenu.demo.components;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.theme.lumo.LumoUtility;
import java.util.Locale;

/** Search field that hides the {@link SideNav} entries not matching what is typed. */
@SuppressWarnings("serial")
public class MenuItemFilter extends TextField {

  private final SideNav allItemsNav;

  public MenuItemFilter(SideNav allItemsNav) {
    this.allItemsNav = allItemsNav;
    addClassNames(LumoUtility.Padding.SMALL);
    setPlaceholder(getTranslation("appjars.dynamicmenu.menuItemFilter.search"));
    setSuffixComponent(new Icon(VaadinIcon.SEARCH));

    setValueChangeMode(ValueChangeMode.LAZY);
    addValueChangeListener(e -> filterItems(e.getValue().toLowerCase(Locale.ROOT)));
  }

  private void filterItems(String filter) {
    for (SideNavItem item : allItemsNav.getItems()) {
      applyFilter(item, filter);
    }
  }

  /** Shows the item when it matches or a descendant does. Returns whether it stayed visible. */
  private boolean applyFilter(SideNavItem item, String filter) {
    boolean itemMatches = item.getLabel().toLowerCase(Locale.ROOT).contains(filter);
    boolean childMatches = false;
    for (SideNavItem child : item.getItems()) {
      childMatches |= applyFilter(child, filter);
    }

    item.setVisible(itemMatches || childMatches);
    item.setExpanded(childMatches);
    if (itemMatches) {
      item.getItems().forEach(MenuItemFilter::showSubtree);
    }

    return itemMatches || childMatches;
  }

  /** Shows the item and everything under it, so a matching branch can be browsed whole. */
  private static void showSubtree(SideNavItem item) {
    item.setVisible(true);
    item.getItems().forEach(MenuItemFilter::showSubtree);
  }

  public SideNav getNav() {
    return allItemsNav;
  }
}
