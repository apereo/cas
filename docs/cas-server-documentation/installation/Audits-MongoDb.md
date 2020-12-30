---
layout: default
title: CAS - Audit Configuration
category: Logs & Audits
---
{% include variables.html %}

# MongoDb Audits

If you intend to use a MongoDb database for auditing functionality, enable the following module in your configuration:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-audit-mongo" %}

{% include {{ version }}/mongodb-configuration.md configKey="cas.audit" %}

{% include {{ version }}/mongodb-audit-configuration.md %}

