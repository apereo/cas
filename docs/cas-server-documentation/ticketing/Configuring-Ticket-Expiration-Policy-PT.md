---
layout: default
title: CAS - Configuring Ticket Expiration Policy Components
category: Ticketing
---

{% include variables.html %}

# Proxy Ticket Policies

`PT` expiration policy governs the time span during which an authenticated user may attempt to validate an `PT`.

The default policy applied to proxy tickets is one where a ticket is expired after a 
fixed number of uses or after a maximum period of inactivity elapses. 

{% include_cached casproperties.html properties="cas.ticket.pt" %}

## Per Service

The expiration policy of proxy tickets can be conditionally decided on a per-application basis. The candidate service
whose proxy ticket expiration policy is to deviate from the default configuration must be designed as such:

```json
{
    "@class" : "org.apereo.cas.services.CasRegisteredService",
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
