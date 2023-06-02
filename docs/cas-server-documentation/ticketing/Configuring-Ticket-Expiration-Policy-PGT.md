---
layout: default
title: CAS - Configuring Ticket Expiration Policy Components
category: Ticketing
---

{% include variables.html %}

# Proxy-Granting Ticket Policies

`PGT` expiration policy governs the time span during which CAS may grant `PT`s with a valid (non-expired) `PGT`.

{% include_cached casproperties.html properties="cas.ticket.pgt" %}

## Per Service

The expiration policy of proxy granting tickets can be conditionally decided on a per-application basis. The candidate service
whose proxy granting ticket expiration policy is to deviate from the default configuration must be designed as such:

```json
{
    "@class" : "org.apereo.cas.services.CasRegisteredService",
    "serviceId" : "^https://.*",
    "name" : "Sample",
    "id" : 10,
    "proxyGrantingTicketExpirationPolicy": {
     "@class": "org.apereo.cas.services.DefaultRegisteredServiceProxyGrantingTicketExpirationPolicy",
     "maxTimeToLiveInSeconds": 30
    } 
}
```

