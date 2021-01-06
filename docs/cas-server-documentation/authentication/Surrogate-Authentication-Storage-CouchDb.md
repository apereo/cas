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
listed multiple times to authorize them to access multiple accounts. Additionally, the 
CouchDb surrogate support may be configured to use a profile attribute containing a 
list of principals the user may surrogate for with the `profileBased` property. 

{% include {{ version }}/couchdb-accounts-surrogate-authentication-configuration.md %}
