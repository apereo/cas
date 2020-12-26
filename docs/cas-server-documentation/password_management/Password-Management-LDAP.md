---
layout: default
title: CAS - Password Management
category: Password Management
---

# Password Management - LDAP

The account password and security questions may be stored inside an LDAP server.

LDAP support is enabled by including the following dependencies in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-pm-ldap" %}

To see the relevant list of CAS properties, please [review this guide](../configuration/Configuration-Properties.html#ldap-password-management).
