# StatsD Storage - CAS Metrics

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Metrics-Storage-StatsD.html

Export CAS metrics to a StatsD agent over UDP.

The StatsD registry pushes metrics over UDP to a StatsD agent eagerly. By default, metrics are exported to a StatsD agent running on your local machine. The StatsD agent host and port to use can be provided using:

The following settings and properties are available from the CAS configuration catalog:

- `management.statsd.metrics.export.buffered` (Third party; type `Boolean`; default `true`): Whether measurements should be buffered before sending to the StatsD server.
- `management.statsd.metrics.export.enabled` (Third party; type `Boolean`; default `true`): Whether exporting of metrics to StatsD is enabled.
- `management.statsd.metrics.export.flavor` (Third party; type `StatsdFlavor`; default `datadog`): StatsD line protocol to use.
- `management.statsd.metrics.export.host` (Third party; type `String`; default `localhost`): Host of the StatsD server to receive exported metrics.
- `management.statsd.metrics.export.max-packet-length` (Third party; type `Integer`; default `1400`): Total length of a single payload should be kept within your network's MTU.
- `management.statsd.metrics.export.polling-frequency` (Third party; type `Duration`; default `10s`): How often gauges will be polled. When a gauge is polled, its value is recalculated and if the value has changed (or publishUnchangedMeters is true), it is sent to the StatsD server.
- `management.statsd.metrics.export.port` (Third party; type `Integer`; default `8125`): Port of the StatsD server to receive exported metrics.
- `management.statsd.metrics.export.protocol` (Third party; type `StatsdProtocol`; default `udp`): Protocol of the StatsD server to receive exported metrics.
- `management.statsd.metrics.export.publish-unchanged-meters` (Third party; type `Boolean`; default `true`): Whether to send unchanged meters to the StatsD server.
- `management.statsd.metrics.export.step` (Third party; type `Duration`; default `1m`): Step size to use in computing windowed statistics like max. To get the most out of these statistics, align the step interval to be close to your scrape interval.
