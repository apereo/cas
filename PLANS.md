# PLANS.md — CAS Protocol Review (v1 / v2 / v3 + SAML 1.1)

## Registered service history restoration (2026-09-10)

Scope: restore a selected registered-service revision through `EntityHistoryEndpoint`, using the
existing services manager save path and preserving unrelated work.

- [x] Trace historical entities, registry persistence, endpoint access checks and existing coverage.
- [x] Add a restore operation scoped to the requested service and historical revision.
- [x] Extend MockMvc tests and the JPA service-registry Puppeteer scenario.
- [x] Document the endpoint, add a brief RC2 note and complete focused validation.

Validation: all eight `EntityHistoryEndpointTests` passed via `testRegisteredService`; module
`checkstyleMain` and `checkstyleTest` passed. The JPA Puppeteer script passed syntax and ESLint checks;
its Docker-backed integration run remains outstanding.

## Palantir service history restore action (2026-09-11)

- [x] Trace the history dialog, shared context-menu helper and service-list refresh path.
- [ ] Add a Restore Version row action using the historical revision id and existing actuator.
- [ ] Cover confirmation, cancellation, failures and refreshed service/history state in Puppeteer.
- [ ] Update the RC2 note, review the scoped diff and validate the browser behavior.

## SAML2 IdP single sign-on bypasses interrupts (2026-09-11)

Found by `interrupt-gateway-login` (fails at the SAML2 passive step): with an SSO session,
`AbstractSamlIdPProfileHandlerController` builds the SAML2 response straight from the TGT, so the login webflow and
its interrupt inquiry never run.

- [x] ~~Veto SSO reuse outside the webflow in `InterruptSingleSignOnParticipationStrategy`~~ — reverted at the
      maintainer's request; not the right fix. Strategy, its tests, docs and RC2 line restored.
- [x] Maintainer decision: gateway-like requests without an SSO session are not interrupted; with an SSO session,
      protocols that do not return to the login flow (SAML2 IdP) skip interrupts, which is acceptable for now.
- [x] `interrupt-gateway-login` restructured: no-SSO passive requests (CAS/OIDC/SAML2), then pending interrupt
      (CAS/OIDC withheld, SAML2 answered from SSO), then acknowledged. Documented the limitation. ESLint passes; not run.

## Interrupts during passive (gateway) requests (2026-09-11)

Scope: finding #2. CAS `gateway`, OIDC `prompt=none` and SAML2 `IsPassive` (the latter two mapped to `gateway`)
must not render the interrupt view (CAS 3.0.3 §2.1.1, OIDC Core errata 2 §3.1.2.1, SAML2 core `IsPassive`).
Decision (user-approved): any required interrupt, blocking or not, withholds the ticket on a passive request.

- [x] `InquireInterruptAction`: when an interrupt is required and the request is a gateway request with a service,
      return `gateway` without storing, tracking or finalizing the interrupt.
- [x] `InterruptWebflowConfigurer`: route `gateway` to `gatewayServicesManagement` from `inquireInterrupt` and from
      `createTicketGrantingTicket` (prepended inquiry); `generateServiceTicket` already has it.
- [x] Tests: action returns `gateway` (blocking and non-blocking), blank gateway still interrupts; wiring test.
- [x] New Puppeteer scenario `interrupt-gateway-login` (OIDC + SAML2 IdP + CAS gateway, AFTER_SSO).
- [x] Docs (trigger modes), RC2 note, notes files, cross-check existing interrupt scenarios, `git diff --check`.

Puppeteer: new `interrupt-gateway-login` (OIDC + SAML2 IdP, AFTER_SSO) checks that CAS gateway, OIDC `prompt=none`
(`login_required`) and SAML2 `IsPassive` (no assertion) never show the pending interrupt, that an interactive request
still does, and that all three succeed once it is acknowledged. ESLint passes. Existing interrupt scenarios do not
send gateway, `prompt=none` or `IsPassive` requests, so they are unaffected. Not run: tests, Gradle, Puppeteer
(linked VM has only Java 11); run `./testcas.sh --test InquireInterruptActionTests`,
`./testcas.sh --test InterruptWebflowConfigurerTests` and the `interrupt-gateway-login` scenario.

## Interrupt block bypass via tracking cookie (2026-09-10)

Scope: finding #1 only. A blocking interrupt with links is recorded as acknowledged when a link is followed, and
the tracking cookie is not bound to the principal, so the "cookie equals fresh result" check skips the interrupt.

- [x] `FinalizeInterruptFlowAction`: following a link on a blocking response validates the link but never tracks it.
- [x] `InquireInterruptAction`: a blocking response is never skipped as already acknowledged (covers existing cookies).
- [x] `SimpleInterruptTrackingEngine`: store the principal id with the tracked response; ignore cookies for others.
- [x] Tests: blocked link not tracked; tracked block still interrupts; other principal's cookie ignored; same user skips.
- [x] Docs (tracking page), RC2 note, Puppeteer cross-check, `git diff --check`.

Puppeteer: `interrupt-afterauthn-groovy` now logs in again after following a blocked link (without logout) and
expects the block; it fails on the old code. `interrupt-aftersso-login` (forged proceed, same-user SSO skip),
`interrupt-aftersso-login-groovy` and the other scenarios re-login as the same principal or log out first, so the
principal binding keeps them passing. Upgrade effect: pre-existing tracking cookies are ignored once. Not run:
tests, Gradle, Puppeteer (linked VM has only Java 11); run `./testcas.sh --test InquireInterruptActionTests`,
`./testcas.sh --test FinalizeInterruptFlowActionTests` and the `interrupt-afterauthn-groovy` Puppeteer scenario.

## Interrupt JSON inquirer race (2026-09-10)

Scope: finding #3 only. `JsonResourceInterruptInquirer` clears and refills a shared map on every inquiry, so a
concurrent login can observe an empty map and skip a (blocking) interrupt. Its file watcher is never started.

- [x] Load the resource once, publish an immutable map through an atomic reference, and serve lookups from it.
- [x] Start the file watcher; reload on create/modify; a failed reload keeps the last good contents.
- [x] Add tests: concurrent inquiries read the resource once and all see the interrupt; file edits are picked up.
- [x] Update the JSON interrupt documentation and RC2 note; cross-check Puppeteer JSON scenarios; `git diff --check`.

Puppeteer: the JSON-backed interrupt scenarios (`interrupt-afterauthn-*`, `interrupt-aftersso-*`,
`adfs-login-interrupt`) read static files and never rewrite them at runtime, so they are unaffected. Behavior
change: non-file resources (classpath/URL) are now read once at startup instead of per inquiry. Not run: tests,
Gradle, Puppeteer (linked VM has only Java 11); run `./testcas.sh --test JsonResourceInterruptInquirerTests`.

## Interrupt REST inquirer status handling (2026-09-10)

Scope: finding #5 only. `RestEndpointInterruptInquirer` must honor the documented contract (payload is read
only on a successful status). Non-2xx responses resolve to no interrupt, matching the existing transport-failure
path; fail-open vs fail-closed for outages is unchanged and remains a separate decision.

- [x] Parse the response body only for 2xx responses; log and return `none()` otherwise.
- [x] Extend `RestEndpointInterruptInquirerTests` with 4xx/5xx JSON error bodies (random-port mock servers).
- [x] Clarify the REST interrupt documentation and add a brief RC2 note.
- [x] Cross-check Puppeteer interrupt scenarios; `git diff --check`; report what was not run.

Puppeteer: no scenario configures `cas.interrupt.rest.*`; JSON, Groovy and regex scenarios do not reach the
changed code and need no adjustment. Tests, Gradle and Puppeteer were not run (the linked VM has only Java 11);
run `./testcas.sh --test RestEndpointInterruptInquirerTests`.

## Interrupt notifications review (2026-09-10)

Scope: static review of `support/cas-server-support-interrupt-{api,core,webflow}`, their
webflow wiring, templates and Puppeteer scenarios. Report CRITICAL/HIGH only; no code changes.

- [x] Map modules, auto-configuration, webflow states and end-to-end flow (login, SSO, logout).
- [x] Review inquirers at system boundaries (REST, Groovy, JSON, regex) for security/performance.
- [x] Review tracking engine, cookie cipher, SSO participation and finalize/skip logic.
- [x] Review interrupt views (templates, links, auto-redirect) and user-controlled input.
- [x] Cross-check Puppeteer interrupt scenarios for coverage of findings.
- [x] Summarize findings; update CLAUDE.md and AGENTS.md with lessons learned.

Findings (no code changed, so no RC2 note): blocked interrupts are bypassable after clicking a link (tracked
as acknowledged); inquiry is not suppressed for passive requests (gateway / prompt=none / IsPassive); JSON
inquirer clear-then-refill race fails open; inquirers re-run up to 3x per login plus per SSO ST (REST builds a
new client per call); REST error bodies interrupt every user while transport errors fail open.


## JMX operations expansion (2026-09-10)

Scope: extend the existing JMX module through CAS-owned service, ticket,
authentication and attribute-cache APIs. Preserve existing operations and unrelated
work. Do not run tests, Gradle tasks or Puppeteer; the user will verify manually.

- [x] Review AGENTS.md, CLAUDE.md, contributor guidance and the existing JMX wiring/tests.
- [x] Trace the relevant APIs and choose useful diagnostics and maintenance operations.
- [x] Enhance service/ticket management and add authentication/MFA and attribute-cache visibility.
- [x] Add focused behavior tests, including management metadata/invocation coverage through an isolated MBean server.
- [x] Cross-check Puppeteer coverage and update the JMX integration guide plus a brief RC2 note.
- [ ] Review the scoped diff, stream lifecycle, optional dependencies and parallel-safe fixtures.

Initial findings: only service and ticket listings are currently exported; service
listing does not close the ServicesManager stream even though its contract requires
closure. Existing tests check only non-null results and do not invoke JMX exports.

Implementation: preserve both existing listing operations and object names; close
service streams, expose loaded service counts/lookup/reload, registry counts,
bounded exact-prefix and principal-session queries, and the existing ticket cleaner.
Add handler/MFA inventories, exact provider availability checks for a registered
service, and service-specific principal attribute cache availability/invalidation.
Optional collaborators are resolved on demand. No backend modules, dependencies,
authentication policies or HTTP endpoints changed.

Coverage added: stream closure on success/failure, bounded queries, invalid inputs,
unknown counts, lookup/reload, cleanup delegation, absent optional components,
MFA inventories without availability probes, exact provider/service selection,
provider failure reporting, and real cache invalidation across two services.
JMX tests use an unregistered MBean server per context, exercise exported operations
and read-only attributes, and verify parameter metadata and maintenance dispatch.

Puppeteer review: no scenario includes the JMX module or invokes JMX. Inspected
service-registry-caching-loader-disabled, redis-ticket-service-registry,
rest-protocol-user-authn-mfa-simple and attribute-repository-resolve-perservice-caching.
Their service loading, ticket queries, MFA authentication and cache expiration
assertions remain applicable; the new operations do not run during these flows.
No scenario changes were needed. None were executed.

Manual verification: run `./testcas.sh --category jmx`. Compilation, JUnit tests,
Gradle checks and Puppeteer execution remain reserved for the user.


## RestClient migration (2026-09-09)

Scope: replace CAS-owned RestTemplate usage and deprecated supporting infrastructure
with RestClient; preserve HTTP contracts and unrelated work. Tests and Gradle runs
are reserved for the user.

- [x] Inventory production/test references and inspect Spring migration guidance.
- [x] Migrate password management, trusted-device storage, Clickatell and Boot Admin clients.
- [x] Adapt existing tests and inspect affected Puppeteer scenarios.
- [x] Add a brief RC2 note and complete static diff/API checks.

Implementation: REST clients retain their existing request factories, JSON conversion,
URI component encoding and status handling. Boot Admin uses the CAS-managed Apache
client directly; the old builder's timeout settings were discarded by its request-factory
customizer, so the effective CAS client timeout configuration remains unchanged.
The password-management customization bean is now passwordChangeServiceRestClient
of type RestClient. No TestRestTemplate or RestOperations usages were present.

Coverage: migrated password-management tests to RestClient and isolated request capture;
added request/header/JSON, URI encoding, HTTP error, trusted-device expiration deletion,
Clickatell failure and Boot Admin registration/deregistration checks in existing classes.
Reviewed spring-boot-admin-server and spring-boot-admin-server-cluster Puppeteer scenarios;
their assertions remain applicable. No dedicated Puppeteer scenarios configure REST
password management, REST trusted-device storage or Clickatell.

Validation: scoped git diff --check passed; checked APIs against cached Spring 7.1.0-M1,
Boot 4.2.0-M1 and Boot Admin sources. No production/test Java references to RestTemplate
or RestOperations remain. No compilation, Gradle tasks, tests or Puppeteer runs executed.

## Ready-listener and CloudWatch test failures (2026-09-09)

