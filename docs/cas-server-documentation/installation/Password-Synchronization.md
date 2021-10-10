---
layout: default
title: CAS - Password Synchronization
category: Authentication
---
{% include variables.html %}


# Password Synchronization

CAs presents the ability to synchronize and update the account password in a variety of
destinations as part of the authentication event. If the authentication attempt is successful,
CAS will attempt to capture the provided password and update destinations that are specified
in CAS settings. Failing to synchronize an account password generally produces errors in the logs
and the event is not considered a catastrophic failure.

## Configuration

Allow the user to synchronize account password to a variety of destinations in-place.

{% include_cached {{ version }}/ldap-configuration.md configKey="cas.authn.password-sync.ldap[0]" %}

## LDAP

Synchronize account passwords with one or more LDAP servers. Support is enabled by including the 
following dependencies in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-ldap" %}

{% include_cached casproperties.html properties="cas.authn.password-sync.ldap" %}
