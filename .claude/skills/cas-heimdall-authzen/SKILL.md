---
name: cas-heimdall-authzen
description: "Review and change Heimdall authorization and its AuthZEN PDP endpoints (support/cas-server-support-heimdall*): request parsing, caller authentication, policies (JDBC, JSON resources), evaluations, PDP metadata, throttling and the heimdall-authzen/heimdall-nginx scenarios. Use when touching Heimdall code, docs or scenarios."
---

# Heimdall and AuthZEN

## Heimdall / AuthZEN review discipline

- Check the latest OpenID Authorization API final specification and editor's draft. AuthZEN is a PEP/PDP
  protocol; wallet exchange uses OID4VCI/OID4VP. Treat a verified-presentation-to-policy bridge as a feature.
- Trace `DefaultAuthorizationPrincipalParser` through `DefaultAuthorizationEngine` and each policy.
  A valid caller token does not establish PDP audience, PEP permissions or DPoP proof possession;
  delegated subject ids are legitimate for trusted PEPs and are not inherently impersonation.
- An AuthZEN `resource.id` names the resource instance, never a policy namespace. AuthZEN requests
  (`AuthorizationRequest.isAuthZen()`: subject, resource and action all present) match `resourceType`, `actions` and an
  optional full-match `resourceIdPattern` across every namespace, and every match must grant. Normal requests
  keep namespace + URI pattern + method and `/heimdall/authorize` rejects AuthZEN fields, which keeps the paths apart.
- Callers authenticate with tokens issued to a registered OAuth/OIDC service whose access strategy allows
  access; `HeimdallRegisteredServiceAccessStrategy` (alone or chained) can refuse it. DPoP and `x509_digest`
  certificate bindings are enforced. `x509_digest` is the RFC 8705 `x5t#S256` thumbprint (base64url SHA-256 of the
  DER certificate); compute it only with `OAuth20Utils.computeCertificateThumbprint` so issuer and verifiers agree. On AuthZEN, Basic means `client_id:client_secret` (reject clients without secrets:
  `DefaultOAuth20ClientSecretValidator.validate` returns true when none is defined); on `/heimdall/authorize` it
  stays CAS user credentials, split on the first colon. Both endpoints are throttled; `HeimdallThrottledHandlerInterceptor`
  forwards only 401s to the throttle interceptors' post-processing. A `ThrottledRequestFilter` cannot limit what
  counts: the default `httpPost()` filter claims every POST and the plan combines filters with `anyMatch`, and the
  interceptors record every non-2xx response (twice: `postHandle` and `afterCompletion`; harmless for decisions,
  since stores keep the latest failure per key or read the authentication audit trail).
  Create registry-backed single-use markers through the `TicketFactory` (TST factory, custom `ExpirationPolicy` in
  a *mutable* properties map: `buildExpirationPolicy` removes that key, so `Map.of` throws), never by instantiating
  ticket implementations.
- macOS JDKs use a polling `WatchService` (2s): a file created and deleted between two polls produces no event.
  Watcher-driven caches must reconcile against the file system on every event rather than trust event kinds. JWT assertions need `jti`/`iat`, are single-use,
  and share the token endpoint's audiences on purpose. A resource without policies denies.
- JDBC trusted parameters (`principal`, `method`, `uri`, `namespace` and the AuthZEN names) are added after
  context and attributes, so they already win; an overwrite claim there was a false finding. Check that policy
  file deletion removes cached grants. `enforceAllPolicies=false` means any one policy grants: a failing policy is
  logged and skipped, and failures are rethrown only when nothing grants (never turn them into a silent deny).
  `enforceAllPolicies=true` stops at the first denial or failure. An empty policy list denies before either runs.
- JDBC policies resolve their `DataSource` by bean name (`dataSourceName`, else `heimdallJdbcDataSource-<sha256(url|username)>`)
  and register a `JpaBeans.newPoolingDataSource` pool (Hikari defaults, `minimumIdle=0`) through
  `GenericApplicationContext.registerBean` under a lock, so Spring closes it on shutdown. Never cache a pool on a
  policy instance (policies are rebuilt on every file reload) and never use `registerSingleton` for closeable beans:
  Spring gives registered singletons no destruction callbacks. The pool key ignores the password (a rotated password
  needs a new `dataSourceName` or a restart; documented). `queryTimeout` (default `PT5S`) is set on the `JdbcTemplate`. Tests override `resolveApplicationContext()` with a
  local context rather than relying on the static `ApplicationContextProvider`, which parallel tests share.
