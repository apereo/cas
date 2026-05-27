# AGENTS.md

This file provides guidance for **AI coding agents** (Codex, Claude Code, Copilot Workspace, and similar autonomous agents) working on the **Apereo CAS** repository. Agents should read this file before making any changes.

> **Note**: This repository is for CAS *contributors*. If you're deploying/configuring CAS, use the **WAR Overlay** method — not this source repository.

## Rules

These rules override any instinct to "be helpful by doing more."

### Ask

- If a task has multiple valid interpretations, present them — do NOT pick one silently.
- If you are unsure how existing code works, read it first. If still unsure, stop and ask.
- If you cannot find a test to verify your change, say so before proceeding.
- Never invent requirements. Do exactly what was asked, nothing more.

### Simplicity

- Write the minimum code that solves the stated problem.
- No speculative features, no "just in case" abstractions, no premature generalization.
- No error handling for impossible scenarios.


### Changes

- Touch only what the task requires. Do not "improve" adjacent code, comments, or formatting.
- Do not refactor things that are not broken.
- Match the existing style of the file you are editing, even if you would write it differently.
- If your change creates unused imports or variables, remove those. Do not remove pre-existing dead code.
- Every changed line must trace directly back to the task at hand.

---

## Repository Context

Apereo CAS is an enterprise single sign-on (SSO) and authentication server. The codebase is a multi-module Gradle project with 500+ modules, built with Java 25+ and Spring Boot 3.x.

### Module Layout

| Directory  | Purpose                                       | Dependency Rule                              |
|------------|-----------------------------------------------|----------------------------------------------|
| `api/`     | Interface definitions and contracts           | Minimal dependencies; no implementation code |
| `core/`    | Core implementations                          | Depends on corresponding `api/` modules      |
| `support/` | Feature modules (LDAP, OIDC, SAML, Duo, etc.) | Can depend on `core/` and other `support/`   |
| `webapp/`  | Web application assemblies (Tomcat, Jetty)    | Aggregates `core/` and `support/` modules    |
| `docs/`    | User-facing documentation (Markdown)          | Independent                                  |
| `ci/`      | CI scripts and test infrastructure            | Independent                                  |
| `style/`   | Checkstyle, SpotBugs, ErrorProne configs      | Independent                                  |

---

## Agent Workflow

### Before Making Changes

1. **Understand the module structure** — identify which module(s) your change affects.
2. **Read existing code** in the target module to understand current patterns and conventions.
3. **Check for existing tests** — understand how the module is tested before adding or modifying code.

### Making Changes

1. **Build first** to confirm a clean baseline:
   ```bash
   ./gradlew build --parallel -x test -x javadoc -x check
   ```
2. **Make focused changes** — one logical change per commit. Keep diffs small and reviewable.
3. **Add or update tests** for every change:
   ```bash
   ./testcas.sh --category CategoryName
   ./testcas.sh --test SpecificTestClassName
   ```
4. **Update documentation** in `docs/cas-server-documentation/` for user-facing changes.
5. **Validate the build** after changes:
   ```bash
   ./gradlew build --parallel -x test -x javadoc -x check
   ```
6. **Run relevant tests** to confirm nothing is broken:
   ```bash
   ./gradlew :module-path:test --tests "*RelevantTest"
   ```

### After Making Changes

- Verify no unused imports or trailing whitespace
- Confirm Checkstyle, SpotBugs, and ErrorProne pass
- Ensure the change is minimal and scoped

---

## Code Conventions (Quick Reference)

| Rule                     | Detail                                                       |
|--------------------------|--------------------------------------------------------------|
| Java version             | Java 25+ — use records, pattern matching, switch expressions |
| Indentation              | 4 spaces, NO tabs                                            |
| Braces                   | Always required, even for single-line blocks                 |
| Max line length          | 200 characters                                               |
| Null safety              | `@NullMarked` at package level (JSpecify)                    |
| Conditionals             | Avoid needless `else`                                        |
| Imports                  | No unused imports                                            |
| Logging                  | Lombok `@Slf4j` → `LOGGER` field                             |
| Dependency injection     | `@RequiredArgsConstructor` with `final` fields               |
| Bean registration        | `@ConditionalOnMissingBean` + `@RefreshScope`                |
| Feature toggles          | `@ConditionalOnFeatureEnabled`                               |
| Configuration classes    | `@AutoConfiguration` or `@Configuration`                     |
| Configuration properties | Annotate with `@RequiresModule`                              |
| Public APIs              | Require Javadoc with `@since` tags                           |
| Avoid                    | `@Data` (too implicit), custom crypto, circular dependencies |

