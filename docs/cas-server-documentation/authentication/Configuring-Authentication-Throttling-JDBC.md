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
depends on the [CAS auditing functionality](../audits/Audits.html) via databases.

Enable the following module in your configuration overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-throttle-jdbc" %}

{% include {{ version }}/rdbms-configuration.md configKey="cas.authn.throttle.jdbc" %}

{% include {{ version }}/jdbc-audit-configuration.md configKey="cas.authn.throttle.jdbc" %}

For additional instructions on how to configure auditing, please [review the following guide](../audits/Audits.html).
