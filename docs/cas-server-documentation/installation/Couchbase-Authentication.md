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

The authentication strategy is able to fetch user attributes as part of the authentication event. To see the relevant list of CAS properties, please [review this guide](../configuration/Configuration-Properties.html#couchbase-authentication).

## Couchbase Principal Attributes

The above dependency may also be used, in the event that principal attributes need to be fetched from a Couchbase database without necessarily authenticating credentials against Couchbase. To see the relevant list of CAS properties, please [review this guide](../configuration/Configuration-Properties.html#couchbase).
