# Elastic Storage - CAS Metrics

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Metrics-Storage-Elastic.html

By default, metrics are exported to Elastic running on your local machine. You can provide the location of the Elastic server via CAS settings.

The following settings and properties are available from the CAS configuration catalog:

- `management.elastic.metrics.export.api-key-credentials` (Third party; type `String`): Base64-encoded credentials string. Mutually exclusive with user-name and password.
- `management.elastic.metrics.export.auto-create-index` (Third party; type `Boolean`; default `true`): Whether to create the index automatically if it does not exist.
- `management.elastic.metrics.export.batch-size` (Third party; type `Integer`; default `10000`): Number of measurements per request to use for this backend. If more measurements are found, then multiple requests will be made.
- `management.elastic.metrics.export.connect-timeout` (Third party; type `Duration`; default `1s`): Connection timeout for requests to this backend.
- `management.elastic.metrics.export.enable-source` (Third party; type `Boolean`; default `false`): Whether to enable _source in the default index template when auto-creating the index.
- `management.elastic.metrics.export.enabled` (Third party; type `Boolean`; default `true`): Whether exporting of metrics to this backend is enabled.
- `management.elastic.metrics.export.host` (Third party; type `String`; default `http://localhost:9200`): Host to export metrics to.
- `management.elastic.metrics.export.index` (Third party; type `String`; default `micrometer-metrics`): Index to export metrics to.
- `management.elastic.metrics.export.index-date-format` (Third party; type `String`; default `yyyy-MM`): Index date format used for rolling indices. Appended to the index name.
- `management.elastic.metrics.export.index-date-separator` (Third party; type `String`; default `-`): Prefix to separate the index name from the date format used for rolling indices.
- `management.elastic.metrics.export.password` (Third party; type `String`): Login password of the Elastic server. Mutually exclusive with api-key-credentials.
- `management.elastic.metrics.export.pipeline` (Third party; type `String`): Ingest pipeline name. By default, events are not pre-processed.
- `management.elastic.metrics.export.read-timeout` (Third party; type `Duration`; default `10s`): Read timeout for requests to this backend.
- `management.elastic.metrics.export.step` (Third party; type `Duration`; default `1m`): Step size (i.e. reporting frequency) to use.
- `management.elastic.metrics.export.timestamp-field-name` (Third party; type `String`; default `@timestamp`): Name of the timestamp field.
- `management.elastic.metrics.export.user-name` (Third party; type `String`): Login user of the Elastic server. Mutually exclusive with api-key-credentials.
