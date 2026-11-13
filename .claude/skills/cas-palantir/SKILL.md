---
name: cas-palantir
description: "Change Palantir, the CAS admin dashboard (templates under palantir/, palantir-*.js and palantir-meridian.css): layout and themes, navigation, tabs and the command palette, dialogs and widgets (jQuery UI, MDC, DataTables, SweetAlert), the applications table and cards, the service wizard and JSON editor, edit guards, feature toggles, endpoint access levels and SAML2 metadata tools. Use when touching Palantir UI code or its scenarios."
---

# Palantir admin dashboard

General view, theme and template rules are in the `cas-ui` skill.

## Layout, themes, navigation and accessibility

- Palantir's layout and themes live in `palantir-meridian.css` (loaded after `palantir.css`): `--palantir-*` tokens are defined
  for light and for dark on `html.palantir-page[data-theme]`, and the shared `--cas-ui-*`/`--cas-theme-*` variables are pointed
  at them inside Palantir. Color new rules with the tokens (`var(--palantir-surface, #fff)` in `palantir.css`), never a bare
  light color; where the brand color sits behind white text, dark needs `--palantir-brand-fill`. The page head applies the
  stored theme (`PalantirTheme` in localStorage: system, light, dark) before first paint; `palantir-theme.js` owns the toggle,
  the settings choice, Chart.js defaults and the `palantir:theme-changed` event. Never set `Chart.defaults.borderColor` or
  `backgroundColor`: Chart.js only applies its dataset palette while both keep their stock values; theme the grid through
  `Chart.defaults.scale.grid.color` and `Chart.defaults.scale.border.color`.
- Palantir's navigation (`fragments/palantir/navigationsidebar.html`) is a top bar: Applications, grouped menus (Identity,
  Sessions, Platform) and the theme, settings and sign-out buttons. Only destinations may be `<li>` (scripts treat every
  `nav.sidebar-navigation ul li` as one and count them with `:visible`; polling also stops when a destination is not
  `:visible`), so closed menus and the narrow-screen overlay hide with `visibility`, never `display: none`.
  `visibility` is animatable: a `transition: all` on anything inside a menu (cas.css has one on sidebar items and icons)
  keeps it on screen after the menu closes, so transitions there name their properties.
  `updatePalantirNavigationGroups()` hides empty groups, marks the active one and writes the page heading.
  A row of inner jQuery UI tabs with one visible tab is clipped out of sight, not hidden, for the same reason.
  DataTables group header rows come from `palantirTableGroupRow(label, columns)` (palantir-utils.js); never inline-style them.
- A Palantir page is a `Tabs` entry (`PalantirDashboardTab(name, index, shortcut)`), a positional MDC tab button in
  `dashboardtabs.html`, a panel `#attribute-tab-N`, a nav `<li data-tab-index=N>`, its scripts in
  `casPalantirDashboardView.html` and lazy initializers through `registerDashboardTabInitializers`; hide it in
  `processNavigationTabs()` when its endpoints are missing. The Users page (index 16, `palantir-users.js`) is a list of
  `PalantirUserLookup.sources()` (key, title, icon, `available()`, `load(username)`, `render(records, username)`): add a
  per-user source there, not a new page (cards are `<details>`; an empty source keeps a hidden card so a reload can
  bring it back, and only the first non-empty card opens by default; a table is the standard `mdc-data-table__table` DataTable, rendered
  empty and filled in the source's `afterRender`); reads go through `PalantirUserLookup.get` (404 means no records; the audit log
  endpoint needs a form content type even on GET and matches its principal as a regex, so quote it).
- Command palette sections are discovered, never listed: every tab of every `.jqueryui-tabs` in a page, at any depth,
  and any element with `data-palantir-search-section="Label"`. New sub-sections need nothing but markup; use
  `data-palantir-search="false"` to hide one, `data-palantir-search-label` / `-keywords` to rename it or add words.
  A tab that a page reveals in its initializer (class `d-none` until then) must declare what it needs with
  `data-palantir-requires="<CasActuatorEndpoints accessor> feature:<CasFeature>"` so search finds it before the page
  is opened; once the page has opened, the page's own show/hide decision wins.
- Short screens: `keepPalantirPopupsInView()` (palantir-utils) caps every `.palantir-dialog` at the window height
  (content scrolls), re-centers dialogs that grow or overflow, and positions select menus upward when there is no room
  below in the window or in their dialog (dialogs clip with `overflow: hidden`). Don't give dialogs fixed heights taller
  than the window, and don't rely on jQuery UI's own `flip` for select menus inside dialogs.
