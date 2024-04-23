---
layout: default
title: CAS - Configuring Authentication Throttling
category: Authentication
---
{% include variables.html %}

# JDBC Throttling Authentication Attempts

Queries a database data source used by the CAS audit facility to 
prevent successive failed login attempts for a particular username 
from the same IP address. This component requires and 
depends on the [JDBC auditing functionality](../audits/Audits-Database.html).

Enable the following module in your configuration overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-throttle-jdbc" %}

{% include_cached casproperties.html properties="cas.authn.throttle.jdbc" %}
