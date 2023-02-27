---
layout: default
title: CAS - Attribute Release Consent
category: Attributes
---

{% include variables.html %}

# CouchDb - Attribute Consent Storage

Support is enabled by including the following module in the WAR Overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-consent-couchdb" %}

<div class="alert alert-warning">:warning: <strong>Usage</strong>
<p><strong>This feature is deprecated and is scheduled to be removed in the future</strong>.</p>
</div>

## Configuration

{% include_cached casproperties.html properties="cas.consent.couch-db" %}
