# Simple Storage - CAS Metrics

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Metrics-Storage-Simple.html

Micrometer ships with a simple, in-memory backend that is automatically used as a fallback if no other registry is configured. This allows you to see what metrics are collected in the metrics endpoint.

The in-memory backend disables itself as soon as you’re using any of the other available backend. You can also disable it explicitly:

The following settings and properties are available from the CAS configuration catalog:

- `management.simple.metrics.export.enabled` (Third party; type `Boolean`; default `true`): Whether exporting of metrics to this backend is enabled.
- `management.simple.metrics.export.mode` (Third party; type `CountingMode`; default `cumulative`): Counting mode.
- `management.simple.metrics.export.step` (Third party; type `Duration`; default `1m`): Step size (i.e. reporting frequency) to use.
