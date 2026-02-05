# COPILOT.md

This repository is **Apereo CAS** (Central Authentication Service), a large multi-module Gradle codebase (server + modules + docs). This file provides **GitHub Copilot / AI-assistant guidance** so changes remain consistent with CAS project practices and reviewer expectations.

> Note: If you are trying to *deploy/configure* CAS, you generally should **not** be editing this repository directly; use the **WAR Overlay** approach instead. Building from source is primarily for contributors.

---

## Goals for AI-assisted changes

When using Copilot (or any AI assistant), optimize for:

- Small, reviewable diffs
- Consistency with existing patterns
- Tests + docs where applicable
- Build correctness (Gradle, module boundaries)
- Security correctness (authN/authZ must not be weakened)

---

## Repository basics

- Multi-module Gradle build
- Embedded webapps under `webapp/` for local runs
- Documentation under `docs/cas-server-documentation/`

---

## Clone & build

```bash
git clone --recursive https://github.com/apereo/cas.git
cd cas
./gradlew build --parallel -x test -x javadoc -x check
```

---

## Running locally

```bash
cd webapp/cas-server-webapp-tomcat
../../gradlew bootRun
```

CAS will be available at `https://localhost:8443/cas`.

---

## Testing

Prefer automated tests. Use:

```bash
./testcas.sh --help
```

Include tests for bug fixes and behavior changes.

---

## Code conventions

- Always use braces
- 4-space indentation, no tabs
- Avoid needless `else`
- Prefer Lombok where appropriate
- Follow Checkstyle & SpotBugs expectations
- Document public APIs and configuration properties
- Make sure there are no unused imports.
- Follow existing patterns for similar features
- Use Java 25+ features where appropriate
---

## Documentation

Update `docs/cas-server-documentation/**` for any user-facing change.

---

## Security guidance

Do not blindly accept AI-generated changes in:
- Authentication flows
- MFA, crypto, ticket validation
- Authorization or access strategies

Require tests and careful review.

---

## What Copilot should NOT do

- Add dependencies without justification
- Reformat unrelated code
- Skip tests/checks
- Replace CAS-specific patterns with generic ones

---

## PR checklist

- Builds locally
- Tests added/updated
- Conventions followed
- Docs updated
- Scope is focused
