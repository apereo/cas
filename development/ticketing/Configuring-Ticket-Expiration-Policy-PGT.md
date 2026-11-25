# Proxy-Granting Ticket Policies

Source: https://apereo.github.io/cas/development/ticketing/Configuring-Ticket-Expiration-Policy-PGT.html

`PGT` expiration policy governs the time span during which CAS may grant `PT`s with a valid (non-expired) `PGT`.

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.pgt.max-length` (Optional; type `Long`; default `50`): Maximum length of the proxy granting ticket, when generating one.

## Per Service

The expiration policy of proxy granting tickets can be conditionally decided on a per-application basis. The candidate service whose proxy granting ticket expiration policy is to deviate from the default configuration must be designed as such:

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
