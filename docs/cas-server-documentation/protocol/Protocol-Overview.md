---
layout: default
title: CAS - Protocol Overview
description: "The protocols CAS supports: CAS, SAML 1.1 and 2.0, OAuth 2.0, OpenID Connect, WS-Federation and REST."
category: Protocols
---

{% include variables.html %}

# Protocols Overview

The following protocols are supported and provided by CAS:

*   [CAS](CAS-Protocol.html)
*   [OAuth](OAuth-Protocol.html)
*   [OpenID Connect](OIDC-Protocol.html)
*   [WS Federation](WS-Federation-Protocol.html)
*   [SAML1](SAML-v1-Protocol.html)
*   [SAML2](../authentication/Configuring-SAML2-Authentication.html)
*   [REST Protocol](REST-Protocol.html)

## Choosing a Protocol

Most deployments support several protocols at once, chosen per application by what the application or its
client library speaks. When you have a choice, the table below can help.

| Protocol                                                         | Use it for                                                                                             | What the application receives                                             | Logout                                                                     |
|------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------|----------------------------------------------------------------------------|
| [CAS](CAS-Protocol.html)                                         | Browser login for web applications with a CAS client, including proxying to back-end services          | A service ticket, validated by the application for user id and attributes | [Back or front channel](../installation/Logout-Single-Signout.html)        |
| [SAML2](../authentication/Configuring-SAML2-Authentication.html) | Federation with SaaS and enterprise applications, and with partners that exchange SAML2 metadata       | A signed XML assertion with attributes                                    | [SAML2 single logout](../installation/Configuring-SAML2-Logout.html)       |
| [OpenID Connect](OIDC-Protocol.html)                             | New web, mobile and single-page applications that need login and user claims                           | An ID token (JWT), plus access and refresh tokens                         | [OpenID Connect logout](../authentication/OIDC-Authentication-Logout.html) |
| [OAuth 2.0](OAuth-Protocol.html)                                 | Authorizing access to APIs; use OpenID Connect when the application also needs to know who the user is | Access and refresh tokens                                                 | Not part of the protocol                                                   |
| [WS-Federation](WS-Federation-Protocol.html)                     | Older Microsoft-based applications that only speak WS-Federation                                       | A SAML security token                                                     | `wsignout1.0` requests                                                     |
| [SAML 1.1](SAML-v1-Protocol.html)                                | Legacy applications that cannot move to SAML2 or CAS                                                   | A SAML 1.1 validation response                                            | [Single logout](../installation/Logout-Single-Signout.html)                |
| [REST](REST-Protocol.html)                                       | Trusted programs that obtain tickets without a browser                                                 | Ticket-granting and service tickets                                       | [Delete the ticket-granting ticket](REST-Protocol-Logout.html)             |

For a new application with no constraints, OpenID Connect is usually the best fit. Prefer CAS when the application
already ships a CAS client, and SAML2 when the application is a SaaS product that offers SAML2 single sign-on.

## Design

CAS presents itself as a multilingual platform supporting protocols such as CAS, SAML2, OAuth2 and OpenID Connect, etc. Support and functionality for each of these protocols continually improves per every iteration and release of the software thanks to excellent community feedback and adoption. While almost all such protocols are similar in nature and intention, they all have their own specific bindings, parameters, payload and security requirements. This section provides a quick introduction on how existing protocols are supported in CAS.

It all starts with something rather trivial: The Bridge.

### The Bridge

The bridge *design pattern* is an approach where an intermediary sits between the client and the server, translating requests back and forth. It acts as a link between the two sides allowing authentication requests from the client to be translated, massaged and transformed and then routed "invisibly" to CAS and then back.

This is a neat trick because the client does not care how the authentication request is processes once it's submitted. The *thing* that receives that request, acting as a bridge can do anything required to process that request and ultimately submitting some sort of response back to the client. The bridge also does not care what external authentication system handles and honors that request and how all that processing internally works. All the bridge cares about is, "I routed the request to X. As long as X gives me back the right stuff, I should be fine to resume".

So the bridge for the most part is the "control tower" of the operation. It speaks many languages and protocols, and just like any decent translator, it knows about the quirks and specifics of each language and as such is able to dynamically translate the technical lingo.

### Supported Protocols

If you understand the above strategy, then you would be glad to learn that *almost* all protocols supported by CAS operate with the same exact intentions. A given CAS deployment is equipped with embedded plugins/bridges/modules that know how to speak SAML2 and CAS, OAuth2 and CAS, or OpenID Connect and CAS or whatever. The right-hand side of that equation is always CAS when you consider, as an example, the following authentication flow with an OAuth2-enabled client application:

1. The CAS deployment has turned on the OAuth2 plugin.
2. An OAuth2 authorization request is submitted to the relevant CAS endpoint.
3. The OAuth2 plugin verifies the request and translates it to a CAS authentication request!
4. The authentication request is routed to the relevant CAS login endpoint.
5. User authenticates and CAS routes the flow back to the OAuth2 plugin, having issued a service ticket for the plugin.
6. The OAuth2 plugin attempts to validate that ticket to retrieve the necessary user profile and attributes.
7. The OAuth2 plugin then proceeds to issue the right OAuth2 response by translating and transforming the profile and validated assertions into what the client application may need.

<div class="alert alert-info">:information_source: <strong>Note</strong><p>The above strategy applies exactly the same, if CAS decides to delegate the authentication to an external identity provider such as Facebook or a SAML2 identity provider.</p></div>

The right-hand side of the flow is always CAS, because the plugin always translates protocol requests into CAS requests. Another way of looking at it is that all protocol plugins and modules are themselves clients of the CAS server! They are issued service tickets and they proceed to validate them just like any other CAS-enabled client. Just like above, to the OAuth2-enabled client all such details are totally transparent and as long as “the right stuff” is produced back to the client, it shall not care.

There are some internal technical and architectural advantages to this approach. Namely:

The core of the CAS authentication engine, flow and components need not be modified at all. After all, we are just integrating yet another client even if it’s embedded directly in CAS itself. Because of that, support for that protocol can be very easily removed, if needed. After all, protocols come and go every day. Finally and just like any other CAS client, all features of the CAS server are readily available and translated to the relevant client removing the need to duplicate and re-create protocol-specific configuration as much as possible. Things like access strategies, attribute release, username providers, etc.
