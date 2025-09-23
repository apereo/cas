---
layout: default
title: CAS - Database Authentication
category: Authentication
---
{% include variables.html %}

# Encode Database Authentication

A JDBC querying handler that will pull back the password and the private salt value for a user to validate the encoded
password using the public salt value. Assumes everything is inside the same database table. Supports settings for
number of iterations as well as private salt.

This password encoding method combines the private Salt and the public salt which it prepends to the password before hashing.
If multiple iterations are used, the bytecode hash of the first iteration is rehashed without the salt values. The final hash
is converted to hex before comparing it to the database value.

{% include_cached casproperties.html properties="cas.authn.jdbc.encode" %}

## Multitenancy

Configuration settings for database authentication can be specified in a multitenant environment.
Please [review this guide](../multitenancy/Multitenancy-Overview.html) for more information.
