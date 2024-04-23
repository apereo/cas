---
layout: default
title: CAS - Passwordless Authentication
category: Authentication
---
{% include variables.html %}

# JPA Passwordless Authentication Tokens

This strategy allows one to store tokens and manage their expiration policy using a relational database.

Support is enabled by including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-passwordless-jpa" %}

{% include_cached casproperties.html properties="cas.authn.passwordless.tokens.jpa" %}
