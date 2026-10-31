---
name: cas-startup-performance
description: "Review CAS startup performance: what auto-configurations actually run, the three startup phases, lazy initialization and eager markers, classpath scanning, deferral over deletion. Use when investigating or changing startup time or bean initialization."
---

# Startup performance

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
