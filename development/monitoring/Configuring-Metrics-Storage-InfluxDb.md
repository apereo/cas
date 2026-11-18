# InfluxDb Storage - CAS Metrics

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Metrics-Storage-InfluxDb.html

Export CAS metrics to an InfluxDB time-series database.

By default, metrics are exported to Influx running on your local machine. The location of the Influx server to use can be provided using:

The following settings and properties are available from the CAS configuration catalog:

- `management.metrics.export.influx.api-version` (Third party; type `InfluxApiVersion`; deprecation `ERROR`, replaced by `management.influx.metrics.export.api-version`)
- `management.metrics.export.influx.auto-create-db` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `management.influx.metrics.export.auto-create-db`)
- `management.metrics.export.influx.batch-size` (Third party; type `Integer`; deprecation `ERROR`, replaced by `management.influx.metrics.export.batch-size`)
- `management.metrics.export.influx.bucket` (Third party; type `String`; deprecation `ERROR`, replaced by `management.influx.metrics.export.bucket`)
- `management.metrics.export.influx.compressed` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `management.influx.metrics.export.compressed`)
- `management.metrics.export.influx.connect-timeout` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.influx.metrics.export.connect-timeout`)
- `management.metrics.export.influx.consistency` (Third party; type `InfluxConsistency`; deprecation `ERROR`, replaced by `management.influx.metrics.export.consistency`)
- `management.metrics.export.influx.db` (Third party; type `String`; deprecation `ERROR`, replaced by `management.influx.metrics.export.db`)
- `management.metrics.export.influx.enabled` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `management.influx.metrics.export.enabled`)
- `management.metrics.export.influx.num-threads` (Third party; type `Integer`; deprecation `ERROR`, no replacement)
- `management.metrics.export.influx.org` (Third party; type `String`; deprecation `ERROR`, replaced by `management.influx.metrics.export.org`)
- `management.metrics.export.influx.password` (Third party; type `String`; deprecation `ERROR`, replaced by `management.influx.metrics.export.password`)
- `management.metrics.export.influx.read-timeout` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.influx.metrics.export.read-timeout`)
- `management.metrics.export.influx.retention-duration` (Third party; type `String`; deprecation `ERROR`, replaced by `management.influx.metrics.export.retention-duration`)
- `management.metrics.export.influx.retention-policy` (Third party; type `String`; deprecation `ERROR`, replaced by `management.influx.metrics.export.retention-policy`)
- `management.metrics.export.influx.retention-replication-factor` (Third party; type `Integer`; deprecation `ERROR`, replaced by `management.influx.metrics.export.retention-replication-factor`)
- `management.metrics.export.influx.retention-shard-duration` (Third party; type `String`; deprecation `ERROR`, replaced by `management.influx.metrics.export.retention-shard-duration`)
- `management.metrics.export.influx.step` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.influx.metrics.export.step`)
- `management.metrics.export.influx.token` (Third party; type `String`; deprecation `ERROR`, replaced by `management.influx.metrics.export.token`)
- `management.metrics.export.influx.uri` (Third party; type `String`; deprecation `ERROR`, replaced by `management.influx.metrics.export.uri`)
- `management.metrics.export.influx.user-name` (Third party; type `String`; deprecation `ERROR`, replaced by `management.influx.metrics.export.user-name`)