---

## Security Boundaries

Agents **must not** autonomously modify the following areas without explicit human approval:

- **Authentication flows** — login, logout, SSO, credential handling
- **Authorization and access control** — service access strategies, attribute release
- **Ticket lifecycle** — ticket creation, validation, expiration, encryption
- **Cryptographic operations** — signing, encryption, key management
- **MFA workflows** — provider registration, bypass logic, trust decisions
- **Session management** — cookie handling, session fixation protections
- **Input validation** — sanitization, injection prevention

### Security Rules for Agents

1. Never weaken existing security constraints
2. Never disable or bypass authentication/authorization checks
3. Use CAS-provided crypto utilities — do not introduce custom cryptographic code
4. Always validate user input
5. Test security-sensitive changes with both positive and negative cases
6. Follow the principle of least privilege

---

## Testing Requirements

| Change Type            | Testing Requirement                                        |
|------------------------|------------------------------------------------------------|
| Bug fix                | Add a test that reproduces the bug and verifies the fix    |
| New feature            | Comprehensive test coverage (unit + integration)           |
| Refactoring            | Existing tests must continue to pass                       |
| Configuration change   | Test with both default and custom configurations           |
| Security-related       | Positive and negative test cases required                  |

### Test Commands

```bash
# List available test categories
./gradlew -q testCategories

# Run a test category
./testcas.sh --category CategoryName

# Run a specific test class
./testcas.sh --test TestClassName

# Run a specific test via Gradle
./gradlew :core:cas-server-core-authentication:test --tests "*AuthenticationHandlerTests"

# Debug tests (port 5005)
./testcas.sh --category CategoryName --debug
```

---

## Common Patterns

### Spring Bean Registration
```java
@Bean
@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
@ConditionalOnMissingBean(name = "myService")
public MyService myService(final CasConfigurationProperties casProperties) {
    return new DefaultMyService(casProperties);
}
```

### Feature Configuration Class
```java
@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ConditionalOnFeatureEnabled(feature = CasFeatureModule.FeatureCatalog.LDAP)
public class LdapAuthenticationConfiguration {
    // bean definitions
}
```

### Service Lookup
```java
val service = servicesManager.findServiceBy(serviceId);
if (service == null) {
    throw new UnauthorizedServiceException("Service not found");
}
```

### Ticket Operations
```java
val tgt = ticketRegistry.getTicket(tgtId, TicketGrantingTicket.class);
if (tgt == null || tgt.isExpired()) {
    throw new InvalidTicketException(tgtId);
}
```

---

## Prohibited Actions

Agents **must not**:

- ❌ Add dependencies without clear justification
- ❌ Reformat code outside the scope of the change
- ❌ Skip tests or quality checks
- ❌ Replace CAS-specific patterns with generic alternatives
- ❌ Perform broad refactorings without prior discussion
- ❌ Modify security-sensitive code without thorough testing
- ❌ Introduce code that doesn't compile or breaks existing tests
- ❌ Remove or weaken existing test assertionsd
- ❌ Guess at requirements. Add features that weren't asked for. 
- ❌ "Improve" code adjacent to your change. 
- ❌ Commit secrets.

---

## Validation Checklist

Before submitting any change, agents must verify:

- [ ] Code compiles: `./gradlew build --parallel -x test -x javadoc -x check`
- [ ] Relevant tests pass: `./testcas.sh --category CategoryName`
- [ ] No unused imports or trailing whitespace
- [ ] Checkstyle compliance (automatic during build)
- [ ] SpotBugs / ErrorProne clean
- [ ] Documentation updated (if user-facing)
- [ ] Change is focused and minimal
- [ ] Security implications have been considered

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
      
