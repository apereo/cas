# AGENTS.md

## Heimdall / AuthZEN review discipline

- Check the latest OpenID Authorization API final specification and editor's draft. AuthZEN is a PEP/PDP
  protocol; wallet exchange uses OID4VCI/OID4VP. Treat a verified-presentation-to-policy bridge as a feature.
- Trace `DefaultAuthorizationPrincipalParser` through `DefaultAuthorizationEngine` and each policy.
  A valid caller token does not establish PDP audience, PEP permissions or DPoP proof possession;
  delegated subject ids are legitimate for trusted PEPs and are not inherently impersonation.
- An AuthZEN `resource.id` names the resource instance, never a policy namespace. AuthZEN requests
  (`AuthorizationRequest.isAuthZen()`: subject, resource and action all present) match `resourceType`, `actions` and an
  optional full-match `resourceIdPattern` across every namespace, and every match must grant. Normal requests
  keep namespace + URI pattern + method and `/heimdall/authorize` rejects AuthZEN fields, which keeps the paths apart.
- Callers authenticate with tokens issued to a registered OAuth/OIDC service whose access strategy allows
  access; `HeimdallRegisteredServiceAccessStrategy` (alone or chained) can refuse it. DPoP and `x509_digest`
  certificate bindings are enforced. `x509_digest` is the RFC 8705 `x5t#S256` thumbprint (base64url SHA-256 of the
  DER certificate); compute it only with `OAuth20Utils.computeCertificateThumbprint` so issuer and verifiers agree. On AuthZEN, Basic means `client_id:client_secret` (reject clients without secrets:
  `DefaultOAuth20ClientSecretValidator.validate` returns true when none is defined); on `/heimdall/authorize` it
  stays CAS user credentials, split on the first colon. Both endpoints are throttled; `HeimdallThrottledHandlerInterceptor`
  forwards only 401s to the throttle interceptors' post-processing. A `ThrottledRequestFilter` cannot limit what
  counts: the default `httpPost()` filter claims every POST and the plan combines filters with `anyMatch`, and the
  interceptors record every non-2xx response (twice: `postHandle` and `afterCompletion`; harmless for decisions,
  since stores keep the latest failure per key or read the authentication audit trail).
  Create registry-backed single-use markers through the `TicketFactory` (TST factory, custom `ExpirationPolicy` in
  a *mutable* properties map: `buildExpirationPolicy` removes that key, so `Map.of` throws), never by instantiating
  ticket implementations.
- macOS JDKs use a polling `WatchService` (2s): a file created and deleted between two polls produces no event.
  Watcher-driven caches must reconcile against the file system on every event rather than trust event kinds. JWT assertions need `jti`/`iat`, are single-use,
  and share the token endpoint's audiences on purpose. A resource without policies denies.
- JDBC trusted parameters (`principal`, `method`, `uri`, `namespace` and the AuthZEN names) are added after
  context and attributes, so they already win; an overwrite claim there was a false finding. Check that policy
  file deletion removes cached grants. `enforceAllPolicies=false` means any one policy grants: a failing policy is
  logged and skipped, and failures are rethrown only when nothing grants (never turn them into a silent deny).
  `enforceAllPolicies=true` stops at the first denial or failure. An empty policy list denies before either runs.
- JDBC policies resolve their `DataSource` by bean name (`dataSourceName`, else `heimdallJdbcDataSource-<sha256(url|username)>`)
  and register a `JpaBeans.newPoolingDataSource` pool (Hikari defaults, `minimumIdle=0`) through
  `GenericApplicationContext.registerBean` under a lock, so Spring closes it on shutdown. Never cache a pool on a
  policy instance (policies are rebuilt on every file reload) and never use `registerSingleton` for closeable beans:
  Spring gives registered singletons no destruction callbacks. The pool key ignores the password (a rotated password
  needs a new `dataSourceName` or a restart; documented). `queryTimeout` (default `PT5S`) is set on the `JdbcTemplate`. Tests override `resolveApplicationContext()` with a
  local context rather than relying on the static `ApplicationContextProvider`, which parallel tests share.
- `JsonAuthorizableResourceRepository` publishes one immutable `ResourceIndex` (by namespace and by AuthZEN
  `resourceType`) per reload; AuthZEN lookups use the type index, so keep both maps in the same snapshot.
- Policies evaluate sequentially; do not reintroduce `parallelStream()` (blocking policies starve `commonPool`).
- AuthZEN subjects are resolved through the principal resolver only for subject type `user` (hardcoded);
  other types become a bare principal. Policies read request data through
  `AuthorizationRequest.resolveAttributeValues` (qualified `subject.*`, `resource.*`, `action.*`, `context.*` names);
  never merge caller-supplied properties into principal attributes, which would let a PEP override the directory.
  AuthZEN `context` is body-only; `/heimdall/authorize` adds non-protocol headers with `putIfAbsent`.
- Palantir rebuilds resources from a fixed field list (`heimdallResourceForStorage`) and re-saves the whole
  namespace; any new `AuthorizableResource` field must be added there or an edit silently drops it.
- AuthZEN PDP metadata: the identifier is `<prefix>/heimdall`, served at `/cas/heimdall/.well-known/authzen-configuration`
  (`HeimdallAuthZenConfigurationController`). The spec inserts the well-known segment after the host
  (`/.well-known/authzen-configuration/cas/heimdall`), outside the CAS context: deployments need a proxy or ENGINE
  rewrite-valve rule, as in the `heimdall-authzen` scenario's `rewrite.config`. `policy_decision_point` must equal the
  identifier the PEP started from. List only endpoints that exist (no search yet).
- AuthZEN decision context: denials carry `context.reason` from `AuthorizationDecisionReason` (set by the engine:
  `subject_unresolved`, `no_matching_resource`, `no_policies`, `policy_denied`); grants carry no context. Never put
  policy names, attribute names or messages in it; details go to DEBUG logs. Search (R5) and the OID4VP wallet bridge
  (R7) are deferred and noted as future work in the Heimdall and verifiable credentials docs.
- AuthZEN evaluations (`/heimdall/authzen/evaluations`): top-level subject/resource/action/context are defaults that an
  entry replaces per key. Authenticate the caller once (`authenticateCaller`), then `resolveSubject` per entry;
  never call `parse` per entry (it would consume single-use JWT assertions). Per-entry failures are `decision:false` with
  `context.error.{status,message}` (no exception text for 500s); short-circuit semantics omit the remaining entries;
  no/empty `evaluations` falls back to the single evaluation. Evaluations run sequentially.
- AuthZEN evaluated denials use HTTP 200 with `decision:false`; authentication failures use 401.
  Discovery/batch/search are separate capabilities, and the specification's example endpoint path is not mandatory.
- Read shared helpers before reporting leaks: request headers already filter credentials and the request
  principal is JSON-ignored. Confirm performance severity with evidence; blocking policy parallel streams
  and unpooled JDBC merit investigation, not an unmeasured claim of outage.
- Puppeteer scenario `heimdall-authzen` covers the AuthZEN endpoint end to end (decisions across namespaces,
  id patterns, deny-wins, empty policies, `X-Request-ID`, unknown fields, 400/401, the Heimdall access strategy,
  client-credential and bearer PEPs, the legacy endpoint, the actuator and throttling). It also enables throttling,
  so keep 401-producing steps at least one throttle window apart. Palantir scenarios only check that tabs load.
- Scenario `heimdall-nginx` drives the shared nginx config (`ci/tests/nginx`) as a PEP: `/api` uses `auth_request`
  against `/heimdall/authorize`. The subrequest must set `proxy_method POST` (it is a GET otherwise) and send a
  `namespace`; nginx does not escape variables in `proxy_set_body`, so URIs with `"` or `\` are rejected by a `map`.
  `auth_request` maps any status other than 2xx/401/403 (such as Heimdall's 404) to 500. Validate edits with a local
  `nginx -t` and stub upstreams; the config resolves `host.docker.internal` at load time.

Guidance for AI coding agents working in the Apereo CAS source tree.

> This repository is for CAS contributors. If the task is deployment/configuration, prefer the WAR overlay approach instead of editing this repo.

## Big picture

- CAS is a very large Gradle monorepo. `settings.gradle` shows the main layering: `api/` defines contracts and config models, `core/` implements platform behavior, `support/` adds protocols/backends/features, and `webapp/` assembles runnable apps.
- The servlet app starts in `webapp/cas-server-webapp-init/src/main/java/org/apereo/cas/web/CasWebApplication.java`; startup is extensible through `ApplicationUtils.getApplicationEntrypointInitializers()`.
- Feature wiring is annotation-driven. Example: `core/cas-server-core-authentication/.../CasCoreAuthenticationAutoConfiguration.java` imports authentication sub-configurations behind `@ConditionalOnFeatureEnabled`.
- Support modules usually follow a family split such as `*-core`, storage variants (`*-jdbc`, `*-mongo`, `*-redis`), protocol/webflow modules, and a thin webapp assembly. OIDC, SAML, tickets, services, and MFA all follow this pattern in `settings.gradle`.
- `api/cas-server-core-api-configuration-model/.../CasConfigurationProperties.java` is the root of the `cas.*` config tree. Property classes are not passive POJOs: `ConfigurationMetadataGenerator` fails if a config model class is missing `@RequiresModule`.

## Conventions you should match

- Java 25 is required (`gradle.properties`); many sources use `import module java.base;`, Lombok `val`, and package-level `@NullMarked` via `package-info.java`.
- `import module java.base` makes `Signature` ambiguous (`java.security.Signature` vs `java.lang.classfile.Signature`); write `java.security.Signature`. Mapping to a `@SuperBuilder` result (`IntStream.mapToObj(i -> X.builder()...build())`) infers a capture type; give the stream a type witness (`.<X>mapToObj(...)`).
- Lombok `val` cannot infer generic poly expressions: `val x = Objects.requireNonNullElse(list, List.of())` (or `...ElseGet(list, List::of)`) becomes `Object`. Assign the plain call to `val` and null-check separately, or declare the type.
- Spring config classes generally use `@AutoConfiguration` or `@Configuration(proxyBeanMethods = false)`, `@EnableConfigurationProperties(CasConfigurationProperties.class)`, `@ConditionalOnFeatureEnabled`, and bean methods with `@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)` plus `@ConditionalOnMissingBean`. See `support/cas-server-support-token-core/.../TokenCoreConfiguration.java`.
- Configuration model classes usually live under `api/.../configuration/model/**`, use Lombok accessors, and carry `@RequiresModule(name = "...")`; example: `LdapAuthorizationProperties`.
- Tests are organized by JUnit tags, not by the plain Gradle `test` task. The shared `buildSrc` test conventions disable `test` and register tasks like `testAuthentication` and `testTickets` for the tags discovered by `TestCategoryTagsValueSource` in `*Tests.java`. Both root category selectors and module-qualified category tasks execute tests; the plain `test` task never does.
- Related test scenarios are often grouped with `@Nested`; example: `support/cas-server-support-token-core/.../JwtBuilderTests.java`.
- Unalias Linux/macOS commands before you run them, specially `tree`, `find`, `grep`, `cat`, etc.
- From a sandbox that cannot delete files, run read-only git commands with `GIT_OPTIONAL_LOCKS=0` (for example `GIT_OPTIONAL_LOCKS=0 git status`); otherwise git can leave a stale `.git/index.lock` that blocks the user's git.
- Consider using StringUtils.EMPTY instead of "" for empty strings, and StringUtils.isNotBlank() instead of != null && !isEmpty() for string checks.
- Keep overloaded methods (same name, any parameters or visibility) next to each other, with no other method between
  them; Checkstyle's `OverloadMethodsDeclarationOrder` fails the build otherwise. Check this when adding a method whose
  name already exists in the class or interface, including private helpers.
- Do not add unnecessary javadoc. Add javadoc only where a public contract or a non-obvious reason genuinely needs it; record rationale for a change in PLANS.md instead.

## Workflows that matter here

- List supported test buckets:
  ```bash
  ./gradlew -q testCategories
  ```
- Run repository test categories through the project script, not `gradle test`:
  ```bash
  ./testcas.sh --category authentication
  ./testcas.sh --category tickets --with-coverage
  ./testcas.sh --category oidc --debug
  ```
- `./testcas.sh --category changed` reads tags only from changed or untracked `*/src/test/java/*Tests.java` files.
  It does not infer test categories from production-source changes; select the relevant categories explicitly for those.
- Run one module or one class directly when narrowing a change:
  ```bash
  ./gradlew :core:cas-server-core-authentication:testAuthenticationHandler --tests "*AcceptUsersAuthenticationHandlerTests"
  ```
- Compile production sources without assembling archives or compiling tests when you only need a fast validation pass:
  ```bash
  ./gradlew classes --parallel
  ./gradlew :support:cas-server-support-json-service-registry:classes
  ```
  Use `assemble` for artifacts and `testClasses` explicitly for test compilation. Ordinary `assemble` does not depend on
  `testJar`; non-minimal publishing and consumers of the `tests` configuration still build it on demand.
- Gradle 9.8.0 enables build caching, the configuration cache, parallel execution and Isolated Projects here.
  Isolated Projects configures every project on a cache miss and ignores configuration on demand. Compare representative
  scoped commands before changing either setting. User-home `gradle.properties` overrides the repository's JVM settings.
- Test category discovery runs inside `TestCategoryTagsValueSource`: only its sorted tag list is a configuration input.
  Implementation edits retain the cached graph; adding/removing categories invalidates it. Do not move source reads back
  into the convention script or restore the old `.gradle/cas-test-tags` fingerprint cache.
- An IDE `JetGradlePlugin` error about `setExcludedTaskNames` comes from the IDE integration, not category discovery.
  Use `--no-isolated-projects` for the affected IDE invocation until that integration supports Isolated Projects; keep
  configuration caching enabled and do not disable Isolated Projects globally to hide an IDE-only violation.
- Many `./testcas.sh` categories shell out to `ci/tests/**/run-*.sh` and require Docker on Linux; the script will refuse those categories when that prerequisite is missing.

## Security-sensitive change discipline

- Verify a security report against the complete execution path before changing code. Identify the attacker-controlled input, the trust decision, and the exact point where validation or isolation is bypassed.
- Keep security fixes at the narrowest shared enforcement point. Preserve unrelated protocol behavior and avoid adding parallel validation, crypto, or metadata abstractions.
- Treat caller-supplied protocol values as untrusted even if CAS later signs the containing message. Internal signing proves CAS produced the final message; it does not authenticate the caller's original value.
- A presented signature must be cryptographically and algorithmically validated whenever it is used as proof, independently of whether metadata requires requests to be signed. Apply the same resolved security parameters to every binding before raw cryptographic validation.
- For unsolicited SAML flows, accept ACS destinations only from the applicable registered-service metadata. For solicited flows, a request-supplied ACS outside metadata requires an actually authenticated request signature.
- Build credentials from matching certificate/private-key material belonging to the message recipient. Never combine a peer's public key with CAS's private key.
- `SubjectConfirmationData.Address` identifies the presenter, not the ACS server. Use trusted proxy-aware client information or omit it; never perform a synchronous DNS lookup of the ACS host while building assertions.
- Scope caches that contain service-specific metadata, keys, validation filters, or trust policy to the registered-service boundary. Address entries by their complete key; do not scan unrelated cached values for a matching entity.

## Test, concurrency, and hand-off expectations

- Reuse the nearest existing test class whenever practical. A security regression should fail on the vulnerable implementation and exercise the actual trust boundary, not merely assert an implementation detail.
- Test web endpoints through `MockMvc` or the existing web-test infrastructure. Never instantiate controller classes directly in tests.
- Prefer real protocol artifacts for crypto tests: sign/encrypt with matching test credentials, serialize and unmarshal when validating embedded XML, and include negative cases for tampering, blocked algorithms, or mismatched destinations.
- Keep tests deterministic and independent of external DNS or network availability. Use loopback addresses, local mock servers, classpath resources, and uniquely named temporary files; clean up local resources in `finally` or try-with-resources blocks.
- Changes and tests must tolerate Gradle parallel mode. Avoid shared mutable static state, fixed temporary filenames, cross-test cache assumptions, and mutation of shared application state when a local fixture will work.
- Never add the Java `synchronized` keyword. When mutual exclusion is genuinely required, use `CasReentrantLock` and its execution helpers consistently with nearby CAS code.
- If the user requests a `PLANS.md` plan, create it before implementation work and check off each step as it is completed.
- When code cannot be compiled locally, check overridden and called signatures for checked exceptions (for example `ByteArrayResource.getInputStream()` declares `IOException`) and reuse helpers' existing filtering instead of repeating it.
- Honor explicit verification boundaries. If the user asks not to run tests, do not invoke tests or Gradle tasks; perform static review such as `git diff --check` and clearly report what was not run.
- For release-bound security work, add one brief, user-facing note to the appropriate security/protocol section of the requested release-notes file after the implementation is complete.
- For all changes, cross check with puppeteer scenarios and make sure they continue to pass and are adjusted correctly.
- When the work is done, end the hand-off with a ready-to-use commit message covering only the uncommitted changes
  (`git --no-optional-locks status`): a conventional subject (`fix:`, `feat:`, ... as in `git log`) and a short body.
  The maintainer commits; do not commit yourself.

## Practical boundaries

- Put new behavior in the narrowest module that already owns that concern; do not skip from `webapp/` straight into backend-specific code when an `api/` or `core/` seam already exists.
- When adding configuration, update the config model class first; otherwise metadata/docs generation will not understand the new property.
- For service-aware logic, look for `ServicesManager.findServiceBy(...)`; for ticket-aware logic, look for `ticketRegistry.getTicket(...)`. Those seams are used repeatedly across `core/` and `support/` and are usually the right integration points.
- Treat authentication, tickets, webflow, logout, MFA, and crypto as security-sensitive areas. Match existing CAS utilities and flows instead of introducing parallel mechanisms.
- Keep diffs surgical: this codebase already has strong patterns, so the fastest path is usually “copy the nearest module family pattern and adapt it” rather than inventing a new abstraction.
- Documentation pages on gh-pages are pre-rendered per version, but they load the stylesheet and script from the shared site root, and every publish overwrites those root files. The current design ships as `stylesheets/site.css`, `stylesheets/site-print.css` and `javascripts/site.js`; the root `stylesheet.css`, `print.css` and `main.js` belong to the released versions' old markup. A redesign that changes page markup must use new asset names, never replace the ones older versions link.
- Unit-test matrix jobs (`tests.yml`) record JaCoCo data only (`testcas.sh --with-coverage` only turns on the agent) and upload the `.exec` files; the single `coverage` job builds `jacocoRootReport` and does every Sonar/Codecov/Coveralls/Codacy upload. Do not move `jacocoRootReport` or `sonar` back into the matrix: the root report compiles all ~430 projects and Sonar re-analyses the whole code base, which is what made each category job 17 minutes instead of 7.
- Every `gradle/actions/setup-gradle` step sets `gradle-home-cache-excludes: caches/build-cache-1` and
  `cache-encryption-key: ${{ secrets.GRADLE_ENCRYPTION_KEY }}`. Preserve both: the local build cache duplicates Develocity
  outputs, and the encryption key enables saving/restoring the project configuration cache. Fork PRs without this secret
  can still build, but cannot restore the encrypted configuration cache. Do not layer `setup-java` or GraalVM Gradle
  caching over `setup-gradle` in the same job.
- CI permits Gradle daemon reuse within a job; `ci/init-build.sh` must not append a disabling user-home property.
  Release, documentation and scenario scripts preserve daemon reuse across their Gradle invocations. CodeQL keeps
  `--no-daemon` for compiler tracing. Test categories and Puppeteer scenarios are fetched once per discovery
  job; print the captured result and split scenario JSON with `jq`, preserving the existing matrix boundaries.
- The CodeQL job (`analysis.yml`) applies `--no-build-cache --rerun-tasks` after its shared build options so traced
  compilation actually runs. The current `settings.gradle` has no CodeQL-specific `JavaCompile` cache guard. Preserve
  these workflow flags: CodeQL only extracts compilations it observes, so restored or up-to-date outputs empty the scan.
- The documentation data generator (`docs/cas-server-documentation-processor`) runs as `java @build/casdocsgen.args ...`, an argument file written by its `docsGeneratorArguments` task from `sourceSets.main.runtimeClasspath`; `publish.sh` no longer builds the ~1 GB `casdocsgen.jar` boot jar. Its Gradle run adds `-DskipErrorProneCompiler=true` (override with `DOCS_GENERATOR_GRADLE_OPTIONS`, an empty value restores Error Prone). It still needs every CAS module compiled: actuators, feature toggles and shell commands are found by ClassGraph scans of the runtime classpath, and third-party settings come from the dependencies' metadata. Those lookups go through `CasDocumentationClassIndex`, one ClassGraph scan of `org` shared by the exporters; add new class lookups there rather than calling `ReflectionUtils`, which scans the whole classpath on every call.
- `ci/docs/publish.sh` exit codes carry meaning for the workflow retry (`retry_on_exit_code: 1`): 1 is a failure worth another attempt, 3 is broken internal links/images/scripts (fails at once, nothing is published), 4 is broken external links only (the site is published first, then the job fails). Keep new failure paths on 1 unless a rerun cannot help. External links are checked only on the weekly schedule or when asked (`--proof-external`, the `proofReadExternal` input or `vars.DOCS_PROOF_EXTERNAL`), with successful results cached for 7 days in `build/htmlproofer`.
- Actuator endpoint blocks (`_includes_site/actuators.html`) take their operations from the `cas_actuator_operations` filter in `_plugins/cas_actuators.rb` (sorted, parameters normalized, curl built there), and settings snippets from `cas_actuator_enable_snippet` / `cas_actuator_security_snippet` via `cas-actuator-snippet.html` in all three formats. Per operation, render only what differs (parameters, response, example); setup, security, settings and troubleshooting are rendered once per include in the shared tabs. The operations table is excluded from `responsiveTables()`.
- Settings search: `ci/docs/index.js` writes `assets/data/<version>/index.json` as `{generated, docs}` (no lunr index; each doc carries name, short type, description, default, kind required/optional/thirdparty, module, duration, deprecation). `site.js` searches it directly (`searchCasSettings`, shared by the Configuration Properties page and the Shift-Shift palette); the page state lives in the URL (`?q=&exact=1&scope=name&kind=cas|thirdparty&deprecated=hide`). The layout exposes `data-docs-base`, `data-docs-version` and `data-docs-build` on `<body>` for these URLs.
- Feature toggles (`Configuration-Feature-Toggles.md`) render through `_includes_site/cas-feature-toggles.html` from the `cas_feature_catalog` filter in `_plugins/cas_features.rb`, which derives feature and module from the `CasFeatureModule.<Feature>[.<module>].enabled` property, groups features by area and links docs only when the target page exists for that version. Add title overrides, groups or docs links there, not in the include. The environment variable form maps both `.` and `-` to `_` (`CasFeatureEnabledCondition` reads through `Environment.getProperty`), unlike the relaxed-binding form used for regular settings.
- Module dependency tabs (`_includes_site/casmodule.html`, included through `include_cached`) are plain Bootstrap tabs: no inline script, the default tab and pane are chosen together in Liquid, and pane ids use the full coordinates plus the variant options. Do not add per-include scripts or truncated ids; either one lets the selected tab and the visible pane drift apart.
- Local serving (`publish.sh --serve true`) keeps the configured `baseurl: /cas`, so the site answers at `http://localhost:4000/cas/<version>/` exactly like production. `variables.html` always sets `basePath` to `/cas` and `site.js` has no localhost path branches; do not reintroduce `--baseurl ""` or localhost-specific paths.
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
- Setting links in docs (required): every reference to a configuration setting anywhere in the documentation, release
  notes included, must be marked so readers can look it up in place. Inline, write `` `cas.server.name`{: .cas-setting} ``
  (an assignment such as `` `cas.tgc.secure=false`{: .cas-setting} `` works too; the value is ignored for the lookup). For a
  fenced `properties` block of settings, put `{: .cas-settings-linked}` on the line right after the closing fence. Mark only
  real CAS or Spring setting names, not prefixes used as headings, removed settings, JSON fields or registered-service
  properties. `initializeCasSettingLinks()` in `site.js` turns these into buttons that call `openCasSettingsPalette(name)`:
  an exact match opens the setting's details inside the Shift-Shift palette, a partial name shows the matches; the page
  never changes. Linked blocks are rebuilt line by line from their text (key, separator, value), because Rouge's
  properties lexer splits keys with `[0]` into separate tokens. `planning/Quick-Start.md` is the reference page.
