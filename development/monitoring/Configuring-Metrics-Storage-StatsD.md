# StatsD Storage - CAS Metrics

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Metrics-Storage-StatsD.html

Export CAS metrics to a StatsD agent over UDP.

The StatsD registry pushes metrics over UDP to a StatsD agent eagerly. By default, metrics are exported to a StatsD agent running on your local machine. The StatsD agent host and port to use can be provided using:

The following settings and properties are available from the CAS configuration catalog:

- `management.metrics.export.statsd.enabled` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `management.statsd.metrics.export.enabled`)
- `management.metrics.export.statsd.flavor` (Third party; type `StatsdFlavor`; deprecation `ERROR`, replaced by `management.statsd.metrics.export.flavor`)
- `management.metrics.export.statsd.host` (Third party; type `String`; deprecation `ERROR`, replaced by `management.statsd.metrics.export.host`)
- `management.metrics.export.statsd.max-packet-length` (Third party; type `Integer`; deprecation `ERROR`, replaced by `management.statsd.metrics.export.max-packet-length`)
- `management.metrics.export.statsd.polling-frequency` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.statsd.metrics.export.polling-frequency`)
- `management.metrics.export.statsd.port` (Third party; type `Integer`; deprecation `ERROR`, replaced by `management.statsd.metrics.export.port`)
- `management.metrics.export.statsd.protocol` (Third party; type `StatsdProtocol`; deprecation `ERROR`, replaced by `management.statsd.metrics.export.protocol`)
- `management.metrics.export.statsd.publish-unchanged-meters` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `management.statsd.metrics.export.publish-unchanged-meters`)
- `management.metrics.export.statsd.queue-size` (Third party; deprecation `ERROR`, no replacement)
