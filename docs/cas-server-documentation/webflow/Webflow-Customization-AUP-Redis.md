---
layout: default
title: CAS - Redis Acceptable Usage Policy
category: Acceptable Usage Policy
---

{% include variables.html %}

# Redis Acceptable Usage Policy

CAS can be configured to use a Redis instance as the storage mechanism. Decisions
are mapped to a combination of CAS username and the designated AUP attribute name, and are read back at later logins
when the principal does not carry the AUP attribute itself.

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-aup-redis" %}

{% include_cached casproperties.html properties="cas.acceptable-usage-policy.redis" %}
