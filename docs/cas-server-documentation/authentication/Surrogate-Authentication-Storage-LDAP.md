---
layout: default
title: CAS - Surrogate Authentication
category: Authentication
---
{% include variables.html %}


# LDAP Surrogate Authentication

LDAP support for surrogate authentication is enabled by including the following dependencies in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-surrogate-authentication-ldap" %}

Surrogate accounts may also be retrieved from an LDAP instance. Such accounts are expected to be found in a configured attribute defined for the primary user in LDAP whose value(s) may be examined against a regular expression pattern of your own choosing to further narrow down the list of authorized surrogate accounts. 

{% include_cached casproperties.html properties="cas.authn.surrogate.ldap" %}
