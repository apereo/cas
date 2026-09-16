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
- Unalias Linux/macOS commands before you run them, specially `tree`, `find`, `grep`, `cat`, etc.
- From a sandbox that cannot delete files, run read-only git commands with `GIT_OPTIONAL_LOCKS=0` (for example `GIT_OPTIONAL_LOCKS=0 git status`); otherwise git can leave a stale `.git/index.lock` that blocks the user's git.
- Consider using StringUtils.EMPTY instead of "" for empty strings, and StringUtils.isNotBlank() instead of != null && !isEmpty() for string checks.

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

## Security-sensitive change discipline

- Verify a security report against the complete execution path before changing code. Identify the attacker-controlled input, the trust decision, and the exact point where validation or isolation is bypassed.
- Keep security fixes at the narrowest shared enforcement point. Preserve unrelated protocol behavior and avoid adding parallel validation, crypto, or metadata abstractions.
- Treat caller-supplied protocol values as untrusted even if CAS later signs the containing message. Internal signing proves CAS produced the final message; it does not authenticate the caller's original value.
- A presented signature must be cryptographically and algorithmically validated whenever it is used as proof, independently of whether metadata requires requests to be signed. Apply the same resolved security parameters to every binding before raw cryptographic validation.
- For unsolicited SAML flows, accept ACS destinations only from the applicable registered-service metadata. For solicited flows, a request-supplied ACS outside metadata requires an actually authenticated request signature.
- Build credentials from matching certificate/private-key material belonging to the message recipient. Never combine a peer's public key with CAS's private key.
- `SubjectConfirmationData.Address` identifies the presenter, not the ACS server. Use trusted proxy-aware client information or omit it; never perform a synchronous DNS lookup of the ACS host while building assertions.
- Scope caches that contain service-specific metadata, keys, validation filters, or trust policy to the registered-service boundary. Address entries by their complete key; do not scan unrelated cached values for a matching entity.

## Test, concurrency, and hand-off expectations

- Reuse the nearest existing test class whenever practical. A security regression should fail on the vulnerable implementation and exercise the actual trust boundary, not merely assert an implementation detail.
- Test web endpoints through `MockMvc` or the existing web-test infrastructure. Never instantiate controller classes directly in tests.
- Prefer real protocol artifacts for crypto tests: sign/encrypt with matching test credentials, serialize and unmarshal when validating embedded XML, and include negative cases for tampering, blocked algorithms, or mismatched destinations.
- Keep tests deterministic and independent of external DNS or network availability. Use loopback addresses, local mock servers, classpath resources, and uniquely named temporary files; clean up local resources in `finally` or try-with-resources blocks.
- Changes and tests must tolerate Gradle parallel mode. Avoid shared mutable static state, fixed temporary filenames, cross-test cache assumptions, and mutation of shared application state when a local fixture will work.
- Never add the Java `synchronized` keyword. When mutual exclusion is genuinely required, use `CasReentrantLock` and its execution helpers consistently with nearby CAS code.
- If the user requests a `PLANS.md` plan, create it before implementation work and check off each step as it is completed.
- When code cannot be compiled locally, check overridden and called signatures for checked exceptions (for example `ByteArrayResource.getInputStream()` declares `IOException`) and reuse helpers' existing filtering instead of repeating it.
- Honor explicit verification boundaries. If the user asks not to run tests, do not invoke tests or Gradle tasks; perform static review such as `git diff --check` and clearly report what was not run.
- For release-bound security work, add one brief, user-facing note to the appropriate security/protocol section of the requested release-notes file after the implementation is complete.
- For all changes, cross check with puppeteer scenarios and make sure they continue to pass and are adjusted correctly.

## Practical boundaries

- Put new behavior in the narrowest module that already owns that concern; do not skip from `webapp/` straight into backend-specific code when an `api/` or `core/` seam already exists.
- When adding configuration, update the config model class first; otherwise metadata/docs generation will not understand the new property.
- For service-aware logic, look for `ServicesManager.findServiceBy(...)`; for ticket-aware logic, look for `ticketRegistry.getTicket(...)`. Those seams are used repeatedly across `core/` and `support/` and are usually the right integration points.
- Treat authentication, tickets, webflow, logout, MFA, and crypto as security-sensitive areas. Match existing CAS utilities and flows instead of introducing parallel mechanisms.
- Keep diffs surgical: this codebase already has strong patterns, so the fastest path is usually “copy the nearest module family pattern and adapt it” rather than inventing a new abstraction.

## OIDC verifiable credentials (OID4VCI / OID4VP)

