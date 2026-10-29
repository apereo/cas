# AGENTS.md

Guidance for AI coding agents working in the Apereo CAS source tree.

> This repository is for CAS contributors. If the task is deployment/configuration, prefer the WAR overlay approach instead of editing this repo.

## Skills: read the one that matches the work

Subsystem review discipline, scenario know-how and procedures live in skills under `.claude/skills/<name>/SKILL.md`.
Claude Code loads them on demand; other agents should open the file listed below before working in that area. The
rules in a skill are as binding as the ones in this file. Before reporting any change as done, follow `cas-handoff`.
When you learn something new, record it in the skill that owns the area, or here when no skill fits.

| Skill | Read when the work touches |
|---|---|
| [`cas-actuators`](.claude/skills/cas-actuators/SKILL.md) | Actuator endpoints, management security |
| [`cas-attribute-consent`](.claude/skills/cas-attribute-consent/SKILL.md) | `support/cas-server-support-consent*` |
| [`cas-build-ci`](.claude/skills/cas-build-ci/SKILL.md) | Gradle build logic, `.github/workflows`, `ci/` scripts |
| [`cas-docs-site`](.claude/skills/cas-docs-site/SKILL.md) | `docs/**`, release notes, `ci/docs/**` |
| [`cas-google-authenticator`](.claude/skills/cas-google-authenticator/SKILL.md) | `support/cas-server-support-gauth*` |
| [`cas-groovy-scripting`](.claude/skills/cas-groovy-scripting/SKILL.md) | `core/cas-server-core-scripting`, Groovy scripts |
| [`cas-handoff`](.claude/skills/cas-handoff/SKILL.md) | Finishing any task that changed files |
| [`cas-heimdall-authzen`](.claude/skills/cas-heimdall-authzen/SKILL.md) | `support/cas-server-support-heimdall*` |
| [`cas-import-pr`](.claude/skills/cas-import-pr/SKILL.md) | Bringing a contributor PR onto a local branch |
| [`cas-interrupt-notifications`](.claude/skills/cas-interrupt-notifications/SKILL.md) | `support/cas-server-support-interrupt*` |
| [`cas-ldap`](.claude/skills/cas-ldap/SKILL.md) | LDAP modules |
| [`cas-mcp-server`](.claude/skills/cas-mcp-server/SKILL.md) | `support/cas-server-support-mcp` |
| [`cas-oauth-oidc-authentication`](.claude/skills/cas-oauth-oidc-authentication/SKILL.md) | OAuth/OIDC/UMA client authentication, access-token-protected endpoints, `OAuth20UserProfile`, login callback |
| [`cas-oauth-oidc`](.claude/skills/cas-oauth-oidc/SKILL.md) | `support/cas-server-support-oauth*`, `support/cas-server-support-oidc*` |
| [`cas-oid4vc`](.claude/skills/cas-oid4vc/SKILL.md) | `support/cas-server-support-oidc-vc*`, wallets, SD-JWT/mdoc |
| [`cas-passwordless-webauthn`](.claude/skills/cas-passwordless-webauthn/SKILL.md) | `support/cas-server-support-passwordless*`, `support/cas-server-support-webauthn*` |
| [`cas-protocol`](.claude/skills/cas-protocol/SKILL.md) | Service/proxy validation, SAML 1.1 |
| [`cas-puppeteer-scenarios`](.claude/skills/cas-puppeteer-scenarios/SKILL.md) | `ci/tests/puppeteer/**`, `ci/tests/**/run-*.sh`, scenario cross-checks |
| [`cas-saml`](.claude/skills/cas-saml/SKILL.md) | `support/cas-server-support-saml*` metadata, SAML2 IdP session store |
| [`cas-sandbox-verification`](.claude/skills/cas-sandbox-verification/SKILL.md) | Gradle, JDK 25 or Maven Central unavailable; the device VM test runner |
| [`cas-service-registries`](.claude/skills/cas-service-registries/SKILL.md) | `ServicesManager`, `support/cas-server-support-*-service-registry*` |
| [`cas-session-stores`](.claude/skills/cas-session-stores/SKILL.md) | `CasSessionStore`, browser storage, the `BROWSER_STORAGE` cookie fallback |
| [`cas-startup-performance`](.claude/skills/cas-startup-performance/SKILL.md) | Startup time, eager beans, classpath scanning |
| [`cas-storage-backends`](.claude/skills/cas-storage-backends/SKILL.md) | MongoDB, Redis, JPA/Hibernate, DynamoDB modules |
| [`cas-testing`](.claude/skills/cas-testing/SKILL.md) | Running tests, writing tests, flaky tests |
| [`cas-ticket-registries`](.claude/skills/cas-ticket-registries/SKILL.md) | `support/cas-server-support-*-ticket-registry`, ticket compactors |
| [`cas-ui`](.claude/skills/cas-ui/SKILL.md) | Templates, `static/**`, themes, theme builder, Palantir |
| [`cas-web-security`](.claude/skills/cas-web-security/SKILL.md) | `CasWebSecurityConfigurer`, CSRF, authentication entry points |

