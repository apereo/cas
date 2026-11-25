# Datadog Storage - CAS Metrics

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Metrics-Storage-Datadog.html

Export CAS metrics to Datadog, which receives them periodically using your API key.

Datadog registry pushes metrics to `datadoghq` periodically. To export metrics to Datadog, your API key must be provided:

The following settings and properties are available from the CAS configuration catalog:

- `management.datadog.metrics.export.api-key` (Third party; type `String`): Datadog API key.
- `management.datadog.metrics.export.application-key` (Third party; type `String`): Datadog application key. Not strictly required, but improves the Datadog experience by sending meter descriptions, types, and base units to Datadog.
- `management.datadog.metrics.export.batch-size` (Third party; type `Integer`; default `10000`): Number of measurements per request to use for this backend. If more measurements are found, then multiple requests will be made.
- `management.datadog.metrics.export.compress` (Third party; type `Boolean`; default `true`): Whether payload should be compressed.
- `management.datadog.metrics.export.connect-timeout` (Third party; type `Duration`; default `1s`): Connection timeout for requests to this backend.
- `management.datadog.metrics.export.descriptions` (Third party; type `Boolean`; default `true`): Whether to publish descriptions metadata to Datadog. Turn this off to minimize the amount of metadata sent.
- `management.datadog.metrics.export.enabled` (Third party; type `Boolean`; default `true`): Whether exporting of metrics to this backend is enabled.
- `management.datadog.metrics.export.host-tag` (Third party; type `String`; default `instance`): Tag that will be mapped to "host" when shipping metrics to Datadog.
- `management.datadog.metrics.export.read-timeout` (Third party; type `Duration`; default `10s`): Read timeout for requests to this backend.
- `management.datadog.metrics.export.step` (Third party; type `Duration`; default `1m`): Step size (i.e. reporting frequency) to use.
- `management.datadog.metrics.export.uri` (Third party; type `String`; default `https://api.datadoghq.com`): URI to ship metrics to. Set this if you need to publish metrics to a Datadog site other than US, or to an internal proxy en-route to Datadog.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.
