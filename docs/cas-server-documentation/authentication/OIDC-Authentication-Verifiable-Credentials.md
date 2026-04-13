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
### Credential Endpoint

Issues a verifiable credential to the wallet once the access token, proof, and requested
credential configuration have been validated.

```bash
POST /oidc/oidcVcCredential
```

This endpoint expects:

- A bearer access token.
- A requested `credential_configuration_id`.
- A proof object with a `nonce` as a claim.
     
The endpoint body is expected as:

```json
{
  credential_configuration_id: "myorg",
  proof: {
    proof_type: "jwt",
    jwt: proof
  }
}
```

### Nonce Endpoint

Produces a fresh c_nonce that may be used by the wallet in a later proof for the
credential request.
    
```bash
POST /oidc/oidcVcNonce
```

This endpoint typically returns:

- `c_nonce`
- `c_nonce_expires_at`

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

- Accept the pre-authorized_code grant.
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

## Credential Signing

After claims are collected and validated, CAS signs the credential using issuer key material.

For JWT-based credential formats, this generally reuses the same signing infrastructure
used for ID tokens and other JWT artifacts, while still producing a payload that is
specific to the verifiable credential format being issued.
