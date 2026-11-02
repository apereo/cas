---
name: cas-docs-site
description: "Edit the CAS documentation site (docs/cas-server-documentation, Jekyll plugins, site.js/site.css, ci/docs/publish.sh, the docs data generator) and release notes: page titles and descriptions, setting links, settings rules, release-notes front matter and tags, Quick Start wizard, sidebar, mobile layout. Use when editing any documentation page or release_notes/RC*.md."
---


# CAS documentation site

## Documentation site and release notes

- Documentation pages on gh-pages are pre-rendered per version, but they load the stylesheet and script from the shared site root, and every publish overwrites those root files. The current design ships as `stylesheets/site.css`, `stylesheets/site-print.css` and `javascripts/site.js`; the root `stylesheet.css`, `print.css` and `main.js` belong to the released versions' old markup. A redesign that changes page markup must use new asset names, never replace the ones older versions link.
- The documentation data generator (`docs/cas-server-documentation-processor`) runs as `java @build/casdocsgen.args ...`, an argument file written by its `docsGeneratorArguments` task from `sourceSets.main.runtimeClasspath`; `publish.sh` no longer builds the ~1 GB `casdocsgen.jar` boot jar. Its Gradle run adds `-DskipErrorProneCompiler=true` (override with `DOCS_GENERATOR_GRADLE_OPTIONS`, an empty value restores Error Prone). It still needs every CAS module compiled: actuators, feature toggles and shell commands are found by ClassGraph scans of the runtime classpath, and third-party settings come from the dependencies' metadata. Those lookups go through `CasDocumentationClassIndex`, one ClassGraph scan of `org` shared by the exporters; add new class lookups there rather than calling `ReflectionUtils`, which scans the whole classpath on every call.
- `ci/docs/publish.sh` exit codes carry meaning for the workflow retry (`retry_on_exit_code: 1`): 1 is a failure worth another attempt, 3 is broken internal links/images/scripts (fails at once, nothing is published), 4 is broken external links only (the site is published first, then the job fails). Keep new failure paths on 1 unless a rerun cannot help. External links are checked only on the weekly schedule or when asked (`--proof-external`, the `proofReadExternal` input or `vars.DOCS_PROOF_EXTERNAL`), with successful results cached for 7 days in `build/htmlproofer`.
- Actuator endpoint blocks (`_includes_site/actuators.html`) take their operations from the `cas_actuator_operations` filter in `_plugins/cas_actuators.rb` (sorted, parameters normalized, curl built there), and settings snippets from `cas_actuator_enable_snippet` / `cas_actuator_security_snippet` via `cas-actuator-snippet.html` in all three formats. Per operation, render only what differs (parameters, response, example); setup, security, settings and troubleshooting are rendered once per include in the shared tabs. The operations table is excluded from `responsiveTables()`.
- Settings search: `ci/docs/index.js` writes `assets/data/<version>/index.json` as `{generated, docs}` (no lunr index; each doc carries name, short type, description, default, kind required/optional/thirdparty, module, duration, deprecation). `site.js` searches it directly (`searchCasSettings`, shared by the Configuration Properties page and the Shift-Shift palette); the page state lives in the URL (`?q=&exact=1&scope=name&kind=cas|thirdparty&deprecated=hide`). The layout exposes `data-docs-base`, `data-docs-version` and `data-docs-build` on `<body>` for these URLs.
- Feature toggles (`Configuration-Feature-Toggles.md`) render through `_includes_site/cas-feature-toggles.html` from the `cas_feature_catalog` filter in `_plugins/cas_features.rb`, which derives feature and module from the `CasFeatureModule.<Feature>[.<module>].enabled` property, groups features by area and links docs only when the target page exists for that version. Add title overrides, groups or docs links there, not in the include. The environment variable form maps both `.` and `-` to `_` (`CasFeatureEnabledCondition` reads through `Environment.getProperty`), unlike the relaxed-binding form used for regular settings.
- Module dependency tabs (`_includes_site/casmodule.html`, included through `include_cached`) are plain Bootstrap tabs: no inline script, the default tab and pane are chosen together in Liquid, and pane ids use the full coordinates plus the variant options. Do not add per-include scripts or truncated ids; either one lets the selected tab and the visible pane drift apart.
- Local serving (`publish.sh --serve true`) keeps the configured `baseurl: /cas`, so the site answers at `http://localhost:4000/cas/<version>/` exactly like production. `variables.html` always sets `basePath` to `/cas` and `site.js` has no localhost path branches; do not reintroduce `--baseurl ""` or localhost-specific paths.
- Release notes (`release_notes/RC<n>.md`) carry a `release:` front matter block (summary, facts, upgrade notes, highlights, optional spotlight over a front matter image list) and open with `{% include release-digest.html %}` and end with `{% include release-footer.html %}`. Tag New & Noteworthy topics with a block IAL on the line after the heading and Other Stuff bullets with a list item IAL (`- {: .fixed} ...`): types are `new`, `changed`, `fixed`, `action`, `removed`; add `data-area="..."` only when the first link does not identify the area. The area must be a key of `AREAS` in `_plugins/cas_release_notes.rb` (SSO cookies and sessions are `tickets`; there is no `sso`). `_plugins/cas_release_notes.rb` normalizes the tags, groups Other Stuff by area and resolves `section:` names to kramdown GFM heading ids before rendering; a `section:` that names no heading logs a warning. Upgrade notes may point at an Other Stuff area group by its label. `release_notes/RC.md` (front matter `release_aggregate:`) is generated from every RC page in the same folder: topics with the same title are merged and show each release candidate's notes, identical Other Stuff items are listed once with all their release candidates, and the `<!-- release-changes -->` marker is where the generated changes go. Never edit changes into that page by hand.
- Section heading icons come from `CAS_SECTION_ICONS` in `site.js` and apply only to direct `h2` children of the article whose id is in that map; add an entry there rather than icons in markdown.
- In the development docs, `site.js` wraps every table inside `#cas-docs-container` in a framed `.table-scroll` card, so do not use `<table>` for layout inside components (buttons, headers, badges); use flex markup instead.
- Documentation property blocks get their settings from the `cas_properties` / `cas_third_party_properties` filters in `docs/cas-server-documentation/_plugins/cas_properties.rb`; do not loop over `site.data` in Liquid for this, it scans ~14k entries per block. Liquid `assign` inside an include writes to the page scope, so a `casproperties` include nested inside another block's panels must pass `topics="false"` (and usually `intro="false"`) or it resets the outer block's state. Their output is wrapped in `{::nomarkdown}`, so include them at the start of a line in markdown or pass the capture through `markdownify` (with `|`, not `||`). Catalog descriptions carry raw `<`/`>` (for example `management.endpoint.<id>.access`); the plugin escapes every tag outside `INLINE_TAGS` into `descriptionHtml` / `summaryText`, so print those fields and never unescape descriptions in Liquid: one stray tag inside the actuator modal makes kramdown drop the closing tags and everything after it on the page.
- Documentation page titles must be unique: use `title: CAS - <page H1>`, and give overview pages a `description:`
  front matter entry (under 160 characters). Pages without one get their meta description from the first paragraph in
  `_layouts/default.html`, which skips dependency boilerplate, short or colon-ended lead-ins and "see this guide" lines.
