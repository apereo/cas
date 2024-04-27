---
layout: default
title: CAS - Metrics
category: Monitoring & Statistics
---

{% include variables.html %}

# AppOptics Storage - CAS Metrics

By default, the AppOptics registry periodically pushes metrics to `api.appoptics.com/v1/measurements`. 
To export metrics to SaaS AppOptics, your API token must be provided.

{% include_cached casproperties.html thirdPartyStartsWith="management.metrics.export.appoptics" %}
