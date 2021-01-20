---
layout: default
title: CAS - Audit Configuration
category: Logs & Audits
---
{% include variables.html %}

# Couchbase Audits

If you intend to use a Couchbase database for auditing functionality, enable the following module in your configuration:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-audit-couchbase" %}

{% include casproperties.html properties="cas.audit.couchbase" %}
