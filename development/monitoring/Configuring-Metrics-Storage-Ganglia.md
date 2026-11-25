# Ganglia Storage - CAS Metrics

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Metrics-Storage-Ganglia.html

Export CAS metrics to a Ganglia monitoring server.

By default, metrics are exported to Ganglia running on your local machine. The Ganglia server host and port to use can be provided using:

The following settings and properties are available from the CAS configuration catalog:

- `management.ganglia.metrics.export.addressing-mode` (Third party; type `GMetric.UDPAddressingMode`; default `multicast`): UDP addressing mode, either unicast or multicast.
- `management.ganglia.metrics.export.duration-units` (Third party; type `TimeUnit`; default `milliseconds`): Base time unit used to report durations.
- `management.ganglia.metrics.export.enabled` (Third party; type `Boolean`; default `true`): Whether exporting of metrics to Ganglia is enabled.
- `management.ganglia.metrics.export.host` (Third party; type `String`; default `localhost`): Host of the Ganglia server to receive exported metrics.
- `management.ganglia.metrics.export.port` (Third party; type `Integer`; default `8649`): Port of the Ganglia server to receive exported metrics.
- `management.ganglia.metrics.export.step` (Third party; type `Duration`; default `1m`): Step size (i.e. reporting frequency) to use.
- `management.ganglia.metrics.export.time-to-live` (Third party; type `Integer`; default `1`): Time to live for metrics on Ganglia. Set the multicast Time-To-Live to be one greater than the number of hops (routers) between the hosts.
