---
layout: default
title: CAS - Actuator Endpoint - Health
category: Monitoring & Statistics
---

{% include variables.html %}

# Actuator Endpoint - Health

The `health` actuator endpoint reports whether CAS and the systems it depends on are up, for load balancers and monitoring tools.

Shows application health information.

{% include_cached actuators.html endpoints="health" %}
