---
name: cas-actuators
description: "Review and change CAS actuator endpoints: the security filter chain, access levels and IP access, REST actuator endpoints, exposure vs access, unmasked responses, request-path regexes, auditing and the actuator scenarios. Use when touching actuator endpoints or management security."
---

# Actuator endpoints

## Actuator endpoints review discipline

- There are ~63 endpoints. Inventory them with
  `grep -rl --include=*.java -E "@(RestControllerEndpoint|ControllerEndpoint|Endpoint|WebEndpoint)\(id"`;
  `support/cas-server-support-reports-core` owns about a third, the rest are scattered across
  feature modules. Review them as one attack surface, not one module at a time.
- The whole security boundary is a single `SecurityFilterChain` built by
  `CasWebSecurityConfigurerAdapter.configureHttpSecurity`. Rules are added in order --
  ignored patterns (permitAll) -> each `CasWebSecurityConfigurer.configure` -> per-endpoint
  `cas.monitor.endpoints.endpoint.*` -> `configureEndpointAccessToDenyUndefined` -> static
  resources -> form login -- and Spring Security takes the first match, so anything a
  `getIgnoredEndpoints` contributes wins over every endpoint rule that follows.
- `prepareProtocolEndpoint` emits `/<ep>**` as well as `/<ep>/**`. The first form is a
  within-segment wildcard, so an ignored endpoint also permits sibling paths sharing its
  prefix. Check that before adding an entry.
- That chain disables security headers and logout globally, and CSRF everywhere except the paths that configurers
  declare through `getCsrfProtectedEndpoints()` (CIBA verification, WebAuthn). It is the same chain that serves the
  actuator. Any browser-reachable session credential (`cas.monitor.endpoints.form-login-enabled`) therefore leaves
  mutating actuator operations unprotected against cross-site requests.
- `ActuatorEndpointProperties.access` defaults to `DENY` and the `endpoint` map always
  carries a synthetic `defaults` key. The adapter iterates the map and calls
  `EndpointRequest.to("defaults")`, which resolves to no path and silently registers nothing;
  the real default comes from `configureEndpointAccessToDenyUndefined`. Do not read the loop
  as the place defaults are enforced.
- `configureEndpointAccessToDenyUndefined` reads `management.endpoint.<rootPath>.access` with
  a raw `Environment.getProperty` call and maps `UNRESTRICTED`/`READ_ONLY` to `permitAll`.
  Relaxed spellings of that key are not seen (fail-closed), but the canonical spelling turns a
  Boot-level exposure decision into anonymous Spring Security access.
- `IP_ADDRESS` access is evaluated by `IpAddressAuthorizationManager`, which reads
  `cas.audit.engine.alternate-client-addr-header-name` (default `X-Forwarded-For`) straight off the
  request. This is intentional and is NOT a CAS defect: the fronting proxy, load balancer and
  external boot configuration are expected to set that header as a trusted header, or to blank it
  out and let Tomcat's own valves resolve the client address. Configuring that correctly is left to
  the deployer, so do not report header spoofing here as a finding.
- `BaseCasRestActuatorEndpoint` endpoints are NOT Spring Boot operations. They are discovered by
  CAS's own `RestActuatorEndpointDiscoverer`, mapped by `RestActuatorEndpointHandlerMapping`, and
  their handlers are Spring MVC mappings rather than `@ReadOperation`/`@WriteOperation` methods.
  Boot applies an access level by filtering operations, and these endpoints declare none, so
  `management.endpoint.<id>.access=READ_ONLY` does not stop their `@PostMapping`, `@PutMapping`,
  `@PatchMapping` or `@DeleteMapping` handlers the way it stops a Boot write operation. That is
  the gap `RestActuatorEndpointHandlerMapping.isMappingPermittedByAccessLevel` closes: it resolves
  the endpoint's access through the `EndpointAccessResolver` bean and applies the same rules Boot
  applies to operations, registering nothing under `NONE` and only `GET`/`HEAD`/`OPTIONS` mappings
  under `READ_ONLY`. Note that `configureEndpointAccessToDenyUndefined` maps `READ_ONLY` to
  `permitAll`, which is why the write methods used to get through, and maps `NONE` to `denyAll`, so
  a `NONE` endpoint is usually refused by security before the missing mapping matters.
  `ActuatorEndpointAccessControlTests` covers all of it, with `releaseAttributes` beside
  `registeredServices` as the Boot operation endpoint that has always behaved this way.
