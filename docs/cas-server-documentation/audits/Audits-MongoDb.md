---
layout: default
title: CAS - MongoDb Audits
category: Logs & Audits
---
{% include variables.html %}

# MongoDb Audits

Store the CAS audit trail in a MongoDB collection, so audit records from every node end up in one durable place.

If you intend to use a MongoDb database for auditing functionality, enable the following module in your configuration:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-audit-mongo" %}

{% include_cached casproperties.html properties="cas.audit.mongo" %}

