---
layout: default
title: CAS - Actuator Endpoint - Http Exchanges
category: Monitoring & Statistics
---

{% include variables.html %}

# Actuator Endpoint - Http Exchanges

Displays HTTP exchange information (by default, the last 100 HTTP request-response exchanges). Requires an `HttpExchangeRepository` bean.

{% include_cached actuators.html endpoints="httpexchanges" %}
