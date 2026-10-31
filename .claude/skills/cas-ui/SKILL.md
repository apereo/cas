---
name: cas-ui
description: "Change CAS views and presentation: Thymeleaf render path, theme resolution and theme sources, templates and escaping, the theme builder, account profile pages, .cas-hero cards, Palantir admin UI, previewing views without a running CAS. Use when touching templates, static CSS/JS, themes or Palantir."
---

# CAS views, themes and Palantir

## View and presentation layer

- Entry points: `support/cas-server-support-thymeleaf` (auto-configuration),
  `support/cas-server-support-thymeleaf-core` (resolvers, views, dialect),
  `support/cas-server-support-themes-core` (theme resolvers and theme sources),
  `core/cas-server-core-web-api` (`ResponseHeadersEnforcementFilter`, `AbstractCasView`),
  `support/cas-server-support-validation-core` (protocol response views).
- The render path is: request → `ChainingThemeResolver` → `ChainingTemplateViewResolver` →
  delegate template resolvers → `SpringTemplateEngine` → `ThemeBasedViewResolver` /
  `ThymeleafViewResolver`. Review it as a path; the individual classes look correct in isolation
  and the defects live at the seams (what the chain discards, what the cache key omits, what the
  theme name is allowed to be).
- Theme names come from a request header and a cookie by default and are never validated. Any new
  consumer of `ThemeResolver.resolveThemeName` must assume hostile input.
- Caching flags set on a delegate resolver mean nothing if the enclosing chain is not cacheable.
  When changing cache behaviour here, confirm the effect at `TemplateManager`, not at the bean.
- Protocol response views: CAS 2.0/3.0 XML escape through `escapeXml10`; mustache `{{ }}` escapes
  and `{{{ }}}` does not. CAS 1.0 and per-line attribute renderers are plain text with no
  structural escaping — validate values rather than relying on the template.
- Templates: use `th:text` for model data and reserve `th:utext` for `#{...}` message codes.
  Thymeleaf JS inlining (`th:inline="javascript"`, `[[${...}]]`) is safe; string-concatenating a
  model value into a `<script>` body or into `innerHTML` is not.
- Puppeteer scenarios that cover this layer: `themes-per-service`, `themes-external-per-service`,
  `themes-collection-twbs-login`, `themes-collections-example`, `thymeleaf-templates-external`,
  `thymeleaf-templates-rest`, `cas-validation-protocol-v2`, `cas-validation-protocol-v3`,
  `pm-account-profile`. Re-run these for any change to theme resolution, template resolution or
  static-resource registration; `thymeleaf-templates-external` asserts that the configured
  template prefix directory is reachable over HTTP.
- Test view components through `MockMvc` and the existing web-test infrastructure; use Lombok
  (`val`, `@RequiredArgsConstructor`, `@Slf4j`) and current Java features as the rest of the tree does.
- Account profile and account-management flows share `static/css/account.css` and `static/js/account.js`. The profile is a
  rail (`#navigationMenu`, one `#linkXxx` per `#divXxx` panel, `data-account-panel`) plus card lists with inline details;
  the flows (sign-up, password reset, forgot username, must-change/expired password) render through
  `fragments/accountflow :: shell(flow, step, steps, content)`, which owns the step trail (`screen.account.flow.<flow>.*`)
  and keeps `h2`/`p` out of the brand panel so `#content h2` and `#content p` still hit the card. The strength meter
  markup lives in `fragments/accountflow :: strengthMeter`; keep its ids, `passwordMeter.js` depends on them.
- A module adds to the account profile through `AccountProfileContributionWebflowConfigurer` (core-webflow-api):
  `withRenderAction(id)` for data the profile shows, `withOperation(eventId, actionId)` for a form the profile posts (its
  action state returns to the profile view on success and on error), `customizeAccountFlow` for extra views. Register it with
  its own `CasWebflowExecutionPlanConfigurer` in a configuration guarded by the `AccountManagement` feature. Every profile
  operation that takes an id from the request (session, token, device, scratch codes, rename) must check that the record
  belongs to the signed-in principal before touching it; return `error()` and leave the record alone otherwise. Views reused
  from the login flow post to `${activeFlowId == 'account'} ? @{/account} : @{/login}` and offer a cancel link to
  `@{/account}` only in the account flow. Account flow views render at `/cas/account` with no `execution` parameter, so a
  link to `/account#divXxx` only changes the fragment and never leaves the view; `account.js` reopens the last panel. `data-account-action="confirm-submit"` on a `type="button"` inside a form asks
  with SweetAlert (`data-confirm-*`) and submits the form. Scratch codes and other secrets shown once go to the flash scope.
