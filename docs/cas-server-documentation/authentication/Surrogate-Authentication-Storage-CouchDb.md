---
layout: default
title: CAS - Surrogate Authentication
category: Authentication
---
{% include variables.html %}


# CouchDb Surrogate Authentication Registration

CouchDb support for surrogate authentication is enabled by including the following dependencies in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-surrogate-authentication-couchdb" %}

Surrogate accounts may also be retrieved from an CouchDb instance. By default, this takes 
the form of surrogate/principal key/value pairs. Users authorized as surrogates may be 
listed multiple times to authorize them to access multiple accounts.

{% include casproperties.html properties="cas.authn.surrogate.couch-db" %}
