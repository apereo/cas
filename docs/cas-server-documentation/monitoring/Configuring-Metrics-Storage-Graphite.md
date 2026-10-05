---
layout: default
title: CAS - Graphite Storage - CAS Metrics
category: Monitoring & Statistics
---

{% include variables.html %}

# Graphite Storage - CAS Metrics

Export CAS metrics to a Graphite server for storage and graphing.

By default, metrics are exported to Graphite running on your local
machine. The Graphite server host and port to use can be provided using:

{% include_cached casproperties.html thirdPartyStartsWith="management.metrics.export.graphite" %}