- A row of a label, a jQuery UI selectmenu and buttons uses `.palantir-control-row` (centered, one height).
- Keyboard shortcuts are listed from `palantirShortcutGroups()` (palantir-hotkeys.js) for both the `?` overlay and the
  Settings tab; add a new hotkey there too.
- `initializeTableAndDialogAccessibility()` (palantir-utils.js) names every DataTable and its pagination, names icon-only
  controls from their `title`, marks modal dialogs `aria-modal` and names jQuery UI select menus from their label (jQuery
  UI points the label's `for` at the button). Tables with a row context menu get focusable rows (Enter clicks, Shift+F10
  opens the menu) through `initializeDataTableContextMenu`. jQuery UI tabs still trip axe's nested-interactive rule.
- The command palette (`palantir-command-palette.js`, Ctrl/⌘+K and `/`) reads its entries from the nav, the outer inner-tab
  row of each panel, `registeredServicesById` and a few actions each time it opens, so a new page or inner tab shows up on its
  own; it is a jQuery UI dialog with a categorized autocomplete, not another library. Ctrl+K, `/` and `?` are one document keydown
  handler in `initializeCommandPalette`: Ctrl+K works inside fields, `/` and `?` are matched on `event.key` (not
  hotkeys key codes) so they work on any layout and the numeric keypad, and are ignored while typing in a field.
- To look at Palantir without a running CAS: render `palantir/casPalantirDashboardView` with Thymeleaf, the layout dialect,
  OGNL and a stub `#cas` expression object (jars from a built war; SpEL `?:` inside `${}` and `T(...)` need rewriting for
  OGNL), serve `static/` with the webjars (npm for those not in the war), and stub the actuator URLs in Playwright.
- Palantir dialogs sit outside `#main-content`, so `applySharedDialogStyle()` (palantir-utils.js, called from
  `initializePalantir`) adds `cas-ui palantir-dialog` on `dialogcreate`; style dialogs through those classes, qualified enough
  to beat the Cupertino theme (`.ui-dialog.palantir-dialog …`). Use the `classes` option, not the deprecated `dialogClass`.
  jQuery 4 dropped `isWindow`/`isFunction`, which the jQuery UI 1.12 position plugin shipped with contextMenu still calls and
  which replaces jQuery UI's own position: `installJQueryCompatibility()` must run before any dialog, menu or tooltip opens.
  Tom Select wrappers carry `z-index: 10000`; open select menus are raised above them.

## Forms, dialogs and widgets

- Palantir forms: Enter submits through the form's primary button, which is `type="submit"` while the form keeps
  `onsubmit="return false;"` (the button's click handler does the work); every dashboard form needs that attribute, since a
  form with one text field and no handler reloads the page on Enter. Don't add per-input Enter key handlers.
- Palantir file pickers are drop zones: an invisible file input covers a themed `<label>`, so click, keyboard and drop reach
  the native input (`.palantir-saml-metadata-import__file`); don't leave a bare `type="file"` in a themed dialog.
- Palantir date fields use the jQuery UI date picker: give the input the `jquery-datepicker` class and, for a format other
  than the picker's default, a `dateFormat` data value (`createInputField({data: {dateFormat: "yy-mm-dd"}})`; quoted text is
  literal, e.g. `yy-mm-dd'T'00:00:00` for a date-time field). `initializeDatePickers` attaches it and fires `input` on pick.
  The picker's `#ui-datepicker-div` is raised above dialogs in `palantir-meridian.css`. Don't add another date library.
- A SweetAlert opened over a modal jQuery UI dialog can only take focus because `allowAlertsOverModalDialogs` extends
  `_allowInteraction`; without it the dialog pulls focus back and the alert's inputs cannot be typed into.
- `PalantirProgress` (palantir-utils.js) is the modal progress dialog for waits: `open(title, message, detail)`,
  `update(message, percent)` with `null` for "busy" (an animated bar; the server reports no progress), `close()`.
  Import uses it: real upload percentages from `xhr.upload` progress events, busy while CAS imports, busy while the table
  refreshes. Start busy rather than at 0%: intercepting proxies (and Playwright routes) deliver no upload events.
- jQuery UI tooltips (one `$(document).tooltip`) close on `mouseleave` or jQuery's `remove` event; markup replaced
  with `innerHTML` (the wizard step bar, toolbars) fires neither, so `closeOrphanedTooltips` watches the targets of open
  tooltips and closes those detached or hidden. Prefer `$(x).html(...)` for new redraws anyway.
