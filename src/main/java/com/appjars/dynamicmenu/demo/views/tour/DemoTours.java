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
package com.appjars.dynamicmenu.demo.views.tour;

import com.appjars.dynamicmenu.flow.view.ListMenuItemsView;
import com.appjars.dynamicmenu.flow.view.util.TestIds;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.server.auth.AccessAnnotationChecker;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.vaadin.addons.antlerflow.tour.EngineType;
import org.vaadin.addons.antlerflow.tour.Tour;
import org.vaadin.addons.antlerflow.tour.TourButton;
import org.vaadin.addons.antlerflow.tour.TourButtonType;
import org.vaadin.addons.antlerflow.tour.TourStep;

/**
 * Factory of the guided tours offered by the demo. A step attaches to a {@code data-antler-target}
 * marker that a client-side resolver puts on the first visible match of its selector, never to the
 * selector itself; a selector matching nothing leaves the step centered.
 */
public final class DemoTours {

  /** Session attribute used to start a tour after navigating to its view. */
  public static final String PENDING_TOUR_ATTRIBUTE = DemoTours.class.getName() + ".pendingTour";

  static final String KEY_PREFIX = "appjars.dynamicmenu.demo.tour.";

  private static final String TARGET_ATTR = "data-antler-target";

  /** Ids set by the demo itself, used as tour anchors. */
  public static final String ID_DRAWER_HEADER = "demo-drawer-header";

  public static final String ID_MENU_FILTER = "demo-menu-filter";
  public static final String ID_SIDE_NAV = "demo-side-nav";
  public static final String ID_TOUR_MENU = "demo-tour-menu";
  public static final String ID_USER_MENU = "demo-user-menu";

  /** Styles an entry of a tour menu: see {@code styles.css}. */
  public static final String CLASS_TOUR_ENTRY = "demo-tour-entry";

  public enum DemoTour {
    MENU, EDITOR, ITEM_DIALOG, ITEM_PERMISSIONS
  }

  private static final AccessAnnotationChecker ACCESS_CHECKER = new AccessAnnotationChecker();

  /** The view a tour runs on, or {@code null} when it can be taken from anywhere. */
  public static Class<? extends Component> view(DemoTour tour) {
    return switch (tour) {
      case MENU -> null;
      case EDITOR, ITEM_DIALOG, ITEM_PERMISSIONS -> ListMenuItemsView.class;
    };
  }

  /** Whether the current user is allowed to open the view the tour runs on. */
  public static boolean isAvailable(DemoTour tour) {
    Class<? extends Component> view = view(tour);
    return view == null || ACCESS_CHECKER.hasAccess(view);
  }

  /** Suffix of the message key naming the tour in a menu. */
  public static String labelKey(DemoTour tour) {
    return switch (tour) {
      case MENU -> "menu";
      case EDITOR -> "editor";
      case ITEM_DIALOG -> "itemdialog";
      case ITEM_PERMISSIONS -> "itempermissions";
    };
  }

  /** The icon standing for the tour in a menu. */
  public static VaadinIcon icon(DemoTour tour) {
    return switch (tour) {
      case MENU -> VaadinIcon.MENU;
      case EDITOR -> VaadinIcon.LIST;
      case ITEM_DIALOG -> VaadinIcon.EDIT;
      case ITEM_PERMISSIONS -> VaadinIcon.KEY;
    };
  }

  /** An icon and a label side by side, for one entry of a tour menu. */
  public static Component menuEntry(VaadinIcon icon, String label) {
    Icon prefix = icon.create();
    Div entry = new Div(prefix, new Span(label));
    entry.addClassName(CLASS_TOUR_ENTRY);
    return entry;
  }

  /** Whether the tour points at components living inside an overlay (see {@link #start}). */
  public static boolean touchesAnOverlay(DemoTour tour) {
    return tour == DemoTour.ITEM_DIALOG || tour == DemoTour.ITEM_PERMISSIONS;
  }

  /** A step. {@code selector} is the real element selector; null renders the step centered. */
  private record StepDef(String key, String selector, String position, boolean first, boolean last) {}

  /** Restores {@code overflow} on the parent of the highlighted element. */
  private static final String TOUR_CSS_JS =
      """
      if (!document.getElementById('demo-tour-css')) {
        const style = document.createElement('style');
        style.id = 'demo-tour-css';
        style.textContent =
            'body :not(body):has(> .driver-active-element) { overflow: visible !important; }';
        document.head.appendChild(style);
      }
      """;

