---
layout: default
title: CAS - Google Authenticator Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# JPA Google Authenticator Registration

Registration records and tokens may be kept inside a database instance via the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-gauth-jpa" %}

To learn how to configure database drivers, [please see this guide](../installation/JDBC-Drivers.html).

{% include_cached casproperties.html properties="cas.authn.mfa.gauth.jpa" %}

