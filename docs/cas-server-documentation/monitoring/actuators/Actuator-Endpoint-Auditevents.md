---
layout: default
title: CAS - Actuator Endpoint - AuditEvents
category: Monitoring & Statistics
---

{% include variables.html %}

# Actuator Endpoint - AuditEvents

Exposes audit events information for the current application. Requires an `AuditEventRepository` bean.

{% include_cached actuators.html endpoints="auditevents" %}
