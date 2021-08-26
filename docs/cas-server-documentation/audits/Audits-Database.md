---
layout: default
title: CAS - Audit Configuration
category: Logs & Audits
---
{% include variables.html %}

# Database Audits

If you intend to use a database for auditing functionality, enable the following module in your configuration:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-audit-jdbc" %}

To learn how to configure database drivers, please [review this guide](../installation/JDBC-Drivers.html).

{% include casproperties.html properties="cas.audit.jdbc" %}
