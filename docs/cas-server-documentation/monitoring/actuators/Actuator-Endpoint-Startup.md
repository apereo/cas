---
layout: default
title: CAS - Actuator Endpoint - Startup
category: Monitoring & Statistics
---

{% include variables.html %}

# Actuator Endpoint - Startup

Shows the startup steps data collected by the `ApplicationStartup`. Requires the `SpringApplication` to be configured with a `BufferingApplicationStartup`.

{% include_cached actuators.html endpoints="startup" %}
