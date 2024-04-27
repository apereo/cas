---
layout: default
title: CAS - Metrics
category: Monitoring & Statistics
---

{% include variables.html %}

# Stackdriver Storage - CAS Metrics

The Stackdriver registry periodically pushes metrics to Stackdriver. To export metrics to SaaS Stackdriver, 
you must provide your Google Cloud project ID.

{% include_cached casproperties.html thirdPartyStartsWith="management.metrics.export.stackdriver" %}
