---
layout: default
title: CAS - Monitoring
category: Monitoring & Statistics
---

{% include variables.html %}

# CAS - Zipkin Distributed Tracing

[Brave](https://github.com/openzipkin/brave) is a distributed tracing instrumentation library. Brave typically intercepts production requests to gather timing data, 
correlate and propagate trace contexts. While typically trace data is sent to Zipkin server or Amazon X-Ray, etc.

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-tracing-zipkin" %}

{% include_cached casproperties.html thirdPartyStartsWith="management.zipkin.tracing" %}
