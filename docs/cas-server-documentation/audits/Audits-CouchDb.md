---
layout: default
title: CAS - Audit Configuration
category: Logs & Audits
---
{% include variables.html %}

# CouchDb Audits

If you intend to use a CouchDb database for auditing functionality, enable the following module in your configuration:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-audit-couchdb" %}

{% include_cached casproperties.html properties="cas.audit.couch-db" %}
