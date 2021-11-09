---
layout: default
title: CAS - U2F - FIDO Universal 2nd Factor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Redis U2F - FIDO Universal Registration

Device registrations may be kept inside a Redis instance by including the following module in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-u2f-redis" %}

{% include_cached casproperties.html properties="cas.authn.mfa.u2f.redis" %}
