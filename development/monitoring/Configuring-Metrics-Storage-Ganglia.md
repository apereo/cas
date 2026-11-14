# Ganglia Storage - CAS Metrics

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Metrics-Storage-Ganglia.html

Export CAS metrics to a Ganglia monitoring server.

By default, metrics are exported to Ganglia running on your local machine. The Ganglia server host and port to use can be provided using:

The following settings and properties are available from the CAS configuration catalog:

- `management.metrics.export.ganglia.addressing-mode` (Third party; type `GMetric.UDPAddressingMode`; deprecation `ERROR`, replaced by `management.ganglia.metrics.export.addressing-mode`)
- `management.metrics.export.ganglia.duration-units` (Third party; type `TimeUnit`; deprecation `ERROR`, replaced by `management.ganglia.metrics.export.duration-units`)
- `management.metrics.export.ganglia.enabled` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `management.ganglia.metrics.export.enabled`)
- `management.metrics.export.ganglia.host` (Third party; type `String`; deprecation `ERROR`, replaced by `management.ganglia.metrics.export.host`)
- `management.metrics.export.ganglia.port` (Third party; type `Integer`; deprecation `ERROR`, replaced by `management.ganglia.metrics.export.port`)
- `management.metrics.export.ganglia.rate-units` (Third party; type `TimeUnit`; deprecation `ERROR`, no replacement)
- `management.metrics.export.ganglia.step` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.ganglia.metrics.export.step`)
- `management.metrics.export.ganglia.time-to-live` (Third party; type `Integer`; deprecation `ERROR`, replaced by `management.ganglia.metrics.export.time-to-live`)
