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

In this role, CAS acts as an **OpenID Credential Issuer** and extends its existing OpenID Connect
capabilities with support for credential issuance, credential metadata publication, proof validation,
nonce generation, and wallet-facing issuance flows.
                
The following capabilities are in place:

- Issuer metadata is published via the `.well-known/openid-credential-issuer` endpoint.
- CAS may issue credentials formats such as `SD-JWT VC` (selective disclosure), etc.
- Dedicated endpoints for credential issuance and nonce generation are available.
- Credential offers may be produced and shared with wallets.
- Pre-authorized code flows may be used to obtain issuance-scoped access tokens.
- Access tokens issued for verifiable credential flows may carry authorization context for one or more credential configurations.
- Proofs may be validated to ensure possession of holder key material and to prevent replay.
 
Supported formats are:

- `dc+sd-jwt`
- `jwt_vc_json`
- `jwt_vc_json-ld`

## Overview

Support is enabled by including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-oidc-vc" %}

{% include_cached casproperties.html properties="cas.authn.oidc.vc" %}

## Endpoints

The following endpoints are typically involved in the verifiable credential issuance flow.

### Issuer Metadata

Publishes issuer capabilities and supported credential configuration metadata.

```bash
GET /oidc/.well-known/openid-credential-issuer
```

This endpoint generally advertises:

- The credential issuer identifier.
- The credential endpoint.
- The nonce endpoint, when supported.
- Supported credential configurations.
- Supported formats and signing algorithms.
- 
#### Metadata Location

