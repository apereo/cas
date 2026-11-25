# AppOptics Storage - CAS Metrics

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Metrics-Storage-AppOptics.html

By default, the AppOptics registry periodically pushes metrics to `api.appoptics.com/v1/measurements`. To export metrics to SaaS AppOptics, your API token must be provided.

> **Deprecated**
>
> AppOptics has been retired as a service, and Spring Boot deprecates its AppOptics metrics export with no replacement. Expect this registry to be removed in a future CAS version, and move to another metrics storage option.

The following settings and properties are available from the CAS configuration catalog:

- `management.appoptics.metrics.export.api-token` (Third party; type `String`; deprecation `WARNING`, no replacement): AppOptics API token.
- `management.appoptics.metrics.export.batch-size` (Third party; type `Integer`; deprecation `WARNING`, no replacement; default `500`): Number of measurements per request to use for this backend. If more measurements are found, then multiple requests will be made.
- `management.appoptics.metrics.export.connect-timeout` (Third party; type `Duration`; deprecation `WARNING`, no replacement; default `5s`): Connection timeout for requests to this backend.
- `management.appoptics.metrics.export.enabled` (Third party; type `Boolean`; default `true`): Whether exporting of metrics to this backend is enabled.
- `management.appoptics.metrics.export.floor-times` (Third party; type `Boolean`; deprecation `WARNING`, no replacement; default `false`): Whether to ship a floored time, useful when sending measurements from multiple hosts to align them on a given time boundary.
- `management.appoptics.metrics.export.host-tag` (Third party; type `String`; deprecation `WARNING`, no replacement; default `instance`): Tag that will be mapped to "@host" when shipping metrics to AppOptics.
- `management.appoptics.metrics.export.read-timeout` (Third party; type `Duration`; default `10s`): Read timeout for requests to this backend.
- `management.appoptics.metrics.export.step` (Third party; type `Duration`; default `1m`): Step size (i.e. reporting frequency) to use.
- `management.appoptics.metrics.export.uri` (Third party; type `String`; deprecation `WARNING`, no replacement; default `https://api.appoptics.com/v1/measurements`): URI to ship metrics to.
