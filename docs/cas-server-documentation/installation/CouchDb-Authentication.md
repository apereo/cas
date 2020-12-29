---
layout: default
title: CAS - CouchDb Authentication
category: Authentication
---

{% include variables.html %}

# CouchDb Authentication

Verify and authenticate credentials against a [CouchDb](http://couchdb.apache.org/) instance
via pac4j. CAS will automatically create the design documents required by pac4j.
Support is enabled by including the following dependency in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-couchdb-authentication" %}

{% include {{ version }}/principal-transformation-configuration.md configKey="cas.authn.couch-db" %}

{% include {{ version }}/password-encoding-configuration.md configKey="cas.authn.couch-db" %}

{% include {{ version }}/couchdb-configuration.md configKey="cas.authn" %}

{% include {{ version }}/couchdb-authentication-configuration.md %}