- The OID4VCI Nonce Endpoint must stay publicly reachable (OID4VCI 1.0, section 7.1: it is not a protected resource). Do not add it to the interceptor's protected list; rate limiting or throttling is the appropriate control there. The presentation request creation endpoint is CAS's own verifier API and should be protected.
- A credential offer's `tx_code` is optional in the protocol but mandatory once the offer declares one. Keep it independent of any value the caller already holds, and leave a way to disable it for wallets that collect no user input, otherwise the walt.id puppeteer scenario cannot redeem an offer.
- Verify a JWT proof's `typ` header as well as its signature. A proof or key-binding JWT that is only checked for signature, audience and freshness can be satisfied by a token minted for a different protocol.
- When reviewing these flows, confirm the normative text against the current published OID4VCI, OID4VP and SD-JWT VC specifications rather than from memory; the drafts changed substantially before 1.0.
- The issuance wire format is OID4VCI 1.0: the credential request takes `proofs` (plural) plus either `credential_configuration_id` or `credential_identifier` (mutually exclusive), and the response is `{"credentials":[{"credential":...}]}` with no `format` anywhere. A batch is one request carrying several proofs, yielding one credential per proof of the same configuration; there is no batch endpoint, and the metadata advertises `batch_credential_issuance`. The proof challenge comes only from the nonce endpoint, never the token response. A CAS credential identifier is the credential configuration id itself, which is what lets `resolveConfigurationId` treat both request parameters the same way.
- Check which generation of a third-party service CI is pinned to before trusting it as an interop signal. walt.id ships two: the original Issuer/Verifier/Wallet APIs implement the drafts, while Issuer2, Verifier2 and Wallet API v2 (`waltid/wallet-api2`, port 7006) implement the finalized 1.0 specs. Reading the deprecated `waltid-openid4vc` library's models -- a required `format`, a singular `proof` -- and concluding "the vendor cannot do 1.0" is the mistake to avoid; check the vendor's current docs and published images.
- OpenID4VCI and RFC 8414 both locate metadata by inserting the well-known segment between the host and the issuer's path (`https://host/.well-known/openid-credential-issuer/cas/oidc`), not by appending it the way OpenID Connect Discovery does. It applies to the credential issuer metadata and the authorization server metadata alike, so fixing one and not the other just moves the failure one step later. CAS sits under the `/cas` context path, so those URLs never reach the application and the container answers 404 itself; the fix is a rewrite, either in the fronting proxy or in CAS's own Tomcat rewrite valve with `valve-type=ENGINE`, which runs before a context is selected. Scope the rule to the documents CAS publishes rather than all of `/.well-known/`, or it will swallow `/.well-known/acme-challenge/`. This is invisible to any wallet that guesses the appended form, which is why it survived until a spec-correct wallet was used.
- The pre-authorized code grant is unauthenticated by design: CAS reads the `pre-authorized_code` and `grant_type` form parameters through a `DirectFormClient`, and the client comes from the offer, not the request. A wallet chooses its authentication method from discovery metadata, so `token_endpoint_auth_methods_supported` includes `none`, which is now also a constant on `OAuth20ClientAuthenticationMethods`.
- Every value in `token_endpoint_auth_methods_supported` is run through `OAuth20ClientAuthenticationMethods.parse`, which `orElseThrow`s, by all three OIDC client authenticators. Any value the enum does not know turns client authentication into a 400 with a bare "No value present", so adding a method to that list means adding it to the enum in the same change.
- Advertising `none` does not stop a client choosing something else: a wallet picks its own method from the list, and walt.id prefers `private_key_jwt` whenever a JWT method appears there even though it has no client registration. A deployment whose clients are unregistered wallets should advertise only the methods those wallets can actually use; that is configuration, not a CAS defect. Do not read such a failure as the `none` support being broken.
- `script.json` `properties` is an array of command-line arguments, so every element must be a plain `--key=value` string. There is nowhere to put a JSON comment; explain scenario configuration here or in PLANS.md instead.
- Wallet API v2 needs no account, database or front end in CI: authentication is off and persistence is SQLite in the shipped configuration, so the scenario creates a wallet over HTTP and drives `POST /wallet`, `credentials/receive` and `credentials/present` directly. That is why the walt.id scenario no longer opens a browser.
- OID4VP delivery rules interlock rather than stacking: whether a request may be signed follows from the client identifier prefix, and whether it may be served by reference follows from whether it is signed. Check the prefix before reasoning about `request_uri`, and read the spec's own unsigned example, which passes every parameter by value.
- The credential endpoint is a protected resource, not a token endpoint, and the two speak different error vocabularies. A token problem is a 401 with a `WWW-Authenticate` challenge (`invalid_token` per RFC 6750 section 3.1, and the challenge is mandatory on any 401 per RFC 9110 section 15.5.2, named only when a token was actually presented); a request problem is a 400 with one of OpenID4VCI's own codes -- `invalid_credential_request`, `unsupported_credential_type`, `credential_request_denied`, `invalid_proof`, `invalid_nonce`. Reaching for `OAuth20Constants.INVALID_REQUEST` here is the reflex to resist.
- `invalid_proof` and `invalid_nonce` are not interchangeable: a wallet told its nonce is stale fetches a new one and retries, while a wallet told its proof is invalid stops. Carry the distinction on the exception (`OidcVerifiableCredentialProofException`), not in the message text, or it is lost the moment the exception crosses a layer.
- An endpoint that answers the same error for a stolen token, an unknown credential type and a bad proof is not merely unhelpful; it is untestable from the client side, which is how these three defects survived together.
- `getAccessTokenFromRequest` accepts the access token from an `access_token` or `token` request parameter as well as the authorization header, and that is deliberate. RFC 9700 section 4.3.2 forbids *clients* from using the query-parameter method; it places no requirement on the resource server, and the maintainer's position is that how a caller presents its token is the caller's business. Do not propose closing it as a CAS defect.
- An `OAuth20AuthorizationResponseBuilder.supports` that returns false does not reject anything; it hands the request to the next builder, which succeeds without whatever this one would have added. Conditions that belong in an error path must not live there. The VC builder gated on client type and PKCE and so dropped `authorization_details` for every client it excluded, with the failure surfacing at the credential endpoint much later. Keep `supports` to "is this request mine", and report refusals from `build`.
- Overriding `build` on a response builder loses the parent's `@Audit` annotation unless it is repeated on the override.
- Single-use state is consumed by the delete, not by the read before it. `TicketRegistry.deleteTicket` returns a count every registry derives from an atomic removal (`map.remove`, Redis `DEL`, JPA affected rows), so `delete(...) > 0` is a compare-and-swap and the only caller seeing a non-zero count is the winner. A `getTicket` ahead of it is a pre-check, not the decision -- which is why the nonce service is safe despite looking racy, and why the pre-authorized code was not: it used `update()` plus delete-if-expired, a read-modify-write, and ran after the token was already minted.
- Consume single-use state before the thing it authorizes exists. An `OAuth20AccessTokenGeneratorCustomizer` runs during token generation, so a check there cannot refuse the request; the grant request extractor runs before it and can.
- `OAuth20HandlerInterceptorAdapter.doesUriMatchPattern` matches `/<segment>(/)*$`, so an endpoint is protected only when its path *ends* with the configured segment. Anything with a path variable is public whatever the interceptor lists -- which is intended for the wallet's request object lookup. An endpoint that must be protected takes its identifier as a query parameter.
- Explanation belongs in the javadoc of the method it explains, with its parameters. Do not write inline block comments inside method bodies.
- A verifiable credential is not an ID token. Routing it through `IdTokenSigningAndEncryptionService` makes the client's `signIdToken`, `encryptIdToken` and `idTokenSigningAlg` decide whether a credential is signed, encrypted, and with what -- none of which belong to it. Credentials sign through `BaseOidcVerifiableCredentialEncoder.signCredential`, whose algorithm comes from `credentialSigningAlgValuesSupported`, the same property the verifier checks.
- `BaseTokenSigningAndEncryptionService.signToken` reads a `typ` *claim*, unsets it, and promotes it to the JWS header. Anything that replaces that path must set the header `typ` itself, or the credential loses the media type its verifier requires.
- Review this subsystem at its boundaries, not per class. The interesting defects live where the VC code borrows general CAS machinery: `IdTokenSigningAndEncryptionService` decides how a credential is signed or encrypted, `TransientSessionTicket` and the global `cas.ticket.tst` policy decide how long every offer, code, nonce and presentation request lives, and `BaseOAuth20Controller.getAccessTokenFromRequest` decides where a bearer token may come from.
- Ask who is authorized for what. `cas.authn.oidc.vc.issuer.credential-configurations` says what the issuer can mint, never who may ask for it; a per-service policy is what ties the two together. `OidcRegisteredService.verifiableCredentialsPolicy` is now read through `OidcVerifiableCredentialPolicyUtils.resolveAllowedCredentialConfigurationIds` at all three points where a credential type is claimed -- offer transaction, authorization details, credential endpoint -- and the result is always an intersection with what the issuer publishes, so a policy can narrow a service but never widen it.
- An unread field on a registered service is a finding, not a feature. `verifiableCredentialsPolicy` and `DefaultRegisteredServiceOidcVerifiableCredentialsPolicy` both existed and shipped in the schema while nothing in CAS ever called `getVerifiableCredentialsPolicy`, which reads from the outside exactly like an enforced authorization control. When a policy type exists, grep for its getter before assuming it does anything.
- An empty policy means "no opinion", not "deny everything". Authorization policies here default open when unconfigured, because they are added to deployments that were already working; a policy that denied by default would break every existing service on upgrade.
- CAS is both issuer and verifier here, and the verifier only trusts CAS-issued credentials: `iss` must equal the local issuer, `vct` must map to a local configuration, the signature is checked against CAS's own keystore, and a `status` claim is refused rather than ignored. That is a trust policy, not a defect -- OpenID4VP leaves issuer trust to the verifier ("Verifiers must verify that the issuer of a received presentation is trusted on their own"), and a verifier that cannot evaluate revocation must fail closed. Do not report it as a compliance gap. Widening it means external issuer trust, `x5c`, DID resolution, OpenID Federation and Token Status List fetching, which is a feature with its own configuration surface, not a fix.
- What is worth checking in that code is consistency between the two halves: the verifier should require everything the issuer always emits. `exp` was optional at verification while issuance always stamps it, which let a credential that never expires through.

