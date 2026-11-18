# Stackdriver Storage - CAS Metrics

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Metrics-Storage-Stackdriver.html

The Stackdriver registry periodically pushes metrics to Stackdriver. To export metrics to SaaS Stackdriver, you must provide your Google Cloud project ID.

The following settings and properties are available from the CAS configuration catalog:

- `management.metrics.export.stackdriver.batch-size` (Third party; type `Integer`; deprecation `ERROR`, replaced by `management.stackdriver.metrics.export.batch-size`)
- `management.metrics.export.stackdriver.connect-timeout` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.stackdriver.metrics.export.connect-timeout`)
- `management.metrics.export.stackdriver.enabled` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `management.stackdriver.metrics.export.enabled`)
- `management.metrics.export.stackdriver.num-threads` (Third party; type `Integer`; deprecation `ERROR`, no replacement)
- `management.metrics.export.stackdriver.project-id` (Third party; type `String`; deprecation `ERROR`, replaced by `management.stackdriver.metrics.export.project-id`)
- `management.metrics.export.stackdriver.read-timeout` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.stackdriver.metrics.export.read-timeout`)
- `management.metrics.export.stackdriver.resource-labels` (Third party; type `Map<String,String>`; deprecation `ERROR`, replaced by `management.stackdriver.metrics.export.resource-labels`)
- `management.metrics.export.stackdriver.resource-type` (Third party; type `String`; deprecation `ERROR`, replaced by `management.stackdriver.metrics.export.resource-type`)
- `management.metrics.export.stackdriver.step` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.stackdriver.metrics.export.step`)
- `management.metrics.export.stackdriver.use-semantic-metric-types` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `management.stackdriver.metrics.export.use-semantic-metric-types`)
