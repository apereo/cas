---
layout: default
title: CAS - Configuring Authentication Policy
category: Authentication
---
{% include variables.html %}

# Source Selection - Authentication Policy

Allows CAS to select authentication handlers based
on the credential source. This allows the authentication engine to restrict the task of validating credentials
to the selected source or account repository, as opposed to every authentication handler.

{% include_cached casproperties.html properties="cas.authn.policy.source-selection-enabled" %}