- Before changing what an access level does to these endpoints, measure the blast radius rather
  than reasoning from the annotation: every REST actuator endpoint declares
  `defaultAccess = Access.NONE` and the web application ships `management.endpoints.access.default=NONE`,
  which suggests widespread breakage, but in fact all 44 test classes and all 69 puppeteer scenarios
  that call one already declare `management.endpoint.<id>.access` or `management.endpoints.access.default`
  explicitly. Walk the test tree and `ci/tests/puppeteer/scenarios/*/script.json` and count.
- Do not confuse that with exposure. A REST actuator endpoint left out of
  `management.endpoints.web.exposure.include` answers `403`, not `200`: no rule is registered for
  it in `configureEndpointAccessToDenyUndefined`, and Spring Security denies a request no matcher
  claims. Verify the status a given combination of exposure and access actually produces before
  describing any of it as reachable.
- When `management.server.port` differs, `CasCoreWebManagementContextConfiguration` registers the
  handler mappings in the child management context but no `SecurityFilterChain`. Verify what
  actually secures that port before assuming the main chain applies.
- Several endpoints are authentication primitives, not diagnostics: `casValidate`,
  `samlValidate`, `samlPostProfileResponse` and `tokenAuth` mint CAS assertions, signed SAML
  responses and JWTs for an arbitrary username, and the `password` parameter is optional (absent
  entirely for `tokenAuth`) -- with no password they fall through to `PrincipalResolver` and
  succeed. Anything that widens actuator access widens impersonation.
- These endpoints return configuration values and service definitions unmasked, unlike Boot's own
  `/env` and `/configprops`. `casConfig` `/retrieve` returns raw property values and
  `registeredServices` serializes `OAuthRegisteredService.clientSecrets` (the `value` field carries
  no `@JsonIgnore`). Masking was proposed and rejected, so do not re-propose it without agreeing
  the approach with the maintainer first. `casConfig` `/encrypt` and `/decrypt` are a deliberate
  operator tool that Palantir exposes in its UI, not an oracle to close.
- Before proposing masking on any actuator response, work out how the admin UI consumes it: grep
  `support/cas-server-support-thymeleaf/src/main/resources/static/js/palantir-*.js`. Palantir's
  service editor does a full `GET /registeredServices/{id}` -> edit -> `PUT` round-trip
  (`palantir-services.js`), so masking any path it reads would write the mask back into the
  registry, and `/export` has the same requirement; redacting there needs the save path to preserve
  existing secrets first. `casConfig` `/retrieve` does not have that problem, because
  `palantir-pac4j.js` reads only `name` and `propertySource` from it.
- User-supplied strings are compiled into regular expressions on the request path:
  `configurationMetadata/{term}` (then matched over every known property in a `parallelStream`)
  and `casConfig` `/retrieve`. Treat these as ReDoS/CPU-exhaustion boundaries; actuator endpoints
  are not covered by CAS's authentication throttling.
- No actuator endpoint carries `@Audit`. Session destruction, service deletion, key rotation,
  configuration mutation and token minting all happen without an Inspektr record. Check for this
  before claiming an administrative action is traceable.
- Puppeteer coverage: `actuator-endpoint-cors`, `actuator-endpoint-login-jdbc`,
  `actuator-endpoint-management`, `actuator-endpoint-metrics`, `actuator-endpoint-reports`,
  `actuator-endpoint-roles`, `actuator-form-login`, `actuator-form-login-ldap`,
  `actuator-form-login-ldap-roles`, `actuator-form-login-ldap-userauthz`,
  `configuration-refresh-endpoint`, `sso-sessions-endpoint`, `tomcat-forwarded-headers`
  (the only `IP_ADDRESS` scenario). Every one of them asserts that an *authorized* caller gets
  200; none asserts 401/403 for an unauthenticated one, and 158 scenarios run with
  `management.endpoints.web.exposure.include=*`. A change to endpoint security therefore needs a
  new negative scenario rather than a rerun.
