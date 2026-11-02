---
name: cas-sandbox-verification
description: "Compile and test CAS changes from a restricted sandbox or remote VM where Gradle, JDK 25 or Maven Central are unavailable: the VM javac/Error Prone compile check, the VM test runner and its stale build dirs, emulators (moto, fakeredis, UnboundID), background-process limits, and fetching specifications. Use whenever Gradle cannot run in the current environment."
---


# Verifying CAS changes from a sandbox

## Fetching specifications

- The openid.net spec pages are long: WebFetch summaries truncate and can invent text. Fetch the markdown source (`raw.githubusercontent.com/openid/OpenID4VCI/main/1.0/...`, `.../OpenID4VC-HAIP/main/1.0/...`) with curl and grep it instead.
- IETF drafts are blocked on ietf.org here: fetch the markdown source from the draft's GitHub repository at the published tag (`raw.githubusercontent.com/oauth-wg/<repo>/draft-ietf-oauth-<name>-NN/draft-ietf-oauth-<name>.md`), and confirm the number is the latest published one.

## Environment setup

- A remote container may lack JDK 25: `apt-get install openjdk-25-jdk-headless` provides it, and `CI=true` drops the JetBrains vendor requirement of the toolchain. When Maven Central answers `429`, route resolution through `maven-central.storage-download.googleapis.com` with a Gradle init script kept outside the repository; that mirror lags new releases by a day or so, so leave Central in place, restricted to the groups the mirror does not have yet.
- Gradle may be unavailable in a sandboxed or remote review environment because the wrapper cannot download its distribution. When that happens, say the verification was not run instead of implying a test result, and fall back to static review such as `git diff --check` and targeted reading.
- `device_stage_files` refuses any file more than 7 folders below the connected folder, which is every Java source of the
  repository: tar the files into the repository's `build/` (`tar czf build/<task>/changed.tgz -T paths.txt style`) and stage
  the tarball. Checkstyle then runs in the cloud container, which can download the `checkstyle-<version>-all.jar` of
  `gradle/libs.versions.toml` from github.com releases; Maven Central, Lombok and the Gradle cache stay out of reach from
  both machines, so without the cache a `javac` check is not possible: hand over a `build/<name>-run.sh` instead.
- Deleting a file in the device VM is refused there; leave the file in place and ask Misagh to `git rm` it.
- Claude's session cannot get folder access to `~/.gradle` (the auto-mode classifier refuses it), so the VM javac check
  below is not available there; compiled classes under `*/build/classes` from Misagh's IDE show which main modules
  compiled, and `build/account-profile-run.sh` runs compile, checkstyle, the touched tests and the profile scenarios.
- Use `git --no-optional-locks` for every read-only git command in the device VM; a plain `git status` leaves `.git/index.lock` behind there.
- DynamoDB-backed tests can run in the device VM against moto: `pip3 install --user "moto[server]"` works there (Maven Central and MongoDB downloads do not), then start `~/.local/bin/moto_server -p 8000` and run the suite in the same `device_bash` call. It is not DynamoDB Local, so CI can still differ.
- More services for the VM test runner: `pip3 install --user fakeredis` provides `fakeredis.TcpFakeServer`, a Redis emulator on 6379. An UnboundID `InMemoryDirectoryServer` on 10389, with base `dc=example,dc=org`, bind `cn=Directory Manager`/`password` and the schema turned off, runs the GAuth LDAP suite. Start either one in the background in the same `device_bash` call as the tests, and stop it by a PID file, not `pkill -f <name>`: that pattern also matches the calling shell and kills it.
- A fresh device VM has only Java 11 and about 1 GB free under the session home: put the JDK there, but copy the cached
  jars (about 1 GB of highest versions) to `/tmp`, which has room. Read access to `~/.gradle/caches/modules-2/files-2.1`
  has been granted when requested with `device_request_folder_access`. Copy each module's project-closure class and
  resource directories to `/tmp` too: run from the mount, a SAML JPA context did not start within the call's time limit,
  and from local copies it starts in seconds.

