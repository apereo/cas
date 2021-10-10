---
layout: default
title: CAS - Configuring Authentication Throttling
category: Authentication
---
{% include variables.html %}

# CouchDb Throttling Authentication Attempts

Queries a CouchDb data source used by the CAS audit facility to prevent successive failed login attempts 
for a particular username from the same IP address. This component requires and 
depends on the [CAS auditing functionality](../audits/Audits.html) via CouchDb.

Enable the following module in your configuration overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-throttle-couchdb" %}

{% include_cached casproperties.html properties="cas.audit.couch-db" %}

When using this feature the audit facility should be in synchronous mode. For additional instructions 
on how to configure auditing, please [review the following guide](../audits/Audits.html).
