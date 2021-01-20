---
layout: default
title: CAS - Audit Configuration
category: Logs & Audits
---
{% include variables.html %}

# DynamoDb Audits

If you intend to use a DynamoDb database for auditing functionality, enable the following module in your configuration:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-audit-dynamodb" %}
         
{% include casproperties.html properties="cas.audit.dynamo-db" %}