## Big picture

- CAS is a very large Gradle monorepo. `settings.gradle` shows the main layering: `api/` defines contracts and config models, `core/` implements platform behavior, `support/` adds protocols/backends/features, and `webapp/` assembles runnable apps.
- The servlet app starts in `webapp/cas-server-webapp-init/src/main/java/org/apereo/cas/web/CasWebApplication.java`; startup is extensible through `ApplicationUtils.getApplicationEntrypointInitializers()`.
- Feature wiring is annotation-driven. Example: `core/cas-server-core-authentication/.../CasCoreAuthenticationAutoConfiguration.java` imports authentication sub-configurations behind `@ConditionalOnFeatureEnabled`.
- Support modules usually follow a family split such as `*-core`, storage variants (`*-jdbc`, `*-mongo`, `*-redis`), protocol/webflow modules, and a thin webapp assembly. OIDC, SAML, tickets, services, and MFA all follow this pattern in `settings.gradle`.
- `api/cas-server-core-api-configuration-model/.../CasConfigurationProperties.java` is the root of the `cas.*` config tree. Property classes are not passive POJOs: `ConfigurationMetadataGenerator` fails if a config model class is missing `@RequiresModule`.

## Conventions you should match

- Java 25 is required (`gradle.properties`); many sources use `import module java.base;`, Lombok `val`, and package-level `@NullMarked` via `package-info.java`.
- `import module java.base` makes `Signature` ambiguous (`java.security.Signature` vs `java.lang.classfile.Signature`); write `java.security.Signature`. Mapping to a `@SuperBuilder` result (`IntStream.mapToObj(i -> X.builder()...build())`) infers a capture type; give the stream a type witness (`.<X>mapToObj(...)`).
- `assertThrows(cond ? A.class : B.class, ...)` does not compile: the conditional infers two conflicting `T`s, and the lambda is then reported as throwing an unreported `Throwable`. Use an `if`/`else` with one `assertThrows` per branch.
- Lombok `val` cannot infer generic poly expressions: `val x = Objects.requireNonNullElse(list, List.of())` (or `...ElseGet(list, List::of)`) becomes `Object`. Assign the plain call to `val` and null-check separately, or declare the type.
- Spring config classes generally use `@AutoConfiguration` or `@Configuration(proxyBeanMethods = false)`, `@EnableConfigurationProperties(CasConfigurationProperties.class)`, `@ConditionalOnFeatureEnabled`, and bean methods with `@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)` plus `@ConditionalOnMissingBean`. See `support/cas-server-support-token-core/.../TokenCoreConfiguration.java`.
- Configuration classes belong to an auto-configuration: prefer a static inner
  `@Configuration(value = "...", proxyBeanMethods = false)` class nested in the module's main auto-configuration. A
  configuration class kept outside it must follow the structure the sibling modules of that family already use; do not
  introduce a new standalone `@Configuration` plus `@Import` where the family has none.
- Configuration model classes usually live under `api/.../configuration/model/**`, use Lombok accessors, and carry `@RequiresModule(name = "...")`; example: `LdapAuthorizationProperties`.
- Tests are organized by JUnit tags, not by the plain Gradle `test` task. The shared `buildSrc` test conventions disable `test` and register tasks like `testAuthentication` and `testTickets` for the tags discovered by `TestCategoryTagsValueSource` in `*Tests.java`. Both root category selectors and module-qualified category tasks execute tests; the plain `test` task never does.
- Related test scenarios are often grouped with `@Nested`; example: `support/cas-server-support-token-core/.../JwtBuilderTests.java`.
- Unalias Linux/macOS commands before you run them, specially `tree`, `find`, `grep`, `cat`, etc.
- From a sandbox that cannot delete files, run read-only git commands with `GIT_OPTIONAL_LOCKS=0` (for example `GIT_OPTIONAL_LOCKS=0 git status`); otherwise git can leave a stale `.git/index.lock` that blocks the user's git.
- Parse untrusted or configured numbers with Guava's `Longs.tryParse`/`Ints.tryParse` (null when the value is not a plain
  integer that fits), never `NumberUtils.isCreatable`/`isParsable`/`isDigits` followed by `Long.parseLong`/`Integer.parseInt`:
  those checks accept `1.5`, `1e3`, `0x1F` or overflowing values and the parse then throws. `tryParse` rejects null, so
  guard nullable inputs first.
- Consider using StringUtils.EMPTY instead of "" for empty strings, and StringUtils.isNotBlank() instead of != null && !isEmpty() for string checks.
- Keep overloaded methods (same name, any parameters or visibility) next to each other, with no other method between
  them; Checkstyle's `OverloadMethodsDeclarationOrder` fails the build otherwise. Check this when adding a method whose
  name already exists in the class or interface, including private helpers.
