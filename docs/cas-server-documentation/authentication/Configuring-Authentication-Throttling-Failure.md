---
layout: default
title: CAS - Configuring Authentication Throttling
category: Authentication
---
{% include variables.html %}

# Throttling Authentication Attempts - Failure

CAS provides a facility for limiting failed login attempts to support password guessing and related abuse scenarios.
A couple strategies are provided for tracking failed attempts:

1. Source IP - Limit successive failed logins against any username from the same IP address.
2. Source IP and username - Limit successive failed logins against a particular user from the same IP address.

All login throttling components that ship with CAS limit successive failed login attempts that exceed a threshold
rate, which is a time in seconds between two failures. The following properties are provided to define the failure rate.

* `threshold` - Number of failed login attempts.
* `rangeSeconds` - Period of time in seconds.

A failure rate of more than 1 per 3 seconds is indicative of an automated authentication attempt, which is a
reasonable basis for throttling policy. Regardless of policy care should be 
taken to weigh security against access;
overly restrictive policies may prevent legitimate authentication attempts.

<div class="alert alert-info mt-3">:information_source: <strong>Threshold Rate</strong><p>
The failure threshold rate is calculated as: <code>threshold / rangeSeconds</code>. For instance,
the failure rate for the above scenario would be <code>0.333333</code>. An authentication attempt may be considered throttled
if the request submission rate (calculated as the difference between the current date and the last submission date) exceeds
the failure threshold rate.
</p></div>

Enable the following module in your configuration overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-throttle" %}

## Configuration

{% include_cached casproperties.html properties="cas.authn.throttle" includes=".hazelcast,.core,.schedule,.failure" %}

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="throttles" %}

## Throttling Strategies
      
The following throttling strategies are offered by CAS.

| Storage          | Description                                         
|--------------------------------------------------------------------------------------------------------------------------
| IP Address       | Uses a memory map to prevent successive failed login attempts from the same IP address.
| IP Address and Username | Uses a memory map to prevent successive failed login attempts for a username from the same IP address.
| JDBC             | [See this guide](Configuring-Authentication-Throttling-JDBC.html).
| MongoDb          | [See this guide](Configuring-Authentication-Throttling-MongoDb.html).
| Redis            | [See this guide](Configuring-Authentication-Throttling-Redis.html).
| Hazelcast        | [See this guide](Configuring-Authentication-Throttling-Hazelcast.html).

## High Availability

All of the throttling components are suitable for a CAS deployment that satisfies the
[recommended HA architecture](../high_availability/High-Availability-Guide.html). In particular 
deployments with multiple CAS nodes behind a load balancer configured with session 
affinity can use either in-memory or _inspektr_ components. It is
instructive to discuss the rationale. Since load balancer session affinity is determined by source IP address, which
is the same criterion by which throttle policy is applied, an attacker from a fixed location should be bound to the
same CAS server node for successive authentication attempts. A distributed attack, on the other hand, where successive
request would be routed indeterminately, would cause haphazard tracking for in-memory CAS components since attempts
would be split across N systems. However, since the source varies, accurate accounting would be pointless since the
throttling components themselves assume a constant source IP for tracking purposes. The login throttling components
are not sufficient for detecting or preventing a distributed password brute force attack.
