---
layout: default
title: CAS - Redis Google Authenticator Registration
category: Multifactor Authentication
---

{% include variables.html %}

# Redis Google Authenticator Registration

Keep Google Authenticator device registrations and used one-time codes in Redis.

Registration records and tokens may be kept inside a Redis instance via the following module:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-gauth-redis" %}

{% include_cached casproperties.html properties="cas.authn.mfa.gauth.redis" %}