- Puppeteer selectors for these pages are list-based now: `#mfaDevicesList [data-field=source|id|name|type|model|number]`,
  `#mfaDevicesEmpty`, `#registrationOptions`, `#securityQuestionsList`, `#pwdmain h2`, `#reset #fm1 h2`. Do not
  reintroduce DataTables there; filtering and paging come from `data-account-list`.
- To try template changes without rebuilding the war, run the built war with
  `spring.thymeleaf.prefix=file:<module>/src/main/resources/templates/`, static locations pointing at the module's
  `static/`, the message bundle at the module's `messages`, and template caching off.
- WebAuthn and consent screens use `.cas-hero` (centered card with `.cas-hero-icon`) and `fragments/consent.html`
  (`appHeader`, `permission`). Puppeteer relies on `#cibaContainer h1` reading the CIBA header text, `#status` holding the
  WebAuthn title, and the ids `#allow`, `#deny`, `#cancel`, `#scopes`, `#userInfoClaims`, `#informationUrl`, `#privacyUrl`,
  per-scope/claim ids, `#registerButton`, `#registerDiscoverableCredentialButton`, `#residentKeysPanel`, `#device-*`; keep
  them. Iterate with `th:block th:each` around a `th:replace` (replace runs before each on the same element), and index SpEL
  maps with `.get(key)`: `map[key]` treats `key` as a literal. Global `.mdi:before` pins icons to 20px, so size icons on `::before`.
- Interrupt and attribute consent use `.cas-hero.cas-hero-start` with a `.cas-hero-head`. Puppeteer reads `#content h1`/`#content h2`
  and the first `#content p` as the title and intro, so keep the heading first and no `<p>` before the intro. `div#content`
  carries a global shadow; hero cards that are `#content` themselves override it. `MDCTabBar` focuses the tab it activates, so
  initial activation (material.js, consent.js) runs with `focusOnActivate` off.
- Every page card is a `.cas-hero`: a `<span class="cas-hero-icon [is-danger|is-warning|is-success]">` with an `mdi` icon goes
  first, then the existing heading. Use `cas-hero-start` for forms and lists, `cas-hero-wide` for tables. Never add a heading or
  `<p>` before the existing ones and never remove a wrapping `div`: puppeteer asserts `#content div h2`, `#content h2`,
  first `#content p`, `#login h2/h3/p` and `#main-content h2`. Banner root cards turn neutral under `.cas-hero`. Login gets
  `.cas-login-card` (surface only).
- Palantir's layout and themes live in `palantir-meridian.css` (loaded after `palantir.css`): `--palantir-*` tokens are defined
  for light and for dark on `html.palantir-page[data-theme]`, and the shared `--cas-ui-*`/`--cas-theme-*` variables are pointed
  at them inside Palantir. Color new rules with the tokens (`var(--palantir-surface, #fff)` in `palantir.css`), never a bare
  light color; where the brand color sits behind white text, dark needs `--palantir-brand-fill`. The page head applies the
  stored theme (`PalantirTheme` in localStorage: system, light, dark) before first paint; `palantir-theme.js` owns the toggle,
  the settings choice, Chart.js defaults and the `palantir:theme-changed` event. Never set `Chart.defaults.borderColor` or
  `backgroundColor`: Chart.js only applies its dataset palette while both keep their stock values; theme the grid through
  `Chart.defaults.scale.grid.color` and `Chart.defaults.scale.border.color`.
- Palantir forms: Enter submits through the form's primary button, which is `type="submit"` while the form keeps
  `onsubmit="return false;"` (the button's click handler does the work); every dashboard form needs that attribute, since a
  form with one text field and no handler reloads the page on Enter. Don't add per-input Enter key handlers.
- The service wizard's fields are built on first open (`palantirWizardField` builders), after the Applications panel ran
  `initializePalantirWidgets`; `buildRegisteredServiceWizardFields` initializes the form's widgets afterwards, so dropdowns
  get the change callback that runs their `data-change-handler` functions. A bare `.selectmenu()` has no such callback.
- Palantir date fields use the jQuery UI date picker: give the input the `jquery-datepicker` class and, for a format other
  than the picker's default, a `dateFormat` data value (`createInputField({data: {dateFormat: "yy-mm-dd"}})`; quoted text is
  literal, e.g. `yy-mm-dd'T'00:00:00` for a date-time field). `initializeDatePickers` attaches it and fires `input` on pick.
  The picker's `#ui-datepicker-div` is raised above dialogs in `palantir-meridian.css`. Don't add another date library.
