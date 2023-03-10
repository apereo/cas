---
layout: default
title: CAS - Configuring Authentication Events
category: Authentication
---
{% include variables.html %}

# CouchDb Authentication Events

Stores authentication events inside a CouchDb instance.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-events-couchdb" %}

<div class="alert alert-warning">:warning: <strong>Usage</strong>
<p><strong>This feature is deprecated and is scheduled to be removed in the future</strong>.</p>
</div>

{% include_cached casproperties.html properties="cas.events.couch-db" %}