## Parallel test execution and shared registries

- Most categories in `buildSrc/.../TestCategories.groovy` are declared parallel, and JUnit's default mode there is `concurrent` for classes *and* methods, so sibling `@Test` methods in one class run at the same time against the same Spring context. A test that clears a shared registry wholesale -- `servicesManager.getAllServicesOfType(...).forEach(servicesManager::delete)` in a setup step, say -- deletes what its siblings just saved, and the failure surfaces in whichever method lost the race rather than in the one that did the clearing.
- The symptom to recognize: a test asserting on a service it saved itself gets the value that belongs to the "service was missing" code path. `OpenIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidatorTests` failed exactly that way, reporting `expected: <old-service> but was: <new-service>`, because another method's clear removed the saved service and the validator then resolved a fresh one.
- Isolate by identifier, not by emptying the registry: give each test a UUID-bearing client id and assert only on that id. `@Execution(ExecutionMode.SAME_THREAD)` fixes the within-class case but not another class sharing the context, so prefer removing the global mutation.
- Write registry predicates as `expected.equals(service.getClientId())` rather than the reverse: once the registry is no longer cleared, entries from other tests flow through the same stream.
- The mirror image of that symptom is a test asserting a *negative* that only holds while the registry
  happens to contain no match for its service id. `servicesManager.findServiceBy(<random id>)` returning
  null is a property of the shared registry, not of the test's own fixture, and it stops holding the
  moment a sibling saves a catch-all definition. `SamlIdPServicesManagerRegisteredServiceLocatorTests`
  saves several (`.+`, `callbackUrl + ".*"`) into the registry shared by every `@Tag("SAML2")` class and
  calls `servicesManager.deleteAll()` in its `@BeforeEach`, so it is both halves of this hazard at once.
  `SamlIdPDelegatedClientAuthenticationRequestCustomizerTests.verifyAuthorization` failed on it,
  reporting `expected: <false> but was: <true>` only in a full run.
- Two facts make that failure mode sharper than it looks. `SamlRegisteredService.getEvaluationPriority()`
  returns `0` while `RegisteredService`'s default is `Ordered.LOWEST_PRECEDENCE`, and priority is the
  *first* key in `BaseRegisteredService`'s comparator, so one leftover catch-all `SamlRegisteredService`
  outranks every non-SAML definition for every lookup in that context no matter what evaluation order
  the other test sets. And `BaseRegisteredServiceAccessStrategy` initialises a non-null
  `DefaultRegisteredServiceDelegatedAuthenticationPolicy` whose `permitUndefined` defaults to `true`
  with empty `allowedProviders`, so `isProviderAllowed` answers true for *any* service that resolves.
  Together: "some service matched" is the same as "delegated authentication is permitted", and no
  evaluation order a test can set will win the race.
- So a negative assertion that depends on a registry lookup cannot be made deterministic by registering
  a better-ranked service of its own. Isolate the lookup instead -- construct the component under test
  with a stubbed `ServicesManager` for exactly the assertions that reach it, and leave the wired bean
  for the assertions that do not. That is what the customizer test now does, and it gained the
  policy-permits branch as a second assertion in the process.

## Puppeteer scenario init scripts

- `ci/tests/puppeteer/run.sh` runs a scenario's `initScript` entries with `eval "source ${script}"`, so they execute in the runner's own shell. An `exit` on the success path therefore terminates the whole scenario run, which looks like the scenario dying silently right after the init script's last line of output. Let a successful init script fall off the end, and reserve `exit 1` for the failure path, which is what that exit is there for. `ci/tests/ldap/run-ad-server.sh` still carries an `exit 0` early return for the already-running case and has the same hazard.
- Prefer polling the service's own port over a container health check, and dump `docker compose logs` on the failure path: an unhealthy container tells you nothing, while the service's logs say why it would not start.

## OpenID Connect discovery metadata

- Discovery metadata is a contract, not a description: RFC 8414 makes some entries mandatory *because* of what others advertise. `token_endpoint_auth_signing_alg_values_supported` MUST be present when `private_key_jwt` or `client_secret_jwt` is in `token_endpoint_auth_methods_supported` (and MUST NOT contain `none`). When adding an entry to one list, check whether the specification makes a second entry mandatory as a result.
- Nothing in CAS's own test suite reads this metadata as a client would, so missing entries stay invisible until a real relying party parses it. Treat a third-party client's complaint about metadata as a CAS defect until the specification says otherwise.

