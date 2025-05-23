---
layout: default
title: CAS - Stateless Ticket Registry
category: Ticketing
---

{% include variables.html %}

# Stateless Ticket Registry

The stateless ticket registry is a ticket registry that does not track or store tickets in a persistent manner
via a backend storage technology. All generated tickets are self-contained and are able to carry their own state
which in turn makes them portable across CAS nodes and clustered deployments. Each ticket
is digitally encrypted to ensure its integrity and confidentiality. Furthermore, generated tickets are compressed as much
as possible and are constrained to a pre-defined size to ensure backward compatibility with various CAS clients where possible.

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-stateless-ticket-registry" %}

## Features

- No centralized backend storage or caching technology is required to be present, configured, installed, managed, maintained, tuned, etc.
- ...as a result, you do not need to worry about storage schema upgrades, migrations, etc.
- ...as a result, you do not need to worry about cleaning up expired tickets or garbage-collecting ticket entities.
- ...as a result, you do not need to worry about sharing tickets across CAS nodes in a clustered deployment and synchronizing state.
- ...as a result, you do not need to pay for storage or possible caching technology licenses especially if your CAS deployment is cloud-native.
        
The above features do come with a number of caveats and limitations. See below.

## Supported Protocols

- [CAS Protocol](../protocol/CAS-Protocol.html) is supported.
- [SAML1 Protocol](../protocol/SAML-v1-Protocol.html) is supported.
- [SAML2 Protocol](../authentication/Configuring-SAML2-Authentication.html) is supported with the following exceptions:
  - [SAML2 attribute queries](../installation/Configuring-SAML2-AttributeQuery.html)
- [OAuth2 Protocol](../authentication/OAuth-Authentication.html) is supported with the following exceptions:
  - [Device Authorization](../authentication/OAuth-ProtocolFlow-DeviceAuthorization.html)
  - [Token Exchange](../authentication/OAuth-ProtocolFlow-TokenExchange.html)
- [OpenID Connect Protocol](../protocol/OIDC-Protocol.html) is supported with the following exceptions: 
  - [DPoP](../authentication/OIDC-Authentication-DPoP.html)

<div class="alert alert-info">:information_source: <strong>What About...?</strong><p>
Remember that not all CAS modules and features that interact with the ticket registry to create, update, fetch or remove tickets are supported.
The objective is to start with a small batch of most common features and capabilities and iteratively grow and improve. If you do find something that 
might be missing or acts dysfunctional, please investigate, isolate, verify and consider contributing a fix.
</p></div>

## Suggestions

- Increase the expiration policy of service tickets to be around `30` seconds to allow for decryption operations to decode tickets in time.
- Assign names to all authentication handlers, and preferably short, concise names.
- Use shorter URLs for applications, especially those that use the CAS protocol. This will help minimize the size of the generated service tickets.

## Caveats

The stateless ticket registry may not be a suitable solution for all deployment scenarios and its use and adoption does require a number of
compromises and security trade-offs. The following is a list of limitations and caveats that one should be aware of:

<div class="alert alert-info">:information_source: <strong>Life Advice</strong><p>
Depending on your point of view, any one of the caveats noted here could be argued as a minor lapse in security. Lessened security constraints 
around generated tickets or the inability to manage one's single sign-on session remotely, etc might be a deal breaker for you. Needless to say, 
you should examine and understand the security trade-offs carefully before you decide to use this option, or any option for that matter.
</p></div>

- The expiration policies for all generated tickets are set to ignore re-usability or idle/inactivity limits, and are set to *only* enforce an expiration instant.
- Generated tickets are generally controlled to be no larger than `256` characters. You *might* need to adjust your servlet container of choice to allow for larger form/response header sizes. Likewise, you must ensure your applications, particularly those that deal with CAS or OpenID Connect protocols are OK with somewhat larger and longer ticket and token sizes.
- Super long application URLs that might negatively influence the size of the generated service ticket are compressed using a pre-defined modest shortening technique, which in turn is taken into account by a specialized ticket validation strategy. For best results, and this is true for all CAS-supported protocols, it is recommended that applications use shorter URLs.
- To minimize the length of the generated tickets, tickets are only encrypted.
- The end user's browser session management features are heavily employed in all stateless ticket exchanges. The browser must be able to support i.e. local/session storage.
- **Important:** All attributes produced and collected during the first leg of the authentication transaction will be lost and ignored during back-channel ticket validation attempts. Such attempts instruct CAS to fetch all attributes from configured attribute repositories once more. In other words, if your attributes are only produced once during the authentication transaction by an authentication handler and family, you must also configure [an attribute repository](../integration/Attribute-Resolution.html) to fetch the attributes yet again during ticket validation operations.
- In the absence of a central backend storage service, back-channel single logout operations are not supported. Likewise, all operations that ask for active single sign-on sessions or anything that in general deals with tracking single sign-on sessions is out of scope and unlikely to be supported. You will lose the ability to determine whether a user is logged in and as a result will be unable to administratively terminate a user's session.


