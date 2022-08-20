---
layout: default
title: CAS - Multifactor Authentication Triggers
category: Multifactor Authentication
---

{% include variables.html %}

# Global Multifactor Authentication Trigger

MFA can be triggered for all applications and users regardless of individual settings. In other words, you may only enable this trigger
if you wish to activate a multifactor provider for all requests, applications and users.

{% include_cached casproperties.html properties="cas.authn.mfa.triggers.global" %}