## Sender-constrained tokens (DPoP)

- CAS mints DPoP-bound tokens but the protected-resource half of RFC 9449 is thin. `BaseOAuth20Controller.getAccessTokenFromRequest` is the single place every protected resource resolves a token from, and it parses the authorization scheme by name; a token type CAS is willing to issue has to be listed there or the token is silently unusable, reported as `[access_token]: [null]` and then `INVALID_TICKET`.
- The two Nimbus verifiers are not interchangeable. `DPoPTokenRequestVerifier` belongs to the token endpoint and checks `htm`, `htu` and `jti`; `DPoPProtectedResourceRequestVerifier` belongs to every other endpoint and additionally binds the proof to the token through `ath` and to the confirmation recorded at issuance. `OAuth20ProofOfPossessionValidator` now has one method for each: `validate` and `validateProtectedResourceRequest`. A resource endpoint that calls the wrong one appears to work, because `htu` still matches, while checking nothing that matters.
- `ath` hashes the access token *as the client presented it*, not its decoded identifier. Pass `getAccessTokenFromRequest(...).getKey()`, never `getValue()`.
- Before reporting a gap here, read the endpoint. The userinfo endpoint was already doing the resource verification correctly in an overridden `validateAccessToken`, and the defect worth reporting was the redundant second call beside it -- the opposite shape from the one first assumed. Grepping for the validator bean found the wrong call and missed the right one.
- Identifiers that arrive on an unauthenticated request parameter are not identities. Under grants that carry their own proof of authorization -- the OpenID4VCI pre-authorized code above all -- `client_id` is whatever the wallet felt like sending. Resolve from the authenticated profile, then the token, then the parameter.

## Environment limits

- Leave no `.git/index.lock` behind. `git status` and `git diff` take that lock to refresh the index, and in a remote environment that cannot delete files the lock survives the command and blocks every subsequent git operation the maintainer runs. Read git state with `git --no-optional-locks status` / `git --no-optional-locks diff`, which never takes it, and before finishing check `ls .git/*.lock` and clear anything left. If deletion is refused, request it rather than leaving the repository wedged.
- Gradle may be unavailable in a sandboxed or remote review environment because the wrapper cannot download its distribution. When that happens, say the verification was not run instead of implying a test result, and fall back to static review such as `git diff --check` and targeted reading.


## LDAP review discipline

- Start at the shared seam. `LdapUtils` builds connection configs, filters, authenticators, DN/entry resolvers
  and person-attribute DAOs for every LDAP-backed feature; `LdapConnectionFactory` wraps every search, modify,
  add, delete and password-modify. Findings there generalize; findings in one feature module usually do not.
- Follow the whole flow: property model -> `LdapUtils` construction -> auto-configuration bean -> the calling
  repository or handler -> the webflow/endpoint boundary that consumes the result. Most real defects live in
  the mismatch between two of those layers, not inside one class.
- Search results are a trust boundary. Check the LDAP result code, not just whether an entry came back, and
  check what happens on truncation (server size/time/admin limits), on referral or continuation references,
  and on multi-valued attributes that directories return in ranges.
- Connection factories are pooled and stateful. Confirm they are created once as beans, that a pool used for
  user binds is passivated back to its original bind state, and that maps of factories are keyed by something
  that actually distinguishes two configurations.
- For any code that reads or writes a password, verify the transport requirement against the current RFC text
  and confirm the guard is enforcement rather than a log line; read the boolean carefully for inverted sense.
- Treat a fail-open authorization branch as critical regardless of how it is reached. Trace the default value
  of every property the branch depends on, including Spring Boot defaults CAS does not override.
- Watch for values that are written by one path and compared by another, and for filter/template parameters
  that are silently ignored when the operator's template does not reference them.
- Secrets and directory internals must not reach logs or released attributes: password-reset answers, bind
  credentials, diagnostic messages and matched DNs all deserve a second look.

## Startup performance review discipline

- Ask what runs, not what exists. The interesting question for an auto-configuration is not whether it is
  imported but what executes during condition evaluation, bean construction and the lifecycle events.
- Trace the three phases separately: context preparation and initializers, context refresh and bean
  creation, and the started/ready listeners. Duplicated work across two phases is a common defect —
  the same expensive operation performed once early and once late is easy to miss because each site
  looks reasonable on its own.
- The web application enables lazy initialization globally. Read `@Lazy(false)`, `InitializingBean`,
  `SmartInitializingSingleton` and event listeners as the real eager set, and follow their constructor
  parameters: a conditionally-disabled bean still builds every collaborator it declares directly, so use
  `ObjectProvider` on anything a `BeanSupplier` guard may decide not to use.
- Treat classpath scanning as a resource with a fixed answer. `ServiceLoader.load`, `classpath*:` patterns
  and resource-pattern resolvers should be evaluated once per JVM; finding one inside a loop, a factory
  method or a per-request path is a finding, not a nitpick.
- Check defaults from the operator's point of view. Instrumentation, validation and catalog-building
  features are useful when asked for and pure overhead otherwise; whichever way the default goes, the
  documentation table and the code must agree.
- Prefer deferral over deletion when the work is genuinely needed: build indexes on first access, resolve
  optional beans through providers, and let first-request initialization carry work that has no reason to
  block readiness.
- Startup timings cannot be verified in a sandboxed environment without a JDK matching the build and
  network access for Gradle. When that is the case, justify each change by naming the beans, scans or
  binds it removes from the critical path, and say plainly that no timing run was performed.
- Lazy initialization is a property of the running web application, not of the test suite. The setting
  lives in the web application's own resources, which are on the `bootRun` source set and not on any
  module's test classpath, so module tests construct the context eagerly. A change to whether a bean is
  eager therefore cannot be proven by module tests alone; they only show the eager path still works.
  Validate it through the puppeteer scenarios that exercise the affected area, and say which layer each
  result actually covers.
- Before removing or adding an eager marker, find the bean's real consumers rather than assuming the
  annotation is what registers it. Framework infrastructure usually discovers collaborators by type,
  and a type lookup resolves a lazy definition from its declared return type and instantiates it then;
  the annotation frequently only decides *when* that happens, not *whether* it happens.

## View and presentation layer

- Entry points: `support/cas-server-support-thymeleaf` (auto-configuration),
  `support/cas-server-support-thymeleaf-core` (resolvers, views, dialect),
  `support/cas-server-support-themes-core` (theme resolvers and theme sources),
  `core/cas-server-core-web-api` (`ResponseHeadersEnforcementFilter`, `AbstractCasView`),
  `support/cas-server-support-validation-core` (protocol response views).
