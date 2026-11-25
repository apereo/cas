# Stackdriver Storage - CAS Metrics

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Metrics-Storage-Stackdriver.html

The Stackdriver registry periodically pushes metrics to Stackdriver. To export metrics to SaaS Stackdriver, you must provide your Google Cloud project ID.

The following settings and properties are available from the CAS configuration catalog:

- `management.stackdriver.metrics.export.auto-create-metric-descriptors` (Third party; type `Boolean`; default `true`): Whether it should be attempted to create a metric descriptor before writing a time series.
- `management.stackdriver.metrics.export.batch-size` (Third party; type `Integer`; default `10000`): Number of measurements per request to use for this backend. If more measurements are found, then multiple requests will be made.
- `management.stackdriver.metrics.export.connect-timeout` (Third party; type `Duration`; default `1s`): Connection timeout for requests to this backend.
- `management.stackdriver.metrics.export.enabled` (Third party; type `Boolean`; default `true`): Whether exporting of metrics to this backend is enabled.
- `management.stackdriver.metrics.export.metric-type-prefix` (Third party; type `String`; default `custom.googleapis.com/`): Prefix for metric type. Valid prefixes are described in the Google Cloud documentation (https://cloud.google.com/monitoring/custom-metrics#identifier).
- `management.stackdriver.metrics.export.project-id` (Third party; type `String`): Identifier of the Google Cloud project to monitor.
- `management.stackdriver.metrics.export.read-timeout` (Third party; type `Duration`; default `10s`): Read timeout for requests to this backend.
- `management.stackdriver.metrics.export.resource-labels` (Third party; type `Map<String,String>`): Monitored resource's labels.
- `management.stackdriver.metrics.export.resource-type` (Third party; type `String`; default `global`): Monitored resource type.
- `management.stackdriver.metrics.export.step` (Third party; type `Duration`; default `1m`): Step size (i.e. reporting frequency) to use.
- `management.stackdriver.metrics.export.use-semantic-metric-types` (Third party; type `Boolean`; default `false`): Whether to use semantically correct metric types. When false, counter metrics are published as the GAUGE MetricKind. When true, counter metrics are published as the CUMULATIVE MetricKind.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.
