---
layout: default
title: CAS - Actuator Endpoint - Thread Dump
category: Monitoring & Statistics
---

{% include variables.html %}

# Actuator Endpoint - Thread Dump

The `threaddump` actuator endpoint returns a snapshot of the threads in the CAS JVM, which helps diagnose hung or slow requests.

Performs a thread dump.

{% include_cached actuators.html endpoints="threaddump" %}