## Compile checks without Gradle

- Compile-check changed sources when Gradle cannot run: in the device VM, download a JDK 25 from `api.github.com/repos/adoptium/temurin25-binaries/releases/assets/<id>` (`Accept: application/octet-stream`; `github.com` itself is blocked), get read access to `~/.gradle/caches/modules-2/files-2.1`, copy the highest version of every jar to local disk (the mount runs out of file handles with ~1,700 jars open), and run `javac -proc:full -processorpath <lombok 1.18.x jar>` with an `@argfile` classpath of the repo's `*/build/classes/java/{main,test}` plus those jars. To run tests in the same VM: copy `*/build/{classes/java,resources}/{main,test}` to local disk (class lookups across ~1,400 directories on the mount are too slow), build the classpath from the module's project closure (parse `project(":...")` references in `build.gradle`, following `configuration: "tests"` into test classes) instead of every module, pick jar versions from `gradle/libs.versions.toml` rather than the highest in the cache (the highest Hibernate/Hypersistence pairing does not load), leave out `spring-cloud-*` integration jars and `log4j-to-slf4j`/`logback`, put the freshly compiled changed classes first, and drive JUnit with a small `LauncherFactory` main. Services such as LDAP, MongoDB or DynamoDB are not available there, so those suites still need a local run.
- The VM compile check can also run Error Prone, which `-Werror` turns every warning of into a build failure: add `-XDcompilePolicy=byfile -XDshould-stop.ifError=FLOW "-Xplugin:ErrorProne <the -Xep flags of project-conventions.gradle> -XepOpt:NullAway:OnlyNullMarked=true -XepOpt:NullAway:JSpecifyMode=true"`, the `-J--add-exports`/`--add-opens` of `jdk.compiler`'s `api, main, model, parser, processing, tree, util, code, comp, file` packages, and every cached jar on `-processorpath`. NullAway refuses to start without one of its package options. For the VM test runner, leave out every `spring-cloud-*` jar except `spring-cloud-commons` and `spring-cloud-context` (the Vault, Consul and Kubernetes bootstrap configurations otherwise start and fail).
- The compile script's `main` mode wipes `out/`, test classes included: run `test` mode again after every `main` run, or the runner falls back to stale prebuilt test classes and silently runs fewer tests.
- Upstream commits since the compile script's base can touch modules whose prebuilt classes are stale: restrict its file list with `include.txt` (module paths) rather than compiling every changed file.
- To compile a Java file outside the repository with Lombok, copy the repo's `lombok.config` next to it; without it, `@Slf4j` creates `log` rather than `LOGGER`.
- When a rename script converts a variable, check it against the compiler and the diff: a local or constructor parameter
  that shares the old name but means something else (`OidcConfigurationContext context`, an `OAuth20AuthorizationRequest
  context`, a log map) still compiles after the rename.
- Overlaying changed files on a VM copy misses files that were later reverted to the base: keep a list of every file ever
  touched and ship all of them, or re-extract the base first.
- Compiling several modules' tests in one `javac` run (or with every module's `build/classes/java/test` on the class path)
  hides missing test dependencies: Gradle gives a module only the test classes of its `configuration: "tests"`
  dependencies (`oidc` has no `oauth` test classes, so `OAuth20TestUtils` is out of reach there). Before handing off, compile
  each changed module's tests on its own, with the oauth/oidc test directories removed from the class path and only its
  declared test dependencies added back. Put helpers that `oidc` tests need in `AbstractOidcTests`.
- For the Error Prone check, put every cached jar on `-processorpath` (Error Prone needs more than its own jars) and
  restrict annotation processing to Lombok with `-processor lombok.launch.AnnotationProcessorHider$AnnotationProcessor,lombok.launch.AnnotationProcessorHider$ClaimingProcessor`;
  long class paths go into `@argfiles` for both `javac` and `java`.

## The VM test runner

