---
name: cas-testing
description: "Run and write tests in the CAS Gradle monorepo: testcas.sh categories, JUnit tags, single-module test tasks, fast compile checks, how tests stay correct under JUnit parallel execution (shared registries, ports, pac4j clients, negative assertions), test authentications and principal attributes, and ticket-granting cookies in MockMvc tests. Use when running tests, choosing a test category, writing or fixing a *Tests.java class, or chasing a flaky test."
---

# Running and writing CAS tests

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
- `./testcas.sh --category changed` reads tags only from changed or untracked `*/src/test/java/*Tests.java` files.
  It does not infer test categories from production-source changes; select the relevant categories explicitly for those.
- Run one module or one class directly when narrowing a change:
  ```bash
  ./gradlew :core:cas-server-core-authentication:testAuthenticationHandler --tests "*AcceptUsersAuthenticationHandlerTests"
  ```
- Compile production sources without assembling archives or compiling tests when you only need a fast validation pass:
  ```bash
  ./gradlew classes --parallel
  ./gradlew :support:cas-server-support-json-service-registry:classes
  ```
  Use `assemble` for artifacts and `testClasses` explicitly for test compilation. Ordinary `assemble` does not depend on
  `testJar`; non-minimal publishing and consumers of the `tests` configuration still build it on demand.
- Many `./testcas.sh` categories shell out to `ci/tests/**/run-*.sh` and require Docker on Linux; the script will refuse those categories when that prerequisite is missing.

## Parallel test execution and shared registries

- Initialize shared pac4j clients with `DelegatedIdentityProviders.initialize(client)`, never a bare `client.init()`: pac4j returns immediately, uninitialized, while another thread is initializing the same instance. The helper waits lock-free on `isInitializing()`.
- Most categories in `buildSrc/.../TestCategories.groovy` are declared parallel, and JUnit's default mode there is `concurrent` for classes *and* methods, so sibling `@Test` methods in one class run at the same time against the same Spring context. A test that clears a shared registry wholesale -- `servicesManager.getAllServicesOfType(...).forEach(servicesManager::delete)` in a setup step, say -- deletes what its siblings just saved, and the failure surfaces in whichever method lost the race rather than in the one that did the clearing.
- The symptom to recognize: a test asserting on a service it saved itself gets the value that belongs to the "service was missing" code path. `OpenIdFederationAuthorizationCodeResponseTypeAuthorizationRequestValidatorTests` failed exactly that way, reporting `expected: <old-service> but was: <new-service>`, because another method's clear removed the saved service and the validator then resolved a fresh one.
- Never add `@Execution(ExecutionMode.SAME_THREAD)` to a test class; rework the test to isolate its own state instead.
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
- A clear can sit in an abstract base class, out of sight of the class that fails. `BaseThemeTests` emptied
  the services registry in a `@BeforeEach`, which ran before every method of every subclass, and two subclasses
  in different files (`RegisteredServiceThemeResolverTests.ExampleThemeTests`,
  `ChainingThemeResolverTests.ThemeDefinitionTests`) share one context. The symptom is the resolver's fallback
  (`expected: <some-theme> but was: <example>`, the default theme) instead of the value on the test's own
  service. A test that empties the registry to prove a result is remembered (`verifyThemeIsResolvedOncePerRequest`)
  proves the same thing by deleting only its own service.
- Ports are shared state too, and the trap has a specific shape. `MockWebServer.getRandomPort()`
  now draws from **21000-24999**, a band nothing else in the repository binds, and a Checkstyle rule
  (`reservedMockWebServerPorts`) keeps it that way. It used to draw from 4000-9999, which overlapped
  about 150 ports the tests and CI containers already use -- 5432, 6379, 8080, 8443, 9042 and 9092
  among them -- as well as the `${random.int[3000,9000]}` convention test property sources use for
  their own servers. A test that hardcodes a port inside the mock range and depends on nothing
  listening there is racing every mock server in its category: `HttpUtilsTests.verifyExec` and
  `verifyBearerToken` did exactly that on 8080 and 8081, asserting that an unreachable proxy yields
  an error response, and a sibling test's mock server landing on 8080 turned "connection refused"
  into `200 OK` and inverted the assertion. The symptom is one of two identical tests failing, which
  is the giveaway that it is timing rather than logic. Moving the band fixed that class of failure
  wholesale; hardcoded 8080/8081 elsewhere in the tree is mostly harmless string-building and
  serialization anyway, and only tests that actually open a connection were ever exposed.

## Test authentications and principal attributes

- `CoreAuthenticationTestUtils.getAuthentication(name, attributes)` puts the attributes on the authentication; its principal
  keeps the default test attributes (`cn=TEST`, `givenName=test`, ...). Anything that merges the two with the principal last
  (JWT claims of token authentication, released attributes) sees the principal's values, and a service from
  `RegisteredServiceTestUtils.getRegisteredService` requires `uid=uid`, `givenName=CASUser` and the `memberOf` values on the
  principal. Pass them with `getAuthentication(getPrincipal(name, attributes))`.
- A test whose outcome depends on its own registered service's properties (secrets, policies) must register a URL nobody
  else uses (`"https://token.example.org/" + UUID.randomUUID()`), not `RegisteredServiceTestUtils.CONST_TEST_URL`: other
  services in the shared context match that URL too, and which one `findServiceBy` returns depends on test order.

## Ticket-granting cookies in MockMvc tests

- A TGC is pinned to the client address and user agent read from `ClientInfoHolder` and the request when it is built.
  MockMvc's filters clear `ClientInfoHolder` after every request, so a cookie built afterwards in the test thread carries no
  address, fails validation silently (`retrieveCookieValue` returns `null`) and the request behaves as if no one signed in.
  Call `ClientInfoHolder.setClientInfo(ClientInfo.from(request))` right before `addCookie`, and send the same `User-Agent`
  with the MockMvc request. A test that expects SSO but sees the login page has usually hit this; do not "fix" it by
  asserting the login redirect.

## Puppeteer scenario init scripts

- Mockito: never call another mock inside `thenReturn(...)` (`thenReturn(accessToken.getAuthentication())` with a mocked
  token is an UnfinishedStubbingException); read the values into locals first.

## Queue-backed ticket registries (Kafka, AMQP, Pulsar, GCP Pub/Sub)

- MariaDB runs on host port 3307 (`ci/tests/mariadb/run-mariadb-server.sh` maps `3307:3306`), MySQL on 3306, so `testJDBC`
  can run both. Every MariaDB test class uses `jdbc:mariadb://localhost:3307` with `@EnabledIfListeningOnPort(port = 3307)`,
  as do the `mfa-gauth-login-encrypted`, `mfa-webauthn-register-logout-login` and `mfa-yubikey-storage` scenarios (their
  `dockerExec` calls run inside the container and are unaffected). A MariaDB test pointed at 3306 reaches MySQL and fails
  with `caching_sha2_password ... sslMode=trust`.

## Google Authenticator credential repositories (all backends)

- `MockWebServer.requestLineConsumer` exposes the request line, for asserting the HTTP method a client sends.
- `MockRequestContext.create(...)` throws `Exception`; a test method that calls it must declare `throws Throwable`.

## Hazelcast ticket registry

- The Gradle build runs Error Prone with warnings as errors on main and test sources (e.g. `CanonicalDuration`: `Duration.ofMinutes(-1)`, not `ofSeconds(-60)`); compile-check tests with Error Prone too, not only main. NullAway reports the same pre-existing errors on `HazelcastTicketRegistry` before and after a change, so compare against the old sources rather than expecting zero.