- The render path is: request → `ChainingThemeResolver` → `ChainingTemplateViewResolver` →
  delegate template resolvers → `SpringTemplateEngine` → `ThemeBasedViewResolver` /
  `ThymeleafViewResolver`. Review it as a path; the individual classes look correct in isolation
  and the defects live at the seams (what the chain discards, what the cache key omits, what the
  theme name is allowed to be).
- Theme names come from a request header and a cookie by default and are never validated. Any new
  consumer of `ThemeResolver.resolveThemeName` must assume hostile input.
- Caching flags set on a delegate resolver mean nothing if the enclosing chain is not cacheable.
  When changing cache behaviour here, confirm the effect at `TemplateManager`, not at the bean.
- Protocol response views: CAS 2.0/3.0 XML escape through `escapeXml10`; mustache `{{ }}` escapes
  and `{{{ }}}` does not. CAS 1.0 and per-line attribute renderers are plain text with no
  structural escaping — validate values rather than relying on the template.
- Templates: use `th:text` for model data and reserve `th:utext` for `#{...}` message codes.
  Thymeleaf JS inlining (`th:inline="javascript"`, `[[${...}]]`) is safe; string-concatenating a
  model value into a `<script>` body or into `innerHTML` is not.
- Puppeteer scenarios that cover this layer: `themes-per-service`, `themes-external-per-service`,
  `themes-collection-twbs-login`, `themes-collections-example`, `thymeleaf-templates-external`,
  `thymeleaf-templates-rest`, `cas-validation-protocol-v2`, `cas-validation-protocol-v3`,
  `pm-account-profile`. Re-run these for any change to theme resolution, template resolution or
  static-resource registration; `thymeleaf-templates-external` asserts that the configured
  template prefix directory is reachable over HTTP.
- Test view components through `MockMvc` and the existing web-test infrastructure; use Lombok
  (`val`, `@RequiredArgsConstructor`, `@Slf4j`) and current Java features as the rest of the tree does.

## CAS protocol (v1/v2/v3 + SAML 1.1) review discipline

- Entry points to trace, in order: `AbstractServiceValidateController` ->
  `DefaultCentralAuthenticationService.validateServiceTicket` / `createProxyGrantingTicket` /
  `grantProxyTicket` -> `ServiceValidationViewFactory` -> the `protocol/{2.0,3.0}` mustache
  templates or `Saml10SuccessResponseView`. Review the whole chain; the interesting defects are in
  the ordering between these stages, not inside any one class.
- Ticket-state correctness rule for this codebase: read, check and mutate a ticket inside the same
  `lockRepository.execute(...)` block. `LockRepository` defaults to a JVM-local registry, so any
  invariant that must hold across nodes needs registry-level atomicity, not a lock.
- Protocol beans reached from a controller are singletons. Never introduce (or leave) per-request
  mutable state on them; bind request parameters into a local object instead.
- Anything derived from `pgtUrl`, `service`/`targetService`, or `TARGET` is attacker-controlled at
  the point CAS first touches it. Authorization checks on those values must be anchored/exact
  matches, and scheme restrictions must be enforced in code, not documented in Javadoc.
- Check response wire formats against the published CAS 3.0 protocol spec and the SAML 1.1
  browser/artifact profile, not from memory: required error codes, `<cas:proxies>` ordering,
  `InResponseTo`/`Recipient`/`Conditions` on the SAML 1.1 response.
- Cross-check every protocol change against these puppeteer scenarios:
  `cas-validation-protocol-v2`, `cas-validation-protocol-v3`, `ticket-validation-cas-renew`,
  `ticket-validation-saml1`, `ticket-validation-casv3-pgt`, `ticket-validation-casv3-pgtiou`,
  `ticket-validation-casv3-pgt-multiple-pt`, `ticket-validation-casv3-pgt-stateless`,
  `single-logout-cas-backchannel`, `single-logout-cas-frontchannel`. Note that some of them assert
  current non-compliant behavior (`ticket-validation-cas-renew` asserts
  `code="INVALID_TICKET"` where the spec wants `INVALID_TICKET_SPEC`), so a compliance fix means
  updating the scenario in the same change.
- Coverage gaps to be aware of: no scenario exercises concurrent validation of a single ticket, and
  `ticket-validation-saml1` always supplies a `RequestID`, so the SAML 1.1 default `InResponseTo`
  path is untested.

## Interrupt notifications

- Review the whole webflow path in `InterruptWebflowConfigurer`, not a single action: inquiry is wired at several
  states, and the request-scoped finalized marker decides whether later checkpoints re-run the inquirers.
- Treat the tracking cookie as a user-held acknowledgement. Changes to finalize/tracking must keep blocked
  responses unacknowledgeable and must consider which principal the cookie was issued for.
- Puppeteer interrupt scenarios often log out between logins, which removes the tracking cookie; a regression step for tracking must log in again without logging out.
- Passive requests (CAS `gateway`, OIDC `prompt=none`, SAML `IsPassive`) must not render the interrupt view. When adding an event to an action that is prepended to an existing action state, add the matching transition to that state; an unmatched event silently continues to the next action.
- A check that only runs in the login webflow is bypassed wherever a protocol module reuses the SSO session directly (for example the SAML2 IdP). For interrupts this is accepted for now; do not close it by vetoing SSO participation from the interrupt module without agreeing on the approach with the maintainer.
- Inquirer errors currently fail open; any fix must state the intended behavior for blocking policies.
- Reloadable inquirer state (files, watchers) must be published atomically; a failed reload keeps the last good contents rather than failing open.
- Payloads map onto `InterruptResponse`, whose no-arg constructor means "interrupt". Parse external responses only after the status check, and test error bodies against random-port `MockWebServer` instances.

## Groovy and scripting

- Start at `core/cas-server-core-scripting`; roughly fifty modules delegate to the same five classes, so
  changes there are changes to authentication, attribute release, MFA, AUP, OIDC, SAML and themes at once.
- `ScriptResourceCacheManager` is a singleton whose `close()` clears the entire cache — it must never be
  used in a try-with-resources block.
- Script execution is serialized per script instance on a blocking lock. Review every script-backed
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
- That chain disables CSRF, security headers and logout globally, and it is the same chain
  that serves the actuator. Any browser-reachable session credential (`cas.monitor.endpoints.form-login-enabled`)
  therefore leaves mutating actuator operations unprotected against cross-site requests.
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