- Group `&&` inside `||` with parentheses (`(a && b) || (c && d)`); Error Prone's `OperatorPrecedence` warns otherwise and
  `-Werror` fails `compileJava`. Run Error Prone over every module you changed, new code included, before handing off.
- Never `catch (RuntimeException e)` or `catch (Error e)`; Checkstyle's `IllegalCatch` fails the build. Catch the specific
  exception, or `Exception` when any failure of the call must be handled the same way.
- Do not add unnecessary javadoc. Add javadoc only where a public contract or a non-obvious reason genuinely needs it; record rationale for a change in PLANS.md instead.
- Explanation belongs in the javadoc of the method it explains, with its parameters. Do not write inline block comments inside method bodies.
- Turning attribute text into numbers: `NumberUtils.createNumber` decodes a leading zero as octal. Only convert when the
  `createBigDecimal(text).toPlainString()` round trip returns the same text; no hand-written regular expressions for this.
- A Lombok `val` holding `Optional.or(...)` over pac4j's `Optional<Object>` request attribute is inferred as `Optional<Object>`; declare such locals with an explicit type.
- NullAway (JSpecify mode) does not carry a `containsKey` check over to `get`. Once a package is `@NullMarked`, a map
  whose values are `@Nullable` (Spring Boot's `AuditEvent.getData()`) fails `compileJava` on `map.get(key).toString()`.
  Read the value once and handle null, e.g. `Objects.toString(map.get(key), null)`; null-marking a package surfaces these
  in code nobody touched, so compile the module after adding a `package-info.java`.

## Security-sensitive change discipline

- Verify a security report against the complete execution path before changing code. Identify the attacker-controlled input, the trust decision, and the exact point where validation or isolation is bypassed.
- Keep security fixes at the narrowest shared enforcement point. Preserve unrelated protocol behavior and avoid adding parallel validation, crypto, or metadata abstractions.
- Treat caller-supplied protocol values as untrusted even if CAS later signs the containing message. Internal signing proves CAS produced the final message; it does not authenticate the caller's original value.
- A presented signature must be cryptographically and algorithmically validated whenever it is used as proof, independently of whether metadata requires requests to be signed. Apply the same resolved security parameters to every binding before raw cryptographic validation.
- For unsolicited SAML flows, accept ACS destinations only from the applicable registered-service metadata. For solicited flows, a request-supplied ACS outside metadata requires an actually authenticated request signature.
- Build credentials from matching certificate/private-key material belonging to the message recipient. Never combine a peer's public key with CAS's private key.
- `SubjectConfirmationData.Address` identifies the presenter, not the ACS server. Use trusted proxy-aware client information or omit it; never perform a synchronous DNS lookup of the ACS host while building assertions.
- Scope caches that contain service-specific metadata, keys, validation filters, or trust policy to the registered-service boundary. Address entries by their complete key; do not scan unrelated cached values for a matching entity.
- When replacing a library that verifies tokens, check what it does when nothing is configured: pac4j's `JwtAuthenticator` with no signature configuration skipped verification and accepted unsigned tokens, which let any `sub` log in through JWT authentication. Token validation in CAS refuses everything when the service defines no signing secret (`TokenAuthenticationSecurity`); keep it that way.

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
- Test sources compile with Error Prone and `-Werror`, so a warning such as `JdkObsolete` fails `compileTestJava`. When a test needs the flagged type on purpose (an unmodifiable `LinkedList` to get the non-`RandomAccess` wrapper), put `@SuppressWarnings("JdkObsolete")` on that test method.
- `WebApplicationServiceFactory` validates HTTP service URLs (a host, no user information, no backslash), so never hand a registered service's regex `serviceId` such as `https://oauth\.example\.org.*` to `RegisteredServiceTestUtils.getService`; use a concrete URL that the pattern matches.
- A fixed cipher key in a test must have the size the cipher expects: stringable encryption keys default to 512 bits (86 base64url characters) for `A256CBC-HS512`, and a shorter key fails with `DecryptionException`.
- `DefaultServiceMatchingStrategy` compares service URLs verbatim up to the query and decodes only the query, so `+` and `%20` in parameters still match while an encoded `#` or `/` before the query never does.

## Practical boundaries

- Put new behavior in the narrowest module that already owns that concern; do not skip from `webapp/` straight into backend-specific code when an `api/` or `core/` seam already exists.
- When adding configuration, update the config model class first; otherwise metadata/docs generation will not understand the new property.
- For service-aware logic, look for `ServicesManager.findServiceBy(...)`; for ticket-aware logic, look for `ticketRegistry.getTicket(...)`. Those seams are used repeatedly across `core/` and `support/` and are usually the right integration points.
- Treat authentication, tickets, webflow, logout, MFA, and crypto as security-sensitive areas. Match existing CAS utilities and flows instead of introducing parallel mechanisms.
- Keep diffs surgical: this codebase already has strong patterns, so the fastest path is usually “copy the nearest module family pattern and adapt it” rather than inventing a new abstraction.
