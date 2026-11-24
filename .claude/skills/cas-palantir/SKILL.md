---
name: cas-palantir
description: "Change Palantir, the CAS admin dashboard (templates under palantir/, palantir-*.js and palantir-meridian.css): layout and themes, navigation, tabs, the command palette and keyboard shortcuts, dialogs and widgets (jQuery UI, MDC, DataTables, SweetAlert), escaping server values into markup, the applications table and cards, the Home overview, system metrics, zen mode and the chart magnifier, the service wizard, JSON editor, drafts and edit guards, feature toggles, endpoint access levels and SAML2 metadata tools. Use when touching Palantir UI code, its overview controller or its scenarios."
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
- Magnify any chart or SVG diagram with markup only: a button with `data-palantir-magnify="<target id>"` (optional
  `data-palantir-magnify-title`), or `data-palantir-magnifiable="Title"` on the target for a click (legend clicks and controls inside are left alone)
  (`PalantirMagnifier` in palantir-utils: Chart.js canvases are redrawn, SVGs zoom and pan).
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
- Only the scripts every page uses are `<script>` tags (theme, palantir, dashboard, endpoints, utils, settings, charts,
  discovery, features, command palette, hotkeys, edit guard). Every page's own scripts, Ace (minified build) and the
  diff library are bundles under `scripts` in the dashboard configuration, fetched by `PalantirScripts`
  (palantir-utils.js): `PalantirScripts.PAGES` names each page's bundles, `PalantirScripts.NEEDS` the bundles a bundle
  needs (a page with an Ace field needs `ace`; a page building dialogs with the wizard's field builders needs
  `fields`). A new page script goes in its page's bundle, never in the static list.
- Scripts share one global scope, so core code (and a page reaching into another page) never names another bundle's
  function directly: register tab initializers as `() => initializeX()`; answer "can this be offered" from
  `PalantirAvailability` (palantir-endpoints.js), not from the page's class; before calling into another page, open it
  with `initializeDashboardTab` or `await PalantirScripts.ready("bundle")`; when the other page only needs refreshing
  if it was opened, guard with `typeof x === "function"`. State or helpers several pages use (`registeredServicesById`,
  `applicationProtocolOf`, audit targets, `refreshCasServerConfiguration`) live in core. Page scripts run no code at
  load other than declarations, because they may load after `DOMContentLoaded`.
- Check cross-bundle references after moving code with a dependency analysis (espree and eslint-scope from
  `ci/tests/puppeteer/node_modules`): every reference from one bundle into another not in its `NEEDS` must be guarded,
  lazy, or behind a page init.
- In a `th:inline="javascript"` block, never write `[` directly before an inlined `/*[[...]]*/` (as in `[/*[[...]]*/]`):
  Thymeleaf reads `[/` as a closing element and the page fails to render. Put the value on its own line.
- A page whose link can name something (`PalantirLinks.DETAILS`) also clears it when its bare link is followed
  (`PalantirLinks.CLEARED`), so Back to `#users` shows no user; decide whether an application exists from its own
  read, never from the list read earlier.
- `PalantirPanelFocus` (palantir-utils.js) focuses the first usable text field of a page or inner tab when it is shown;
  don't add per-page focus calls on activation. Mark a better field `data-palantir-autofocus`, or keep a region out with
  `data-palantir-no-autofocus`. Esc in a page field blurs it. A scenario that presses single-key shortcuts after switching
  pages must press Escape (or blur) first.
- `initializeTableAndDialogAccessibility()` (palantir-utils.js) names every DataTable and its pagination, names icon-only
  controls from their `title`, marks modal dialogs `aria-modal` and names jQuery UI select menus from their label (jQuery
  UI points the label's `for` at the button). Tables with a row context menu get focusable rows (Enter clicks, Shift+F10
  opens the menu) through `initializeDataTableContextMenu`. jQuery UI tabs still trip axe's nested-interactive rule.