## SAML2 metadata review discipline

- There are two independent metadata subsystems and they share almost nothing. *SP metadata*
  runs `SamlRegisteredService` -> `SamlRegisteredServiceDefaultCachingMetadataResolver` ->
  `SamlRegisteredServiceMetadataResolverCacheLoader` -> the resolvers under
  `.../cache/resolver/**` -> OpenSAML filters. *IdP metadata* runs
  `SamlIdPMetadataGenerator` -> `SamlIdPMetadataLocator` (one per storage backend) ->
  `SamlIdPMetadataResolver` -> `SamlIdPMetadataCredentialResolver`. A third, `saml-mdui-core`,
  is a separate adapter stack that does not reuse either. Review each as a path.
- `SamlIdPMetadataResolver` is a singleton that calls `setMetadataRootElement` — which swaps
  `AbstractBatchMetadataResolver`'s backing store — on every cache miss, then reads it back.
  Anything reached through `SamlIdPMetadataCredentialResolver` inherits that shared state, so
  per-service IdP metadata is where concurrency defects in this area live. That swap-then-read
  pair is now held under a `CasReentrantLock` on the miss path only, with the cache re-checked
  inside the lock; `setMetadataRootElement` is `protected` and its javadoc carries the calling
  contract. Anything new that installs a backing store on that singleton has to take the same
  lock, and results handed out of the resolver must be materialized (`List.copyOf`) rather than
  left as a lazy view over a store a later resolution replaces. There is no puppeteer scenario
  for per-service IdP metadata — no scenario sets `idpMetadataLocation` — so this path is
  covered by `SamlIdPMetadataResolverTests` alone.
- CAS does not use OpenSAML's `HTTPMetadataResolver` / `FileBackedHTTPMetadataResolver`.
  `UrlResourceMetadataResolver` reimplements fetch-and-back-up by hand, which is why the usual
  guarantees are absent: no background refresh timer and no min/max refresh delay. Do not assume
  OpenSAML semantics when reading this code. The backup file is now treated the way that class
  would treat it: `force-metadata-refresh` (default `true`) means "do not serve the backup
  without checking the remote first", never "delete the backup", a successful download
  overwrites it in place, and `resolveMetadataLocation` falls back onto it when the download
  cannot be completed. The two readers of the backup want different answers about root validity
  — reusing it instead of contacting the remote requires `Boolean.TRUE.equals(isRootValid())`,
  falling back onto it only requires `!Boolean.FALSE.equals(...)`, because a document with no
  `validUntil` reports its validity as unknown. Keeping the backup also makes MDQ's
  `If-None-Match` path reachable for the first time.
- The MDQ subclass has its own failure vocabulary and it interacts with that fallback.
  `fetchMetadata` returns `null` rather than throwing when the server is unreachable *and* a
  backup exists, so `getMetadataResolverFromResponse` must tolerate a null response; it still
  throws `UnauthorizedServiceException` when there is nothing to fall back onto, and
  `resolveMetadataLocation` rethrows that rather than swallowing it, which is what
  `MetadataQueryProtocolMetadataResolverTests.verifyResolverFails` asserts. Read the status
  with `HttpStatus.resolve`, never `valueOf`, or a non-standard code throws instead of
  reaching the fallback.
- The metadata backup file is named `sha(metadataLocation)` while the Caffeine key is
  `serviceId|metadataLocation`. Two services pointing at one URL are two cache entries over
  one file, with no lock around write, read or delete. Check that pairing before proposing
  anything that touches the backup directory.
- Trust here is opt-in and fails open. A service without `metadataSignatureLocation` gets no
  `SignatureValidationFilter` at all, and `SamlUtils.buildSignatureValidationFilter` returns
  `null` — not an exception — when the configured resource cannot be read, after which the
  caller logs a warning and loads the metadata anyway. Read `addSignatureValidationFilterIfNeeded`
  before claiming signature validation is enforced.
- `buildRequiredValidUntilFilterIfNeeded` only runs when `metadataMaxValidity > 0`, and
  `SamlRegisteredServiceMetadataExpirationPolicy` reads `cacheDuration` but never `validUntil`.
  Expiry and validity are two different mechanisms in this code; do not conflate them. The
  policy can also return a negative duration when a service expiration date is in the past.
- IdP metadata backends are not interchangeable. JPA and MongoDB resolve the *global*
  document with an unfiltered "first row" query (`SELECT r FROM SamlIdPMetadataDocument r`,
  `findOne(new Query())`), so a per-service document can answer as the global one; Redis scans
  by `appliesTo`. `appliesTo` is `SamlIdPUtils.getSamlIdPMetadataOwner` = `name + '-' + id`,
  which also becomes a directory name on the file-system locator and a key elsewhere —
  unsanitized, and it changes when a service is renamed.
- The file-system generator writes the IdP signing and encryption private keys in the clear
  with default permissions; the other backends run the key through `metadataCipherExecutor`.
  Do not describe key-at-rest handling as uniform across backends.
- `/idp/metadata` is public and calls `generate(...)` on every request, including with a
  caller-supplied `service` parameter. Generation is guarded only by an `exists()` check, with
  no lock across threads or nodes, and it mints two RSA keypairs (`key-size` defaults to 4096).
- The parser pool in `CasCoreSamlAutoConfiguration` is properly hardened (doctype disallowed,
  external entities off, secure processing on), so XXE is not the gap. Response size is:
  metadata bodies are read with `IOUtils.toString` with no bound.
- `HttpUtils.execute` builds a new `CloseableHttpClient` with its own pooling connection
  manager per call and only the response is ever closed. Every metadata fetch pays that, and
  the MDQ resolver additionally omits `.httpClient(...)`, so it does not use the deployment's
  configured TLS trust and hostname verifier.
- Check MDQ against the SAML profile for the Metadata Query Protocol, not from memory: a
  compliant client MUST send `Accept: application/samlmetadata+xml`. CAS puts
  `cas.authn.saml-idp.metadata.mdq.supported-content-type` on `Content-Type` of a GET and then
  sends `Accept: */*`. Signature verification is RECOMMENDED for servers there, not a client
  MUST, so do not report the optional filter as a spec violation — report it as fail-open.
- Two request-path amplifiers to watch: `SamlRegisteredServiceMetadataHealthIndicator` calls
  `isAvailable(...)` for every SAML service, which pings each metadata URL on every health
  poll; and `DynamicMetadataResolverAdapter.getEntityDescriptorForEntityId` rebuilds its whole
  resolver aggregate and refetches over MDQ on every call, driven by an unauthenticated
  `entityId` request parameter that `MetadataUIUtils` asks for twice per login render.
