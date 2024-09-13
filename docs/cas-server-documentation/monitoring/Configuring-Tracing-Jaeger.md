---
layout: default
title: CAS - Monitoring
category: Monitoring & Statistics
---

{% include variables.html %}

# CAS - Jaeger Distributed Tracing

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-tracing-jaeger" %}

{% include_cached casproperties.html properties="cas.monitor.jaeger" 
    thirdPartyStartsWith="management.opentelemetry" %}

