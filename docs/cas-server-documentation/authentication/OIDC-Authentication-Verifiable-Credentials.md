---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# Verifiable Credentials - OpenID Connect Authentication

OpenID Connect Authentication can be used in conjunction with Verifiable Credentials to 
provide a secure and decentralized way of verifying user identities. Verifiable Credentials 
are digital credentials that can be issued by trusted authorities and can be presented by users 
to prove their identity or attributes.
                         
The following capabilities are in place:

- Issuer metadata is published via the `.well-known/openid-credential-issuer` endpoint.
- CAS may issue credentials formats such as `SD-JWT VC` (selective disclosure), etc.
- Dedicated endpoints for credential issuance and nonce generation are available.

## Overview

Support is enabled by including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-oidc-vc" %}

{% include_cached casproperties.html properties="cas.authn.oidc.vc" %}
   
## Flow

The flow starts with CAS publishing issuer metadata at `/.well-known/openid-credential-issuer`. This 
document advertises the issuer identifier, the credential endpoint, supported credential 
configurations, and, when implemented, the nonce endpoint. Wallets use this metadata to 
discover what CAS can issue and how to interact with it.

Before requesting a credential, the wallet obtains authorization using OAuth 2.0 
mechanisms associated with the credential issuer or its authorization server. OIDC4VCI 
is explicitly built on OAuth 2.0, and the wallet uses OAuth authorization to gain the 
right to receive one or more credentials from the issuer.

Once the wallet has an access token, CAS uses its existing access token and 
principal resolution machinery to identify the authenticated subject. 
At this point, CAS has the `Principal` and its attributes, which become the source 
data for the credential claims that may be issued. 

CAS then selects the requested `credential_configuration_id` from issuer 
metadata and maps principal attributes into credential claims according to 
that configuration. In practice, this is typically configuration-driven, so 
each claim definition identifies its source principal attribute, whether it 
is mandatory, and how it should be typed or normalized before issuance.

When credential binding is required, the wallet sends a proof with 
the credential request to demonstrate possession of the key material 
to which the credential should be bound. OIDC4VCI supports proof-based 
issuance so that the issued credential can be tied to wallet-controlled 
cryptographic material rather than being just a bearer artifact.

The wallet calls the credential endpoint with its access token, 
the requested credential configuration, and, if required, the proof. 
CAS validates the access token, validates the proof, checks the requested 
credential configuration, resolves claims from the principal, and prepares 
the credential payload for issuance.

After claims are prepared, CAS signs the credential using its issuer key 
material. For JWT-based credential formats, this can reuse the same underlying 
JWS signing infrastructure CAS already uses for ID tokens, although the credential 
payload is distinct from an ID token and follows verifiable credential format rules instead.

## Nonce Proof

Proofs are expected to carry a `nonce` claims. The nonce lets CAS tell whether the wallet’s 
proof is fresh, instead of a replay. In OIDC4VCI, the wallet sends a proof showing it controls 
the key the credential should be bound to, and the spec says `c_nonce` is the main defense 
against replay of that proof.

Without a nonce, an attacker who somehow gets hold of a previously 
valid proof could try to send it again and get a duplicate credential issued 
to the same key. 

In practical terms, the flow is:

- Wallet asks CAS for a fresh c_nonce from the nonce endpoint.
- Wallet builds its proof and includes that nonce.
- CAS checks that the nonce matches one it issued, is still fresh, and has not already been used.
- CAS consumes it so the same proof cannot be replayed.
