---
layout: default
title: CAS - Metrics
category: Monitoring & Statistics
---

{% include variables.html %}

# Elastic Storage - CAS Metrics

By default, metrics are exported to Elastic running on your local machine. You can 
provide the location of the Elastic server via CAS settings.

{% include_cached casproperties.html thirdPartyStartsWith="management.metrics.export.elastic" %}