- MDC dialogs (`MDCDialog.attachTo`) whose only content is an Ace editor fail to open (MDC's focus trap throws
  "Element must have at least one focusable child"); show documents with `showPalantirCodeDialog(dialogId, title, text,
  mode)` (jQuery UI, editor id = dialog id + "Editor"). Ten MDC dialogs with buttons remain (consent, tenants, pac4j,
  attribute definitions, SAML metadata entries, changelog, sessions, HTTP traces, thread dumps).
- A lookup panel inside a page (passwordless account, interrupt inquiry in `palantir-interrupts.js`) uses the
  `passwordless-account-*` classes: search form, banner message, result card, detail/capability grids, chips, and a
  DataTable per list; reveal its inner tab with `showElements($("#<id>").parent())` only when its endpoint exists.
  The Users page links to it with `openInterruptInquiry(username)` (`data-user-navigate`, not `data-user-action`).
- Markup that CAS sends to be shown as HTML (interrupt messages) goes through `sanitizePalantirHtml` (palantir-utils.js:
  inert parse, allow-listed tags without attributes, http/https/mailto links only); use `palantirHtmlText` for plain-text
  cells. Never put such markup in the page with `.html()` directly.

## Applications table and cards

- The applications type filter is a `$.fn.dataTable.ext.search` function limited to `#applicationsTable`, so the cards,
  paging and summary follow it; `updateApplicationTypeFilter` rebuilds its choices in `fetchServices`. Filter the cards
  through the table, never on their own.
- DataTables 3 renamed the settings fields (`nTable` is now `table`, `sTableId` is `tableId`): in `ext.search` functions and
  events, find the table through the API, `new $.fn.dataTable.Api(settings).table().node()`, never a settings field.
- After an application is added, changed or deleted, Palantir calls `fetchServices`: it alone refreshes the table, cards,
  services chart, SAML2 metadata sources and the search's application index. Removing a table row alone leaves them stale.
- Palantir applications: the card view (`#applicationsCards`) is drawn from the DataTable's filtered current page on every
  draw and when a row's class changes, so search, paging and the selected row apply to both views; card buttons call
  `applicationActionHandler(key, context)`, the same handler as the row context menu, with `applicationRowContext(id)`.
- The applications selection bar is `position: fixed` at the bottom of the window (it must not push content). It is
  moved to `<body>` on initialization, since an ancestor would otherwise turn fixed into scrolling with the page, so
  style it outside `#dashboard` (its own selectors), hide it when the Applications page is not shown, and lift it
  above the CAS footer (`--palantir-selection-bar-offset`). Palantir scrolls an inner container, not the window.
- Disabling applications (`PalantirApplicationAccess`, palantir-services-actions.js): only
  `DefaultRegisteredServiceAccessStrategy` has `enabled` (serialized only when false, `NON_DEFAULT`); any other strategy
  is combined in an AND `ChainingRegisteredServiceAccessStrategy` with a bare disabled default strategy, which enabling
  removes (a chain left with one strategy is unwrapped). Disabled = default off, AND chain holding one, OR chain of only
  those. Selection (`PalantirApplicationSelection`) is a set of ids shown through checkboxes in the type column and the
  cards; their `change` events are taken in the capture phase, because the table's Material data-table component
  treats any checkbox change inside it as its own row checkbox and throws. Bulk delete goes one DELETE per id with
  `PalantirProgress`; Delete All is `DELETE registeredServices` (it removes stored definitions, so palantir-login never
  runs it).
- `fetchServices` keeps the applications table's search when it reloads (it used to clear it, so disabling or deleting
  while searching lost the search, and scenario steps that searched right after a reload raced the reset). Scenario
  steps that rely on a row being drawn must search for it: palantir-login has more applications than one page shows.
- Import posts the chosen file to `registeredServices/import` with the content type of its extension (JSON, YAML, or
  octet-stream for a zip, which the endpoint reads as a zip of JSON files); the file input's `change` handler is bound
  once in `initializeServiceButtons` (namespaced) and the input is cleared, so never bind it inside the button's click.
- Service history (`palantir-services-history.js`) is a jQuery UI dialog over the `entityHistory` endpoint: versions in
  `#entityHistoryTable` (latest first, row data carries `serviceId`/`revisionId`/`index`), a side-by-side diff drawn with
  the jsdiff webjar (global `Diff`) over JSON with sorted keys, the right side always the selected version; restore goes
  through `PalantirServiceHistory.restore` from the footer and the row context menu.

## Service wizard and JSON editor

