# New Relic Storage - CAS Metrics

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Metrics-Storage-NewRelic.html

Export CAS metrics to New Relic, which receives them periodically using your account id and API key.

New Relic registry pushes metrics to New Relic periodically. To export metrics to New Relic, your API key and account id must be provided:

The following settings and properties are available from the CAS configuration catalog:

- `management.metrics.export.newrelic.account-id` (Third party; type `String`; deprecation `ERROR`, replaced by `management.newrelic.metrics.export.account-id`)
- `management.metrics.export.newrelic.api-key` (Third party; type `String`; deprecation `ERROR`, replaced by `management.newrelic.metrics.export.api-key`)
- `management.metrics.export.newrelic.batch-size` (Third party; type `Integer`; deprecation `ERROR`, replaced by `management.newrelic.metrics.export.batch-size`)
- `management.metrics.export.newrelic.client-provider-type` (Third party; type `ClientProviderType`; deprecation `ERROR`, replaced by `management.newrelic.metrics.export.client-provider-type`)
- `management.metrics.export.newrelic.connect-timeout` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.newrelic.metrics.export.connect-timeout`)
- `management.metrics.export.newrelic.enabled` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `management.newrelic.metrics.export.enabled`)
- `management.metrics.export.newrelic.event-type` (Third party; type `String`; deprecation `ERROR`, replaced by `management.newrelic.metrics.export.event-type`)
- `management.metrics.export.newrelic.meter-name-event-type-enabled` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `management.newrelic.metrics.export.meter-name-event-type-enabled`)
- `management.metrics.export.newrelic.num-threads` (Third party; type `Integer`; deprecation `ERROR`, no replacement)
- `management.metrics.export.newrelic.read-timeout` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.newrelic.metrics.export.read-timeout`)
- `management.metrics.export.newrelic.step` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.newrelic.metrics.export.step`)
- `management.metrics.export.newrelic.uri` (Third party; type `String`; deprecation `ERROR`, replaced by `management.newrelic.metrics.export.uri`)
