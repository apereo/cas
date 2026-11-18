---
name: cas-puppeteer-runner
description: "How puppeteer scenarios are built, started and retried: ci/tests/puppeteer/run.sh (init and bootstrap scripts, instance builds, linting, properties, exit codes, timeouts), the CI matrix jobs in functional-tests.yml, tests.yml and native-tests.yml (Gradle and Node caches, Docker image prefetch, retries, AOT training), and the test servers and containers under ci/tests/**/run-*.sh. Use when changing run.sh, an init script, a CI scenario job or a test server, or when a scenario fails for reasons outside its script."
---

# Puppeteer runner, CI jobs and test servers

Writing the scenario scripts themselves is in the `cas-puppeteer-scenarios` skill.

## run.sh and init scripts

- `ci/tests/puppeteer/run.sh` runs a scenario's `initScript` entries with `eval "source ${script}"`, so they execute in the runner's own shell. An `exit` on the success path therefore terminates the whole scenario run, which looks like the scenario dying silently right after the init script's last line of output. Let a successful init script fall off the end, and reserve `exit 1` for the failure path, which is what that exit is there for. `ci/tests/ldap/run-ad-server.sh` still carries an `exit 0` early return for the already-running case and has the same hazard.
- Init scripts are sourced, so `set -e`, `set -u` or `pipefail` in one would stay on for the rest of `run.sh`; 17 scenarios source such a script, and a leaked `set -e` made a failing test exit `run.sh` at its first attempt with code 1 (whole-script retry) instead of 5 after its in-process attempts. `run.sh` turns those options off after each init script, so do not rely on them persisting.
- `initScript` entries run once *per instance*, right before that instance starts; `bootstrapScript` runs once for the scenario. A multi-instance scenario whose nodes share a backend (Pulsar, MySQL, MongoDB, Kafka, Redis) must start it from `bootstrapScript`: as an init script it is restarted under the first node before the second one starts, which wipes its state and leaves node 1's connections to reconnect (or not) to a fresh server. The symptom is node 1 silently missing everything node 2 publishes.
- Instances whose resolved dependencies are identical share one build: `run.sh` builds only the first instance of each dependency set and copies its artifact to the others. Only instance-specific `dependencies` cause another build. Instances still start one after another, because several multi-instance scenarios need instance 1 up before instance 2 starts (Spring Boot Admin client registration, passive service-registry replication, cas2cas delegation).
- `run.sh` joins a scenario's `properties` with spaces and passes them unquoted, so a value containing a space is cut at the space (`display-name=My CAS One` arrives as `My`). Use values without spaces in `script.json`.
- `run.sh` lints the scenario before running it and stops on any ESLint error. Page globals used inside `page.evaluate` or `page.waitForFunction` callbacks (`$`, `ace`, Palantir classes) must be named in the script's `/* global ... */` header, or `no-undef` fails the job; run `npx eslint scenarios/<name>/script.js` from `ci/tests/puppeteer` before handing a scenario off.
- `run.sh` installs the Chrome build Puppeteer pins (`npx puppeteer browsers install chrome`) after `npm install`, because npm can finish without puppeteer's postinstall having downloaded it, which only shows up later as `Could not find Chrome`.
- `run.sh` exit codes carry meaning for the `Run Tests` retry (`retry_on_exit_code: 1`): 1 is a setup failure worth another attempt (init scripts, containers, npm), 2 a failed build, 3 a build that exceeded `PUPPETEER_BUILD_CTR`, 4 a CAS instance that exited or did not answer its health check within `PUPPETEER_STARTUP_TIMEOUT` seconds (300 in CI, unlimited locally; a single probe may use the whole remaining budget, since some login pages take ~30 s to render, e.g. `thymeleaf-templates-rest` before its template server is up), and 5 a scenario script that still failed after its in-process attempts (3 in CI, against the running server). 2-5 fail at once; a whole-script retry cannot fix them and repeats the build and startup. Keep new failure paths on 1 only if a rerun can help.
- In CI, `run.sh` bounds each script attempt with `PUPPETEER_SCRIPT_TIMEOUT` (420 seconds; 0 locally, meaning no limit). A script whose steps all finish but whose node process never exits then fails that attempt and is retried, instead of running into the 20 minute job timeout.

