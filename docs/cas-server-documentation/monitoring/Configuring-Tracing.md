---
layout: default
title: CAS - Monitoring
category: Monitoring & Statistics
---

{% include variables.html %}

# CAS - Distributed Tracing

CAS ships auto configuration for the following tracers:

- OpenTelemetry 
- OpenZipkin Brave

## OpenTelemetry

[OpenTelemetry (Otel)](https://opentelemetry.io) is a collection of standardized vendor-agnostic tools, APIs, and SDKs. It allows us to instrument, generate, and 
collect telemetry data, which helps in analyzing application behavior or performance. Telemetry data can include logs, 
metrics, and traces. We can either automatically or manually instrument the code for HTTP, DB calls, and more.

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-tracing-otel" %}

## OpenZipkin Brave

[Brave](https://github.com/openzipkin/brave) is a distributed tracing instrumentation library. Brave typically intercepts production requests to gather timing data, 
correlate and propagate trace contexts. While typically trace data is sent to Zipkin server or Amazon X-Ray, etc.

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-tracing-brave" %}

## Configuration

{% include_cached casproperties.html thirdPartyStartsWith="management.tracing" %}
