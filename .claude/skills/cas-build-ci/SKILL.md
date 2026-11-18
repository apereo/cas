---
name: cas-build-ci
description: "Gradle build settings (configuration cache, Isolated Projects, build cache, test-category discovery), the GitHub Actions workflows (tests.yml coverage job, Sonar and Codecov, setup-gradle caching, daemon reuse, CodeQL) and GraalVM native images. Use when changing build logic under buildSrc or gradle/, the root Gradle settings, native image hints, or anything under .github/workflows or ci/."
---



# CAS build and CI workflows

## Gradle build settings

- Gradle 9.8.0 enables build caching, the configuration cache, parallel execution and Isolated Projects here.
  Isolated Projects configures every project on a cache miss and ignores configuration on demand. Compare representative
  scoped commands before changing either setting. User-home `gradle.properties` overrides the repository's JVM settings.
- Test category discovery runs inside `TestCategoryTagsValueSource`: only its sorted tag list is a configuration input.
  Implementation edits retain the cached graph; adding/removing categories invalidates it. Do not move source reads back
  into the convention script or restore the old `.gradle/cas-test-tags` fingerprint cache.
- Build flags (`-D<name>`) are read only through `CasBuildSettings`: `buildProperty` (system property, then the
  upper-snake-case environment variable, e.g. `GENERATE_GIT_PROPERTIES`; a blank environment variable counts as unset),
  `isFlagEnabled` (unset → default; empty or `true` → on; anything else → off) and `stringProperty` (blank → default). Do not call `providers.systemProperty`, `System.getProperty`
  or `System.getenv` from build scripts, convention plugins or module build files, and do not recompute flags in the
  root `ext {}`: `configureProjectExtensions` already exposes them as project extensions on every project, root
  included. `settings.gradle` cannot see buildSrc and repeats the same lookup rule by hand; `PublishProjectModulesMetadataTask`
  reads its MongoDB URI at execution time on purpose so the secret stays out of the configuration cache.
- Gradle configures extension and task closures delegate-first: a name the configured object lacks falls through to the
  script and its `ext`, so a mistyped option in `nmcpAggregation { centralPortal { ... } }` silently writes a root
  property instead of failing, and a right-hand side named like an option reads the option itself. nmcp 1.x calls it
  `publishingType` (`publicationType` was the 0.0.x name); compute values into a local variable outside the closure,
  as `build.gradle` does with `centralPortalPublishingType`.
- `buildScript` (puppeteer `script.json` → `-DbuildScript`) must name an existing file: `war-conventions` fails the build
  otherwise, so a scenario that drops its `include.gradle` must drop its `buildScript` entry too.
- An IDE `JetGradlePlugin` error about `setExcludedTaskNames` comes from the IDE integration, not category discovery.
  Use `--no-isolated-projects` for the affected IDE invocation until that integration supports Isolated Projects; keep
  configuration caching enabled and do not disable Isolated Projects globally to hide an IDE-only violation.

## Web application packaging

- `bootRun` must not forward the Gradle daemon's `java.home` to the toolchain JVM. With a Zulu daemon and JBR
  launcher, that override makes JBR look in Zulu's runtime image for `META-INF/jbrapi.registry`; HotswapAgent 2.0.5
  then aborts in `ProxyRepository.Registry.readEntries`. Preserve the launched JVM's own `java.home`.
- `tomcat-embed-core` already holds the classes of `tomcat-coyote`, `-util`, `-util-scan` (except its JSP TLD parser),
  `-api`, `-jni`, `-juli` and `-servlet-api`; `springboottomcat` excludes them from `tomcat-catalina-ha`, which keeps only
  `tomcat-catalina-ha` and `tomcat-tribes` for session clustering. The Tomcat war, the Tomcat starter and the config server war keep `jakarta.servlet-api`,
  `jakarta.el-api` and expressly off `runtimeClasspath`: Tomcat provides both APIs and its EL implementation, while the
  Jetty war still needs them from the `jakarta` bundle.
- Mail ships as `angus-mail` plus `jakarta.mail-api`, as Spring Boot's mail module does; the combined
  `org.eclipse.angus:jakarta.mail` jar duplicates both. AspectJ ships as `aspectjweaver` only, a superset of `aspectjrt`.
- CAS serves `material-components-web` only through its self-contained `dist/material-components-web.min.{js,css}`, so
  the webjar is declared with `transitive = false`; its npm dependencies (`material__*`, `tslib`) are about 7 MB of jars.
  Webjar paths CAS serves are listed in `core/cas-server-core-web/src/main/resources/cas_common_messages.properties`.
- CAS schedules with Spring (`@EnableScheduling`/`@Scheduled`, Boot's `TaskScheduler`, Spring's `CronExpression`), never
  Quartz; Quartz, OSHI and Metro (`jaxws-rt`, `streambuffer`) were removed from the shared bundles because nothing used
  them. JAX-WS in `ws-sts`/`ws-idp` runs on CXF, whose `cxf-rt-bindings-soap` brings `jakarta.xml.ws-api` and
  `jakarta.xml.soap-api`; do not add Metro back next to CXF (both register a `jakarta.xml.ws.spi.Provider`, and Metro
  also registers a `ServletContainerInitializer` in every deployment).
