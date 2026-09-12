# CLAUDE.md

This repository is **Apereo CAS** (Central Authentication Service), a large-scale enterprise authentication server built with Java, Gradle, and Spring Boot. This file provides guidance for Claude Code and Claude AI assistants so that changes remain consistent with CAS project practices and reviewer expectations.

> **Note**: If you are trying to *deploy/configure* CAS, you should **not** be editing this repository directly; use the **WAR Overlay** approach instead. Building from source is for contributors only.

---

## Project Overview

- **Type**: Multi-module Gradle project (500+ modules)
- **Language**: Java 25+
- **Frameworks**: Spring Boot 4.x, Spring Cloud, Spring Webflow
- **Build Tool**: Gradle 9.x with parallel builds and configuration cache enabled
- **Architecture**: Modular design — API → Core → Support → Webapp layers

### Module Organization

```
api/           → Interface definitions and contracts
core/          → Core implementations of API contracts
support/       → Feature modules (LDAP, OIDC, SAML, Duo, etc.)
webapp/        → Web application modules (Tomcat, Jetty, etc.)
docs/          → User-facing documentation
ci/            → CI scripts and test helpers
style/         → Checkstyle, SpotBugs, ErrorProne configs
```

**Key principle**: `api/` modules define contracts, `core/` implements them, `support/` adds features. Always respect module boundaries and avoid circular dependencies.

---

## Goals for AI-Assisted Changes

When generating or modifying code in this project, optimize for:

- **Small, reviewable diffs** — keep changes focused and minimal
- **Consistency with existing patterns** — follow the conventions already in the codebase
- **Tests + docs** — every bug fix needs a test; user-facing changes need documentation
- **Build correctness** — respect Gradle module boundaries and dependency scopes
- **Security correctness** — authentication, authorization, and crypto must not be weakened

---

## Build & Run

### Full Build (Skip Tests)
```bash
./gradlew build --parallel -x test -x javadoc -x check
```

### Build Specific Module
```bash
./gradlew :core:cas-server-core-authentication:build
```

### Run Locally
```bash
cd webapp/cas-server-webapp-tomcat
../../gradlew bootRun
```
Access at: `https://localhost:8443/cas`

### Clean Build
```bash
./gradlew clean build --no-build-cache
```

---

## Testing

### Test Framework
Use `./testcas.sh` for comprehensive testing:

```bash
# See available test categories
./gradlew -q testCategories

# Run specific test category
./testcas.sh --category CategoryName

# Run specific test class
./testcas.sh --test TestClassName

# Run with coverage
./testcas.sh --category CategoryName --with-coverage

# Debug mode (port 5005)
./testcas.sh --category CategoryName --debug
```

### Run Single Test (Direct Gradle)
```bash
./gradlew :core:cas-server-core-authentication:test --tests "*AuthenticationHandlerTests"
./gradlew :support:cas-server-support-ldap:test --tests "*LdapAuthenticationHandlerTests.verifySuccess"
```

### Test Conventions
- Every bug fix needs a test
- New features require comprehensive test coverage
- Tests belong in the same module as the code they test
- Use `@SpringBootTest` for integration tests
- Use `@Nested` for organizing related test cases

---

## Code Conventions

### Java Style
- **Java version**: Java 25+ (use modern features: records, pattern matching, switch expressions, sealed classes)
- **Indentation**: 4 spaces, NO tabs
- **Braces**: Always use braces, even for single-line blocks
- **Null safety**: Use `@NullMarked` at package level (JSpecify annotations)
- **Conditionals**: Avoid needless `else` statements
- **Imports**: No unused imports (enforced by Checkstyle)
- **Line length**: 200 characters max

### Lombok Usage
Lombok is heavily used throughout the codebase:
- `@Getter` / `@Setter` for bean properties
- `@RequiredArgsConstructor` for dependency injection
- `@Slf4j` for logging (field name: `LOGGER`, static)
- `@ToString` / `@EqualsAndHashCode` with `doNotUseGetters = true`
- **Avoid** `@Data` (too implicit)

