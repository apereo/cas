---
layout: default
title: CAS - Atlas Storage - CAS Metrics
category: Monitoring & Statistics
---

{% include variables.html %}

# Atlas Storage - CAS Metrics

Export CAS metrics to Netflix Atlas, a dimensional time-series database.

By default, metrics are exported to Atlas running on your
local machine. The location of the Atlas server to use can be provided using:

{% include_cached casproperties.html thirdPartyStartsWith="management.metrics.export.atlas" %}
