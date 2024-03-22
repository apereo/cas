---
layout: default
title: CAS - Metrics
category: Monitoring & Statistics
---

{% include variables.html %}

# Ganglia Storage - CAS Metrics

By default, metrics are exported to Ganglia running on your local
machine. The Ganglia server host and port to use can be provided using:

{% include_cached casproperties.html thirdPartyStartsWith="management.metrics.export.ganglia" %}
