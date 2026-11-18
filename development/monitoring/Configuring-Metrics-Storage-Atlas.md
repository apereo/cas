# Atlas Storage - CAS Metrics

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Metrics-Storage-Atlas.html

Export CAS metrics to Netflix Atlas, a dimensional time-series database.

By default, metrics are exported to Atlas running on your local machine. The location of the Atlas server to use can be provided using:

The following settings and properties are available from the CAS configuration catalog:

- `management.metrics.export.atlas.batch-size` (Third party; type `Integer`; deprecation `ERROR`, replaced by `management.atlas.metrics.export.batch-size`)
- `management.metrics.export.atlas.config-refresh-frequency` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.atlas.metrics.export.config-refresh-frequency`)
- `management.metrics.export.atlas.config-time-to-live` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.atlas.metrics.export.config-time-to-live`)
- `management.metrics.export.atlas.config-uri` (Third party; type `String`; deprecation `ERROR`, replaced by `management.atlas.metrics.export.config-uri`)
- `management.metrics.export.atlas.connect-timeout` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.atlas.metrics.export.connect-timeout`)
- `management.metrics.export.atlas.enabled` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `management.atlas.metrics.export.enabled`)
- `management.metrics.export.atlas.eval-uri` (Third party; type `String`; deprecation `ERROR`, replaced by `management.atlas.metrics.export.eval-uri`)
- `management.metrics.export.atlas.lwc-enabled` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `management.atlas.metrics.export.lwc-enabled`)
- `management.metrics.export.atlas.meter-time-to-live` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.atlas.metrics.export.meter-time-to-live`)
- `management.metrics.export.atlas.num-threads` (Third party; type `Integer`; deprecation `ERROR`, no replacement)
- `management.metrics.export.atlas.read-timeout` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.atlas.metrics.export.read-timeout`)
- `management.metrics.export.atlas.step` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.atlas.metrics.export.step`)
- `management.metrics.export.atlas.uri` (Third party; type `String`; deprecation `ERROR`, replaced by `management.atlas.metrics.export.uri`)
