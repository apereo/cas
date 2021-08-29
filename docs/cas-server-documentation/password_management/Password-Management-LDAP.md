---
layout: default
title: CAS - Password Management
category: Password Management
---

{% include variables.html %}

# Password Management - LDAP

The account password and security questions may be stored inside an LDAP server.

LDAP support is enabled by including the following dependencies in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-pm-ldap" %}

{% include_cached casproperties.html properties="cas.authn.pm.ldap" %}
