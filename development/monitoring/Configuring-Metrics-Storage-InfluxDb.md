# InfluxDb Storage - CAS Metrics

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Metrics-Storage-InfluxDb.html

Export CAS metrics to an InfluxDB time-series database.

By default, metrics are exported to Influx running on your local machine. The location of the Influx server to use can be provided using:

The following settings and properties are available from the CAS configuration catalog:

- `management.influx.metrics.export.api-version` (Third party; type `InfluxApiVersion`): API version of InfluxDB to use. Defaults to 'v1' unless an org is configured. If an org is configured, defaults to 'v2'.
- `management.influx.metrics.export.auto-create-db` (Third party; type `Boolean`; default `true`): Whether to create the Influx database if it does not exist before attempting to publish metrics to it. InfluxDB v1 only.
- `management.influx.metrics.export.batch-size` (Third party; type `Integer`; default `10000`): Number of measurements per request to use for this backend. If more measurements are found, then multiple requests will be made.
- `management.influx.metrics.export.bucket` (Third party; type `String`): Bucket for metrics. Use either the bucket name or ID. Defaults to the value of the db property if not set. InfluxDB v2 only.
- `management.influx.metrics.export.compressed` (Third party; type `Boolean`; default `true`): Whether to enable GZIP compression of metrics batches published to Influx.
- `management.influx.metrics.export.connect-timeout` (Third party; type `Duration`; default `1s`): Connection timeout for requests to this backend.
- `management.influx.metrics.export.consistency` (Third party; type `InfluxConsistency`; default `one`): Write consistency for each point.
- `management.influx.metrics.export.db` (Third party; type `String`; default `mydb`): Database to send metrics to. InfluxDB v1 only.
- `management.influx.metrics.export.enabled` (Third party; type `Boolean`; default `true`): Whether exporting of metrics to this backend is enabled.
- `management.influx.metrics.export.org` (Third party; type `String`): Org to write metrics to. InfluxDB v2 only.
- `management.influx.metrics.export.password` (Third party; type `String`): Login password of the Influx server. InfluxDB v1 only.
- `management.influx.metrics.export.read-timeout` (Third party; type `Duration`; default `10s`): Read timeout for requests to this backend.
- `management.influx.metrics.export.retention-duration` (Third party; type `String`): Time period for which Influx should retain data in the current database. For instance 7d, check the influx documentation for more details on the duration format. InfluxDB v1 only.
- `management.influx.metrics.export.retention-policy` (Third party; type `String`): Retention policy to use (Influx writes to the DEFAULT retention policy if one is not specified). InfluxDB v1 only.
- `management.influx.metrics.export.retention-replication-factor` (Third party; type `Integer`): How many copies of the data are stored in the cluster. Must be 1 for a single node instance. InfluxDB v1 only.
- `management.influx.metrics.export.retention-shard-duration` (Third party; type `String`): Time range covered by a shard group. For instance 2w, check the influx documentation for more details on the duration format. InfluxDB v1 only.
- `management.influx.metrics.export.step` (Third party; type `Duration`; default `1m`): Step size (i.e. reporting frequency) to use.
- `management.influx.metrics.export.token` (Third party; type `String`): Authentication token to use with calls to the InfluxDB backend. For InfluxDB v1, the Bearer scheme is used. For v2, the Token scheme is used.
- `management.influx.metrics.export.uri` (Third party; type `String`; default `http://localhost:8086`): URI of the Influx server.
- `management.influx.metrics.export.user-name` (Third party; type `String`): Login user of the Influx server. InfluxDB v1 only.
