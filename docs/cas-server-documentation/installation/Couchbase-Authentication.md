---
layout: default
title: CAS - Couchbase Authentication
category: Authentication
---
{% include variables.html %}


# Couchbase Authentication

Verify and authenticate credentials using [Couchbase](http://www.couchbase.com/).

Support is enabled by including the following dependency in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-couchbase-authentication" %}

{% include {{ version }}/principal-transformation.md configKey="cas.authn.couchbase" %}

{% include {{ version }}/password-encoding.md configKey="cas.authn.couchbase" %}

{% include {{ version }}/couchbase-configuration.md configKey="cas.authn.couchbase" %}

{% include {{ version }}/couchbase-authentication-configuration.md %}

## Couchbase Principal Attributes

The above dependency may also be used, in the event that principal attributes 
need to be fetched from a Couchbase database without necessarily authenticating credentials against Couchbase. 

{% include {{ version }}/couchbase-attribute-resolution-configuration.md %}