### Spring Configuration Patterns
- Use `@AutoConfiguration` or `@Configuration` for config classes
- Always use `@ConditionalOnFeatureEnabled` for feature toggles
- Use `@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)` for runtime-refreshable beans
- Use `@ConditionalOnMissingBean` to allow overrides
- Order beans with `@Order` or implement `Ordered`

### Bean Registration Example
```java
@Bean
@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
@ConditionalOnMissingBean(name = "myService")
public MyService myService(final CasConfigurationProperties casProperties) {
    return new DefaultMyService(casProperties);
}
```

### Package Structure
```java
@NullMarked
package org.apereo.cas.authentication;

import org.jspecify.annotations.NullMarked;
```

### Module Dependencies
- `api/` modules: Define interfaces only, minimal dependencies
- `core/` modules: Depend on corresponding `api/` modules
- `support/` modules: Can depend on `core/` and other `support/` modules
- Use `api` vs `implementation` dependency scopes appropriately

---

## Quality Checks

- **Checkstyle**: Enforced via `style/checkstyle-rules.xml` (line length: 200 chars)
- **SpotBugs**: Static analysis via `style/spotbugs-excludes.xml`
- **ErrorProne**: Enabled by default (skip with `-DskipErrorProneCompiler=true`)
- **NullAway**: Null safety analysis (skip with `-DskipNullAway=true`)

---

## Documentation

- Update `docs/cas-server-documentation/` for any user-facing change
- Public APIs require Javadoc with `@since` version tags
- Configuration properties need `@RequiresModule` annotation

---

## Security Guidance

**CRITICAL**: Do not blindly accept or generate changes in these areas without careful review and testing:
- Authentication flows (login, logout, SSO)
- Authorization and access control
- Ticket validation and issuance
- Cryptographic operations
- MFA workflows
- Session management
- Input validation and sanitization

