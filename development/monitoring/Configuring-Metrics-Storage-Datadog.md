# Datadog Storage - CAS Metrics

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Metrics-Storage-Datadog.html

Export CAS metrics to Datadog, which receives them periodically using your API key.

Datadog registry pushes metrics to `datadoghq` periodically. To export metrics to Datadog, your API key must be provided:

The following settings and properties are available from the CAS configuration catalog:

- `management.metrics.export.datadog.api-key` (Third party; type `String`; deprecation `ERROR`, replaced by `management.datadog.metrics.export.api-key`)
- `management.metrics.export.datadog.application-key` (Third party; type `String`; deprecation `ERROR`, replaced by `management.datadog.metrics.export.application-key`)
- `management.metrics.export.datadog.batch-size` (Third party; type `Integer`; deprecation `ERROR`, replaced by `management.datadog.metrics.export.batch-size`)
- `management.metrics.export.datadog.connect-timeout` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.datadog.metrics.export.connect-timeout`)
- `management.metrics.export.datadog.descriptions` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `management.datadog.metrics.export.descriptions`)
- `management.metrics.export.datadog.enabled` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `management.datadog.metrics.export.enabled`)
- `management.metrics.export.datadog.host-tag` (Third party; type `String`; deprecation `ERROR`, replaced by `management.datadog.metrics.export.host-tag`)
- `management.metrics.export.datadog.num-threads` (Third party; type `Integer`; deprecation `ERROR`, no replacement)
- `management.metrics.export.datadog.read-timeout` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.datadog.metrics.export.read-timeout`)
- `management.metrics.export.datadog.step` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.datadog.metrics.export.step`)
- `management.metrics.export.datadog.uri` (Third party; type `String`; deprecation `ERROR`, replaced by `management.datadog.metrics.export.uri`)

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.
