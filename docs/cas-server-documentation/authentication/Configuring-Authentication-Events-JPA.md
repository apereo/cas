---
layout: default
title: CAS - Configuring Authentication Events
category: Authentication
---
{% include variables.html %}

# JPA Authentication Events

Stores authentication events into a RDBMS.

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-events-jpa" %}

{% include casproperties.html
modules="cas-server-support-events-jpa"
properties="cas.events.jpa" %}