### Security Rules
- Never weaken existing security constraints
- Always validate user input
- Use CAS-provided crypto utilities (don't roll your own)
- Test security changes with both positive and negative cases
- Follow the principle of least privilege

---

## What Claude Should NOT Do

- ❌ Add dependencies without justification
- ❌ Reformat unrelated code
- ❌ Skip tests or quality checks
- ❌ Replace CAS-specific patterns with generic alternatives
- ❌ Make broad refactorings without discussion
- ❌ Modify authentication/authorization without thorough testing
- ❌ Generate code that weakens security posture

---

## PR Checklist

- ✅ Builds locally without errors
- ✅ Tests pass (or new tests added)
- ✅ Checkstyle/SpotBugs/ErrorProne clean
- ✅ Documentation updated for user-facing changes
- ✅ Scope is focused and reviewable
- ✅ No unused imports or trailing whitespace
- ✅ Security implications reviewed

---

## Useful Gradle Tasks

```bash
./gradlew tasks                                                    # List all tasks
./gradlew -q testCategories                                        # Show test categories
./gradlew :core:cas-server-core-authentication:dependencies        # Module dependencies
./gradlew dependencyUpdates                                        # Check for updates
./gradlew javadoc                                                  # Generate Javadoc
```

---

## Additional Resources

- [Contributor Guidelines](https://apereo.github.io/cas/developer/Contributor-Guidelines.html)
- [Build Process](https://apereo.github.io/cas/developer/Build-Process.html)
- [Documentation](https://apereo.github.io/cas/development)
- [Architecture](https://apereo.github.io/cas/development/planning/Architecture.html)



## Behavioral guidelines 


### 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

### 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

### 3. Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

The test: Every changed line should trace directly to the user's request.

### 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.

---

## OIDC Verifiable Credentials Notes

- The OID4VCI Nonce Endpoint is public by specification; never place it behind client authentication.
- Secrets that gate a flow must be independent of values the caller already possesses, and single-use state must be consumed atomically.
- Check protocol behavior against the current published specification, not from memory.
- Surface: `support/cas-server-support-oidc-vc` only. Issuance is offer -> `OidcVerifiableCredentialOfferEndpointController`
  -> pre-authorized code -> `AccessTokenPreAuthorizedCodeGrantRequestExtractor` -> `OidcVerifiableCredentialEndpointController`
  -> `OidcDefaultVerifiableCredentialIssuerService` -> an `OidcVerifiableCredentialEncoder`. Presentation is
  `org.apereo.cas.vc.presentation` (note the different package root), two controllers and no service layer.
- The wire format currently implemented is OID4VCI draft 13, not 1.0: `{format, credential}` responses,
  a separate `batch_credential_endpoint`, singular `proof`, no `credential_identifier`. 1.0 uses
  `{"credentials":[{"credential":...}]}`, folds batching into the credential endpoint via `proofs`, advertises
  `batch_credential_issuance`, and drops `format` everywhere. Do not assume a change here is cosmetic; the
  walt.id puppeteer wallet is lenient enough to pass with either shape, so it will not catch a regression.
- OID4VP 1.0 serves a `request_uri` payload as a JWT (`application/oauth-authz-req+jwt`).
  `OidcVerifiableCredentialPresentationRequestEndpointController.fetchRequest` returns raw JSON instead. The
  `redirect_uri` client identifier prefix forbids signing, so the JWT is unsigned -- that is still a JWT.
- Credentials are signed through `IdTokenSigningAndEncryptionService`, which means a registered service's
  `signIdToken`, `encryptIdToken` and `idTokenSigningAlg` silently decide how credentials are signed. An
  `encryptIdToken` client gets a JWE that is not an SD-JWT, and the effective algorithm may not be one of the
  advertised `credential_signing_alg_values_supported`. Treat credential signing as its own concern.
  Note that `AbstractOidcTests.getOidcRegisteredService(clientId)` enables ID token encryption, so any test that
  needs to parse an issued credential must use the four-argument overload with `encrypt` set to false.
- Credential validity comes from `credential-validity` on each credential configuration (default `P30D`) and is
  applied in `BaseOidcVerifiableCredentialEncoder.sign`, which derives `exp` from the `iat` it sets so the two are
  exactly the configured duration apart; the JSON-LD encoder reads `validFrom`/`validUntil` back off those claims
  rather than taking its own timestamp. Keep that relationship if you touch either. Separately, every piece of VC
  *exchange* state -- offer transaction, pre-authorized code, nonce, presentation request -- is a
  `TransientSessionTicket` sharing the global `cas.ticket.tst` five-minute TTL and single use, with no VC-specific
  expiration policy. Credential lifetime and exchange lifetime are different concerns; do not conflate them.
- Which credential configurations a client may obtain is checked only against the global
  `cas.authn.oidc.vc.issuer.credential-configurations` map, never against the registered service. Any authorization
  change here should start from `OidcVerifiableCredentialAuthorizationDetails.from` and the offer controller.
- `OidcVerifiableCredentialAuthorizationCodeAuthorizationResponseBuilder.supports` engages only for a public client
  with PKCE. For every other client `authorization_details` is dropped without an error and the failure only shows
  up later at the credential endpoint.
- As a verifier, CAS trusts only credentials it issued: `iss` must equal the local issuer, `vct` must map to a local
  credential configuration, and the signing key comes from a locally registered service. There is no external issuer
  trust list, `x5c`, DID or federation path, and a `status` claim is rejected rather than checked.
- The presentation response endpoint tells the wallet `{"status":"verified"}` and deletes the transaction. Nothing
  returns the outcome or the disclosed claims to the relying party that created the request.
- The credential endpoint is a protected resource: it should answer 401 with `WWW-Authenticate` for a bad token and
  use `invalid_proof` / `invalid_nonce`. It currently answers 400 with `invalid_request`, and
  `getAccessTokenFromRequest` accepts the token from a query parameter.
- Performance: each encoder re-runs `principalResolver.resolve` per credential, so a batch multiplies
  person-directory lookups, and every nonce is a ticket-registry write.


## LDAP Subsystem Notes

- LDAP settings are shared: `AbstractLdapProperties` / `AbstractLdapSearchProperties` back every LDAP-capable
  feature, so a defect in `LdapUtils` or `LdapConnectionFactory` is a defect in every LDAP module at once.
  Review those two classes first, and treat any change there as touching authentication.
- `pageSize` defaults to `0` (paging off) and result-code checks are entry-presence checks. Reviewing an LDAP
  search means asking what happens when the directory returns a partial or non-success result, not only when
  it returns the expected entry.
- Build `ConnectionFactory` instances once, as beans, and inject them. Constructing one inside a per-request
  method creates and tears down a connection pool on every call.
- Write operations return a boolean and swallow exceptions. A caller that persists state must inspect that
  result; silently returning success on a failed write is a correctness bug, not a logging gap.
- Configuration that a code path writes must be the same configuration another code path reads. Asymmetry
  between the value written and the value compared makes a feature look enabled while never taking effect.
- Filter placeholders are bound only when present in the template. An unbound parameter is dropped silently,
  which can widen a security-relevant filter; check that every value a filter is meant to constrain is used.
- Any authorization branch that grants access because a required-role list is empty is fail-open. Verify the
  effective default of the property the list comes from, not just the CAS-side default.
- Password-bearing LDAP operations must be checked against RFC 3062 confidentiality requirements. A log
  warning is not enforcement, and the transport check itself should be read carefully for inverted logic.

## Startup Performance Notes

- Startup cost concentrates in three places: work done while the auto-configuration graph is assembled,
  work done by eagerly-initialized beans, and work done by application lifecycle listeners. Investigate
  those three before tuning any individual component.
- `spring.main.lazy-initialization` is enabled for the web application, so every `@Lazy(false)` is a
  deliberate opt-out that needs a reason. An eager bean drags its whole dependency graph in with it.
- When a bean is guarded by `BeanSupplier`/`BeanCondition`, declare its collaborators as `ObjectProvider`.
  Declaring them directly instantiates them before the condition is evaluated, so a feature that is off by
  default still pays for its dependencies on every startup.
- `ServiceLoader` lookups and `classpath*:` resource scans are fixed for the lifetime of the JVM. Resolve
  them once and reuse the result; never place one inside a loop or on a path that runs per bean, per file
  or per request.
- Diagnostics that instrument the whole startup sequence — startup-event recording, configuration
  validation, metadata catalogs — must be opt-in or built lazily. A feature whose only consumer is an
  actuator endpoint that is not exposed by default should not be enabled by default.
- Binding `CasConfigurationProperties` walks a property model spanning several hundred classes. Treat any
  code that re-binds or re-validates that tree as expensive and make sure it happens at most once per run.
- Reports and summaries should resolve their data sources only after establishing that there is something
  to report, and expensive indexes should be built on first access rather than in a constructor.
- Deferring work to the first request is an accepted pattern here (webflow definitions are built that way).
  Prefer it over doing the work during context refresh.
- To measure, start the server with `-DCAS_APP_STARTUP=buffering` and read the `startup` actuator endpoint,
  or `-DCAS_APP_STARTUP=jfr` for a flight-recording session. Both are profiling aids and are off by default.
- Actuator endpoint discovery does not depend on eager beans. Handler mappings are found by a type
  lookup, and the endpoint access rules in the security filter chain come from the endpoint suppliers,
  which are resolved by type while the chain is built. An eager marker on actuator configuration
  usually only decides when the endpoint beans and their request mappings are created.
- Module tests do not run with lazy initialization; that setting belongs to the web application's own
  resources. Any change to bean eagerness must be validated with the puppeteer scenarios covering the
  affected area, and the report should state which layer each result covers.

## View / Presentation Layer Notes

- The resolver registered with the Thymeleaf engine is `ChainingTemplateViewResolver`, not the
  delegates inside it. Cache and existence flags on the delegates are discarded — the chain's own
  `TemplateResolution` decides whether a parsed template is cached. Read the chain's constructor
  before concluding that `spring.thymeleaf.cache` has any effect.
- Thymeleaf's template cache key does not include the resolved theme. Any theme-dependent resolver
  is therefore only safe to cache if the theme is first added to `templateResolutionAttributes`;
  otherwise one tenant's theme is served to another.
- Theme names are attacker-controlled by default: `RequestHeaderThemeResolver` reads the `theme`
  header and `CookieThemeResolver` reads a cookie, both in the default chain. Treat a theme name
  as untrusted input on every path it reaches — template path interpolation
  (`String.format(resourceName, themeName)`), `ThemeBasedViewResolver.resolvers`,
  `ResourceBundleThemeSource.themeCache`, and message-source lookups.
- Any per-request map keyed by a request-supplied value needs a bound or an allow-list. Several
  view caches are `ConcurrentHashMap`s that are never evicted.
- Theme resolution runs once per *template resolution*, not once per request — that means once per
  fragment, per resolver, per prefix. Anything expensive in a `ThemeResolver` (service lookup,
  access-strategy evaluation, resource existence checks, HTTP calls) is multiplied accordingly.
  If a resolver stores its answer in a request attribute, verify something actually reads it back.
- `th:utext` disables escaping. Reserve it for `#{...}` message lookups; never use it on model
  values that originate from HTTP input, audit records, service metadata or user profiles.
- The default `cas.http-web-request.header.content-security-policy` includes `unsafe-inline` and
  `unsafe-eval`, so CSP is not a mitigation for anything in the view layer. The filter supports an
  `@nonce@` placeholder — assume it is not in use.
- `cas.view.template-prefixes` directories are also registered as static resource locations under
  `/**`. Anything placed there is publicly readable, templates included.
- Line-delimited protocol output (CAS 1.0, per-line attribute renderers) has no structural
  escaping. XML/JSON views escape; the plain-text ones do not.

## CAS Protocol Notes (v1 / v2 / v3, SAML 1.1)

- The protocol surface is small and worth memorizing: `support/cas-server-support-validation-core`
  (`AbstractServiceValidateController` plus the `v1`/`v2`/`v3` subclasses and the response views),
  `support/cas-server-support-validation` (`CasValidationAutoConfiguration` wires controllers,
  views and validation specifications), `core/cas-server-core-validation-api` (specifications),
  `core/cas-server-core/DefaultCentralAuthenticationService` (the whole ticket lifecycle), and
  `support/cas-server-support-saml` + `support/cas-server-support-saml-core-api` for `/samlValidate`.
- `DefaultCentralAuthenticationService` is where ST/PT/PGT correctness lives. Read a ticket
  **inside** `lockRepository.execute(...)`, never before it: a ticket captured outside the lock is a
  stale snapshot, and single-use enforcement (`countOfUses >= numberOfUses`) evaluated against a
  snapshot is not enforcement. The default `LockRepository` is JVM-local, so a lock alone never
  makes an operation cluster-safe.
- Validation specifications (`ChainingCasProtocolValidationSpecification` and friends) are
  singleton beans whose `renew` flag is mutated per request by a `ServletRequestDataBinder`.
  Treat any per-request state on a protocol bean as a concurrency defect at a security boundary.
- Order matters in `handleTicketValidation`: the proxy-callback path (`pgtUrl` -> authentication
  transaction -> PGT creation) currently runs before ticket validation. Anything that performs I/O
  or mints a credential must come after the ticket, service match and validation specification
  have all been checked.
- `pgtUrl` is caller-supplied. `RegisteredServiceProxyPolicy.isAllowedProxyCallbackUrl`
  implementations that use `RegexUtils.find` are doing an unanchored substring search; that is not
  an authorization check. CAS 3.0 also requires the callback to be HTTPS with peer trust
  established — no scheme check exists today, and the shared HTTP client follows redirects.
- Response templates live in `support/cas-server-support-thymeleaf/src/main/resources/templates/protocol/`.
  `{{{...}}}` is unescaped; anything interpolated there (proxy URLs) or used as an element name
  (attribute names, only space-sanitized by `CasProtocolAttributesRenderer.sanitizeAttributeName`)
  is an XML-injection surface.
- Error codes are part of the protocol. CAS 3.0 requires `INVALID_TICKET_SPEC` for validation
  specification failures and `INTERNAL_ERROR` for unexpected failures; `CasProtocolConstants`
  currently has neither, and `messages.properties` defines `INVALID_TICKET_SPEC` text that is
  never emitted.
- SAML 1.1: `Response/@InResponseTo` must be the request's `RequestID` and `Response/@Recipient`
  must be the consumer URL. `Saml10ObjectBuilder.newResponse` overloads `InResponseTo` with the
  TARGET hostname and never sets `Recipient`. Conditions come from
  `Saml10ObjectBuilder.newConditions` — `NotBefore` is skew-backdated while `NotOnOrAfter` is
  computed from `now()`, so the two are not symmetric.
- Performance hot spot: `validateServiceTicket` evaluates the attribute release policy twice with
  near-identical contexts and merges four attribute maps, then the views resolve the registered
  service and attributes again while rendering. Release policies usually hit person-directory
  back-ends, so duplicated resolution is a real per-validation cost, not a micro-optimization.
- XXE is already handled in the SAML1 request path; don't re-report it.

## Interrupt Notifications Notes

- Surface: `support/cas-server-support-interrupt-{api,core,webflow}`, `InterruptWebflowConfigurer` (all state
  wiring), `FinalizeInterruptFlowAction`/`InquireInterruptAction`, `SimpleInterruptTrackingEngine`, and
  `templates/interrupt/casInterruptView.html`.
- Inquiry runs at up to three checkpoints per login (after `realSubmit`/`sendTicketGrantingTicket`, prepended to
  `createTicketGrantingTicket` in `AFTER_AUTHENTICATION`, prepended to `generateServiceTicket`) and again on every
  SSO service ticket. The "finalized" marker is request-scoped and set only after the user acts, so a
  non-interrupting result re-runs every inquirer (REST calls included) at each checkpoint.
- The skip decision is "tracking cookie equals a fresh inquiry result". The cookie stores
  `SimpleInterruptTrackingEngine.TrackedInterrupt` (principal id + response) and is ignored for other principals;
  blocking responses are never tracked (`FinalizeInterruptFlowAction` link path) and never skipped
  (`InquireInterruptAction`). Keep both guards, and keep the record registered in `CasInterruptRuntimeHints`.
- Inquirer failures are fail-open (`none()` or `null`). `InterruptResponse`'s no-arg constructor defaults to
  `interrupt=true`, so any stray JSON body becomes an interrupt; `RestEndpointInterruptInquirer` therefore parses
  only `2xx` responses. Keep that guard when touching the REST path.
- `JsonResourceInterruptInquirer` serves lookups from an immutable map swapped atomically on reload; never
  mutate shared inquirer state in place on the request path. Inquirers are created inside configurer lambdas,
  not as beans, so their `DisposableBean.destroy()` is never called by Spring.
- Passive requests: OIDC `prompt=none` and SAML `IsPassive` are mapped to `gateway`. `InquireInterruptAction` returns
  `gateway` when an interrupt is required on such a request, and every state that runs the inquiry must route
  `gateway` to `gatewayServicesManagement`. An unmatched event in an action state with prepended actions falls
  through to the next action (for `createTicketGrantingTicket`, that creates the TGT).
- `interruptCookieCipherExecutor` auto-enables crypto when keys exist, but `interruptCookieValueManager` reads only
  `crypto.enabled`; keep the two decisions identical.
- The view renders `principal.id` (message parameter), `interrupt.message` and `interrupt.data` with `th:utext`.
- Puppeteer `interrupt-aftersso-login` covers a forged `proceed` on a blocked response; `interrupt-afterauthn-groovy`
  covers following a blocked link and logging in again without logging out.
- Protocol modules can reuse an SSO session without the login webflow: the SAML2 IdP builds responses straight from
  the TGT (`AbstractSamlIdPProfileHandlerController.singleSignOnSessionExists`), so webflow-only checks such as
  interrupts do not run there. That is accepted for now (maintainer decision); vetoing through
  `InterruptSingleSignOnParticipationStrategy` was rejected. Expected gateway-like behavior: no SSO session -> no
  interrupt; SSO session and the request returns to the login flow -> no interrupt, no ticket; SSO session answered
  outside the login flow -> no interrupt evaluated.
- Puppeteer `interrupt-gateway-login` covers CAS gateway, OIDC `prompt=none` and SAML2 `IsPassive` without an SSO
  session, then with a pending interrupt (AFTER_SSO; SAML2 is answered from the SSO session), then after acknowledgement.

## Groovy / Scripting Notes

- Surface: `api/cas-server-core-api-scripting` (`ExecutableCompiledScript`, `ExecutableCompiledScriptFactory`,
  `ScriptResourceCacheManager`) and `core/cas-server-core-scripting` (`GroovyShellScript` for inline
  `groovy { ... }`, `WatchableGroovyScriptResource` for `file:`/`classpath:` `.groovy`, `ScriptingUtils`,
  `GroovyScriptResourceCacheManager`, `GroovyScriptCacheManagerEndpoint`). ~50 modules consume it, so a
  defect in those five classes is a defect in attribute release, AUP, MFA, OIDC, SAML, themes, interrupts
  and webflow at once. Review them first.
- `ScriptResourceCacheManager` is `AutoCloseable` and its `close()` invalidates the WHOLE shared cache.
  Never put `getScriptResourceCacheManager()` in a try-with-resources; it is a singleton bean, not a
  per-call resource.
- Every `execute(...)` serializes on the script's own `CasReentrantLock`, now via the blocking
  `executeAndThrow`/`execute`. Cached scripts are shared singletons, so concurrent requests still run one at
  a time and a slow script (HTTP/LDAP are star-imported into the compiler config, so scripts doing I/O is
  expected) is a throughput bottleneck — but it no longer yields a silent `null`. Do not reintroduce
  `tryLock` here: its 5-second timeout returned `null` with no error, and callers such as MFA triggers and
  AUP read `null` as "skip", i.e. fail-open. A script that blocks forever now blocks its callers, which is
  the intended failure mode for a security decision; nested execution of two different scripts in opposite
  orders could deadlock, so avoid cross-script invocation.
- Never hand `groovy.lang.Binding` an immutable map. `Binding(Map)` keeps the map you give it, and
  `ScriptingUtils.executeGroovyShellScript` writes a `logger` variable into it on every run, so a
  `Map.of()` makes the next execution throw `UnsupportedOperationException` — which that method swallows,
  turning the call into a silent `null`. Use the no-arg `new Binding()`, which builds its map lazily.
- `GroovyShellScript` holds its binding in a static ThreadLocal whose value is an owner-scoped
  `ScriptBinding(instanceId, variables)`; `execute` reads and removes it before taking the lock and uses it
  only when the owner matches, so a binding belongs to one thread, one script and one execution. The
  ThreadLocal stays static because ErrorProne's `ThreadLocalUsage` rejects a per-instance one, and the
  owner id rather than the script reference keeps `ReferenceEquality` quiet and avoids retaining the script.
  `setBinding` must be followed by `execute` on the same thread. `WatchableGroovyScriptResource` does not
  support bindings at all (the interface default is a no-op); external scripts receive arguments
  positionally.
- `failOnError` is honored by both implementations, but their defaults differ deliberately:
  `WatchableGroovyScriptResource` defaults to `true`, `GroovyShellScript` to `false`. Inline scripts have
  always swallowed failures and features depend on it — `ReturnMappedAttributeReleasePolicy` releases the
  attributes whose scripts worked, `GroovyRegisteredServiceUsernameProvider` falls back to the principal id,
  and both have tests asserting exactly that. So an inline script still yields `null` unless the caller
  passes `true` or calls `setFailOnError(true)`. Never unbox a script result directly, and decide
  per call site whether `null` should be a decision or a failure.
- Resolve scripts through `ScriptResourceCacheManager.resolveScriptableResource(...)`. Calling
  `fromScript(...)` or `fromResource(...)` on a request path recompiles the script (new `GroovyClassLoader`
  and class per call) and, for `fromResource`, starts a `FileWatcherService` thread that nothing closes.
  The cache manager is a Spring bean reached through `ApplicationContextProvider`, and plenty of components
  are constructed outside Spring (tests included), so resolve it as
  `.map(cacheManager -> cacheManager.resolveScriptableResource(...)).orElseGet(() -> factory.fromScript(...))`
  rather than `orElseThrow`. Hoist the resolution out of any loop over attributes or values.
- A script held by a serializable service definition (an access strategy, a username provider) belongs in a
  `@JsonIgnore @Transient transient ExecutableCompiledScript` field built lazily — see
  `GroovyRegisteredServiceAccessStrategy` and `GroovySurrogateRegisteredServiceAccessStrategy`. Lombok's
  `@EqualsAndHashCode` skips transient fields, so the definition's identity is unaffected.
- `ScriptingUtils.getObjectInstanceFromGroovyResource` (the `newObjectInstance(Resource, ...)` path used for
  Groovy classes with constructor arguments) caches the compiled class per resource URI and recompiles only
  when the file's last-modified time changes. It has no file watcher, so that timestamp is the reload
  signal.
- Cache keys are `sha256` of the joined key parts. Include everything the compiled script depends on;
  omitting a discriminator shares one compiled script across callers that should not share it.
- Never compile caller-supplied Groovy. Groovy executes AST transformations inside the compiler —
  `groovy.transform.ASTTest` runs a caller-supplied closure at compile time and `groovy.lang.Grab` fetches
  jars — so compiling text is executing it, even though nothing calls `run()`, which is why it reads as
  safe. `ScriptingUtils.validateGroovyScript` drives a bare `SourceUnit` (`parse`, `completePhase`,
  `convert`, then check the error collector) and is what the `groovyCache` `resources/validate` operation
  uses; keep it that way, and do not "improve" it into a full compile to catch unresolved classes. Two traps
  in that sequence: with the Parrot parser `SourceUnit.parse` only builds the AST builder, so errors do not
  surface until `convert`, and `convert` needs `completePhase` before it or it throws a `GroovyBugError`
  about the phase. The safety comes from there being no `CompilationUnit`: global transformations are
  registered as a compilation unit's phase operations, and local ones require semantic analysis, which is
  past where this stops. A `CompilationUnit` stopped at `Phases.CONVERSION` would NOT be safe — Grape's
  `GrabAnnotationTransformation` is global and runs at exactly that phase. `resources/{key}` on that endpoint returns script file
  contents, so treat the whole endpoint as administrative. It is `Access.NONE` by default.
- CAS REST actuator endpoints (`BaseCasRestActuatorEndpoint` / `@RestActuatorEndpoint`) use Spring MVC
  mappings rather than `@ReadOperation`/`@WriteOperation`, so Spring Boot's `Access.READ_ONLY` cannot
  distinguish a `GET` from a `POST` or `DELETE` on them. Granting read-only access to such an endpoint grants
  its write operations too; do not rely on the access level to gate a mutating mapping.
- Groovy scripts are configuration, not user input: every script body traced in this tree comes from a
  registered service definition or a `cas.*` property. Keep it that way — never let a request-derived value
  reach `isScript`/`fromScript`.
