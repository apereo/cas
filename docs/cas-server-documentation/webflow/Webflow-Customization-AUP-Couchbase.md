---
layout: default
title: CAS - Web Flow Acceptable Usage Policy
category: Acceptable Usage Policy
---

{% include variables.html %}

# Couchbase Acceptable Usage Policy

CAS can be configured to use a Couchbase instance as the storage mechanism. Upon accepting the policy, the
decision is kept inside a document with a `username` column and the AUP attribute name with the result of the decision.

<div class="alert alert-warning">:warning: <strong>Usage</strong>
<p><strong>This feature is deprecated and is scheduled to be removed in the future</strong>.</p>
</div>

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-aup-couchbase" %}

{% include_cached casproperties.html properties="cas.acceptable-usage-policy.couchbase" %}
