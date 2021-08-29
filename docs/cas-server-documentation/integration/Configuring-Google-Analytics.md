---
layout: default
title: CAS - Google Analytics
category: Integration
---

{% include variables.html %}

# Google Analytics

Google Analytics can be used to deliver useful statistics. create custom dimensions and metrics to gain
insight into CAS and user traffic.

Support is enabled by including the following module in the WAR Overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-google-analytics" %}

Furthermore, CAS presents the ability to drop in a special cookie upon successful authentication events to be later process
and consumed by Google Analytics. The value of this cookie is determined as a principal/authentication attribute.

{% include_cached casproperties.html properties="cas.google-analytics" %}
