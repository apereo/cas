---
layout: default
title: CAS - Configuring Authentication Policy
category: Authentication
---
{% include variables.html %}

# Any - Authentication Policy

Satisfied if any handler succeeds. Supports a `tryAll` flag to avoid short circuiting
and try every handler even if one prior succeeded.

{% include casproperties.html properties="cas.authn.policy.any" %}

