---
layout: default
title: CAS - Simple Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Simple Multifactor Authentication - Rate Limiting

CAS is able to support rate-limiting for token requests based on the token-bucket
algorithm, via the [Bucket4j](https://bucket4j.com/) project. This means that token requests that reach a certain configurable capacity within
a time window may either be blocked or _throttled_ to slow down. This is done to
protect the system from overloading, allowing you to introduce a scenario to allow CAS `120` token requests per minute with a refill rate of `10` requests per
second that would continually increase in the capacity bucket. Please note that the bucket allocation strategy is specific to the client IP address.

{% include_cached casproperties.html properties="cas.authn.mfa.simple.bucket4j" %}