- The shared `commons` bundle no longer carries commons-compress, -jexl3, -cli or -pool2. The object pool comes with
  the `redis` bundle (Lettuce pooling, `RedisObjectFactory`) and the `commonspool` bundle (`api` of `memcached-core`).
  Velocity is declared by the SAML modules (`saml-core-api` exports it) and ZXing by the OTP/GAuth modules, not by
  `core-util(-api)`. `failOnVersionConflict()` is on, so dropping a pinned library cannot silently downgrade it: a
  module that pulled another version would already have failed to resolve.
- Apache HttpClient 4 is no longer a `runtimeOnly` of every project. Bundles that exclude `httpclient` from a library
  get the `httpclient4` bundle appended by the list at the end of `gradle/dependencies.gradle`; add a bundle there when
  it excludes `httpclient` from a library that uses it. `core-scripting` carries it because Groovy scripts get
  `org.apache.http` packages as default imports (`ScriptingUtils`). `springcloud` and `springcloudconfigclient` are left
  out on purpose: every module and war gets them, and their libraries do not use HttpClient 4.
- `webapp-dependencies-conventions` registers `WebjarTrimTransform` (buildSrc, Java) on `runtimeClasspath` of the web
  applications (wars, starter, `bootRun`): source maps and `.d.ts` files go from every WebJar, and per-WebJar rules drop
  what CAS never serves (Font Awesome js/svgs/sprites/metadata, other jquery-ui themes, highlight.js languages and
  styles, legacy mdi fonts, datatables examples, ...). Served paths come from `webjars.*` in
  `cas_common_messages.properties`; when a view starts using another file of a trimmed WebJar, relax its rule.
  License and notice files are always kept. Other jars pass through untouched; module POMs are unchanged, and an
  overlay's own copy of a WebJar wins over the war's (the freefair overlay spec uses `DuplicatesStrategy.EXCLUDE`).
- Jackson 2 (`com.fasterxml.jackson` databind/core) is the `jackson2` bundle, not part of `jackson` (which every module
  gets): WebAuthn gets it from Yubico's POM, `core-scripting` carries it for the `com.fasterxml.jackson.databind`
  default import of Groovy scripts, and `grouper` carries it because it excludes Jackson 2 core from grouperClient.
  Jackson 3 code that only needs `com.fasterxml.jackson.annotation` keeps getting `jackson-annotations` from `jackson`.
  JMS is gone; WebSocket is the `springbootwebsocket` bundle (`qr-authentication`).
- spring-messaging is the `springmessaging` bundle, not part of `spring` (which every module gets). `qr-authentication`
  (STOMP) and `pulsar-ticket-registry` declare it; it is appended (list at the end of `gradle/dependencies.gradle`) to
  the bundles that exclude it but need it: `springkafka`, `springcloudbus*`, `springintegrationamqp`, `springcloudvault`.
  spring-integration-core and Spring Pulsar bring it on their own. A new bundle that excludes spring-messaging must
  check whether its library needs it.
- commons-collections4 is the `commonscollections4` bundle (not in `commons`): `aup-core`, `duo-core`, `x509-core` declare
  it, and it is appended to `mailgun`, which excludes it. The Nimbus OAuth/OIDC SDK (`oauth2-oidc-sdk`) is the
  `nimbusoidc` bundle (not in `nimbus`): `oauth-core-api` declares it as `api` because its validators expose SDK types
  (DPoP exceptions, nonces, confirmations); `oidc-core-api`, `oidc-federation`, `heimdall`, `pac4j-api` (its Jackson
  module is loaded through `META-INF/services`, so it must stay on the runtime class path) and `pac4j-oidc` declare it,
  `oauth`, `oidc` and `oidc-vc` for tests; it is appended to `azuread` (msal4j) and `pac4joidcoauth`, which exclude it.
- `docs/cas-server-documentation-processor` depends on every bundle (`libraries.each`) and declares what it imports
  that no bundle carries (commons-cli). Before dropping a library from a bundle, search the whole repository
  (`docs/` included), not only `api`, `core`, `support` and `webapp`.
- The `jakarta` bundle no longer carries a JAXB runtime: `jaxbimpl` is appended (list at the end of
  `gradle/dependencies.gradle`) to the bundles that exclude their libraries' runtime (`casclient`, `geode`,
  `hibernate`) and to `springws` (Spring-WS `Jaxb2Marshaller`); CXF brings its own. cqengine is used on-heap only, so
  its kryo dependencies are excluded (memcached declares `kryo` itself). Thymeleaf runs with the Spring dialect (SpEL),
  so OGNL is excluded. The configuration processor is the `springbootconfigprocessor` bundle, an annotation processor of
  `core-api-configuration-model` only; `springbootconfigmetadata` is runtime (jsoup, configuration metadata).
