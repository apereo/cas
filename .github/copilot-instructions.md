# GitHub Copilot Instructions for Apereo CAS

This is the **Apereo CAS** (Central Authentication Service) codebase - a large-scale enterprise authentication server built with Java, Gradle, and Spring Boot.

> **Note**: If you're deploying/configuring CAS, use the **WAR Overlay** method, not this repository. Building from source is for contributors.

## Project Overview

- **Type**: Multi-module Gradle project (500+ modules)
- **Language**: Java 25+
- **Frameworks**: Spring Boot 3.x, Spring Cloud, Spring Webflow
- **Build Tool**: Gradle 8.x with parallel builds enabled
- **Architecture**: Modular design with clear separation between API, Core, Support, and Webapp layers

## Module Organization

```
api/           → Interface definitions and contracts (e.g., cas-server-core-api-authentication)
core/          → Core implementations (e.g., cas-server-core-authentication)
support/       → Feature modules (e.g., cas-server-support-ldap, cas-server-support-duo)
webapp/        → Web application modules (Tomcat, Jetty, etc.)
docs/          → Documentation and user guides
ci/            → CI scripts and test helpers
style/         → Checkstyle, SpotBugs, ErrorProne configs
```

**Key principle**: `api/` modules define contracts, `core/` implements them, `support/` adds features. Respect module boundaries.

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

# Debug mode
./testcas.sh --category CategoryName --debug
```

### Run Single Test (Direct Gradle)
```bash
# Run specific test class
./gradlew :core:cas-server-core-authentication:test --tests "*AuthenticationHandlerTests"

# Run specific test method
./gradlew :support:cas-server-support-ldap:test --tests "*LdapAuthenticationHandlerTests.verifySuccess"
```

### Test Conventions
- Every bug fix needs a test
- New features require comprehensive test coverage
- Tests belong in the same module as the code they test
- Use `@SpringBootTest` for integration tests
- Use `@Nested` for organizing related test cases

## Code Conventions

### Java Style
- **Java version**: Java 25+ (use modern features: records, pattern matching, switch expressions)
- **Indentation**: 4 spaces, NO tabs
- **Braces**: Always use braces, even for single-line blocks
- **Null safety**: Use `@NullMarked` at package level (JSpecify annotations)
- **Conditionals**: Avoid needless `else` statements
- **Imports**: No unused imports (enforced by Checkstyle)

### Lombok Usage
Lombok is heavily used. Key patterns:
- `@Getter` / `@Setter` for bean properties
- `@RequiredArgsConstructor` for dependency injection
- `@Slf4j` for logging (field name: `LOGGER`, static)
- `@ToString` / `@EqualsAndHashCode` with `doNotUseGetters = true`
- Avoid `@Data` (too implicit)

Example:
```java
@Slf4j
@RequiredArgsConstructor
public class MyService {
    private final ServiceManager servicesManager;
    
    public void doSomething() {
        LOGGER.debug("Processing request");
    }
}
```

### Spring Configuration
- Configuration classes use `@AutoConfiguration` or `@Configuration`
- Always use `@ConditionalOnFeatureEnabled` for feature toggles
- Use `@RefreshScope` for runtime-refreshable beans
- Use `@ConditionalOnMissingBean` to allow overrides
- Order beans with `@Order` or implement `Ordered`

Example:
```java
@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.LDAP)
public class LdapAuthenticationConfiguration {
    
    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    @ConditionalOnMissingBean(name = "ldapAuthenticationHandler")
    public AuthenticationHandler ldapAuthenticationHandler(
        @Qualifier("ldapAuthenticationProperties") final LdapProperties ldapProperties) {
        // implementation
    }
}
```

### Module Dependencies
- `api/` modules: Define interfaces only, minimal dependencies
- `core/` modules: Depend on corresponding `api/` modules
- `support/` modules: Can depend on `core/` and other `support/` modules
- Avoid circular dependencies
- Use `api` vs `implementation` dependency scopes appropriately

### Package Structure
```java
// Always declare package with @NullMarked
@NullMarked
package org.apereo.cas.authentication;

