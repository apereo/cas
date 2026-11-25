# Humio Storage - CAS Metrics

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Metrics-Storage-Humio.html

By default, the Humio registry periodically pushes metrics to `cloud.humio.com`. To export metrics to SaaS Humio, you must provide your API token.

The following settings and properties are available from the CAS configuration catalog:

- `management.humio.metrics.export.api-token` (Third party; type `String`): Humio API token.
- `management.humio.metrics.export.batch-size` (Third party; type `Integer`; default `10000`): Number of measurements per request to use for this backend. If more measurements are found, then multiple requests will be made.
- `management.humio.metrics.export.connect-timeout` (Third party; type `Duration`; default `5s`): Connection timeout for requests to this backend.
- `management.humio.metrics.export.enabled` (Third party; type `Boolean`; default `true`): Whether exporting of metrics to this backend is enabled.
- `management.humio.metrics.export.read-timeout` (Third party; type `Duration`; default `10s`): Read timeout for requests to this backend.
- `management.humio.metrics.export.step` (Third party; type `Duration`; default `1m`): Step size (i.e. reporting frequency) to use.
- `management.humio.metrics.export.tags` (Third party; type `Map<String,String>`): Humio tags describing the data source in which metrics will be stored. Humio tags are a distinct concept from Micrometer's tags. Micrometer's tags are used to divide metrics along dimensional boundaries.
- `management.humio.metrics.export.uri` (Third party; type `String`; default `https://cloud.humio.com`): URI to ship metrics to. If you need to publish metrics to an internal proxy en-route to Humio, you can define the location of the proxy with this.
