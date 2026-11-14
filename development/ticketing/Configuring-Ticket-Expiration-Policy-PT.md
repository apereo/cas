# Proxy Ticket Policies

Source: https://apereo.github.io/cas/development/ticketing/Configuring-Ticket-Expiration-Policy-PT.html

`PT` expiration policy governs the time span during which an authenticated user may attempt to validate an `PT`.

The default policy applied to proxy tickets is one where a ticket is expired after a fixed number of uses or after a maximum period of inactivity elapses.

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.pt.number-of-uses` (Optional; type `Long`; default `1`): Number of uses allowed.
- `cas.ticket.pt.time-to-kill-in-seconds` (Optional; type `Long`; default `10`): Number of seconds after which this ticket becomes invalid.

## Per Service

The expiration policy of proxy tickets can be conditionally decided on a per-application basis. The candidate service whose proxy ticket expiration policy is to deviate from the default configuration must be designed as such:

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