- `JsonAuthorizableResourceRepository` publishes one immutable `ResourceIndex` (by namespace and by AuthZEN
  `resourceType`) per reload; AuthZEN lookups use the type index, so keep both maps in the same snapshot.
- Policies evaluate sequentially; do not reintroduce `parallelStream()` (blocking policies starve `commonPool`).
- AuthZEN subjects are resolved through the principal resolver only for subject type `user` (hardcoded);
  other types become a bare principal. Policies read request data through
  `AuthorizationRequest.resolveAttributeValues` (qualified `subject.*`, `resource.*`, `action.*`, `context.*` names);
  never merge caller-supplied properties into principal attributes, which would let a PEP override the directory.
  AuthZEN `context` is body-only; `/heimdall/authorize` adds non-protocol headers with `putIfAbsent`.
- Palantir rebuilds resources from a fixed field list (`heimdallResourceForStorage`) and re-saves the whole
  namespace; any new `AuthorizableResource` field must be added there or an edit silently drops it.
- AuthZEN PDP metadata: the identifier is `<prefix>/heimdall`, served at `/cas/heimdall/.well-known/authzen-configuration`
  (`HeimdallAuthZenConfigurationController`). The spec inserts the well-known segment after the host
  (`/.well-known/authzen-configuration/cas/heimdall`), outside the CAS context: deployments need a proxy or ENGINE
  rewrite-valve rule, as in the `heimdall-authzen` scenario's `rewrite.config`. `policy_decision_point` must equal the
  identifier the PEP started from. List only endpoints that exist (no search yet).
- AuthZEN decision context: denials carry `context.reason` from `AuthorizationDecisionReason` (set by the engine:
  `subject_unresolved`, `no_matching_resource`, `no_policies`, `policy_denied`); grants carry no context. Never put
  policy names, attribute names or messages in it; details go to DEBUG logs. Search (R5) and the OID4VP wallet bridge
  (R7) are deferred and noted as future work in the Heimdall and verifiable credentials docs.
- AuthZEN evaluations (`/heimdall/authzen/evaluations`): top-level subject/resource/action/context are defaults that an
  entry replaces per key. Authenticate the caller once (`authenticateCaller`), then `resolveSubject` per entry;
  never call `parse` per entry (it would consume single-use JWT assertions). Per-entry failures are `decision:false` with
  `context.error.{status,message}` (no exception text for 500s); short-circuit semantics omit the remaining entries;
  no/empty `evaluations` falls back to the single evaluation. Evaluations run sequentially.
- AuthZEN evaluated denials use HTTP 200 with `decision:false`; authentication failures use 401.
  Discovery/batch/search are separate capabilities, and the specification's example endpoint path is not mandatory.
- Read shared helpers before reporting leaks: request headers already filter credentials and the request
  principal is JSON-ignored. Confirm performance severity with evidence; blocking policy parallel streams
  and unpooled JDBC merit investigation, not an unmeasured claim of outage.
- Puppeteer scenario `heimdall-authzen` covers the AuthZEN endpoint end to end (decisions across namespaces,
  id patterns, deny-wins, empty policies, `X-Request-ID`, unknown fields, 400/401, the Heimdall access strategy,
  client-credential and bearer PEPs, the legacy endpoint, the actuator and throttling). It also enables throttling,
  so keep 401-producing steps at least one throttle window apart. Palantir scenarios only check that tabs load.
- Scenario `heimdall-nginx` drives the shared nginx config (`ci/tests/nginx`) as a PEP: `/api` uses `auth_request`
  against `/heimdall/authorize`. The subrequest must set `proxy_method POST` (it is a GET otherwise) and send a
  `namespace`; nginx does not escape variables in `proxy_set_body`, so URIs with `"` or `\` are rejected by a `map`.
  `auth_request` maps any status other than 2xx/401/403 (such as Heimdall's 404) to 500. Validate edits with a local
  `nginx -t` and stub upstreams; the config resolves `host.docker.internal` at load time.
