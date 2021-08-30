---
layout: default
title: CAS - Configuring Ticket Expiration Policy Components
category: Ticketing
---

{% include variables.html %}

# Ticket Expiration Policies

CAS supports a pluggable and extensible policy framework to control the expiration policy of ticket-granting
tickets (`TGT`), proxy-granting tickets (`PGT`), service tickets (`ST`), proxy tickets (`PT`), etc.

<div class="alert alert-info"><strong>There Is More</strong><p>There are many other types of 
artifacts in CAS that take the base form of a ticket abstraction. Each protocol or feature may 
introduce a new ticket type that carries its own expiration policy and you will need to 
consult the documentation for that feature or behavior to realize how expiration 
policies for a specific ticket type may be tuned and controlled.</p></div>

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="ticketExpirationPolicies" casModule="cas-server-support-reports" %}

## Ticket-Granting Ticket Policies

`TGT` expiration policy governs the time span during which an authenticated user may grant `ST`s with a valid (non-expired) `TGT` without
having to re-authenticate. An attempt to grant an ST with an expired `TGT` would require the user to re-authenticate
to obtain a new (valid) `TGT`.

### Default

This is the default option, which provides a hard-time out as well as a sliding window.

{% include_cached casproperties.html properties="cas.ticket.tgt.core,cas.ticket.tgt.primary" %}

Ticket expiration policies are activated in the following conditions:

- If the timeout values for the default policy are all set to zero or less, CAS shall ensure tickets are *never* considered expired.
- Disabling a policy requires that all its timeout settings be set to a value equal or less than zero.
- If not ticket expiration policy is determined, CAS shall ensure the ticket are *always* considered expired.

<div class="alert alert-info"><strong>Keep What You Need!</strong><p>You are encouraged to only keep and maintain 
properties and settings needed for a particular policy. It is <strong>UNNECESSARY</strong> to grab a copy of all 
fields or keeping a copy as a reference while leaving them commented out. This strategy would ultimately lead to 
poor upgrades increasing chances of breaking changes and a messy deployment at that.</p></div>

Ticket expiration policies are activated in the following order:

1. Tickets are never expired, if and when settings for the default policy are configured accordingly.
2. Timeout
3. Default
4. Throttled Timeout
5. Hard Timeout
6. Tickets always expire immediately.

### Per Service

The expiration policy of ticket granting tickets can be conditionally decided on a per-application basis. The candidate service
whose ticket granting ticket expiration policy is to deviate from the default configuration must be designed as such:

```json
{
    "@class" : "org.apereo.cas.services.RegexRegisteredService",
    "serviceId" : "^https://.*",
    "name" : "Sample",
    "id" : 10,
    "ticketGrantingTicketExpirationPolicy": {
      "@class": "org.apereo.cas.services.DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy",
      "maxTimeToLiveInSeconds": 5
    }
}
```

Remember that applications are responsible to manage their own session. CAS will not and cannot manage the application session
and generally has no control over the application's timeout policies, logout practices, etc. The expiration policy
of the ticket-granting ticket per application allows to CAS to use that policy as an override and separate from the global defaults,
at the time the ticket is constructed and only if the incoming service request matches 
that given registered service definition. Once created, the policy remains global and affects all other applications and 
it has nothing to do with how the application manages its own sessions.

### Timeout

The expiration policy applied to TGTs provides for most-recently-used expiration policy, similar to a Web server session timeout.
For example, a 2-hour time span with this policy in effect would require a `TGT` to be used every 2 hours or less, otherwise
it would be marked as expired.

{% include_cached casproperties.html properties="cas.ticket.tgt.timeout" %}

### Hard Timeout

The hard timeout policy provides for finite ticket lifetime as measured from the time of creation. For example, a 4-hour time span
for this policy means that a ticket created at 1PM may be used up until 5PM; subsequent attempts to use it will mark it expired
and the user will be forced to re-authenticate.

