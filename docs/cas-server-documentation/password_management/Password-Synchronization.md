---
layout: default
title: CAS - Password Synchronization
category: Authentication
---
{% include variables.html %}

# Password Synchronization

CAS presents the ability to synchronize and update the account password in a variety of
destinations as part of the authentication event. If the authentication attempt is successful,
CAS will attempt to capture the provided password and update destinations that are specified
in CAS settings. Failing to synchronize an account password generally produces errors in the logs
and the event is not considered a catastrophic failure.

## LDAP

Synchronize account passwords with one or more LDAP servers. Support is enabled by including the 
following dependencies in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-ldap" %}

{% include_cached casproperties.html properties="cas.authn.password-sync.ldap" %}