- The command palette (`palantir-command-palette.js`, Ctrl/⌘+K and `/`) reads its entries from the nav, the outer inner-tab
  row of each panel, `registeredServicesById` and a few actions each time it opens, so a new page or inner tab shows up on its
  own; it is a jQuery UI dialog with a categorized autocomplete, not another library. Ctrl+K, `/` and `?` are one document keydown
  handler in `initializeCommandPalette`: Ctrl+K works inside fields, `/` and `?` are matched on `event.key` (not
  hotkeys key codes) so they work on any layout and the numeric keypad, and are ignored while typing in a field.
  It reopens with the last query (selected) and marks the last chosen label, from `localStorage["PalantirCommandPalette"]`;
  the mark is applied once, in the autocomplete `open` event after `autoFocus`, only for the search the dialog opens with.
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

- Every label, caption, heading, tab, button, chip, switch label, column header, card title, dialog title and dialog
  button in Palantir is title case, including the ones built in JS and the step titles the access check endpoint returns:
  "Chain Several Strategies", "Look Up User casuser", "3 Applications Deleted". Short words (a, an, the, and, or, of, in,
  on, to, for, by, with, at, as, from, via, per, than, into) stay lowercase unless first or last; acronyms keep their
  capitals (SAML2, SSO, MFA); hyphenated words capitalize each part (Sign-On); particles of phrasal verbs are capitalized
  (Turned On, Look Up). Sentences stay sentence case: tooltips (`title` attributes), screen-reader-only text, help and
  hint text, placeholders, dialog body text, notes, toasts and long "Are you sure ...?" confirmations. When a string
  changes, update the puppeteer scenarios that assert it (`grep` the `palantir-*` scenarios).
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
- To send the user from one page to a lookup on another (an impersonation card's *Fetch Attributes* opening attribute
  resolution, `resolveSurrogateAccountAttributes` in `palantir-authn.js`): click the rail item, `await
  initializeDashboardTab(Tabs.X)`, select the inner jQuery UI tab, set the field and float its MDC label (the
  `--label-floating`, `--float-above` and `--notched` classes), then click the page's own lookup button.
- A page that shows one username's records (Throttles, Consent, Audit Events) uses the same form: the
  `passwordless-account-search throttles-username-search` form with a Username field, a filter button and a hidden
  *Show All* button, and a `throttles-username-hint` line that says what is shown and, when filtered, how many records
  the username has. Ask the endpoint for that username rather than filtering in the page, ignore a response that
  belongs to an earlier filter, and keep the filter across polling.
- MFA › providers (`PalantirMfaProviders`, `palantir-mfa.js`) reads each provider's settings from the `env` endpoint
  (first property source wins, so each setting shows once), defaults and descriptions from a configuration metadata
  search, registrations from the repository summaries and Duo reachability from `duoPing?providerId=`; secrets are
  masked by name and never matched by the filter. Duo settings are a list: the entry is the one whose `id` or `name`
  is the provider's, else `[0]`.
- Text fields never draw the `#dashboard :focus-visible` ring (one rule near the top of palantir-meridian.css): a
  field inside a rounded search wrapper shows focus on the wrapper with `:focus-within`, a bordered field changes its
  border color. A new search wrapper needs its own `:focus-within` rule.
- MFA provider settings are the standard DataTable (`#mfaProviderSettingsTable`, hidden group column with group rows
  like the configuration table), rebuilt by `renderDetail`, which destroys the previous instance first.
- Access Check (`PalantirAccessCheck`, palantir-authz.js; access tab): the `serviceAccess` endpoint answers
  `{allowed, reason, steps, service, registeredService, authentication}` for 200, 401 and 403 alike; steps are
  private `AccessStep` records of the endpoint (`id`, `title`, `outcome` PASSED/FAILED/SKIPPED/INFO, `detail`, `rules`, inner
  `steps`) built by its private `AccessExplainer`. The application field sends `client_id` (OAuth/OIDC),
  `entityId` (SAML2) or `service` from one field (`#accessApplication`); `PalantirAccessCheck.resolve` mirrors how
  CAS finds the application: OAuth/OIDC only by client id (case-insensitive), SAML2 only by entity id, and a plain
  service URL only among CAS applications, each in evaluation order, so a catch-all of one protocol never captures
  another's requests and `update()` shows it under
  the field. Suggestions are hidden once the value resolves. `PalantirAccessCheck.openFor(id)` backs the applications'
  *Check Access* menu item. The endpoint runs the enforcer once and then explains; strategies that call out or run
  scripts are never invoked again to explain. Every check returns only a summary of the application (no definition; the UI reads the definition from
  `registeredServices`), a failure to check answers 500 and is not a decision;
  `client_id` or `entityId` alone is enough to name an application. The sorted application list is cached per
  `registeredServicesVersion`; bump it after changing `registeredServicesById`. Recent checks live in
  `localStorage["PalantirAccessChecks"]`, never with passwords.
