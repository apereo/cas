---
layout: default
title: CAS - Metrics
category: Monitoring & Statistics
---

{% include variables.html %}

# Simple Storage - CAS Metrics

Micrometer ships with a simple, in-memory backend that is automatically
used as a fallback if no other registry is configured.
This allows you to see what metrics are collected in the metrics endpoint.

The in-memory backend disables itself as soon as you’re using any of
the other available backend. You can also disable it explicitly:

{% include_cached casproperties.html thirdPartyStartsWith="management.metrics.export.simple" %}