- Every documentation page needs an opening paragraph right after the H1 that the layout can use as its description:
  at least 40 characters, not ending in a colon, and not a "see this guide" or dependency lead-in.
- On phones (`max-width: 760px`) the sticky header is offset by `--docs-masthead-height` so only the navigation bar
  stays visible, and `site.js` sets `--docs-header-height` to that bar alone; keep both in step when changing the masthead.
- The docs load two web fonts only, Newsreader (headings) and DM Sans (everything else); `--font-mono` is the system
  monospace stack. Do not add font families or hotlink images from third-party hosts; keep images under `images/`.
- Large topics are split into one page per concern with a sidebar submenu (see `authorization/Heimdall-Authorization-*.md`
  and `multitenancy/Multitenancy-*.md`); when moving a section, update cross-page anchors and links in release notes.

## OIDC verifiable credentials (OID4VCI / OID4VP)

- The OID4VCI Nonce Endpoint must stay publicly reachable (OID4VCI 1.0, section 7.1: it is not a protected resource). Do not add it to the interceptor's protected list; rate limiting or throttling is the appropriate control there. The presentation request creation endpoint is CAS's own verifier API and should be protected.
- A credential offer's `tx_code` is optional in the protocol but mandatory once the offer declares one. Keep it independent of any value the caller already holds, and leave a way to disable it for wallets that collect no user input, otherwise the walt.id puppeteer scenario cannot redeem an offer.
- Verify a JWT proof's `typ` header as well as its signature. A proof or key-binding JWT that is only checked for signature, audience and freshness can be satisfied by a token minted for a different protocol.
- When reviewing these flows, confirm the normative text against the current published OID4VCI, OID4VP and SD-JWT VC specifications rather than from memory; the drafts changed substantially before 1.0.
- The issuance wire format is OID4VCI 1.0: the credential request takes `proofs` (plural) plus either `credential_configuration_id` or `credential_identifier` (mutually exclusive), and the response is `{"credentials":[{"credential":...}]}` with no `format` anywhere. A batch is one request carrying several proofs, yielding one credential per proof of the same configuration; there is no batch endpoint, and the metadata advertises `batch_credential_issuance`. The proof challenge comes only from the nonce endpoint, never the token response. A CAS credential identifier is the credential configuration id itself, which is what lets `resolveConfigurationId` treat both request parameters the same way.
- Check which generation of a third-party service CI is pinned to before trusting it as an interop signal. walt.id ships two: the original Issuer/Verifier/Wallet APIs implement the drafts, while Issuer2, Verifier2 and Wallet API v2 (`waltid/wallet-api2`, port 7006) implement the finalized 1.0 specs. Reading the deprecated `waltid-openid4vc` library's models -- a required `format`, a singular `proof` -- and concluding "the vendor cannot do 1.0" is the mistake to avoid; check the vendor's current docs and published images.
- OpenID4VCI and RFC 8414 both locate metadata by inserting the well-known segment between the host and the issuer's path (`https://host/.well-known/openid-credential-issuer/cas/oidc`), not by appending it the way OpenID Connect Discovery does. It applies to the credential issuer metadata and the authorization server metadata alike, so fixing one and not the other just moves the failure one step later. CAS sits under the `/cas` context path, so those URLs never reach the application and the container answers 404 itself; the fix is a rewrite, either in the fronting proxy or in CAS's own Tomcat rewrite valve with `valve-type=ENGINE`, which runs before a context is selected. Scope the rule to the documents CAS publishes rather than all of `/.well-known/`, or it will swallow `/.well-known/acme-challenge/`. This is invisible to any wallet that guesses the appended form, which is why it survived until a spec-correct wallet was used.
- The pre-authorized code grant is unauthenticated by design: CAS reads the `pre-authorized_code` and `grant_type` form parameters through a `DirectFormClient`, and the client comes from the offer, not the request. A wallet chooses its authentication method from discovery metadata, so `token_endpoint_auth_methods_supported` includes `none`, which is now also a constant on `OAuth20ClientAuthenticationMethods`.
- Every value in `token_endpoint_auth_methods_supported` is run through `OAuth20ClientAuthenticationMethods.parse`, which `orElseThrow`s, by all three OIDC client authenticators. Any value the enum does not know turns client authentication into a 400 with a bare "No value present", so adding a method to that list means adding it to the enum in the same change.
- Advertising `none` does not stop a client choosing something else: a wallet picks its own method from the list, and walt.id prefers `private_key_jwt` whenever a JWT method appears there even though it has no client registration. A deployment whose clients are unregistered wallets should advertise only the methods those wallets can actually use; that is configuration, not a CAS defect. Do not read such a failure as the `none` support being broken.
- `script.json` `properties` is an array of command-line arguments, so every element must be a plain `--key=value` string. There is nowhere to put a JSON comment; explain scenario configuration here or in PLANS.md instead.
- Wallet API v2 needs no account, database or front end in CI: authentication is off and persistence is SQLite in the shipped configuration, so the scenario creates a wallet over HTTP and drives `POST /wallet`, `credentials/receive` and `credentials/present` directly. That is why the walt.id scenario no longer opens a browser.
- OID4VP delivery rules interlock rather than stacking: whether a request may be signed follows from the client identifier prefix, and whether it may be served by reference follows from whether it is signed. Check the prefix before reasoning about `request_uri`, and read the spec's own unsigned example, which passes every parameter by value.
- The credential endpoint is a protected resource, not a token endpoint, and the two speak different error vocabularies. A token problem is a 401 with a `WWW-Authenticate` challenge (`invalid_token` per RFC 6750 section 3.1, and the challenge is mandatory on any 401 per RFC 9110 section 15.5.2, named only when a token was actually presented); a request problem is a 400 with one of OpenID4VCI 1.0's own codes (section 8.3.1.2) -- `invalid_credential_request`, `unknown_credential_configuration`, `unknown_credential_identifier`, `credential_request_denied`, `invalid_proof`, `invalid_nonce`, `invalid_encryption_parameters`. Draft-era `unsupported_credential_type` / `unsupported_credential_format` are gone from 1.0; a `credential_identifier` is unknown when it is not among the identifiers of the token's authorization details. Reaching for `OAuth20Constants.INVALID_REQUEST` here is the reflex to resist.
- `invalid_proof` and `invalid_nonce` are not interchangeable: a wallet told its nonce is stale fetches a new one and retries, while a wallet told its proof is invalid stops. Carry the distinction on the exception (`OidcVerifiableCredentialProofException`), not in the message text, or it is lost the moment the exception crosses a layer.
- An endpoint that answers the same error for a stolen token, an unknown credential type and a bad proof is not merely unhelpful; it is untestable from the client side, which is how these three defects survived together.
- `getAccessTokenFromRequest` accepts the access token from an `access_token` or `token` request parameter as well as the authorization header, and that is deliberate. RFC 9700 section 4.3.2 forbids *clients* from using the query-parameter method; it places no requirement on the resource server, and the maintainer's position is that how a caller presents its token is the caller's business. Do not propose closing it as a CAS defect.
- An `OAuth20AuthorizationResponseBuilder.supports` that returns false does not reject anything; it hands the request to the next builder, which succeeds without whatever this one would have added. Conditions that belong in an error path must not live there. The VC builder gated on client type and PKCE and so dropped `authorization_details` for every client it excluded, with the failure surfacing at the credential endpoint much later. Keep `supports` to "is this request mine", and report refusals from `build`.
- Overriding `build` on a response builder loses the parent's `@Audit` annotation unless it is repeated on the override.
- Single-use state is consumed by the delete, not by the read before it. `TicketRegistry.deleteTicket` returns a count every registry derives from an atomic removal (`map.remove`, Redis `DEL`, JPA affected rows), so `delete(...) > 0` is a compare-and-swap and the only caller seeing a non-zero count is the winner. A `getTicket` ahead of it is a pre-check, not the decision -- which is why the nonce service is safe despite looking racy, and why the pre-authorized code was not: it used `update()` plus delete-if-expired, a read-modify-write, and ran after the token was already minted.
- Consume single-use state before the thing it authorizes exists. An `OAuth20AccessTokenGeneratorCustomizer` runs during token generation, so a check there cannot refuse the request; the grant request extractor runs before it and can.
- `OAuth20HandlerInterceptorAdapter.doesUriMatchPattern` matches `/<segment>(/)*$`, so an endpoint is protected only when its path *ends* with the configured segment. Anything with a path variable is public whatever the interceptor lists -- which is intended for the wallet's request object lookup. An endpoint that must be protected takes its identifier as a query parameter.
- Explanation belongs in the javadoc of the method it explains, with its parameters. Do not write inline block comments inside method bodies.
- A verifiable credential is not an ID token. Routing it through `IdTokenSigningAndEncryptionService` makes the client's `signIdToken`, `encryptIdToken` and `idTokenSigningAlg` decide whether a credential is signed, encrypted, and with what -- none of which belong to it. Credentials sign through `BaseOidcVerifiableCredentialEncoder.signCredential`, whose algorithm comes from `credentialSigningAlgValuesSupported`, the same property the verifier checks.
- `BaseTokenSigningAndEncryptionService.signToken` reads a `typ` *claim*, unsets it, and promotes it to the JWS header. Anything that replaces that path must set the header `typ` itself, or the credential loses the media type its verifier requires.
- Review this subsystem at its boundaries, not per class. The interesting defects live where the VC code borrows general CAS machinery: `IdTokenSigningAndEncryptionService` decides how a credential is signed or encrypted, `TransientSessionTicket` and the global `cas.ticket.tst` policy decide how long every offer, code, nonce and presentation request lives, and `BaseOAuth20Controller.getAccessTokenFromRequest` decides where a bearer token may come from.
- Ask who is authorized for what. `cas.authn.oidc.vc.issuer.credential-configurations` says what the issuer can mint, never who may ask for it; a per-service policy is what ties the two together. `OidcRegisteredService.verifiableCredentialsPolicy` is now read through `OidcVerifiableCredentialPolicyUtils.resolveAllowedCredentialConfigurationIds` at all three points where a credential type is claimed -- offer transaction, authorization details, credential endpoint -- and the result is always an intersection with what the issuer publishes, so a policy can narrow a service but never widen it.
- An unread field on a registered service is a finding, not a feature. `verifiableCredentialsPolicy` and `DefaultRegisteredServiceOidcVerifiableCredentialsPolicy` both existed and shipped in the schema while nothing in CAS ever called `getVerifiableCredentialsPolicy`, which reads from the outside exactly like an enforced authorization control. When a policy type exists, grep for its getter before assuming it does anything.
- An empty policy means "no opinion", not "deny everything". Authorization policies here default open when unconfigured, because they are added to deployments that were already working; a policy that denied by default would break every existing service on upgrade.
- CAS is both issuer and verifier here, and the verifier only trusts CAS-issued credentials: `iss` must equal the local issuer, `vct` must map to a local configuration, the signature is checked against CAS's own keystore, and a `status` claim must point into a status list CAS publishes with a `VALID` entry (read from the ticket registry, never fetched); any other status reference is refused rather than ignored. That is a trust policy, not a defect -- OpenID4VP leaves issuer trust to the verifier ("Verifiers must verify that the issuer of a received presentation is trusted on their own"), and a verifier that cannot evaluate revocation must fail closed. Do not report it as a compliance gap. Widening it means external issuer trust, `x5c`, DID resolution, OpenID Federation and fetching other issuers' status lists, which is a feature with its own configuration surface, not a fix.
- Proof validation takes the credential configuration id (`OidcVerifiableCredentialProofValidator.validate(proof, configurationId, nonces)`)
  and enforces that configuration's `proof-signing-alg-values-supported` and `cryptographic-binding-methods-supported`; the
  one-argument overload names no configuration and accepts anything verifiable, which is what the unit tests rely on. Holder keys come
  from `jwk` (a stray `kid` next to it is ignored), `x5c` (leaf key only, validity checked) or a `did:jwk` `kid`.
