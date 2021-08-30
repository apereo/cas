---
layout: default
title: CAS - Configuring Authentication Policy
category: Authentication
---
{% include variables.html %}

# Unique Principal - Authentication Policy

Satisfied if and only if the requesting principal has not already authenticated with CAS.
Otherwise the authentication event is blocked, preventing multiple logins.

<div class="alert alert-warning"><strong>Usage Warning</strong><p>Activating this policy is not without cost,
as CAS needs to query the ticket registry and all tickets present to 
determine whether the current user has established an authentication 
session anywhere. This will surely add a performance burden to the deployment. Use with care.</p></div>

{% include_cached casproperties.html properties="cas.authn.policy.unique-principal" %}
