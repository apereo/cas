---
layout: default
title: CAS - Datadog Storage - CAS Metrics
category: Monitoring & Statistics
---

{% include variables.html %}

# Datadog Storage - CAS Metrics

Export CAS metrics to Datadog, which receives them periodically using your API key.

Datadog registry pushes metrics to `datadoghq` periodically. To export
metrics to Datadog, your API key must be provided:

{% include_cached casproperties.html thirdPartyStartsWith="management.metrics.export.datadog" %}
