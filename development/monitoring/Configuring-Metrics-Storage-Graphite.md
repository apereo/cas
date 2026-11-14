# Graphite Storage - CAS Metrics

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Metrics-Storage-Graphite.html

Export CAS metrics to a Graphite server for storage and graphing.

By default, metrics are exported to Graphite running on your local machine. The Graphite server host and port to use can be provided using:

The following settings and properties are available from the CAS configuration catalog:

- `management.metrics.export.graphite.duration-units` (Third party; type `TimeUnit`; deprecation `ERROR`, replaced by `management.graphite.metrics.export.duration-units`)
- `management.metrics.export.graphite.enabled` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `management.graphite.metrics.export.enabled`)
- `management.metrics.export.graphite.graphite-tags-enabled` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `management.graphite.metrics.export.graphite-tags-enabled`)
- `management.metrics.export.graphite.host` (Third party; type `String`; deprecation `ERROR`, replaced by `management.graphite.metrics.export.host`)
- `management.metrics.export.graphite.port` (Third party; type `Integer`; deprecation `ERROR`, replaced by `management.graphite.metrics.export.port`)
- `management.metrics.export.graphite.protocol` (Third party; type `GraphiteProtocol`; deprecation `ERROR`, replaced by `management.graphite.metrics.export.protocol`)
- `management.metrics.export.graphite.rate-units` (Third party; type `TimeUnit`; deprecation `ERROR`, replaced by `management.graphite.metrics.export.rate-units`)
- `management.metrics.export.graphite.step` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.graphite.metrics.export.step`)
- `management.metrics.export.graphite.tags-as-prefix` (Third party; type `String[]`; deprecation `ERROR`, replaced by `management.graphite.metrics.export.tags-as-prefix`)
