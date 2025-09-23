---
layout: default
title: CAS - Configuring Authentication Throttling
category: Authentication
---
{% include variables.html %}

# Throttling Authentication Attempts - Capacity

CAS is able to support request rate-limiting based on the token-bucket algorithm, via the [Bucket4j](https://bucket4j.com/) project. This
means that authentication requests that reach a certain configurable capacity within a time window may either be blocked or _throttled_ to slow down. This is done to 
protect the system from overloading, allowing you to introduce a scenario to allow CAS `120` authentication requests per minute with a refill rate of `10` requests per 
second that would continually increase in the capacity bucket. Please note that the bucket allocation strategy is specific to the client IP address.

Enable the following module in your configuration overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-throttle-bucket4j" %}

{% include_cached casproperties.html properties="cas.authn.throttle.bucket4j" %}
