---
name: cas-puppeteer-scenarios
description: "Write, fix and review the end-to-end puppeteer scenarios under ci/tests/puppeteer: run.sh behavior, init/bootstrap scripts, script.json properties and variations, storage-backend scenarios (JPA, Redis, Mongo, DynamoDB, LDAP, REST, localstack), cas.js helpers, actuator coverage and the CI retry rules. Use when touching ci/tests/puppeteer or ci/tests/**/run-*.sh, or cross-checking a change against its scenarios."
---

# CAS puppeteer scenarios

## Puppeteer scenario init scripts

- `ci/tests/puppeteer/run.sh` runs a scenario's `initScript` entries with `eval "source ${script}"`, so they execute in the runner's own shell. An `exit` on the success path therefore terminates the whole scenario run, which looks like the scenario dying silently right after the init script's last line of output. Let a successful init script fall off the end, and reserve `exit 1` for the failure path, which is what that exit is there for. `ci/tests/ldap/run-ad-server.sh` still carries an `exit 0` early return for the already-running case and has the same hazard.
- Init scripts are sourced, so `set -e`, `set -u` or `pipefail` in one would stay on for the rest of `run.sh`; 17 scenarios source such a script, and a leaked `set -e` made a failing test exit `run.sh` at its first attempt with code 1 (whole-script retry) instead of 5 after its in-process attempts. `run.sh` turns those options off after each init script, so do not rely on them persisting.
- Prefer polling the service's own port over a container health check, and dump `docker compose logs` on the failure path: an unhealthy container tells you nothing, while the service's logs say why it would not start.
- In CI, `run.sh` launches the Gradle build (`bootWar`, or `bootJar` for starter scenarios; native keeps `build` + `nativeCompile`) in the background and runs npm install, ESLint, bootstrap and init scripts while it builds, waiting on the build process only before launching the CAS instance that needs it. Init and bootstrap scripts must therefore never depend on the built artifact, and a fixed `sleep` in them now runs while the build competes for CPU, so poll instead. `PUPPETEER_BUILD_OVERLAP=false` restores the sequential order. `PUPPETEER_BUILD_CTR` is the build timeout in minutes, measured from launch.
- A container answering HTTP is not a container ready for setup calls. Apache Syncope's Tomcat answers `/syncope/` several seconds before its content loader populates the empty Master domain, and admin REST calls made in that window fail with `AuthorizationDeniedException` (seen once the build overlapped the init scripts and slowed startup). `ci/tests/syncope/run-syncope-server.sh` therefore waits for `Started SyncopeCoreApplication` in the container log; readiness waits in other init scripts should likewise key off the application's own started signal, bounded, with `exit 1` on timeout.
- Scenario matrix jobs in `functional-tests.yml` restore the Gradle User Home with `cache-read-only: true` and do not set `cache: 'gradle'` on `setup-java`; saving from every one of the ~560 jobs cost ~12 s each and churned the Actions cache, and the two actions caching the same directory conflict.
- The matrix jobs cache the Node.js install under `${{ runner.tool_cache }}/node/<NODE_VERSION_REQUIRED>` with a key that is that exact version, restored before `setup-node` and saved only from the default branch on a miss. A version change in `NODE_CURRENT` or a scenario's `requirements.nodejs` is a new key, so `setup-node` downloads the instructed version; keep those values exact versions (a range would never match the cached directory) and do not add `check-latest`.
- The matrix jobs start `ci/tests/puppeteer/prefetch-docker-images.sh <scenario>` right after checkout (in `native-tests.yml`, after `Configure CI` and only when the scenario's native run is enabled, since init scripts run only in the `--nr` step and the pulls then finish during the native build): it finds the images the scenario's bootstrap, init and ready scripts need (their `docker run`/`pull`/`build` commands, `*_IMAGE` and `*COMPOSE_FILE` assignments, compose files, Dockerfile `FROM` lines, and the `ci/tests` scripts they call) and pulls them in the background while the job sets up, logging to `$RUNNER_TEMP/docker-prefetch.log` (printed by the `Docker Prefetch Summary` step). Images a script builds itself, compose services with `build:` and run.sh's own `cas-<scenario>` images are skipped, and so are scenarios that are disabled or miss a `conditions.env` variable (run.sh skips those too). It is best-effort: an image it misses or is still pulling is pulled by the init script as before, so new init scripts need no changes, but keep image references literal or in a plain `*_IMAGE=` assignment so they are found. `--list` prints what it would pull. Images are not stored in the Actions cache: it is already at its limit, and restoring a tarball is a download too.
- `initScript` entries run once *per instance*, right before that instance starts; `bootstrapScript` runs once for the scenario. A multi-instance scenario whose nodes share a backend (Pulsar, MySQL, MongoDB, Kafka, Redis) must start it from `bootstrapScript`: as an init script it is restarted under the first node before the second one starts, which wipes its state and leaves node 1's connections to reconnect (or not) to a fresh server. The symptom is node 1 silently missing everything node 2 publishes.
- Instances whose resolved dependencies are identical share one build: `run.sh` builds only the first instance of each dependency set and copies its artifact to the others. Only instance-specific `dependencies` cause another build. Instances still start one after another, because several multi-instance scenarios need instance 1 up before instance 2 starts (Spring Boot Admin client registration, passive service-registry replication, cas2cas delegation).
- `run.sh` exit codes carry meaning for the `Run Tests` retry (`retry_on_exit_code: 1`): 1 is a setup failure worth another attempt (init scripts, containers, npm), 2 a failed build, 3 a build that exceeded `PUPPETEER_BUILD_CTR`, 4 a CAS instance that exited or did not answer its health check within `PUPPETEER_STARTUP_TIMEOUT` seconds (300 in CI, unlimited locally; a single probe may use the whole remaining budget, since some login pages take ~30 s to render, e.g. `thymeleaf-templates-rest` before its template server is up), and 5 a scenario script that still failed after its in-process attempts (3 in CI, against the running server). 2-5 fail at once; a whole-script retry cannot fix them and repeats the build and startup. Keep new failure paths on 1 only if a rerun can help.
- `tests.yml`, `functional-tests.yml` and `native-tests.yml` retry failures the same way: a boolean `retryFailedScenarios`
  dispatch input gates a step that runs `ci/tests/select-failed-jobs.sh <list.json>`, which narrows the listed
  categories/scenarios in place to the jobs that failed, timed out or were cancelled in the latest attempt of the
  workflow's previous completed run, and writes a step summary. Call that script from any other matrix workflow; do not
  inline the logic. It matches job names, so a matrix job's `name:` must stay exactly the category or scenario, and the
  listing job needs `permissions: actions: read`. No matching job leaves `[]`, so every matrix job that consumes the list is
  skipped when it is empty (an empty matrix fails the job). A category retry disables PTS and coverage and skips the
  `coverage` job, which would otherwise upload a partial report.
- `run.sh` joins a scenario's `properties` with spaces and passes them unquoted, so a value containing a space is cut at the space (`display-name=My CAS One` arrives as `My`). Use values without spaces in `script.json`.
- `ci/tests/keycloak/run-keycloak-server.sh` polls the `cas` realm's discovery document; Keycloak answers HTTP well before the realm import finishes, and a pac4j OIDC client that fetched its metadata in that window stays broken for every later in-process attempt.
- `run.sh` installs the Chrome build Puppeteer pins (`npx puppeteer browsers install chrome`) after `npm install`, because npm can finish without puppeteer's postinstall having downloaded it, which only shows up later as `Could not find Chrome`.
- A scenario that navigates to `https://localhost:9859/anything/...` needs `ci/tests/httpbin/run-httpbin-server.sh` among its init scripts; without it the redirect lands on `chrome-error://` and the ticket assertion fails.
- Webflow executions are client-side by default (`cas.webflow.session.storage=false`): the `execution` value is the signed and encrypted flow state, so replaying one simply resumes that state, and "reusing a consumed execution" is only meaningful with server-side storage (`http-session-ticket-registry` covers it). Server-side keys are numbered per session (`e1s1`, `e2s1`, ...), and a new HTTP session numbers from `e1` again, so opening the login page before the replay hands out the very key being replayed and the "replay" logs in. Replay from a non-CAS page, after deleting the TGC with `page.deleteCookie({name: "TGC", url: "https://localhost:8443/cas/"})` (`cas.deleteCookies(page)` only sees cookies of the page's current URL, which on httpbin excludes the `/cas` path). A well-formed but altered execution fails decoding with `ClientFlowExecutionRepositoryException`, and `FlowExecutionExceptionResolver` redirects to a fresh flow (302). A malformed one (no `uuid_data` shape) raises `BadlyFormattedFlowExecutionKeyException`, which the resolver now also restarts; it drops any `execution` query parameter from the redirect, which is what avoids the redirect loop that once made it skip this exception. `webflow-clientside-sessions` exercises all three cases plus a double submit and the back button. The ticket-granting cookie value embeds the `User-Agent` when `cas.tgc.pin-to-session` is on; a request without one is pinned to a placeholder (`DefaultCasCookieValueManager`), so the cookie only matches later requests that also send none. It used to throw and answer 500.
- `actuator-endpoint-roles` is the table-driven check of endpoint security: every row is called anonymously (401), as `casviewer` without `ADMIN` (403) and as `casadmin` (2xx, or "reachable" for operations needing a payload). Users come from its `users.json` (`cas.monitor.endpoints.json.location`). Add a row there when adding an actuator endpoint whose module needs no external service; rows default to `Content-Type: application/json`, so give operations that consume form parameters (the `casValidate` ones) `contentType: FORM`, or they answer 415.
- To prove CAS refuses a tampered SAML2 AuthnRequest, intercept the browser hop from the SP to `/cas/idp/profile/SAML2/*/SSO` with `page.setRequestInterception(true)` (see `saml2-idp-login-acs-signed`). POST binding: rewrite `SAMLRequest` in `postData` and `request.continue({postData})`. Redirect binding: inflate with `zlib.inflateRawSync`, alter, deflate, then `request.abort()` and `cas.goto` the altered URL rather than overriding `url` on a redirected request. Assert that the tampering really happened, otherwise a request without the attribute passes for the wrong reason.
- `cas.newPage(browser)` returns the browser's first tab when one exists; it opens a new one only with `cas.newPage(browser, true)`. A helper that takes a page, intercepts its requests and closes it must force a new page, or it closes the scenario's main page and later calls fail with "Attempted to use detached Frame" (`saml2-idp-login-acs-signed`).
- Before writing a negative assertion, read the server code for the exact error. Each token-endpoint failure surfaces differently: device code reuse → `access_denied` (`InvalidOAuth20DeviceTokenException`), a mismatched ST service → `INVALID_SERVICE` and the ticket is consumed. Where the specification names the error (CIBA Core §11: `authorization_pending`, `invalid_grant`), the scenario asserts the specification even when the current code differs, and the hand-off says so.
- `from` in actuator and ticket registry queries (`ssoSessions?from=`, `TicketRegistryStreamCriteria`) is a 0-based offset in every registry. A scenario that deletes all SSO sessions passes `from=0` (or omits it); `from=1` leaves the first session alive, which only shows once that session outlives the scenario's other timeouts, such as a remember-me TGT in a browser profile that later browsers share.
- In CI, `run.sh` bounds each script attempt with `PUPPETEER_SCRIPT_TIMEOUT` (420 seconds; 0 locally, meaning no limit). A script whose steps all finish but whose node process never exits then fails that attempt and is retried, instead of running into the 20 minute job timeout.
- AOT training runs extract the built artifact with `-Djarmode=tools extract` and launch what it produces. Since Spring Boot
  4.2 (gh-51372) extracting a war writes `cas/cas.jar` (root web content moved to `META-INF/resources/`), no longer
  `cas/cas.war`; `run.sh` launches `cas/cas.jar` and falls back to `cas/cas.<projectType>`. When a Spring Boot bump fails
  every AOT scenario at once, diff `loader/spring-boot-jarmode-tools/.../ExtractCommand.java` between the two tags first.
- `ci/tests/aws/run-aws-server.sh` serves the `AmazonWebServices` unit test category, not only scenarios: tests start as
  soon as it returns, and `@EnabledIfListeningOnPort(port = 4566)` passes while localstack's services are still starting.
  Components that create their AWS resources while starting (the log4j2 `SQSAppender` creates its queue in `start()`, and
  log4j drops an appender whose start fails) then fail the test. The script removes any previous container with
  `docker rm -f`, waits until it is gone (a `--rm` container still being removed makes `docker run --name` conflict), and
  polls `/_localstack/health` until every service it enables is `available` or `running`, bounded, `exit 1` on timeout.
- A proxy callback (`pgtUrl`) is contacted by CAS itself (`ProxyAuthenticationHandler` expects an acceptable status), so
  it must be a local endpoint: GitHub stopped answering requests from the CI runners with an acceptable status, which
  failed `ticket-validation-casv3-pgt` and the `cas` unit tests using `https://www.github.com`. Scenarios use httpbin
  (`https://localhost:9859/anything/...`, with `--cas.http-client.host-name-verifier=none` for its certificate); unit
  tests use `new MockWebServer(true)` with `cas.http-client.host-name-verifier=none`, which covers the context's http
  client only: a `Cas20ProxyHandler` built in the test (delivers the PGT IOU) needs its own client on
  `CasSSLContext.disabled()`, or the IOU callback fails with PKIX path errors. A public URL is fine only where
  nothing contacts it (a redirect target, a service id, a failure path).
- WebAuthn scenarios drive the CDP virtual authenticator (`cas.createWebAuthnVirtualAuthenticator(page, protocol, resident, options)`
  spreads `options` into the authenticator, e.g. `isUserVerified`). `cas.setWebAuthnSignCount(device, n)` replaces each
  credential with the same key and a different sign count, which is how `mfa-webauthn-register-logout-login` proves a
  cloned authenticator (count going back) is refused; `WebAuthn.setUserVerified` toggles user verification mid-scenario.
- The OIDC front-channel logout page (`casPropagateLogoutView`) calls each `logoutUrl` with JSONP, so the front-channel
  request shows up as a script request in the browser. Capture it with `page.on("request")` filtered on the URL and read
  `iss`/`sid` from its query; httpbin answers it with JSON, which the page ignores.
- `cas.generateOtp(config, stepOffset)` builds the code for a neighbouring time step without waiting; use it to test
  `window-size` (the next step is accepted with the default window, refused with `window-size=1`) and replay
  (`USED_OTPS` records the codes already sent). `parseOtpAuthenticationUrl` reads `digits` and `period` from the URI.
- A token sent by a client may be a JWT (`jwtAccessToken`, `jwtRefreshToken`). Every lookup of a client-supplied token in
  the ticket registry must first go through `OAuth20JwtAccessTokenEncoder.toDecodableCipher(...).decode(token)`, which
  returns non-JWT values unchanged; revocation and the refresh grant skipped this. `OAuth20RefreshTokenAuthenticator`
  (public clients presenting a JWT refresh token) still does, because it has no `JwtBuilder`.
- `.gitignore` drops every `ci/tests/puppeteer/**/*.jwks`: client key sets a scenario ships must use another extension
  (`client-public.json`), or the scenario passes locally and fails in CI with a missing file.
- The REST multifactor trigger (`cas.authn.mfa.triggers.rest`) sends a GET with a JSON body unless `method` is set; a mock
  endpoint registered with `app.post` never sees it. Set `cas.authn.mfa.triggers.rest.method=POST`.
- `WebAuthn.getCredentials` omits `rpId` for non-resident credentials, and `WebAuthn.addCredential` refuses a credential
  without one; `cas.setWebAuthnSignCount` passes the relying party (default `localhost`).
- Navigating to the page already shown plus a `#fragment` does not reload it: to see data imported through an actuator,
  `cas.goto` the page without the fragment, then click the tab.
- The OIDC per-service "signing" key set (`fetchJsonWebKeySetForSigning`, `InternalJwtAccessTokenCipherExecutor`) also
  verifies what the application signs (request objects via `DefaultOAuth20RequestParameterResolver`), so public-only keys
  must stay in it; only keys marked `use=enc` may be dropped.
- CAS shell validation: the `quit` command deliberately returns EXECUTION_ERROR and Spring Shell then prints a
  CommandExecutionException trace; `.github/workflows/validation.yml` only counts exception lines before the quit message.
- SPNEGO scenarios get a real KDC from `ci/tests/kerberos/run-kerberos-server.sh` (port 10088, not 88: macOS runs its own
  KDC on 88). Chrome sends Negotiate tokens only with `--auth-server-allowlist=localhost`; it reads `KRB5_CONFIG` and
  `KRB5CCNAME` from the environment given to `cas.newBrowser` (a missing cache makes it show the 401 body, which is the
  login form in mixed mode). CAS must get the port from `kerberos-conf`: blank `cas.authn.spnego.system.kerberos-kdc` and
  `kerberos-realm`, because Java ignores krb5.conf once both system properties are set and they cannot carry a port.
  jcifs-ext logs the service principal in with `jcifs-service-password` only when `javax.security.auth.useSubjectCredsOnly`
  is true (`cas.authn.spnego.system.use-subject-creds-only=true`). With the CAS default (false) it skips its JAAS login
  and the JDK looks for a keytab instead, which fails with "Cannot find key of appropriate type to decrypt AP-REQ".

## Storage backend scenarios (service registry, SAML2 metadata, MFA devices, consent)

- One scenario covers several storage backends of a feature through `variations` in `script.json`: `run.sh` runs the
  scenario once per variation, adding its properties and exporting `SCENARIO_VARIATION`. The build carries every
  backend module, and each variation switches the others off with `CasFeatureModule.<Feature>.<module>.enabled=false`
  (Google Authenticator `jpa/redis/mongo/ldap/dynamodb`, trusted devices `jdbc/redis/rest/dynamodb`, consent
  `ldap/mongo/redis/rest/dynamodb`, WebAuthn `jpa/mongo/redis/ldap/dynamodb/rest`); the init scripts start every backend. Examples:
  `mfa-webauthn-register-logout-login`, `mfa-gauth-login-encrypted`, `mfa-gauth-login-trusted-devices-storage`
  (formerly `-jdbc`), `attribute-consent-storage`, `aup-login-storage`, `authn-throttle-storage` (formerly
  `authn-throttle-with-jdbc`; each throttle reads its own store, so the audit module it needs stays on: JDBC and
  MongoDb throttles read the audit trail), `passwordless-login-storage` (formerly `passwordless-login-jpa`; the mongo
  variation also keeps the account in MongoDb), `http-session-storage` (formerly `http-session-mongodb`). Storage modules that share a bean name without a module toggle cannot
  ship together; WebAuthn's had none until its storage auto-configurations got `module = "..."`.
- Each such scenario also reads the backend directly with `cas.dockerExec(container, args)` (containers: `mariadb`,
  `postgres-server`, `mongodb-server`, `redis_server_master`, `ldap-server`, `localstack`), so a variation that silently
  fell back to the in-memory default fails. LDAP-backed repositories that keep records in an attribute of the user's
  entry (Google Authenticator, consent: `description`, `search-filter=cn={0}`) need the entry first:
  `ci/tests/ldap/add-ldap-entries.sh <ldif>` waits for `ldap-server` and adds it. The 389ds schema has no CAS
  attributes, so point the attribute setting at a standard one the entry's object classes allow (`description`,
  `carLicense`, `postalCode` on `inetOrgPerson`).
- CAS Redis templates write values LZ4-compressed and JDK-serialized, so `redis-cli GET` shows nothing readable;
  check `EXISTS` or `--scan` for keys instead.
- Storage tests that extend `BaseAcceptableUsagePolicyRepositoryTests` only check that a submitted decision is read back
  when `hasLiveUpdates()` is true; the MongoDb and Redis repositories stored decisions without ever reading them, and
  their tests did not notice. Use a random principal there, since the backend outlives the test.
- `cas.doDelete(url)` swallows failures unless a failure handler is given; assert a status with
  `cas.doRequest(url, "DELETE", headers, status)`.
- Read JPA tables by looking their names up (`information_schema`), not by entity name: CAS's physical naming
  strategy turns `JpaMultifactorAuthenticationTrustRecord` into `jpa_multifactor_authentication_trust_record`, and a
  `LIKE '%WebAuthn%'` also matches the `webauthn_sequence` table. MariaDB `LIKE` ignores case.
- The `mfa-gauth-login-trusted-devices-storage` outage check needs the storage to report itself unavailable when its
  backend is down (`MultifactorAuthenticationTrustStorage.isAvailable`): the webflow then skips trusted devices and asks
  for the token. JPA checks a connection; Redis pings.
- Service registry scenarios call `cas.verifyServiceRegistry(backend)` (in `cas.js`), passing `insert(definition)` (write
  straight into the backend, bypassing CAS) and `contains(id)`. It checks load (scheduled reload picks up the external
  definition), save through the `registeredServices` actuator, rename, and delete, each confirmed in the backend and after a
  reload. Such a scenario needs `cas.service-registry.core.init-from-json=false`, because with it on the embedded JSON
  registry joins the chain and answers in place of the backend, plus a 2 s `cas.service-registry.schedule` and httpbin.
  `service-registry-ldap` (389ds `ldap-server`, `ou=services`, object class `account`) and `service-registry-aws-s3`
  (localstack, `docker exec localstack awslocal s3api`) are the examples.
- A property value with a space (`cn=Directory Manager`) goes through `environmentVariables` and `${VAR}` in
  `script.json`, as `password-sync-ldap` does.
- localstack accepts bucket names real S3 rejects, so a scenario cannot catch an invalid bucket name; assert names in a unit
  test. `AmazonS3ServiceRegistry.determineBucketName` keeps `[a-z0-9.-]`, collapses `..`, and caps the name at 63 characters
  with `-<id>` last. Object metadata is built only from non-null values (`Map.of` rejects null).
- SAML2 metadata source scenarios (`saml2-idp-login-metadata-redis`, `saml2-idp-login-metadata-git`, and the older
  `saml2-idp-login-sp-metadata-mongodb`) keep IdP metadata in the backend, fetch it with `curl` in `ready.sh` before
  starting SimpleSAMLphp, and manage SP metadata through
  `samlIdPRegisteredServiceMetadata/managers/<ResolverSimpleClassName>`; its `DELETE` consumes form-encoded requests and
  answers 204. The Git scenario uses a local bare remote under `/tmp/cas-saml-metadata-git` (`push-changes=true`, or the
  IdP metadata is only committed locally) and commits SP metadata to it by hand, the way the docs describe.
- `GitSamlRegisteredServiceMetadataResolver` reads `sp-metadata/<id>-<name>.xml` (written by CAS) and `<name>.xml` (by hand):
  the id prefix counts only when numeric, and a hand-committed file's id is derived from its name, so it is stable across
  loads.
- DynamoDB variations and scenarios read DynamoDB Local (`dynamodb-server`, port 8000, no CLI inside) over HTTP with
  `cas.dynamoDbScan(table)` and `cas.dynamoDbRequest(operation, payload)` in `cas.js`, which sign requests with SigV4.
  DynamoDB Local keeps one database per access key and region, so give CAS explicit
  `credential-access-key=AKIAIPPIGGUNIO74C63Z`, the matching secret key and `region=us-east-1` (not `local-instance=true`,
  which takes credentials from the environment), plus `drop-tables-on-startup=true`. Items come back in the wire format
  (`item.principal.S`). For an outage (`docker pause dynamodb-server`) set `connection-timeout`, `socket-timeout` and
  `client-execution-timeout` to about `1000`, or each SDK call retries for many seconds.
- REST backends in scenarios use an in-script Express server (`aup-login-rest`, `attribute-consent-storage`'s `rest`
  variation, `audit-rest`, ports 5611-5619; 5619 is the REST multifactor trigger of `mfa-provider-selection-trigger-groovy`). A REST configuration source must answer before CAS boots, so
  `configuration-properties-rest` starts `config-server.js` from `init.sh` and stops it in `exit.sh`.
- Service ids in scenario JSON are regular expressions: `^https://localhost:9859/anything/1` also matches `/anything/10`,
  so give new services distinct path names. A scenario that sets `cas.service-registry.json.location` has no catch-all
  service, which is what lets it assert that a URL is refused.
- The login page is a webflow response, which sets its own cache headers; check per-service security headers on
  `/cas/p3/serviceValidate?service=...` instead (`service-security-headers`).
- A ticket-granting ticket's last used time changes only when a ticket is issued from it, not when the login page is shown;
  time-based SSO participation and throttled timeouts are measured from that.
- Values encrypted with a service public key use RSA with PKCS#1 v1.5 padding, which Node no longer decrypts: decrypt with
  `RSA_NO_PADDING` and drop everything up to the first zero byte after the second byte.
- localstack scenarios (`configuration-properties-aws`, `simple-mfa-login-aws-ses`, `logging-config-sqs`,
  `audit-aws-firehose`) seed and read AWS resources with `docker exec localstack awslocal ...`; SES mail is read from
  `http://localhost:4566/_aws/ses`.
- `template-accessibility` runs axe-core (`@axe-core/puppeteer`) and fails on serious or critical WCAG 2.1 A/AA
  violations only. Element ids must be unique per page: a duplicate id referenced by `aria-describedby` or `label for`
  is a critical violation (the Google Authenticator page with multiple-device registration on still has two `#token`).
  `layout.html` fades the page content in (`.fade-in`, 0.7 s, opacity and blur), so axe run right after a navigation
  reports `color-contrast` on everything in the card; wait for `document.getAnimations()` that end (skip infinite ones,
  such as the notification bell) before analyzing.
- The puppeteer webapp runs without `--add-opens java.base/java.util`, but Gradle tests open it. Code that reflects on
  private JDK fields (Kryo serializers in the Memcached registry) passes unit tests and fails in scenarios and plain
  `java -jar` deployments.
- Servlet filters that extract the service treat `UnauthorizedServiceException` as an unauthorized service (status 403,
  `RequestDispatcher.ERROR_EXCEPTION`, continue the chain), and MVC interceptors ignore it, leaving the refusal to the
  login flow. Thrown from either, its `@ResponseStatus` sends a 403 whose error dispatch fails the same way: empty body.
- Spring Cloud closes the bootstrap context once a refresh is done, so a `PropertySourceLocator` that closes its client
  in `destroy()` leaves the located property source with a closed client. The AWS configuration sources take a
  `Supplier` of clients and open and close one per refresh or change.
- CAS reads a plain number in a `@DurationCapable` setting as seconds (`Beans.newDuration`), so `socket-timeout=1000`
  means 1000 seconds; write durations as `PT1S`.
- Every CAS MongoDb module pulls in Spring Boot's MongoDb data auto-configuration, whose `gridFsTemplate` needs exactly
  one injectable `MongoTemplate`. A module with zero (only `MongoOperations` beans) or two `MongoTemplate` beans breaks
  startup unless its templates are `@Bean(defaultCandidate = false)` and injected by `@Qualifier`; Boot then creates
  its own template. Every CAS MongoDb template is a non-default candidate except the ticket registry's (`@Primary`);
  keep it that way for new ones. `mfa-gauth-trusted-devices-mongo` builds two MongoDb modules together.
- The Cassandra and Memcached ticket registries encrypt by default with random keys, so multi-node scenarios set shared
  `cas.ticket.registry.<registry>.crypto.encryption.key`/`signing.key`. Cassandra in CI uses client TLS: set
  `cas.http-client.host-name-verifier=none` and `ssl-protocols=TLSv1.2`, and read it with
  `docker exec cassandra cqlsh --ssl -e "<query>"` (unquoted table names are lowercase: the TGT table is `cas.ticketgrantingticketstable`). Memcached: `stats` over `nc`; the ticket registry also needs `memcached-spy` (or `memcached-aws-elasticache`) in `dependencies`.
- CAS Redis values are a 4-byte big-endian length followed by an LZ4 block of the Java-serialized object; a fixture such as
  `redis-authentication-login/accounts.json` must be regenerated when the serialized class changes.
- The Kafka console consumer can exit non-zero on `--timeout-ms`; read its output with `spawnSync`. The RADIUS test server
  takes `casuser`/`Mellon`, secret `testing123`, PAP; both RADIUS modules need `client.inet-address` set.
- WebAuthn LDAP keeps registrations in an attribute of the user's entry (`description` on 389ds). The OIDC MongoDb
  keystore needs both `cas.authn.oidc.jwks.mongo.host` and `.collection`; client JWKS go to `<collection>-ClientJwksStore`.
- The Google Authenticator JSON repository writes used scratch codes back to its file, so a scenario with variations or
  retries runs out of codes. Copy the account file to `${TMPDIR}` from an init script (runs per variation) and point
  `cas.authn.mfa.gauth.json.location` at the copy, as `mfa-gauth-login-trusted-devices-storage` does; unpause paused
  containers in a `finally`.
- Scenarios that change a backend must put it back at the start, because `run.sh` retries a failed script against the
  same CAS instance and containers (`configuration-properties-aws` does this). Keep property values free of spaces
  (`run.sh` cuts them), e.g. `mail.text=Username:${username}`.
- A SAML2 metadata module with `SAMLIdentityProviderMetadata` on replaces the file-system IdP metadata beans even when
  its own IdP metadata is off; scenarios that keep IdP metadata on disk switch it off with
  `CasFeatureModule.SAMLIdentityProviderMetadata.<module>.enabled=false`.

## Actuator endpoint coverage in scenarios

- `samlIdPRegisteredServiceMetadata`: every operation in `saml2-idp-login-metadata-redis` (and `-git`); `oidcVcStatus`:
  `oidc-verifiable-credentials`, reading the published status list back (2 bits per entry, LSB first, zlib, base64url);
  `redisTicketsCache`: `redis-ticket-registry-loadbalanced` (the cache is per node; another node's delete evicts it);
  `qrDevices`: `qrauthn-login`, which needs `cas.authn.qr.json.location` (without it every device is authorized and
  nothing is stored); `passwordless`: `passwordless-delegated-login`; `duoPing`, `duoAccountStatus`, `duoAdmin`:
  `mfa-duo-universal-login` (runs only where the Duo secrets exist) and, without providers, `actuator-endpoint-roles`.
- `gcpLogs` has no scenario: it calls the real Cloud Logging API, no emulator exists, and adding
  `gcp-logging` to a build pulls in Spring Cloud GCP's Stackdriver trace auto-configuration, which stops CAS
  from starting without Google credentials (exit code 4 in `actuator-endpoint-roles`).
- `qrauthn-login` logs in over a STOMP callback; `connectAndLogin` returns a promise that settles when that
  callback's assertions finish, and the browser is closed only after it, or the assertions hit a detached frame.

## Google Authenticator credential repositories (all backends)

- One puppeteer scenario can cover several storage backends: put every backend module in `dependencies`, list the init scripts comma-separated in `initScript`, and give each backend a `variations` entry that turns the others off with `--CasFeatureModule.<Feature>.<module>.enabled=false` (each backend auto-configuration carries `@ConditionalOnFeatureEnabled`). `SCENARIO_VARIATION` holds the variation name. `mfa-gauth-login-encrypted-stores` does this for GAuth on JPA and Redis, with encryption on.
