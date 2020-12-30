---
layout: default
title: CAS - Audit Configuration
category: Logs & Audits
---
{% include variables.html %}

# Database Audits

If you intend to use a database for auditing functionality, enable the following module in your configuration:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-audit-jdbc" %}

To learn how to configure database drivers, please [review this guide](JDBC-Drivers.html).

{% include {{ version }}/rdbms-configuration.md configKey="cas.audit.jdbc" %}

{% include {{ version }}/jdbc-audit-configuration.md %}

{% include {{ version }}/job-scheduling-configuration.md configKey="cas.audit.jdbc" %}
