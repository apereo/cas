---
layout: default
title: CAS - Database Authentication
category: Authentication
---
{% include variables.html %}

# Database Authentication

Database authentication is enabled by including the following dependencies in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-jdbc" %}

To learn how to configure database drivers, [please see this guide](../installation/JDBC-Drivers.html).

## Configuration

### Query Database Authentication

Authenticates a user by comparing the user password (which can be encoded with a password encoder)
against the password on record determined by a configurable database query.

{% include_cached casproperties.html properties="cas.authn.jdbc.query" %}


### Search Database Authentication

Searches for a user record by querying against a username and password;
the user is authenticated if at least one result is found.

{% include_cached casproperties.html properties="cas.authn.jdbc.search" %}

### Bind Database Authentication

Authenticates a user by attempting to create a database connection using the username and (hashed) password.

{% include_cached casproperties.html properties="cas.authn.jdbc.bind" %}


### Encode Database Authentication

A JDBC querying handler that will pull back the password and the private salt value for a user and validate the encoded
password using the public salt value. Assumes everything is inside the same database table. Supports settings for
number of iterations as well as private salt.

This password encoding method combines the private Salt and the public salt which it prepends to the password before hashing.
If multiple iterations are used, the bytecode hash of the first iteration is rehashed without the salt values. The final hash
is converted to hex before comparing it to the database value.

{% include_cached casproperties.html properties="cas.authn.jdbc.encode" %}

## Password Policy Enforcement

A certain number of database authentication schemes have limited support for detecting locked/disabled/etc accounts
via column names that are defined in the CAS settings. To learn how to enforce a password policy, please [review this guide](../installation/Password-Policy-Enforcement.html).
