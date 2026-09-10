# AGENTS.md

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
- Spring config classes generally use `@AutoConfiguration` or `@Configuration(proxyBeanMethods = false)`, `@EnableConfigurationProperties(CasConfigurationProperties.class)`, `@ConditionalOnFeatureEnabled`, and bean methods with `@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)` plus `@ConditionalOnMissingBean`. See `support/cas-server-support-token-core/.../TokenCoreConfiguration.java`.
- Configuration model classes usually live under `api/.../configuration/model/**`, use Lombok accessors, and carry `@RequiresModule(name = "...")`; example: `LdapAuthorizationProperties`.
- Tests are organized by JUnit tags, not by the plain Gradle `test` task. The shared `buildSrc` test conventions disable `test` and generate tasks like `testAuthentication`, `testTickets`, etc. from `@Tag(...)` values found in `*Tests.java`.
- Related test scenarios are often grouped with `@Nested`; example: `support/cas-server-support-token-core/.../JwtBuilderTests.java`.
- Unalias Linux/macOS commands before you run them, specially `tree`, `find`, `grep`, `cat`, etc.

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
- Run one module or one class directly when narrowing a change:
  ```bash
  ./gradlew :core:cas-server-core-authentication:test --tests "*AuthenticationHandlerTests"
  ```
- Compile the tree without the expensive checks when you only need a fast validation pass:
  ```bash
  ./gradlew build --parallel -x test -x javadoc -x check
  ```
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
- Honor explicit verification boundaries. If the user asks not to run tests, do not invoke tests or Gradle tasks; perform static review such as `git diff --check` and clearly report what was not run.
- For release-bound security work, add one brief, user-facing note to the appropriate security/protocol section of the requested release-notes file after the implementation is complete.
- For all changes, cross check with puppeteer scenarios and make sure they continue to pass and are adjusted correctly.

## Practical boundaries

- Put new behavior in the narrowest module that already owns that concern; do not skip from `webapp/` straight into backend-specific code when an `api/` or `core/` seam already exists.
- When adding configuration, update the config model class first; otherwise metadata/docs generation will not understand the new property.
- For service-aware logic, look for `ServicesManager.findServiceBy(...)`; for ticket-aware logic, look for `ticketRegistry.getTicket(...)`. Those seams are used repeatedly across `core/` and `support/` and are usually the right integration points.
- Treat authentication, tickets, webflow, logout, MFA, and crypto as security-sensitive areas. Match existing CAS utilities and flows instead of introducing parallel mechanisms.
- Keep diffs surgical: this codebase already has strong patterns, so the fastest path is usually “copy the nearest module family pattern and adapt it” rather than inventing a new abstraction.

## OIDC verifiable credentials (OID4VCI / OID4VP)

- The OID4VCI Nonce Endpoint must stay publicly reachable (OID4VCI 1.0, section 7.1: it is not a protected resource). Do not add it to the interceptor's protected list; rate limiting or throttling is the appropriate control there. The presentation request creation endpoint is CAS's own verifier API and should be protected.
- A credential offer's `tx_code` is optional in the protocol but mandatory once the offer declares one. Keep it independent of any value the caller already holds, and leave a way to disable it for wallets that collect no user input, otherwise the walt.id puppeteer scenario cannot redeem an offer.
- Verify a JWT proof's `typ` header as well as its signature. A proof or key-binding JWT that is only checked for signature, audience and freshness can be satisfied by a token minted for a different protocol.
- When reviewing these flows, confirm the normative text against the current published OID4VCI, OID4VP and SD-JWT VC specifications rather than from memory; the drafts changed substantially before 1.0.

## Environment limits

- Gradle may be unavailable in a sandboxed or remote review environment because the wrapper cannot download its distribution. When that happens, say the verification was not run instead of implying a test result, and fall back to static review such as `git diff --check` and targeted reading.


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