- Wizard option lists of types that live in optional modules filter on what the deployment carries: access strategies
  use `PalantirDashboardConfiguration.accessStrategyAvailable(className)`, fed by the dashboard model's
  `supportedAccessStrategies` scan; do not hard-code a module's strategy as always present.
- MFA providers resolve their type (`PalantirMfaProviders.typeOf`) from the `cas.authn.mfa.*.id` settings, then the
  fixed friendly name, then the default id; never derive a settings prefix or repository from the provider id alone.
  Repository summaries go through `PalantirCredentialRepositories.summary(repository, fresh)`, shared by the provider
  overview and the repository cards. Archive imports go through `BaseCasRestActuatorEndpoint.importArchive` and answer
  `{imported, skipped}`, with 422 when nothing was stored and 413 when the archive is too large.
- Duo account tools (MFA › Providers, Duo only): `duoAccountStatus` (selector-only, so its link may be listed as
  `duoAccountStatus-username`; `CasActuatorEndpoints.duoAccountStatus()` strips the template) and `duoAdmin` (GET user,
  POST `bypassCodes`, PUT user with status, firstName, lastName, email; writes carry `data-palantir-write="duoAdmin"`).
  State lives in `PalantirMfaProviders.duoAccounts` per provider so `renderDetail` can redraw; the username field keeps
  focus across redraws. Never create bypass codes or edit accounts in scenarios: the Duo test account is shared.
- Repository-wide tools (MFA › Multifactor Credential Repositories) are cards over the repository endpoints: counts come from a
  `?summary=true` request that answers `{records, users}` rather than from loading the records, so secrets such as
  Google Authenticator keys never reach the page; *Delete All* asks with plain Delete all/Cancel buttons (no typed name, Misagh); import
  sends a zip as `application/zip` and a single record as JSON.

## Escaping server values

- Markup that CAS sends to be shown as HTML (interrupt messages) goes through `sanitizePalantirHtml` (palantir-utils.js:
  inert parse, allow-listed tags without attributes, http/https/mailto links only); use `palantirHtmlText` for plain-text
  cells. Never put such markup in the page with `.html()` directly.
- Values from the server go into markup through `palantirCode(value)` (escaped `<code>`) or
  `escapePalantirMarkup`; never `<code>${value}</code>`. User agents, principals and attribute values are
  attacker-controlled.
- Text that comes from the server or from users, such as log messages, logger names and metric tag values, is put in
  the page with jQuery `text` or text nodes, or `escapePalantirMarkup` inside markup, never as raw HTML.
- There is one escaper, `escapePalantirMarkup` in palantir-utils.js; do not add file-local `escapeHtml` helpers.
  DataTables renders every column as HTML, hidden ones included: a column of plain text declares
  `render: palantirTextColumn` and its rows hold the raw value (so sorting, searching and group rows see the real
  text); a column that holds built markup escapes each value with `escapePalantirMarkup` or `palantirCode`.
- jQuery UI dialog titles (`title`, `dialog("option", "title", …)`) are set as text: never escape what goes in them,
  or `&` shows as `&amp;`. Swal is the opposite: `title` is markup, `titleText` is text.
- Descriptions from configuration metadata may carry markup: show them with `sanitizePalantirHtml` and search them
  with `palantirHtmlText`.
- Administrator checks in Palantir's Java use `PalantirAuthorities.isAdministrator`.
- Writes to the actuator and to Palantir made with the browser's session need the CSRF token (Palantir declares those
  paths through `getCsrfProtectedSessionEndpoints`). `PalantirCsrf` in palantir-utils.js adds `X-CSRF-TOKEN` to every
  same-origin `$.ajax` and `fetch` write; a form posted by script, such as Logout, calls `PalantirCsrf.addTo(form)`.
  A raw `XMLHttpRequest` write must set the header itself. Clients with an `Authorization` header are not asked.
