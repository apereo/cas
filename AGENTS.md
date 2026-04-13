# AGENTS.md — Apereo CAS

Enterprise SSO server: Java 25+, Spring Boot 3.x, Gradle 8.x, 500+ modules.

## Architecture

Four-layer module hierarchy with strict dependency direction:

```
api/     → Interfaces & contracts only (no implementations)
core/    → Implementations of api/ contracts
support/ → Feature modules (LDAP, OIDC, SAML, etc.) — depend on core/
webapp/  → Deployable web applications (Tomcat, Jetty, Undertow)
```

Each feature follows this pattern: `api/cas-server-core-api-authentication` defines interfaces → `core/cas-server-core-authentication` implements them → `support/cas-server-support-ldap` extends with LDAP-specific behavior. Never add dependencies going upward (e.g., `api/` must not depend on `core/`).

## Build Commands

```bash
./gradlew build --parallel -x test -x javadoc -x check   # Full build, skip tests
./gradlew :support:cas-server-support-ldap:build          # Single module
cd webapp/cas-server-webapp-tomcat && ../../gradlew bootRun # Run locally (https://localhost:8443/cas)
```

## Testing

Tests use JUnit 5 `@Tag` annotations matching categories defined in `gradle/tests.gradle`. The default `test` task is **disabled**; you must target a category:

```bash
./testcas.sh --category Authentication        # Run a category via wrapper script
./testcas.sh --test DefaultAuthenticationManagerTests  # Run specific test class
./gradlew :core:cas-server-core-authentication:testAuthentication  # Direct Gradle: test<CategoryName>
```

Every test class requires `@Tag("CategoryName")` — see `gradle/tests.gradle` `TestCategories` enum for valid values. Tests also require:
- `@SpringBootTestAutoConfigurations` — composite annotation replacing dozens of Spring Boot auto-config imports (defined in `core/cas-server-core-util-api/src/test/java/.../SpringBootTestAutoConfigurations.java`)
- `@ExtendWith(CasTestExtension.class)` — sets system properties and mock request context
- `@SpringBootTest(classes = {...})` — listing the specific `*AutoConfiguration` and `*Configuration` classes under test

## Configuration Class Pattern

Two-tier structure — one `@AutoConfiguration` entry-point per module that `@Import`s inner `@Configuration` classes:

```java
// Entry point: registered in META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
@AutoConfiguration
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.LDAP, module = "authentication")
@Import({LdapAuthenticationConfiguration.class, ...})
public class CasLdapAuthenticationAutoConfiguration {}

// Inner detail class with bean definitions
@Configuration(value = "LdapAuthenticationConfiguration", proxyBeanMethods = false)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.LDAP, module = "authentication")
class LdapAuthenticationConfiguration {
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "ldapPrincipalFactory")
    public PrincipalFactory ldapPrincipalFactory() { ... }
}
```

Every configuration class must use `@ConditionalOnFeatureEnabled` referencing `CasFeatureModule.FeatureCatalog`. Bean names often use `BEAN_NAME` constants on interfaces (e.g., `ServicesManager.BEAN_NAME`).

## Java Conventions

- **Module imports**: Files use `import module java.base;` instead of individual `java.*` imports
- **Lombok**: `LOGGER` field via `@Slf4j` (configured in `lombok.config`); use `val` for local type inference; avoid `@Data`
- **Null safety**: Every package needs `package-info.java` with `@NullMarked` (JSpecify); NullAway enforces this at compile time
- **Compiler strictness**: `-Werror` is on by default; ErrorProne + NullAway run during compilation (skip with `-DskipErrorProneCompiler=true` / `-DskipNullAway=true`)

## Key Files

| Purpose | Path |
|---|---|
| Version catalog | `gradle/libs.versions.toml` |
| Dependency declarations | `gradle/dependencies.gradle` |
| Test infra & categories | `gradle/tests.gradle` |
| Spring Boot/webapp setup | `gradle/springboot.gradle`, `gradle/war.gradle` |
| Checkstyle rules (200-char lines) | `style/checkstyle-rules.xml` |
| Feature toggle annotation | `core/cas-server-core-util-api/.../ConditionalOnFeatureEnabled.java` |
| Feature catalog enum | `api/cas-server-core-api-configuration-model/.../CasFeatureModule.java` |
| All CAS properties | `api/cas-server-core-api-configuration-model/.../CasConfigurationProperties.java` |
| Test base annotation | `core/cas-server-core-util-api/src/test/.../SpringBootTestAutoConfigurations.java` |

## Module build.gradle Pattern

Support modules declare metadata and split `implementation` vs `testImplementation` deps. Test-only jars from other modules use `configuration: "tests"`:

```groovy
dependencies {
    implementation project(":core:cas-server-core-authentication-api")
    testImplementation project(path: ":core:cas-server-core-authentication-api", configuration: "tests")
}
```

## Security

Never weaken auth/ticket/crypto/MFA flows. Use CAS-provided crypto utilities. All changes to `authentication/`, `tickets/`, `webflow/` packages require test coverage with both positive and negative cases.

