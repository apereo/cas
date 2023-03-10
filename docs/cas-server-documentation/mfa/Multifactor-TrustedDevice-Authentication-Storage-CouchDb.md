---
layout: default
title: CAS - Trusted Device Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# CouchDb Device Storage - Multifactor Authentication Trusted Device/Browser

User decisions may also be kept inside a CouchDb instance.

<div class="alert alert-warning">:warning: <strong>Usage</strong>
<p><strong>This feature is deprecated and is scheduled to be removed in the future</strong>.</p>
</div>

Support is provided via the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-trusted-mfa-couchdb" %}

<div class="alert alert-warning">:warning: <strong>Usage</strong>
<p><strong>This feature is deprecated and is scheduled to be removed in the future</strong>.</p>
</div>

{% include_cached casproperties.html properties="cas.authn.mfa.trusted.couch-db" %}
