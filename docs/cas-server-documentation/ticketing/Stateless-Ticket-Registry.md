---
layout: default
title: CAS - Stateless Ticket Registry
category: Ticketing
---

{% include variables.html %}

# Stateless Ticket Registry

The stateless ticket registry is a ticket registry that does not track or store tickets in a persistent manner
via a backend storage technology. All generated tickets are self-contained and are able to carry their own state
which in turns makes them portable across CAS nodes and deployments specially for clustered CAS deployments. Each ticket
is digitally encrypted to ensure its integrity and confidentiality. Furthermore, generated tickets are compressed as much
as possible and are constrained to pre-defined size to ensure backward compatibility with various CAS clients where possible.

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-stateless-ticket-registry" %}

<div class="alert alert-info"><strong>Under Construction</strong><p>
Greetings, intrepid explorer of cutting-edge features! You've stumbled upon our latest and greatest creation, but fair warning – 
it's still basking in the glow of the experimental stage. Everyone is counting on your courageous spirit to dive in, test it out, and share your thoughts.
Rest assured, it will get better over time. Pardon our digital dust, and enjoy the ride!
</p></div>

## Limitations & Caveats

The stateless ticket registry may not a suitable solution for all deployment scenarios and its use and adoption does require a number of
compromises and accepting of trade-offs. The following is a list of limitations and caveats that one should be aware of:

- [CAS Protocol](../protocol/CAS-Protocol.html), with the exception of CAS proxy authentication, is supported.
- [SAML Protocol](../protocol/SAML-Protocol.html) is supported.
- [SAM2 Protocol](../authentication/Configuring-SAML2-Authentication.html) is supported.
- Service ticket expiration policies are set to ignore re-usability or idle/inactivity limits, and are set to *only* enforce an expiration instant.
- Generated service tickets are generally controlled to be no larger than a non-configurable `256` characters.
- Super long application URLs that might negatively influence the size of the generated service ticket are compressed using a pre-defined modest shortening technique, which in turn is taken into account by a specialized service ticket validation strategy. For best results, it is recommended that applications use shorter `service` URLs.
- To minimize the length of the generated tickets, service tickets are not signed; only encrypted.
- The end user's browser session management features are heavily employed in all stateless ticket exchanges. The browser must be able to support i.e. local/session storage.
- All attributed produced and collected during the first leg of the authentication transaction will be lost and ignored during back-channel ticket validation attempts. Such attempts instruct CAS to fetch all attributes from configured attribute repositories once more. In other words, if your attributes are only produced once during the authentication transaction by an authentication handler and family, you must also configure an attribute repository to re-fetch the attributes during ticket validation.
- In the absence of a central backend storage service, back-channel single logout operations are not supported. Likewise, all operations that ask for active single sign-on sessions or anything that in general deals with tracking single sign-on sessions is out of scope and unlikely to be supported.