- `/palantir/dashboard/session` answers only the signed-in name; never hand the session id to scripts.
- The wizard is split by what each part does, all loaded together by `PalantirScripts.load("applications")` in the order
  the template lists them: `palantir-services-wizard.js` (dialog, type picker, building the definition),
  `-fields.js` (inputs, selects, mapped values, validity; its own `fields` bundle, as other pages use it), `-release.js`, `-access.js`, `-policies.js` (the sections),
  `-populate.js` (filling from a definition) and `-suggestions.js`. The System page is split the same way:
  `palantir-system.js` (wiring and state) with `-startup.js`, `-dependencies.js`, `-http.js` and `-memory.js` holding
  functions that use no state of `initializeSystemOperations`. A new helper that needs that state stays inside it.
- Pop-ups go through `PalantirDialogs` (palantir-utils.js): `wait(title, text)`, `confirm({title, text, ...})` (whose
  answer has `isConfirmed`), `error(title, text)` and `done(title, text)`; titles are text. Use `Swal.fire` directly
  only for pop-ups with their own buttons or content, such as forms.
- Anything read again on its own goes through `PalantirPolling.every(name, run, {tab, panel, when, every})`: one clock,
  paused while the browser tab is hidden or the poll does not apply, one request at a time, a run as soon as it applies
  again, and the refresh interval read live from the settings. Never add a `setInterval` poller.
- Polled tables redraw with `draw(false)` so paging survives a refresh, and each poll is wrapped in `palantirOneAtATime`
  (palantir-utils.js) so a slow endpoint never gets a second request while the first is on its way; a reload after a
  change calls the unwrapped function. Every value in an actuator URL path or query goes through `encodeURIComponent`.
- Definition JSON is read with `palantirParseJson` and written with `palantirStringifyJson` (palantir-utils.js), never
  plain `JSON.parse`/`JSON.stringify`: ids above 2^53 are kept as their digits and written back as numbers. A field
  that produces such an id registers it with `palantirLargeId`; digit strings that were text stay text.

- Uploading SAML2 metadata to a metadata manager goes through `PalantirSamlMetadataUpload` (in
  `palantir-protocols.js`), shared by Protocols › SAML2 › Metadata Management and the wizard's Upload Metadata button
  beside the metadata location. The dialog markup lives in `protocolstab.html`, which only administrators get, so the
  wizard checks `PalantirSamlMetadataUpload.available()` before offering it. A managed application's metadata
  location is its manager's source id, such as `mongodb://`. Manage Metadata, beside it under the same condition,
  closes the wizard through its usual `beforeClose` question and calls `showSamlMetadataManager(sourceId)` on
  `dialogclose`; Keep Editing triggers `palantir:closecancelled` on the dialog, which drops that pending handler.
  `palantir-services-saml` covers both with a MongoDb metadata manager.
  A manager's `store` must throw when the document was not stored; the metadata endpoint then answers 500 with the
  reason and the wizard leaves the metadata location alone.
- Old drafts are redacted at start-up (`removeExpiredDrafts`, marked `secretsLeftOut`), not only when written.

- A Tom Select that accepts names it does not suggest (`create: true`), such as the configuration property name, sets
  `addPrecedence: true`, so Enter takes what was typed and a suggestion is picked with the arrows or a click; an exact
  match is still chosen. Search the id first (weighted `searchField`), and turn a typed but unchosen value into an
  option before `setValue`, which ignores values that are not options.

### Switches and checkboxes

- Every switch and checkbox in Palantir has one look: a pill track, `--palantir-toggle-off` grey and `--palantir-toggle-on`
  green, with a white knob (`palantir-meridian.css`, end of file). MDC switches keep their markup and classes, since
  scripts read and set `mdc-switch--selected` and `aria-checked`; only their drawing is overridden. Visible checkboxes
  are drawn the same way with `appearance: none`; checkboxes marked `palantir-sr-only` and `role="switch"` inputs with
  their own track are left alone. A new switch reuses the switchbutton fragment, an MDC switch or a checkbox, never a
  look of its own.

### Feature catalog