- `generateServiceDefinition` leaves out fields whose container is switched off, judged by `isWizardContainerSwitchedOff`
  (inline `display: none`, or the `hide` class with no inline display), since type handlers reveal fields with either
  `showElements` or jQuery `show()`. Don't judge it by `:visible`: the wizard hides every section but the current one.
- Palantir's navigation (`fragments/palantir/navigationsidebar.html`) is a top bar: Applications, grouped menus (Identity,
  Sessions, Platform) and the theme, settings and sign-out buttons. Only destinations may be `<li>` (scripts treat every
  `nav.sidebar-navigation ul li` as one and count them with `:visible`; polling also stops when a destination is not
  `:visible`), so closed menus and the narrow-screen overlay hide with `visibility`, never `display: none`.
  `updatePalantirNavigationGroups()` hides empty groups, marks the active one and writes the page heading.
  A row of inner jQuery UI tabs with one visible tab is clipped out of sight, not hidden, for the same reason.
  DataTables group header rows come from `palantirTableGroupRow(label, columns)` (palantir-utils.js); never inline-style them.
- Palantir applications: the card view (`#applicationsCards`) is drawn from the DataTable's filtered current page on every
  draw and when a row's class changes, so search, paging and the selected row apply to both views; card buttons call
  `applicationActionHandler(key, context)`, the same handler as the row context menu, with `applicationRowContext(id)`.
- Palantir's service wizard (`palantir-services-sections.js`) maps section groups to `STEPS` (Basics, Protocol,
  Access & attributes, Policies) plus a review. Every available section keeps a tile with `data-section-key` even when its
  step is not shown (the `palantir-login` scenario clicks them by key), and `activate(key)` leaves the review. The type is
  chosen in `chooseRegisteredServiceType()` (SweetAlert with cards), not a select. Every Policies section is an
  `advanced-option`: `advancedSectionsOf(step)` keeps that step clickable while advanced options are hidden, and
  `showStep` turns them on through `#hideAdvancedOptionsButton`. Footer buttons are in DOM order (Cancel, Validate, Copy,
  Save); don't reorder them with CSS `order`, which leaves the keyboard order behind.
- Palantir's feature catalog (`palantir-cas-features.js`) groups `CasFeatureModule.<Feature>[.<module>]` names into one
  card per feature; `CAS_FEATURE_TITLES` holds titles the casing can't split. `CAS_FEATURES` stays the flat name list
  other scripts read.
- To look at Palantir without a running CAS: render `palantir/casPalantirDashboardView` with Thymeleaf, the layout dialect,
  OGNL and a stub `#cas` expression object (jars from a built war; SpEL `?:` inside `${}` and `T(...)` need rewriting for
  OGNL), serve `static/` with the webjars (npm for those not in the war), and stub the actuator URLs in Playwright.
- Palantir dialogs sit outside `#main-content`, so `applySharedDialogStyle()` (palantir-utils.js, called from
  `initializePalantir`) adds `cas-ui palantir-dialog` on `dialogcreate`; style dialogs through those classes, qualified enough
  to beat the Cupertino theme (`.ui-dialog.palantir-dialog …`). Use the `classes` option, not the deprecated `dialogClass`.
  jQuery 4 dropped `isWindow`/`isFunction`, which the jQuery UI 1.12 position plugin shipped with contextMenu still calls and
  which replaces jQuery UI's own position: `installJQueryCompatibility()` must run before any dialog, menu or tooltip opens.
  Tom Select wrappers carry `z-index: 10000`; open select menus are raised above them.
- Palantir's CAS login entry point: see the `cas-web-security` skill.
- Header panels (`fragments/header.html`, `fragments/logindrawer.html`) carry `.cas-ui` and serve both themes: MDC
  (`MDCDialog`, modal `MDCDrawer` with its `.mdc-drawer-scrim` as the next sibling) and Bootstrap (`modal`, `offcanvas`), so
  keep both class sets. The notification count is the number of `.cas-notification-message` items not `[hidden]`; an item
  shown client-side starts `hidden`. The twbs theme does not load `cas.css` and styles them in its own `cas.css`. Puppeteer
  uses `#drawerButton`; `#drawerCloseButton` and `#cas-notifications-menu` are wired in `material.js`.

## CAS protocol (v1/v2/v3 + SAML 1.1) review discipline

