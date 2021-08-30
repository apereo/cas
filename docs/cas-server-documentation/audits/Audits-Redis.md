---
layout: default
title: CAS - Audit Configuration
category: Logs & Audits
---
{% include variables.html %}

# Redis Audits

If you intend to use a Redis database for auditing functionality, enable the following module in your configuration:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-audit-redis" %}

{% include_cached casproperties.html properties="cas.audit.redis" %}

