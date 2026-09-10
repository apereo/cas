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
