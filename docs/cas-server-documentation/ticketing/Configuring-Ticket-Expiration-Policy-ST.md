---
layout: default
title: CAS - Configuring Ticket Expiration Policy Components
category: Ticketing
---

{% include variables.html %}

# Service Ticket Policies

ST expiration policy governs the time span during which an authenticated user may attempt to validate an ST.

The default policy applied to service tickets is one where a ticket is expired after a fixed number of uses or after a maximum
period of inactivity elapses.

{% include_cached casproperties.html properties="cas.ticket.st" %}

## Per Service

The expiration policy of service tickets can be conditionally decided on a per-application basis. The candidate service
whose service ticket expiration policy is to deviate from the default configuration must be designed as such:

```json
{
    "@class" : "org.apereo.cas.services.CasRegisteredService",
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
