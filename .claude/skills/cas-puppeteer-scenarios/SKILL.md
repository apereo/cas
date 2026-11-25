---
name: cas-puppeteer-scenarios
description: "Write, fix and review the end-to-end puppeteer scenarios under ci/tests/puppeteer/scenarios: script.json properties and variations, browser and cas.js helpers, the server behavior a step should assert (errors, refusals, protocol responses), storage-backend scenarios (JPA, Redis, Mongo, DynamoDB, LDAP, REST, localstack), actuator coverage and multi-server scenarios. Use when writing or changing a scenario script, or cross-checking a change against its scenarios; for run.sh, CI jobs and test servers use cas-puppeteer-runner."
---



# CAS puppeteer scenarios

## Browser and cas.js helpers

- Drive jQuery UI select menus in dialogs through the widget (`selectmenu("open")`, then the `menuWidget()`'s `menu("focus", null, item)` and `menu("select", $.Event("click", {target}))`), not with a coordinate click on the item, which misses when the dialog scrolls; then wait for the result with `page.waitForFunction` rather than asserting at once.
- `cas.newPage(browser)` returns the browser's first tab when one exists; it opens a new one only with `cas.newPage(browser, true)`. A helper that takes a page, intercepts its requests and closes it must force a new page, or it closes the scenario's main page and later calls fail with "Attempted to use detached Frame" (`saml2-idp-login-acs-signed`).
- After a redirect to a `custom://` scheme, the tab stops taking keyboard input in CI (most likely Chrome's tab-modal external-protocol prompt): `cas.type` and `cas.pressEnter` report success but the form is never submitted, so a later `cas.loginWith` times out while navigation still works and CAS logs nothing. Continue on a fresh tab (`cas.newPage(context, true)`) after a custom-scheme step (`oidc-logout`).
- Palantir's Users tab puts the focus in its username field once the tab has loaded, asynchronously, so a hotkey such as "/" pressed soon after opening it is typed into the field instead (`palantir-search-users` opens another tab before testing "/").
- WebAuthn scenarios drive the CDP virtual authenticator (`cas.createWebAuthnVirtualAuthenticator(page, protocol, resident, options)`
  spreads `options` into the authenticator, e.g. `isUserVerified`). `cas.setWebAuthnSignCount(device, n)` replaces each
  credential with the same key and a different sign count, which is how `mfa-webauthn-register-logout-login` proves a
  cloned authenticator (count going back) is refused; `WebAuthn.setUserVerified` toggles user verification mid-scenario.
- `cas.generateOtp(config, stepOffset)` builds the code for a neighbouring time step without waiting; use it to test
  `window-size` (the next step is accepted with the default window, refused with `window-size=1`) and replay
  (`USED_OTPS` records the codes already sent). `parseOtpAuthenticationUrl` reads `digits` and `period` from the URI.
- `WebAuthn.getCredentials` omits `rpId` for non-resident credentials, and `WebAuthn.addCredential` refuses a credential
  without one; `cas.setWebAuthnSignCount` passes the relying party (default `localhost`).
- Navigating to the page already shown plus a `#fragment` does not reload it: to see data imported through an actuator,
  `cas.goto` the page without the fragment, then click the tab.
- `.gitignore` drops every `ci/tests/puppeteer/**/*.jwks`: client key sets a scenario ships must use another extension
  (`client-public.json`), or the scenario passes locally and fails in CI with a missing file.
- X.509 scenarios present the client certificate from their own folder (`trustStoreCertificateFile` /
  `trustStorePrivateKeyFile` in `script.json`): their `init.sh` re-signs that `cert.pem` and imports it into the
  scenario's `truststore.jks`. `x509-login-passwordless` presented `x509-login`'s certificate, trusted only because it
  shares the subject and key, and every handshake failed once the runners moved to Ubuntu 26.04. A failed handshake in
  the `request`-based interception shows up as `ERR_CONNECTION_REFUSED`, and `cas.logr` logs only its first argument,
  so put the error in the message.

## Server behavior to assert

- Strapi 5.61.1's CAS provider requires `email_verified` to be boolean `true`; seed it for the scenario's fixture user
  and allow its release. CAS's OIDC profile mapper converts the stub's `true` string into a boolean. Strapi keeps upstream
  tokens in its session and redirects to `/api/auth/cas/callback` without token query parameters; assert the callback's
  successful JSON response (`jwt` and the CAS user's identity), not an `id_token` in the browser URL.
- Webflow executions are client-side by default (`cas.webflow.session.storage=false`): the `execution` value is the signed and encrypted flow state, so replaying one simply resumes that state, and "reusing a consumed execution" is only meaningful with server-side storage (`http-session-ticket-registry` covers it). Server-side keys are numbered per session (`e1s1`, `e2s1`, ...), and a new HTTP session numbers from `e1` again, so opening the login page before the replay hands out the very key being replayed and the "replay" logs in. Replay from a non-CAS page, after deleting the TGC with `page.deleteCookie({name: "TGC", url: "https://localhost:8443/cas/"})` (`cas.deleteCookies(page)` only sees cookies of the page's current URL, which on httpbin excludes the `/cas` path). A well-formed but altered execution fails decoding with `ClientFlowExecutionRepositoryException`, and `FlowExecutionExceptionResolver` redirects to a fresh flow (302). A malformed one (no `uuid_data` shape) raises `BadlyFormattedFlowExecutionKeyException`, which the resolver now also restarts; it drops any `execution` query parameter from the redirect, which is what avoids the redirect loop that once made it skip this exception. `webflow-clientside-sessions` exercises all three cases plus a double submit and the back button. The ticket-granting cookie value embeds the `User-Agent` when `cas.tgc.pin-to-session` is on; a request without one is pinned to a placeholder (`DefaultCasCookieValueManager`), so the cookie only matches later requests that also send none. It used to throw and answer 500.
- `actuator-endpoint-roles` is the table-driven check of endpoint security: every row is called anonymously (401), as `casviewer` without `ADMIN` (403) and as `casadmin` (2xx, or "reachable" for operations needing a payload). Users come from its `users.json` (`cas.monitor.endpoints.json.location`). Add a row there when adding an actuator endpoint whose module needs no external service; rows default to `Content-Type: application/json`, so give operations that consume form parameters (the `casValidate` ones) `contentType: FORM`, or they answer 415.
- To prove CAS refuses a tampered SAML2 AuthnRequest, intercept the browser hop from the SP to `/cas/idp/profile/SAML2/*/SSO` with `page.setRequestInterception(true)` (see `saml2-idp-login-acs-signed`). POST binding: rewrite `SAMLRequest` in `postData` and `request.continue({postData})`. Redirect binding: inflate with `zlib.inflateRawSync`, alter, deflate, then `request.abort()` and `cas.goto` the altered URL rather than overriding `url` on a redirected request. Assert that the tampering really happened, otherwise a request without the attribute passes for the wrong reason.
- A scenario that stubs an MFA verification service sees the provider's availability check as well as the real calls (YubiKey pings its API URL without parameters before the first verification), so pick the request to assert by its content, not by position (`mfa-yubikey-login-attribute-per-service`).
- The SAML2 identity provider releases attributes listed in its default attribute definitions (`samlidp-attribute-definitions.json`: `uid`, `mail`, ...) under their `urn:oid` names, with the plain name as `FriendlyName`. Scripts that read an assertion key attributes by `FriendlyName`, falling back to `Name` (`saml2-idp-login-encrypted`).
- With static users enabled (the default `casuser`), a wrong password for an LDAP user is reported as "account not recognized": the static handler's `AccountNotFoundException` outranks the LDAP handler's `FailedLoginException` in the webflow exception catalog. LDAP scenarios that assert login errors set `--cas.authn.accept.enabled=false` (`pm-must-change-password-ldap-ad`).
- Before writing a negative assertion, read the server code for the exact error. Each token-endpoint failure surfaces differently: device code reuse → `access_denied` (`InvalidOAuth20DeviceTokenException`), a mismatched ST service → `INVALID_SERVICE` and the ticket is consumed. Where the specification names the error (CIBA Core §11: `authorization_pending`, `invalid_grant`), the scenario asserts the specification even when the current code differs, and the hand-off says so.
- `from` in actuator and ticket registry queries (`ssoSessions?from=`, `TicketRegistryStreamCriteria`) is a 0-based offset in every registry. A scenario that deletes all SSO sessions passes `from=0` (or omits it); `from=1` leaves the first session alive, which only shows once that session outlives the scenario's other timeouts, such as a remember-me TGT in a browser profile that later browsers share.
- A proxy callback (`pgtUrl`) is contacted by CAS itself (`ProxyAuthenticationHandler` expects an acceptable status), so
  it must be a local endpoint: GitHub stopped answering requests from the CI runners with an acceptable status, which
  failed `ticket-validation-casv3-pgt` and the `cas` unit tests using `https://www.github.com`. Scenarios use httpbin
  (`https://localhost:9859/anything/...`, with `--cas.http-client.host-name-verifier=none` for its certificate); unit
  tests use `new MockWebServer(true)` with `cas.http-client.host-name-verifier=none`, which covers the context's http
  client only: a `Cas20ProxyHandler` built in the test (delivers the PGT IOU) needs its own client on
  `CasSSLContext.disabled()`, or the IOU callback fails with PKIX path errors. A public URL is fine only where
  nothing contacts it (a redirect target, a service id, a failure path).
- The OIDC front-channel logout page (`casPropagateLogoutView`) calls each `logoutUrl` with JSONP, so the front-channel
  request shows up as a script request in the browser. Capture it with `page.on("request")` filtered on the URL and read
  `iss`/`sid` from its query; httpbin answers it with JSON, which the page ignores.
- A token sent by a client may be a JWT (`jwtAccessToken`, `jwtRefreshToken`). Every lookup of a client-supplied token in
  the ticket registry must first go through `OAuth20JwtAccessTokenEncoder.toDecodableCipher(...).decode(token)`, which
  returns non-JWT values unchanged; revocation and the refresh grant skipped this. `OAuth20RefreshTokenAuthenticator`
  (public clients presenting a JWT refresh token) still does, because it has no `JwtBuilder`.
- The REST multifactor trigger (`cas.authn.mfa.triggers.rest`) sends a GET with a JSON body unless `method` is set; a mock
  endpoint registered with `app.post` never sees it. Set `cas.authn.mfa.triggers.rest.method=POST`.
- The OIDC per-service "signing" key set (`fetchJsonWebKeySetForSigning`, `InternalJwtAccessTokenCipherExecutor`) also
  verifies what the application signs (request objects via `DefaultOAuth20RequestParameterResolver`), so public-only keys
  must stay in it; only keys marked `use=enc` may be dropped.
- CAS shell validation: the `quit` command deliberately returns EXECUTION_ERROR and Spring Shell then prints a
  CommandExecutionException trace; `.github/workflows/validation.yml` only counts exception lines before the quit message.
- Negative-path facts learned while adding refusal checks (read from the code, confirm on first run):
  `RegisteredServiceResponseHeadersEnforcementFilter` answers 403 for any request whose `service` is unregistered or denied,
  validation endpoints included, so mismatch against a registered service; a TGT id passed as `ticket` is `INTERNAL_ERROR`, not
  `INVALID_TICKET_SPEC`. `OidcJwtAuthenticator` refuses a reused `jti`, so client assertions need a random one or in-process
  retries fail. PKCE failures (wrong, missing or replayed verifier) are client-authentication failures (401), not
  `invalid_grant`. `RequestParameterPolicyEnforcementFilter` matches `pattern-to-block` against the raw (encoded) request URI and
  takes `characters-to-forbid` as space-separated single characters. `ValidateCaptchaAction` refuses a blank token without
  calling the verify URL, and a reCAPTCHA v3 score equal to the threshold is refused. Trusted-device storages drop expired
  records on every read, so the cleaner is only observable in the backend itself (the JSON file).
- `palantir-login` was split by dashboard area into `palantir-login` (home, tabs, settings, shortcuts),
  `palantir-search-users` (command palette, user lookup, interrupt inquiry, throttles), `palantir-services-editor` (wizard,
  plain editor, edit conflicts, MFA policy), `palantir-services-saml` (metadata import and health), `palantir-services-actions`
  (history, compare, restore, duplicate, import, bulk actions) and `palantir-system` (read-only endpoints, modules, metrics,
  sign-ins). Add a new Palantir check to the scenario of its area, with only the modules that area needs.

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
- The JSON service registry writes every service CAS saves, such as automatically or dynamically registered clients,
  into its location. Point it at a fresh copy under `overlay/${SCENARIO}` made by `init.sh` (as `oidc-federation-end2end`
  does), never at the scenario's own `services/` folder, or a later run reuses stale registrations (an OpenID Federation
  RP registered with keys that no longer exist fails as "Request object signature or claims are invalid").
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
- One puppeteer scenario can cover several storage backends: put every backend module in `dependencies`, list the init scripts comma-separated in `initScript`, and give each backend a `variations` entry that turns the others off with `--CasFeatureModule.<Feature>.<module>.enabled=false` (each backend auto-configuration carries `@ConditionalOnFeatureEnabled`). `SCENARIO_VARIATION` holds the variation name. `mfa-gauth-login-encrypted-stores` does this for GAuth on JPA and Redis, with encryption on.

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

## Multi-server scenarios

- `oidc-federation-end2end` runs a CAS trust anchor (8443) and a CAS OP (8444) with two relying parties: go-oidfed OFFA
  (`oidfed/offa`, pinned, `https://localhost` on port 443 with host networking) and the scenario's Node RP (`rp.js`,
  `https://localhost:9861`, started inside the script). `init.sh` works in the git-ignored
  `overlay/oidc-federation-end2end/`: it exports the CAS keystore's key pair as PEM for both RPs (OpenSSL 3 needs the
  current PKCS12 format, LibreSSL on macOS the legacy one), builds the JVM truststore, pins the trust anchor's public key in
  OFFA's config, and writes the trust anchor's subordinate directory there, adding `offa.json` from OFFA's live entity
  configuration because OFFA generates its keys on first start. The OP requires a relying party trust mark: both RPs
  carry one issued by the trust anchor with its scenario key (`trust-mark.js`, also a CLI); `init.sh` puts OFFA's in
  `federation.extra_entity_configuration_data.trust_marks` (raw `trust_marks` claim). Not in `federation.trust_marks`:
  go-oidfed v0.14.0 parses a configured `trust_mark_jwt` with verification on and no keys, and OFFA exits at startup. OFFA's root page (`/`) shows the claims of its session,
  which is what the script asserts after logging in through `/login?op=<OP entity id>&next=...`. That page calls OFFA's
  own `/auth` from loopback, which fiber v3 refuses (`403 Untrusted Proxy`) unless `server.trusted_proxies` lists
  `127.0.0.1` and `::1`: without `TrustProxy`, `IsProxyTrusted()` is false. OFFA discovers OPs by walking the trust
  anchor's `/list` without parameters and fetching each subordinate's entity configuration (go-oidfed
  `SimpleEntityCollector`), every `op_discovery.local.entity_collection_interval` (10 s here); its first run happens
  before the trust anchor starts, so the script reloads `/login` until the OP's `data-value="<entity id>"` shows. The
  Node RP covers what OFFA does not send (trust chain header, PAR, client assertions addressed to the issuer) and the
  refused requests; the script also checks the trust anchor's `/list` filters directly. The OP also runs `oidc-vc` and
  publishes `metadata/op-trust-mark.jwt` (signed RS256 with the trust anchor's federation key, no `exp`; re-sign it with
  that key if the trust anchor's keys or the OP's entity id change), so the script checks `openid_credential_issuer`,
  `oauth_authorization_server` and a trust mark that verifies with the trust anchor's key in the OP entity configuration.
  Its OP is also an OID4VP verifier with the `openid_federation` prefix and `direct_post.jwt`: `wallet.js` (no walt.id,
  which lacks the prefix) gets a `dc+sd-jwt` credential through a pre-authorized offer (`vc-client` from `services/`),
  resolves CAS through the trust anchor with `rp.resolveEntity`, verifies the request object with the
  `openid_credential_verifier` signing keys, encrypts its response with `RSA-OAEP-256` to the OP keystore encryption key
  published there, and the script collects the verified claims. `trust-mark.js` issues relying party trust marks
  for the trust anchor: required by the Node RP and, as a CLI from `init.sh`, for OFFA. The waits before the OP may
  resolve an entity again (`RERESOLUTION_DELAY`) are read from the OP's `unresolved-entity-retention` in `script.json`;
  keep that setting in seconds (`PTnS`). Scenario `*.jwks` files are git-ignored (`ci/tests/puppeteer/**/*.jwks`)
  except under `oidc-federation-end2end/metadata/`: a scenario that needs committed keys must add its own exception to
  `.gitignore`, or `git status` will not show the file and CI will run without it.
- How `run.sh` builds, starts and retries scenarios, CI jobs and the test servers under `ci/tests/`: see the `cas-puppeteer-runner` skill.
