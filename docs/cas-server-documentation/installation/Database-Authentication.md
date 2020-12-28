---
layout: default
title: CAS - Database Authentication
category: Authentication
---
{% include variables.html %}


# Database Authentication

Database authentication is enabled by including the following dependencies in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-jdbc" %}

To learn how to configure database drivers, [please see this guide](JDBC-Drivers.html).

## Configuration

### Query Database Authentication

{% include {{ version }}/rdbms-configuration.md configKey="cas.authn.jdbc.query[0]" %}

{% include {{ version }}/principal-transformation.md configKey="cas.authn.jdbc.query[0]" %}

{% include {{ version }}/password-encoding.md configKey="cas.authn.jdbc.query[0]" %}

{% include {{ version }}/jdbc-query-authentication-configuration.md %}


### Search Database Authentication

{% include {{ version }}/rdbms-configuration.md configKey="cas.authn.jdbc.search[0]" %}

{% include {{ version }}/principal-transformation.md configKey="cas.authn.jdbc.search[0]" %}

{% include {{ version }}/password-encoding.md configKey="cas.authn.jdbc.search[0]" %}

{% include {{ version }}/jdbc-search-authentication-configuration.md %}


### Bind Database Authentication

{% include {{ version }}/rdbms-configuration.md configKey="cas.authn.jdbc.bind[0]" %}

{% include {{ version }}/principal-transformation.md configKey="cas.authn.jdbc.bind[0]" %}

{% include {{ version }}/password-encoding.md configKey="cas.authn.jdbc.bind[0]" %}

{% include {{ version }}/jdbc-bind-authentication-configuration.md %}


### Encode Database Authentication

{% include {{ version }}/rdbms-configuration.md configKey="cas.authn.jdbc.encode[0]" %}

{% include {{ version }}/principal-transformation.md configKey="cas.authn.jdbc.encode[0]" %}

{% include {{ version }}/password-encoding.md configKey="cas.authn.jdbc.encode[0]" %}

{% include {{ version }}/jdbc-encode-authentication-configuration.md %}

## Password Policy Enforcement

A certain number of database authentication schemes have limited support for detecting locked/disabled/etc accounts
via column names that are defined in the CAS settings. To learn how to enforce a password policy, please [review this guide](Password-Policy-Enforcement.html).
