---
layout: default
title: CAS - Metrics
category: Monitoring & Statistics
---

{% include variables.html %}

# Graphite Storage - CAS Metrics

By default, metrics are exported to Graphite running on your local
machine. The Graphite server host and port to use can be provided using:

{% include_cached casproperties.html thirdPartyStartsWith="management.metrics.export.graphite" %}
