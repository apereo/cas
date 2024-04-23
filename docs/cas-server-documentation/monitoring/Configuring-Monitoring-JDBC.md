---
layout: default
title: CAS - Monitoring
category: Monitoring & Statistics
---

{% include variables.html %}

# JDBC Monitoring

Monitor the status and availability of a relational SQL database.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-jdbc-monitor" %}

{% include_cached casproperties.html properties="cas.monitor.jdbc" %}

{% include_cached actuators.html endpoints="health" healthIndicators="dataSourceHealthIndicator" %}
