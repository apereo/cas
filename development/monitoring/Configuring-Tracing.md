# CAS - Distributed Tracing

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Tracing.html

CAS ships auto configuration for the following tracers:

| Platform | Reference |
| --- | --- |
| OpenTelemetry | [See this guide](https://apereo.github.io/cas/development/monitoring/Configuring-Tracing-OpenTelemetry.html). |
| Zipkin Brave | [See this guide](https://apereo.github.io/cas/development/monitoring/Configuring-Tracing-Zipkin.html). |
| Jaeger | [See this guide](https://apereo.github.io/cas/development/monitoring/Configuring-Tracing-Jaeger.html). |

The following settings and properties are available from the CAS configuration catalog:

- `management.tracing.baggage.correlation.enabled` (Third party; type `Boolean`; default `true`): Whether to enable correlation of the baggage context with logging contexts.
- `management.tracing.baggage.correlation.fields` (Third party; type `List<String>`): List of fields that should be correlated with the logging context. That means that these fields would end up as key-value pairs in e.g. MDC.
- `management.tracing.baggage.enabled` (Third party; type `Boolean`; default `true`): Whether to enable Micrometer Tracing baggage propagation.
- `management.tracing.baggage.local-fields` (Third party; type `List<String>`): List of fields that should be accessible within the JVM process but not propagated over the wire. Local fields are not supported with OpenTelemetry.
- `management.tracing.baggage.remote-fields` (Third party; type `List<String>`): List of fields that are referenced the same in-process as it is on the wire. For example, the field "x-vcap-request-id" would be set as-is including the prefix.
- `management.tracing.baggage.tag-fields` (Third party; type `List<String>`): List of fields that should automatically become tags.
- `management.tracing.exemplars.include` (Third party; type `TracingProperties.Exemplars.Include`; default `sampled-traces`): Traces to include as exemplars. 'all' is not supported when using Prometheus.
- `management.tracing.export.enabled` (Third party; type `Boolean`; default `true`): Whether auto-configuration of tracing is enabled to export and propagate traces.
- `management.tracing.export.otlp.enabled` (Third party; type `Boolean`; default `true`): Whether auto-configuration of tracing is enabled to export OTLP traces.
- `management.tracing.export.zipkin.connect-timeout` (Third party; type `Duration`; default `1s`): Connection timeout for requests to Zipkin.
- `management.tracing.export.zipkin.enabled` (Third party; type `Boolean`; default `true`): Whether auto-configuration of tracing is enabled to export Zipkin traces.
- `management.tracing.export.zipkin.encoding` (Third party; type `ZipkinProperties.Encoding`; default `json`): How to encode the POST body to the Zipkin API.
- `management.tracing.export.zipkin.endpoint` (Third party; type `String`; default `http://localhost:9411/api/v2/spans`): URL to the Zipkin API.
- `management.tracing.export.zipkin.read-timeout` (Third party; type `Duration`; default `10s`): Read timeout for requests to Zipkin.
- `management.tracing.mdc.span-id-key` (Third party; type `String`; default `spanId`): Key under which the span ID is added to the `org.slf4j.MDC`.
- `management.tracing.mdc.trace-id-key` (Third party; type `String`; default `traceId`): Key under which the trace ID is added to the `org.slf4j.MDC`.
- `management.tracing.propagation.consume` (Third party; type `List<TracingProperties.Propagation.PropagationType>`; default `["W3C", "B3", "B3_MULTI"]`): Tracing context propagation types consumed by the application.
- `management.tracing.propagation.produce` (Third party; type `List<TracingProperties.Propagation.PropagationType>`; default `["W3C"]`): Tracing context propagation types produced by the application.
- `management.tracing.propagation.type` (Third party; type `List<TracingProperties.Propagation.PropagationType>`): Tracing context propagation types produced and consumed by the application. Setting this property overrides the more fine-grained propagation type properties.
- `management.tracing.sampling.probability` (Third party; type `Float`; default `0.1`): Probability in the range from 0.0 to 1.0 that a trace will be sampled.
