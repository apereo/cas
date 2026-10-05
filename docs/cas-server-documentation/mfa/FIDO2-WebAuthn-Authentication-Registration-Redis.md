---
layout: default
title: CAS - Redis FIDO2 WebAuthn Multifactor Registration
category: Multifactor Authentication
---

{% include variables.html %}

# Redis FIDO2 WebAuthn Multifactor Registration

Keep FIDO2 WebAuthn device registrations in Redis.

Device registrations may be kept inside a Redis database instance by including the following module in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-webauthn-redis" %}

{% include_cached casproperties.html properties="cas.authn.mfa.web-authn.redis" %}
