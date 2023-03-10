---
layout: default
title: CAS - Couchbase Authentication
category: Authentication
---
{% include variables.html %}


# Couchbase Authentication

Verify and authenticate credentials using [Couchbase](http://www.couchbase.com/).

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-couchbase-authentication" %}

<div class="alert alert-warning">:warning: <strong>Usage</strong>
<p><strong>This feature is deprecated and is scheduled to be removed in the future</strong>.</p>
</div>

{% include_cached casproperties.html properties="cas.authn.couchbase" %}

## Couchbase Principal Attributes

The above dependency may also be used, in the event that principal attributes 
need to be fetched from a Couchbase database without necessarily authenticating credentials against Couchbase. 

<div class="alert alert-warning">:warning: <strong>Usage</strong>
<p><strong>This feature is deprecated and is scheduled to be removed in the future</strong>.</p>
</div>

{% include_cached casproperties.html properties="cas.authn.attribute-repository.couchbase" %}