- A feature with modules is managed in one dialog (`CasFeatureToggles.manage`): a switch for the feature itself and one
  per module, each starting at what it will do after a restart. CAS reads a module's flag on its own
  (`CasFeatureEnabledCondition`), so turning the feature itself off writes its own flag and every module's. Only the
  flags that change are written. A module card and a feature without modules keep Turn On and Turn Off (`change`).

### Users page

- Each card is a source in `PalantirUserLookup.sources()`; a source with `counted: false` (the attribute release
  preview) shows no count and is left out of the summary. Token ids are bearer tokens: the tokens card keeps them in
  `PalantirUserLookup.tokens` and puts only an index and a masked id in the page.
- `oauthTokens` takes an optional `username`; never list every token in the browser to filter them there. The tokens
  card is `lazy`: it is read when opened, since the endpoint scans the ticket registry.
- `PalantirAttributeRelease` previews `releaseAttributes` (a POST, so it needs the endpoint writable) on the Users page
  and from an application's menu.

### Home

- Users page sources all load together; a source that would need a scan of every ticket gets a by-user lookup on the
  server (as `oauthTokens?username=` uses `TicketRegistry.query` by type and principal), never an on-request button.

## Applications table and cards

- Per-application menu actions go in the `applicationsTable` context menu items (palantir-services.js) with `write` naming
  the endpoint they change and `visible` deciding which applications get them, dispatched in `applicationActionHandler`;
  e.g. Rotate Client Secrets (`PalantirClientSecretRotation`, palantir-services-actions.js) for OAuth and OIDC apps.
  Actuator `@PostMapping` operations (such as oidcJwks rotate/revoke) must be called with POST, and a SweetAlert answer
  is read from `result.isConfirmed`, never from the result being truthy.
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
  `PalantirProgress`; Delete All is `DELETE registeredServices` (it removes stored definitions, so the Palantir
  scenarios never run it).
- `fetchServices` keeps the applications table's search when it reloads (it used to clear it, so disabling or deleting
  while searching lost the search, and scenario steps that searched right after a reload raced the reset). Scenario
  steps that rely on a row being drawn must search for it: the Palantir scenarios can have more applications than one page shows.
- Import posts the chosen file to `registeredServices/import` with the content type of its extension (JSON, YAML, or
  octet-stream for a zip, which the endpoint reads as a zip of JSON files); the file input's `change` handler is bound
  once in `initializeServiceButtons` (namespaced) and the input is cleared, so never bind it inside the button's click.
- Service history (`palantir-services-history.js`) is a jQuery UI dialog over the `entityHistory` endpoint: versions in
  `#entityHistoryTable` (latest first, row data carries `serviceId`/`revisionId`/`index`), a side-by-side diff drawn with
  the jsdiff webjar (global `Diff`) over JSON with sorted keys, the right side always the selected version; restore goes
  through `PalantirServiceHistory.restore` from the footer and the row context menu.
- `fetchServices` returns a promise that resolves with `true` once the table holds the applications, or `false` when
  they could not be read; never race it against a timer. After a change, call `PalantirApplicationSelection.reload()`,
  which tells the operator when the change was saved but the list could not be read again. Navigation to an application
  goes through `navigateToApplication(id)`, which marks its one row by id via `revealApplicationRow`; never search the
  table for an id or a name to find a row.

## Home, metrics and settings