- The service wizard's fields are built in the background once the applications are loaded and the browser is idle
  (`prepareRegisteredServiceWizardWhenIdle`; palantir-login checks the `palantir-wizard-built` mark follows
  `palantir-dashboard-shown`), or on first open if sooner (`palantirWizardField` builders), after the Applications panel ran
  `initializePalantirWidgets`; `buildRegisteredServiceWizardFields` initializes the form's widgets afterwards, so dropdowns
  get the change callback that runs their `data-change-handler` functions. A bare `.selectmenu()` has no such callback.
- A registered service description is HTML (the login page renders it with `th:utext`). Palantir shows it through
  `renderPalantirDescription`, an allowlist of `p`, `br`, `b`/`strong`, `i`/`em` and `u` without attributes, and uses
  `describePalantirDescription` for plain text (search, tooltips); never `escapePalantirMarkup`, which shows the markup.
  The wizard's editor saves a single paragraph without its `<p>` wrapper (`unwrapDescriptionParagraph`, its `renderer`).
  Server-side views write it with `th:utext="${#cas.sanitizeHtml(...)}"` (jsoup, same allowlist); never a bare `th:utext`.
- The wizard's key/value fields (`createMappedInputField`) emit `java.util.TreeMap`, which sorts the keys; a map whose
  order matters (MFA provider selection mappings, first match wins) passes `mapType: "java.util.LinkedHashMap"`.
- A wizard field that several policy types share lists each type's simple class name in `cssClasses`
  (`"hide TypeA TypeB"`); the type's change handler shows `.<Type>` and hides the rest. Fields of different types may share
  a `paramName` (`attributeReleasePolicy.entityIds` is a string for one policy and a set for others): population fills
  every input with that name, and generation skips the hidden ones.
- `generateServiceDefinition` leaves out fields whose container is switched off, judged by `isWizardContainerSwitchedOff`
  (inline `display: none`, or the `hide` class with no inline display), since type handlers reveal fields with either
  `showElements` or jQuery `show()`. Don't judge it by `:visible`: the wizard hides every section but the current one.
- Palantir's service wizard (`palantir-services-sections.js`) maps section groups to `STEPS` (Basics, Protocol,
  Access & attributes, Policies) plus a review. Every available section keeps a tile with `data-section-key` even when its
  step is not shown (the `palantir-login` scenario clicks them by key), and `activate(key)` leaves the review. The type is
  chosen in `chooseRegisteredServiceType()` (SweetAlert with cards), not a select. Every Policies section is an
  `advanced-option`: `advancedSectionsOf(step)` keeps that step clickable while advanced options are hidden, and
  `showStep` turns them on through `#hideAdvancedOptionsButton`. Footer buttons are in DOM order (Cancel, Validate, Copy,
  Save); don't reorder them with CSS `order`, which leaves the keyboard order behind.
- Edit conflicts: `PalantirServiceEditGuard.remember(key, definition)` records the server definition an edit of an
  existing application starts from (drafts keep it as `original`, and a resumed draft restores it);
  `checkConflict(mode, text)` re-reads the application before a PUT and returns `save`, `overwrite` or `cancel`. The
  check is client side: the registry has no version to lock on.
- JSON editor (`#editServiceDialog`, `PalantirServiceEditor` / `PalantirServiceTemplates` in `palantir-services-editor.js`):
  two views, a template start screen for new definitions and the editor; footer buttons carry `data-editor-view`.
  Templates reach the page as `serviceTemplates` (`{source, type, definition}`): built in from the palantir module's
  `service-definitions/*.json` (a template's `name`/`description` are its card title/summary; put the telling setting
  first, it is the card's peek; `"..."` client id/secret are replaced on use; `DashboardControllerTests` fails if a
  template loses a field when CAS reads it), and shared from the templates directory (read per view); there are no
  per-browser templates. Use `PalantirServiceEditor.editor()`, not `initializeAceEditor("serviceEditor")`, which would put
  the cobalt theme back; the code theme is `ace/theme/palantir` (`.ace-palantir`, `--palantir-code-*` tokens). Switching
  to the wizard lists settings the wizard dropped and hands the edit-conflict original over.
- SAML2 wizard "From Metadata" (`PalantirSamlMetadataImport` in `palantir-services-wizard-saml2.js`): URLs go through
  `samlIdPRegisteredServiceMetadataCache/preview` (browsers cannot read most metadata URLs: no CORS), pasted/chosen XML
  is parsed with `DOMParser`; the definition parts are merged over the wizard's JSON and filled with
  `populateWizardFromServiceDefinition`, which fills contacts through `populateRegisteredServiceContacts` (the generic
  input loop skips `contacts`). The endpoint id is `samlIdPRegisteredServiceMetadataCache` (capital P), which link keys
  keep; the accessor must accept that spelling.
