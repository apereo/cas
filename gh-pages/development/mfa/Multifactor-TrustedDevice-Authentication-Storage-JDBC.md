---
layout: default
title: CAS - Trusted Device Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# JDBC Device Storage - Multifactor Authentication Trusted Device/Browser

User decisions may also be kept inside a regular RDBMS of your own choosing.

Support is provided via the following module:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-trusted-mfa-jdbc" %}

To learn how to configure database drivers, [please see this guide](../installation/JDBC-Drivers.html).

{% include casproperties.html properties="cas.authn.mfa.trusted.jpa" %}
