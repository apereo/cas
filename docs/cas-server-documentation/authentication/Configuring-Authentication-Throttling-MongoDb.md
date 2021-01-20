---
layout: default
title: CAS - Configuring Authentication Throttling
category: Authentication
---
{% include variables.html %}

# MongoDb Throttling Authentication Attempts

Queries a MongoDb data source used by the CAS audit facility to 
prevent successive failed login attempts for a particular username 
from the same IP address. This component requires and depends on 
the [CAS auditing functionality](../audits/Audits-MongoDb.html) via MongoDb.

Enable the following module in your configuration overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-throttle-mongo" %}

{% include casproperties.html properties="cas.audit.mongo" %}
