---
layout: default
title: CAS - Redis Throttling Authentication Attempts
category: Authentication
---
{% include variables.html %}

# Redis Throttling Authentication Attempts

Uses Redis to prevent successive failed login attempts for a particular username from the same IP address.
Each failed attempt is recorded under a key built from the client IP address and the username (compared without regard to case),
which holds only the two most recent failures and expires on its own once the configured failure range has passed.
Checking an attempt reads that single key, so its cost does not grow with the number of users or audit records.

The Redis connection is configured through the same settings as [Redis audits](../audits/Audits-Redis.html), and throttled
attempts are still recorded in the [CAS audit log](../audits/Audits.html).

Enable the following module in your configuration overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-throttle-redis" %}

{% include_cached casproperties.html properties="cas.audit.redis" %}
