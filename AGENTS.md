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

## Practical boundaries

- Put new behavior in the narrowest module that already owns that concern; do not skip from `webapp/` straight into backend-specific code when an `api/` or `core/` seam already exists.
- When adding configuration, update the config model class first; otherwise metadata/docs generation will not understand the new property.
- For service-aware logic, look for `ServicesManager.findServiceBy(...)`; for ticket-aware logic, look for `ticketRegistry.getTicket(...)`. Those seams are used repeatedly across `core/` and `support/` and are usually the right integration points.
- Treat authentication, tickets, webflow, logout, MFA, and crypto as security-sensitive areas. Match existing CAS utilities and flows instead of introducing parallel mechanisms.
- Keep diffs surgical: this codebase already has strong patterns, so the fastest path is usually “copy the nearest module family pattern and adapt it” rather than inventing a new abstraction.
