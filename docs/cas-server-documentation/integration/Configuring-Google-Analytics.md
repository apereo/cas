---
layout: default
title: CAS - Google Analytics
category: Integration
---

{% include variables.html %}

# Google Analytics

Google Analytics can be used to deliver useful statistics. create custom dimensions and metrics to gain
insight into CAS and user traffic.

<div class="alert alert-info"><strong>Compatibility</strong><p>
Google Analytics 4 is the next-generation measurement solution, and it has replaced Universal Analytics in CAS. 
On July 1, 2023, standard Universal Analytics properties will stop processing new hits. If you still rely on 
Universal Analytics, we recommend that you prepare to use Google Analytics 4 going forward.
</p></div>

Support is enabled by including the following module in the WAR Overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-google-analytics" %}

Furthermore, CAS presents the ability to drop in a special cookie upon successful authentication events to be later process
and consumed by Google Analytics. The value of this cookie is determined as a principal/authentication attribute.

{% include_cached casproperties.html properties="cas.google-analytics" %}