{% include_cached casproperties.html properties="cas.ticket.tgt.hard-timeout" %}

### Throttled

The throttled timeout policy extends the TimeoutExpirationPolicy with the concept of throttling where a ticket may be used at
most every N seconds. This policy was designed to thwart denial of service conditions where a rogue or misconfigured client
attempts to consume CAS server resources by requesting high volumes of service tickets in a short time.

{% include_cached casproperties.html properties="cas.ticket.tgt.throttled-timeout" %}

### Never

The never expires policy allows tickets to exist indefinitely. 

<div class="alert alert-warning"><strong>Usage Warning!</strong><p>Use of this policy has significant consequences to overall
security policy and should be enabled only after a thorough review by a qualified security team. There are also implications to
server resource usage for the ticket registries backed by filesystem storage. Since disk storage for tickets can never be reclaimed
for those registries with this policy in effect, use of this policy with those ticket registry implementations
is strongly discouraged.</p></div>

## Service Ticket Policies

ST expiration policy governs the time span during which an authenticated user may attempt to validate an ST.

### Default

This is the default policy applied to service tickets where a ticket is expired after a fixed number of uses or after a maximum
period of inactivity elapses.

{% include_cached casproperties.html properties="cas.ticket.st" %}

### Per Service

The expiration policy of service tickets can be conditionally decided on a per-application basis. The candidate service
whose service ticket expiration policy is to deviate from the default configuration must be designed as such:

```json
{
    "@class" : "org.apereo.cas.services.RegexRegisteredService",
    "serviceId" : "^https://.*",
    "name" : "Sample",
    "id" : 10,
    "serviceTicketExpirationPolicy": {
      "@class": "org.apereo.cas.services.DefaultRegisteredServiceServiceTicketExpirationPolicy",
      "numberOfUses": 1,
      "timeToLive": "10"
    }
}
```

## Proxy Ticket Policies

`PT` expiration policy governs the time span during which an authenticated user may attempt to validate an `PT`.

### Default

This is the default policy applied to proxy tickets where a ticket is expired after a fixed number of uses or after a maximum
period of inactivity elapses. 

{% include_cached casproperties.html properties="cas.ticket.pt" %}

### Per Service

The expiration policy of proxy tickets can be conditionally decided on a per-application basis. The candidate service
whose proxy ticket expiration policy is to deviate from the default configuration must be designed as such:

```json
{
    "@class" : "org.apereo.cas.services.RegexRegisteredService",
    "serviceId" : "^https://.*",
    "name" : "Sample",
    "id" : 10,
    "proxyTicketExpirationPolicy": {
     "@class": "org.apereo.cas.services.DefaultRegisteredServiceProxyTicketExpirationPolicy",
     "numberOfUses": 1,
     "timeToLive": "30"
    } 
}
```

## Proxy-Granting Ticket Policies

`PGT` expiration policy governs the time span during which CAS may grant PTs with a valid (non-expired) `PGT`.

### Default

By default, the expiration policy assigned to proxy-granting 
tickets is controlled by the same policy assigned to ticket-granting tickets.

### Per Service

The expiration policy of proxy granting tickets can be conditionally decided on a per-application basis. The candidate service
whose proxy granting ticket expiration policy is to deviate from the default configuration must be designed as such:

```json
{
    "@class" : "org.apereo.cas.services.RegexRegisteredService",
    "serviceId" : "^https://.*",
    "name" : "Sample",
    "id" : 10,
    "proxyGrantingTicketExpirationPolicy": {
     "@class": "org.apereo.cas.services.DefaultRegisteredServiceProxyGrantingTicketExpirationPolicy",
     "maxTimeToLiveInSeconds": 30
    } 
}
```

{% include_cached casproperties.html properties="cas.ticket.pgt" %}


## Transient Session Ticket Policies

TST expiration policy governs the time span during which CAS can track a specific activity tied to a session.

{% include_cached casproperties.html properties="cas.ticket.tst" %}

