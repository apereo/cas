# AppOptics Storage - CAS Metrics

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Metrics-Storage-AppOptics.html

By default, the AppOptics registry periodically pushes metrics to `api.appoptics.com/v1/measurements`. To export metrics to SaaS AppOptics, your API token must be provided.

The following settings and properties are available from the CAS configuration catalog:

- `management.metrics.export.appoptics.api-token` (Third party; type `String`; deprecation `ERROR`, replaced by `management.appoptics.metrics.export.api-token`)
- `management.metrics.export.appoptics.batch-size` (Third party; type `Integer`; deprecation `ERROR`, replaced by `management.appoptics.metrics.export.batch-size`)
- `management.metrics.export.appoptics.connect-timeout` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.appoptics.metrics.export.connect-timeout`)
- `management.metrics.export.appoptics.enabled` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `management.appoptics.metrics.export.enabled`)
- `management.metrics.export.appoptics.floor-times` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `management.appoptics.metrics.export.floor-times`)
- `management.metrics.export.appoptics.host-tag` (Third party; type `String`; deprecation `ERROR`, replaced by `management.appoptics.metrics.export.host-tag`)
- `management.metrics.export.appoptics.num-threads` (Third party; type `Integer`; deprecation `ERROR`, no replacement)
- `management.metrics.export.appoptics.read-timeout` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.appoptics.metrics.export.read-timeout`)
- `management.metrics.export.appoptics.step` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.appoptics.metrics.export.step`)
- `management.metrics.export.appoptics.uri` (Third party; type `String`; deprecation `ERROR`, replaced by `management.appoptics.metrics.export.uri`)
