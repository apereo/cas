# Transient Session Ticket Policies

Source: https://apereo.github.io/cas/development/ticketing/Configuring-Ticket-Expiration-Policy-TST.html

`TST` expiration policy governs the time span during which CAS can track a specific activity tied to a session.

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.tst.number-of-uses` (Optional; type `Long`; default `1`): Controls number of times a ticket can be used within CAS server.
- `cas.ticket.tst.time-to-kill-in-seconds` (Optional)
