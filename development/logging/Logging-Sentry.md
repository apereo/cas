# Overview

Source: https://apereo.github.io/cas/development/logging/Logging-Sentry.html

[Sentry](https://sentry.io/) allows you to track logs and error in real time. It provides insight into production deployments and information to reproduce and fix crashes.

The integration here supports error handling and reporting to Sentry, performance monitoring via spans and transactions as well as Sentry logging support.

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-sentry"
}
```

The [Logging](https://apereo.github.io/cas/development/logging/Logging.html) configuration file must be adjusted to match the following:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="warn">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </Console>
        <!-- Setting minimumBreadcrumbLevel modifies the default minimum level to add breadcrumbs from INFO to DEBUG  -->
        <!-- Setting minimumEventLevel the default minimum level to capture an event from ERROR to WARN  -->
        <Sentry name="Sentry" 
                minimumBreadcrumbLevel="DEBUG"
                minimumEventLevel="WARN"
                dsn="..." />
    </Appenders>
    <Loggers>
        <Root level="INFO">
            <AppenderRef ref="Sentry"/>
            <AppenderRef ref="casConsole"/>
        </Root>
    </Loggers>
</Configuration>
```

The following settings and properties are available from the CAS configuration catalog:

- `sentry.app-start-extender` (Third party; type `IAppStartExtender`)
- `sentry.attach-server-name` (Third party; type `Boolean`)
- `sentry.attach-stacktrace` (Third party; type `Boolean`)
- `sentry.attach-threads` (Third party; type `Boolean`)
- `sentry.backpressure-monitor` (Third party; type `IBackpressureMonitor`)
- `sentry.bundle-ids` (Third party; type `Set<String>`)
- `sentry.cache-dir-path` (Third party; type `String`)
- `sentry.capture-open-telemetry-events` (Third party; type `Boolean`)
- `sentry.clients.ok-http-enabled` (Third party; type `Boolean`; default `false`): Enable automatic instrumentation of Spring-managed OkHttp clients. Disabled by default.
- `sentry.composite-performance-collector` (Third party; type `CompositePerformanceCollector`)
- `sentry.connection-status-provider` (Third party; type `IConnectionStatusProvider`)
- `sentry.connection-timeout-millis` (Third party; type `Integer`)
- `sentry.context-tags` (Third party; type `List<String>`)
- `sentry.continuous-profiler` (Third party; type `IContinuousProfiler`)
- `sentry.cron.default-checkin-margin` (Third party; type `Long`)
- `sentry.cron.default-failure-issue-threshold` (Third party; type `Long`)
- `sentry.cron.default-max-runtime` (Third party; type `Long`)
- `sentry.cron.default-recovery-threshold` (Third party; type `Long`)
- `sentry.cron.default-timezone` (Third party; type `String`)
- `sentry.data-collection` (Third party; type `DataCollection`)
- `sentry.date-provider` (Third party; type `SentryDateProvider`)
- `sentry.deadline-timeout` (Third party; type `Long`)
- `sentry.debug` (Third party; type `Boolean`)
- `sentry.debug-manager` (Third party; type `IDebugManager`)
- `sentry.debug-meta-loader` (Third party; type `IDebugMetaLoader`)
- `sentry.default-scope-type` (Third party; type `ScopeType`)
- `sentry.diagnostic-level` (Third party; type `SentryLevel`)
- `sentry.dist` (Third party; type `String`)
- `sentry.distinct-id` (Third party; type `String`)
- `sentry.distribution-controller` (Third party; type `IDistributionApi`)
- `sentry.dsn` (Third party; type `String`)
- `sentry.enable-aot-compatibility` (Third party; type `Boolean`; default `false`): If set to true, this flag disables all AOP related features (e.g. `io.sentry.spring7.tracing.SentryTransaction`, `io.sentry.spring7.tracing.SentrySpan`) to successfully compile to GraalVM
- `sentry.enable-app-start-profiling` (Third party; type `Boolean`)
- `sentry.enable-auto-session-tracking` (Third party; type `Boolean`)
- `sentry.enable-backpressure-handling` (Third party; type `Boolean`)
- `sentry.enable-cache-tracing` (Third party; type `Boolean`)
- `sentry.enable-database-transaction-tracing` (Third party; type `Boolean`)
- `sentry.enable-deduplication` (Third party; type `Boolean`)
- `sentry.enable-event-size-limiting` (Third party; type `Boolean`)
- `sentry.enable-external-configuration` (Third party; type `Boolean`)
- `sentry.enable-legacy-profiling` (Third party; type `Boolean`)
- `sentry.enable-pretty-serialization-output` (Third party; type `Boolean`)
- `sentry.enable-queue-tracing` (Third party; type `Boolean`)
- `sentry.enable-scope-persistence` (Third party; type `Boolean`)
- `sentry.enable-screen-tracking` (Third party; type `Boolean`)
- `sentry.enable-shutdown-hook` (Third party; type `Boolean`)
- `sentry.enable-spotlight` (Third party; type `Boolean`)
- `sentry.enable-time-to-full-display-tracing` (Third party; type `Boolean`)
- `sentry.enable-uncaught-exception-handler` (Third party; type `Boolean`)
- `sentry.enable-user-interaction-breadcrumbs` (Third party; type `Boolean`)
- `sentry.enable-user-interaction-tracing` (Third party; type `Boolean`)
- `sentry.enabled` (Third party; type `Boolean`)
- `sentry.envelope-disk-cache` (Third party; type `IEnvelopeCache`)
- `sentry.envelope-reader` (Third party; type `IEnvelopeReader`)
- `sentry.environment` (Third party; type `String`)
- `sentry.event-processors` (Third party; type `List<EventProcessor>`)
- `sentry.exception-resolver-order` (Third party; type `Integer`; default `1`): Report all or only uncaught web exceptions.
- `sentry.executor-service` (Third party; type `ISentryExecutorService`)
- `sentry.fatal-logger` (Third party; type `ILogger`)
- `sentry.feedback-options` (Third party; type `SentryFeedbackOptions`)
- `sentry.flush-timeout-millis` (Third party; type `Long`)
- `sentry.force-init` (Third party; type `Boolean`)
- `sentry.fully-displayed-reporter` (Third party; type `FullyDisplayedReporter`)
- `sentry.gesture-target-locators` (Third party; type `List<GestureTargetLocator>`)
- `sentry.global-hub-mode` (Third party; type `Boolean`)
- `sentry.graphql.ignored-error-types` (Third party; type `List<String>`): List of error types the Sentry Graphql integration should ignore.
- `sentry.idle-timeout` (Third party; type `Long`)
- `sentry.ignored-check-ins` (Third party; type `List<FilterString>`)
- `sentry.ignored-errors` (Third party; type `List<FilterString>`)
- `sentry.ignored-exceptions-for-type` (Third party; type `Set<Class<? extends Throwable>>`)
- `sentry.ignored-span-origins` (Third party; type `List<FilterString>`)
- `sentry.ignored-transactions` (Third party; type `List<FilterString>`)
- `sentry.in-app-excludes` (Third party; type `List<String>`)
- `sentry.in-app-includes` (Third party; type `List<String>`)
- `sentry.init-priority` (Third party; type `InitPriority`)
- `sentry.instrumenter` (Third party; type `Instrumenter`; deprecation `WARNING`, no replacement)
- `sentry.integrations` (Third party; type `List<Integration>`)
- `sentry.keep-transactions-open-for-async-responses` (Third party; type `Boolean`; default `false`)
- `sentry.logger` (Third party; type `ILogger`)
- `sentry.logging.enabled` (Third party; type `Boolean`; default `true`): Enable/Disable logging auto-configuration.
- `sentry.logging.loggers` (Third party; type `List<String>`): List of loggers the SentryAppender should be added to.
- `sentry.logging.minimum-breadcrumb-level` (Third party; type `Level`): Minimum logging level for recording breadcrumbs.
- `sentry.logging.minimum-event-level` (Third party; type `Level`): Minimum logging level for recording events.
- `sentry.logging.minimum-level` (Third party; type `Level`): Minimum logging level for recording log events.
- `sentry.logs.enabled` (Third party; type `Boolean`)
- `sentry.logs.logger-batch-processor-factory` (Third party; type `ILoggerBatchProcessorFactory`)
- `sentry.max-attachment-size` (Third party; type `Long`)
- `sentry.max-breadcrumbs` (Third party; type `Integer`)
- `sentry.max-cache-items` (Third party; type `Integer`)
- `sentry.max-depth` (Third party; type `Integer`)
- `sentry.max-feature-flags` (Third party; type `Integer`)
- `sentry.max-queue-size` (Third party; type `Integer`)
- `sentry.max-request-body-size` (Third party; type `SentryOptions.RequestSize`)
- `sentry.max-spans` (Third party; type `Integer`)
- `sentry.max-trace-file-size` (Third party; type `Long`)
- `sentry.metrics.enabled` (Third party; type `Boolean`)
- `sentry.metrics.metrics-batch-processor-factory` (Third party; type `IMetricsBatchProcessorFactory`)
- `sentry.modules-loader` (Third party; type `IModulesLoader`)
- `sentry.open-telemetry-mode` (Third party; type `SentryOpenTelemetryMode`)
- `sentry.options-observers` (Third party; type `List<IOptionsObserver>`)
- `sentry.org-id` (Third party; type `String`)
- `sentry.performance-collectors` (Third party; type `List<IPerformanceCollector>`)
- `sentry.print-uncaught-stack-trace` (Third party; type `Boolean`)
- `sentry.profile-lifecycle` (Third party; type `ProfileLifecycle`)
- `sentry.profile-session-sample-rate` (Third party; type `Double`)
- `sentry.profiler-converter` (Third party; type `IProfileConverter`)
- `sentry.profiles-sample-rate` (Third party; type `Double`)
- `sentry.profiling-traces-dir-path` (Third party; type `String`)
- `sentry.profiling-traces-hz` (Third party; type `Integer`)
- `sentry.proguard-uuid` (Third party; type `String`)
- `sentry.propagate-traceparent` (Third party; type `Boolean`)
- `sentry.proxy.host` (Third party; type `String`)
- `sentry.proxy.pass` (Third party; type `String`)
- `sentry.proxy.port` (Third party; type `String`)
- `sentry.proxy.type` (Third party; type `Proxy.Type`)
- `sentry.proxy.user` (Third party; type `String`)
- `sentry.reactive.thread-local-accessor-enabled` (Third party; type `Boolean`; default `true`): Enable/Disable usage of `io.micrometer.context.ThreadLocalAccessor` for Scopes propagation
- `sentry.read-timeout-millis` (Third party; type `Integer`)
- `sentry.release` (Third party; type `String`)
- `sentry.replay-controller` (Third party; type `ReplayController`)
- `sentry.sample-rate` (Third party; type `Double`)
- `sentry.scope-observers` (Third party; type `List<IScopeObserver>`)
- `sentry.scopes-storage-factory` (Third party; type `IScopesStorageFactory`)
- `sentry.sdk-version` (Third party; type `SdkVersion`)
- `sentry.send-client-reports` (Third party; type `Boolean`)
- `sentry.send-default-pii` (Third party; type `Boolean`; deprecation `WARNING`, no replacement)
- `sentry.send-modules` (Third party; type `Boolean`)
- `sentry.sentry-client-name` (Third party; type `String`)
- `sentry.serializer` (Third party; type `ISerializer`)
- `sentry.server-name` (Third party; type `String`)
- `sentry.session-flush-timeout-millis` (Third party; type `Long`)
- `sentry.session-replay` (Third party; type `SentryReplayOptions`)
- `sentry.session-tracking-interval-millis` (Third party; type `Long`)
- `sentry.shutdown-timeout-millis` (Third party; type `Long`)
- `sentry.socket-tagger` (Third party; type `ISocketTagger`)
- `sentry.span-factory` (Third party; type `ISpanFactory`)
- `sentry.spotlight-connection-url` (Third party; type `String`)
- `sentry.ssl-socket-factory` (Third party; type `SSLSocketFactory`)
- `sentry.start-profiler-on-app-start` (Third party; type `Boolean`)
- `sentry.strict-trace-continuation` (Third party; type `Boolean`)
- `sentry.tags` (Third party; type `Map<String,String>`)
- `sentry.thread-checker` (Third party; type `IThreadChecker`)
- `sentry.timer-executor-service` (Third party; type `ISentryExecutorService`)
- `sentry.trace-options-requests` (Third party; type `Boolean`)
- `sentry.trace-propagation-targets` (Third party; type `List<String>`)
- `sentry.trace-sampling` (Third party; type `Boolean`; deprecation `WARNING`, no replacement)
- `sentry.traces-sample-rate` (Third party; type `Double`)
- `sentry.transaction-profiler` (Third party; type `ITransactionProfiler`)
- `sentry.transport-factory` (Third party; type `ITransportFactory`)
- `sentry.transport-gate` (Third party; type `ITransportGate`)
- `sentry.use-git-commit-id-as-release` (Third party; type `Boolean`; default `true`): Whether to use Git commit id as a release.
- `sentry.user-filter-order` (Third party; type `Integer`): Defines the `io.sentry.spring.SentryUserFilter` order. The default value is `org.springframework.core.Ordered.LOWEST_PRECEDENCE`, if Spring Security is auto-configured, its guaranteed to run after Spring Security filter chain.
- `sentry.version-detector` (Third party; type `IVersionDetector`)
- `sentry.view-hierarchy-exporters` (Third party; type `List<ViewHierarchyExporter>`)

Breadcrumbs are kept in memory (by default the last `100` records) and are sent with events. For example, by default, if you log 100 entries with `logger.info` or `logger.warn`, no event is sent to Sentry. If you then log with `logger.error`, an event is sent to Sentry that includes those `100` info or warn messages. For this to work, `SentryAppender` needs to receive all log entries to decide what to keep as breadcrumb or send as event. Set the `SentryAppender` log level configuration to a value lower than what is set for the `minimumBreadcrumbLevel` and `minimumEventLevel` so that it receives these log messages.

Finally, you will need to configure your DSN (client key) and optionally other values such as environment and release.

You can do so in a `src/main/resources/sentry.properties` file:

```properties
dsn=https://12345@12345.ingest.sentry.io/12345
```

Or via system properties when you launch CAS:

```properties
java -Dsentry.dsn=https://12345@12345.ingest.sentry.io/12345 ...
```

Or via environment variables before you launch CAS:

```bash
export SENTRY_DSN=https://12345@12345.ingest.sentry.io/12345
```
