# Service Ticket Policies

Source: https://apereo.github.io/cas/development/ticketing/Configuring-Ticket-Expiration-Policy-ST.html

ST expiration policy governs the time span during which an authenticated user may attempt to validate an ST.

The default policy applied to service tickets is one where a ticket is expired after a fixed number of uses or after a maximum period of inactivity elapses.

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.st.max-length` (Optional; type `Integer`; default `20`): Maximum length of generated service tickets.
- `cas.ticket.st.number-of-uses` (Optional; type `Long`; default `1`): Controls number of times a service ticket can be used within CAS server. Usage in CAS context means service ticket validation transaction.
- `cas.ticket.st.proxy-granting-ticket-tracking-policy` (Optional; type `TicketTrackingPolicyTypes`; default `all`): Proxy granting ticket tracking policy for service tickets. Available values are as follows: - `MOST_RECENT`: Only track the most recent ticket. - `ALL`: Track all tickets.
- `cas.ticket.st.time-to-kill-in-seconds` (Optional; type `String`; default `PT10S`): Time in seconds that service tickets should be considered live in CAS server.

## Per Service

The expiration policy of service tickets can be conditionally decided on a per-application basis. The candidate service whose service ticket expiration policy is to deviate from the default configuration must be designed as such:

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
