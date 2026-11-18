# Humio Storage - CAS Metrics

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Metrics-Storage-Humio.html

By default, the Humio registry periodically pushes metrics to `cloud.humio.com`. To export metrics to SaaS Humio, you must provide your API token.

The following settings and properties are available from the CAS configuration catalog:

- `management.metrics.export.humio.api-token` (Third party; type `String`; deprecation `ERROR`, replaced by `management.humio.metrics.export.api-token`)
- `management.metrics.export.humio.batch-size` (Third party; type `Integer`; deprecation `ERROR`, replaced by `management.humio.metrics.export.batch-size`)
- `management.metrics.export.humio.connect-timeout` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.humio.metrics.export.connect-timeout`)
- `management.metrics.export.humio.enabled` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `management.humio.metrics.export.enabled`)
- `management.metrics.export.humio.num-threads` (Third party; type `Integer`; deprecation `ERROR`, no replacement)
- `management.metrics.export.humio.read-timeout` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.humio.metrics.export.read-timeout`)
- `management.metrics.export.humio.repository` (Third party; deprecation `ERROR`, no replacement)
- `management.metrics.export.humio.step` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.humio.metrics.export.step`)
- `management.metrics.export.humio.tags` (Third party; type `Map<String,String>`; deprecation `ERROR`, replaced by `management.humio.metrics.export.tags`)
- `management.metrics.export.humio.uri` (Third party; type `String`; deprecation `ERROR`, replaced by `management.humio.metrics.export.uri`)