  /**
   * Blocks user scrolling while a step is anchored, by cancelling the input events rather than
   * setting {@code overflow}, so programmatic scrolling still works. Targets taller than the
   * viewport are re-aligned to their top, which the engine would otherwise center and cut off.
   */
  private static final String SCROLL_LOCK_JS =
      """
      if (window.__demoTourScrollLock) { window.__demoTourScrollLock.stop(); }
      // A centered step is highlighted through a zero-sized dummy element: it is not anchored to
      // anything, so the page stays scrollable.
      const scoped = () => {
        const el = document.querySelector('.driver-active-element');
        if (!el || el.id === 'driver-dummy-element') return false;
        const r = el.getBoundingClientRect();
        return r.width > 4 && r.height > 4;
      };
      const inPopover = (t) => !!(t && t.closest && t.closest('.driver-popover'));
      const target = (e) => (e.composedPath && e.composedPath()[0]) || e.target;
      const block = (e) => { if (scoped() && !inPopover(target(e))) { e.preventDefault(); } };
      const KEYS = new Set(['ArrowUp', 'ArrowDown', 'PageUp', 'PageDown', 'Home', 'End', ' ']);
      const blockKeys = (e) => {
        if (!scoped() || !KEYS.has(e.key)) return;
        const t = target(e);
        if (inPopover(t)) return;
        if (t && /^(INPUT|TEXTAREA)$/.test(t.tagName)) return;
        e.preventDefault();
      };
      document.addEventListener('wheel', block, {passive: false, capture: true});
      document.addEventListener('touchmove', block, {passive: false, capture: true});
      document.addEventListener('keydown', blockKeys, true);

      // Re-frame a target that does not fit on screen, once the engine has finished scrolling to it
      let framed = null;
      let pending = 0;
      const frame = () => {
        const el = document.querySelector('.driver-active-element');
        if (!el || el.id === 'driver-dummy-element') { framed = null; return; }
        if (el === framed) return;
        framed = el;
        clearTimeout(pending);
        pending = setTimeout(() => {
          if (el !== document.querySelector('.driver-active-element')) return;
          if (el.getBoundingClientRect().height > window.innerHeight * 0.9) {
            el.scrollIntoView({block: 'start', inline: 'nearest'});
          }
        }, 60);
      };
      const frameObs = new MutationObserver(frame);
      frameObs.observe(document.body, {subtree: true, attributes: true, attributeFilter: ['class']});
      frame();

      window.__demoTourScrollLock = { stop() {
        document.removeEventListener('wheel', block, true);
        document.removeEventListener('touchmove', block, true);
        document.removeEventListener('keydown', blockKeys, true);
        frameObs.disconnect();
        clearTimeout(pending);
        window.__demoTourScrollLock = null; } };
      """;

  /**
   * $0 is a JSON map of {stepId: cssSelector}. Tags the first visible match of each selector with a
   * space-separated list of step ids, since two steps may describe the same element.
   */
  private static final String RESOLVE_TARGETS_JS =
      """
      const MAP = JSON.parse($0);
      const ATTR = 'data-antler-target';
      const resolve = () => {
        const wanted = new Map();
        Object.keys(MAP).forEach(id => {
          for (const el of document.querySelectorAll(MAP[id])) {
            const r = el.getBoundingClientRect();
            if (r.width > 4 && r.height > 4) { wanted.set(el, (wanted.get(el) || []).concat(id)); break; }
          }
        });
        document.querySelectorAll('[' + ATTR + ']')
            .forEach(el => { if (!wanted.has(el)) { el.removeAttribute(ATTR); } });
        wanted.forEach((ids, el) => {
          const value = ids.join(' ');
          if (el.getAttribute(ATTR) !== value) { el.setAttribute(ATTR, value); }
        });
      };
      if (window.__antlerResolver) { window.__antlerResolver.stop(); }
      let scheduled = false;
      const schedule = () => { if (scheduled) return; scheduled = true;
        requestAnimationFrame(() => { scheduled = false; resolve(); }); };
      resolve();
      const obs = new MutationObserver(schedule);
      obs.observe(document.body, {childList: true, subtree: true, attributes: true,
          attributeFilter: ['hidden', 'style', 'class']});
      window.__antlerResolver = { stop() { obs.disconnect();
        document.querySelectorAll('[' + ATTR + ']').forEach(el => el.removeAttribute(ATTR));
        window.__antlerResolver = null; } };
      """;

  /**
   * Keeps the popover next to its target, which the engine positions only once. Driver.js
   * repositions on {@code resize}, so one is dispatched whenever the target has actually moved.
   */
  private static final String REANCHOR_JS =
      """
      if (window.__demoTourReanchor) { window.__demoTourReanchor.stop(); }
      let frame = 0;
      let last = '';
      const tick = () => {
        const el = document.querySelector('.driver-active-element');
        const r = el ? el.getBoundingClientRect() : null;
        const at = r ? [r.x, r.y, r.width, r.height].join() : '';
        if (at !== last) { last = at; window.dispatchEvent(new Event('resize')); }
        frame = requestAnimationFrame(tick);
      };
      frame = requestAnimationFrame(tick);
      window.__demoTourReanchor = { stop() {
        cancelAnimationFrame(frame);
        window.__demoTourReanchor = null; } };
      """;