- The Versions menu lists `documentation_versions` from `docs/cas-server-documentation/_config.yml` (development plus
  maintained releases); update it together with the EOL schedule in `developer/Maintenance-Policy.md`.
- Third-party CSS and JS in `_layouts/default.html` are pinned to exact versions with `integrity` (sha384) and
  `crossorigin="anonymous"`. Recompute the hash whenever a version changes; Google Fonts and the gtag script cannot carry one.
  Analytics is the GA4 tag `G-W3M8JXP7GP`, loaded only on `apereo.github.io`.
- Do not dim muted text with `opacity`: `--docs-muted` on its own meets 4.5:1 in both themes, and opacity drops it below.
  `--docs-amber` is darker in light mode for the same reason.
- In `casmodule.html`, only the tab links sit inside `role="tablist"`; the Resources dropdown is a sibling `<button>`.
- Mobile layout: content must never run past the 1.25rem gutter. A flex or grid child that holds long inline `code` needs
  `min-width: 0`, and the code needs `overflow-wrap: anywhere` (`break-word` does not lower the min-content width, so the
  item still overflows). Bootstrap `.row` blocks in content lose their negative margins below 760px. To check, load pages in
  375px iframes and look for elements whose right edge passes `#cas-docs-container` outside a scrolling ancestor.
- Release notes pages carry `docs-release-notes` on `<body>` (set in `_layouts/default.html` from the page URL); their prose,
  footer included, is justified and hyphenated, while stats, facts, the release switcher and inline code are left alone.
