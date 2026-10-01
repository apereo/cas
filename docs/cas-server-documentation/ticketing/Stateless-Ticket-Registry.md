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

{% include_cached casproperties.html properties="cas.ticket.registry.stateless" %}

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
- Turn off ticket-granting cookie signing and keep its encryption, to keep the cookie within browser limits. 

## Ticket-granting Cookie Size

The ticket-granting cookie carries the entire stateless ticket-granting ticket, including the authenticated principal id,
the credentials, and the authentication attributes. Principal attributes are not kept in the ticket, as noted below.
The size of the cookie grows with the number and size of the authentication attributes and credentials, and multifactor authentication providers
such as Duo Security can add many of their own. Browsers only guarantee cookies of up to `4096` bytes and silently drop
larger ones, in which case every request asks the user to sign in again. CAS logs a warning when the cookie exceeds this size:

```bash
WARN <Cookie [TGC] is [4436] bytes, larger than the [4096] bytes browsers are guaranteed to accept...>
```

You may turn off cookie signing and keep cookie encryption, which makes the cookie about a quarter smaller:

{% include_cached casproperties.html properties="cas.tgc.crypto" %}

<div class="alert alert-warning">:warning: <strong>Signing Key</strong><p>Signing remains active as long as
the signing key is defined. Remove the signing key to turn signing off, and do not turn off cookie encryption.
</p></div>

## Caveats

The stateless ticket registry may not be a suitable solution for all deployment scenarios and its use and adoption does require a number of
compromises and security trade-offs. The following is a list of limitations and caveats that one should be aware of:

<div class="alert alert-info">:information_source: <strong>Life Advice</strong><p>
Depending on your point of view, any one of the caveats noted here could be argued as a minor lapse in security. Lessened security constraints 
around generated tickets or the inability to manage one's single sign-on session remotely, etc might be a deal breaker for you. Needless to say, 
you should examine and understand the security trade-offs carefully before you decide to use this option, or any option for that matter.
</p></div>

- Tickets are not single-use. A service ticket, proxy ticket or OAuth authorization code can be validated or exchanged again and again until it expires, unlike what the CAS protocol and OAuth2 specifications require, so keep their expiration short. Expiration policies ignore usage counts and *only* enforce an expiration instant; the ticket-granting ticket expires at the end of its maximum lifetime, and any idle timeout configured for it is not enforced.
- Issued tickets cannot be revoked. Logging out removes the ticket-granting cookie from the browser, but a copy of that cookie remains valid until the ticket-granting ticket expires. Likewise, revoking an OAuth access or refresh token has no effect before it expires.
- Generated tickets are generally controlled to be no larger than `256` characters. You *might* need to adjust your servlet container of choice to allow for larger form/response header sizes. Likewise, you must ensure your applications, particularly those that deal with CAS or OpenID Connect protocols are OK with somewhat larger and longer ticket and token sizes.
- Super long application URLs that might negatively influence the size of the generated service ticket are compressed using a pre-defined modest shortening technique, which in turn is taken into account by a specialized ticket validation strategy. For best results, and this is true for all CAS-supported protocols, it is recommended that applications use shorter URLs.
- To minimize the length of the generated tickets, tickets are only encrypted.
- The single sign-on session is tracked by the ticket-granting cookie as usual, which carries the stateless ticket-granting ticket itself. Browsers only guarantee cookies of up to `4096` bytes and silently drop larger ones, which ends the single sign-on session.
- **Important:** Principal attributes produced and collected during the first leg of the authentication transaction are not kept in any ticket. The ticket-granting ticket keeps the principal id, and CAS fetches all principal attributes from configured attribute repositories once more every time the ticket-granting ticket is read, such as when single sign-on sessions are established for applications, and again during back-channel ticket validation attempts. As a result, single sign-on decisions such as multifactor authentication triggers, access strategies and single sign-on participation policies that are based on principal attributes, as well as attribute release, only see attributes that the attribute repositories produce. In other words, if your attributes are only produced once during the authentication transaction by an authentication handler and family, such as claims from delegated authentication or multifactor authentication providers, you must also configure [an attribute repository](../integration/Attribute-Resolution.html) to fetch the attributes yet again. The ticket-granting ticket keeps its authentication attributes as they are; service tickets, proxy tickets and OAuth or OpenID Connect tokens only carry the authentication method, the successful authentication handlers, the credential types, remember-me, the delegated identity provider name (`clientName`) and the multifactor authentication context and trusted device attributes, kept as text. Other authentication attributes are not released during ticket validation. Principals that do not accept new attributes, such as those produced by surrogate authentication, keep the attributes they were created with.
- Every read of the ticket-granting ticket asks the configured attribute repositories for principal attributes. Results are cached for a period of time so most reads do not reach the repositories. If principal resolution fails with an error, the ticket-granting ticket is treated as missing and the user is asked to sign in again. Attribute repositories that do not respond may instead produce no attributes, depending on their configuration.
- Sessions kept in the ticket registry, such as replicated sessions for delegated authentication or OAuth and OpenID Connect, or HTTP sessions stored in the ticket registry, travel in their session cookie as stateless tickets. They expire at a fixed instant after they are created, and since the whole session is in the cookie, it must stay well under `4096` bytes.
- [Simple multifactor authentication](../mfa/Simple-Multifactor-Authentication.html) tokens are stored as stateless tickets while the user still receives and types the short code. The webflow keeps the stored token and checks the code against it, so a code is only accepted in the login flow that sent it. Tokens are not removed after use and stay valid until they expire. Tokens obtained from the REST endpoint are returned as stateless tickets, so the full ticket id, and not a short code, must be presented to validate them.
- In the absence of a central backend storage service, back-channel single logout operations are not supported. Likewise, all operations that ask for active single sign-on sessions or anything that in general deals with tracking single sign-on sessions is out of scope and unlikely to be supported. You will lose the ability to determine whether a user is logged in and as a result will be unable to administratively terminate a user's session.