- Puppeteer coverage: `saml2-idp-metadata-caching`, `saml2-idp-login-idp-initiated-mdq`,
  `saml2-idp-login-sp-metadata-{directory,groovy,jdbc,json,mongodb}`,
  `saml2-idp-login-sp-override-metadata`, `saml2-idp-login-metadata-aws-s3`, `saml-mdui`,
  `delegated-login-saml2-mongodb-metadata`. All are single-threaded happy paths against a
  reachable metadata source, so nothing here covers concurrency, a metadata host that is down,
  signature-validation failure, or MDQ content negotiation. Changes in those areas need new
  scenarios rather than a rerun.

## Attribute consent review discipline

- Start at `ConsentDecision`, not at a repository. `id` is a primitive `long` with
  `@GeneratedValue`, so only JPA ever assigns it; `DefaultConsentDecisionBuilder.build`
  leaves it at `0` for every other store. That single field is the key in four backends:
  LDAP's `mergeDecision` guards on `id < 0` and then `removeDecision(0)` wipes the user's
  other decisions, Redis keys on `ConsentDecision:<principal>:<id>`, Mongo maps it to
  `_id`, and DynamoDB uses it as the table HASH key. Before trusting any consent storage
  behavior, ask what `id` actually holds on the production path.
- `BaseConsentRepositoryTests` calls `decision.setId(1/100/200)` before every store, so the
  shared suite never exercises the id the engine really produces, and no test stores two
  decisions for one user. Treat "the repository tests pass" as no evidence here.
- Consent is a gate in the login webflow, not a filter at attribute release.
  `ConsentWebflowConfigurer` prepends `CheckConsentRequiredAction` to the login flow's
  `generateServiceTicket` state; the only other consumer is
  `SamlIdPConsentSingleSignOnParticipationStrategy`. Nothing in
  `RegisteredServiceAttributeReleasePolicy` reads a stored decision, so the consented set
  never constrains what is released, and any path that issues without the login flow --
  REST protocol tickets, proxy tickets, OIDC refresh/userinfo, the OpenID4VCI
  pre-authorized code grant -- releases attributes with no consent check at all.
- Follow `ConsentQueryResult`. It carries the decision and the service, and
  `DefaultConsentActivationStrategy` throws all of that away by returning `.isRequired()`.
  Each discarded result costs another full `getConsentableAttributes` evaluation (person
  directory included) and another repository read; a consented login does three of each.
- `DefaultConsentEngine.executeRepositoryOperation` calls `toConsentRepository(tenant)` on
  every operation, and `TenantJdbcConsentRepositoryBuilder` builds a DataSource plus a
  Hibernate `EntityManagerFactory` inside it, then destroys them. Check the multitenant
  path before judging consent performance; the single-tenant path is not representative.
- Repository lookups must be checked for operator and pattern correctness, not just for
  the right column names. `DynamoDbConsentFacilitator` matches SERVICE and ID with
  `ComparisonOperator.GE` (correct for the date ranges other CAS DynamoDB facilitators
  use it for, wrong for an equality lookup) over a `scanPaginator`, and
  `RedisConsentRepository` interpolates the raw principal id into a SCAN glob so a
  principal containing `*` or `?` reaches other users' decisions.
- Deletes deserve the same reading as reads. `JpaConsentRepository.deleteConsentDecisions`
  uses `getSingleResult()` and so silently deletes nothing for any user with more than one
  decision, and `ChainingConsentRepository` stores to every repository but deletes with
  `anyMatch`, which stops at the first success. Revocation failing quietly is worse than
  consent failing loudly.
- A decision that will not decipher is fatal, not recoverable:
  `getConsentableAttributesFrom` turns any failure into `IllegalArgumentException`, which
  propagates out of `isConsentRequiredFor` and out of the login flow. Key rotation, or a
  node without the shared `cas.consent.core.crypto` keys, locks the user out instead of
  re-prompting.
- `ConfirmConsentAction` parses `option`, `reminder` and `reminderTimeUnit` straight off
  the request with `Integer.parseInt` / `Long.parseLong` / `ChronoUnit.valueOf` and
  persists them unvalidated. `ChronoUnit.FOREVER` and `ERAS` parse fine and then throw
  from `createdDate.plus(...)` on every later login.
- `cas.consent.core.active` defaults to `true`, so adding the module turns consent on for
  every service that releases attributes. Weigh findings about the consent path as if it
  is always on.
- Wallet/OID4VCI integration is the real gap, not a defect in the consent modules:
  `BaseOidcVerifiableCredentialEncoder` reads `principal.getAttributes()` directly against
  the issuer's configured claim list, so a credential handed to a wallet carries no
  consent record, no per-claim choice, and no revocation. Selective disclosure exists in
  the SD-JWT encoder but the holder never chooses what is disclosed at issuance time.
- Puppeteer coverage: `attribute-consent` (in-memory repository, one user, one service),
  `multitenancy-consent-jdbc`, `saml2-idp-login-consent`, `saml2-idp-login-consent-with-sso`.
  No scenario exercises LDAP, Redis, Mongo, DynamoDB or REST consent, and none stores two
  decisions for the same user, which is exactly the shape every storage defect above needs.

## MongoDB backends (service registry and ticket registry)

- Three modules carry everything: `cas-server-support-mongo-core` (`MongoDbConnectionFactory`,
  `BaseConverters`, `DefaultCasMongoTemplate`, `MongoDbClusterTopologyManager`),
  `cas-server-support-mongo-service-registry`, and `cas-server-support-mongo-ticket-registry`
  (registry, `MongoDbTicketDocument`, `MongoDbTicketRegistryFacilitator`, catalog provider).
  Findings in the core factory apply to all ~19 `*-mongo` support modules at once; findings in a
  registry usually do not generalize.
- `MongoDbConnectionFactory.buildMongoDbClient` has two branches and they are not equivalent. The
  `client-uri` branch applies the connection string and nothing else: no `CasSSLContext`, no
  `ZonedDateTimeCodecProvider`, no pool/socket/server settings, no read or write concern, no
  `retryWrites`. A connection string cannot express a custom trust store, so a deployment using
  `client-uri` silently falls back to the JVM default trust material. Check which branch a setting
  lives in before describing it as configurable. (Still open.)