- Wizard fields nested under a policy (`multifactorPolicy.x`) must carry `paramType` unless a type selector of that
  policy sets its `@class`; otherwise a definition that only sets those fields is generated without `@class` and CAS
  cannot read it. Fields built from the discovery profile are created in `fetchIfNeeded().always(...)`, never `.done`:
  without the profile they would not exist and their values would be dropped when an application is edited.
- `registeredServices` `validate`, POST and PUT answer 400 `{"error": <Jackson message>}` for a definition CAS cannot
  read (`RegisteredServiceJsonSerializer.from` only logs and returns null); Palantir shows it via `showUnreadableDefinition`.
- Filling the wizard (`populateWizardFromServiceDefinition`) is event-driven: it starts right after the wizard is set
  up, mapped rows fill synchronously (the add button builds its row at once), and the definition is generated when
  the rows are done; a light `.is-populating` overlay replaces the old "Loading Service Definition" alert. Do not
  reintroduce fixed delays: `buildRegisteredServiceWizardFields` loads the discovery profile first and waits for the
  description editor, and `setDescriptionEditorValue` applies only the latest value and regenerates when it lands late.
- Wizard performance traps: never call `cas.attachFields()` (every text field of the page, plus another set of
  listeners each time) from a per-row or per-open path; use `attachPalantirTextFields(scope)`. Bind handlers in code
  that runs per open or per reload with a namespace and `.off(...)` first (switch buttons, the applications table's
  `draw`). `generateServiceDefinition` coalesces calls into one run. Fields built from a remote document (discovery
  profile, OIDC discovery) must still be created when it fails, with free entry, or their values are dropped on edit.
  Profile with a realistic harness (features, 400 attributes, a rich OIDC application), not the bare mocks.
- The wizard and the plain editor are guarded by `PalantirServiceEditGuard` (`palantir-services-guard.js`): each dialog's
  `beforeClose` asks when its JSON differs from the settled baseline, drafts live in localStorage under
  `PalantirServiceDraft:<wizard|editor>:<id|new:<class>>` as `{text, savedAt}` (drafts older than 30 days or without a
  readable `savedAt` are dropped on read and swept by `initialize()`), and a successful save must call `saved(mode)` before closing.
  Scripts and scenarios that cancel a dialog after changing fields must answer "Discard your changes?" (deny button).

## Features, endpoint access and SAML2 tools

- Palantir SAML2 metadata health (`PalantirSamlMetadataHealth`, palantir-services-wizard-saml2.js) is an applications menu
  item for SAML2 applications: it downloads through the cache endpoint's `/preview`, resolves through the cache endpoint
  itself, and reads certificate expiry in the browser; it never writes. There is no separate SAML2 metadata sources tab.
- Palantir's feature catalog (`palantir-cas-features.js`) groups `CasFeatureModule.<Feature>[.<module>]` names into one
  card per feature; `CAS_FEATURE_TITLES` holds titles the casing can't split. `CAS_FEATURES` stays the flat name list
  other scripts read.
- Endpoint access levels reach Palantir as `actuatorEndpointAccess` (endpoint id to `READ_ONLY`/`UNRESTRICTED`, resolved
  with the `EndpointAccessResolver` the REST mappings use). A control that sends anything but a GET carries
  `data-palantir-write="<endpoint id>"` (disabled by `applyPalantirEndpointAccess`, also when drawn later), a context
  menu item carries `write: "<endpoint id>"`, and a `$.ajaxPrefilter` refuses what slips through (status text
  `palantir-read-only`, which `displayBanner` ignores). Mark every new write control; several "read" actions are POSTs
  (`casConfig/retrieve`, `releaseAttributes`, `casValidate`, `serviceAccess`). Compare endpoint ids case-insensitively:
  Boot keys the links of CAS REST endpoints in lowercase (`ticketregistry`) while the access map uses the id
  (`ticketRegistry`); a case-sensitive match lets writes through.
- Feature catalog switches (`CasFeatureToggles` in `palantir-cas-features.js`): a feature with modules is switched
  through its modules' flags only, never `CasFeatureModule.<feature>.enabled`, which also governs the feature itself;
  store `CasFeatureModule.<feature>[.<module>].enabled`
  in a chosen dynamic source via `casConfig/update`; feature conditions are read at startup, so the catalog shows
  pending states from the env endpoint and asks for a restart instead of offering a refresh.

## CAS login

- Palantir's CAS login entry point: see the `cas-web-security` skill.