- Theme builder (`fragments/themebuilder.html`, `static/js/theme-builder.js`, `static/css/theme-builder.css`): on when the
  theme property `cas.theme-builder.enabled` is true (default). Its property list is `#cas.themeBuilder`
  (`CasThymeleafTemplatesDirector`), parsed from the comments and declarations of `cas-theme-default.properties`, so a new
  theme property needs its comment there and shows up in the builder (and the docs) on its own; give it a preview in
  `PREVIEWS` or a `data-cas-theme-builder-toggle` element, otherwise it is marked "after deployment".
- A theme property that removes a presentational element must, for the builder, render it while `#cas.themeBuilderEnabled`
  with `th:classappend="... ? 'cas-theme-builder-off'"` and `data-cas-theme-builder-toggle="<property>"`. Never do this for
  form inputs or scripts (fingerprinting, remember-username, public workstation, warn-on-redirect): those stay deploy-only.
- Static resources are content-versioned (`custom-<md5>.css`), so match file names with an optional `-[0-9a-f]{32}` suffix.
  Logout clears session storage, which drops the builder draft. Puppeteer: `#cas-theme-builder-menu`,
  `#cas-theme-builder.is-open`, `#ctb-tab-*`, `#ctb-prop-<property with dots as dashes>`, `#ctb-style-<variable>`,
  `#ctb-output-properties`, `#ctb-output-css`, `#ctb-reset`, `#cas-theme-builder-close`; the panel slides out, so wait
  before asserting it is gone. `login-success` and `template-accessibility` cover it.
- `cas.main-content.position` (3x3 grid, default `topcenter`) works through auto margins on `#main-content` (vertical)
  and on `#content` plus the page's root element (horizontal), not `align-items`/`justify-content` on the scrolling
  wrapper: auto margins collapse when a page is taller than the screen, so it still scrolls from the top. Pages render
  `main > div#content > <page root>`; most roots are the card with Bootstrap `m-auto`, so overrides need `!important`.
- Enumerated theme properties need their values in `CHOICES` (theme-builder.js) so the builder offers a dropdown; layout
  variants are a container class (`cas-providers-<layout>`, `cas-mfa-layout-<layout>`, `cas-consent-list`,
  `cas-scratch-codes-list`) styled in cas.css, never different markup, so scenario selectors keep working and the
  builder can preview them by swapping the class. Bootstrap utilities (`w-100`, `flex-column`, `mmw-15`) win with
  `!important`/max-width, so drop them from markup a layout must override.
- Java code that needs a theme property outside templates resolves it like `QRUtils.generateQRCode(requestContext, ...)`:
  the `themeResolver` and `themeSource` beans from the flow's application context, falling back to defaults when absent.
- To preview a view without its feature module (MFA, consent, delegation), render it with `SpringTemplateEngine`, the
  layout dialect, a stub `#cas` expression object, a link builder that returns `/cas` as context path and a proxied
  `IThymeleafRequestContext` (bind status from the model) so `th:field`/`#fields` work; serve the output from
  `static/themes/` of a running CAS so `/cas/...` assets resolve.
- Theme sources (`support/cas-server-support-themes-core`): every theme resolves `<theme>` (template-prefix file or classpath
  bundle) → `cas-theme-custom` → `cas-theme-default`, for the default, aggregate, per-service and per-tenant themes alike.
  Files read from template prefixes go in as `StaticMessageSource#setCommonMessages`, never `addMessage` per locale: the
  lookup uses the browser's `Accept-Language`, and per-locale entries silently drop the file for other languages.
- No English in templates or inline scripts: use `#{...}` for markup and natural JS inlining (`/*[[#{key}]]*/ "fallback"`) in
  `th:inline="javascript"` scripts; substitute client-side values with `.replace("{0}", () => value)` (function form, so `$`
  in the value is literal). `webauthn.js` reads its strings from `window.casWebAuthnMessages`, set by
  `fragments/webAuthnMessages :: messages`, which every template must include before loading the script.
- Random values in the UI (passwords, client secrets) come from `crypto.getRandomValues` with rejection sampling, never
  `Math.random()`. Page-level CSS tokens (`--acct-*`, light `--palantir-*`) derive from `--cas-ui-*` / `--cas-theme-*` so the
  theme builder and `custom.css` reach them.

## FIDO2 WebAuthn / passkeys review discipline

- Screenshots of themed views without a running CAS: stage the template, `cas.css`, logo and background via `build/`,
  install `normalize.css`, `bootstrap` (grid), `material-components-web`, `@mdi/font` from npm at the versions in
  `libs.versions.toml`, rebuild the layout shell (header, `.bgimage` main, footer) and capture with Playwright.
  The layout's `div#content` carries a shadow, so narrow cards use another fragment id.
