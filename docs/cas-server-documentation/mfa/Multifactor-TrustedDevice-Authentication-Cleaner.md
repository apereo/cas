---
layout: default
title: CAS - Trusted Device Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Multifactor Authentication Trusted Device/Browser - Cleaning

User decisions must be remembered and processed later on subsequent requests. A background *cleaner* process is also automatically scheduled to 
scan the chosen repository/database/registry periodically and remove expired records based on configured threshold parameters.

<div class="alert alert-warning">:warning: <strong>Cleaner Usage</strong><p>In a clustered CAS deployment, it is best to keep 
the cleaner running on one designated CAS node only and turn it off on all others via CAS settings. Keeping the cleaner running on all 
nodes may likely lead to severe performance and locking issues.</p></div>

{% include_cached casproperties.html properties="cas.authn.mfa.trusted.cleaner" %}