- CAS does not depend on Google Tink, so never use Nimbus `Ed25519Verifier`, `Ed25519Signer`, `OctetKeyPairGenerator` or the
  X25519 classes, in main code or tests; they fail without it. Verify EdDSA with
  `EncodingUtils.verifyJwsSignature(EncodingUtils.newJsonWebKey(okp.toPublicJWK().toJSONString()).getKey(), jws)` after checking the
  header algorithm is `EdDSA`; in tests, generate keys with `KeyPairGenerator.getInstance("Ed25519")` and sign with jose4j
  (`JsonWebSignature`, `AlgorithmIdentifiers.EDDSA`, `PublicJsonWebKey.Factory.newPublicJwk(publicKey)` for a `jwk` header).
- `OidcRequestParameterResolver` drops every requested scope that is not supported, so a scope CAS must honour has to be
  part of `OidcServerDiscoverySettingsFactory.resolveScopesSupported`, which also feeds `scopes_supported`. Credential
  configuration scopes are added there; scope-granted credential configurations are recorded on the access token by
  `OidcVerifiableCredentialsAccessTokenGeneratorCustomizer` (authorization code and refresh grants), narrowed by the service
  policy, and the credential endpoint combines them with authorization details.
- OID4VP key binding algorithms live in `OidcVerifiableCredentialPresentationResponseEndpointController.KEY_BINDING_ALGORITHMS_SUPPORTED`, which also feeds `kb-jwt_alg_values`; `sd-jwt_alg_values` comes from the `dc+sd-jwt` configurations. OpenID4VP wants fully specified identifiers (`Ed25519`, not `EdDSA`); Ed25519 key binding is verified with the JDK `Signature` so both header values work. `alg_values` is not defined for `dc+sd-jwt`.
- Issued credentials carry no `client_id` or `credential_configuration_id` (claim or header): they reveal the relying party to every verifier. CAS's verifier finds the issuer key by `kid` (an `OidcRegisteredService` key selector with `jwksKeyId` set, through `getJsonWebKeySigningKey`), the same key the JWKS publishes.
- Authorization details follow RFC 9396 through refresh: `OAuth20Utils.getAuthorizationDetails(token)` reads them from the
  code or refresh token being exchanged, the access token factory and `OAuth20DefaultTokenGenerator.generateRefreshToken`
  use it, and the code, access token and refresh token compactors keep them through `OAuth20Utils.to/fromCompactAuthorizationDetails`.