Scope: isolate configuration-validation status in the startup tests and remove
application-logging re-entry from CloudWatch appender initialization and diagnostics.
Preserve existing changes and the saved manual-test workflow.

- [x] Trace the shared system property, asynchronous ready listener and CloudWatch initialization cycle.
- [x] Coordinate startup test contexts and make ready-listener completion deterministic.
- [x] Use Log4j status logging and direct exception handling inside the CloudWatch appender.
- [x] Extend focused regression coverage and cross-check the CloudWatch Puppeteer scenario.
- [x] Add a brief RC2 note and complete scoped static validation; leave tests to the user.

Implementation: both startup test classes hold the system-properties resource lock
through context initialization. The ready test uses an inline async executor so the
listener publishes its result before the assertion and before the lock is released.
CloudWatch diagnostics use AbstractAppender's status logger; resource lookups use
direct try/catch while preserving creation fallback and the existing shutdown latch.

Coverage: the existing CloudWatch specification test now checks status reporting and
resource creation after failed group/stream lookups. Parameter cases no longer
reconfigure global logging. Plugin discovery is checked without creating an AWS client.
The LocalStack logging configuration is loaded explicitly into an isolated context by
the existing integration test; unit-test logging is console-only.

Puppeteer review: aws-cloudwatch-logging still exercises login and retrieves ten log
entries with message, timestamp and level. No application-log or endpoint behavior
changed, so its assertions remain applicable. Ready-listener changes are test-only.

Static validation: scoped git diff --check passed; both logging fixtures parse as XML,
and changed Java files remain within the repository line-length limit. Reviewed the
Log4j status-listener, plugin-discovery and context lifecycle APIs against cached source.
No compilation, Gradle tasks, JUnit tests or Puppeteer scenarios were executed.

## Thymeleaf template caching fix (2026-09-08)

Scope: verify the reported loss of parsed-template caching and restore it without
sharing themed templates or fragments across themes. Preserve the unrelated review below.

- [x] Trace CAS view construction and Thymeleaf 3.1.5 template/fragment cache keys.
- [x] Pass the resolved theme before cache lookup and preserve delegate cache validity.
- [x] Cover repeated renders, theme switching, fragments and disabled caching in existing tests.
- [x] Cross-check relevant Puppeteer scenarios and extend theme-switching coverage.
- [x] Add a brief RC2 note and review the scoped diff.

Findings: the chain replaces delegate resolutions with a non-cacheable resolution.
Thymeleaf creates both root and fragment cache keys before resolver invocation. The shared
MVC resolver must remain uncached because ThemeViewResolver mutates its returned views;
ThemeViewResolver already caches those views separately for each theme. REST templates can
vary with arbitrary request headers and query parameters, so they must remain uncached.

Validation: retain the saved manual-test preference; add regressions but do not run
tests or Gradle. Report static checks separately from execution results.

Implemented: CasThymeleafView decorates the engine entrypoint used by ThymeleafView
to supply a theme-qualified TemplateSpec. File and classpath delegates consume that same
theme snapshot. The chain returns the selected delegate's resolution, preserving mode,
decoupled logic and cache validity, with a non-cacheable override when caching is disabled
or theme attributes are absent. Configurations with a REST template endpoint stay uncached.

Regression coverage added: MockMvc renders across two themes and the unthemed fallback,
root/fragment cache hits, fresh model values on cache hits, cache-disabled rendering,
unqualified direct engine calls, delegate validity and per-theme MVC view-object isolation.
Classpath resolution also checks that it consumes the theme already used in the cache key.

Puppeteer review: themes-per-service, themes-external-per-service,
themes-collection-twbs-login, themes-collections-example, thymeleaf-templates-external,
thymeleaf-templates-rest, cas-validation-protocol-v2, cas-validation-protocol-v3 and
pm-account-profile. Extended themes-external-per-service to assert the themed footer is
present only for the fancy theme, including repeat visits after switching back and forth.
The external static-resource HTTP assertion remains intact.

Static validation passed: scoped git diff --check, changed Java line-length review and
node --check for the modified Puppeteer script. No compilation, Gradle, JUnit, browser
scenario execution or performance timing was performed.

## Scope

CAS protocol functionality **only**: `/login` (credential + service ticket issuance, renew,
gateway), `/validate` (CAS 1.0), `/serviceValidate` + `/proxyValidate` (CAS 2.0),
`/p3/serviceValidate` + `/p3/proxyValidate` (CAS 3.0), `/proxy` (PGT -> PT), `/logout` +
single logout, and `/samlValidate` (SAML 1.1 browser profile). Includes the ticket types
those endpoints issue and consume (TGT, ST, PT, PGT, PGTIOU) and the response views.

Explicitly **out of scope**: OAuth, OIDC, SAML2 IdP, WS-Fed, MFA, delegated auth, LDAP,
themes/view layer, and anything not on a CAS-protocol request path.

Goal: CRITICAL / HIGH impact only — security, protocol compliance, performance. Review at
system boundaries (request in -> ticket state -> response out), not isolated components.
**Identify and explain only. No code changes. No new tests.**

## Steps

- [x] 1. Inventory the protocol surface: validation core/api, support-validation, ticket
      core, SAML1.1 module, protocol endpoint controllers, response views, webflow entry
- [x] 2. Fetch and read the current published specs: CAS Protocol 3.0.4 spec, CAS 1.0/2.0
      sections, SAML 1.1 browser/artifact profile requirements
- [x] 3. Trace ticket lifecycle end to end: TGT issue -> ST issue -> ST validate ->
      PGT issue -> PT issue -> PT validate; expiry, single-use, service matching
- [x] 4. Compliance: required/optional response elements per version, `renew`, `gateway`,
      `pgtUrl` callback rules, error codes, XML/JSON response shapes, SAML 1.1 assertion
      conditions (NotBefore/NotOnOrAfter, Recipient, AudienceRestriction)
- [x] 5. Security: ticket forgery/replay, service URL matching + open redirect, pgtUrl
      validation, proxy chain authorization, SLO SSRF/amplification, XML parsing (XXE),
      timing/atomicity of single-use consumption
- [x] 6. Performance: per-request registry lookups, ticket registry round-trips, XML
      marshalling cost, service lookup caching, SLO fan-out blocking the response
- [x] 7. Rank by impact; discard nitpicks
- [x] 8. Cross-check each finding against `ci/tests/puppeteer` scenarios that exercise the
      affected protocol path
- [x] 9. Report a short (2-3 sentence) summary per finding
- [x] 10. Update `CLAUDE.md` and `AGENTS.md` with CAS-protocol guidance for future sessions

## Findings

Ranked; CRITICAL/HIGH only. No code changes were made.

### CRITICAL

1. **ST single-use is not atomic — service-ticket replay.**
   `DefaultCentralAuthenticationService.validateServiceTicket` reads the ticket from the
   registry *before* entering `lockRepository.execute(...)`, then evaluates `isExpired()` and
   `update()` on that captured snapshot. Concurrent validations each hold a copy with
   `countOfUses == 0`, so both pass and both get an assertion. Default lock is a JVM-local
   `DefaultLockRegistry`. Fix: re-read inside the lock, or add a consume/CAS operation to
   `TicketRegistry`.
   Puppeteer: no scenario covers concurrent validation of one ticket.

2. **`renew` enforcement uses shared mutable bean state.**
   `serviceValidateControllerValidationSpecification` and siblings are singleton
   `ChainingCasProtocolValidationSpecification` beans, but
   `AbstractServiceValidateController.validateAssertion` calls `spec.reset()` and then
   data-binds the request's `renew` onto that same instance per request. A concurrent
   non-renew request clears the flag another in-flight renew request set. Fix: per-request
   specification instance, or pass `renew` as an argument instead of bean state.
   Puppeteer: `ticket-validation-cas-renew` is single-threaded and would not catch it.

### HIGH — security

3. **pgtUrl authorization is a substring regex and does not require HTTPS.**
   `RegexMatchingRegisteredServiceProxyPolicy.isAllowedProxyCallbackUrl` uses unanchored,
   case-insensitive `RegexUtils.find`, so `https://app.example.org/.*` also matches
   `https://evil.example/?x=https://app.example.org/`. `ProxyAuthenticationHandler` never
   checks the scheme (its Javadoc claims it does) and the shared HTTP client follows
   redirects by default. CAS 3.0 requires pgtUrl to be HTTPS with peer trust established.
   The repo's own `ticket-validation-casv3-pgt/services/Sample-1.json` ships `^https?://.*`.

4. **Proxy callback runs before the ticket is validated.**
   In `AbstractServiceValidateController.handleTicketValidation`,
   `getServiceCredentialsFromRequest` -> `handleProxyGrantingTicketDelivery` performs the full
   authentication transaction (outbound GET to the caller-supplied pgtUrl) and mints/persists a
   PGT *before* `validateServiceTicket` checks service match, before the validation
   specification and before `enforceTicketValidationAuthorizationFor`. A leaked ST therefore
   drives outbound requests and orphaned PGT creation even when validation then fails.

5. **SAML 1.1 `Response` has a bogus `InResponseTo` and no `Recipient`.**
   `Saml10ObjectBuilder.newResponse` assigns `InResponseTo` from the `recipient` argument,
   which `AbstractSaml10ResponseView.getServiceIdFromRequest` computes as only the *host* of
   the TARGET URL; it is overwritten only when the SOAP body carried a `RequestID`. The
   SAML 1.1 `Response/@Recipient` attribute is never set, so an RP cannot bind the assertion
   to its own request/endpoint.
   Puppeteer: `ticket-validation-saml1` always sends a `RequestID`, so the default path is
   never exercised.

