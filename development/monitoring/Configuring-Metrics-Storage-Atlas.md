# Atlas Storage - CAS Metrics

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Metrics-Storage-Atlas.html

Export CAS metrics to Netflix Atlas, a dimensional time-series database.

By default, metrics are exported to Atlas running on your local machine. The location of the Atlas server to use can be provided using:

The following settings and properties are available from the CAS configuration catalog:

- `management.atlas.metrics.export.batch-size` (Third party; type `Integer`; default `10000`): Number of measurements per request to use for this backend. If more measurements are found, then multiple requests will be made.
- `management.atlas.metrics.export.config-refresh-frequency` (Third party; type `Duration`; default `10s`): Frequency for refreshing config settings from the LWC service.
- `management.atlas.metrics.export.config-time-to-live` (Third party; type `Duration`; default `150s`): Time to live for subscriptions from the LWC service.
- `management.atlas.metrics.export.config-uri` (Third party; type `String`; default `http://localhost:7101/lwc/api/v1/expressions/local-dev`): URI for the Atlas LWC endpoint to retrieve current subscriptions.
- `management.atlas.metrics.export.connect-timeout` (Third party; type `Duration`; default `1s`): Connection timeout for requests to this backend.
- `management.atlas.metrics.export.enabled` (Third party; type `Boolean`; default `true`): Whether exporting of metrics to this backend is enabled.
- `management.atlas.metrics.export.eval-uri` (Third party; type `String`; default `http://localhost:7101/lwc/api/v1/evaluate`): URI for the Atlas LWC endpoint to evaluate the data for a subscription.
- `management.atlas.metrics.export.lwc-enabled` (Third party; type `Boolean`; default `false`): Whether to enable streaming to Atlas LWC.
- `management.atlas.metrics.export.lwc-ignore-publish-step` (Third party; type `Boolean`; default `true`): Whether expressions with the same step size as Atlas publishing should be ignored for streaming. Used for cases where data being published to Atlas is also sent into streaming from the backend.
- `management.atlas.metrics.export.lwc-step` (Third party; type `Duration`; default `5s`): Step size (reporting frequency) to use for streaming to Atlas LWC. This is the highest supported resolution for getting an on-demand stream of the data. It must be less than or equal to management.metrics.export.atlas.step and management.metrics.export.atlas.step should be an even multiple of this value.
- `management.atlas.metrics.export.meter-time-to-live` (Third party; type `Duration`; default `15m`): Time to live for meters that do not have any activity. After this period the meter will be considered expired and will not get reported.
- `management.atlas.metrics.export.num-threads` (Third party; type `Integer`; default `4`): Number of threads to use with the metrics publishing scheduler.
- `management.atlas.metrics.export.read-timeout` (Third party; type `Duration`; default `10s`): Read timeout for requests to this backend.
- `management.atlas.metrics.export.step` (Third party; type `Duration`; default `1m`): Step size (i.e. reporting frequency) to use.
- `management.atlas.metrics.export.uri` (Third party; type `String`; default `http://localhost:7101/api/v1/publish`): URI of the Atlas server.
