---
name: cas-build-ci
description: "Gradle build settings (configuration cache, Isolated Projects, build cache, test-category discovery, build flags through CasBuildSettings), the GitHub Actions workflows (tests.yml coverage job, Sonar and Codecov, setup-gradle caching, daemon reuse, CodeQL) and GraalVM native images. Use when changing build logic under buildSrc or gradle/, the root Gradle settings, native image hints, or anything under .github/workflows or ci/; for dependency bundles and what ships in the wars use cas-dependencies."
---




# CAS build and CI workflows

## Gradle build settings

- The build (Gradle 9.9.2 at the time of writing; `gradle/wrapper/gradle-wrapper.properties` is authoritative) enables
  build caching, the configuration cache, parallel execution and Isolated Projects.
- Upgrade the wrapper only with `./gradlew :wrapper --gradle-version=X.Y.Z --gradle-distribution-sha256-sum=<sum>`, run
  twice (the second run regenerates `gradlew`, `gradlew.bat` and the jar from the new version's templates); never edit
  those files or `distributionUrl` by hand. The sum is `<distributionUrl>.sha256` (`curl -sL`, it redirects).
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
- Dependency bundles and web application packaging: see the `cas-dependencies` skill.

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