- Digital Credentials API (maintainer decisions): the RP page drives `navigator.credentials.get()`; the presentation request
  carries `response_mode` `dc_api`/`dc_api.jwt` and `origin` (accepted when `checkCallbackValid(service, origin + "/")`); the
  answer is `digital_credentials_request` (unsigned under `redirect_uri`, signed with `expected_origins` under an x509 prefix);
  the RP posts `{request_id, data}` to `POST oidcVcPresentationResult` (client-authenticated by the interceptor's path match).
  The key binding audience is always `origin:<origin>` (with or without a trailing slash); DC transactions carry `PROPERTY_ORIGIN`
  and are refused at the response URI. Encrypted DC responses carry no `state`; the JWE `kid` must be the request id.
- `x509_hash` client identifier (`ClientIdentifierPrefixes.X509_HASH`): `OAuth20Utils.computeCertificateThumbprint` of the
  OIDC signing key's leaf certificate. `resolveClientIdentifier(OidcConfigurationContext)` serves both the request and the
  key binding audience check; both x509 prefixes need the key's chain (`resolveCertificateChain`), only `x509_san_dns` a SAN.
- `direct_post.jwt` (opt-in `response-mode=DIRECT_POST_JWT`, maintainer decision): the request transaction keeps a
  per-request P-256 `ECDH-ES` private JWK (`PROPERTY_RESPONSE_ENCRYPTION_KEY`) whose `kid` is the request id, so the
  response endpoint finds the request from the JWE header before decrypting; the decrypted `state` must equal the `kid`.
  Encryption-requested transactions refuse plaintext presentations but accept plaintext errors (OID4VP 1.0 section 8.3.1).
  The presentation request may carry `response_mode`; the setting is a floor (a request may add encryption, never drop it).
  Scenario `oidc-verifiable-credentials-waltid` presents once per mode; walt.id encrypts to the first usable ECDH-ES JWK.
- A wallet's OID4VP error response is unauthenticated, so it never ends a request (maintainer decision): it is recorded
  under `resolvePresentationErrorId`, apart from the verified outcome (`resolvePresentationResultId`), the request stays
  open until it expires, and `oidcVcPresentationResult` answers `pending` while the request lives, then the error. A
  verified outcome wins and discards the error when collected.
- Issued credentials carry the signing key's `x5c` (from the keystore JWK, via `JsonWebTokenSigner.certificateChain`) without a trailing self-signed trust anchor (HAIP 1.0 section 6.1.1); the `X509_SAN_DNS` request object does the same (section 5). Both go through `CertUtils.withoutTrustAnchor`. A self-signed signer (`CertUtils.isSelfIssued` on the leaf) is accepted with a warning (maintainer decision), not refused. A key with no chain sends no `x5c`; test keystores have none, so `vc-issuer-x5c.jwks` (EC P-256 leaf for `sso.example.org` + root, valid to 2126) exists for that. Under `import module java.base`, import `java.security.cert.X509Certificate` explicitly (`javax.security.cert` clashes).
- Presentation transactions carry `clientId` (and `redirectUri` for same-device); results carry `clientId` and `responseCode`. `oidcVcPresentationResult` answers `404` to any other client or to a missing/wrong `response_code`. Test transactions built by hand must set `clientId`, or results cannot be collected.
- DCQL has no `required` per claim: optional claims become claim `id`s plus `claim_sets` (all, then required; or each alone when none is required), and the verifier requires the required claims or, when all are optional, at least one.
- The OID4VP response URI takes `vp_token` or `error` (never both) with `state`; an error response is consumed, answered
  `200` with `{}`, and recorded as `{"status":"error","error":...,"error_description":...}` for `oidcVcPresentationResult`.
- Turning attribute text into numbers: `NumberUtils.createNumber` decodes a leading zero as octal. Only convert when the
  `createBigDecimal(text).toPlainString()` round trip returns the same text; no hand-written regular expressions for this.
- What is worth checking in that code is consistency between the two halves: the verifier should require everything the issuer always emits. `exp` was optional at verification while issuance always stamps it, which let a credential that never expires through.
- Check every authorization path the metadata advertises end to end. Each credential configuration publishes a `scope`, which tells a wallet it may use scope-based authorization (OpenID4VCI 5.1.2), but the credential endpoint only honours pre-authorized tokens and `authorization_details`; EUDI's issuance library favours scopes. A metadata field is a promise to wallets, not decoration.
- Metadata is per format: `vct` belongs to `dc+sd-jwt` only, while `jwt_vc_json` and `jwt_vc_json-ld` need `credential_definition` (Appendix A.1). The type list and the JSON-LD context come from `BaseOidcVerifiableCredentialEncoder.resolveCredentialTypes` and `VCDM_V2_CONTEXT`, which both the metadata service and the encoders use; keep it that way so metadata and credential cannot drift. Never reference a JSON-LD context CAS does not serve; the VCDM 2.0 base context's `@vocab` already covers custom terms.
- A credential is only portable if a third party can find the issuer key. CAS publishes `/.well-known/jwt-vc-issuer` (`issuer` + the OIDC `jwks_uri`, which carries the credential signing key under the `kid` in the credential header); it needs the same host-level rewrite as the other well-known documents. There is still no `x5c` on credentials, which HAIP requires. CAS verifying its own credentials in puppeteer proves nothing about third-party verification.
- Wallets differ mostly at the edges: proof keys by `jwk`, `kid` (did:key, did:jwk) or `x5c`, EdDSA vs ES256, `redirect_uri` vs `x509_san_dns`/`x509_hash`, `direct_post` vs `direct_post.jwt`. The walt.id scenario pins P-256, `jwk` and `redirect_uri`, so it cannot catch regressions in the others.
- Key attestations (OpenID4VCI 1.0 Appendix D) arrive two ways: the `key_attestation` header of a `jwt` proof (the proof key must be attested) and the `attestation` proof type (exactly one, its `nonce` must be a `c_nonce`, one credential per attested key). `key_attestations_required` means "required" by its mere presence, so an empty `{}` must still be written: the field overrides the class-level `NON_EMPTY` with `NON_NULL`. Trust anchors live under the issuer settings; wallet attestation (OAuth client attestation) is a different trust relationship and gets its own settings under client authentication.
- `OidcVerifiableCredentialProofValidator` is a `@FunctionalInterface`: new operations on it must be `default` methods, or every lambda implementation breaks.
- Client authentication at the token and PAR endpoints is a chain of pac4j direct clients (`requiresAuthenticationAccessTokenInterceptor` takes every `DirectClient` of `oauthSecConfig`); a new method is an `OAuth20AuthenticationClientProvider` bean, as `OidcClientAttestationAuthenticator` (`attest_jwt_client_auth`) is. The first client that yields a profile wins, so a service that must use one method sets `tokenEndpointAuthenticationMethod`; `OAuth20Utils.isTokenAuthenticationMethodSupportedFor` enforces it at the token endpoint only, never at PAR.
- `x5c` trust for attestations goes through `CertUtils.readTrustAnchors` and `CertUtils.validateCertificateChain` (PKIX, no anchor in the chain, no self-issued certificate, as HAIP requires); key and client attestations share them. Under `import module java.base`, `CertificateException` is ambiguous with `javax.security.cert` and needs an explicit import, like `X509Certificate`.
- OID4VCI notification ids are transient session tickets bound to the access token's client and principal (not the token itself, so a refreshed token still works) and marked by their own property, since nonces and offers are transient session tickets too; `OidcVerifiableCredentialNotificationService.notify` is audited (`OIDC_VERIFIABLE_CREDENTIAL_NOTIFICATION`), with its resolvers registered by the oidc-vc configuration. The notification endpoint reads its body as a string with `JacksonObjectMapperFactory.builder().strictDuplicateDetection(true)` (the factory flag, never a hand-built `JsonFactory`), so malformed JSON is `invalid_notification_request` rather than the controller-wide `invalid_credential_request`.
- `OidcVerifiableCredentialEndpointControllerTests` is close to checkstyle's 2,500-line `FileLength` limit; keep additions compact or put them in another existing class.
- Token Status List entries are transient session tickets (`TST-vcstatus-<list>-<index>`), one per credential, expiring with it; their properties are strings, because registries that serialize tickets, and the stateless compactor, do not keep `Long` or `Integer` types. The stateless registry is detected by the stored ticket being `isStateless()`, and credentials are then issued without status. The status list token is rebuilt by scanning the registry and cached per node for its `ttl`. The entry's principal is the credential's `sub` (the resolved principal), not the access token's authentication principal, which is `nobody` in the pre-authorized code tests.
- `CharacterEncodingFilter` adds `;charset=UTF-8` to every response content type, `application/statuslist+jwt` included; assert media types with `contentTypeCompatibleWith`.
- Deflate and inflate through `CompressionUtils.deflate(bytes, raw)` / `CompressionUtils.inflate(bytes, offset, raw)`: raw DEFLATE for the stateless ticket registry, ZLIB (`raw=false`) for Token Status Lists, whose spec test vectors `CompressionUtilsTests` checks. Do not add private `Deflater`/`Inflater` loops.
- The VM test runner's copied build dirs predate any unpushed round: compile the Java files of the previous round's commit together with the current ones, or settings and methods added there are missing. `AbstractTicketRegistry.getTicket(id, type)` throws when the ticket is absent; use `getTicket(id)` to probe.
- OID4VCI encryption (section 10): a request asking for an encrypted response MUST itself be encrypted, so response encryption cannot ship without request decryption. Requests decrypt with the current OIDC keystore keys whose `use` is `enc` (keys without `use` are not encryption keys); the published copies get a JWE `alg` (`RSA-OAEP-256` / `ECDH-ES`) because the keystore's `alg` is a signing algorithm. Never mutate the cached keystore keys. Check `credential_response_encryption` before issuing, so a bad key does not burn a nonce or a status index; errors are never encrypted.
- Nested `@Nested` test classes that extend `AbstractOidcTests` do not inherit the enclosing class's `@ImportAutoConfiguration` or `@TestPropertySource`: repeat what they need, including whatever the enclosing instance autowires (the outer instance is built from the nested context).
- The openid.net spec pages are long: WebFetch summaries truncate and can invent text. Fetch the markdown source (`raw.githubusercontent.com/openid/OpenID4VCI/main/1.0/...`, `.../OpenID4VC-HAIP/main/1.0/...`) with curl and grep it instead.
- The VM compile check can also run Error Prone, which `-Werror` turns every warning of into a build failure: add `-XDcompilePolicy=byfile -XDshould-stop.ifError=FLOW "-Xplugin:ErrorProne <the -Xep flags of project-conventions.gradle> -XepOpt:NullAway:OnlyNullMarked=true -XepOpt:NullAway:JSpecifyMode=true"`, the `-J--add-exports`/`--add-opens` of `jdk.compiler`'s `api, main, model, parser, processing, tree, util, code, comp, file` packages, and every cached jar on `-processorpath`. NullAway refuses to start without one of its package options. For the VM test runner, leave out every `spring-cloud-*` jar except `spring-cloud-commons` and `spring-cloud-context` (the Vault, Consul and Kubernetes bootstrap configurations otherwise start and fail).

## Parallel test execution and shared registries

- Initialize shared pac4j clients with `DelegatedIdentityProviders.initialize(client)`, never a bare `client.init()`: pac4j returns immediately, uninitialized, while another thread is initializing the same instance. The helper waits lock-free on `isInitializing()`.
- Most categories in `buildSrc/.../TestCategories.groovy` are declared parallel, and JUnit's default mode there is `concurrent` for classes *and* methods, so sibling `@Test` methods in one class run at the same time against the same Spring context. A test that clears a shared registry wholesale -- `servicesManager.getAllServicesOfType(...).forEach(servicesManager::delete)` in a setup step, say -- deletes what its siblings just saved, and the failure surfaces in whichever method lost the race rather than in the one that did the clearing.
- The symptom to recognize: a test asserting on a service it saved itself gets the value that belongs to the "service was missing" code path. `OpenIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidatorTests` failed exactly that way, reporting `expected: <old-service> but was: <new-service>`, because another method's clear removed the saved service and the validator then resolved a fresh one.
- Never add `@Execution(ExecutionMode.SAME_THREAD)` to a test class; rework the test to isolate its own state instead.
- Isolate by identifier, not by emptying the registry: give each test a UUID-bearing client id and assert only on that id. `@Execution(ExecutionMode.SAME_THREAD)` fixes the within-class case but not another class sharing the context, so prefer removing the global mutation.
- Write registry predicates as `expected.equals(service.getClientId())` rather than the reverse: once the registry is no longer cleared, entries from other tests flow through the same stream.
- The mirror image of that symptom is a test asserting a *negative* that only holds while the registry
  happens to contain no match for its service id. `servicesManager.findServiceBy(<random id>)` returning
  null is a property of the shared registry, not of the test's own fixture, and it stops holding the
  moment a sibling saves a catch-all definition. `SamlIdPServicesManagerRegisteredServiceLocatorTests`
  saves several (`.+`, `callbackUrl + ".*"`) into the registry shared by every `@Tag("SAML2")` class and
  calls `servicesManager.deleteAll()` in its `@BeforeEach`, so it is both halves of this hazard at once.
  `SamlIdPDelegatedClientAuthenticationRequestCustomizerTests.verifyAuthorization` failed on it,
  reporting `expected: <false> but was: <true>` only in a full run.
- Two facts make that failure mode sharper than it looks. `SamlRegisteredService.getEvaluationPriority()`
  returns `0` while `RegisteredService`'s default is `Ordered.LOWEST_PRECEDENCE`, and priority is the
  *first* key in `BaseRegisteredService`'s comparator, so one leftover catch-all `SamlRegisteredService`
  outranks every non-SAML definition for every lookup in that context no matter what evaluation order
  the other test sets. And `BaseRegisteredServiceAccessStrategy` initialises a non-null
  `DefaultRegisteredServiceDelegatedAuthenticationPolicy` whose `permitUndefined` defaults to `true`
  with empty `allowedProviders`, so `isProviderAllowed` answers true for *any* service that resolves.
  Together: "some service matched" is the same as "delegated authentication is permitted", and no
  evaluation order a test can set will win the race.
- So a negative assertion that depends on a registry lookup cannot be made deterministic by registering
  a better-ranked service of its own. Isolate the lookup instead -- construct the component under test
  with a stubbed `ServicesManager` for exactly the assertions that reach it, and leave the wired bean
  for the assertions that do not. That is what the customizer test now does, and it gained the
  policy-permits branch as a second assertion in the process.

- A clear can sit in an abstract base class, out of sight of the class that fails. `BaseThemeTests` emptied
  the services registry in a `@BeforeEach`, which ran before every method of every subclass, and two subclasses
  in different files (`RegisteredServiceThemeResolverTests.ExampleThemeTests`,
  `ChainingThemeResolverTests.ThemeDefinitionTests`) share one context. The symptom is the resolver's fallback
  (`expected: <some-theme> but was: <example>`, the default theme) instead of the value on the test's own
  service. A test that empties the registry to prove a result is remembered (`verifyThemeIsResolvedOncePerRequest`)
  proves the same thing by deleting only its own service.

- Ports are shared state too, and the trap has a specific shape. `MockWebServer.getRandomPort()`
  now draws from **21000-24999**, a band nothing else in the repository binds, and a Checkstyle rule
  (`reservedMockWebServerPorts`) keeps it that way. It used to draw from 4000-9999, which overlapped
  about 150 ports the tests and CI containers already use -- 5432, 6379, 8080, 8443, 9042 and 9092
  among them -- as well as the `${random.int[3000,9000]}` convention test property sources use for
  their own servers. A test that hardcodes a port inside the mock range and depends on nothing
  listening there is racing every mock server in its category: `HttpUtilsTests.verifyExec` and
  `verifyBearerToken` did exactly that on 8080 and 8081, asserting that an unreachable proxy yields
  an error response, and a sibling test's mock server landing on 8080 turned "connection refused"
  into `200 OK` and inverted the assertion. The symptom is one of two identical tests failing, which
  is the giveaway that it is timing rather than logic. Moving the band fixed that class of failure
  wholesale; hardcoded 8080/8081 elsewhere in the tree is mostly harmless string-building and
  serialization anyway, and only tests that actually open a connection were ever exposed.

## Puppeteer scenario init scripts

- `ci/tests/puppeteer/run.sh` runs a scenario's `initScript` entries with `eval "source ${script}"`, so they execute in the runner's own shell. An `exit` on the success path therefore terminates the whole scenario run, which looks like the scenario dying silently right after the init script's last line of output. Let a successful init script fall off the end, and reserve `exit 1` for the failure path, which is what that exit is there for. `ci/tests/ldap/run-ad-server.sh` still carries an `exit 0` early return for the already-running case and has the same hazard.
- Init scripts are sourced, so `set -e`, `set -u` or `pipefail` in one would stay on for the rest of `run.sh`; 17 scenarios source such a script, and a leaked `set -e` made a failing test exit `run.sh` at its first attempt with code 1 (whole-script retry) instead of 5 after its in-process attempts. `run.sh` turns those options off after each init script, so do not rely on them persisting.
- Prefer polling the service's own port over a container health check, and dump `docker compose logs` on the failure path: an unhealthy container tells you nothing, while the service's logs say why it would not start.
- In CI, `run.sh` launches the Gradle build (`bootWar`, or `bootJar` for starter scenarios; native keeps `build` + `nativeCompile`) in the background and runs npm install, ESLint, bootstrap and init scripts while it builds, waiting on the build process only before launching the CAS instance that needs it. Init and bootstrap scripts must therefore never depend on the built artifact, and a fixed `sleep` in them now runs while the build competes for CPU, so poll instead. `PUPPETEER_BUILD_OVERLAP=false` restores the sequential order. `PUPPETEER_BUILD_CTR` is the build timeout in minutes, measured from launch.
- A container answering HTTP is not a container ready for setup calls. Apache Syncope's Tomcat answers `/syncope/` several seconds before its content loader populates the empty Master domain, and admin REST calls made in that window fail with `AuthorizationDeniedException` (seen once the build overlapped the init scripts and slowed startup). `ci/tests/syncope/run-syncope-server.sh` therefore waits for `Started SyncopeCoreApplication` in the container log; readiness waits in other init scripts should likewise key off the application's own started signal, bounded, with `exit 1` on timeout.
- Scenario matrix jobs in `functional-tests.yml` restore the Gradle User Home with `cache-read-only: true` and do not set `cache: 'gradle'` on `setup-java`; saving from every one of the ~560 jobs cost ~12 s each and churned the Actions cache, and the two actions caching the same directory conflict.
- The matrix jobs cache the Node.js install under `${{ runner.tool_cache }}/node/<NODE_VERSION_REQUIRED>` with a key that is that exact version, restored before `setup-node` and saved only from the default branch on a miss. A version change in `NODE_CURRENT` or a scenario's `requirements.nodejs` is a new key, so `setup-node` downloads the instructed version; keep those values exact versions (a range would never match the cached directory) and do not add `check-latest`.
- Instances whose resolved dependencies are identical share one build: `run.sh` builds only the first instance of each dependency set and copies its artifact to the others. Only instance-specific `dependencies` cause another build. Instances still start one after another, because several multi-instance scenarios need instance 1 up before instance 2 starts (Spring Boot Admin client registration, passive service-registry replication, cas2cas delegation).
- `run.sh` exit codes carry meaning for the `Run Tests` retry (`retry_on_exit_code: 1`): 1 is a setup failure worth another attempt (init scripts, containers, npm), 2 a failed build, 3 a build that exceeded `PUPPETEER_BUILD_CTR`, 4 a CAS instance that exited or did not answer its health check within `PUPPETEER_STARTUP_TIMEOUT` seconds (300 in CI, unlimited locally; a single probe may use the whole remaining budget, since some login pages take ~30 s to render, e.g. `thymeleaf-templates-rest` before its template server is up), and 5 a scenario script that still failed after its in-process attempts (3 in CI, against the running server). 2-5 fail at once; a whole-script retry cannot fix them and repeats the build and startup. Keep new failure paths on 1 only if a rerun can help.

## OpenID Connect discovery metadata

- Discovery metadata is a contract, not a description: RFC 8414 makes some entries mandatory *because* of what others advertise. `token_endpoint_auth_signing_alg_values_supported` MUST be present when `private_key_jwt` or `client_secret_jwt` is in `token_endpoint_auth_methods_supported` (and MUST NOT contain `none`). When adding an entry to one list, check whether the specification makes a second entry mandatory as a result.
- Nothing in CAS's own test suite reads this metadata as a client would, so missing entries stay invisible until a real relying party parses it. Treat a third-party client's complaint about metadata as a CAS defect until the specification says otherwise.

## Sender-constrained tokens (DPoP)

- CAS mints DPoP-bound tokens but the protected-resource half of RFC 9449 is thin. `BaseOAuth20Controller.getAccessTokenFromRequest` is the single place every protected resource resolves a token from, and it parses the authorization scheme by name; a token type CAS is willing to issue has to be listed there or the token is silently unusable, reported as `[access_token]: [null]` and then `INVALID_TICKET`.
- The two Nimbus verifiers are not interchangeable. `DPoPTokenRequestVerifier` belongs to the token endpoint and checks `htm`, `htu` and `jti`; `DPoPProtectedResourceRequestVerifier` belongs to every other endpoint and additionally binds the proof to the token through `ath` and to the confirmation recorded at issuance. `OAuth20ProofOfPossessionValidator` now has one method for each: `validate` and `validateProtectedResourceRequest`. A resource endpoint that calls the wrong one appears to work, because `htu` still matches, while checking nothing that matters.
- `ath` hashes the access token *as the client presented it*, not its decoded identifier. Pass `getAccessTokenFromRequest(...).getKey()`, never `getValue()`.
- Before reporting a gap here, read the endpoint. The userinfo endpoint was already doing the resource verification correctly in an overridden `validateAccessToken`, and the defect worth reporting was the redundant second call beside it -- the opposite shape from the one first assumed. Grepping for the validator bean found the wrong call and missed the right one.
- Identifiers that arrive on an unauthenticated request parameter are not identities. Under grants that carry their own proof of authorization -- the OpenID4VCI pre-authorized code above all -- `client_id` is whatever the wallet felt like sending. Resolve from the authenticated profile, then the token, then the parameter.

## Environment limits

- Leave no `.git/index.lock` behind. `git status` and `git diff` take that lock to refresh the index, and in a remote environment that cannot delete files the lock survives the command and blocks every subsequent git operation the maintainer runs. Read git state with `git --no-optional-locks status` / `git --no-optional-locks diff`, which never takes it, and before finishing check `ls .git/*.lock` and clear anything left. If deletion is refused, request it rather than leaving the repository wedged.
- The remote shell has no git identity. To bring a contributor's PR onto a local branch, fetch each non-merge commit as a patch from `https://api.github.com/repos/apereo/cas/commits/<sha>` with `Accept: application/vnd.github.patch` and apply it with `git -c user.name=... -c user.email=... am --3way`, which keeps the contributor as author. Skip the "Merge branch 'master'" commits; the local branch is cut from master already.
- Gradle may be unavailable in a sandboxed or remote review environment because the wrapper cannot download its distribution. When that happens, say the verification was not run instead of implying a test result, and fall back to static review such as `git diff --check` and targeted reading.
- Maintainer: run Checkstyle on every changed Java file before handing work off, even without Gradle. Use the version in
  `gradle/libs.versions.toml` (`checkstyle-<version>-all.jar` from the Checkstyle GitHub releases; 14.x needs Java 21+) as
  `java -Dcheckstyle.suppressions.file=style/checkstyle-suppressions.xml -Dcheckstyle.importcontrol.file=style/import-control.xml
  -jar checkstyle.jar -c style/checkstyle-rules.xml <files>`. When the workspace JDK is older, tar the files and `style/` into
  `build/` (deletable without asking) and run it where Java 21 is available.


## LDAP review discipline

- Start at the shared seam. `LdapUtils` builds connection configs, filters, authenticators, DN/entry resolvers
  and person-attribute DAOs for every LDAP-backed feature; `LdapConnectionFactory` wraps every search, modify,
  add, delete and password-modify. Findings there generalize; findings in one feature module usually do not.
- Follow the whole flow: property model -> `LdapUtils` construction -> auto-configuration bean -> the calling
  repository or handler -> the webflow/endpoint boundary that consumes the result. Most real defects live in
  the mismatch between two of those layers, not inside one class.
- Search results are a trust boundary. Check the LDAP result code, not just whether an entry came back, and
  check what happens on truncation (server size/time/admin limits), on referral or continuation references,
  and on multi-valued attributes that directories return in ranges.
- Connection factories are pooled and stateful. Confirm they are created once as beans, that a pool used for
  user binds is passivated back to its original bind state, and that maps of factories are keyed by something
  that actually distinguishes two configurations.
- For any code that reads or writes a password, verify the transport requirement against the current RFC text
  and confirm the guard is enforcement rather than a log line; read the boolean carefully for inverted sense.
- Treat a fail-open authorization branch as critical regardless of how it is reached. Trace the default value
  of every property the branch depends on, including Spring Boot defaults CAS does not override.
- Watch for values that are written by one path and compared by another, and for filter/template parameters
  that are silently ignored when the operator's template does not reference them.
- Secrets and directory internals must not reach logs or released attributes: password-reset answers, bind
  credentials, diagnostic messages and matched DNs all deserve a second look.

## Startup performance review discipline

- Ask what runs, not what exists. The interesting question for an auto-configuration is not whether it is
  imported but what executes during condition evaluation, bean construction and the lifecycle events.
- Trace the three phases separately: context preparation and initializers, context refresh and bean
  creation, and the started/ready listeners. Duplicated work across two phases is a common defect —
  the same expensive operation performed once early and once late is easy to miss because each site
  looks reasonable on its own.
- The web application enables lazy initialization globally. Read `@Lazy(false)`, `InitializingBean`,
  `SmartInitializingSingleton` and event listeners as the real eager set, and follow their constructor
  parameters: a conditionally-disabled bean still builds every collaborator it declares directly, so use
  `ObjectProvider` on anything a `BeanSupplier` guard may decide not to use.
- Treat classpath scanning as a resource with a fixed answer. `ServiceLoader.load`, `classpath*:` patterns
  and resource-pattern resolvers should be evaluated once per JVM; finding one inside a loop, a factory
  method or a per-request path is a finding, not a nitpick.
- Check defaults from the operator's point of view. Instrumentation, validation and catalog-building
  features are useful when asked for and pure overhead otherwise; whichever way the default goes, the
  documentation table and the code must agree.
- Prefer deferral over deletion when the work is genuinely needed: build indexes on first access, resolve
  optional beans through providers, and let first-request initialization carry work that has no reason to
  block readiness.
- Startup timings cannot be verified in a sandboxed environment without a JDK matching the build and
  network access for Gradle. When that is the case, justify each change by naming the beans, scans or
  binds it removes from the critical path, and say plainly that no timing run was performed.
- Lazy initialization is a property of the running web application, not of the test suite. The setting
  lives in the web application's own resources, which are on the `bootRun` source set and not on any
  module's test classpath, so module tests construct the context eagerly. A change to whether a bean is
  eager therefore cannot be proven by module tests alone; they only show the eager path still works.
  Validate it through the puppeteer scenarios that exercise the affected area, and say which layer each
  result actually covers.
- Before removing or adding an eager marker, find the bean's real consumers rather than assuming the
  annotation is what registers it. Framework infrastructure usually discovers collaborators by type,
  and a type lookup resolves a lazy definition from its declared return type and instantiates it then;
  the annotation frequently only decides *when* that happens, not *whether* it happens.

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

## CAS protocol (v1/v2/v3 + SAML 1.1) review discipline

- Entry points to trace, in order: `AbstractServiceValidateController` ->
  `DefaultCentralAuthenticationService.validateServiceTicket` / `createProxyGrantingTicket` /
  `grantProxyTicket` -> `ServiceValidationViewFactory` -> the `protocol/{2.0,3.0}` mustache
  templates or `Saml10SuccessResponseView`. Review the whole chain; the interesting defects are in
  the ordering between these stages, not inside any one class.
- Ticket-state correctness rule for this codebase: read, check and mutate a ticket inside the same
  `lockRepository.execute(...)` block. `LockRepository` defaults to a JVM-local registry, so any
  invariant that must hold across nodes needs registry-level atomicity, not a lock.
- Protocol beans reached from a controller are singletons. Never introduce (or leave) per-request
  mutable state on them; bind request parameters into a local object instead.
- Anything derived from `pgtUrl`, `service`/`targetService`, or `TARGET` is attacker-controlled at
  the point CAS first touches it. Authorization checks on those values must be anchored/exact
  matches, and scheme restrictions must be enforced in code, not documented in Javadoc.
- Check response wire formats against the published CAS 3.0 protocol spec and the SAML 1.1
  browser/artifact profile, not from memory: required error codes, `<cas:proxies>` ordering,
  `InResponseTo`/`Recipient`/`Conditions` on the SAML 1.1 response.
- Cross-check every protocol change against these puppeteer scenarios:
  `cas-validation-protocol-v2`, `cas-validation-protocol-v3`, `ticket-validation-cas-renew`,
  `ticket-validation-saml1`, `ticket-validation-casv3-pgt`, `ticket-validation-casv3-pgtiou`,
  `ticket-validation-casv3-pgt-multiple-pt`, `ticket-validation-casv3-pgt-stateless`,
  `single-logout-cas-backchannel`, `single-logout-cas-frontchannel`. Note that some of them assert
  current non-compliant behavior (`ticket-validation-cas-renew` asserts
  `code="INVALID_TICKET"` where the spec wants `INVALID_TICKET_SPEC`), so a compliance fix means
  updating the scenario in the same change.
- Coverage gaps to be aware of: no scenario exercises concurrent validation of a single ticket, and
  `ticket-validation-saml1` always supplies a `RequestID`, so the SAML 1.1 default `InResponseTo`
  path is untested.

## Interrupt notifications

- Review the whole webflow path in `InterruptWebflowConfigurer`, not a single action: inquiry is wired at several
  states, and the request-scoped finalized marker decides whether later checkpoints re-run the inquirers.
- Treat the tracking cookie as a user-held acknowledgement. Changes to finalize/tracking must keep blocked
  responses unacknowledgeable and must consider which principal the cookie was issued for.
- Puppeteer interrupt scenarios often log out between logins, which removes the tracking cookie; a regression step for tracking must log in again without logging out.
- Passive requests (CAS `gateway`, OIDC `prompt=none`, SAML `IsPassive`) must not render the interrupt view. When adding an event to an action that is prepended to an existing action state, add the matching transition to that state; an unmatched event silently continues to the next action.
- A check that only runs in the login webflow is bypassed wherever a protocol module reuses the SSO session directly (for example the SAML2 IdP). For interrupts this is accepted for now; do not close it by vetoing SSO participation from the interrupt module without agreeing on the approach with the maintainer.
- Inquirer errors currently fail open; any fix must state the intended behavior for blocking policies.
- Reloadable inquirer state (files, watchers) must be published atomically; a failed reload keeps the last good contents rather than failing open.
- Payloads map onto `InterruptResponse`, whose no-arg constructor means "interrupt". Parse external responses only after the status check, and test error bodies against random-port `MockWebServer` instances.

## Groovy and scripting

- Start at `core/cas-server-core-scripting`; roughly fifty modules delegate to the same five classes, so
  changes there are changes to authentication, attribute release, MFA, AUP, OIDC, SAML and themes at once.
- `ScriptResourceCacheManager` is a singleton whose `close()` clears the entire cache — it must never be
  used in a try-with-resources block.
- Script execution is serialized per script instance on a blocking lock. Review every script-backed
  decision for what a `null` result means and prefer fail-closed defaults at security boundaries — MFA
  triggers and AUP read `null` as "skip". Never guard script execution with a timed `tryLock`; a timeout
  that returns `null` is indistinguishable from a script that declined.
- Do not compile scripts on a request path (`fromScript`/`fromResource` per call). Go through
  `resolveScriptableResource`, or hold the compiled script in a transient field, and remember `fromResource`
  also starts a file-watcher thread that nothing closes. The cache manager comes from
  `ApplicationContextProvider` and is absent outside Spring, so always fall back to the factory instead of
  throwing, or you will break components and tests that are constructed directly.
- Inline (`groovy { ... }`) and external (`file:`/`classpath:`) scripts behave differently: only external
  scripts honour `failOnError` and support reload, and only inline scripts support bindings (the
  `setBinding` interface default is a no-op). Inline scripts swallow `GroovyRuntimeException`. Test both
  forms, and cover bindings with concurrent and pooled-thread tests — `GroovyShellScriptTests.BindingTests`
  is the pattern.
- Treat compiling a Groovy script as running it: AST transformations execute inside the compiler. Script
  text that comes from anywhere other than deployment configuration must be parsed
  (`ScriptingUtils.validateGroovyScript`), never compiled.
- Puppeteer coverage lives in the 18 `ci/tests/puppeteer/scenarios/*groovy*` scenarios
  (`service-access-strategy-groovy`, `surrogate-login-groovy`, `mfa-provider-selection-trigger-groovy`,
  `interrupt-afterauthn-groovy`, `webflow-groovy-action`, …). None of them exercise concurrency, script
  errors, or cache expiry, so a change in this area needs new coverage rather than a rerun.

## Actuator endpoints review discipline

- There are ~63 endpoints. Inventory them with
  `grep -rl --include=*.java -E "@(RestControllerEndpoint|ControllerEndpoint|Endpoint|WebEndpoint)\(id"`;
  `support/cas-server-support-reports-core` owns about a third, the rest are scattered across
  feature modules. Review them as one attack surface, not one module at a time.
- The whole security boundary is a single `SecurityFilterChain` built by
  `CasWebSecurityConfigurerAdapter.configureHttpSecurity`. Rules are added in order --
  ignored patterns (permitAll) -> each `CasWebSecurityConfigurer.configure` -> per-endpoint
  `cas.monitor.endpoints.endpoint.*` -> `configureEndpointAccessToDenyUndefined` -> static
  resources -> form login -- and Spring Security takes the first match, so anything a
  `getIgnoredEndpoints` contributes wins over every endpoint rule that follows.
- `prepareProtocolEndpoint` emits `/<ep>**` as well as `/<ep>/**`. The first form is a
  within-segment wildcard, so an ignored endpoint also permits sibling paths sharing its
  prefix. Check that before adding an entry.
- That chain disables CSRF, security headers and logout globally, and it is the same chain
  that serves the actuator. Any browser-reachable session credential (`cas.monitor.endpoints.form-login-enabled`)
  therefore leaves mutating actuator operations unprotected against cross-site requests.
- `ActuatorEndpointProperties.access` defaults to `DENY` and the `endpoint` map always
  carries a synthetic `defaults` key. The adapter iterates the map and calls
  `EndpointRequest.to("defaults")`, which resolves to no path and silently registers nothing;
  the real default comes from `configureEndpointAccessToDenyUndefined`. Do not read the loop
  as the place defaults are enforced.
- `configureEndpointAccessToDenyUndefined` reads `management.endpoint.<rootPath>.access` with
  a raw `Environment.getProperty` call and maps `UNRESTRICTED`/`READ_ONLY` to `permitAll`.
  Relaxed spellings of that key are not seen (fail-closed), but the canonical spelling turns a
  Boot-level exposure decision into anonymous Spring Security access.
- `IP_ADDRESS` access is evaluated by `IpAddressAuthorizationManager`, which reads
  `cas.audit.engine.alternate-client-addr-header-name` (default `X-Forwarded-For`) straight off the
  request. This is intentional and is NOT a CAS defect: the fronting proxy, load balancer and
  external boot configuration are expected to set that header as a trusted header, or to blank it
  out and let Tomcat's own valves resolve the client address. Configuring that correctly is left to
  the deployer, so do not report header spoofing here as a finding.
- `BaseCasRestActuatorEndpoint` endpoints are NOT Spring Boot operations. They are discovered by
  CAS's own `RestActuatorEndpointDiscoverer`, mapped by `RestActuatorEndpointHandlerMapping`, and
  their handlers are Spring MVC mappings rather than `@ReadOperation`/`@WriteOperation` methods.
  Boot applies an access level by filtering operations, and these endpoints declare none, so
  `management.endpoint.<id>.access=READ_ONLY` does not stop their `@PostMapping`, `@PutMapping`,
  `@PatchMapping` or `@DeleteMapping` handlers the way it stops a Boot write operation. That is
  the gap `RestActuatorEndpointHandlerMapping.isMappingPermittedByAccessLevel` closes: it resolves
  the endpoint's access through the `EndpointAccessResolver` bean and applies the same rules Boot
  applies to operations, registering nothing under `NONE` and only `GET`/`HEAD`/`OPTIONS` mappings
  under `READ_ONLY`. Note that `configureEndpointAccessToDenyUndefined` maps `READ_ONLY` to
  `permitAll`, which is why the write methods used to get through, and maps `NONE` to `denyAll`, so
  a `NONE` endpoint is usually refused by security before the missing mapping matters.
  `ActuatorEndpointAccessControlTests` covers all of it, with `releaseAttributes` beside
  `registeredServices` as the Boot operation endpoint that has always behaved this way.
- Before changing what an access level does to these endpoints, measure the blast radius rather
  than reasoning from the annotation: every REST actuator endpoint declares
  `defaultAccess = Access.NONE` and the web application ships `management.endpoints.access.default=NONE`,
  which suggests widespread breakage, but in fact all 44 test classes and all 69 puppeteer scenarios
  that call one already declare `management.endpoint.<id>.access` or `management.endpoints.access.default`
  explicitly. Walk the test tree and `ci/tests/puppeteer/scenarios/*/script.json` and count.
- Do not confuse that with exposure. A REST actuator endpoint left out of
  `management.endpoints.web.exposure.include` answers `403`, not `200`: no rule is registered for
  it in `configureEndpointAccessToDenyUndefined`, and Spring Security denies a request no matcher
  claims. Verify the status a given combination of exposure and access actually produces before
  describing any of it as reachable.
- When `management.server.port` differs, `CasCoreWebManagementContextConfiguration` registers the
  handler mappings in the child management context but no `SecurityFilterChain`. Verify what
  actually secures that port before assuming the main chain applies.
- Several endpoints are authentication primitives, not diagnostics: `casValidate`,
  `samlValidate`, `samlPostProfileResponse` and `tokenAuth` mint CAS assertions, signed SAML
  responses and JWTs for an arbitrary username, and the `password` parameter is optional (absent
  entirely for `tokenAuth`) -- with no password they fall through to `PrincipalResolver` and
  succeed. Anything that widens actuator access widens impersonation.
- These endpoints return configuration values and service definitions unmasked, unlike Boot's own
  `/env` and `/configprops`. `casConfig` `/retrieve` returns raw property values and
  `registeredServices` serializes `OAuthRegisteredService.clientSecrets` (the `value` field carries
  no `@JsonIgnore`). Masking was proposed and rejected, so do not re-propose it without agreeing
  the approach with the maintainer first. `casConfig` `/encrypt` and `/decrypt` are a deliberate
  operator tool that Palantir exposes in its UI, not an oracle to close.
- Before proposing masking on any actuator response, work out how the admin UI consumes it: grep
  `support/cas-server-support-thymeleaf/src/main/resources/static/js/palantir-*.js`. Palantir's
  service editor does a full `GET /registeredServices/{id}` -> edit -> `PUT` round-trip
  (`palantir-services.js`), so masking any path it reads would write the mask back into the
  registry, and `/export` has the same requirement; redacting there needs the save path to preserve
  existing secrets first. `casConfig` `/retrieve` does not have that problem, because
  `palantir-pac4j.js` reads only `name` and `propertySource` from it.
- User-supplied strings are compiled into regular expressions on the request path:
  `configurationMetadata/{term}` (then matched over every known property in a `parallelStream`)
  and `casConfig` `/retrieve`. Treat these as ReDoS/CPU-exhaustion boundaries; actuator endpoints
  are not covered by CAS's authentication throttling.
- No actuator endpoint carries `@Audit`. Session destruction, service deletion, key rotation,
  configuration mutation and token minting all happen without an Inspektr record. Check for this
  before claiming an administrative action is traceable.
- Puppeteer coverage: `actuator-endpoint-cors`, `actuator-endpoint-login-jdbc`,
  `actuator-endpoint-management`, `actuator-endpoint-metrics`, `actuator-endpoint-reports`,
  `actuator-endpoint-roles`, `actuator-form-login`, `actuator-form-login-ldap`,
  `actuator-form-login-ldap-roles`, `actuator-form-login-ldap-userauthz`,
  `configuration-refresh-endpoint`, `sso-sessions-endpoint`, `tomcat-forwarded-headers`
  (the only `IP_ADDRESS` scenario). Every one of them asserts that an *authorized* caller gets
  200; none asserts 401/403 for an unauthenticated one, and 158 scenarios run with
  `management.endpoints.web.exposure.include=*`. A change to endpoint security therefore needs a
  new negative scenario rather than a rerun.

## SAML2 metadata review discipline

- There are two independent metadata subsystems and they share almost nothing. *SP metadata*
  runs `SamlRegisteredService` -> `SamlRegisteredServiceDefaultCachingMetadataResolver` ->
  `SamlRegisteredServiceMetadataResolverCacheLoader` -> the resolvers under
  `.../cache/resolver/**` -> OpenSAML filters. *IdP metadata* runs
  `SamlIdPMetadataGenerator` -> `SamlIdPMetadataLocator` (one per storage backend) ->
  `SamlIdPMetadataResolver` -> `SamlIdPMetadataCredentialResolver`. A third, `saml-mdui-core`,
  is a separate adapter stack that does not reuse either. Review each as a path.
- `SamlIdPMetadataResolver` is a singleton that calls `setMetadataRootElement` — which swaps
  `AbstractBatchMetadataResolver`'s backing store — on every cache miss, then reads it back.
  Anything reached through `SamlIdPMetadataCredentialResolver` inherits that shared state, so
  per-service IdP metadata is where concurrency defects in this area live. That swap-then-read
  pair is now held under a `CasReentrantLock` on the miss path only, with the cache re-checked
  inside the lock; `setMetadataRootElement` is `protected` and its javadoc carries the calling
  contract. Anything new that installs a backing store on that singleton has to take the same
  lock, and results handed out of the resolver must be materialized (`List.copyOf`) rather than
  left as a lazy view over a store a later resolution replaces. There is no puppeteer scenario
  for per-service IdP metadata — no scenario sets `idpMetadataLocation` — so this path is
  covered by `SamlIdPMetadataResolverTests` alone.
- CAS does not use OpenSAML's `HTTPMetadataResolver` / `FileBackedHTTPMetadataResolver`.
  `UrlResourceMetadataResolver` reimplements fetch-and-back-up by hand, which is why the usual
  guarantees are absent: no background refresh timer and no min/max refresh delay. Do not assume
  OpenSAML semantics when reading this code. The backup file is now treated the way that class
  would treat it: `force-metadata-refresh` (default `true`) means "do not serve the backup
  without checking the remote first", never "delete the backup", a successful download
  overwrites it in place, and `resolveMetadataLocation` falls back onto it when the download
  cannot be completed. The two readers of the backup want different answers about root validity
  — reusing it instead of contacting the remote requires `Boolean.TRUE.equals(isRootValid())`,
  falling back onto it only requires `!Boolean.FALSE.equals(...)`, because a document with no
  `validUntil` reports its validity as unknown. Keeping the backup also makes MDQ's
  `If-None-Match` path reachable for the first time.
- The MDQ subclass has its own failure vocabulary and it interacts with that fallback.
  `fetchMetadata` returns `null` rather than throwing when the server is unreachable *and* a
  backup exists, so `getMetadataResolverFromResponse` must tolerate a null response; it still
  throws `UnauthorizedServiceException` when there is nothing to fall back onto, and
  `resolveMetadataLocation` rethrows that rather than swallowing it, which is what
  `MetadataQueryProtocolMetadataResolverTests.verifyResolverFails` asserts. Read the status
  with `HttpStatus.resolve`, never `valueOf`, or a non-standard code throws instead of
  reaching the fallback.
- The metadata backup file is named `sha(metadataLocation)` while the Caffeine key is
  `serviceId|metadataLocation`. Two services pointing at one URL are two cache entries over one
  file, and nothing upstream serializes them. The sharing is deliberate — a federation aggregate
  should not be downloaded once per service — so the fix was to make the write atomic:
  `writeMetadataToBackupFile` writes a sibling temporary file and moves it into place, falling back
  to `REPLACE_EXISTING` where `ATOMIC_MOVE` is unsupported, rather than giving each service its own
  copy or holding a lock across a download. Anything new that writes into the backup directory goes
  through that method, and remember the `.tmp` siblings when writing directory assertions.
- Trust here is opt-in, and it used to fail open as well. A service without
  `metadataSignatureLocation` still gets no `SignatureValidationFilter` at all — that is the
  operator's choice, not a defect. What changed is the other half:
  `SamlUtils.buildSignatureValidationFilter` still returns `null` rather than throwing when the
  configured resource cannot be read, but `addSignatureValidationFilterIfNeeded` now treats that
  null as fatal and throws. It is only reached once something has established that the metadata is
  meant to be verified — the service defines a signature location, or the stored document carries a
  signature — so a null there means the verification that was asked for cannot be performed. Every
  resolver catches the failure and yields no metadata, which is the intended outcome; do not
  "fix" that by restoring the warning.
- `buildRequiredValidUntilFilterIfNeeded` only runs when `metadataMaxValidity > 0`, and
  `SamlRegisteredServiceMetadataExpirationPolicy` reads `cacheDuration` but never `validUntil`.
  Expiry and validity are two different mechanisms in this code; do not conflate them. The
  policy can also return a negative duration when a service expiration date is in the past.
- IdP metadata backends are not interchangeable, and what to check in each is whether the *global*
  lookup is scoped. JPA and MongoDB used an unfiltered "first row" query
  (`SELECT r FROM SamlIdPMetadataDocument r`, `findOne(new Query())`) and so could answer with a
  per-service document and its keys; both now query `appliesTo` for the global owner like any other.
  The rest were already scoped: Redis scans by `appliesTo`, DynamoDB queries it, GCP uses it as the
  blob id, S3 as the bucket, Git as the directory. REST sends `appliesTo` only for a service and
  leaves the global case to the remote server, which is that endpoint's contract rather than a CAS
  defect. `appliesTo` is `SamlIdPUtils.getSamlIdPMetadataOwner` = `name + '-' + id`, which also
  becomes a directory name on the file-system locator and a key elsewhere — unsanitized, and it
  changes when a service is renamed.
- Per-service IdP metadata is not something CAS generates. `SamlIdPMetadataLocator
  .shouldGenerateMetadataFor` is `registeredService.isEmpty()` and no backend overrides it, so the
  generator only ever mints the global document — at context startup, from each backend generator's
  `afterPropertiesSet`. A per-service document exists only where an operator provisioned one: a
  directory named by `idpMetadataLocation` for the file-system locator, or rows and documents
  inserted directly for JDBC and MongoDB.
- The guard in front of generation reinforces that. `AbstractSamlIdPMetadataLocator.exists` calls
  `fetch`, and every per-service `fetchInternal` falls back to the global document when the service
  has none, so `exists(service)` is true as soon as the *global* document exists. Both
  `BaseSamlIdPMetadataGenerator.generate` and `SamlIdPMetadataResolver.resolveMetadata` check
  `exists` before `shouldGenerateMetadataFor`, which means `generate(Optional.of(service))` is a
  no-op in a normally-started deployment and returns the global document. That fallback is the
  intended behaviour — a service without its own metadata uses the IdP's — not a defect.
- Two consequences worth carrying. Scoping the global lookup matters only for deployments that
  provision per-service rows, so do not describe it as reachable out of the box. And a test cannot
  create a per-service JDBC or MongoDB document through the generator: it has to insert one the way
  an operator would, through the entity manager or `mongoTemplate`, and on JDBC through the
  dialect-specific `JpaSamlIdPMetadataDocumentFactory`. `JpaSamlIdPMetadataGeneratorTests
  .verifyService` looks like it covers this and does not — its `assertNotNull`s are satisfied by the
  global fallback.
- The file-system generator writes the IdP signing and encryption private keys in the clear
  with default permissions; the other backends run the key through `metadataCipherExecutor`.
  Do not describe key-at-rest handling as uniform across backends.
- `/idp/metadata` is public and calls `generate(...)` on every request, including with a
  caller-supplied `service` parameter. Read that together with the two bullets above before
  calling it a resource-exhaustion vector, which is the mistake made once in this repo: generation
  is guarded by an `exists()` check that the global document already satisfies, so in a started
  deployment the endpoint mints nothing and simply returns the global metadata. What is genuinely
  unguarded is the first generation, before any document exists — no lock across threads or nodes,
  and two RSA keypairs at a default `key-size` of 4096.
- The parser pool in `CasCoreSamlAutoConfiguration` is properly hardened (doctype disallowed,
  external entities off, secure processing on), so XXE is not the gap. Response size is:
  metadata bodies are read with `IOUtils.toString` with no bound.
- `HttpUtils.execute` used to build a `CloseableHttpClient`, with its own pooling connection
  manager, per call and never close it. The response handler buffers the entity into a
  `ByteArrayEntity`, so the connection did return to that pool — and the pool was then dropped with
  an idle socket in it, no evictor, and nothing that would ever lease from it again, so the socket
  outlived its TTL. Clients are now reused, keyed on everything that decides their construction:
  the `HttpClientFactory` identity (a sentinel when the request carries no CAS `HttpClient`),
  `redirectsEnabled`, and whether automatic retries are left on. Timeouts are static and so are not
  in the key. Anything with a proxy or DNS overrides still gets a client of its own and is closed
  when the request finishes; so is anything past the registry bound.
- Three things follow from that sharing, and all three are load-bearing. Shared clients carry
  explicit pool limits, because a per-call client had a pool to itself and the library's defaults
  of 25 total and 5 per route would otherwise become a ceiling. Shared connections revalidate on
  every lease, because a pooled connection can outlive the server it points at — a restarted
  service, a rotated load balancer, or a `MockWebServer` a test just closed. And it is only safe to
  close a client the moment `execute` returns because the handler already buffered the body; a
  change that streams the response instead would break that.
- Connection reuse is bounded by `connectionTimeToLive`, which defaults to five seconds
  (`org.apereo.cas.util.http.HttpUtils.connectionTimeToLive`). Bursts reuse connections; steady
  low-rate traffic will still reconnect. Raising it is an operator decision, not a CAS default to
  change quietly.
- Check MDQ against the SAML profile for the Metadata Query Protocol, not from memory: a compliant
  client MUST send `Accept: application/samlmetadata+xml`. CAS used to put
  `cas.authn.saml-idp.metadata.mdq.supported-content-type` on `Content-Type` of a GET and then send
  `Accept: */*`, so negotiation never happened; that property is now the `Accept` value and its
  default is the media type the profile requires. Signature verification is RECOMMENDED for servers
  there, not a client MUST, so do not report the optional filter as a spec violation — report it as
  fail-open.
- The MDQ resolver also used to drop `.httpClient(...)` from its request, so it fell back to a
  default SSL context instead of the deployment's trust store and hostname verifier while the plain
  URL resolver used them. `httpClient` is `protected` on `UrlResourceMetadataResolver` for that
  reason; any new request built in this family passes it.
- The two request-path amplifiers here are fixed, and the shape of each fix is worth keeping.
  `SamlRegisteredServiceMetadataHealthIndicator` still asks `isAvailable(...)` of every SAML
  service on every poll, but the URL resolver now answers from the metadata backup file when one
  exists and only probes the network for a service with no local copy — the question is whether CAS
  can serve the service, and a local copy answers it. `DynamicMetadataResolverAdapter` caches
  resolved entity descriptors *and* misses (bounded, expiring on
  `cas.saml-metadata-ui.schedule.repeat-interval`), because the entity id driving the fetch arrives
  on an unauthenticated login request; caching the misses is what bounds what a caller can provoke.
  Its rebuild replaces a resolver every request shares, so build-then-read is under a
  `CasReentrantLock`, the same remedy as `SamlIdPMetadataResolver`.
- `MetadataUIUtils` no longer has `isMetadataFoundForEntityId` or the overload of
  `locateMetadataUserInterfaceForEntityId` that takes an adapter. Both resolved the entity
  descriptor themselves, and the login action called one after the other, so every render resolved
  twice. The action resolves once and passes the descriptor down; do not reintroduce a helper that
  resolves internally.
- Puppeteer coverage: `saml2-idp-metadata-caching`, `saml2-idp-login-idp-initiated-mdq`,
  `saml2-idp-login-sp-metadata-{directory,groovy,jdbc,json,mongodb}`,
  `saml2-idp-login-sp-override-metadata`, `saml2-idp-login-metadata-aws-s3`, `saml-mdui`,
  `delegated-login-saml2-mongodb-metadata`. All are single-threaded happy paths against a
  reachable metadata source, so nothing here covers concurrency, a metadata host that is down,
  signature-validation failure, or MDQ content negotiation. Changes in those areas need new
  scenarios rather than a rerun.

## Attribute consent review discipline

- Start at `ConsentDecision`, not at a repository. `id` is a primitive `long` with
  `@GeneratedValue`, so only JPA generates one. That single field is the key in four
  backends: LDAP merges by it, Redis keys on `ConsentDecision:<principal>:<id>`, Mongo maps
  it to `_id` by naming convention, and DynamoDB uses it as the table HASH key. It used to
  stay `0` on every one of them, which collapsed each user (LDAP, Redis) or the whole
  deployment (Mongo, DynamoDB) onto a single decision. `DefaultConsentDecisionBuilder.build`
  now assigns a random positive id, JPA still replaces it with a generated one, and LDAP's
  `mergeDecision` assigns one when `id <= 0`. Anything that creates a `ConsentDecision`
  outside the builder -- the actuator's `/import`, say -- still arrives with id `0`.
- `BaseConsentRepositoryTests` used to call `decision.setId(1/100/200)` before every store,
  which is why the id defect never failed a test. `verifyMultipleDecisionsForPrincipal` now
  stores builder-produced decisions for two users across two services and then deletes by id
  and by principal, against every backend; `getOtherUser()` is overridden where the second
  user must already exist (LDAP). The class holds a `@ResourceLock` because
  `verifyDeleteRecordsForPrincipal` calls `deleteAll()` on the shared repository.
- Consent is a gate in the login webflow, not a filter at attribute release.
  `ConsentWebflowConfigurer` prepends `CheckConsentRequiredAction` to the login flow's
  `generateServiceTicket` state; the only other consumer is
  `SamlIdPConsentSingleSignOnParticipationStrategy`. Nothing in
  `RegisteredServiceAttributeReleasePolicy` reads a stored decision, so the consented set
  never constrains what is released, and any path that issues without the login flow --
  REST protocol tickets, proxy tickets, OIDC refresh/userinfo, the OpenID4VCI
  pre-authorized code grant -- releases attributes with no consent check at all.
- Follow `ConsentQueryResult`. It carries the decision, the service and now the resolved
  consentable attributes, and it used to be thrown away: `ConsentActivationStrategy` returned
  a bare boolean and `CheckConsentRequiredAction` reduced it to an event, so
  `prepareConsentForRequestContext` repeated the full `getConsentableAttributes` evaluation
  (person directory included) and the repository read in the same request. The strategy now
  returns the result and the action hands it to `prepareConsentForRequestContext`, which
  resolves nothing when the result carries attributes -- a result built by a custom strategy
  carries none and still falls back to resolving. What remains per consent interaction is one
  evaluation and read for the prompt, one of each inside `storeConsentDecision` on confirm,
  and one more from the `generateServiceTicketAfterConsent` clone that re-runs the check;
  `getRegisteredServiceForConsent`, and with it the access strategy, still runs twice per
  prompt. Count these by request before claiming a number.
- `DefaultConsentEngine.executeRepositoryOperation` calls `toConsentRepository(tenant)` on
  every operation, and `TenantJdbcConsentRepositoryBuilder` builds a DataSource plus a
  Hibernate `EntityManagerFactory` inside it, then destroys them. Check the multitenant
  path before judging consent performance; the single-tenant path is not representative.
- Repository lookups must be checked for operator and pattern correctness, not just for
  the right column names. `DynamoDbConsentFacilitator` matched SERVICE and ID with
  `ComparisonOperator.GE` (correct for the date ranges other CAS DynamoDB facilitators
  use it for, wrong for an equality lookup); it now uses `EQ`, but every read is still a
  full `scanPaginator` because `id` is the table's only key. `RedisConsentRepository`
  interpolated the raw principal into a SCAN glob; it now escapes glob characters and keeps
  only keys whose remainder is a numeric id, because the `:` delimiter alone lets principal
  `alice` reach the decisions of `alice:x`. Deleting one decision there is a `DEL` of the
  exact key rather than a scan.
- Deletes deserve the same reading as reads. `JpaConsentRepository.deleteConsentDecisions`
  used `getSingleResult()` and so silently deleted nothing for any user with more than one
  decision; it is a bulk JPQL delete now. `ChainingConsentRepository` -- which is what the
  `consentRepository` bean always is, wrapping every configured store -- stored to every
  repository but deleted with `anyMatch`, stopping at the first success; it now asks every
  repository. `RestfulConsentRepository` sent `id` where the documented contract and the
  test's own mock server say `decisionId`, so a conforming server read a single revocation
  as "revoke everything". Revocation failing quietly is worse than consent failing loudly.
- A decision that will not decipher used to be fatal: `getConsentableAttributesFrom` turned any
  failure into `IllegalArgumentException`, which propagated out of `isConsentRequiredFor` and out
  of the login flow, so key rotation or a node without the shared `cas.consent.core.crypto` keys
  locked the user out. It now logs and returns no attributes, which reads as a mismatch and
  re-prompts; the new decision is written back with the current keys. Weigh every failure on this
  path the same way -- the login flow has no recovery for an exception thrown from the consent
  check, so the question is always whether re-prompting is safe, not whether the error is correct.
- `ConfirmConsentAction` parsed `option`, `reminder` and `reminderTimeUnit` straight off the request
  with `Integer.parseInt` / `Long.parseLong` / `ChronoUnit.valueOf` and persisted them unvalidated, so
  a missing parameter was a 500 on the confirm POST and `ChronoUnit.FOREVER` or `ERAS` parsed fine and
  then threw from `createdDate.plus(...)` on every later login. Each value now falls back on the
  configured default, and the pair is rejected unless it can actually be applied to a date -- checked by
  applying it, since `LocalDateTime.isSupported` accepts `ERAS` and only the ISO era range rejects it.
  `DefaultConsentEngine.calculateReminderExpirationDate` is the second half of that: a stored reminder
  that cannot produce a date means consent is required, never an exception, which is what protects
  decisions that were written before this validation existed or through the actuator's import.
- `ConsentDecision.createdDate` is a zone-less `LocalDateTime` that one node writes and another compares
  against its own clock, so it is produced with `ZoneOffset.UTC`, not `ZoneId.systemDefault()`. Before that,
  the backends disagreed with each other: JPA, Redis, LDAP, DynamoDB and REST keep the wall-clock value
  verbatim, so a node in another zone read a decision as hours older or newer, while MongoDB was accidentally
  correct because Spring Data converts `LocalDateTime` through the system zone in both directions and so
  normalizes to the instant. The reminder units offered in the consent view go down to seconds, so that skew
  is not academic. `OneTimeToken.issuedDateTime` and the Google Authenticator token repositories still carry
  the same pattern with second-scale windows.
- `cas.consent.core.active` defaults to `true`, so adding the module turns consent on for
  every service that releases attributes. Weigh findings about the consent path as if it
  is always on.
- Wallet/OID4VCI integration is the real gap, not a defect in the consent modules:
  `BaseOidcVerifiableCredentialEncoder` reads `principal.getAttributes()` directly against
  the issuer's configured claim list, so a credential handed to a wallet carries no
  consent record, no per-claim choice, and no revocation. Selective disclosure exists in
  the SD-JWT encoder but the holder never chooses what is disclosed at issuance time.
- Puppeteer coverage: `attribute-consent` (in-memory repository, one user, one service),
  `multitenancy-consent-jdbc`, `saml2-idp-login-consent`, `saml2-idp-login-consent-with-sso`.
  No scenario exercises LDAP, Redis, Mongo, DynamoDB or REST consent, and none stores two
  decisions for the same user, which is exactly the shape every storage defect above needs.

## MongoDB backends (service registry and ticket registry)

- Three modules carry everything: `cas-server-support-mongo-core` (`MongoDbConnectionFactory`,
  `BaseConverters`, `DefaultCasMongoTemplate`, `MongoDbClusterTopologyManager`),
  `cas-server-support-mongo-service-registry`, and `cas-server-support-mongo-ticket-registry`
  (registry, `MongoDbTicketDocument`, `MongoDbTicketRegistryFacilitator`, catalog provider).
  Findings in the core factory apply to all ~19 `*-mongo` support modules at once; findings in a
  registry usually do not generalize.
- `MongoDbConnectionFactory.buildMongoDbClient` has two branches and they are not equivalent. The
  `client-uri` branch applies the connection string and nothing else: no `CasSSLContext`, no
  `ZonedDateTimeCodecProvider`, no pool/socket/server settings, no read or write concern, no
  `retryWrites`. A connection string cannot express a custom trust store, so a deployment using
  `client-uri` silently falls back to the JVM default trust material. Check which branch a setting
  lives in before describing it as configurable. (Still open.)
- Read the defaults from the operator's point of view before judging a flow: `ssl-enabled=false`,
  ticket-registry `crypto.enabled=false`, `write-concern=ACKNOWLEDGED`, `read-concern=AVAILABLE`,
  `retry-writes=false`. With the cipher off, `AbstractTicketRegistry.collectAndDigestTicketAttributes`
  returns attributes undigested, so the Mongo document carries the principal, the service and every
  authentication attribute in clear beside the ticket JSON. (Still open.)
- Durability is part of the security argument here. `deleteTicket` is a compare-and-swap only as far
  as the write concern makes the delete durable; `w:1` plus a replica-set failover can roll back the
  removal of a one-time-use ticket, code or nonce. Evaluate single-use state against the write
  concern, not just against the code path. (Still open.)
- Bean-construction side effects in a refresh-scoped bean are runtime operations, not startup
  operations: the bean method runs again on every `/actuator/refresh`. Destructive setup
  (`drop-collection`, `drop-indexes`) therefore lives in a plain-singleton `InitializingBean` that
  the refresh-scoped registry declares with `@DependsOn`, so singleton scope is what limits it to
  one execution per context. Express "startup only" through bean scope, not through a flag the
  configuration class retains; a configuration class holding mutable state to remember whether it
  has run is the shape to avoid. Do not mark those initializers `@Lazy(false)` either -- the
  `@DependsOn` edge already orders them, and eager marking would move Mongo I/O onto the startup
  path of an application that otherwise initializes lazily.
- Do NOT close a Mongo client from a bean's disposal here, and treat the leaked clients as a known
  cost rather than a bug to patch locally. CAS declares ~2400 beans
  `@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)`, and `ScopedProxyMode.DEFAULT` means NO scoped
  proxy: consumers hold the instance itself, not a proxy that re-resolves. `RefreshScope.refreshAll()`
  therefore disposes objects that live consumers are still using. Making `DefaultCasMongoTemplate` a
  `DisposableBean` that closed its client broke `/actuator/health` in the
  `mongodb-ticket-service-registry` scenario: `BeanContainer.destroy()` disposed the templates inside
  `mongoHealthIndicatorTemplate`, while Spring Boot's health registry -- not refresh-scoped, so
  outside the refresh cascade -- still held the `CompositeHealthIndicator` built over them, and the
  next check failed with `ClientSessionException: state should be: open`. The login path survived only
  because its whole chain is refresh-scoped and was rebuilt together, which is what makes this failure
  mode easy to miss.
- Carry two rules from that. A resource held behind a refresh-scoped bean cannot be released by
  disposal alone -- the owner's lifetime has to match its consumers', which is a scoped-proxy
  decision, not a `destroy()` one. And never reason about refresh behaviour from the annotation
  alone: `ScopedProxyMode.DEFAULT` is the opposite of what the name suggests.
- For the record, Spring Data will not release these clients either:
  `SimpleMongoClientDatabaseFactory(MongoClient, String)` records the client as externally managed and
  `MongoDatabaseFactorySupport.destroy()` is `if (mongoInstanceCreated) { closeClient(); }`. CAS always
  hands in its own client, because a connection string cannot express the `MongoClientSettings` it
  needs. So the clients really are never closed -- that is the cost of the model above, not an
  oversight at the template.
- `BeanContainer` extends `DisposableBean`, and `ListBeanContainer.destroy()` disposes every
  `DisposableBean` entry and closes every `Closeable` one. That is exactly how the health templates
  came to be closed: a container of resources is not a leak, it is a disposal amplifier.
- `mongoDbTicketRegistryTemplate` is `@Primary`. Any module injecting `MongoTemplate` by type gets
  the ticket-registry database; check the qualifier before assuming a module talks to its own.
  (Still open.)
- The ticket registry propagates storage failures rather than returning a benign value:
  `addSingleTicket` and `updateTicket` declare `throws Exception`, and a ticket definition missing
  from the catalog throws. Only a genuine miss returns null. Do not reintroduce a `catch (Throwable)`
  that logs and continues — that is what made a failed insert look like a successful login. When
  reviewing any storage backend, read the catch blocks before the happy path.
- Multi-collection reads go through `MongoDbTicketRegistry.streamTicketDocuments`, which records
  every cursor it opens and closes them from the returned stream's `onClose`; a consumer that
  short-circuits never lets `flatMap` close the inner stream it was reading. Callers must close the
  stream. Per-collection queries are bounded by `limitQuery` to `from + count`, because no single
  collection can contribute more than that to the result, while the global skip/limit stays in the
  JVM since it spans collections — do not push `skip` down, it does not mean the same thing there.
- Collection names are resolved once per call and `distinct()`-ed, so definitions sharing a storage
  name are read and counted once.
- Writer and reader must agree on key mapping. `MappingMongoConverter` is configured with
  `MongoDbConnectionFactory.MAP_KEY_DOT_REPLACEMENT`, and `digestAttributeKey` applies the same
  replacement when a query addresses `attributes.<key>`. Check the converter's configuration
  whenever new code addresses a map field by path.
- `IDX_PRINCIPAL` is created on every ticket collection, not only on the ticket-granting ticket
  collection, because `deleteTicketsFor` and the principal criteria run against all of them.
- `casTicketRegistryLockRepository` exists for Redis and JPA and does not exist for Mongo, so
  `LockRepository` is JVM-local on a Mongo cluster. `updateTicket` is an unconditional `updateFirst`
  with no version check, so every read-modify-write invariant is last-writer-wins across nodes.
  (Still open.)
- Puppeteer coverage is `mongodb-ticket-service-registry` only. It refreshes the context first, then
  clears sessions, logs in once and asserts exactly one ticket-granting ticket, the health indicator
  and the ticket-registry cleaner — so it exercises the refresh path but asserts nothing about what
  survives a refresh. There is no crypto-enabled variant, no TLS and no concurrency, so a change in
  this area needs a new scenario rather than a rerun. `http-session-mongodb`, `authn-events-mongodb`,
  `configuration-properties-mongodb`, `simple-mfa-trusted-device-mongodb`,
  `saml2-idp-login-sp-metadata-mongodb` and `delegated-login-saml2-mongodb-metadata` exercise the
  shared connection factory and are the ones to re-run for any `cas-server-support-mongo-core` change.
- `MongoDbTicketRegistryTests` clears the whole registry in `@BeforeEach` while JUnit runs its
  methods concurrently, so any new test there must assert on identifiers it created itself (a UUID
  principal or attribute value) rather than on registry-wide counts.

## Browser storage cookie fallback

- `BROWSER_STORAGE` state (Duo Universal Prompt, SAML IdP, stateless ticket registry, account profile) goes through the
  shared `storage/casBrowserStorageWriteView` and `ReadView`, which call `writeToBrowserStorage` / `readFromBrowserStorage`
  in `cas.js`. When local or session storage throws or is unavailable, the payload is written to chunked cookies
  (`CasBrowserStorage_<context>_<i>` plus a `_n` count cookie, Secure on https, `SameSite=Lax`, host-only, path = CAS
  context path from `casBrowserStorageCookiePath` in `fragments/scripts.html`). The read view merges them (cookies win,
  since a successful storage write clears the cookie copy) and posts the usual `browserStorage` parameter, so no server
  read path changed. The login fails only when cookies are refused too.
- The context segment of the cookie name is encoded identically in `cas.js` (`encodeBrowserStorageCookieToken`) and in
  `WebUtils.removeBrowserStorageCookies`: keep `A-Z a-z 0-9 - . ~`, percent-encode every other UTF-8 byte, so `_` is an
  unambiguous separator. Change both together.
- Do not clear the cookies on read generically: the stateless ticket registry reads its TGT payload on every login.
  Single-use consumers clear their own context server-side (Duo does, in `DuoSecurityUniversalPromptValidateLoginAction`);
  logout and the 422 page clear all of them.
- The Duo payload is the serialized, encrypted flow state, likely tens of KB and 10+ cookies (not yet measured in a run). CAS accepts 500KB headers, but
  fronting proxies often cap a header at 8-16KB; that is deployment configuration, documented on the Duo page.
- Scenario `mfa-duo-universal-login-storage-fails` covers both halves: storage broken with cookies falling back to a full
  Duo login, and storage plus script cookies broken showing the error panel.

## Stateless ticket registry review discipline

- The ticket id is the ticket: prefix + base64url(AES-GCM(header + compact string)). The header is one byte for raw or
  raw-deflated (whichever is smaller), then the length and bytes of the prefix the ticket was issued under; reads reject
  a prefix mismatch, since the prefix outside the ciphertext picks the compactor and some layouts have the same field
  count (AT/RT, PT/PGT). The compact string is `CompactTicketCodec`: `1;` then `<length>:<value>` per
  field, lists nested the same way, so values are never escaped. Store enums by `name()`, never by ordinal. Compactors add fields in `compactFields` and read them
  with `parse(value, exactCount)`; bump `CompactTicketCodec.VERSION` when a layout changes, and say in the release notes
  that tickets issued before the upgrade are unreadable.
- Module layout: `TicketCompactor`, `CompactTicketCodec`, `CompactTicketAuthentication` and the core compactors live in
  `org.apereo.cas.ticket.registry.compact`, with `StatelessTicketRegistry` and `ShortenedServiceMatchingStrategy`, in
  `support/cas-server-support-stateless-ticket-registry-api`; the `stateless-ticket-registry` module keeps the
  auto-configuration. Modules that ship their own compactors (OAuth core, Simple MFA core) depend on the API module
  `compileOnly`, so their stateless bean configurations carry `@ConditionalOnClass(StatelessTicketRegistry.class)` next to
  the feature condition (the feature is enabled by default, so without the class check every deployment without the
  stateless module fails to start). Native hints for compactors: `CasStatelessTicketRegistryRuntimeHints` in the API module.
- Authentication in ST, PT, PGT and OAuth tickets is `CompactTicketAuthentication`: principal id, authentication date,
  handlers, credential types, remember-me and the retained authentication attributes (`clientName`, MFA context,
  trusted device) as text. Other attributes are not kept (documented caveat).
- `expand` creates tickets through the ticket factories, then sets the creation time and a
  `FixedInstantExpirationPolicy`; the registry then sets the id it was looked up by.
- The TGT compact form is its authentication only, as typed JSON with principal attributes stripped; updates of
  `services`, `proxyGrantingTickets` or `descendantTickets` never reach the TGC, and single logout is documented as unsupported.
- No local decode cache: expanded tickets are mutable per request, the crypto and inflate cost is small next to the JSON
  parse, and a cache is server-side state.
- There is no delete: `deleteSingleTicket` is the base no-op, so `deleteTicket(...)` returns 0 and nothing is ever
  consumed. Code that treats `delete > 0` as the single-use decision fails closed here; code that uses a deterministic
  TST id as a replay marker (`TransientSessionTicketFactory.normalizeTicketId`: DPoP, client assertions, Heimdall)
  never finds it and fails open. Callers must use the ticket `addTicket` returns: the stored id is re-encoded.
- `TransientSessionTicketCompactor` stringifies properties, so only flat string properties survive, and a one-element list
  comes back as a plain string (read list properties with `CollectionUtils.toCollection`); object-valued TSTs
  (VP transactions and results) do not. It keeps the full service id: flows resume with that service
  (delegation back to the SAML2 IdP callback with `srid`/`entityId`), so `getShortenedId` is only for tickets that are
  validated against a presented service (ST, PT, PGT, OAuth).
- Maintainer decision: the stateless registry stays 100% stateless, in the spirit of the Shibboleth IdP client-side
  storage. Never propose a server-side replay, nonce or single-use store as the fix for anything here; fixes must be
  expressible in the ticket or client storage itself (encoding, binding, lifetimes, key versioning).
- `CompactTicketCodecTests`, `StatelessTicketRegistryTests.verifyServiceTicketFieldsCannotBeInjectedThroughService`,
  `verifyServiceTicketForDistinguishedNamePrincipal` and `verifyExpandedTicketsCarryTheirStatelessIds` guard the format.
- The stateless TGT is carried by the TGC exactly as with any other registry: `SendTicketGrantingTicketAction` sets the
  TGC with the (compacted, encrypted) TGT id, and the TGC value manager signs and encrypts it as usual. Maintainer rule:
  use the existing TGC behavior unchanged; no parallel cookies, bindings, digests or browser-storage copies of the TGT,
  and no stateless-only webflow wiring for the SSO session. Browser storage remains for Duo and the SAML IdP only.
- Browsers drop cookies over 4096 bytes, and a Duo TGT makes the default (encrypted and signed) TGC about 4.3 KB. The
  documented remedy is `cas.tgc.crypto.signing-enabled=false` (TGC crypto uses
  `EncryptionOptionalSigningOptionalJwtCryptographyProperties`): the JWE is `dir` + `A256CBC-HS512`, already authenticated,
  and dropping the JWS layer saves about a quarter. A defined `cas.tgc.crypto.signing.key` keeps signing on
  (`BaseStringCipherExecutor`). Scenario `mfa-duo-universal-login-stateless` runs with it.
- `TicketGrantingTicketCompactor` keeps only the TGT's authentication (typed JSON via `BaseJacksonSerializer.forType`),
  with principal attributes removed from the principal and the handler-result principals. On expand it always re-resolves
  them through `defaultPrincipalResolver` with a `BasicIdentifiableCredential` of the principal id, keeping that id (like
  `DefaultCentralAuthenticationService.rebuildStatelessTicketPrincipal`; attribute repository results are cached), and
  creates the TGT through the `TicketGrantingTicketFactory`, then sets the creation time and a `FixedInstantExpirationPolicy`
  from the compact fields. Maintainer: no ticket impl classes and no hand-built tickets in compactors; use the factories.
  Every compactor (core and OAuth) takes an `ObjectProvider<TicketFactory>` (the OAuth factories depend on the ticket
  registry) and creates the ticket through its factory with a null parent ticket, then sets the creation time and a
  `FixedInstantExpirationPolicy`. Proxy-granting and proxy tickets go through a service ticket created by the service
  ticket factory. Factories look up registered services on every expansion; expected.
  SSO-time decisions only see attribute-repository attributes; handler-only attributes (Duo, delegated claims) are gone.
- VC issuance works statelessly (maintainer decision): `OAuth20CodeCompactor` keeps `authorizationDetails` and
  `OAuth20AccessTokenCompactor` keeps `credentialConfigurationIds` and `authorizationDetails`, both as untyped JSON that
  expands to maps (`OidcVerifiableCredentialEndpointController` reads `credential_configuration_id` from either form). The
  pre-authorized code and the c_nonce are not single use there: `consumePreAuthorizationCode` returns a stateless code
  without deleting it, and the nonce service accepts a valid stateless nonce when the delete finds nothing. Offer
  transactions reference the stored code id, the nonce endpoint returns the stored id, the offer service reads a stateless
  transaction back by id, and the token response customizer reads a stateless access token back for `authorization_details`.
  VP is unsupported there. `OidcVerifiableCredentialIssuanceTests.StatelessTicketRegistryTests` covers the issuance.
- `getTicket(id).getId()` must equal `id`, as with every other registry: callers such as `InitialFlowSetupAction` put
  `ticket.getId()` into scope and look it up again. The stateless registry sets every expanded ticket's id to the id it was
  looked up by, unless the compactor's `isTicketIdRetained()` is true (device user codes, whose id is the user code). No
  compact layout may contain the ticket's own id, or updates nest the previous id.
- Expanded TGTs get a `FixedInstantExpirationPolicy` at the original policy's maximum expiration time, so an idle
  timeout is not enforced. Maintainer: no idle timeout, no single use and no revocation, by design (deployment trade-offs,
  as with the Shibboleth IdP); keys are created or copied by hand, as with any registry. A sliding idle timeout was built and
  rejected, do not reintroduce it. Non-happy paths are reviewed last.
- Scenarios `stateless-ticket-registry`, `stateless-ticket-registry-saml2-idp`, `oauth2-login-stateless`,
  `oidc-login-stateless`, `mfa-duo-universal-login-stateless`, `ticket-validation-casv3-pgt-stateless` cover happy
  paths only; none covers VC/VP, DPoP, private_key_jwt, replay or delimiter input.
- Scenario `stateless-ticket-registry-load` combines interrupt notifications, the SAML2 identity provider, OIDC, proxy
  tickets and delegation to the simplesamlphp SAML2 IdP, checks unhappy paths (tampered or forged tickets and cookies,
  expired and reused service tickets, cookie replay after logout, attribute-based access at single sign-on) and runs a load
  over plain HTTP (`STATELESS_LOAD_ITERATIONS`, `STATELESS_LOAD_CONCURRENCY`, `STATELESS_LOAD_BROWSERS`). It also runs
  Simple MFA by email (mockmock mail server from `init.sh`): a code typed in another user's flow and a wrong code fail,
  the right code passes, single sign-on does not ask again; replicated sessions are on for OAuth and pac4j.
- Simple MFA with the stateless registry (maintainer: short code typed, stateless id held by the flow).
  `CasSimpleMultifactorAuthenticationService.store` returns the stored ticket; the send-token and verify-email actions
  put its id on the flow credential (`CasSimpleMultifactorTokenCredential.ticketId`; the view binds `token` only). Read the
  credential from the flow scope, not `WebUtils.getCredential`, which returns null while the token is still blank.
  Lookup loads the ticket by that id and compares the typed code in constant time with
  `CasSimpleMultifactorAuthenticationTicket.getCode` (`code` property, else the id); with no id, or on mismatch, the code
  is looked up as a ticket id, which keeps the stateful behavior (another principal submitting a code burns it, as
  `simple-mfa-login` expects). `CasSimpleMultifactorAuthenticationTicketCompactor` keeps service, code and principal id.
  The token endpoint answers with the stored id. Tokens are not single-use with the stateless registry; the collision
  check in `generate` and the wrong-code fallback each log a warning from the stateless registry's failed decode.
- Maintainer: callers that hand a ticket id to a browser or another party read it from the ticket `addTicket` returns;
  the registry does not change the ticket passed in, and reading the ticket back after adding it was rejected (cost,
  type-specific code). Fix call sites as scenarios need them, not all at once. Delegation: the webflow manager keeps the
  built transient ticket in the flow (it carries the request properties) and hands the stored ticket to
  `DelegatedClientSessionManager.trackIdentifier(WebContext, Ticket, Client)` and the CAS client session key. Still
  open: password reset, account registration and others not in the scenarios.
- Duo `TICKET_REGISTRY` session storage is unsupported with the stateless registry (maintainer decision: document only,
  no code). The TST holds the whole flow (authentication, result builder, all webflow scopes) as objects, Duo's SDK
  rejects a `state` over 1024 characters, and the Duo webflow is wired at startup by storage type, so a runtime fallback
  onto browser storage does not work (no `restore` transition, no storage write). Users must set `BROWSER_STORAGE`.
- Session stores on the ticket registry (pac4j `TicketRegistrySessionStore`, Spring Session
  `TicketRegistrySessionRepository`) keep only text properties, since the transient ticket compactor stringifies values:
  other values go in as base64 Java serialization text, times as ISO-8601. The default in-memory registry hands the
  same ticket instance to concurrent requests, so never add or remove keys of a stored ticket's properties map while
  saving a session: Spring Session keeps a fixed set of properties (all attributes under one `attributes` property)
  and only replaces their values. The session cookie (and the Spring Session id)
  follow the id of the ticket the registry returns on add and update. With the stateless registry the whole session
  rides in that cookie, so keep sessions small; the session ticket expires at a fixed instant from its creation.
  pac4j saves the request to resume as a `FoundAction`/`OkAction` (exceptions); the pac4j store serializes exceptions
  without their stack trace, which otherwise pushes the session cookie past 4096 bytes and the browser drops it (the
  OAuth callback then lands on the redirect URI without a code). The load scenario fails on any oversized cookie.
- PAR on the stateless registry: `OidcPushedAuthorizationRequestCompactor` (oidc-core-api) keeps a slim form of the
  request context (maintainer choice over the serialized request); the PAR authentication keeps its principal id, date
  and all attributes as text, since the authorize step merges those attributes into the user's authentication. The
  registered service is looked up again by client id; client credentials are dropped from the parameters; the client's
  pac4j profile is not kept. `request_uri` is read from the ticket `addTicket` returns.
- The OAuth replicated session cookie path is configured in `OAuth20HandlerInterceptorAdapter.preHandle`
  (`OAuth20ConfigurationContext.configureSessionReplicationCookiePath`) before the pac4j security interceptor can write
  the cookie; configuring it only in the controllers left a `Path=/` cookie from the first request after startup.
- Scenarios that start an external SAML2 IdP from `readyScript` (after CAS is up) must call `/cas/sp/idp/metadata`
  before the first delegated login: the pac4j client failed to load the IdP metadata at startup, and redirecting to it
  fails with a `NullPointerException` in `ChainingMetadataResolver.setResolvers` until that endpoint forces a reload.

## Passwordless authentication review discipline

- `AcceptPasswordlessAuthenticationAction` must not compare tokens itself. The submitted token goes to the
  `AuthenticationManager` as a `OneTimePasswordCredential`, so wrong tokens are audited (`AUTHENTICATION_FAILED`, principal =
  username); an earlier version compared first and only authenticated a match, which left failed guesses unaudited.
- The same credential is authenticated more than once per login: by the action, again by `DefaultCasDelegatingWebflowEventResolver`
  (it re-authenticates whatever credential is in the flow), and a third time by `ServiceTicketRequestWebflowEventResolver` when an
  SSO session exists. So the handler must not consume the token. The action consumes it after `super.doExecuteInternal` returns a
  non-failure event and before any ticket exists (the resolver only puts the result builder; the TGT is created in a later state),
  and treats `deleteToken(...) == false` as a lost race. Removing the credential from the flow to avoid re-authentication breaks the
  SSO/renew path, which then resolves the principal from the existing session.
- View-state entry actions run on every re-entry. `STATE_ID_PASSWORDLESS_DISPLAY` creates and sends a token on entry, and the
  accept failure transition re-enters it, so any change to the failure path changes how many emails/SMS a guess costs.
- The MFA branch replacing the token is by design: there MFA is the only factor, and deployments either disable device
  registration or put it behind MFA. Do not report `DetermineMultifactorPasswordlessAuthenticationAction` building a
  credential-less authentication as a bypass.
- View-state entry action results are ignored, so the create-token action returning `error()` does not change the flow; what it
  does decide is whether a token is stored. `emailToken`/`smsToken` report true only for an actual delivery.
- Token repositories must agree on the `PasswordlessTokenRepository` contract: `findToken` returns only unexpired tokens,
  `deleteToken` removes the token `findToken` returned and returns true only for the caller that removed it (affected rows,
  `getDeletedCount()`, `Map.remove`, a `2xx` from REST), and `clean()` removes expired rows. The encoded record is written before
  the store assigns an id, so `findToken` sets the id from the stored entity (`withId`); the id inside the decoded record is null. The existing
  `verifyCleaner` tests in the JPA and Mongo modules asserted that `clean()` removes a live token, i.e. they encoded the inverted
  query; they now assert the contract. `clean()` is global, so keep exactly one test per class calling it and give every other
  test a live token of its own.
- `PasswordlessTokenAuthenticationHandler.supports` accepts any `OneTimePasswordCredential` subclass (Duo passcodes included)
  because it is registered globally; check the credential type hierarchy before widening or relying on it.
- When reviewing wallet/passkey integration, check against the current WebAuthn Level 3 Recommendation and the W3C Digital
  Credentials API plus OpenID4VP 1.0 (DC API response modes) rather than older drafts.

## FIDO2 WebAuthn / passkeys review discipline

- Review against WebAuthn Level 3 (W3C Recommendation, 2026) and the Yubico `webauthn-server-core` version in
  `gradle/libs.versions.toml`; the ceremony logic lives in the vendored `com.yubico.core.WebAuthnServer`, the browser side in
  `support/cas-server-support-thymeleaf/.../static/js/webauthn/webauthn.js`.
- Already present, do not re-report: related origins (`/.well-known/webauthn`), conditional mediation on the passwordless
  user-id view, `signalAllAcceptedCredentials`/`signalCurrentUserDetails`, session-bound challenges, and the MFA handler's
  check that the asserted user handle maps to the in-progress principal.
- Yubico enforces UV only when the request says `REQUIRED`; `userVerificationRequirement` unset means UV is not checked.
- The library copies `backupEligible`/`backupState`/transports only if the repository returns them from `lookup` and
  `getCredentialIdsForUsername`, and it can only store transports the browser sent (`response.transports`).
- All WebAuthn puppeteer registration scenarios set `allow-untrusted-attestation=true`; the defaults reject `none`
  attestation, which synced passkey providers return. Keep that in mind before calling a scenario representative of defaults.
- `allow-untrusted-attestation` and `user-verification-requirement` are operator decisions: do not change their defaults;
  set them explicitly in tests and scenarios. UV `REQUIRED` needs a `ctap2` virtual authenticator (`cas.js` defaults to `u2f`).
- Yubico requests `credProps` on every registration by itself; read the answer from `RegistrationResult.isDiscoverable()`.
- `webauthn.js` uses the native `PublicKeyCredential.parse*OptionsFromJSON` and `toJSON()` with no fallback. Yubico parses
  with `FAIL_ON_UNKNOWN_PROPERTIES=true`; `toJSON()` output passes only because `publicKey`/`publicKeyAlgorithm` are ignored
  and `authenticatorData` is `@JsonIgnore`d, so check Yubico's `@JsonCreator`s before sending any new client field.
- `signalUnknownCredential` is destructive (Chrome's virtual authenticator deletes the passkey). Report
  `unknownCredential` only from the owning account's registrations (user handle, else request username), never from the
  node-local credential index in `BaseWebAuthnCredentialRepository`, which lags other nodes by up to a minute.
  User handles are random and resolve only through the account's registrations, so after the last passkey is deleted a
  login with it cannot be traced to an account; the account profile therefore signals on delete, from the device detail
  `relyingPartyId` (`WebAuthnUtils.determineRelyingPartyId` in the config module) and the device id (the credential id).
  Chrome's `u2f` virtual authenticators ignore signals; use `ctap2` (resident or not) to assert a signal in puppeteer.
- Verification here: Maven Central is blocked, so check Yubico APIs by cloning `github.com/Yubico/java-webauthn-server` at the
  version in `libs.versions.toml`. `webauthn.js` is too deep to stage; copy it under the ignored `build/` folder, stage that,
  and exercise it in Playwright's Chromium with a CDP virtual authenticator. After `device_commit_files`, check the file on
  the device (size or a grep): one commit reported success while the device kept the old content.
- `/.well-known/passkey-endpoints` follows the W3C Passkey Endpoints Working Draft (Jan 2026): 200, `application/json`, no
  redirect, `{}` allowed. CAS has no direct URL into WebAuthn registration, so the defaults point at the plain account
  profile (`/account`), never at a panel fragment such as `#divMfaRegisteredAccounts`, and only when
  `CasFeatureModule.FeatureCatalog.AccountManagement.isRegistered()`. `WebAuthnControllerMvcTests` enables account
  management, so its wired document carries both URLs; `{}` only appears with the feature off.
- JSON examples pasted into the documentation are pretty-printed (one member per line, two-space indent), never minified.
- Resident key: the discoverable button asks `REQUIRED`; the default button asks `PREFERRED` only when
  `allow-primary-authentication` is on (`WebAuthnServer.determineResidentKeyRequirement`), so MFA-only deployments do not
  spend security-key slots. `passwordless-login-passkey` registers with the default button to cover this.
- `isBrowserSupported()` in `webauthn.js` is synchronous on purpose (call sites test it directly) and must not require a
  platform authenticator: headless Chrome's virtual authenticators and security-key users would fail it.
- Backup flags live on the stored `RegisteredCredential` (`backupEligible`/`backupState`, deprecated-experimental in Yubico
  2.9 but stable in WebAuthn Level 3); Yubico rejects an assertion whose BE differs from a stored BE. `lookup`/`lookupAll`
  return the stored credential as is, so never rebuild it field by field; `updateSignatureCount` refreshes BS and fills BE
  only for records stored before BE was kept. Transports live on `CredentialRegistration` and go out through
  `getCredentialIdsForUsername`. Chrome's CDP virtual authenticator sets BE/BS via `defaultBackupEligibility`/`defaultBackupState`.
- Provider names: `CredentialRegistration.aaguid` (UUID string, absent for the all-zero AAGUID) is set at registration
  only; older records cannot recover it. `webauthn-passkey-providers.json` is a snapshot of
  `passkeydeveloper/passkey-authenticator-aaguids` `aaguid.json` (name, `icon_light`, `icon_dark`; upstream has no
  license file and may empty the list; icons are provider logos the list publishes for RP display), refreshed with
  `jq 'with_entries(.key |= ascii_downcase) | map_values({name, icon_light, icon_dark} | with_entries(select(.value != null)))'`
  and kept pretty-printed. `WebAuthnUtils.getPasskeyProvider` keeps only SVG data URIs (light first). The device
  manager uses the name only when attestation metadata names no device; `SuccessfulRegistrationResult.passkeyProvider`
  carries name and icon to the registration page, `SuccessfulAuthenticationResult.passkeyProvider` (the credential just
  used) to the login page. The account profile gets the icon as the device detail `icon` holding the path of
  `GET /webauthn/passkey-providers/{aaguid}/icon` (permitAll, sandboxed CSP, `nosniff`), never the data URI: devices
  live in flow scope and webflow execution state is client-side, so data URIs would bloat every request. Chrome's
  virtual authenticators send unlisted or zero AAGUIDs, so scenarios cannot show a provider name.
- `#device-icon` on the registration and login pages only has an image when attestation metadata supplies `imageUrl`
  (the bundled Yubico list, remote PNGs) or the passkey provider has an icon; `showDeviceInfo` hides it otherwise or
  when the image fails to load.
- Passkey upgrade (conditional create): states exist only when `passkey-upgrade-enabled`, `allow-primary-authentication`
  and `allow-untrusted-attestation` are all on (no warning otherwise, by design); `startConditionalRegistration` re-checks
  them. The check action reads the login flow's own `flowScope.credential` (MFA subflows write request/conversation scope,
  which `WebUtils.getCredential` would return) and requires a `UsernamePasswordCredential` in the authentication.
  `user.name` is the typed username but the stored `userIdentity.name` is always the principal id; the typed name is kept
  in `CredentialRegistration.userEntityName` (null when equal) so `signalCurrentUserDetails` sends the name of the
  credential just used instead of renaming the passkey. `excludeCredentials` is rebuilt from the principal id.
  Headless Chrome reports `conditionalCreate`; with a CDP virtual authenticator the conditional `create()` never settles,
  so the page continues on its timeout (5 s); with none it rejects at once.
- Screenshots of themed views without a running CAS: stage the template, `cas.css`, logo and background via `build/`,
  install `normalize.css`, `bootstrap` (grid), `material-components-web`, `@mdi/font` from npm at the versions in
  `libs.versions.toml`, rebuild the layout shell (header, `.bgimage` main, footer) and capture with Playwright.
  The layout's `div#content` carries a shadow, so narrow cards use another fragment id.

## Queue-backed ticket registries (Kafka, AMQP, Pulsar, GCP Pub/Sub)

- Each node keeps its own copy of the registry and broadcasts every change, so the consuming side must be
  one consumer group, queue or subscription *per node*, named from the node's `PublisherIdentifier`
  (`cas.ticket.registry.core.queue-identifier`, random when unset). A name shared by the nodes turns the
  broadcast into load balancing: each change reaches one node. The AMQP registry (a queue per identifier) and
  the Kafka service registry stream (`groupId` = identifier) do it right, and the Kafka ticket registry now
  consumes in `<group-id>-<queue-identifier>`. Pulsar (`subscription-name`) and GCP Pub/Sub
  (`<topic>Subscription`) still share one name across nodes and have no two-instance scenario.
- The symptom is a two-instance puppeteer scenario that fails about half its CI runs, with every in-process
  attempt of a failing run failing alike: partitions are assigned once when the instances start and the
  attempts reuse the running servers. `ci/tests/kafka/docker-compose.yml` sets `KAFKA_NUM_PARTITIONS=1`, so in
  a shared group a single consumer owns each topic, and a ticket never reaches the other node, which then
  rejects the TGC and clears it.

## Google Authenticator credential repositories (all backends)

- Review every backend against the same contract, with crypto on: `cas.authn.mfa.gauth.crypto.enabled` defaults to `true`, but every puppeteer scenario and the repository tests turn it off, so undecoded returns and double encryption never show up in CI.
- A device delete must be scoped to its owner. WebAuthn, YubiKey and Duo device managers delete by username and id; `OneTimeTokenCredentialDeviceManager` and `GoogleAuthenticatorDeleteAccountAction` delete by id alone, and ids are guessable (JPA sequence; `currentTimeMillis` before 42bcde1).
- `BaseOneTimeTokenCredentialRepository.encode()` mutates its argument. Never pass an object you will keep or save again; JPA `get(id)` and InMemory `get(id)` return stored, undecoded instances, and updating them double-encrypts.
- `count()` means devices in Redis, JPA, Mongo and DynamoDB, but users in InMemory, JSON and LDAP; check which a caller assumes.
- A repository read that fails must not look like "no devices": `OneTimeTokenAccountCheckRegistrationAction` sends empty or null results to enrollment (REST returns null, JSON returns empty on errors).
- When testing deletes or updates, store devices for two users and two devices per user, and assert the others survive; single-user tests hid the DynamoDB `GE` filter and the LDAP update bug.
- To make a range-filter bug visible in a DynamoDB delete test, pick keys that a wrong comparison would sweep up: the kept user's name sorts after the deleted one ("z…" vs "a…"), and the kept device ids are larger than the deleted id. Random UUIDs alone pass by luck half the time.
- DynamoDB tables keyed by `id` should delete with `DeleteItem` on that key, not a scan followed by deletes.
- Webflow actions and device managers that take a device id from the request must load it with the owner-scoped `get(principal.getId(), id)` and act only on what that returns. Unscoped `get(id)` / `delete(id)` are for the actuator and internal paths.
- Every repository read returns a decoded copy (JPA detaches before decoding); `update()` must persist `properties`, which the verification flags of the delete and confirm actions live in.
- The delete/confirm verification flags are stored on the account as `<flag>:<epoch millis>` and expire (`GoogleAuthenticatorAccountVerificationUtils`). They cannot live in flow or conversation scope: the login webflow keeps its execution state client-side, and the AJAX check and the form submit carry different execution keys.
- `CasReentrantLock.tryLock(supplier)` returns null when the lock is not acquired; never let that null reach a caller as "no accounts".
- `MockWebServer.requestLineConsumer` exposes the request line, for asserting the HTTP method a client sends.
- `MockRequestContext.create(...)` throws `Exception`; a test method that calls it must declare `throws Throwable`.
- `BaseOneTimeTokenCredentialRepository.encode()` returns an encoded copy and leaves its argument alone; store and return the copy, and read normalized values (lowercased username) from it, not from the argument.
- Compile-check changed sources when Gradle cannot run: in the device VM, download a JDK 25 from `api.github.com/repos/adoptium/temurin25-binaries/releases/assets/<id>` (`Accept: application/octet-stream`; `github.com` itself is blocked), get read access to `~/.gradle/caches/modules-2/files-2.1`, copy the highest version of every jar to local disk (the mount runs out of file handles with ~1,700 jars open), and run `javac -proc:full -processorpath <lombok 1.18.x jar>` with an `@argfile` classpath of the repo's `*/build/classes/java/{main,test}` plus those jars. To run tests in the same VM: copy `*/build/{classes/java,resources}/{main,test}` to local disk (class lookups across ~1,400 directories on the mount are too slow), build the classpath from the module's project closure (parse `project(":...")` references in `build.gradle`, following `configuration: "tests"` into test classes) instead of every module, pick jar versions from `gradle/libs.versions.toml` rather than the highest in the cache (the highest Hibernate/Hypersistence pairing does not load), leave out `spring-cloud-*` integration jars and `log4j-to-slf4j`/`logback`, put the freshly compiled changed classes first, and drive JUnit with a small `LauncherFactory` main. Services such as LDAP, MongoDB or DynamoDB are not available there, so those suites still need a local run.
- Per-tenant ciphers must be built once and reused: a cipher built from settings with blank keys generates its own keys, so a new instance per call cannot decode what the previous one encoded. `BaseOneTimeTokenCredentialRepository` caches them by tenant and crypto settings.
- In the VM test runner, tenant property binding (`containsBindingFor(...)`) did not take effect, so tenant-specific crypto paths pass there and can still fail under Gradle; check those with a local Gradle run.
- WebAuthn FIDO metadata: `FidoMetadataDownloader.refreshBlob()` always downloads; `loadCachedBlob()` uses the cache until the BLOB's `nextUpdate`. CAS refreshes at startup and falls back to the cache on `IOException` (the FIDO service rate-limits with HTTP 429). `CompositeAttestationTrustSourceTests` downloads from the real service; a 429 there with no cache from an earlier run is an outage, not a regression.
- Active Directory keeps a single value of `description` on user objects. The LDAP GAuth repository therefore writes all of an entry's accounts as one JSON array value; writing one value per account loses all but one device on AD.
- JPA with `@GeneratedValue` ids (Hibernate 7): `merge()` of an entity whose id is set but not in the table throws `OptimisticLockException`; reset the id to `0` to insert. The JPA GAuth repository does this when `update()` inserts a missing device.
- The VM test runner's copied build dirs go stale once Misagh commits: recompile every Java file touched by the commits of the current work (`git diff --name-only <first commit>^ HEAD`) plus the uncommitted ones, not just `git status`.
- A webflow action that validates a GAuth token and then updates the device must read the device again after validation: the validator updates its own copy (used scratch code, last-used time), and updating an older copy undoes that.
- Use `git --no-optional-locks` for every read-only git command in the device VM; a plain `git status` leaves `.git/index.lock` behind there.
- DynamoDB-backed tests can run in the device VM against moto: `pip3 install --user "moto[server]"` works there (Maven Central and MongoDB downloads do not), then start `~/.local/bin/moto_server -p 8000` and run the suite in the same `device_bash` call. It is not DynamoDB Local, so CI can still differ.
- `DynamoDbTableUtils.createTable(..., globalSecondaryIndexes)` creates GSIs with a new table and adds missing ones to an existing table with `UpdateTable`; pass every GSI key attribute in the attribute definitions.
- MongoDB: a query with a collation uses only indexes with the same collation. Give such an index an explicit name, so it does not clash with a plain index on the same field. Avoid `MongoDbConnectionFactory.createOrUpdateIndexes` for collated indexes: the server returns the collation expanded, the options never compare equal, and the index is dropped and rebuilt at every startup.
- Spring Data Redis with Lettuce: `executePipelined` opens a dedicated connection for the pipeline (a new TCP connection when there is no pool), so a pipeline is slower than a few plain commands on the shared connection. Pipeline only bulk work such as batched deletes, never a per-login write.
- `CasRedisTemplate.scan(pattern, count)` treats `count` as a cap on the results, not as the `SCAN COUNT` hint; for a hint, use `RedisTemplate.scan(ScanOptions)` and close the cursor.
- More services for the VM test runner: `pip3 install --user fakeredis` provides `fakeredis.TcpFakeServer`, a Redis emulator on 6379. An UnboundID `InMemoryDirectoryServer` on 10389, with base `dc=example,dc=org`, bind `cn=Directory Manager`/`password` and the schema turned off, runs the GAuth LDAP suite. Start either one in the background in the same `device_bash` call as the tests, and stop it by a PID file, not `pkill -f <name>`: that pattern also matches the calling shell and kills it.
- To compile a Java file outside the repository with Lombok, copy the repo's `lombok.config` next to it; without it, `@Slf4j` creates `log` rather than `LOGGER`.
- Hibernate 7 JPQL bulk deletes (`DELETE FROM Entity e WHERE ...`) also delete the matching rows of the entity's element-collection tables (`DELETE ... WHERE id IN (SELECT ...)`), so they need no native SQL. The physical naming strategy turns table names into snake case (`google_authenticator_registration_record`), so native SQL must not use the `@Table` name as written.
- A Java `Stream` built with `onClose(...)` releases nothing unless it is closed: wrap any stream that holds a cursor or connection, such as `CasRedisTemplate.scan(...)`, in try-with-resources, even for a terminal `count()` or `collect()`. When pooling is enabled, CAS turns off Lettuce native-connection sharing, so every leaked Redis connection is a pooled one. To catch such a leak in a test, enable the pool with a small `max-active` and call the method more times than the pool allows. The fakeredis emulator in the VM cannot run the Redis ticket-registry suites that need RediSearch.
- One puppeteer scenario can cover several storage backends: put every backend module in `dependencies`, list the init scripts comma-separated in `initScript`, and give each backend a `variations` entry that turns the others off with `--CasFeatureModule.<Feature>.<module>.enabled=false` (each backend auto-configuration carries `@ConditionalOnFeatureEnabled`). `SCENARIO_VARIATION` holds the variation name. `mfa-gauth-login-encrypted-stores` does this for GAuth on JPA and Redis, with encryption on.

## Google Authenticator Redis repository

- Accounts live twice: the record at `CAS_TOKEN_ACCOUNT:<id>` and a copy as a member of the set `CAS_TOKEN_PRINCIPAL:<username>`. `get(username)`, `count(username)` and OTP validation read the set, not the record, so a delete that removes only the record leaves a device that is still listed and still validates.
- Reads must not repair the index. A read that removes set members whose record "does not exist" acts on whatever the read saw; with `read-from` sending reads to replicas, and the record and the set on different cluster slots, a lagging replica makes a live device look orphaned and the removal on the primary is permanent. Repair belongs on the explicit `delete(id)`: when the record is missing the owner is unknown, so it scans `CAS_TOKEN_PRINCIPAL:*` in batches and removes members with that id.
- Remove set members by the raw bytes Redis returned (`executePipelined(..., RedisSerializer.byteArray())` then `sRem`), not by re-serializing a deserialized account; the value serializer is LZ4 over JDK serialization, and a removal only matches if the bytes are identical.
- Spring Data Redis pipelining works on Lettuce cluster connections (`LettuceClusterConnection` does not override `openPipeline`) and key `SCAN` spans all nodes, so pipelines and scans here are cluster-safe.
