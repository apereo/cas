# Graphite Storage - CAS Metrics

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Metrics-Storage-Graphite.html

Export CAS metrics to a Graphite server for storage and graphing.

By default, metrics are exported to Graphite running on your local machine. The Graphite server host and port to use can be provided using:

The following settings and properties are available from the CAS configuration catalog:

- `management.graphite.metrics.export.duration-units` (Third party; type `TimeUnit`; default `milliseconds`): Base time unit used to report durations.
- `management.graphite.metrics.export.enabled` (Third party; type `Boolean`; default `true`): Whether exporting of metrics to Graphite is enabled.
- `management.graphite.metrics.export.graphite-tags-enabled` (Third party; type `Boolean`): Whether Graphite tags should be used, as opposed to a hierarchical naming convention. Enabled by default unless "tagsAsPrefix" is set.
- `management.graphite.metrics.export.host` (Third party; type `String`; default `localhost`): Host of the Graphite server to receive exported metrics.
- `management.graphite.metrics.export.port` (Third party; type `Integer`; default `2004`): Port of the Graphite server to receive exported metrics.
- `management.graphite.metrics.export.protocol` (Third party; type `GraphiteProtocol`; default `pickled`): Protocol to use while shipping data to Graphite.
- `management.graphite.metrics.export.rate-units` (Third party; type `TimeUnit`; default `seconds`): Base time unit used to report rates.
- `management.graphite.metrics.export.step` (Third party; type `Duration`; default `1m`): Step size (i.e. reporting frequency) to use.
- `management.graphite.metrics.export.tags-as-prefix` (Third party; type `String[]`): For the hierarchical naming convention, turn the specified tag keys into part of the metric prefix. Ignored if "graphiteTagsEnabled" is true.
