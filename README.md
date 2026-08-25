# Dynamic Menu AppJars - Demo

A demo Spring Boot application showcasing the Dynamic Menu appjar: a multi-level application menu
stored in your own datastore, with a built-in editor, role-based visibility, pluggable icon sets and
JSON import/export.

## Running the demo

You need **JDK 21** and **Maven 3.9+**. Nothing else: the database is embedded and there are no
services to start.

1. `git clone` this repository and enter it.
2. Run `mvn` — the default goal is `spring-boot:run`. The first build downloads the Vaadin
   frontend, which can take a few minutes.
3. Open http://localhost:8080.
4. Stop it with `Ctrl+C`.

The application starts on a public landing page that presents the appjar features, the free/full
license model, and guided tours of the drawer and the menu editor.

On the first start a small sample menu is created, so the drawer is populated from the very
beginning. Each of its four entries decides differently who gets to see it, which is what makes
switching accounts worth doing.

The landing page and the menu are open to anyone. The menu editor (`/dm/list`) is an administration
screen — the appjar annotates it `@PermitAll` — so it asks you to sign in first.

The drawer is built once per page load, so after editing the menu use **Refresh menu** in the
editor's actions menu (or reload the page) to see your changes there.

### Demo accounts

Sign in from the drawer footer or at `/login`. There is nothing to type: the accounts are listed in a
combo box and their credentials are submitted for you (they are in-memory, with the password equal to
the account name).

| Account | Role | What it sees in the drawer |
|---|---|---|
| *not signed in* | — | Documentation |
| `user` | `USER` | + Administration → Menu editor |
| `manager` | `MANAGER` | the same as `user` |
| `admin` | `ADMIN` | + Administration → Audit log |

`manager` exists so there is a role that is neither of the other two: give an entry `MANAGER` and
`ADMIN` as *sufficient* roles and it appears for those two accounts only.

### Views to point entries at

Three views exist purely so menu entries can target real routes and be gated by real access rules:

| Route | Annotation | Notes |
|---|---|---|
| `dashboard` | `@PermitAll` | any signed-in user |
| `reports/:period?` | `@RolesAllowed({"MANAGER", "ADMIN"})` | has a route parameter, which the editor asks for |
| `audit` | `@RolesAllowed("ADMIN")` | the seeded Audit log entry points here |

The seeded menu leaves one slot free under the five-item limit of the free version. The intended
exercise: create an entry pointing at `reports`, give it `MANAGER` and `ADMIN` as sufficient roles,
and switch accounts to watch it come and go.

### Resetting the sample menu

The H2 database lives in `./data` and the sample menu is only created when the menu is empty. Delete
that folder to get the original four entries back.

## Guided tours

Launch them from the landing page or from the **Guided tour** menu in the application header, which
is available on every screen ("This page" starts the tour of the view you are on, and is disabled on
views that have none):

| Tour | What it covers |
|---|---|
| The application menu | The drawer: filtering, hierarchy, where the entries come from, and how they change per user |
| The menu editor | The items tree, creating entries, import/export, reordering |
| Editing a menu item | The item editor: label, parent, target route, icon |
| Permissions of a menu item | The permission rules that decide who sees an entry |

## Configuration

All of the appjar's properties, in `src/main/resources/application.properties`:

| Property | Default | What it does |
|---|---|---|
| `com.appjars.dynamicmenu.url.views.menuitemsview` | `dm/list` | Route the menu item editor is mounted at |
| `appjars.dynamicmenu.icon.size` | `MEDIUM` | Application-wide default icon size (`SMALL`, `MEDIUM`, `LARGE`), overridable per entry in the editor |

The demo's own settings live in the same file, grouped by datasource, JPA, Vaadin and logging.

## What the appjar does

- **Multi-level menu from your datastore.** Menu entries are persisted through the appjar's own
  data layer and nest as deep as you need. Your layout asks `DynamicMenuItemProvider` for the
  entries and adds them to any navigation component — the appjar imposes no layout of its own.
- **Menu editor.** A ready-to-use administration view (`ListMenuItemsView`, mounted at the route
  set by `com.appjars.dynamicmenu.url.views.menuitemsview`, `dm/list` by default) to create, edit
  and delete entries, and to reorder them by dragging rows: drop one on another to nest it, or
  between two rows to place it there.
- **Visibility by role.** Each entry carries its own security constraints — roles that are
  necessary, sufficient or forbidden — evaluated against the authorities of the current user. An
  entry can also be left to follow the access rules already declared on its target route, and a
  rejected entry is left out of the menu together with its children.
- **Pluggable icon sets.** Icons are resolved generically from any enum implementing Vaadin's
  `IconFactory`, so exposing an `IconFamilyProvider` bean makes your own families selectable in the
  editor, and each entry can set its icon size. Out of the box the appjar offers Vaadin Icons; this
  demo replaces that provider (see `DemoIconProviderConfiguration`) to add the three FontAwesome
  families — pulled in by the demo's own `font-awesome-iron-iconset` dependency — and `CustomIcons`,
  a hand-written family backed by an SVG iconset, as an example of plugging in your own.
- **Internal routes and external links.** An entry points either at one of the routes actually
  registered in your application — with a field for each of its URL parameters — or at an external
  address. Entries can also be plain separators.
- **Import and export.** The whole menu can be exported as JSON and imported back, with a preview
  of which entries are new and which already exist, and four merge strategies.
- **Internationalization.** Marking an entry as internationalized turns its label and tooltip into
  keys resolved through your own translation files.

## License

This demo runs in free mode: every feature is fully functional, limited to 5 menu items in
total. A full license removes the limit — nothing else changes. Get one at
[appjars.com](https://www.appjars.com), or read the
[AppJars documentation](https://docs.appjars.com).