import org.jspecify.annotations.NullMarked;
```

## Quality Checks

### Checkstyle
Enforced via `style/checkstyle-rules.xml`:
- Line length: 200 characters
- Suppressions in `style/checkstyle-suppressions.xml`

### SpotBugs
Static analysis configured in `style/spotbugs-excludes.xml`

### ErrorProne
Enabled by default, skip with `-DskipErrorProneCompiler=true`

### NullAway
Null safety analysis, skip with `-DskipNullAway=true`

## Documentation

### User Documentation
Update `docs/cas-server-documentation/` for user-facing changes:
- Configuration properties → Add to relevant `*.md` files
- New features → Update feature documentation
- API changes → Update integration guides

### Code Documentation
- Public APIs require Javadoc
- Configuration properties need `@RequiresModule` annotation
- Include `@since` version tags for new APIs

Example:
```java
/**
 * Authenticates credentials against LDAP directory.
 *
 * @author Your Name
 * @since 8.0.0
 */
@RequiresModule(name = "cas-server-support-ldap")
public class LdapAuthenticationHandler implements AuthenticationHandler {
    // ...
}
```

## Security Considerations

**CRITICAL**: Always review AI-generated changes in these areas:
- Authentication flows (login, logout, SSO)
- Authorization and access control
- Ticket validation and issuance
- Cryptographic operations
- MFA workflows
- Session management
- Input validation and sanitization

### Security Best Practices
- Never weaken existing security constraints
- Always validate user input
- Use CAS-provided crypto utilities (don't roll your own)
- Test security changes with both positive and negative cases
- Follow principle of least privilege

## Common Patterns

### Bean Registration
```java
@Bean
@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
@ConditionalOnMissingBean(name = "myService")
public MyService myService(final CasConfigurationProperties casProperties) {
    return new DefaultMyService(casProperties);
}
```

### Configuration Properties
```java
@RequiresModule(name = "cas-server-support-ldap")
@Getter
@Setter
public class LdapAuthenticationProperties implements Serializable {
    private String ldapUrl;
    private String baseDn;
}
```

### Service Registry
```java
val service = servicesManager.findServiceBy(serviceId);
if (service == null) {
    throw new UnauthorizedServiceException("Service not found");
}
```

### Ticket Operations
```java
val ticketGrantingTicket = ticketRegistry.getTicket(tgtId, TicketGrantingTicket.class);
if (ticketGrantingTicket == null || ticketGrantingTicket.isExpired()) {
    throw new InvalidTicketException(tgtId);
}
```

## Development Workflow

### Making Changes
1. Build successfully: `./gradlew build -x test`
2. Add/update tests: `./testcas.sh --category YourCategory`
3. Run Checkstyle: Automatic during build
4. Update docs if user-facing
5. Keep diffs minimal and focused

### What NOT to Do
- ❌ Add dependencies without justification
- ❌ Reformat unrelated code
- ❌ Skip tests or quality checks
- ❌ Replace CAS patterns with generic alternatives
- ❌ Make broad refactorings without discussion
- ❌ Modify authentication/authorization without thorough testing

### PR Requirements
- ✅ Builds locally without errors
- ✅ Tests pass (or new tests added)
- ✅ Checkstyle/SpotBugs/ErrorProne clean
- ✅ Documentation updated
- ✅ Scope is focused and reviewable
- ✅ No unused imports or trailing whitespace

## Useful Gradle Tasks

```bash
# List all tasks
./gradlew tasks

# Show test categories
./gradlew -q testCategories

# Show dependencies for a module
./gradlew :core:cas-server-core-authentication:dependencies

# Check for dependency updates
./gradlew dependencyUpdates

# Generate Javadoc
./gradlew javadoc

# Run specific webapp
cd webapp/cas-server-webapp-tomcat && ../../gradlew bootRun
```

## Debugging

### Enable Remote Debugging
```bash
./testcas.sh --category CategoryName --debug
```
Debugger listens on port 5005

### Verbose Logging
Add to `src/main/resources/log4j2.xml`:
```xml
<Logger name="org.apereo.cas" level="debug"/>
```

## Additional Resources

- [Contributor Guidelines](https://apereo.github.io/cas/developer/Contributor-Guidelines.html)
- [Build Process](https://apereo.github.io/cas/developer/Build-Process.html)
- [Documentation](https://apereo.github.io/cas/development)
- [Architecture](https://apereo.github.io/cas/development/planning/Architecture.html)