- The VM test runner's copied build dirs predate any unpushed round: compile the Java files of the previous round's commit together with the current ones, or settings and methods added there are missing. `AbstractTicketRegistry.getTicket(id, type)` throws when the ticket is absent; use `getTicket(id)` to probe.
- The VM test runner's copied build dirs go stale once Misagh commits: recompile every Java file touched by the commits of the current work (`git diff --name-only <first commit>^ HEAD`) plus the uncommitted ones, not just `git status`.
- Refreshing the VM build after upstream moves on: copy each module's `build/classes/java/{main,test}` and `build/resources/{main,test}` from the maintainer's Gradle build into `cls/` and `res/` (`refresh-cls.sh`), take the latest jar of each artifact from the connected Gradle cache (`files-2.1`) and rewrite the class paths in place (`refresh-jars.py`, then `addnew.py` for artifacts the class paths never had, leaving out Gradle plugins and CAS's own published jars), and set the compile base (`base.txt`) to a commit older than that build. Split artifacts replace a jar outright (ClassGraph 6 is `classgraph` plus `-base`, `-classpath` and `-vfs`): drop the old version rather than keeping both.
- A process a `device_bash` call starts in the background dies when the call returns, `setsid` and `nohup` included, so a test batch left running there leaves an empty log. Run each batch in the foreground under `timeout 170` with `timeout_ms` at 180000, splitting the classes when they take longer.
- The device shell kills background processes when the call returns, so `nohup ... &` test runs die; run the test classes in
  chunks of about a dozen per call (Spring contexts take 20–90 seconds per chunk).
- In the VM test runner, tenant property binding (`containsBindingFor(...)`) did not take effect, so tenant-specific crypto paths pass there and can still fail under Gradle; check those with a local Gradle run.
- In the VM test runner, leave `cas-server-*` jars of the Gradle cache off the classpath: released CAS jars such as `cas-server-webapp-resources` bring their own `application.properties` (`spring.mvc.pathmatch.matching-strategy=ant-path-matcher`) and the context fails to start. Processes started in the background are killed when the `device_bash` call returns, even with `setsid nohup`; run each suite in the foreground under the call's time limit, one nested class at a time if needed. To run in parallel like CI, pass all of CI's settings, `junit.jupiter.execution.parallel.config.executor-service=WORKER_THREAD_POOL` included: without it, work stealing runs `@Transactional` base tests on a thread that already holds a test transaction (`Cannot start new transaction without ending existing transaction`), before and after any change. The Hazelcast suites run there (embedded members); modules without prebuilt test classes need their tests compiled first.
- Run the oidc-federation tests from the module directory: they read `./src/test/resources/subordinates` relative to the
  working directory, and every context fails to load elsewhere.
- The token-authentication and token-webflow contexts do load in the VM once the class path carries the module's
  `build.gradle` project closure (main and test classes and resources of every `project(":...")` it names) and the module's
  own `src/test/resources`. The shared OAuth/OIDC runner class path is wrong for token-webflow: Gradle gives it no OIDC
  classes, while the shared one registers `OidcTokenAuthenticationHandler` (`@ConditionalOnClass(CasOidcAutoConfiguration)`)
  and the test then fails for a reason Gradle never sees. Build a per-module class path without any OAuth/OIDC directory
  (check `cls2/*` too) before reading a token-webflow failure. Do not write such failures off as "Gradle only" without
  running them this way.
- The VM's own hostname does not resolve, so Spring Boot contexts fail with `UnknownHostException`; write a hosts file
  mapping it to 127.0.0.1 and pass `-Djdk.net.hosts.file=<file>` to the test JVM.
- Picking the highest jar of every artifact breaks SAML contexts in three ways: commons-codec newer than
  `gradle/libs.versions.toml` decodes metadata certificates strictly and fails, `bcprov-ext`, Pulsar's shaded Bouncy
  Castle and `jclouds-bouncycastle` clash with `bcprov`/`bcpkix`, and Saxon takes over `TransformerFactory`. Use the
  catalog version of commons-codec and leave the others out. A group filter on `org.apereo.cas` also drops
  `org.apereo.cas.client` (`cas-client-core`), which delegation tests need.
