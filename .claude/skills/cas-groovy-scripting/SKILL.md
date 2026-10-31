---
name: cas-groovy-scripting
description: "Review and change Groovy scripting support (core/cas-server-core-scripting): ScriptResourceCacheManager, CompiledGroovyScript, inline vs external scripts, compiling off the request path, script security. Use when touching Groovy scripts or any module that runs them."
---

# Groovy and scripting

## Groovy and scripting

- Start at `core/cas-server-core-scripting`; roughly fifty modules delegate to the same five classes, so
  changes there are changes to authentication, attribute release, MFA, AUP, OIDC, SAML and themes at once.
- `ScriptResourceCacheManager` is a singleton whose `close()` clears the entire cache — it must never be
  used in a try-with-resources block.
- `CompiledGroovyScript.of` decides how a script runs: a stateless script (only final fields holding immutable
  values) gets a fresh instance per call and runs in parallel; a stateful one keeps a shared instance serialized
  on a blocking lock, on purpose, so its fields stay consistent. Do not report that lock as a bottleneck. Review every script-backed
  decision for what a `null` result means and prefer fail-closed defaults at security boundaries — MFA
  triggers and AUP read `null` as "skip". Never guard script execution with a timed `tryLock`; a timeout
  that returns `null` is indistinguishable from a script that declined.
- Do not compile scripts on a request path (`fromScript`/`fromResource` per call). Go through
  `resolveScriptableResource`, or hold the compiled script in a transient field, and remember `fromResource`
  also starts a file-watcher thread that nothing closes. The cache manager comes from
  `ApplicationContextProvider` and is absent outside Spring, so always fall back to the factory instead of
  throwing, or you will break components and tests that are constructed directly.
- Inline (`groovy { ... }`) and external (`file:`/`classpath:`) scripts behave differently: only external
  scripts honour `failOnError` and support reload, and only inline scripts support bindings (the
  `setBinding` interface default is a no-op). Inline scripts swallow `GroovyRuntimeException`. Test both
  forms, and cover bindings with concurrent and pooled-thread tests — `GroovyShellScriptTests.BindingTests`
  is the pattern.
- Treat compiling a Groovy script as running it: AST transformations execute inside the compiler. Script
  text that comes from anywhere other than deployment configuration must be parsed
  (`ScriptingUtils.validateGroovyScript`), never compiled.
- Puppeteer coverage lives in the 18 `ci/tests/puppeteer/scenarios/*groovy*` scenarios
  (`service-access-strategy-groovy`, `surrogate-login-groovy`, `mfa-provider-selection-trigger-groovy`,
  `interrupt-afterauthn-groovy`, `webflow-groovy-action`, …). None of them exercise concurrency, script
  errors, or cache expiry, so a change in this area needs new coverage rather than a rerun.