OpenID4VCI locates the credential issuer metadata by inserting `/.well-known/openid-credential-issuer`
into the issuer identifier *between the host and the path*, rather than by appending it the way
OpenID Connect Discovery does; [RFC 8414](https://www.rfc-editor.org/rfc/rfc8414) locates the
authorization server metadata the same way. A wallet issued against
`https://sso.example.org/cas/oidc` therefore asks for:

```bash
GET https://sso.example.org/.well-known/openid-credential-issuer/cas/oidc
GET https://sso.example.org/.well-known/oauth-authorization-server/cas/oidc
```

CAS is normally deployed under the `/cas` context path, so neither request reaches the
application at all and the servlet container answers with its own `404`. Route them onto the
paths CAS serves, either in the proxy that fronts CAS or with the embedded Tomcat rewrite valve.
The valve must be registered on the engine, which runs before a context is selected.

The rewrite rule would be similar to:

```
RewriteRule ^/\.well-known/(openid-credential-issuer|oauth-authorization-server|openid-configuration)(/.+)$ $2/.well-known/$1 [L]
```

Naming the documents explicitly, rather than matching every well-known path, leaves unrelated
ones such as `/.well-known/acme-challenge/<token>` untouched.

A wallet that cannot resolve this metadata may not begin issuance at all.

#### Token Endpoint Authentication

The pre-authorized code grant carries no client credentials. CAS authenticates the exchange from
the `pre-authorized_code` and `grant_type` request parameters themselves, and the client bound to
the credential offer is recorded on the pre-authorization code when the offer is created.

A wallet decides how to authenticate from the authorization server metadata, so the token endpoint
has to advertise that it accepts requests with no client authentication. CAS includes `none` in
authentication methods supported for the token endpoint by default for this reason. If the
list is narrowed, keep `none` in it; without it a wallet picks one of the credentialed methods it
sees instead -- typically `private_key_jwt` -- and the exchange is rejected, because the wallet has
no client registration to authenticate with.

### Credential Endpoint

Issues a verifiable credential to the wallet once the access token, proof, and requested
credential configuration have been validated.

```bash
POST /oidc/oidcVcCredential
```

This endpoint expects:

- An access token, presented as `Authorization: Bearer ...` or, when the token response named the
  token type `DPoP`, as `Authorization: DPoP ...` per [RFC 9449](https://www.rfc-editor.org/rfc/rfc9449).
- The requested credential, named either by `credential_configuration_id` or, when the token
  response returned `credential_identifiers` in its authorization details, by
  `credential_identifier`. The two are mutually exclusive.
- A `proofs` object holding one or more proof JWTs, each carrying a `nonce` claim.

The endpoint body is expected as:

```json
{
  "credential_configuration_id": "myorg",
  "proofs": {
    "jwt": ["eyJ0eXAiOiJvcGVuaWQ0dmNpL..."]
  }
}
```

There is no separate batch credential endpoint. A batch is a single credential request carrying
several proofs, and the response holds one credential per proof, all of the same credential
configuration. How many proofs are accepted is advertised as `batch_credential_issuance` in the
issuer metadata and controlled by `cas.authn.oidc.vc.issuer.batch-size`.

The response is:

```json
{
  "credentials": [
    {"credential": "eyJhbGciOiJSUzI1NiIs..."}
  ]
}
```

### Nonce Endpoint

Produces a fresh c_nonce that may be used by the wallet in a later proof for the
credential request.
    
```bash
POST /oidc/oidcVcNonce
```

This endpoint returns `c_nonce`. The challenge is never returned from the token endpoint.

### Credential Offer Endpoint

Exposes a prepared credential offer for a previously-created issuance transaction.
                       
```bash
GET /oidc/oidcVcCredentialOffer/{transactionId}
```

This endpoint does not establish subject identity on its own. Instead, it
dereferences a short-lived server-side issuance transaction and returns the corresponding
credential offer document.

### Trusted Transaction Creation Endpoint

Creates a server-side issuance transaction for a known subject and returns an opaque
transaction identifier and a wallet-facing offer URI.
           
```bash
POST /oidc/oidcVcCredentialOfferTransactions
```
   
The endpoint body is expected to be:

```json
{
  "principal": "...",
  "credentialConfigurationIds": ["..."]
}
```
This endpoint is intended for trusted callers such as:

- Administrative tools
- Internal backend services & APIs
- Authenticated CAS user interfaces

This endpoint is protected and should not be exposed as an anonymous wallet-facing API.

### Token Endpoint

Exchanges an authorization artifact, such as a pre-authorized code, for an access token
that may later be used at the credential endpoint.
   
```bash
POST /oidc/token
```

When used for verifiable credential issuance, this endpoint may:

- Accept the `pre-authorized_code` grant.
- Require a `tx_code`.
- Return a `c_nonce`.
- Produce an access token that is scoped to credential issuance.

Example request:

```bash
POST /oidc/token
Content-Type: application/x-www-form-urlencoded

grant_type=urn:ietf:params:oauth:grant-type:pre-authorized_code&
pre-authorized_code=L0Qw0sT6dP5P7l7xM0H2AqQ0g9vM2j5fByuYwQ&
tx_code=TST-1234
```

## Nonce Proof

Proofs are expected to carry a `nonce` claim. The nonce lets CAS tell whether the wallet’s
proof is fresh, instead of a replay. In OIDC4VCI, the wallet sends a proof showing it controls
the key the credential should be bound to, and the c_nonce is the primary defense
against replay of that proof.

Without a nonce, an attacker who somehow gets hold of a previously
valid proof could try to send it again and get a duplicate credential issued
to the same key.

In practical terms, the flow is:

- The wallet asks CAS for a fresh c_nonce from the nonce endpoint, or receives one from the token endpoint.
- The wallet builds its proof and includes that nonce.
- CAS checks that the nonce matches one it issued, is still fresh, and has not already been used.
- CAS consumes it so the same proof cannot be replayed.

The token endpoint issues the nonce. The credential endpoint enforces it while validating the proof.

## Authorization Code Flow

The Authorization Code Flow allows a wallet to obtain authorization to receive one or more verifiable credentials 
through a standard OAuth 2.0/OpenID Connect authorization process. Unlike the Pre-Authorized Code Flow, where a 
trusted backend initiates the issuance transaction, the Authorization Code Flow is wallet-driven. 
The wallet begins by sending an authorization request to the Credential Issuer’s authorization endpoint, 
requesting one or more credential configurations using the `authorization_details` parameter. The request may 
also include the `openid` scope if the wallet wishes to authenticate the end-user and receive an ID Token 
as part of the token response. The wallet exchanges the authorization code at the token endpoint to obtain 
an access token that is specifically authorized for credential issuance. 

## Pre-Authorized Code Flow

In pre-authorized code flows, CAS or a trusted backend prepares the issuance transaction
before the wallet starts the OAuth exchange.

The general flow is:

- A trusted caller creates an issuance transaction.
- CAS stores the transaction and issues a pre-authorized code.
- CAS returns a wallet-facing `credential_offer_uri`.
- The wallet resolves the offer.
- The wallet exchanges the pre-authorized code at the token endpoint.
- CAS returns an access token, and optionally a c_nonce.
- The wallet calls the credential endpoint with the access token and proof.
- CAS validates the request and issues the credential.

## Verifiable Presentations

CAS can also act as a verifier and ask a wallet to present a credential. A relying party creates a
presentation request, and CAS returns a deep link the wallet can open, usually rendered as a QR code.

{% include_cached casproperties.html properties="cas.authn.oidc.vc.presentation" %}

## Credential Validity

Each credential configuration controls how long the credentials it issues remain valid via
`credential-validity`, which defaults to thirty days. The value sets the `exp` claim of the
issued credential, and the `validUntil` property for formats that carry one. A wallet stores a
credential long after the issuance exchange has finished, so this period describes the useful
life of the credential itself and is unrelated to the lifetime of the offer, the pre-authorized
code, the nonce or the access token used to obtain it.

## Credential Signing

After claims are collected and validated, CAS signs the credential using issuer key material.

For JWT-based credential formats, this generally reuses the same signing infrastructure
used for ID tokens and other JWT artifacts, while still producing a payload that is
specific to the verifiable credential format being issued.
