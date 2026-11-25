# CAS - OpenTelemetry Distributed Tracing

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Tracing-OpenTelemetry.html

[OpenTelemetry (Otel)](https://opentelemetry.io/) is a collection of standardized vendor-agnostic tools, APIs, and SDKs. It allows us to instrument, generate, and collect telemetry data, which helps in analyzing application behavior or performance. Telemetry data can include logs, metrics, and traces. We can either automatically or manually instrument the code for HTTP, DB calls, and more.

Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-tracing-otel"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `management.opentelemetry.enabled` (Third party; type `Boolean`; default `true`): Whether OpenTelemetry should be enabled. If OpenTelemetry is disabled, only propagators are configured. Metrics, traces, and logging will use no-op implementations.
- `management.opentelemetry.logging.export.max-batch-size` (Third party; type `Integer`; default `512`): Maximum batch size for each export. This must be less than or equal to 'maxQueueSize'.
- `management.opentelemetry.logging.export.max-queue-size` (Third party; type `Integer`; default `2048`): Maximum number of logs that are kept in the queue before they will be dropped.
- `management.opentelemetry.logging.export.otlp.compression` (Third party; type `OtlpLoggingProperties.Compression`; default `none`): Method used to compress the payload.
- `management.opentelemetry.logging.export.otlp.connect-timeout` (Third party; type `Duration`; default `10s`): Connect timeout for the OTel collector connection.
- `management.opentelemetry.logging.export.otlp.endpoint` (Third party; type `String`): URL to the OTel collector's HTTP API.
- `management.opentelemetry.logging.export.otlp.headers` (Third party; type `Map<String,String>`): Custom HTTP headers you want to pass to the collector, for example auth headers.
- `management.opentelemetry.logging.export.otlp.ssl.bundle` (Third party; type `String`): SSL bundle name.
- `management.opentelemetry.logging.export.otlp.timeout` (Third party; type `Duration`; default `10s`): Call timeout for the OTel Collector to process an exported batch of data. This timeout spans the entire call: resolving DNS, connecting, writing the request body, server processing, and reading the response body. If the call requires redirects or retries all must complete within one timeout period.
- `management.opentelemetry.logging.export.otlp.transport` (Third party; type `Transport`; default `http`): Transport used to send the logs.
- `management.opentelemetry.logging.export.schedule-delay` (Third party; type `Duration`; default `1s`): The delay interval between two consecutive exports.
- `management.opentelemetry.logging.export.timeout` (Third party; type `Duration`; default `30s`): Maximum time an export will be allowed to run before being cancelled.
- `management.opentelemetry.logging.limits.max-attribute-value-length` (Third party; type `Integer`): Maximum number of characters for string attribute values.
- `management.opentelemetry.logging.limits.max-attributes` (Third party; type `Integer`; default `128`): Maximum number of attributes per log record.
- `management.opentelemetry.map-environment-variables` (Third party; type `Boolean`; default `true`): Whether to map OpenTelemetry SDK environment variables to Spring Boot configuration properties.
- `management.opentelemetry.otlp.compression` (Third party; type `OtlpProperties.Compression`): Method used to compress the payload.
- `management.opentelemetry.otlp.endpoint` (Third party; type `String`): OTLP endpoint to connect to.
- `management.opentelemetry.otlp.headers` (Third party; type `Map<String,String>`): Additional headers to be passed with every request.
- `management.opentelemetry.resource-attributes` (Third party; type `Map<String,String>`): Resource attributes.
- `management.opentelemetry.tracing.export.include-unsampled` (Third party; type `Boolean`; default `false`): Whether unsampled spans should be exported.
- `management.opentelemetry.tracing.export.max-batch-size` (Third party; type `Integer`; default `512`): Maximum batch size for each export. This must be less than or equal to 'maxQueueSize'.
- `management.opentelemetry.tracing.export.max-queue-size` (Third party; type `Integer`; default `2048`): Maximum number of spans that are kept in the queue before they will be dropped.
- `management.opentelemetry.tracing.export.otlp.compression` (Third party; type `OtlpTracingProperties.Compression`; default `none`): Method used to compress the payload.
- `management.opentelemetry.tracing.export.otlp.connect-timeout` (Third party; type `Duration`; default `10s`): Connect timeout for the OTel collector connection.
- `management.opentelemetry.tracing.export.otlp.endpoint` (Third party; type `String`): URL to the OTel collector's HTTP API.
- `management.opentelemetry.tracing.export.otlp.headers` (Third party; type `Map<String,String>`): Custom HTTP headers you want to pass to the collector, for example auth headers.
- `management.opentelemetry.tracing.export.otlp.ssl.bundle` (Third party; type `String`): SSL bundle name.
- `management.opentelemetry.tracing.export.otlp.timeout` (Third party; type `Duration`; default `10s`): Call timeout for the OTel Collector to process an exported batch of data. This timeout spans the entire call: resolving DNS, connecting, writing the request body, server processing, and reading the response body. If the call requires redirects or retries all must complete within one timeout period.
- `management.opentelemetry.tracing.export.otlp.transport` (Third party; type `Transport`; default `http`): Transport used to send the spans.
- `management.opentelemetry.tracing.export.schedule-delay` (Third party; type `Duration`; default `5s`): The delay interval between two consecutive exports.
- `management.opentelemetry.tracing.export.timeout` (Third party; type `Duration`; default `30s`): Maximum time an export will be allowed to run before being cancelled.
- `management.opentelemetry.tracing.limits.max-attribute-value-length` (Third party; type `Integer`): Maximum number of characters for string attribute values.
- `management.opentelemetry.tracing.limits.max-attributes` (Third party; type `Integer`; default `128`): Maximum number of attributes per span.
- `management.opentelemetry.tracing.limits.max-attributes-per-event` (Third party; type `Integer`; default `128`): Maximum number of attributes per event.
- `management.opentelemetry.tracing.limits.max-attributes-per-link` (Third party; type `Integer`; default `128`): Maximum number of attributes per link.
- `management.opentelemetry.tracing.limits.max-events` (Third party; type `Integer`; default `128`): Maximum number of events per span.
- `management.opentelemetry.tracing.limits.max-links` (Third party; type `Integer`; default `128`): Maximum number of links per span.
- `management.opentelemetry.tracing.sampler` (Third party; type `OpenTelemetryTracingProperties.Sampler`; default `parent-based-trace-id-ratio`): Sampler to use.
