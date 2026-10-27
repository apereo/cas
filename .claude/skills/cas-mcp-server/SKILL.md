---
name: cas-mcp-server
description: "Review and extend the CAS MCP server (support/cas-server-support-mcp): @McpTool beans, security and auditing, optional tool groups, typed results and output schemas, secret redaction, resources and completion, tests and the mcp-server-* scenarios. Use when adding or changing MCP tools, resources or prompts."
---

# CAS MCP server module

## MCP server module (`support/cas-server-support-mcp`)

- Tools are plain beans whose methods carry `@McpTool` plus `@PreAuthorize("hasRole('MCP_ADMIN')")`;
  `CasMcpToolsTests.verifyToolsAreSecuredByProxies` asserts every such bean is an AOP proxy. Read tools need `MCP_ADMIN`.
- Tools that change CAS (`AI.mcp-write`, off by default) live in separate `*AdministrationTools` beans so the toggle can drop
  them: core ones in `CasAdministrationTools` (sessions with SLO through `SingleLogoutRequestExecutor`, throttle release via
  `ThrottledSubmissionsStore.removeIf`, `setServiceEnabled` on `DefaultRegisteredServiceAccessStrategy` including chained
  ones, `setLogLevel` through Spring Boot's `LoggingSystem`), optional ones as extra `@Bean` methods in the group's nested
  configuration, or a nested configuration of their own (`CasMcpOAuthAdministrationConfiguration`). Each bean method carries
  `@ConditionalOnFeatureEnabled(module = "mcp-write", enabledByDefault = false)`, each tool `hasRole('MCP_OPERATOR')`,
  `readOnlyHint = false` and `destructiveHint = true` unless harmless. They accept session references and record ids from
  the read tools, never raw TGT or token ids. Tests: `CasMcpToolsTests` turns `mcp-write` on (bean level, `MCP_OPERATOR`
  mock user, Mockito `verify` on the mocked MFA repositories), `CasMcpServerTests.WriteToolsTests` (MockMvc, `mcpoperator`,
  tokens), and `ProtocolTests` asserts they are not listed by default. Checkstyle rejects `public LoggerXxx` and a parameter
  named `logger` (non-static logger rule).
- A tool over an optional feature follows the consent pattern: `compileOnly` (and `testImplementation`) on that module, a
  nested `@Configuration` with `@ConditionalOnClass` and feature module `mcp-<group>`, an `ObjectProvider` for the backing
  bean and a `reason` in the result when it is absent; test activation in `CasMcpAutoConfigurationTests`.
- Never return secrets: device fingerprints, trusted-device record keys, client secrets, key material, setting values.
  Tests assert the secret string is absent from the result.
- A turned-off events repository is a `BeanSupplier` proxy, not null: check `BeanSupplier.isProxy`.
- Every `@McpTool`/`@McpResource` call is audited by one infrastructure advisor (`CasMcpAuditInterceptor`, ordered before
  method security so denials are recorded) that hands a `CasMcpOperation` (an `AuditableEntity`, principal = the MCP
  user) to the `@Audit`-annotated `CasMcpAuditor`. New tools are covered automatically; never add `@Audit` to tool
  methods (the default audit principal resolver would take the first argument, the person being looked up).
- Tools summarize; they never return serialized CAS objects (tickets, services' secrets) or attribute values outside
  findPerson/previewAttributeRelease. Ticket-granting ticket ids are bearer tokens: return
  `CasMcpSessionReferences.of(id)` and accept references back through `CasMcpSessionReferences.resolve` (with the
  username). Service JSON from findService/matchService goes through the secret redaction in `CasServiceManagementTools`.
- MCP capabilities are set in `webapp/cas-server-webapp-resources/src/main/resources/application.properties`
  (`spring.ai.mcp.server.capabilities.*`) and mirrored in `BaseCasMcpTests`; a `@McpResource` or `@McpPrompt` bean is
  silently not advertised while its capability is false. Resources (`org.apereo.cas.mcp.resources`) return JSON built
  with a plain `JsonMapper`: CAS's `JacksonObjectMapperFactory` mappers use `NON_DEFAULT` inclusion and drop `false`/`0`.
- `DefaultAuthenticationRiskEvaluator` is `@Transactional` on `transactionManagerEvents`; test contexts that use a real
  evaluator need that bean (`PseudoTransactionManager`, as `CasCoreEventsAutoConfiguration` registers it).
- Tools that evaluate a person at an application build their subject with `CasMcpEvaluationContextFactory.build`
  (service, matching registered service, resolved principal, `authentication()`); request-aware components get
  `currentHttpExchange()`, the live request with parameters, headers, cookies and query string hidden, or
  `newWebflowRequestContext(context)`, a webflow request context over it with service, registered service, credential
  and authentication in scope. Do not resolve services or principals by hand in a tool. Tools that need client details
  (IP address, user agent, location) take them as parameters and wrap them in `CasMcpClient`: `ClientInfoHolder`
  describes the MCP operator. `currentHttpExchange(client)` exposes them as remote address, `User-Agent` and the
  `geolocation` parameter; `withClientInfo(client, ...)` swaps `ClientInfoHolder` only around components that read it
  (MFA triggers). Do not swap it around audited calls such as the risk evaluator, or the audit trail records the
  supplied address instead of the operator's.
- A new tool also goes into `CAS_TOOLS` (`CasMcpServerTests`, `mcp-server-tools/script.js` when core), the role-denial
  lists, `FeatureDisabledTests`, and the tables and feature-toggle list in `Admin-MCP-Server.md`. A new optional group also
  goes into `CasDeploymentTools.MCP_TOOL_GROUPS` (feature module to bean name), which `describeDeployment` reports.
- Tools return typed records with `@McpTool(generateOutputSchema = true)` (only findService/matchService return free-form
  JSON strings): tools/list then carries an `outputSchema` and calls return `structuredContent`, which the MCP SDK validates
  against the schema and copies into the text content. Mark optional components `@Nullable` (jspecify) and annotate the
  record `@JsonInclude(NON_NULL)`: non-null components are `required` in the schema and a `null` fails validation. Never
  return `null` from a typed tool; return the record with a `reason`. Keep JSON names stable (puppeteer reads them), use
  `String` for dates, `Object` for nested CAS objects whose shape should not become schema, `@JsonProperty` for Java
  keywords such as `default`, and Lombok `@Builder` for records with many optional components. Bean-level tests use the
  record accessors.
- A tool that combines others (explainLogin) calls the other tool beans and converts their records to maps with a plain
  `JsonMapper`. `CasMcpAuditInterceptor` binds a `ScopedValue` for the outermost call, so nested tool calls are not audited.
- Puppeteer builds CAS with `-DskipNestedConfigMetadataGen=true`, so the settings catalog at runtime holds only third-party
  (Spring) settings and no `cas.*` ones. Scenarios exercise settings tools with a Spring setting such as
  `spring.ai.mcp.server.name`; `cas.*` settings and the `module` filter are covered by unit tests (`McpSettingsMetadata.json`).
- MCP completion (`@McpComplete`) only completes resource-template and prompt arguments, never tool parameters. Completion
  methods carry `@PreAuthorize` like tools and are not audited; `spring.ai.mcp.server.capabilities.completion` must be true.
- Claude's shells have no JDK 25 or Maven Central; `build/mcp-run.sh` compiles, runs checkstyle, the `RestfulApi` tests
  and the MCP puppeteer scenarios on the Mac.
- OAuth resource checks use `OAuth20RequestParameterResolver.resolveAllowedResources(service)`, which adds the CAS-wide
  `cas.authn.oauth.core.allowed-resources` (default: the CAS MCP endpoint) to the service's `allowedResources`; never
  match against `getAllowedResources()` alone. `AbstractOAuth20Tests` and `BaseCasMcpTests` allow `https://cas.example.org/cas/mcp`.
- Bearer access tokens (`AI.mcp-oauth`, nested `CasMcpOAuthConfiguration`, needs OIDC for the issuer): the filter
  `CasMcpBearerTokenAuthenticationFilter` is built inside a `CasWebSecurityConfigurer`, never exposed as a bean (Spring Boot
  registers `Filter` beans for every request). It sits before `BasicAuthenticationFilter`, acts only on the MCP path with a
  `Bearer` header, sets a `CasMcpBearerTokenAuthentication` (principal id, client id, scopes) without saving it, and grants
  `ROLE_MCP_ADMIN` only from `cas.mcp.oauth.required-attributes` (empty matches nobody), and `ROLE_MCP_OPERATOR` only with the
  `mcp:write` scope plus `cas.mcp.oauth.operator-required-attributes` (`grantRoles`), checked against attributes resolved
  through `defaultPrincipalResolver` (token principal as current principal): a token's principal carries only the attributes
  released to the client, so `memberOf` and the like are usually missing there. The unauthenticated challenge comes from
  `defaultAuthenticationEntryPointFor(..., mcp matcher)`, registered before `httpBasic` initializes so it wins. Being first
  also makes it Spring Security's fallback for requests no entry point claims (browser requests to actuators), so outside
  the MCP path it answers with the Basic challenge alone. A configurer must never set an explicit `authenticationEntryPoint`,
  which replaces every other one; Palantir's CAS login registers its entry point with `defaultAuthenticationEntryPointFor`
  for `/palantir/**` only (`DashboardControllerTests.CasAuthenticationTests`). The security context is saved in a request attribute only (async SSE dispatches need
  it), never in the HTTP session.
- Spring Security's Basic challenge is `Basic realm="Realm", charset="UTF-8"` (MockMvc reports it as more than one value), so
  `CasMcpAuthenticationEntryPoint` delegates to `BasicAuthenticationEntryPoint` rather than writing it, and tests compare with
  that entry point's output (`CasMcpServerTests.basicChallenge`). `jsonPath(...).value(List.of(...))` reads `null` because the
  expected value's class (`List.of`'s immutable type) becomes the target type; assert `[0]` and `length()` instead. Protected resource metadata lives under the CAS
  context (`/cas/.well-known/oauth-protected-resource/mcp`), because the root well-known path is outside the web app; clients
  reach it through `resource_metadata`. Tests build `OAuth20DefaultAccessToken` directly and add it to the registry
  (`CasMcpServerTests.newAccessToken`); `mcp-server-saml-oidc` gets a JWT token through the password grant.
- `CasMcpEvaluationContextFactory` hands the username to the principal resolvers as the current principal. Without attribute repositories the chain holds only `EchoingPrincipalResolver`, which echoes the current principal, so resolving without one yields `NullPrincipal` (`nobody`).
- OAuth/OIDC services serialize their secret as `clientSecrets` (a list of objects with `value`); `getClientSecret()` is `@JsonIgnore`. Redaction of service definitions must cover the plural property, which `SECRET_PROPERTY` in `CasServiceManagementTools` does with `secrets?`.
- `CasOidcTools.checkOidcClientResource` reuses `RegisteredServiceOAuthResourceMatcher` (`isValidResource`,
  `findAllowedResource`) so the tool and the protocol endpoints decide resource indicators the same way; new tools are
  added to `CasMcpServerTests.CAS_TOOLS` and to the optional-tool lists of the `mcp-server-*` scenarios.