  /**
   * Vaadin 25 overlays paint in the browser top layer, so the tour popover is promoted there too and
   * re-asserted whenever another overlay opens.
   */
  private static final String PROMOTE_TOP_LAYER_JS =
      """
      if (window.__demoTourTopLayer) { window.__demoTourTopLayer.stop(); }
      const promote = () => document.querySelectorAll('.driver-popover').forEach(el => {
        if (el.getAttribute('popover') !== 'manual') el.setAttribute('popover', 'manual');
        el.style.margin = '0';
        try { if (!el.matches(':popover-open')) el.showPopover(); } catch (e) {}
      });
      const reassert = () => { const el = document.querySelector('.driver-popover');
        if (el && el.matches(':popover-open')) { try { el.hidePopover(); el.showPopover(); } catch (e) {} } };
      const onToggle = (e) => { const t = e.target;
        if (e.newState === 'open' && t && t.classList && !t.classList.contains('driver-popover')) reassert(); };
      document.addEventListener('toggle', onToggle, true);
      const obs = new MutationObserver(promote);
      obs.observe(document.body, {childList: true, subtree: true});
      promote();
      window.__demoTourTopLayer = { stop() { obs.disconnect();
        document.removeEventListener('toggle', onToggle, true);
        document.querySelectorAll('.driver-popover[popover]').forEach(el => {
          try { el.hidePopover(); } catch (e) {} el.removeAttribute('popover'); });
        window.__demoTourTopLayer = null; } };
      """;

  private static final String STOP_JS =
      """
      if (window.__antlerResolver) { window.__antlerResolver.stop(); }
      if (window.__demoTourTopLayer) { window.__demoTourTopLayer.stop(); }
      if (window.__demoTourScrollLock) { window.__demoTourScrollLock.stop(); }
      if (window.__demoTourReanchor) { window.__demoTourReanchor.stop(); }
      const css = document.getElementById('demo-tour-css');
      if (css) { css.remove(); }
      """;

  private DemoTours() {}

  /** Creates the tour, attaches it to {@code host} and starts it. */
  public static void start(DemoTour tour, Component host,
      SerializableFunction<String, String> translator) {
    start(tour, host, translator, () -> {}, () -> {});
  }

  /**
   * Same, running {@code onStart} once the tour started and {@code onStop} when it ends. Used by the
   * tours that need a dialog open throughout: antler-tour has no per-step hooks.
   */
  public static void start(DemoTour tour, Component host,
      SerializableFunction<String, String> translator, Runnable onStart, Runnable onStop) {
    List<StepDef> defs = steps(tour);
    Tour t = create(defs, translator);
    host.getElement().appendChild(t.getElement());
    host.getElement().executeJs(TOUR_CSS_JS);
    host.getElement().executeJs(SCROLL_LOCK_JS);
    host.getElement().executeJs(REANCHOR_JS);
    host.getElement().executeJs(RESOLVE_TARGETS_JS, targetJson(defs));
    t.addTourCompletedListener(e -> stop(t, host, onStop));
    t.addTourCanceledListener(e -> stop(t, host, onStop));
    t.start();
    if (touchesAnOverlay(tour)) {
      host.getElement().executeJs(PROMOTE_TOP_LAYER_JS);
    }
    // Last, so the top layer listener is armed before the overlay opens
    onStart.run();
  }

  private static void stop(Tour tour, Component host, Runnable onStop) {
    onStop.run();
    host.getElement().executeJs(STOP_JS);
    tour.getElement().removeFromParent();
  }

  private static Tour create(List<StepDef> defs, SerializableFunction<String, String> translator) {
    List<TourStep> steps = defs.stream().map(def -> step(def, translator)).toList();
    // Driver.js (MIT) is the only engine this demo may ship: Shepherd.js is not free commercially.
    // It is the default; setting it explicitly rules out an accidental fallback.
    return Tour.builder().engineType(EngineType.DRIVER).steps(steps).showCancelButton(true)
        .allowClose(true).build();
  }

  private static List<StepDef> steps(DemoTour tour) {
    return switch (tour) {
      case MENU -> menuSteps();
      case EDITOR -> editorSteps();
      case ITEM_DIALOG -> itemDialogSteps();
      case ITEM_PERMISSIONS -> itemPermissionsSteps();
    };
  }

