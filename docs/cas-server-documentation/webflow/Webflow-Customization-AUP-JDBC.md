---
layout: default
title: CAS - Web Flow Acceptable Usage Policy
category: Acceptable Usage Policy
---

{% include variables.html %}

# JDBC Acceptable Usage Policy

CAS can be configured to use a database as the storage mechanism. Upon accepting the
policy, the adopter is expected to provide a table name where the  decision
is kept and the table is assumed to contain a `username` column as well as
one that matches the AUP attribute name defined.

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-aup-jdbc" %}

{% include_cached casproperties.html properties="cas.acceptable-usage-policy.jdbc" %}