6. **XML injection surface in CAS 2.0/3.0 validation responses.**
   `casServiceValidationSuccess.mustache` renders `<cas:proxy>{{{.}}}</cas:proxy>` with a
   triple mustache (unescaped) and proxy URLs are only substring-checked (see #3).
   `CasProtocolAttributesRenderer.sanitizeAttributeName` only replaces spaces with
   underscores before the name is interpolated into an element name.

### HIGH — protocol compliance

7. **Wrong error codes.** Validation-specification failures (proxy ticket on
   `/serviceValidate`, unsatisfied `renew`) return `INVALID_TICKET`; CAS 3.0 requires
   `INVALID_TICKET_SPEC`. There is no such constant in `CasProtocolConstants`, though
   `messages.properties` already defines the text. The catch-all `Throwable` branch returns
   `INVALID_REQUEST` where the spec requires `INTERNAL_ERROR`.
   Puppeteer: `ticket-validation-cas-renew` asserts `code="INVALID_TICKET"` and must be
   updated alongside any fix.

### HIGH — performance

8. **Attribute-release policy is evaluated at least twice per validation.**
   `validateServiceTicket` calls `attributePolicy.getAttributes(context)` and then
   `registeredService.getAttributeReleasePolicy().getAttributes(releasePolicyContext)` on a
   near-identical context (only `selectedService` vs `service` differ), then merges four
   attribute maps; the response views then call `servicesManager.findServiceBy(...)` and
   re-resolve attributes again while rendering. Release policies commonly hit
   person-directory back-ends, so this doubles remote attribute cost on every
   `/serviceValidate`, `/p3/serviceValidate` and `/samlValidate`.

### Notes (lower, recorded for context)

- `SamlServiceFactory.createService` re-parses the SOAP envelope with JDOM each time the
  service is created; `AbstractSaml10ResponseView` calls `extractService(request)` again at
  render time, so `/samlValidate` parses the request body at least twice. **Fixed** — see below.
- `DefaultServiceMatchingStrategy.compareServices` URL-decodes both sides and compares
  case-insensitively, which loosens ST-to-service binding for URLs differing only in case or
  percent-encoding.
- XXE is correctly handled (`AbstractSamlObjectBuilder.constructDocumentFromXml` and the
  `DocumentBuilderFactory` path both disable DTDs and external entities). Proxy-chain
  ordering in `<cas:proxies>` is spec-correct (most recent first).

## Replace Java monitor synchronization (2026-09-08)

Scope: replace all seven Java `synchronized` keyword usages in the current source tree;
retain prose references and preserve existing staged/unstaged work. Verification is
limited to static review under the user's saved manual-test preference.

- [x] Inventory keyword usages and inspect existing changes and lock utilities.
- [x] Trace collection ownership, cache initialization, delivery shutdown and mock-server lifecycle.
- [x] Replace each monitor with a suitable concurrent collection, coordination primitive or lock;
      remove unnecessary locking and preserve required ordering and completion behavior.
- [x] Extend relevant existing regression coverage and cross-check affected Puppeteer scenarios.
- [x] Review the final diff and rescan the repository; record verification and remaining limitations.

Inventory: `DefaultAuthenticationResultBuilder` (2), `ResourceBundleThemeSource` (2),
`CloudWatchAppender` (2), test utility `MockWebServer.Worker` (1).

Implementation decisions:

- Authentication builders must be thread-safe by interface contract. Use insertion-ordered
  `CopyOnWriteArraySet` storage, a matching concurrent credential list, and one local history
  snapshot for both attribute merging and principal election.
- Theme creation and parent initialization share one `CasReentrantLock`. Add blocking
  `execute` helpers because existing `tryLock` helpers can skip work after five seconds.
  Cached reads retain their fast path, and parent assignment occurs under the same lock.
- CloudWatch's monitor only signals one-way shutdown; a `CountDownLatch` preserves that
  signal even if it arrives before the delivery thread waits. Preserve the zero-period
  indefinite wait, timed flushing, final queue drain and existing shutdown join timeout.
- The mock server creates one worker thread; `stop` closes its socket without acquiring
  the worker monitor. Remove the unused method monitor without adding another lock.

Regression coverage added (not executed): authentication snapshot consistency, insertion
order and duplicate handling; lock reentrancy, release after exceptions and concurrent
updates; CloudWatch shutdown and pending-event delivery with zero/30-second flush periods.
Existing theme-source and HTTP-client tests were inspected for compatibility.

Puppeteer static cross-check: `aws-cloudwatch-logging`, `rest-authentication-login`,
`themes-per-service`, `themes-external-per-service`, `themes-collection-twbs-login`,
`themes-collections-example`, `thymeleaf-templates-external`, `thymeleaf-templates-rest`,
`cas-validation-protocol-v2`, `cas-validation-protocol-v3`, `pm-account-profile`.
Existing assertions still match the intended behavior; no scenario changes are needed.
No tests, Gradle tasks, checkstyle tasks or Puppeteer scenarios have been executed.

Final static review: all seven executable keyword usages are gone. The only remaining
Java matches are three existing Javadoc references in `CasReentrantLock`.
The focused `git diff --check` passes. A repository-wide check also reports pre-existing
Windows-wrapper whitespace; unrelated wrapper edits are preserved. Reviewed imports,
checkstyle rules, lock release and acquisition behavior, collection serialization and
ordering, and worker ownership. Added a brief RC2 note under Other Stuff.

### Compile follow-up: ThreadPriorityCheck

- [x] Inspect the reported test compilation warning and current staged changes.
- [x] Replace scheduler hints with explicit coordination between concurrent test tasks.
- [x] Perform a focused static diff check; leave build/test execution to the user.

Replaced `Thread.yield()` with a timed `CyclicBarrier` before each locked increment.
All 20 workers rendezvous outside the lock; the test still verifies 2,000 total updates.
Focused `git diff --check` passes. No Gradle tasks or tests were run.

## View layer: CAS 1.0 newline safety + REST template header leak (2026-09-08)

Scope: two fixes carried over from the view/presentation-layer review — line-break injection in
the line-delimited CAS 1.0 validation response, and the credential-header leak in the REST-backed
Thymeleaf template resolver. Nothing else from that review was changed.

- [x] Trace both defects to their enforcement points and confirm no shared upstream fix applies.
- [x] Strip line breaks in `Cas10ResponseView`, which owns the `\n`-delimited response format,
      so every attribute renderer (including custom ones) is covered by one change.
- [x] Filter credential-bearing headers out of the outbound header map in
      `RestfulUrlTemplateResolver` before it calls the external template endpoint.
- [x] Extend the two nearest existing test classes with regressions that fail on the old code.
- [x] Record both changes in `docs/cas-server-documentation/release_notes/RC2.md`.
- [x] Static review of the final diff; cross-check the affected Puppeteer scenarios.

Implementation decisions:

- The CAS 1.0 response has no escaping mechanism, so a value carrying a newline cannot be
  represented — it has to be removed. `Cas10ResponseView.sanitizeResponseLine` strips `\R` (any
  Unicode line-break sequence, so `\r` alone and `\r\n` are covered, not just `\n`) from the
  principal identifier and from each rendered attribute line. Fixing it in the view rather than in
  `AttributeValuesPerLineProtocolAttributesRenderer` means the guarantee holds for any renderer
  wired into `cas1ProtocolAttributesRenderer`, present or future.
- `RestfulUrlTemplateResolver` crosses into a separate trust domain, so `Cookie`, `Set-Cookie`,
  `Authorization` and `Proxy-Authorization` are dropped from the forwarded set. The rest of the
  inbound headers, the resolver's own `owner`/`template`/`resource`/`theme`/`locale` values, and
  the configured `cas.view.rest.headers` still flow through with unchanged precedence.
  `HttpRequestUtils.getRequestHeaders` was deliberately left alone: it has a dozen callers that
  keep the headers in-process, and narrowing it would be a broad refactor.

Regression coverage added (not executed):

- `Cas10ResponseViewTests.verifySuccessViewRejectsLineBreaksInPrincipalId` asserts the exact
  two-line response for a principal id of `casuser\nyes\nadministrator`.
- `Cas10ResponseViewTests.verifySuccessViewRejectsLineBreaksInAttributeValues` asserts that an
  attribute value of `staff\nadministrator=true` no longer produces a standalone
  `administrator=true` line.
- `RestfulUrlTemplateResolverTests.verifyCredentialHeadersAreNotForwarded` drives a real request
  through `MockWebServer.headersConsumer` and asserts the server never receives `Cookie`,
  `Authorization` or `Proxy-Authorization`, while `X-Custom-Header` and `template` still arrive.

Puppeteer static cross-check: `cas-validation-protocol-v2`, `cas-validation-protocol-v3`,
`thymeleaf-templates-rest`, `thymeleaf-templates-external`. None assert on line breaks in a
CAS 1.0 response or on credential headers reaching the template endpoint, so no scenario changes
are needed.

### Verification not run

The device shell has no network access and the Gradle `9.8.0-rc-1` wrapper distribution is not
cached locally, so `./gradlew` cannot bootstrap and no build, test, checkstyle or Puppeteer task
was executed. Verification was static: focused `git diff --check` passes, and imports, generics,
Checkstyle line length and Mockito default-answer behavior were reviewed by hand. Still to run:

```bash
./gradlew :support:cas-server-support-validation:test --tests "*Cas10ResponseViewTests"
./gradlew :support:cas-server-support-thymeleaf:test --tests "*RestfulUrlTemplateResolverTests"
```

### Follow-ups noticed but deliberately not changed

- `RestfulUrlTemplateResolver` writes its own `owner`/`template`/`resource`/`theme`/`locale`
  entries into the header map *before* copying the inbound headers, so a client header of the same
  name still overwrites the resolver's value. A separate injection defect from the leak.
- `HttpUtils.execute` passes every outbound header key and value through
  `SpringExpressionLanguageValueResolver`, a `${...}` SpEL template context with system properties
  and environment variables bound as variables. Any caller forwarding client-supplied headers
  hands attacker-controlled text to that resolver; this is shared by all `HttpUtils` callers and
  is not a surgical fix.

## View layer: theme-name validation + per-request theme resolution (2026-09-09)

Scope: two more items from the view-layer review — the dead memoization in
`RegisteredServiceThemeResolver`, and the unbounded caches keyed by a client-supplied theme name.

- [x] Confirm the theme-definition contract in the documentation before relying on it.
- [x] Establish that `ChainingThemeResolver` is the single gate every theme consumer passes through.
- [x] Validate client-supplied theme names against a real theme definition at that gate.
- [x] Make `RegisteredServiceThemeResolver` read back the request attribute it already writes.
- [x] Add regressions that fail on the old code; check the existing theme tests still hold.
- [x] Record both changes in `docs/cas-server-documentation/release_notes/RC2.md`.

Contract relied upon: both `User-Interface-Customization-Themes-Static.html` and
`User-Interface-Customization-ThemedViews.html` open with "Add a `[theme_name].properties` placed
to the root of `src/main/resources`". Every shipped theme honors it — `cas-theme-default.properties`,
`example.properties`, `twbs.properties` on the classpath, and `fancy.properties` at the root of the
external template prefix in the `themes-external-per-service` scenario.

Implementation decisions:

- The check went into `ChainingThemeResolver` rather than into the individual caches. It is the one
  bean every consumer injects — the two theme-aware template resolvers, the REST template resolver,
  `CasThymeleafTemplatesDirector` and `ThemeBasedViewResolver` — so guarding it keeps invalid names
  out of `ThemeBasedViewResolver.resolvers`, `ResourceBundleThemeSource.themeCache` and the
  `themes/%s/` template path interpolation at once, instead of guarding three sites separately.
  Nothing else reads `CookieThemeResolver.THEME_REQUEST_ATTRIBUTE_NAME` or
  `SessionThemeResolver.THEME_SESSION_ATTRIBUTE_NAME` directly, so there is no way around the gate.
- Validation is scoped to *client-supplied* resolvers, added through a new
  `addClientSuppliedResolver` rather than to everything the chain produces. The Groovy and REST
  theme sources deliberately return a name of the script's choosing without checking for a
  definition — `RegisteredServiceThemeResolverTests.verifyGroovyTheme` encodes that with a
  `some-theme` fixture that has no properties file. Validating the whole chain would have tightened
  admin-configured behavior and broken that test; validating client input alone fixes the actual
  threat and leaves the rest untouched.
- The existence probe checks `[theme].properties` under each configured template prefix and at the
  classpath root, mirroring `DefaultCasThemeSource.createMessageSource`. It deliberately does not
  use `ResourceBundle.getBundle` the way `RegisteredServiceThemeResolver.resolveThemeForService`
  does, because that would feed caller-controlled names into the JDK's bundle cache; a theme that
  ships only locale-suffixed bundles and no base file is outside the documented contract.
- Successful lookups are recorded in a `definedThemeNames` set. Only positives are cached, so the
  set is bounded by the themes the deployment actually ships; a name that fails is re-probed, which
  is a classpath miss rather than a retained entry.
- `RegisteredServiceThemeResolver` now reads back the attribute that `rememberThemeName` has always
  written. That write is not unused — `WsFederationCookieManager` reads the same attribute to carry
  the theme across a federation round trip — so the fix is to consume it, not to remove it. As a
  side effect, the WS-Fed restore path now actually takes effect.

Regression coverage added (not executed):

- `ChainingThemeResolverTests` — a defined theme in the `theme` header is honored; an undefined one
  and a traversing one (`../../../../etc/cas/config`) are ignored in favor of the default; an
  undefined header value yields to the registered service's own theme rather than suppressing it.
- `RegisteredServiceThemeResolverTests.verifyThemeIsResolvedOncePerRequest` — resolves once, asserts
  the request attribute is set, deletes the service, and asserts the second call still returns the
  remembered theme. Fails on the old code, which would re-resolve and fall back to the default.

Existing theme tests re-checked by reading: `verifyGroovyTheme` and `verifyUrlTheme` still pass
because the registered-service resolver is not validated; `verifyCustomTheme` and `verifyCustomSource`
use `custom-theme.properties` and `ext-templates/my-theme.properties`, which exist;
`ServiceThemeResolverTests` only ever resolves to the default.

### Verification not run

Same constraint as the previous section — no network on the device shell and no cached Gradle
`9.8.0-rc-1` distribution, so nothing was built or executed. `git diff --check` passes and line
lengths, imports and Lombok constructor generation were reviewed by hand. Still to run:

```bash
./gradlew :support:cas-server-support-themes:test --tests "*ChainingThemeResolverTests"
./gradlew :support:cas-server-support-themes:test --tests "*RegisteredServiceThemeResolverTests"
./gradlew :support:cas-server-support-themes:test --tests "*ServiceThemeResolverTests"
```

Puppeteer static cross-check: `themes-per-service`, `themes-external-per-service`,
`themes-collection-twbs-login`, `themes-collections-example`, `thymeleaf-templates-external`,
`thymeleaf-templates-rest`. All drive themes through registered services or the configuration, not
through a request header or cookie, so none is affected.

### Follow-ups noticed but deliberately not changed

- `ChainingThemeResolver` still walks the chain on every template resolution. Now that the expensive
  resolver memoizes per request the remaining cost is small, but a chain-level request attribute
  would collapse it entirely. Safe to add — `ChainingThemeResolver.setThemeName` is the inherited
  no-op, so nothing mutates the theme mid-request through the chain.
- Because `ChainingThemeResolver.setThemeName` is a no-op, the `?theme=` request parameter handled by
  `ThemeChangeInterceptor` currently has no effect at all. Separate defect, not touched.
- The Groovy and REST theme sources remain the one path that can name a theme with no definition
  behind it. Tightening them would make the theme contract uniform, at the cost of a behavior change
  and one test fixture.

### Amendment (2026-09-09): theme validation made uniform

`addClientSuppliedResolver` has been removed. `ChainingThemeResolver` now validates every theme
name it is handed, whatever resolver produced it, so the `[theme].properties` contract holds
uniformly instead of only for caller-supplied names. This supersedes the scoping decision recorded
in the section above.

The consequence is that the Groovy and REST theme sources are now held to the same contract: a
script or endpoint returning a name with no definition behind it falls back through the chain to
the default theme instead of selecting a theme that does not exist. No production code was needed
for that tightening — removing the distinction produced it.

Fixtures added so the existing tests keep exercising real themes rather than accidental ones:

- `support/cas-server-support-themes/src/test/resources/some-theme.properties` — `GroovyTheme.groovy`
  returns `some-theme` for `RegisteredServiceThemeResolverTests.verifyGroovyTheme`.
- `support/cas-server-support-themes/src/test/resources/shire.properties` — `tenants.json` assigns
  theme `shire` for `TenantThemeResolverTests.verifyOperation`.

Test added: `ChainingThemeResolverTests.verifyUndefinedThemeFromRestEndpointIsIgnored` drives a
registered service whose theme is a REST endpoint returning a random name and asserts the default
theme is used, covering the tightened behavior directly.

Fixture audit for the uniform rule (each verified to have a definition, or to be unaffected):

- `custom-theme`, `cas-theme-default`, `ext-templates/my-theme`, `more-ext-templates/my-theme` —
  all already present in the themes test resources.
- `ServiceThemeResolverTests` uses `myTheme` with no definition and already expects the default.
- Puppeteer `themes-collection-twbs-login`, `themes-collection-twbs-caps` and
  `themes-collections-example` set their theme through `cas.theme.default-theme-name`; the default
  is returned without validation, so they are unaffected.
- Puppeteer `themes-per-service` assigns `example` per service and `themes-collections` ships
  `example.properties`; `themes-external-per-service` assigns `fancy` and ships `fancy.properties`
  at the root of its template prefix; `multitenancy-login` ships `classpath/shire-theme.properties`.
- Puppeteer `login-success-public-workstation`, `login-success-warning-redirect`,
  `mfa-duo-universal-prompt-public-workstation` and `mfa-gauth-login-trusted-devices` assign
  `fancy` with no definition anywhere, so they already resolved to the default theme through
  `resolveThemeForService` and are unchanged.
- No Puppeteer scenario configures a Groovy or REST theme source.

Verification remains static for the same reason as the sections above; the commands to run are
unchanged, plus `--tests "*TenantThemeResolverTests"`.

## View layer: Thymeleaf template caching (2026-09-09)

Scope: the highest-impact performance item from the view-layer review — `ChainingTemplateViewResolver`
declaring every resolution non-cacheable, so no template or fragment was ever cached.

- [x] Establish why the flag was set and what breaks if it is simply flipped.
- [x] Make cacheability a per-resolution decision instead of a blanket one.
- [x] Take the chain's cacheability from `spring.thymeleaf.cache` like every other resolver.
- [x] Close the related trap on the directly-registered theme resolver bean.
- [x] Add tests over the resolution validity; record the change in RC2.

The problem: the chain overrode `computeTemplateResource`, kept only the delegate's
`ITemplateResource` and discarded the delegate's `TemplateResolution`, then wrapped the resource in
its own resolution using its own hard-coded `cacheable = false`. Every knob read correctly and
nothing warned, but the engine was told never to cache. Over twenty template resolutions per login
page render were therefore re-resolved and re-parsed on every request.

Why it could not simply be flipped: `ThemeFileTemplateResolver` and `ThemeClassLoaderTemplateResolver`
resolve the theme from the current request inside `computeTemplateResource` and interpolate it into
the resource path, so one template name maps to different files per request. Thymeleaf's cache key
carries the template name, owner, mode and resolution attributes, not the resolved theme, so caching
those resolutions under that key would serve one theme's markup to another.

Implementation decisions:

- Cacheability is now decided per resolution rather than per resolver. A new marker interface
  `ThemeAwareTemplateResolver`, implemented by `ThemeFileTemplateResolver` and
  `ThemeClassLoaderTemplateResolver` (and inherited by `RestfulUrlTemplateResolver`), identifies
  resolvers whose answer varies with the request. The chain records which delegate answered and
  returns `NonCacheableCacheEntryValidity` for those, deferring to `super.computeValidity` otherwise.
  A base template with no themed override is resolved by a plain resolver and is cached; a themed
  override is not. Deployments with no themed templates — the default — cache everything.
- This keeps a single chain bean, a single engine registration and the existing resolver order.
  Splitting into separate cacheable and non-cacheable chains would have reordered resolution across
  multiple template prefixes, so it was rejected.
- The hand-off between `computeTemplateResource` and `computeValidity` uses a `ThreadLocal` that is
  set on a successful resolution and consumed by the validity call. It is deliberately fail-safe: an
  absent value is read as theme dependent, so if the two were ever called in an unexpected order the
  result is lost caching, never a wrong cache hit.
- `themeClassLoaderTemplateResolver` is registered with the engine as its own bean as well as being a
  chain delegate, and was cacheable. It is theme dependent, so it is now explicitly non-cacheable.
  This closes the trap noted in the earlier review entry.
- `thymeleafViewResolver.setCache(false)` was left alone, contrary to what the earlier review entry
  suggested. It is load-bearing: `ThemeViewResolver#configureTemplateThemeDefaultLocation` mutates
  the returned view's template name per theme, and that resolver's cache key carries no theme, so a
  cached instance would be shared and rewritten across themes. `ThemeViewResolver` already applies a
  theme-qualified cache in front of it. A comment now records this so it is not "fixed" later.

Regression coverage added (not executed), in `ChainingTemplateViewResolverTests`:
a theme-independent resolution reports a cacheable validity; a resolution served by
`ThemeClassLoaderTemplateResolver` reports a non-cacheable one even with the chain cacheable; and a
chain configured non-cacheable still reports non-cacheable.

### Verification not run

Unchanged from the sections above: no network on the device shell and no cached Gradle
`9.8.0-rc-1` distribution, so nothing was compiled or executed. Three Thymeleaf API assumptions are
compile-checked rather than verified here — `ICacheEntryValidity` / `NonCacheableCacheEntryValidity`
in `org.thymeleaf.cache`, the `computeValidity` signature on `AbstractConfigurableTemplateResolver`,
and `TemplateResolution#getValidity` used by the tests. Each fails loudly at compile time if wrong.
The call order of `computeTemplateResource` before `computeValidity` is an assumption that fails
safe by design. Still to run:

```bash
./gradlew :support:cas-server-support-thymeleaf:test --tests "*ChainingTemplateViewResolverTests"
./gradlew :support:cas-server-support-thymeleaf:test --tests "*ThemeClassLoaderTemplateResolverTests"
./gradlew :support:cas-server-support-thymeleaf:test --tests "*RestfulUrlTemplateResolverTests"
```

Puppeteer cross-check: `themes-per-service`, `themes-external-per-service`,
`themes-collection-twbs-login`, `themes-collections-example`, `thymeleaf-templates-external`,
`thymeleaf-templates-rest`. These are the scenarios that would surface a stale or cross-theme
template, and `multitenancy-login` already sets `spring.thymeleaf.cache=false`.

### Amendment (2026-09-09): caching decision moved off the ThreadLocal

The `ThreadLocal<Boolean>` hand-off in `ChainingTemplateViewResolver` is gone. It only existed
because the class overrode `computeTemplateResource` and `computeValidity` separately and needed to
carry "which delegate answered" between the two hooks. Overriding `resolveTemplate` instead removes
the split, and with it the need to carry anything.

The chain now returns the winning delegate's own `TemplateResolution` unchanged. That was the
original defect in one sentence: the class unwrapped the delegate's resolution down to its
`ITemplateResource` and rebuilt a resolution from its own settings, discarding the delegate's
template mode, decoupled-logic flag and cache validity. Handing the delegate's resolution straight
back fixes caching without any conditional logic in the chain at all.

Cacheability is now decided once, in `configureTemplateViewResolver`, which every resolver
construction site already flows through:

```java
resolver.setCacheable(thymeleafProperties.isCache() && !(resolver instanceof ThemeAwareTemplateResolver));
```

`ThemeAwareTemplateResolver` therefore earns its place as the single predicate rather than as a hint
consumed inside the chain. `chain.setCacheable(...)` and the explicit `themeCp.setCacheable(false)`
are both gone as redundant; the chain's own prefix, suffix, mode and cacheable settings now take no
part in resolution, which the class javadoc states.

Test coverage moved with the logic. `ChainingTemplateViewResolverTests` now asserts the chain's real
contract — a cacheable delegate's resolution comes back cacheable, a non-cacheable delegate's comes
back non-cacheable, and an empty chain resolves to nothing. `CasThymeleafConfigurationTests` gained
`verifyThemeAwareResolversAreNeverCacheable`, which inspects the assembled chain and asserts every
theme-aware delegate is non-cacheable while every other one follows `spring.thymeleaf.cache`. That
test class already configures two template prefixes and a REST view endpoint, so it covers the
`ThemeFileTemplateResolver`, `ThemeClassLoaderTemplateResolver` and `RestfulUrlTemplateResolver`
cases together.

One Thymeleaf assumption changed shape: instead of `ICacheEntryValidity` and
`NonCacheableCacheEntryValidity`, the code now depends on `AbstractTemplateResolver#resolveTemplate`
being overridable and on `AbstractConfigurableTemplateResolver#computeTemplateResource` remaining an
abstract hook (implemented here as an unused stub). Both fail loudly at compile time. Nothing was
built or run; the constraints recorded in the sections above are unchanged.

### Amendment (2026-09-09): marker interface replaced by a cacheable flag

`ThemeAwareTemplateResolver` is deleted. `ThemeFileTemplateResolver` and
`ThemeClassLoaderTemplateResolver` no longer implement anything extra, and
`configureTemplateViewResolver` now takes the decision as a parameter:

```java
private static void configureTemplateViewResolver(final AbstractConfigurableTemplateResolver resolver,
                                                  final ThymeleafProperties thymeleafProperties,
                                                  final boolean cacheable) {
    resolver.setCacheable(cacheable && thymeleafProperties.isCache());
```

Each of the five construction sites states its own answer: `false` for the REST resolver, for the
per-prefix theme resolver and for the `themeClassLoaderTemplateResolver` bean; `true` for the
per-prefix plain resolver and the `classLoaderTemplateResolver` bean. The `instanceof` test is gone
along with the interface.

This also drops the conflation noted earlier. The marker had two unrelated reasons riding on it —
"the cache key cannot tell themes apart" for the two theme resolvers, and "the body is fetched over
HTTP per request" for the REST resolver. A per-site boolean simply records the answer without
implying they share a cause, which matters if the theme-qualified cache key is adopted later: the
two theme resolvers would flip to `true` and the REST one would stay `false`.

The trade is that a future resolver added to the chain must remember to pass the right value; there
is no longer a type the compiler or a reader can key off. The javadoc on the parameter carries the
rule.

`CasThymeleafConfigurationTests.verifyThemeAwareResolversAreNeverCacheable` now identifies the
theme-aware resolvers by concrete type through a local `isThemeAware` predicate.
`RestfulUrlTemplateResolver` extends `ThemeFileTemplateResolver`, so the first branch covers it.

### Amendment (2026-09-09): explanatory javadoc removed from the code

Per review, the member-level javadoc and block comments added during this work were removed from
`ChainingTemplateViewResolver`, `ChainingThemeResolver`, `Cas10ResponseView`,
`CasThymeleafAutoConfiguration` and `CasThymeleafConfigurationTests`. Pre-existing javadoc, all
class-level javadoc and the `@author`/`@since` headers are untouched, and nothing that Checkstyle
requires was removed: `MissingJavadocMethod` is scoped to `public` and exempts `@Override`, and
`JavadocVariable` applies to public fields only, so the stripped items were all protected/private
members or overrides.

The rationale for four non-obvious pieces of code now lives only here:

- `ChainingTemplateViewResolver.resolveTemplate` returns the winning delegate's own
  `TemplateResolution` deliberately, to preserve its template mode, decoupled-logic flag and cache
  validity. Unwrapping it to an `ITemplateResource` is what lost parsed-template caching originally.
- `ChainingTemplateViewResolver.computeTemplateResource` returns null because `resolveTemplate` is
  overridden and the hook is unreachable; it exists only to satisfy the abstract parent.
- The `cacheable` argument to `configureTemplateViewResolver` is `false` for any resolver whose
  answer varies per request, because Thymeleaf's template cache key carries no theme.
- `thymeleafViewResolver.setCache(false)` is load-bearing: `ThemeViewResolver` mutates the views it
  is handed, and its cache key carries no theme.


---

# Fix: CRITICAL #2 — `renew` enforced via shared mutable bean state

## Decision

`renew` will be resolved from the `HttpServletRequest` that `isSatisfiedBy` already receives;
the controller stops writing to the shared specification bean. `cas.sso.renew-authn-enabled`
keeps its current (inert) effect on the validation path — no behavior change there.

## Steps

- [x] 1. `AbstractCasProtocolValidationSpecification`: resolve `renew` from the request,
      OR-ed with the programmatic `renew` field so existing callers/tests keep working
- [x] 2. `ChainingCasProtocolValidationSpecification`: stop mutating children in
      `isSatisfiedBy`; drop the per-request `renew` field; forward `setRenew` explicitly
- [x] 3. `AbstractServiceValidateController.validateAssertion`: remove `spec.reset()` and the
      `ServletRequestDataBinder`; remove the orphaned `initBinder`
- [x] 4. Regression tests: `ServiceValidateControllerRenewTests` (MockMvc, real singleton
      wiring) — shared spec must not retain `renew` after a request, plus a concurrent
      renew/non-renew run
- [x] 5. Unit coverage in `Cas20ProtocolValidationSpecificationTests` and
      `ChainingCasProtocolValidationSpecificationTests` that `renew=true` is honored from the
      request, that other values are not, and that no state is left behind
- [ ] 6. Compile the touched modules and run the validation test categories — **NOT RUN.**
      This machine has only JDK 11 (`sourceCompatibility=25`) and no Gradle cache, so
      `./gradlew` cannot run here. Static review only: `git diff --check` clean, no residual
      references to `initBinder`, `ServletRequestDataBinder` or the removed chain `renew`
      field, and all four `CasProtocolValidationSpecification` implementations reviewed.
- [x] 7. Cross-check puppeteer scenarios — `ticket-validation-cas-renew` is the only scenario
      that sends `renew` to a validation endpoint (`renew=true` on `/validate`,
      `/serviceValidate`, `/p3/serviceValidate`, expecting `no\n\n` / `INVALID_TICKET`);
      `"true"` remains an accepted value so its assertions are unchanged. `sso-policy-service`,
      `sso-renew-existing-session` and `login-double-session` use `renew` on `/login` only,
      which is a different code path. `cas-validation-protocol-v2` / `-v3` send no `renew`.

- [x] 8. Add a user-facing note to the `### CAS Protocol` section of
      `docs/cas-server-documentation/release_notes/RC2.md`

## Verification still owed

- First run of `ServiceValidateControllerRenewTests` failed three tests. Cause was the test
  fixture, not the fix: `ServiceTicketImpl` sets
  `fromNewLogin = credentialProvided || ticket.getCountOfUses() == 0`, so the *first* service
  ticket issued by a fresh ticket-granting ticket counts as issued from a new login even when no
  credentials were supplied. The helper now establishes the single sign-on session, burns that
  first ticket, and only then issues the tickets under test — the same shape as
  `AbstractServiceValidateControllerTests.getHttpServletRequest()`.
- `Cas20ProtocolValidationSpecificationTests` and `ChainingCasProtocolValidationSpecificationTests`
  passed on that run, so the specification change itself is exercised and green.
- Still to run: `ServiceValidateControllerRenewTests` after the fixture fix (the concurrent test
  in particular has never completed), `./testcas.sh --category cas`, and the
  `ticket-validation-cas-renew` puppeteer scenario.


---

# Fix: HIGH #4 — proxy callback fires before the ticket is validated

## CI assertion isolation (2026-09-11)

- [x] Trace the failure: the CAS category runs test methods concurrently, and the controller tests
      share a Spring ticket registry. Successful proxy tests can change the global PGT count
      between the failed-validation test's two reads.
- [x] Scope the regression assertion to the unique TGT created by this test and close the registry stream.
- [x] Review the focused diff and existing renew/PGT Puppeteer scenarios. `git diff --check` passes;
      `ticket-validation-cas-renew`, `ticket-validation-casv3-pgt` and `ticket-validation-casv3-pgtiou`
      require no changes because only the JUnit assertion changed. Puppeteer was not run.
- [ ] Run `ProxyValidateControllerTests` through the parallel CAS category with predictive selection and retries disabled.

## Decision

`CentralAuthenticationService` gains a `default` overload taking the `ServiceTicket` object; the
id-based method resolves the ticket, keeps its null/expired guard, and delegates.
`DefaultCentralAuthenticationService` overrides the new one with the real work. Both carry `@Audit`;
Spring self-invocation means exactly one audit record is produced on either path.

## Steps

- [x] 1. `CentralAuthenticationService`: add
      `default createProxyGrantingTicket(ServiceTicket, AuthenticationResult)` delegating to the id form
- [x] 2. `DefaultCentralAuthenticationService`: split into id-based (resolve + expired guard +
      delegate) and ticket-based (registered-service lookup, access enforcement, proxy policy,
      minting under lock)
- [x] 3. `AbstractServiceValidateController.handleTicketValidation`: capture the service ticket
      before validation, then run `validateServiceTicket` -> validation specification ->
      authentication context, and only then the proxy callback and PGT.
      `handleProxyGrantingTicketDelivery` now takes the `ServiceTicket`
- [x] 4. Regression test `ProxyValidateControllerTests.verifyNoProxyGrantingTicketWhenValidationFails`:
      good ticket, not from a new login, validated with `renew=true` and a `pgtUrl`; asserts the
      response is a failure and the registry contains no proxy-granting tickets rooted in this test's TGT
- [x] 5. RC2 release note
- [ ] 6. Compile and run — **NOT RUN** (JDK 11 only on this machine, no Gradle cache)
- [x] 7. Puppeteer cross-check: `ticket-validation-casv3-pgt`, `ticket-validation-casv3-pgtiou`,
      `ticket-validation-casv3-pgt-multiple-pt`, `ticket-validation-casv3-pgt-stateless` all validate
      a ticket that succeeds, so the PGT is still issued and the callback still runs — just after the
      checks instead of before. No scenario asserts a PGT is issued on a failed validation.

## Side effect worth noting

`getServiceCredentialsFromRequest` authorizes the `pgtUrl` against the **requested** service's proxy
policy, while `createProxyGrantingTicket` enforces `isAllowedToProxy()` against the **ticket's**
service. Validation now runs in between and enforces that the two services match, so the asymmetry
is closed as a side effect of the reorder.

## Also fixed this round

`ServiceValidateControllerRenewTests.ConcurrentEvaluation` was failing because all 50 tickets came
from one ticket-granting ticket for one service: `MostRecentServiceSessionTrackingPolicy` (the
default, `cas.ticket.tgt.core.only-track-most-recent-session=true`) deletes the previously issued
ticket for the same service from the registry, so every ticket but the last was already gone. Each
ticket now gets its own single sign-on session.


---

# Fix: HIGH #5 — SAML 1.1 response `InResponseTo` / `Recipient`

Checked against the SAML 1.1 protocol schema: `ResponseAbstractType` declares
`InResponseTo` as an optional `NCName` (an ID reference to the request's `RequestID`) and
`Recipient` as an optional `anyURI`.

- [x] 1. `Saml10ObjectBuilder.newResponse`: set `Recipient` from the recipient argument when
      present; stop assigning it to `InResponseTo`, which is now set only from the request's
      `RequestID` and otherwise omitted
- [x] 2. `AbstractSaml10ResponseView`: the recipient is the full service URL, not
      `new URI(service.getId()).getHost()`, and is null rather than `"UNKNOWN"` when there is no
      service. Renamed `getServiceIdFromRequest` to `getResponseRecipient`; dropped the orphaned
      `FunctionUtils` import
- [x] 3. `SamlResponseBuilder.createResponse` / `finalizeSamlResponse`: parameter renamed to
      `recipient` with matching javadoc
- [x] 4. Tests in `Saml10ObjectBuilderTests`: recipient is carried, `InResponseTo` echoes the
      `RequestID`, and both are omitted when not supplied
- [x] 5. RC2 release note
- [ ] 6. Compile and run — **NOT RUN** (JDK 11 only on this machine, no Gradle cache)
- [x] 7. Puppeteer cross-check: `ticket-validation-saml1` always sends a `RequestID` and asserts
      only on the assertion body, so it is unaffected. No scenario asserts on `InResponseTo` or
      `Recipient`.

# Fix: HIGH #6 — XML injection surface in the v2/v3 validation responses

- [x] 1. `casServiceValidationSuccess.mustache` (2.0 and 3.0): `<cas:proxy>` switched from the
      unescaped `{{{.}}}` to `{{.}}`, so proxy URLs are XML-escaped. The remaining `{{{.}}}` for
      `formattedAttributes` is deliberate — that model value is markup produced by the renderer,
      which is where safety has to be enforced
- [x] 2. `DefaultCas30ProtocolAttributesRenderer.toXmlElementName`: attribute names are reduced to
      valid XML names before becoming element names, which also covers
      `InlinedCas30ProtocolAttributesRenderer` (it inherits `render` and places the same name inside
      a quoted XML attribute). `CasProtocolAttributesRenderer.sanitizeAttributeName` is deliberately
      left alone so the CAS 1.0 plain-text renderer keeps its current output
- [x] 3. Tests: hostile names cannot produce anything but a single well-formed element, names are
      given a valid start character, ordinary names are untouched, and the inlined renderer cannot be
      broken out of its `name="..."` attribute
- [x] 4. RC2 release note, including the visible change for names with characters that are illegal
      in XML names
- [ ] 5. Compile and run — **NOT RUN**
- [x] 6. Puppeteer cross-check: `ticket-validation-casv3-pgtiou` asserts
      `<cas:proxy>http://localhost:56789/cas</cas:proxy>` and `ProxyValidateControllerTests`
      asserts `<cas:proxy>http://localhost:PORT</cas:proxy>`; neither URL contains a character that
      escaping would alter, so both still pass.

## Verification still owed for both

- `./testcas.sh --category cas`, `--category saml1`, `--category attributes`
- `ticket-validation-saml1`, `ticket-validation-casv3-pgtiou`, `cas-validation-protocol-v2`,
  `cas-validation-protocol-v3`


---

# Fix: HIGH #7 — validation error codes

## Correction to the original finding

The finding claimed an unsatisfied `renew` should report `INVALID_TICKET_SPEC`. That was wrong.
The current CAS protocol specification defines `INVALID_TICKET` as "the ticket provided was not
valid, or the ticket did not come from an initial login and `renew` was set on validation. It will
fail if the ticket was issued from a single sign-on session." So the renew case already reported the
right code, and `ticket-validation-cas-renew` asserting `INVALID_TICKET` is correct and unchanged.

`INVALID_TICKET_SPEC` is "failure to meet the requirements of validation specification", which is the
proxy-ticket-on-`/serviceValidate` case. `INTERNAL_ERROR` is "an internal error occurred during
ticket validation"; `INVALID_REQUEST` is "not all of the required request parameters were present"
and stays where it is, on the missing-parameter branch.

## Steps

- [x] 1. `CasProtocolConstants`: add `ERROR_CODE_INVALID_TICKET_SPEC` and `ERROR_CODE_INTERNAL_ERROR`
- [x] 2. `AbstractCasProtocolValidationSpecification.isRenewRequested` promoted to `public static` so
      the controller can ask the same question the specification asks, instead of re-parsing the
      parameter
- [x] 3. `AbstractServiceValidateController.getValidationSpecificationErrorCode`: `INVALID_TICKET`
      when renew was requested and the assertion is not from a new login, `INVALID_TICKET_SPEC`
      otherwise; catch-all `Throwable` now reports `INTERNAL_ERROR` with a resolved description
- [x] 4. `messages.properties`: added `INTERNAL_ERROR`; `INVALID_TICKET_SPEC` no longer claims to
      cover the `renew` case, since it no longer does
- [x] 5. Tests: `ProxyValidateControllerTests.verifyProxyTicketOnServiceValidateReportsInvalidTicketSpec`
      mints a proxy ticket and validates it on `/serviceValidate`;
      `ServiceValidateControllerRenewTests` now asserts the `INVALID_TICKET` code rather than only
      that the response is a failure
- [x] 6. RC2 release note
- [ ] 7. Compile and run — **NOT RUN** (JDK 11 only on this machine, no Gradle cache)
- [x] 8. Puppeteer cross-check: `ticket-validation-cas-renew` asserts `INVALID_TICKET` and is
      **unchanged**, since that code is correct for the renew case.
- [x] 9. Closed the coverage gap with a new scenario,
      `ticket-validation-casv3-pgt-service-validate`: no existing scenario validated a proxy ticket
      through a service ticket validator. It reuses the `ticket-validation-casv3-pgtiou` shape —
      same `script.json`, same two service definitions, the same request-basket capture of the
      `pgtUrl` callback to recover the `pgtId`, and the same local `proxyValidateRequest` /
      `requestProxyTicket` helpers — then mints a proxy ticket per assertion and checks that
      `/serviceValidate` and `/p3/serviceValidate` (via the shared `cas.validateTicket` helper) both
      reject it with `INVALID_TICKET_SPEC`, while `/p3/proxyValidate` still accepts it.
      `npx eslint` and `node --check` pass on the script; scenarios are discovered from the
      directory listing by the `puppeteerScenarios` Gradle task, so no registration is needed.

## Verification still owed

- `./testcas.sh --category cas`
- `ticket-validation-cas-renew`, `ticket-validation-casv3-pgt`, `ticket-validation-casv3-pgtiou`


---

# Fix: attribute release policy evaluated more than once per validation

## Correction to the original finding

The original write-up called this a throughput problem. It is not: the principal attribute
repository is cached, so the dominant cost is already avoided. What survives is redundant policy
work (definition-store resolution, a `repository.update` cache *write*, `getAttributesInternal`,
default attributes, post-processors, filter — and for a REST or Groovy policy, real I/O the
repository cache does not cover) plus a genuine asymmetry.

## Two duplicate evaluations, both removed

1. `DefaultCentralAuthenticationService.validateServiceTicket` evaluated the policy a second time
   to build the access-strategy principal, and did so against the **raw request service** while
   everything else in the method uses the **resolved service**. `DefaultAuthenticationBuilder.of`
   builds the final principal from exactly the first evaluation's output, and the multivalued
   merger sets `distinctValues(true)`, so the extra merge contributed nothing when the two services
   agreed and contributed attributes computed for the wrong service when they did not. Removed the
   second context, `policyAttributes`, and the fourth merge.

2. Found while double-checking: `DefaultAuthenticationBuilder.of` did not pass the attributes it was
   handed to `RegisteredServiceUsernameProviderContext`, so
   `PrincipalAttributeRegisteredServiceUsernameProvider.resolveUsernameInternal` re-evaluated the
   whole release policy through `getPrincipalAttributesFromReleasePolicy` (it only recomputes when
   `context.getReleasingAttributes()` is null). This is the duplicate that fires in production for
   any service using a principal-attribute username provider. Fixed by threading
   `releasingAttributes(principalAttributes)` into the context — the same pattern
   `AbstractRegisteredServiceAttributeReleasePolicy` already uses.

   Verified all four callers of `DefaultAuthenticationBuilder.of` pass the policy's released
   attributes as `principalAttributes`, so the value is always the right one; and that each of them
   (`validateServiceTicket`, `CasProtocolValidationEndpoint`, `CasReleaseAttributesReportEndpoint`,
   `SSOSamlIdPPostProfileHandlerEndpoint.produce`) calls `ensureServiceAccessIsAllowed` before
   reaching it, so the incidental `isServiceAccessAllowed` check inside the username provider that
   is now skipped was redundant on every path.

## Test

`DefaultCentralAuthenticationServiceTests.DefaultTests.verifyAttributeReleasePolicyIsEvaluatedOnceAgainstTheResolvedService`
registers a service whose access strategy *requires* an attribute that only its release policy
produces, and stubs that policy as a Mockito mock whose answer records the service of every
evaluation. A hand-written implementation was tried first and did not compile:
`RegisteredServiceAttributeReleasePolicy` also declares `getPrincipalAttributesRepository()`, which
nothing on this path calls, so mocking the interface is both shorter and immune to the interface
growing. `InMemoryServiceRegistry.save` stores the definition by reference, so the mock survives
registration. After clearing the recorder (ticket
granting and TGT creation evaluate the policy on paths that were not changed), one validation must
produce exactly one evaluation, against the service the ticket was issued for, with the released
attribute present on the assertion. The registered service keeps the standard
`PrincipalAttributeRegisteredServiceUsernameProvider("uid")` from `RegisteredServiceTestUtils`, so
the count of one covers both fixes; before fix 2 the same test would count two.

- [ ] Compile and run — **NOT RUN** (JDK 11 only on this machine, no Gradle cache).
      `./testcas.sh --category cas` plus the authentication and attributes categories, since
      `DefaultAuthenticationBuilder.of` is shared.

## Verified statically

- `DefaultAssertionBuilder.assemble()` only constructs an `ImmutableAssertion`; it does not
  evaluate the policy.
- `RegisteredServiceAccessStrategyAuditableEnforcer` does not evaluate the policy.
- The only remaining `getAttributeReleasePolicy().getAttributes(...)` calls in
  `DefaultCentralAuthenticationService` are on the `createTicketGrantingTicket` and
  `grantServiceTicket` paths, neither of which is reachable from `validateServiceTicket`.


---

# Fix: `/samlValidate` parses the SOAP body once per caller

## Is it worth fixing?

On throughput alone, no. A SAML 1.1 artifact request is a few hundred bytes and `/samlValidate` is
a low-volume legacy back-channel endpoint. The argument that carries it is determinism, not speed.

`SamlServiceFactory` is consulted several times while one request is handled — the callers found in
the tree are `RegisteredServiceResponseHeadersEnforcementFilter`, `CasLocaleChangeInterceptor`,
`RegisteredServiceCorsConfigurationSource`, `AbstractServiceValidateController.handleRequestInternal`
and `AbstractSaml10ResponseView.renderMergedOutputModel` — so the body is parsed three or four
times, and whichever caller runs first is the one that consumes `request.getReader()`. Every later
caller depends on `readRequestBodyIfAny` failing and falling through to the cached
`PARAMETER_SAML_REQUEST` attribute, with the failure swallowed by a trace-level catch. It works, but
by accident rather than design.

## Change

`SamlServiceFactory` now resolves the artifact and request identifiers once and keeps them on the
request next to the body, in a private `SamlRequestDetails` record. Behavior is unchanged: the same
identifiers come back either way, because the attribute fallback already made repeat parses produce
the same result. What changes is that the result no longer depends on the body still being readable.

- [x] `SamlServiceFactory.getSamlRequestDetails` caches per request; `createService` uses it
- [x] `SamlServiceFactoryTests.verifySoapBodyIsResolvedOncePerRequest` — resolves once with the body
      present, then removes it and asserts the identifiers survive. Written against the attribute
      rather than the servlet reader so it does not depend on `MockHttpServletRequest.getReader()`
      re-read semantics. On the previous code the second call resolves nulls and the test fails.
- [ ] Compile and run — **NOT RUN** (JDK 11 only on this machine, no Gradle cache).
      `./testcas.sh --category saml1` and `--category saml`, plus `ticket-validation-saml1`.

## Groovy / Scripting subsystem review (2026-09-12)

Scope: only the Groovy/scripting aspect of every feature that supports it. CRITICAL/HIGH bugs,
security and performance at system boundaries. Review only — no code changes, no tests, no RC2 note.

- [x] 1. Map the scripting core (`ExecutableCompiledScript(Factory)`, `ScriptResourceCacheManager`,
      `GroovyShellScript`, `WatchableGroovyScriptResource`, `ScriptingUtils`, `CasReentrantLock`).
- [x] 2. Enumerate consumers: 94 non-test classes across ~50 modules; inline `groovy {}` vs
      `file:/classpath:` resolution, cache-manager path vs ad-hoc `fromScript`/`fromResource`.
- [x] 3. Security at boundaries: script *sources* are admin-controlled everywhere (service definitions,
      properties); no attacker-controlled value reaches a script body. Real issues are null-result
      fail-open, binding bleed, and the `groovyCache` actuator.
- [x] 4. Concurrency/lifecycle: shared script singletons behind one 5s `tryLock`; static binding
      ThreadLocal; Caffeine removal listener closes scripts that callers still hold.
- [x] 5. Performance: global cache invalidation from OIDC claim mapping; per-request compiles in
      access-strategy evaluation, `ReturnAllowed`/`PatternMatching` release, surrogate access strategy,
      predicated MFA trigger, SAML groovy metadata resolver.
- [x] 6. Protocol compliance: no CRITICAL/HIGH defect attributable to the scripting layer. Scripted
      OIDC claims/collector write raw script output into `JwtClaims` with no type normalization
      (OIDC Core 1.0 errata 2 s5.1 types) — noted as lower severity only.
- [x] 7. Puppeteer cross-check: 18 `*groovy*` scenarios. No changes made, so none are affected.
- [x] 8. Findings reported; `CLAUDE.md` / `AGENTS.md` updated with a scripting notes section.

### Findings (CRITICAL)

1. `BaseOidcScopeAttributeReleasePolicy.mapClaimToAttribute` opens the shared `ScriptResourceCacheManager`
   singleton in try-with-resources; `close()` is `cache.invalidateAll()`. Every scripted claim, once per
   allowed claim per ID token / userinfo / introspection, wipes every compiled Groovy script server-wide
   and fires the removal listener that closes each script's `FileWatcherService`.
2. All script execution is serialized on `CasReentrantLock.tryLock()` (5s), which returns `null` with no
   exception or log on timeout. Cached scripts are shared singletons, so concurrent logins run one at a
   time; the null then fails *open* — `ScriptedRegisteredServiceMultifactorAuthenticationTrigger` returns
   `Optional.empty()` (MFA skipped) and `AcceptableUsagePolicyVerifyAction` maps null to
   `AcceptableUsagePolicyStatus.skipped()` -> `UNDEFINED` -> AUP skipped.
3. `GroovyShellScript.BINDING_THREAD_LOCAL` is `static` and only cleared inside the `if (lock.tryLock())`
   branch. A lock timeout (or a `setBinding` that never reaches `execute`) leaves one request's binding on
   the worker thread for the next inline script to inherit.

### Findings (HIGH)

4. Per-request compilation: `RegisteredServiceAccessStrategyEvaluator.requiredAttributeFound`,
   `ReturnAllowedAttributeReleasePolicy.executeInlineGroovyScript`, `PatternMatchingAttributeReleasePolicy`
   call `fromScript(...)` per attribute per request instead of the cache manager; `GroovyShellScript.close()`
   is a no-op, so each `GroovyShell.parse` leaves a class + loader behind.
5. `GroovySurrogateRegisteredServiceAccessStrategy.authorizeRequest` builds a new
   `WatchableGroovyScriptResource` per call — recompiles and starts an unclosed `FileWatcherService`
   thread. Same shape: `GroovyResourceMetadataResolver`,
   `PredicatedPrincipalAttributeMultifactorAuthenticationTrigger` (new `GroovyClassLoader` per login).
6. `POST /actuator/groovyCache/resources/validate` compiles caller-supplied Groovy; Groovy executes AST
   transforms at compile time (`@ASTTest`, `@Grab`) and `groovy.transform` is star-imported, so "validate"
   is arbitrary code execution. `GET /resources/{key}` returns external script file contents.
   `Access.NONE` by default.
7. `setFailOnError` is a no-op default that `GroovyShellScript` never overrides, and `GroovyShellScript`
   swallows `GroovyRuntimeException`. Callers passing `failOnError=true`
   (`GroovyRegisteredServiceAccessStrategyEnforcer`, `SamlProfileAuthnContextClassRefBuilder`) get `null`,
   which then NPEs on unboxing or reads as "no decision".
   Related: `GroovyRegisteredServiceAccessStrategyActivationCriteria.shouldActivate` unboxes a nullable
   `Boolean`; `GroovyScriptAttributeReleasePolicy.fetchAttributeValueFromScript` returns `null` from a
   `@NullMarked` non-null method.


## OID4VCI / OID4VP end-to-end review (2026-09-12)

Scope: review-only (no code changes) of `support/cas-server-support-oidc-vc` -- credential offer, pre-authorized
code, token, nonce, credential issuance, issuer/type metadata, and the presentation request/response endpoints --
for CRITICAL/HIGH protocol-compliance, security and performance defects at system boundaries, plus gaps that block
interop with known wallets.

- [x] Map the surface: endpoints, configs, services, metadata, encoders, presentation flow.
- [x] Check the current OID4VCI 1.0 and OID4VP 1.0 published texts for the boundaries under review.
- [x] Review issuance boundary: offer -> pre-auth code -> token -> nonce -> credential endpoint.
- [x] Review presentation boundary: request object, DCQL, response mode, SD-JWT/KB-JWT verification.
- [x] Review metadata boundaries: `/.well-known/openid-credential-issuer` and credential type metadata.
- [x] Summarize CRITICAL/HIGH findings and wallet-interop gaps (no changes made).
- [x] Cross-check affected puppeteer scenarios; update CLAUDE.md / AGENTS.md. No RC2 note: nothing changed.

### Findings (review only, nothing changed)

CRITICAL

1. [FIXED 2026-09-12, see below] Credentials expire five minutes after issuance.
   `BaseOidcVerifiableCredentialEncoder.CLAIM_VALIDITY_IN_MINUTES` is a hardcoded 5 and no lifetime property
   exists, so every issued credential is stale by the time a wallet stores it.
2. [FIXED 2026-09-12, see below] Issuance wire format is OID4VCI draft 13, not 1.0.
3. [FIXED 2026-09-12, see below] The presentation request object is served as raw JSON.
   `GET oidcVcPresentationRequest/{id}` returns a JSON body, but OID4VP 1.0 delivers a `request_uri` payload as a
   signed JWT with content type `application/oauth-authz-req+jwt`.
4. Credentials are signed by the ID-token service. `sign(...)` calls `idTokenSigningAndEncryptionService.encode`,
   so a client with `encryptIdToken` yields a JWE that is not an SD-JWT, `signIdToken=false` yields an unsigned
   credential, and the real algorithm is the client's `idTokenSigningAlg` rather than the advertised
   `credential_signing_alg_values_supported` -- which CAS's own verifier then rejects.

HIGH

5. No per-service authorization of credential configurations. The offer endpoint and
   `OidcVerifiableCredentialAuthorizationDetails.from` only check that a configuration id exists globally, so any
   registered client may obtain any credential type.
6. Pre-authorized code consumption is not atomic. `OidcVerifiableCredentialsAccessTokenGeneratorCustomizer` calls
   `updatePreAuthorizationCode` after the token is minted, outside any lock, so concurrent redemptions of one code
   each yield an access token. Same read-then-delete race in `OidcVerifiableCredentialDefaultNonceService.consume`.
7. The authorization-code VC flow silently no-ops for confidential clients.
   `OidcVerifiableCredentialAuthorizationCodeAuthorizationResponseBuilder.supports` requires a blank client secret
   plus PKCE; otherwise `authorization_details` is dropped and the credential endpoint later rejects the token.
8. CAS can only verify credentials it issued itself. The presentation endpoint pins `iss` to the local issuer,
   resolves `vct` against local configurations and takes the signing key from a locally registered service; there
   is no issuer trust list, `x5c`, DID or federation path, and `status` is rejected outright.
9. The verification result never reaches the relying party. The response endpoint validates, deletes the ticket and
   answers the wallet `{"status":"verified"}`; the caller that created the request cannot poll for the outcome or
   the disclosed claims.
10. Protected-resource error semantics. The credential endpoint answers 400 with `invalid_request`/`error` for an
    invalid or expired token instead of 401 with `WWW-Authenticate`, never emits `invalid_proof`/`invalid_nonce`,
    and `getAccessTokenFromRequest` still accepts the token as an `access_token`/`token` query parameter.
11. Performance: `encode` re-runs `principalResolver.resolve` for every credential in a batch, on top of the
    resolution already done at token time, and every nonce is a ticket-registry write with no reuse.

Wallet-interop gaps: no `proofs`/key attestation, no `credential_identifier`, no deferred or notification endpoint,
no credential response encryption, no signed metadata, no issuer-level `display`, no status list, no DPoP or wallet
attestation, no mdoc/ISO 18013-5, no `direct_post.jwt` encrypted responses, no `transaction_data`, and all VC state
shares the single global `cas.ticket.tst` five-minute TTL. The presentation flow is undocumented in
`OIDC-Authentication-Verifiable-Credentials.md`.

Puppeteer coverage: `oidc-verifiable-credentials`, `oidc-verifiable-credentials-authz-code` and
`oidc-verifiable-credentials-waltid` (walt.id 0.23.2, issuance plus presentation). Nothing changed, so no scenario
needed rerunning; note that walt.id's leniency is why findings 2 and 3 pass today.

### Fixes applied (2026-09-12) — CRITICAL items 1-3 only

- [x] 1. `BaseOidcScopeAttributeReleasePolicy.mapClaimToAttribute` no longer opens the shared
      `ScriptResourceCacheManager` in try-with-resources. Verified as the only site in the tree that
      closed the cache manager. (Already applied in the working tree before this pass.)
- [x] 2. `WatchableGroovyScriptResource.execute` and `GroovyShellScript.execute` acquire the script lock
      with the blocking `CasReentrantLock.executeAndThrow` / `execute` instead of the 5-second `tryLock`,
      so a queued execution can no longer return `null` and be read as "skip MFA" / "skip AUP".
      New `CasReentrantLock.executeAndThrow(CheckedSupplier)` mirrors `tryLock(CheckedSupplier)` without
      the timeout; `tryLock` itself is unchanged for its other callers.
      Tradeoff: a script that never returns now blocks its callers instead of silently failing open, and
      two scripts invoked in opposite orders from each other could deadlock.
- [x] 3. `GroovyShellScript` binding is a per-instance ThreadLocal that `execute` reads and removes before
      taking the lock, so it is scoped to one thread and one execution. The old field was `static` and was
      cleared only on the lock-acquired path, so one request's binding could reach another script.
- [x] Latent bug surfaced by the new binding tests and fixed: the `finally` in `GroovyShellScript.execute`
      reset the shared script with `new Binding(Map.of())`, an immutable map. The next execution's
      `binding.setVariable("logger", ...)` in `ScriptingUtils.executeGroovyShellScript` then threw
      `UnsupportedOperationException`, which that method swallows, so the call returned `null`. Reachable
      whenever a caller executes a cached inline script without calling `setBinding` first —
      `ScriptedRegisteredServiceMultifactorAuthenticationTrigger` is exactly that shape, so an inline
      `groovy { ... }` MFA policy script worked on the first login and silently skipped MFA on every login
      after. Fixed by resetting with the no-arg `new Binding()`, whose variables map is a mutable
      `LinkedHashMap` created on demand. Covered by
      `GroovyShellScriptTests.BindingTests.verifyRepeatedExecutionWithoutABinding`.
- [x] Tests: `GroovyShellScriptTests.BindingTests` (single-consumption, cross-script isolation, 64 barrier-
      released virtual threads on a shared script each asserting its own value and all values distinct,
      200 tasks over a 4-thread pool interleaved with attempts that bind and abort);
      `WatchableGroovyScriptResourceTests.verifyQueuedExecutionIsNotDroppedWhenScriptIsSlow`;
      `CasReentrantLockTests` checked-execution tests; `OidcCustomScopeAttributeReleasePolicyTests
      .verifyGroovyMappingRetainsSharedScriptCache`.
- [x] Docs: `Apache-Groovy-Scripting.md` gains a "Script Execution" section; RC2 release notes gain a
      "Groovy Scripting" section; `CLAUDE.md` / `AGENTS.md` scripting notes corrected to the new behavior.
- [x] Puppeteer: the 18 `*groovy*` scenarios plus `oidc-authzcode-login-customscope-relpolicy` (the only
      scenario with an inline Groovy claim mapping, and so the only one on the item-1 path) are all
      single-user sequential flows with no lock contention and no script file rewritten mid-run, so none
      of them change behavior. Not run.
- [ ] Not run: Gradle build, tests, Puppeteer. The linked machine has only JDK 11; this tree needs JDK 25.
      Run `./testcas.sh --test GroovyShellScriptTests`, `--test WatchableGroovyScriptResourceTests`,
      `--test CasReentrantLockTests`, `--test OidcCustomScopeAttributeReleasePolicyTests`, then
      `oidc-authzcode-login-customscope-relpolicy` and `service-access-strategy-groovy`.

### Fixes applied (2026-09-12) — HIGH items 4 and 5

- [x] 4. Inline scripts on request paths now resolve through `ScriptResourceCacheManager` instead of
      `fromScript(...)` per call: `ReturnAllowedAttributeReleasePolicy.executeInlineGroovyScript`,
      `PatternMatchingAttributeReleasePolicy.buildAttributesForScriptedEntry` (resolution also hoisted out
      of the per-value loop) and `RegisteredServiceAccessStrategyEvaluator.requiredAttributeFound`.
      Each falls back to `fromScript(...)` when no cache manager is in the application context, so
      components and tests constructed outside Spring (for example `DefaultRegisteredServiceAccessStrategyTests`,
      which boots only `RefreshAutoConfiguration`) keep working.
- [x] 5a. `GroovySurrogateRegisteredServiceAccessStrategy` holds its script in a `@JsonIgnore @Transient
      transient` field built once, mirroring `GroovyRegisteredServiceAccessStrategy`, instead of compiling
      and registering a new unclosed `FileWatcherService` on every authorization request. The result is
      also read through `Boolean.TRUE.equals(...)` rather than unboxed.
- [x] 5b. `GroovyResourceMetadataResolver.resolve` resolves its script through the cache manager
      (falling back to `fromResource`), so SAML2 metadata resolution no longer compiles and watches per call.
- [x] 5c. `ScriptingUtils.getObjectInstanceFromGroovyResource` caches the compiled class per resource URI,
      keyed by the resource's last-modified time so an edited script is still picked up. This is the path
      behind `PredicatedPrincipalAttributeMultifactorAuthenticationTrigger`, which compiled a Groovy class
      on every authentication.
- [x] ErrorProne `ThreadLocalUsage` on the item-3 fix: the binding ThreadLocal is static again, but its
      value is an owner-scoped `ScriptBinding(instanceId, variables)` record, so a binding is still consumed
      only by the script that assigned it and is removed on every execution. A binding left behind by an
      aborted attempt is now discarded when any script executes next, rather than inherited.
- [x] Tests: `PatternMatchingAttributeReleasePolicyTests.verifyGroovyTransformationRuleIsCompiledOnce`
      (two attribute values, one cache entry), `ScriptingUtilsTests.verifyObjectClassIsCompiledOnceAndRefreshedOnChange`
      (same class reused, recompiled after the file changes),
      `GroovySurrogateRegisteredServiceAccessStrategyTests.verifyScriptIsBuiltOnceAcrossRequests`.
- [x] Docs: RC2 release notes extended; `CLAUDE.md` / `AGENTS.md` record the cache-with-fallback pattern,
      the transient-field pattern for service definitions, and the mtime-keyed class cache.
- [x] Puppeteer: `service-access-strategy-groovy`, `service-access-strategy-global-groovy`,
      `surrogate-login-groovy`, `saml2-idp-login-sp-metadata-groovy` and
      `mfa-provider-selection-trigger-groovy` cover the touched paths. Behavior is unchanged (same scripts,
      same results, fewer compilations), so none of them need edits. Not run.

### Fixes applied (2026-09-12) — HIGH item 6

Demonstrated first, then fixed. `GroovyScriptCacheManagerEndpointTests.verifyValidationDoesNotRunCompileTimeCode`
posts a body whose only payload is `@ASTTest(value = { new File('<tmp>').write('executed') })` and asserts the
marker file is absent afterwards. Against the unfixed endpoint the file is written: `validate` called
`fromScript(...).compileScript()` -> `GroovyShell.parse`, and Groovy applies AST transformations inside the
compiler, so the closure runs while `parse` is on the stack. Nothing ever calls `run()`, which is exactly why
the operation reads as safe. `groovy.transform` and `groovy.lang` are both star-imported by
`createCompilerConfiguration`, so `@ASTTest` and `@Grab` need no qualification.

- [x] `ScriptingUtils.validateGroovyScript(String)` drives a bare `SourceUnit`: `create`, `parse`,
      `completePhase`, `convert`, then check the error collector. No `CompilationUnit`, so no phase
      operations and therefore no transformations: global ones are registered as a compilation unit's phase
      operations, and local ones require a phase no earlier than semantic analysis. A `CompilationUnit`
      stopped at `Phases.CONVERSION` would NOT have been safe, since Grape's `GrabAnnotationTransformation`
      is global and runs at that very phase.
- [x] Corrected after a failing run: the first attempt stopped at `parse()` and reported nothing for
      `println('Hello`, because the Parrot parser plugin only constructs the AST builder in `parseCST` and
      does the real work in `convert`. `convert` in turn needs `completePhase` first or it throws a
      `GroovyBugError`. Covered by the existing `verifyCompilation`.
- [x] The security test is self-proving rather than vacuous: after asserting the endpoint left no marker
      file, it compiles the very same payload through `fromScript(...).compileScript()` and asserts the
      marker does appear. If `@ASTTest` were not a live vector in this Groovy build, that second assertion
      fails and says so, instead of the first one passing for the wrong reason.
- [x] `GroovyScriptCacheManagerEndpoint.validate` calls it instead of `compileScript()`. Existing
      `verifyCompilation` still holds: a valid body is 200, `println('Hello` is 400.
- [x] Behavior change to document: `validate` is now a syntax check. It no longer reports unresolved
      classes, because resolving them means running the transformation machinery. Documented in
      `Apache-Groovy-Scripting.md` and the RC2 notes.
- [x] Also documented: CAS REST actuator endpoints use Spring MVC mappings rather than
      `@ReadOperation`/`@WriteOperation`, so `Access.READ_ONLY` does not gate their `POST`/`DELETE`
      operations. `groovyCache` must be treated as administrative.

Dead code left in place (not deleted, per the repo's guidance): `ExecutableCompiledScript.compileScript()`
and its two implementations now have no callers.

### Fixes applied (2026-09-12) — HIGH item 7

- [x] `GroovyShellScript` honors `failOnError`: a real field with a Lombok `@Setter` plus class-level
      `@Accessors(chain = true)`, so the setter covariantly overrides the interface method exactly as
      `WatchableGroovyScriptResource` does. `execute(args, clazz, failOnError)` rethrows the
      `GroovyRuntimeException` when asked to, and the 1-arg and 2-arg forms delegate to the field rather
      than the hard-coded `true` that was being ignored.
- [x] `ScriptingUtils.executeGroovyShellScript` takes `failOnError` and rethrows instead of swallowing
      every `Exception`. Without this the fix above would be cosmetic, since a failure raised inside the
      script body was swallowed one level further down. Both public overloads changed signature; the only
      production caller was `GroovyShellScript`, and three test call sites were updated.
- [x] Deliberate decision on the default: `GroovyShellScript.failOnError` defaults to **false**, unlike
      `WatchableGroovyScriptResource` which defaults to true. Flipping it changes every inline-script caller
      using the 1-arg or 2-arg `execute`, and at least two features are built on the lenient behavior and
      assert it — `ReturnMappedAttributeReleasePolicyTests.verifyInlinedGroovyFailsPartially` (a bad script
      among several mapped attributes must not stop the others from being released) and
      `GroovyRegisteredServiceUsernameProviderTests.verifyUsernameProviderInlineWithoutAttribute` (a script
      that throws falls back to the principal id). Aligning the two defaults is a separate decision: it
      needs those two call sites to pass `false` explicitly, plus an audit of roughly twenty other
      inline-script callers, several of them on the login path.
- [x] `GroovyRegisteredServiceAccessStrategyActivationCriteria.shouldActivate` no longer unboxes a nullable
      `Boolean`; it throws, naming the script. Mapping the null to `false` would have been worse than the
      NPE: `RegisteredServiceAccessStrategyActivationCriteria.isAllowIfInactive` defaults to `true`, so
      `DefaultRegisteredServiceAccessStrategy.authorizeRequest` would have granted access without evaluating
      required attributes.
- [x] `GroovyScriptAttributeReleasePolicy.fetchAttributeValueFromScript` returns an empty map rather than
      `null` from a method declared non-null in a `@NullMarked` package.
- [x] Tests: `GroovyShellScriptTests.verifyBadScriptIsReportedWhenAskedTo` (an explicit `true` throws rather
      than yielding null) and `verifyFailOnErrorIsHonoredWhenAssigned` (`setFailOnError(true)` is honored
      for a failure raised inside the script body); `verifyUnknownBadScript` keeps asserting the lenient
      default.
- [x] Docs: `setFailOnError` interface javadoc, RC2 notes, and `CLAUDE.md` records why the two defaults
      differ and which features depend on the lenient one.

### Still open

Lower severity on the `groovyCache` endpoint: `GET /resources/{key}` returns the contents of cached
external script files. Also noted but unreported: the file watcher in
`WatchableGroovyScriptResource.compileScriptResource` swaps `compiledScript` without holding the lock.


## Credential validity is configurable (2026-09-12)

Fix for finding #1 of the OID4VCI/OID4VP review: issued credentials expired five minutes after issuance.

- [x] Add `credential-validity` to `OidcVerifiableCredentialConfigurationProperties` (`@DurationCapable`, default `P30D`).
- [x] Derive `exp` from the `iat` set in `BaseOidcVerifiableCredentialEncoder.sign` so the two are exactly the
      configured duration apart; back the `nbf` skew with `cas.authn.oidc.core.skew`, matching
      `OidcIdTokenGeneratorService`, instead of reusing the validity constant for two unrelated purposes.
- [x] Have `OidcVerifiableCredentialJwtVcJsonLdEncoder` read `validFrom`/`validUntil` off those claims rather than
      taking its own timestamp, so the JSON-LD dates and the JWT dates cannot drift apart.
- [x] Cover with MockMvc tests in `OidcVerifiableCredentialEndpointControllerTests.CredentialValidityTests`:
      default validity, an explicitly configured `P7D` configuration, and `validUntil` tracking `exp` for JSON-LD.
- [x] Document the setting, add an RC2 note, and assert the issued lifetime in the
      `oidc-verifiable-credentials` Puppeteer scenario.

Scope: credential lifetime only. The exchange-state TTLs (offer, pre-authorized code, nonce, presentation request)
still share the global `cas.ticket.tst` policy and were deliberately left alone.

Validation: NOT RUN. The Gradle wrapper cannot download its distribution in this environment and the available JDK
is 11 against a Java 25 source tree, so neither compilation nor the new tests were executed. Verification was
static: the diff was reviewed, and the Puppeteer script passed `node --check` (ESLint is not installed here).

Puppeteer cross-check: `oidc-verifiable-credentials` (assertion added), `oidc-verifiable-credentials-authz-code`
and `oidc-verifiable-credentials-waltid` decode the issuer JWT but assert only subject claims, so all three remain
correct; the longer lifetime can only make the walt.id store-then-present scenario more reliable, not less.


## Presentation requests are delivered as OpenID4VP requires (2026-09-12)

Fix for finding #3. The defect was larger than "the body is JSON": CAS combined the `redirect_uri` client
identifier prefix with a `request_uri`, and OID4VP 1.0 does not permit that pairing at all.

Spec basis, quoted from OpenID for Verifiable Presentations 1.0:
- "The Request URI response MUST be an HTTP response with the content type `application/oauth-authz-req+jwt`
  and the body being a signed, optionally encrypted, request object as defined in [@RFC9101]."
- "Requests using the `redirect_uri` Client Identifier Prefix cannot be signed because there is no method for the
  Wallet to obtain a trusted key for verification. Therefore, implementations requiring signed requests cannot use
  the `redirect_uri` Client Identifier Prefix."
- "Verifiers MUST include the `typ` Header Parameter in Request Objects with the value `oauth-authz-req+jwt` ...
  Wallets MUST NOT process Request Objects where the `typ` Header Parameter is not present or does not have the
  value `oauth-authz-req+jwt`."
- The `aud` claim "MUST be `https://self-issued.me/v2`, when Static Discovery metadata is used", and the spec notes
  that value "is a symbolic string and can be used as an `aud` claim value even when this specification is used
  standalone, without SIOPv2".
- The spec's own unsigned `redirect_uri` example is a plain redirect carrying every parameter as query parameters.

- [x] Add `cas.authn.oidc.vc.presentation.client-identifier-prefix` (`REDIRECT_URI` default, `X509_SAN_DNS`).
- [x] `REDIRECT_URI`: build the entire authorization request into the `openid4vp://authorize` deep link by value,
      return no `request_uri`, and have the fetch endpoint answer 404 because there is nothing legal to serve.
- [x] `X509_SAN_DNS`: sign the request object with the CAS OpenID Connect signing key, headers `typ`,
      `kid` and `x5c`, claims `iss` = client identifier, `aud` = `https://self-issued.me/v2`, `exp` from the
      transaction, and serve it as `application/oauth-authz-req+jwt` with `Cache-Control: no-store`.
- [x] Refuse to sign when the signing key carries no certificate chain, or when no `dNSName` subject alternative
      name in the leaf certificate matches the client identifier: a configuration error beats an object every
      wallet rejects without saying why.
- [x] Resolve the verifier client identifier in one place and have the response endpoint derive the expected key
      binding audience from it, so the two sides cannot drift.
- [x] Encode deep-link parameters with `URLEncoder` rather than a `UriComponentsBuilder` template, because
      `dcql_query` and `client_metadata` are JSON and a URI template would consume their braces.
- [x] Add a controller `@ExceptionHandler`, matching the other verifiable-credential controllers, so a
      configuration failure surfaces as a 400 rather than a 500.
- [x] Rework the MockMvc tests into unsigned and signed nested classes; update the walt.id Puppeteer scenario;
      document both modes and add an RC2 note.

Scope: request delivery only. Response encryption (`direct_post.jwt`) was deliberately left out and remains an
open item, as does support for the `openid_federation`, `decentralized_identifier` and `verifier_attestation`
prefixes.

Validation: NOT RUN here. The Gradle wrapper cannot download its distribution in this environment and the
available JDK is 11 against a Java 25 source tree. The Puppeteer script passed `node --check`; the rest was
reviewed statically.

Open risk worth a real run: the signed happy path has no test. Exercising it needs an OpenID Connect keystore
whose signing key carries an `x5c` chain with a matching `dNSName`, which the generated test keystore does not
have; the test only asserts that CAS refuses to serve an unsigned object in that mode. The walt.id scenario now
covers the by-value shape, but walt.id previously consumed a `request_uri`, so that scenario genuinely needs to be
run rather than reasoned about.


## Issuance speaks OpenID4VCI 1.0 (2026-09-12)

Fix for finding #2, at full alignment.

Spec basis, from OpenID for Verifiable Credential Issuance 1.0:
- Credential request: `credential_identifier` is "REQUIRED when an Authorization Details of type
  `openid_credential` was returned from the Token Response"; `credential_configuration_id` is "REQUIRED if a
  `credential_identifiers` parameter was not returned"; `proofs` is an "Object providing one or more proof of
  possessions". No `format`, no singular `proof`.
- Credential response: `credentials` is an array of objects each holding a `credential`, and "The number of
  elements in the `credentials` array matches the number of keys that the Wallet has provided via the `proofs`
  parameter". No `format`, no `c_nonce`.
- There is no batch credential endpoint; batching is several proofs on the credential endpoint, advertised as
  `batch_credential_issuance`.
- The nonce endpoint example returns `c_nonce` alone, with `Cache-Control: no-store`.

- [x] Credential request: `proofs.jwt[]`, `credential_identifier`, `credential_configuration_id`; drop `format`,
      the singular `proof` and the unused `credential_metadata`.
- [x] Credential response: `credentials` array of `{credential}`; drop `format`.
- [x] Issue one credential per proof, of the one configuration the request names; cap the count with the
      existing `cas.authn.oidc.vc.issuer.batch-size` and advertise it as `batch_credential_issuance`.
- [x] Delete the batch endpoint, its request record, its mapping and `OidcConstants.VC_BATCH_CREDENTIAL_URL`.
- [x] Reject a request that carries both identifiers, or a `credential_identifier` on a token with no
      authorization details.
- [x] Return `credential_identifiers` inside the authorization details so a wallet knows to use
      `credential_identifier`; CAS issues the credential configuration id as the identifier.
- [x] Drop `c_nonce`/`c_nonce_expires_in` from the token response, and `c_nonce_expires_in` from the nonce
      endpoint, which now sends `Cache-Control: no-store`.
- [x] Take the proof validator down to a single proof JWT so the plural case is a plain loop.
- [x] Update the MockMvc tests, the three Puppeteer scenarios, the documentation and the RC2 note.

### The wallet in CI

walt.id ships two generations. The original Wallet API is draft-13: its `waltid-openid4vc` library models a
required `format`, a singular `proof` and a singular `credential`, so it cannot consume a 1.0 response, and no
version bump of that image changes it. Issuer2, Verifier2 and Wallet API v2 implement the finalized 1.0 specs,
and `waltid/wallet-api2:1.0.0` is published.

`oidc-verifiable-credentials-waltid` now runs against `waltid/wallet-api2:1.0.0` on port 7006. Authentication is
off and persistence is SQLite in the shipped configuration, so the scenario no longer needs Postgres, Caddy, the
dev-wallet front end, the account bootstrap container or a browser at all; it creates a wallet with `POST /wallet`
and drives `POST /wallet/{id}/credentials/receive` and `POST /wallet/{id}/credentials/present`.

EUDI was considered and rejected for this role: the reference wallet is Android/iOS, and the EU's server-side
repositories are an issuer and a verifier, not a wallet. `eudi-lib-jvm-openid4vci-kt` is a client library that
would need its own harness.

Validation: NOT RUN here. Gradle cannot download its distribution in this environment and the available JDK is 11
against a Java 25 source tree. The three Puppeteer scripts pass `node --check` and the compose file and shell
script pass syntax checks; everything else was reviewed statically.

Fixed on a real run (2026-09-12): `CredentialValidityTests.issueCredentialClaims` still read the response body as
`get("credential")`. The sweep that moved the assertions to the 1.0 shape matched `jsonPath` expressions only, so
a body read through the object mapper survived with the draft-13 shape and produced an NPE. The same test class
also asserted `OAuth20Constants.ERROR` for an unparseable bearer token, where the access-token extraction throws
and the controller's exception handler reports `invalid_request`. Both corrected. When changing a wire format,
grep for every way a response body is read, not just the assertion style used most often.

Open risks worth a real run:
- The v2 wallet compose mounts no configuration and relies on the shipped defaults. If the image requires
  `publicBaseUrl` in `wallet-service.conf`, a config directory has to be supplied.
- The v2 health check uses `GET /wallet`, and the container is assumed to carry `wget`.
- `GET /wallet/{id}/credentials` returns credential metadata; the scenario asserts on that rather than on the
  raw credential document, whose single-credential endpoint was not confirmed.
- `ci/tests/waltid/Caddyfile`, `init-wallet.sh` and `wallet-api/config/` belong to the v1 stack and are now
  unused. They were left in place because this environment cannot delete files.
