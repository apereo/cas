---
layout: default
title: CAS - Configuring Ticket Expiration Policy Components
category: Ticketing
---

{% include variables.html %}

# Ticket-Granting Ticket Policies

`TGT` expiration policy governs the time span during which an authenticated user may grant `ST`s with a valid (non-expired) `TGT` without
having to re-authenticate. An attempt to grant an ST with an expired `TGT` would require the user to re-authenticate
to obtain a new (valid) `TGT`.

Ticket expiration policies are activated in the following conditions:

- If the timeout values for the default policy are all set to zero or less, CAS shall ensure tickets are *never* considered expired.
- Disabling a policy requires that all its timeout settings be set to a value equal or less than zero.
- If not ticket expiration policy is determined, CAS shall ensure the ticket are *always* considered expired.

<div class="alert alert-info">:information_source: <strong>Keep What You Need!</strong><p>You are encouraged to only keep and maintain 
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

{% tabs exppolicies %}

{% tab exppolicies Default %}

This policy provides a hard-time out as well as a sliding window.

{% include_cached casproperties.html properties="cas.ticket.tgt.core,cas.ticket.tgt.primary" %}

{% endtab %}

{% tab exppolicies <i class="fa fa-person px-1"></i>Per Principal %}

This ticket expiration policy is conditionally activated if and when the authenticated principal contains a `authenticationSessionTimeout` attribute.
This attribute is expected to be a single-valued attribute whose value, measured in seconds, would allow CAS to build a hard timeout
expiration policy and associate it with the ticket-granting ticket. The value may be specified a numeric value or in the `Duration` syntax, i.e. `PT2H`.

{% endtab %}

{% tab exppolicies Per Service %}

The expiration policy of ticket granting tickets can be conditionally decided on a per-application basis. The candidate service
whose ticket granting ticket expiration policy is to deviate from the default configuration must be designed as such:

```json
{
    "@class" : "org.apereo.cas.services.CasRegisteredService",
    "serviceId" : "^https://.*",
    "name" : "Sample",
    "id" : 10,
    "ticketGrantingTicketExpirationPolicy": {
      "@class": "org.apereo.cas.services.DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy",
      "maxTimeToLiveInSeconds": 5,
      "userAgents": {
        "@class": "java.util.LinkedHashMap",
        ".+Firefox.+": 30
      },
      "ipAddresses": {
        "@class": "java.util.LinkedHashMap",
        ".+123.456.+": 60
      }
    }
}
```

Remember that applications are responsible to manage their own session. CAS will not and cannot manage the application session
and generally has no control over the application's timeout policies, logout practices, etc. The expiration policy
of the ticket-granting ticket per application allows CAS to use that policy as an override and separate from the global defaults,
at the time the ticket is constructed and only if the incoming service request matches
that given registered service definition. Once created, the policy remains global and affects all other applications and
it has nothing to do with how the application manages its own sessions.

{% endtab %}

{% tab exppolicies Timeout <i class="fa fa-hourglass px-1"></i> %}

The expiration policy applied to TGTs provides for most-recently-used expiration policy, similar to a Web server session timeout.
For example, a 2-hour time span with this policy in effect would require a `TGT` to be used every 2 hours or less, otherwise
it would be marked as expired.

{% include_cached casproperties.html properties="cas.ticket.tgt.timeout" %}

{% endtab %}

{% tab exppolicies Hard Timeout <i class="fa fa-clock px-1"></i> %}

The hard timeout policy provides for finite ticket lifetime as measured from the time of creation. For example, a 4-hour time span
for this policy means that a ticket created at 1PM may be used up until 5PM; subsequent attempts to use it will mark it expired
and the user will be forced to re-authenticate.

{% include_cached casproperties.html properties="cas.ticket.tgt.hard-timeout" %}

{% endtab %}

{% tab exppolicies <i class="fa fa-lock px-1"></i> Throttled %}

The throttled timeout policy extends the TimeoutExpirationPolicy with the concept of throttling where a ticket may be used at
most every `N` seconds. This policy was designed to thwart denial of service conditions where a rogue or misconfigured client
attempts to consume CAS server resources by requesting high volumes of service tickets in a short time.

{% include_cached casproperties.html properties="cas.ticket.tgt.throttled-timeout" %}

{% endtab %}

{% tab exppolicies Never <i class="fa fa-ban px-1"></i> %}

The never expires policy allows tickets to exist indefinitely. This policy is activated when the primary ticket expiration
policy is configured with negative timeout values.

<div class="alert alert-warning">:warning: <strong>Usage Warning!</strong><p>Use of this policy has significant consequences to overall
security policy and should be enabled only after a thorough review by a qualified security team. There are also implications to
server resource usage for the ticket registries backed by filesystem storage. Since disk storage for tickets can never be reclaimed
for those registries with this policy in effect, use of this policy with those ticket registry implementations
is strongly discouraged.</p></div>

{% endtab %}

{% endtabs %}

## How Do I Choose?

Changing the `TGT` policy is a matter of organizational policy, and typically one that would control the length of the 
overall SSO session for the best user experience. Unless you have good reason to do so or a business use case dictates a scenario, 
the defaults generally are appropriate. Overall, you want to try to keep the length of the SSO session to a minimum to 
avoid security issues, but not too small to cause user annoyance with repeated logins.
