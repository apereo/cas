---
layout: default
title: CAS - AppOptics Storage - CAS Metrics
category: Monitoring & Statistics
---

{% include variables.html %}

# AppOptics Storage - CAS Metrics

By default, the AppOptics registry periodically pushes metrics to `api.appoptics.com/v1/measurements`. 
To export metrics to SaaS AppOptics, your API token must be provided.

<div class="alert alert-warning">:warning: <strong>Deprecated</strong><p>AppOptics has been retired as a service, and Spring Boot
deprecates its AppOptics metrics export with no replacement. Expect this registry to be removed in a future CAS version,
and move to another metrics storage option.</p></div>

{% include_cached casproperties.html thirdPartyStartsWith="management.appoptics.metrics.export" %}
