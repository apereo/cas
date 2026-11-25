# New Relic Storage - CAS Metrics

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Metrics-Storage-NewRelic.html

Export CAS metrics to New Relic, which receives them periodically using your account id and API key.

New Relic registry pushes metrics to New Relic periodically. To export metrics to New Relic, your API key and account id must be provided:

The following settings and properties are available from the CAS configuration catalog:

- `management.newrelic.metrics.export.account-id` (Third party; type `String`): New Relic account ID.
- `management.newrelic.metrics.export.api-key` (Third party; type `String`): New Relic API key.
- `management.newrelic.metrics.export.batch-size` (Third party; type `Integer`; default `10000`): Number of measurements per request to use for this backend. If more measurements are found, then multiple requests will be made.
- `management.newrelic.metrics.export.client-provider-type` (Third party; type `ClientProviderType`; default `insights-api`): Client provider type to use.
- `management.newrelic.metrics.export.connect-timeout` (Third party; type `Duration`; default `1s`): Connection timeout for requests to this backend.
- `management.newrelic.metrics.export.enabled` (Third party; type `Boolean`; default `true`): Whether exporting of metrics to this backend is enabled.
- `management.newrelic.metrics.export.event-type` (Third party; type `String`; default `SpringBootSample`): The event type that should be published. This property will be ignored if 'meter-name-event-type-enabled' is set to 'true'.
- `management.newrelic.metrics.export.meter-name-event-type-enabled` (Third party; type `Boolean`; default `false`): Whether to send the meter name as the event type instead of using the 'event-type' configuration property value. Can be set to 'true' if New Relic guidelines are not being followed or event types consistent with previous Spring Boot releases are required.
- `management.newrelic.metrics.export.read-timeout` (Third party; type `Duration`; default `10s`): Read timeout for requests to this backend.
- `management.newrelic.metrics.export.step` (Third party; type `Duration`; default `1m`): Step size (i.e. reporting frequency) to use.
- `management.newrelic.metrics.export.uri` (Third party; type `String`; default `https://insights-collector.newrelic.com`): URI to ship metrics to.
