---
layout: default
title: CAS - Configuring Authentication Throttling
category: Authentication
---
{% include variables.html %}

# Throttling Authentication Attempts

## Capacity Throttling

CAS is able to support request rate-limiting based on the token-bucket algorithm. This
means that authentication requests that reach a certain configurable capacity within 
a time window may either be blocked or _throttled_ to slow down. This is done to 
protect the system from overloading, allowing you to introduce a scenario to allow 
CAS 120 authentication requests per minute with a refill rate of 10 requests per 
second that would continually increase in the capacity bucket.

Enable the following module in your configuration overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-throttle-bucket4j" %}

{% include casproperties.html
modules="cas-server-support-throttle-bucket4j" %}

## Failure Throttling

CAS provides a facility for limiting failed login attempts to support password guessing and related abuse scenarios.
A couple strategies are provided for tracking failed attempts:

1. Source IP - Limit successive failed logins against any username from the same IP address.
2. Source IP and username - Limit successive failed logins against a particular user from the same IP address.

All login throttling components that ship with CAS limit successive failed login attempts that exceed a threshold
rate in failures per second. The following properties are provided to define the failure rate.

* `failureRangeInSeconds` - Period of time in seconds during which the threshold applies.
* `failureThreshold` - Number of failed login attempts permitted in the above period.

A failure rate of more than 1 per 3 seconds is indicative of an automated authentication attempt, which is a
reasonable basis for throttling policy. Regardless of policy care should be taken to weigh security against access;
overly restrictive policies may prevent legitimate authentication attempts.

Enable the following module in your configuration overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-throttle" %}

### Administrative Endpoints

The following endpoints are provided by CAS:
 
| Endpoint                     | Description
|------------------------------|---------------------------------------------
| `throttles`                  | `GET` request to fetch throttled records.

### IP Address

Uses a memory map to prevent successive failed login attempts from the same IP address.

### IP Address and Username

Uses a memory map to prevent successive failed login attempts for
a particular username from the same IP address.

### JDBC

Please [see this guide](Configuring-Authentication-Throttling-JDBC.html) for more info.

### MongoDb

Please [see this guide](Configuring-Authentication-Throttling-MongoDb.html) for more info.

### Redis

Please [see this guide](Configuring-Authentication-Throttling-Redis.html) for more info.

### Hazelcast

Please [see this guide](Configuring-Authentication-Throttling-Hazelcast.html) for more info.

### CouchDb

Please [see this guide](Configuring-Authentication-Throttling-CouchDb.html) for more info.

## Configuration

{% include casproperties.html properties="cas.authn.throttle" %}

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
