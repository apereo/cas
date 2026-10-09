---
layout: default
title: CAS - New Relic Storage - CAS Metrics
category: Monitoring & Statistics
---

{% include variables.html %}

# New Relic Storage - CAS Metrics

Export CAS metrics to New Relic, which receives them periodically using your account id and API key.

New Relic registry pushes metrics to New Relic periodically. To export
metrics to New Relic, your API key and account id must be provided:

{% include_cached casproperties.html thirdPartyStartsWith="management.metrics.export.newrelic" %}
