---
layout: default
title: CAS - Passwordless Authentication
category: Authentication
---
{% include variables.html %}

# LDAP Passwordless Authentication Storage

This strategy allows one to locate a user record in an LDAP directory. The 
record is expected to carry the user's phone number
or email address via configurable attributes.

Support is enabled by including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-passwordless-ldap" %}

{% include_cached casproperties.html properties="cas.authn.passwordless.accounts.ldap" %}