- Home (tab 17, `PalantirHome`, palantir-home.js; `PalantirOverview` + `PalantirOverviewController` in the palantir
  module, `GET /palantir/dashboard/overview`): one summary, sections computed in parallel on virtual threads with a
  3 s budget, cached per section (stale value or `pending` when slow), only for exposed endpoints and for
  administrators beyond server/health/resources. Health runs each `HealthIndicator` bean with its own 2.5 s limit
  (`SLOW`), timed on its own thread; a check still running is reused, never restarted. HTTP and runtime numbers
  come from the `MeterRegistry`; recent application changes from the shared Javers history when installed
  (`PalantirApplicationChanges`, adapted in `CasPalantirServiceHistoryConfiguration`, `@ConditionalOnClass`, javers is
  `compileOnly`), otherwise from saved/deleted events kept per node in an `AtomicReference` list (no `synchronized`:
  Misagh's preference; no audit trail reads); internal services are skipped. Misagh removed attention, activity and login widgets.
  The MDC tab bar is positional, so Home's tab button is the 18th. A new visit lands on Home; a reload keeps
  `sessionStorage["PalantirSelectedTab"]`. Widgets can be reordered only (no hide/add/reset, Misagh's call).
  Widget buttons use `data-home-for`, never `data-home-widget` (that marks
  the widget); jQuery UI hijacks `draggable` in element props, so set it with `attr`.
- Home draws from `/palantir/dashboard/overview`, asking only for the sections its visible cards read. The Refresh
  button and each card's refresh action pass `fresh=true`, which skips the server's cached sections. A refresh keeps the
  cards that are already on the page and replaces only their bodies, and updates the health chart in place. The grid is
  rebuilt only when the cards shown change.
- The overview's health section lists health indicators and composite health contributors, one entry per bean, with the
  worst status of a composite's checks in the health endpoint's configured status order (CAS puts `WARN` first).
- The page and its cards read sections independently; `PalantirHome.apply` numbers each request and keeps a section only
  from an answer newer than the one it last came from, so a slow page refresh never overwrites a card refreshed since.
- The wizard check for settings it leaves out (`PalantirServiceEditor.paths`) follows lists item by item. Moving between
  the wizard and the JSON editor goes through `PalantirServiceEditGuard.handOver`: the source dialog's draft is
  removed only after the other dialog holds the edits, unsaved edits stay guarded there, and the saved definition the
  edit started from (for the conflict check) moves with them. `PalantirServiceEditor.carry` starts from what the wizard
  generated and adds back only settings no wizard field writes (`wizardSettings`); it goes inside parts whose type the
  wizard only chooses, and takes maps the wizard edits as a whole (`wizardMaps`) from the wizard, so removals and
  ordered maps stay.
- The wizard turns numeric-looking text into numbers only for fields that are not text settings (`isWizardTextSetting`):
  ids, names, secrets, keys and URLs stay strings. A numeric setting gets a `number` field.
- Swal titles with names or other outside text use `titleText`, never `title`, which is markup.
- Wizard contacts are built per contact tab (`wizardContactSlots`); contacts after the third are kept as they are.
- System metrics (`PalantirMetrics`, palantir-metrics.js): the metrics endpoint answers current values only; trends
  are sampled by the page at the refresh interval (rates per minute from successive COUNTs), breakdowns ask
  `metrics/{name}?tag=k:v` once per tag value (first 15). Pins live in `localStorage["PalantirPinnedMetrics"]`. Class
  names: the toolbar is `__toolbar`; `__bar` is a breakdown row.
- Zen mode (`PalantirZenMode`, palantir-settings.js; `html[data-palantir-zen]`, set before paint from
  `localStorage["PalantirZenMode"]`): the navigation is moved off screen, never `display: none`, because the page
  shortcuts and the search find destinations with `nav.sidebar-navigation ul li[...]:visible`.

## Service wizard and JSON editor

- Settings CAS reads from a chain itself rather than its members (the access strategy's `unauthorizedRedirectUrl`) are
  listed as `lift` on the chain kind: the fields show the chain's value and packing moves it back to the chain.
- Matching a draft's list entries to the saved definition (to restore secrets) never goes by position alone.
- Picking a registered application from a text field uses `PalantirApplicationSuggestions.attach` (the access
  check's menu), never a `<select>`. Wizard field suggestions go in `PalantirWizardSuggestions.FIELDS` (map keys as
  `path:key`), never a bare jQuery UI autocomplete; their lists are cached per wizard open by `choicesOf`.
- Chart.js copies axis defaults into each chart when it is made, so theme colors are given as functions
  (`PalantirTheme.chartColors`), never as values read once; code that needs the grid or text color reads
  `PalantirTheme.chartColors`, not `Chart.defaults`.
- Pages are named by their stable key (`Tabs.X.key`, rail `data-page`), not their index: show one with
  `PalantirPages.open(Tabs.X)`, check it with `PalantirPages.available`, and never click a rail item by
  `data-tab-index`. Links are `#<key>` and `#<key>/<detail>` (`PalantirLinks`); a page with details registers a
  handler in `PalantirLinks.DETAILS` and calls `PalantirLinks.shown(tab, detail)` when it shows one. Inner jQuery UI
  tabs are remembered by panel id (`palantirInnerTabIndex`).
- Code that runs on every wizard generation or every key typed must not walk all wizard fields or compile a pattern per
  application: cache what only changes with the fields or the application type (`PalantirWizardChains.modeled()`), read
  a section's options once per drawing, and compile service id patterns once (`PalantirAccessCheck.pattern()`).
- A nested policy chosen by type (delegated authentication, access strategy activation, consent) is a select whose
  renderer writes `{"@class": value}` plus fields carrying the type's short name as a class; its change handler calls
  `showWizardPolicyType(section, select)`, which shows that type's fields and, outside population, empties the rest
  and puts switches back. Give such selects an explicit `id` (the generated one keeps dots after the first) and never
  reuse a section header's id. Prove a new policy with a round trip through `/validate` in palantir-services-editor.
  A type with no fields of its own needs `data: {markerClass: true}` on its option, or generation drops the bare
  `{"@class"}`; options only some application types offer carry `serviceClass` and are added per type (SAML2 in
  `createSamlRegisteredServiceAttributeReleasePolicy`, OIDC in `createOidcRegisteredServiceFields`). Several fields can
  write one nested object (a persistent id generator's salt and attribute): each renderer returns the object with
  its `@class` and `mergeWizardPolicy` joins them; give each a `data("reader")` that picks its value out on populate,
  or the field shows `[object Object]`. When the object's type varies (the identifier generator), the reader notes the opened
  type and the renderer reuses it, forgotten on a type change, so saving never swaps the type.
  Map fields sharing a path across types (`accessStrategy.context`) are filled
  into the container carrying the policy's short class name.
  A type handler empties fields and calls `resetWizardSwitches(section)` only when `!isPopulatingWizard`: populate
  fills inputs before it sets selects, and each select's handler runs then, so an unguarded handler wipes what was
  just filled in. Never reset switches by clicking those whose `param-selected` is true; that turns default-on
  switches off. Two options must never share a value: populate picks the first.

- The service id field (`#registeredServiceIdFieldContainer`) is created in General and moved into the type's
  protocol section by `placeRegisteredServiceIdField` when the wizard opens; a protocol section holding it must not be
  advanced-only, or its required field drops out of validation.

- Suggested values for wizard text fields live in `PalantirWizardSuggestions.FIELDS`
  (`palantir-services-wizard-suggestions.js`), keyed by the field's dotted path, or `path:key` / `path:value` for the two sides of a
  map field; `list: true` completes comma-separated entries one at a time. A new field with standard values gets an
  entry there, not its own autocomplete; values come from the spec or the CAS model (check the Java setter accepts
  them), never `none` for JWS algorithms. They attach on focus, so map rows added later get them too.

- The service wizard's fields are built in the background once the applications are loaded and the browser is idle
  (`prepareRegisteredServiceWizardWhenIdle`; palantir-services-editor checks the `palantir-wizard-built` mark follows
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
  step is not shown (the `palantir-services-editor` scenario clicks them by key), and `activate(key)` leaves the review. The type is
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

### Drafts, comparisons and navigation
- Drafts of application definitions live in `localStorage` for up to thirty days, so they never hold secrets:
  `PalantirServiceEditGuard.writeDraft` replaces the values of secret settings (client secrets, passwords, keys,
  `jwks`) with a marker, also in text that is not valid JSON yet (`redactText`), and resuming a draft puts them back
  from the definition the dialog opens with. A new setting
  that carries a secret under a name the guard does not recognize needs adding to `SECRET_NAMES`.
- Unsaved-change detection and the history diff share `PalantirServiceHistory.sortKeys`: keys are sorted except in
  maps typed as ordered (`LinkedHashMap`), whose order is part of the definition.

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
- Chained policies in the wizard live in palantir-services-chains.js (`PalantirWizardChains`). Its `KINDS` table names
  each chainable setting (path, section, chain class, members key, combine key). The fields hold one member: the
  definition is unpacked (outer chains first) before `populateWizardFromServiceDefinition` and packed (inner first)
  in `buildWizardServiceDefinition`, which `generateServiceDefinition` now wraps. Switching members refills the whole
  wizard from the packed definition. A new chainable setting only needs a `KINDS` entry; members whose type the
  section's select lacks are edited as JSON.
