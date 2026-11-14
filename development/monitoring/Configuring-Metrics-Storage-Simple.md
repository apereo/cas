# Simple Storage - CAS Metrics

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Metrics-Storage-Simple.html

Micrometer ships with a simple, in-memory backend that is automatically used as a fallback if no other registry is configured. This allows you to see what metrics are collected in the metrics endpoint.

The in-memory backend disables itself as soon as you’re using any of the other available backend. You can also disable it explicitly:

The following settings and properties are available from the CAS configuration catalog:

- `management.metrics.export.simple.enabled` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `management.simple.metrics.export.enabled`)
- `management.metrics.export.simple.mode` (Third party; type `CountingMode`; deprecation `ERROR`, replaced by `management.simple.metrics.export.mode`)
- `management.metrics.export.simple.step` (Third party; type `Duration`; deprecation `ERROR`, replaced by `management.simple.metrics.export.step`)
