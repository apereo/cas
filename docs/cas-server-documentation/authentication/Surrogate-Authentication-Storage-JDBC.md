---
layout: default
title: CAS - Surrogate Authentication
category: Authentication
---
{% include variables.html %}


# JDBC Surrogate Authentication Registration

JDBC support for surrogate authentication is enabled by including the following dependencies in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-surrogate-authentication-jdbc" %}

Aside from the usual database settings, this mode requires the specification of 
two SQL queries; one that determines eligibility and one that is able to retrieve
the list of accounts that can be impersonated for a given admin user. 

{% include casproperties.html properties="cas.authn.surrogate.jdbc" %}
