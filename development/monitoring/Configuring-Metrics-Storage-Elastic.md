# Elastic Storage - CAS Metrics

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Metrics-Storage-Elastic.html

By default, metrics are exported to Elastic running on your local machine. You can provide the location of the Elastic server via CAS settings.

The following settings and properties are available from the CAS configuration catalog:

- `management.metrics.export.elastic.api-key-credentials` (Third party; type `String`; deprecation `ERROR`, replaced by `management.elastic.metrics.export.api-key-credentials`)
- `management.metrics.export.elastic.auto-create-index` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `management.elastic.metrics.export.auto-create-index`)
- `management.metrics.export.elastic.batch-size` (Third party; type `Integer`; deprecation `ERROR`, replaced by `management.elastic.metrics.export.batch-size`)
- `management.metrics.export.elastic.connect-timeout` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.elastic.metrics.export.connect-timeout`)
- `management.metrics.export.elastic.enabled` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `management.elastic.metrics.export.enabled`)
- `management.metrics.export.elastic.host` (Third party; type `String`; deprecation `ERROR`, replaced by `management.elastic.metrics.export.host`)
- `management.metrics.export.elastic.index` (Third party; type `String`; deprecation `ERROR`, replaced by `management.elastic.metrics.export.index`)
- `management.metrics.export.elastic.index-date-format` (Third party; type `String`; deprecation `ERROR`, replaced by `management.elastic.metrics.export.index-date-format`)
- `management.metrics.export.elastic.index-date-separator` (Third party; type `String`; deprecation `ERROR`, replaced by `management.elastic.metrics.export.index-date-separator`)
- `management.metrics.export.elastic.num-threads` (Third party; type `Integer`; deprecation `ERROR`, no replacement)
- `management.metrics.export.elastic.password` (Third party; type `String`; deprecation `ERROR`, replaced by `management.elastic.metrics.export.password`)
- `management.metrics.export.elastic.pipeline` (Third party; type `String`; deprecation `ERROR`, replaced by `management.elastic.metrics.export.pipeline`)
- `management.metrics.export.elastic.read-timeout` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.elastic.metrics.export.read-timeout`)
- `management.metrics.export.elastic.step` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.elastic.metrics.export.step`)
- `management.metrics.export.elastic.timestamp-field-name` (Third party; type `String`; deprecation `ERROR`, replaced by `management.elastic.metrics.export.timestamp-field-name`)
- `management.metrics.export.elastic.user-name` (Third party; type `String`; deprecation `ERROR`, replaced by `management.elastic.metrics.export.user-name`)