## CI matrix jobs, caching and retries

- In CI, `run.sh` launches the Gradle build (`bootWar`, or `bootJar` for starter scenarios; native keeps `build` + `nativeCompile`) in the background and runs npm install, ESLint, bootstrap and init scripts while it builds, waiting on the build process only before launching the CAS instance that needs it. Init and bootstrap scripts must therefore never depend on the built artifact, and a fixed `sleep` in them now runs while the build competes for CPU, so poll instead. `PUPPETEER_BUILD_OVERLAP=false` restores the sequential order. `PUPPETEER_BUILD_CTR` is the build timeout in minutes, measured from launch.
- Scenario matrix jobs in `functional-tests.yml` restore the Gradle User Home with `cache-read-only: true` and do not set `cache: 'gradle'` on `setup-java`; saving from every one of the ~560 jobs cost ~12 s each and churned the Actions cache, and the two actions caching the same directory conflict.
- The matrix jobs cache the Node.js install under `${{ runner.tool_cache }}/node/<NODE_VERSION_REQUIRED>` with a key that is that exact version, restored before `setup-node` and saved only from the default branch on a miss. A version change in `NODE_CURRENT` or a scenario's `requirements.nodejs` is a new key, so `setup-node` downloads the instructed version; keep those values exact versions (a range would never match the cached directory) and do not add `check-latest`.
- The matrix jobs start `ci/tests/puppeteer/prefetch-docker-images.sh <scenario>` right after checkout (in `native-tests.yml`, after `Configure CI` and only when the scenario's native run is enabled, since init scripts run only in the `--nr` step and the pulls then finish during the native build): it finds the images the scenario's bootstrap, init and ready scripts need (their `docker run`/`pull`/`build` commands, `*_IMAGE` and `*COMPOSE_FILE` assignments, compose files, Dockerfile `FROM` lines, and the `ci/tests` scripts they call) and pulls them in the background while the job sets up, logging to `$RUNNER_TEMP/docker-prefetch.log` (printed by the `Docker Prefetch Summary` step). Images a script builds itself, compose services with `build:` and run.sh's own `cas-<scenario>` images are skipped, and so are scenarios that are disabled or miss a `conditions.env` variable (run.sh skips those too). It is best-effort: an image it misses or is still pulling is pulled by the init script as before, so new init scripts need no changes, but keep image references literal or in a plain `*_IMAGE=` assignment so they are found. `--list` prints what it would pull. Images are not stored in the Actions cache: it is already at its limit, and restoring a tarball is a download too.
- `tests.yml`, `functional-tests.yml` and `native-tests.yml` retry failures the same way: a boolean `retryFailedScenarios`
  dispatch input gates a step that runs `ci/tests/select-failed-jobs.sh <list.json>`, which narrows the listed
  categories/scenarios in place to the jobs that failed, timed out or were cancelled in the latest attempt of the
  workflow's previous completed run, and writes a step summary. Call that script from any other matrix workflow; do not
  inline the logic. It matches job names, so a matrix job's `name:` must stay exactly the category or scenario, and the
  listing job needs `permissions: actions: read`. No matching job leaves `[]`, so every matrix job that consumes the list is
  skipped when it is empty (an empty matrix fails the job). A category retry disables PTS and coverage and skips the
  `coverage` job, which would otherwise upload a partial report.
- In CI, `cas.prepareChromium` unzips the Chromium profile only when it is missing, so the in-process attempts of a
  script share one profile and a retry starts with the cookies the failed attempt left: the login form is gone and
  `cas.loginWith` times out on `#username`. Sign in only when the form is there
  (`if (await cas.isVisible(page, "#username"))`, as `palantir-login` does), or the first failure hides itself behind
  three identical login timeouts.
- AOT training runs extract the built artifact with `-Djarmode=tools extract` and launch what it produces. Since Spring Boot
  4.2 (gh-51372) extracting a war writes `cas/cas.jar` (root web content moved to `META-INF/resources/`), no longer
  `cas/cas.war`; `run.sh` launches `cas/cas.jar` and falls back to `cas/cas.<projectType>`. When a Spring Boot bump fails
  every AOT scenario at once, diff `loader/spring-boot-jarmode-tools/.../ExtractCommand.java` between the two tags first.

## Test servers and containers

- Prefer polling the service's own port over a container health check, and dump `docker compose logs` on the failure path: an unhealthy container tells you nothing, while the service's logs say why it would not start.
- A container answering HTTP is not a container ready for setup calls. Apache Syncope's Tomcat answers `/syncope/` several seconds before its content loader populates the empty Master domain, and admin REST calls made in that window fail with `AuthorizationDeniedException` (seen once the build overlapped the init scripts and slowed startup). `ci/tests/syncope/run-syncope-server.sh` therefore waits for `Started SyncopeCoreApplication` in the container log; readiness waits in other init scripts should likewise key off the application's own started signal, bounded, with `exit 1` on timeout.
- `ci/tests/keycloak/run-keycloak-server.sh` polls the `cas` realm's discovery document; Keycloak answers HTTP well before the realm import finishes, and a pac4j OIDC client that fetched its metadata in that window stays broken for every later in-process attempt.
- `ci/tests/otel/run-otel-server.sh` keeps the collector's own logger at `WARN` and still follows its logs, so only
  problems reach the scenario output; the `debug` exporter writes through that logger and is silent at that level (set it
  to `INFO` locally to see every batch). It publishes only the OTLP ports (4317/4318; 8888 belongs to the config server
  scenarios) and polls `POST /v1/traces` for a 200 rather than sleeping.
- A scenario that navigates to `https://localhost:9859/anything/...` needs `ci/tests/httpbin/run-httpbin-server.sh` among its init scripts; without it the redirect lands on `chrome-error://` and the ticket assertion fails.
- The `oidc-login-strapi` image must install Corepack explicitly before `corepack enable` on Node 26: Node stopped
  bundling it in v25. Check the pinned Strapi creator's published `engines.node`, not only the latest documentation;
  `create-strapi@5.43.0` excludes Node 26, while `5.61.1` accepts it and generates a SQLite app with `better-sqlite3@12.11.1`.
- `ci/tests/gcp/Dockerfile` builds the Firestore and Pub/Sub emulators on the pinned `google-cloud-cli` Alpine image and
  installs the JDK the emulators need. Do not run `gcloud components update` there: it pulls the newest emulators onto
  the pinned image, so the same commit can pass in the morning and fail in the afternoon. Firestore emulator v1.23.0
  (Cloud SDK 592) requires Java 25, and the container died within the 5 second start check on Java 21. When Renovate
  bumps the image, check the Cloud SDK release notes for emulator Java requirements.
- `ci/tests/aws/run-aws-server.sh` serves the `AmazonWebServices` unit test category, not only scenarios: tests start as
  soon as it returns, and `@EnabledIfListeningOnPort(port = 4566)` passes while localstack's services are still starting.
  Components that create their AWS resources while starting (the log4j2 `SQSAppender` creates its queue in `start()`, and
  log4j drops an appender whose start fails) then fail the test. The script removes any previous container with
  `docker rm -f`, waits until it is gone (a `--rm` container still being removed makes `docker run --name` conflict), and
  polls `/_localstack/health` until every service it enables is `available` or `running`, bounded, `exit 1` on timeout.
- SPNEGO scenarios get a real KDC from `ci/tests/kerberos/run-kerberos-server.sh` (port 10088, not 88: macOS runs its own
  KDC on 88). Chrome sends Negotiate tokens only with `--auth-server-allowlist=localhost`; it reads `KRB5_CONFIG` and
  `KRB5CCNAME` from the environment given to `cas.newBrowser` (a missing cache makes it show the 401 body, which is the
  login form in mixed mode). CAS must get the port from `kerberos-conf`: blank `cas.authn.spnego.system.kerberos-kdc` and
  `kerberos-realm`, because Java ignores krb5.conf once both system properties are set and they cannot carry a port.
  jcifs-ext logs the service principal in with `jcifs-service-password` only when `javax.security.auth.useSubjectCredsOnly`
  is true (`cas.authn.spnego.system.use-subject-creds-only=true`). With the CAS default (false) it skips its JAAS login
  and the JDK looks for a keytab instead, which fails with "Cannot find key of appropriate type to decrypt AP-REQ".
