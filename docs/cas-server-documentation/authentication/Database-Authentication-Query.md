---
layout: default
title: CAS - Database Authentication
category: Authentication
---
{% include variables.html %}

# Query Database Authentication

Authenticates a user by comparing the user password (which can be encoded with a password encoder)
against the password on record determined by a configurable database query.

{% include_cached casproperties.html properties="cas.authn.jdbc.query" %}

## Multitenancy

Configuration settings for database authentication can be specified in a multitenant environment.
Please [review this guide](../multitenancy/Multitenancy-Overview.html) for more information.
