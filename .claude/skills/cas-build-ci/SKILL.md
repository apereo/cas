---
name: cas-build-ci
description: "Gradle build settings (configuration cache, Isolated Projects, build cache, test-category discovery) and the GitHub Actions workflows (tests.yml coverage job, setup-gradle caching, daemon reuse, CodeQL). Use when changing build logic under buildSrc or gradle/, the root Gradle settings, or anything under .github/workflows or ci/."
---


# CAS build and CI workflows

## Gradle build settings

- Gradle 9.8.0 enables build caching, the configuration cache, parallel execution and Isolated Projects here.
  Isolated Projects configures every project on a cache miss and ignores configuration on demand. Compare representative
  scoped commands before changing either setting. User-home `gradle.properties` overrides the repository's JVM settings.
- Test category discovery runs inside `TestCategoryTagsValueSource`: only its sorted tag list is a configuration input.
  Implementation edits retain the cached graph; adding/removing categories invalidates it. Do not move source reads back
  into the convention script or restore the old `.gradle/cas-test-tags` fingerprint cache.
- An IDE `JetGradlePlugin` error about `setExcludedTaskNames` comes from the IDE integration, not category discovery.
  Use `--no-isolated-projects` for the affected IDE invocation until that integration supports Isolated Projects; keep
  configuration caching enabled and do not disable Isolated Projects globally to hide an IDE-only violation.

## GitHub Actions workflows

- Unit-test matrix jobs (`tests.yml`) record JaCoCo data only (`testcas.sh --with-coverage` only turns on the agent) and upload the `.exec` files; the single `coverage` job builds `jacocoRootReport` and does every Sonar/Codecov/Coveralls/Codacy upload. Do not move `jacocoRootReport` or `sonar` back into the matrix: the root report compiles all ~430 projects and Sonar re-analyses the whole code base, which is what made each category job 17 minutes instead of 7.
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
