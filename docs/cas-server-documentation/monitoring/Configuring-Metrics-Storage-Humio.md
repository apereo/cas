---
layout: default
title: CAS - Metrics
category: Monitoring & Statistics
---

{% include variables.html %}

# Humio Storage - CAS Metrics

By default, the Humio registry periodically pushes metrics to `cloud.humio.com`. To export metrics to SaaS Humio, you must provide your API token.

{% include_cached casproperties.html thirdPartyStartsWith="management.metrics.export.humio" %}
