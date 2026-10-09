---
layout: default
title: CAS - Distributed Tracing
description: "Distributed tracing for CAS with Micrometer Tracing, exporting to OpenTelemetry, Zipkin or Jaeger."
category: Monitoring & Statistics
---

{% include variables.html %}

# CAS - Distributed Tracing

CAS ships auto configuration for the following tracers:

| Platform      | Reference                                                 |
|---------------|-----------------------------------------------------------|
| OpenTelemetry | [See this guide](Configuring-Tracing-OpenTelemetry.html). |
| Zipkin Brave  | [See this guide](Configuring-Tracing-Zipkin.html).        |
| Jaeger        | [See this guide](Configuring-Tracing-Jaeger.html).        |

{% include_cached casproperties.html thirdPartyStartsWith="management.tracing" %}