- Read the defaults from the operator's point of view before judging a flow: `ssl-enabled=false`,
  ticket-registry `crypto.enabled=false`, `write-concern=ACKNOWLEDGED`, `read-concern=AVAILABLE`,
  `retry-writes=false`. With the cipher off, `AbstractTicketRegistry.collectAndDigestTicketAttributes`
  returns attributes undigested, so the Mongo document carries the principal, the service and every
  authentication attribute in clear beside the ticket JSON. (Still open.)
- Durability is part of the security argument here. `deleteTicket` is a compare-and-swap only as far
  as the write concern makes the delete durable; `w:1` plus a replica-set failover can roll back the
  removal of a one-time-use ticket, code or nonce. Evaluate single-use state against the write
  concern, not just against the code path. (Still open.)
- Bean-construction side effects in a refresh-scoped bean are runtime operations, not startup
  operations: the bean method runs again on every `/actuator/refresh`. Destructive setup
  (`drop-collection`, `drop-indexes`) therefore lives in a plain-singleton `InitializingBean` that
  the refresh-scoped registry declares with `@DependsOn`, so singleton scope is what limits it to
  one execution per context. Express "startup only" through bean scope, not through a flag the
  configuration class retains; a configuration class holding mutable state to remember whether it
  has run is the shape to avoid. Do not mark those initializers `@Lazy(false)` either -- the
  `@DependsOn` edge already orders them, and eager marking would move Mongo I/O onto the startup
  path of an application that otherwise initializes lazily.
- Do NOT close a Mongo client from a bean's disposal here, and treat the leaked clients as a known
  cost rather than a bug to patch locally. CAS declares ~2400 beans
  `@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)`, and `ScopedProxyMode.DEFAULT` means NO scoped
  proxy: consumers hold the instance itself, not a proxy that re-resolves. `RefreshScope.refreshAll()`
  therefore disposes objects that live consumers are still using. Making `DefaultCasMongoTemplate` a
  `DisposableBean` that closed its client broke `/actuator/health` in the
  `mongodb-ticket-service-registry` scenario: `BeanContainer.destroy()` disposed the templates inside
  `mongoHealthIndicatorTemplate`, while Spring Boot's health registry -- not refresh-scoped, so
  outside the refresh cascade -- still held the `CompositeHealthIndicator` built over them, and the
  next check failed with `ClientSessionException: state should be: open`. The login path survived only
  because its whole chain is refresh-scoped and was rebuilt together, which is what makes this failure
  mode easy to miss.
- Carry two rules from that. A resource held behind a refresh-scoped bean cannot be released by
  disposal alone -- the owner's lifetime has to match its consumers', which is a scoped-proxy
  decision, not a `destroy()` one. And never reason about refresh behaviour from the annotation
  alone: `ScopedProxyMode.DEFAULT` is the opposite of what the name suggests.
- For the record, Spring Data will not release these clients either:
  `SimpleMongoClientDatabaseFactory(MongoClient, String)` records the client as externally managed and
  `MongoDatabaseFactorySupport.destroy()` is `if (mongoInstanceCreated) { closeClient(); }`. CAS always
  hands in its own client, because a connection string cannot express the `MongoClientSettings` it
  needs. So the clients really are never closed -- that is the cost of the model above, not an
  oversight at the template.
- `BeanContainer` extends `DisposableBean`, and `ListBeanContainer.destroy()` disposes every
  `DisposableBean` entry and closes every `Closeable` one. That is exactly how the health templates
  came to be closed: a container of resources is not a leak, it is a disposal amplifier.
- `mongoDbTicketRegistryTemplate` is `@Primary`. Any module injecting `MongoTemplate` by type gets
  the ticket-registry database; check the qualifier before assuming a module talks to its own.
  (Still open.)
- The ticket registry propagates storage failures rather than returning a benign value:
  `addSingleTicket` and `updateTicket` declare `throws Exception`, and a ticket definition missing
  from the catalog throws. Only a genuine miss returns null. Do not reintroduce a `catch (Throwable)`
  that logs and continues — that is what made a failed insert look like a successful login. When
  reviewing any storage backend, read the catch blocks before the happy path.
- Multi-collection reads go through `MongoDbTicketRegistry.streamTicketDocuments`, which records
  every cursor it opens and closes them from the returned stream's `onClose`; a consumer that
  short-circuits never lets `flatMap` close the inner stream it was reading. Callers must close the
  stream. Per-collection queries are bounded by `limitQuery` to `from + count`, because no single
  collection can contribute more than that to the result, while the global skip/limit stays in the
  JVM since it spans collections — do not push `skip` down, it does not mean the same thing there.
- Collection names are resolved once per call and `distinct()`-ed, so definitions sharing a storage
  name are read and counted once.
- Writer and reader must agree on key mapping. `MappingMongoConverter` is configured with
  `MongoDbConnectionFactory.MAP_KEY_DOT_REPLACEMENT`, and `digestAttributeKey` applies the same
  replacement when a query addresses `attributes.<key>`. Check the converter's configuration
  whenever new code addresses a map field by path.
- `IDX_PRINCIPAL` is created on every ticket collection, not only on the ticket-granting ticket
  collection, because `deleteTicketsFor` and the principal criteria run against all of them.
- `casTicketRegistryLockRepository` exists for Redis and JPA and does not exist for Mongo, so
  `LockRepository` is JVM-local on a Mongo cluster. `updateTicket` is an unconditional `updateFirst`
  with no version check, so every read-modify-write invariant is last-writer-wins across nodes.
  (Still open.)
- Puppeteer coverage is `mongodb-ticket-service-registry` only. It refreshes the context first, then
  clears sessions, logs in once and asserts exactly one ticket-granting ticket, the health indicator
  and the ticket-registry cleaner — so it exercises the refresh path but asserts nothing about what
  survives a refresh. There is no crypto-enabled variant, no TLS and no concurrency, so a change in
  this area needs a new scenario rather than a rerun. `http-session-mongodb`, `authn-events-mongodb`,
  `configuration-properties-mongodb`, `simple-mfa-trusted-device-mongodb`,
  `saml2-idp-login-sp-metadata-mongodb` and `delegated-login-saml2-mongodb-metadata` exercise the
  shared connection factory and are the ones to re-run for any `cas-server-support-mongo-core` change.
- `MongoDbTicketRegistryTests` clears the whole registry in `@BeforeEach` while JUnit runs its
  methods concurrently, so any new test there must assert on identifiers it created itself (a UUID
  principal or attribute value) rather than on registry-wide counts.