  private static List<StepDef> menuSteps() {
    return List.of(
        new StepDef("menu.intro", null, null, true, false),
        new StepDef("menu.drawer", "#" + ID_DRAWER_HEADER, "right", false, false),
        new StepDef("menu.filter", "#" + ID_MENU_FILTER, "right", false, false),
        new StepDef("menu.nav", "#" + ID_SIDE_NAV, "right", false, false),
        new StepDef("menu.user", "#" + ID_USER_MENU, "right", false, false),
        new StepDef("menu.provider", null, null, false, true));
  }

  private static List<StepDef> editorSteps() {
    return List.of(
        new StepDef("editor.intro", null, null, true, false),
        new StepDef("editor.grid", testId(TestIds.LIST_GRID), "top", false, false),
        new StepDef("editor.new", testId(TestIds.LIST_NEW_BUTTON), "bottom", false, false),
        new StepDef("editor.actions", testId(TestIds.LIST_ACTIONS_MENU), "bottom", false, false),
        new StepDef("editor.restrictions", testId(TestIds.LIST_RESTRICTIONS), "bottom", false,
            false),
        new StepDef("editor.dragdrop", testId(TestIds.LIST_GRID), "top", false, false),
        new StepDef("editor.finish", null, null, false, true));
  }

  private static List<StepDef> itemDialogSteps() {
    return List.of(
        new StepDef("dialog.intro", null, null, true, false),
        // Points at the first radio button: the group stretches across the dialog, so a popover
        // centered on it would float over empty space
        new StepDef("dialog.tabs", testId(TestIds.DIALOG_TAB_SETTINGS), "bottom", false, false),
        new StepDef("dialog.label", testId(TestIds.DIALOG_LABEL), "bottom", false, false),
        new StepDef("dialog.parent", testId(TestIds.DIALOG_PARENT), "bottom", false, false),
        new StepDef("dialog.url", within(TestIds.DIALOG_URL_TYPE, "vaadin-radio-button"), "bottom",
            false, false),
        new StepDef("dialog.icon", testId(TestIds.DIALOG_ICON_TYPE), "bottom", false, false),
        new StepDef("dialog.permissions", testId(TestIds.DIALOG_TAB_PERMISSIONS), "bottom", false,
            false),
        new StepDef("dialog.save", testId(TestIds.DIALOG_SAVE), "top", false, false),
        new StepDef("dialog.finish", null, null, false, true));
  }

  private static List<StepDef> itemPermissionsSteps() {
    return List.of(
        new StepDef("perm.intro", null, null, true, false),
        new StepDef("perm.tab", testId(TestIds.DIALOG_TAB_PERMISSIONS), "bottom", false, false),
        // The label, not the checkbox, which the layout stretches to the full width
        new StepDef("perm.onlyifallowed", within(TestIds.DIALOG_PERM_ONLY_IF_ALLOWED, "label"),
            "bottom", false, false),
        new StepDef("perm.all", testId(TestIds.DIALOG_PERM_ALL), "bottom", false, false),
        new StepDef("perm.any", testId(TestIds.DIALOG_PERM_ANY), "bottom", false, false),
        // Below: above, the popover covers the fields of the previous two steps
        new StepDef("perm.none", testId(TestIds.DIALOG_PERM_NONE), "bottom", false, false),
        new StepDef("perm.finish", null, null, false, true));
  }

  private static String testId(String value) {
    return "[" + TestIds.ATTRIBUTE + "='" + value + "']";
  }

  /** The first visible {@code descendant} of a marked element. */
  private static String within(String value, String descendant) {
    return testId(value) + " " + descendant;
  }

  private static TourStep step(StepDef def, SerializableFunction<String, String> t) {
    List<TourButton> buttons = new ArrayList<>();
    if (!def.first()) {
      buttons.add(TourButton.builder().label(t.apply(KEY_PREFIX + "btn.back")).secondary(true)
          .type(TourButtonType.PREVIOUS).build());
    }
    buttons.add(
        TourButton.builder().label(t.apply(KEY_PREFIX + (def.last() ? "btn.done" : "btn.next")))
            .type(TourButtonType.NEXT).build());
    return TourStep.builder().id(stepId(def)).attachTo(attachTo(def)).position(def.position())
        .title(t.apply(KEY_PREFIX + def.key() + ".title"))
        .content(t.apply(KEY_PREFIX + def.key() + ".desc")).buttons(buttons).build();
  }

  private static String stepId(StepDef def) {
    return def.key().replace('.', '-');
  }

  private static String attachTo(StepDef def) {
    return def.selector() == null ? null : "[" + TARGET_ATTR + "~='" + stepId(def) + "']";
  }

  /** {@code {stepId: selector}} for the steps that have one, as JSON. */
  private static String targetJson(List<StepDef> defs) {
    return defs.stream().filter(def -> def.selector() != null)
        .map(def -> "\"" + stepId(def) + "\":\"" + def.selector().replace("\"", "\\\"") + "\"")
        .collect(Collectors.joining(",", "{", "}"));
  }
}
