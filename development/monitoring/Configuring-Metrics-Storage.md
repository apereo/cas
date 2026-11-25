# Storage - CAS Metrics

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Metrics-Storage.html

CAS metrics may be routed to varying types of databases for storage and analytics. The following options are available:

- Simple (Memory)
- Graphite
- Ganglia
- Atlas
- Statsd
- InfluxDb
- Prometheus
- New Relic
- AWS CloudWatch
- …

Metrics are largely handled by [Micrometer](https://micrometer.io/). Choose the appropriate storage technology that best fits your needs and requirements.