- Spring Integration (and with it Reactor) is no longer in every module. CAS owns its lock and transaction helpers in
  `core-api-util`: `DefaultLockRepository` takes a lock function, `StripedLockRegistry` and `PassThroughLock` back
  `LockRepository.asDefault()`/`noOp()`, and `PseudoPlatformTransactionManager` is the no-op transaction manager. Only
  the distributed lock registries use Spring Integration: `redis-ticket-registry`, `jpa-ticket-registry` and
  `hazelcast-discovery-zookeeper` declare `libraries.springintegration` (core only) and pass `registry::obtain`. The JPA
  registry also needs `spring-boot-integration` (in `springintegrationjdbc`) for `spring.integration.jdbc.initialize-schema`,
  and its tests import `IntegrationAutoConfiguration` explicitly; `SpringBootTestAutoConfigurations` no longer lists it.
  Spring Integration used to supply the `taskScheduler` bean that OAuth, OIDC and UMA inject by name. Spring Boot's own
  backs off as soon as any other `TaskScheduler` exists (the WebSocket broker's `messageBrokerTaskScheduler` with QR
  authentication), so `CasCoreUtilConfiguration` defines `taskScheduler` whenever no bean has that name; tests also get
  `TaskSchedulingAutoConfiguration` through `SpringBootTestAutoConfigurations`.
- Before adding a library to a bundle that every module gets through `project-conventions` (`jakarta`, `commons`,
  `springboot`, ...), check the default Tomcat war: anything there ships in every deployment. The size review and the
  remaining candidates are in the `claude/webapp-war-size-review.md` project doc.

## GitHub Actions workflows

- Unit-test matrix jobs (`tests.yml`) record JaCoCo data only (`testcas.sh --with-coverage` only turns on the agent) and upload the `.exec` files; the single `coverage` job builds `jacocoRootReport` and does every Sonar/Codecov/Coveralls/Codacy upload. Do not move `jacocoRootReport` or `sonar` back into the matrix: the root report compiles all ~430 projects and Sonar re-analyses the whole code base, which is what made each category job 17 minutes instead of 7.
- SonarCloud's project key is `${sonarOrganization}_${sonarProjectName}` (`apereo_cas` for upstream). Keep the
  coverage, quality-gate and maintainability badges and dashboard links in `developer/Test-Process.md` aligned with it.
  The legacy `org.apereo.cas:cas-server` project reports older CAS 7.3 results; compare project keys and analysis
  revisions before diagnosing a coverage-import failure from a badge discrepancy.
- Codecov receives the aggregate JaCoCo XML without flags. Its project status must not filter on an absent `unit`
  flag or a root `src` directory; CAS sources live in module directories. `parsers.jacoco.partials_as_hits: true`
  counts executed lines with missed branches as covered, aligning the percentage with JaCoCo line coverage.
- Every `gradle/actions/setup-gradle` step sets `gradle-home-cache-excludes: caches/build-cache-1` and
  `cache-encryption-key: ${{ secrets.GRADLE_ENCRYPTION_KEY }}`. Preserve both: the local build cache duplicates Develocity
  outputs, and the encryption key enables saving/restoring the project configuration cache. Fork PRs without this secret
  can still build, but cannot restore the encrypted configuration cache. Do not layer `setup-java` or GraalVM Gradle
  caching over `setup-gradle` in the same job.
- CI permits Gradle daemon reuse within a job; `ci/init-build.sh` must not append a disabling user-home property.
  Release, documentation and scenario scripts preserve daemon reuse across their Gradle invocations. CodeQL keeps
  `--no-daemon` for compiler tracing. Test categories and Puppeteer scenarios are fetched once per discovery
  job; print the captured result and split scenario JSON with `jq`, preserving the existing matrix boundaries.
- The CodeQL job (`analysis.yml`) applies `--no-build-cache --rerun-tasks` after its shared build options so traced
  compilation actually runs. The current `settings.gradle` has no CodeQL-specific `JavaCompile` cache guard. Preserve
  these workflow flags: CodeQL only extracts compilations it observes, so restored or up-to-date outputs empty the scan.

## Native images

- Keep the native builder's default CI heap at 12 GiB on the 16 GiB hosted runner. A 14 GiB heap leaves too little
  room for native allocations, Gradle and the runner; run 42389952060 lost 60 runners, most available logs ending near
  image layout. The shutdown messages do not prove OOM, so verify the next CI run. Use `nativeImageHeapSize` to
  override the budget on a larger runner; increasing the heap on the same machine is not a fix for Palantir's OOM.
- Runtime-selected bean implementations can expose interfaces that their declared bean return type does not.
  The JSON trusted-device store adds `DisposableBean` ahead of `MultifactorAuthenticationTrustStorage`; its Spring
  transaction/audit proxy needs that ordered interface pair plus the Spring proxy interfaces in native metadata.
  Test the interfaces discovered from the concrete implementation, not only the bean's declared interface.