- Docs sidebar: `sidebar.md` stays a plain nested list; `buildSidebarTree()` in `site.js` turns it into the grouped tree. A
  section's group comes from `CAS_SIDEBAR_GROUPS` (by the section's `#anchor`), so add a new top-level section there too; one
  left out lands in the last group. A parent whose first child is an `Overview` link becomes that link (the row is dropped);
  other parents only toggle. Only the current path opens, siblings close as another opens, open rows are sticky at
  `--sidebar-filter-h + depth × --sidebar-row-h`, and `.docs-sidebar` has no top padding so the filter can pin at the very top.
- Settings in docs (required, standing rule from the maintainer): never refer to a configuration setting by name in
  documentation pages, neither inline nor in fenced `properties` blocks. Describe the feature, say that it is available,
  and say that it is enabled, disabled or tuned through CAS settings; the page's `casproperties` include lists them. The
  only exception is release notes, which may name CAS or other settings.
- Setting links in release notes (required): every setting named in release notes must be marked so readers can look
  it up in place. Inline, write `` `cas.server.name`{: .cas-setting} ``
  (an assignment such as `` `cas.tgc.secure=false`{: .cas-setting} `` works too; the value is ignored for the lookup). For a
  fenced `properties` block of settings, put `{: .cas-settings-linked}` on the line right after the closing fence. Mark only
  real CAS or Spring setting names, not prefixes used as headings, removed settings, JSON fields or registered-service
  properties. `initializeCasSettingLinks()` in `site.js` turns these into buttons that call `openCasSettingsPalette(name)`:
  an exact match opens the setting's details inside the Shift-Shift palette, a partial name shows the matches; the page
  never changes. Linked blocks are rebuilt line by line from their text (key, separator, value), because Rouge's
  properties lexer splits keys with `[0]` into separate tokens. `casSettingsFlat()` drops anything in brackets and any
  purely numeric name segment, so `[0]`, `[]`, the catalog's map notation `.[key]`, `.0.` and the environment variable form
  `_0_` all match the same setting. No catalog name has a numeric segment of its own; recheck that if one is ever added. `planning/Quick-Start.md` is the reference page.
- Quick Start wizard (`planning/Quick-Start.md`, `javascripts/quick-start.js`): the page needs exactly eight `## N. ` steps;
  anything else on the page is an `###` inside one of them, or it shows on every step. `data-quick-feature` takes a
  space-separated list and shows the section when any listed feature is selected. Each checkbox's `data-module` must match
  the `casmodule` include in its recipe (the fieldset's `data-modules` holds the always-included ones): step 2's
  Initializr command and step 6's `dependencies` block are built from them, and step 6's `cas.properties` from every
  visible `.cas-settings-linked` block in steps 3–5, in page order, under the nearest heading. Generated blocks are
  plain fenced blocks inside `[data-quick-output]`; the script rewrites their text and gutter and calls
  `linkCasSettingsBlock()` from `site.js` again. The selection is page state: popstate restores the step only, and a link
  into a recipe selects its first feature with a notice. Checkboxes may declare `data-requires` and `data-excludes`
  (space-separated feature values; Interrupt requires Groovy, Redis and Hazelcast exclude each other, declared on both
  sides): checking selects requirements and clears conflicts, clearing a feature clears its dependents, Check All adds
  only what fits, and a selection from the address is normalized with earlier features winning. Test the wizard in
  headless Chromium against a rendered copy (Back/Forward, deep links, rules, Check All output) after changes.
- Every documentation page needs an opening paragraph right after the H1 that the layout can use as its description:
  at least 40 characters, not ending in a colon, and not a "see this guide" or dependency lead-in.
- On phones (`max-width: 760px`) the sticky header is offset by `--docs-masthead-height` so only the navigation bar
  stays visible, and `site.js` sets `--docs-header-height` to that bar alone; keep both in step when changing the masthead.
- The docs load two web fonts only, Bricolage Grotesque (headings) and DM Sans (everything else); `--font-mono` is the system
  monospace stack. Do not add font families or hotlink images from third-party hosts; keep images under `images/`.
- Large topics are split into one page per concern with a sidebar submenu (see `authorization/Heimdall-Authorization-*.md`
  and `multitenancy/Multitenancy-*.md`); when moving a section, update cross-page anchors and links in release notes.
- JSON examples pasted into the documentation are pretty-printed (one member per line, two-space indent), never minified.
