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
- How wallets should present the issuer (`display`: name, language and logo), taken from
  `cas.authn.oidc.vc.issuer.display`; without it wallets show the issuer as unnamed.
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

Verifiers other than CAS locate the keys that sign issued credentials the same way, through the
[JWT VC Issuer Metadata](https://datatracker.ietf.org/doc/draft-ietf-oauth-sd-jwt-vc/) at
`https://sso.example.org/.well-known/jwt-vc-issuer/cas/oidc`, which names the issuer and points to the
OpenID Connect JWKS:

```json
{
  "issuer": "https://sso.example.org/cas/oidc",
  "jwks_uri": "https://sso.example.org/cas/oidc/jwks"
}
```

CAS is normally deployed under the `/cas` context path, so neither request reaches the
application at all and the servlet container answers with its own `404`. Route them onto the
paths CAS serves, either in the proxy that fronts CAS or with the
[embedded Tomcat rewrite valve](../installation/Servlet-Container-Embedded-Tomcat-RewriteValve.html).
The valve must be registered on the engine, which runs before a context is selected.

The rewrite rule would be similar to:

```
RewriteRule ^/\.well-known/(openid-credential-issuer|oauth-authorization-server|openid-configuration|jwt-vc-issuer)(/.+)$ $2/.well-known/$1 [L]
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
sees instead and the exchange is rejected, because the wallet has
no client registration to authenticate with.

For the same reason the authorization server metadata advertises `pre-authorized_grant_anonymous_access_supported`
as `true` whenever the pre-authorized code grant is listed in `grant_types_supported`. A wallet that finds no such
value assumes `false` and may refuse to redeem the code without a `client_id` it does not have.

### Credential Endpoint

Issues a verifiable credential to the wallet once the access token, proof, and requested
credential configuration have been validated.

```bash
POST /oidc/oidcVcCredential
```

This endpoint expects:

- An access token, presented in the `Authorization` header as `Bearer ...` or, when the token response
  named the token type `DPoP`, as `DPoP ...` per [RFC 9449](https://www.rfc-editor.org/rfc/rfc9449).
  An `access_token` or `token` request parameter is accepted as well, as it is elsewhere in CAS.
  A `DPoP`-bound token must be accompanied by a `DPoP` proof header bound to that token; a request
  without one, or with a proof that does not verify, is answered with `401` and
  `WWW-Authenticate: DPoP error="invalid_dpop_proof"`. A proof may not be reused.
- The requested credential, named either by `credential_configuration_id` or, when the token
  response returned `credential_identifiers` in its authorization details, by
  `credential_identifier`. The two are mutually exclusive.
- A `proofs` object holding one or more proof JWTs, each carrying a `nonce` claim.

The endpoint body is expected as:

```json
{
  "credential_configuration_id": "myorg",
  "proofs": {
    "jwt": [
      "eyJ0eXAiOiJvcGVuaWQ0dmNpL..."
    ]
  }
}
```

Each proof must be signed with one of the `proof-signing-alg-values-supported` of the requested credential
configuration and name the holder key by one of its `cryptographic-binding-methods-supported`:

| Proof header | Binding method | Holder key                                                                                      |
|--------------|----------------|-------------------------------------------------------------------------------------------------|
| `jwk`        | `jwk`          | The key itself. A `kid` sent alongside it is ignored.                                           |
| `x5c`        | `jwk`          | The public key of the first certificate, which must be within its validity period.              |
| `kid`        | `did:jwk`      | The key encoded in the `did:jwk` DID URL. Other DID methods cannot be resolved and are refused. |

RSA, EC and Ed25519 (`EdDSA`) keys are accepted. A proof that does not satisfy the configuration is answered
with `invalid_proof`. The defaults are `ES256` and `RS256` with the `jwk` binding method.

There is no separate batch credential endpoint. A batch is a single credential request carrying
several proofs, and the response holds one credential per proof, all of the same credential
configuration. How many proofs are accepted is advertised as `batch_credential_issuance` in the
issuer metadata and controlled by `cas.authn.oidc.vc.issuer.batch-size`.

The response is:

```json
{
  "credentials": [
    {
      "credential": "eyJhbGciOiJSUzI1NiIs..."
    }
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
  "credentialConfigurationIds": [
    "..."
  ]
}
```

The response carries the offer URI and the deep link a wallet opens, typically rendered as a QR code:

```json
{
  "transactionId": "...",
  "credentialOfferUri": "https://sso.example.org/cas/oidc/oidcVcCredentialOffer/...",
  "credentialOfferLink": "openid-credential-offer://?credential_offer_uri=https%3A%2F%2Fsso.example.org%2Fcas%2Foidc%2FoidcVcCredentialOffer%2F...",
  "txCode": "..."
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
- Return the `authorization_details` the token was granted, with their `credential_identifiers`.
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

- The wallet asks CAS for a fresh c_nonce from the nonce endpoint; the token endpoint never returns one.
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

Instead of authorization details, the wallet may request a credential by the `scope` its credential configuration
publishes in the issuer metadata, for example `scope=openid UniversityDegree`. Every granted scope that belongs to a
credential configuration authorizes that configuration, narrowed by the service's verifiable credentials policy.
These scopes are accepted and advertised in `scopes_supported` without being listed among the discovery scopes.
The token response then carries no credential identifiers, so the credential request names the configuration with
`credential_configuration_id`. A wallet may use both mechanisms in one authorization request.

A refresh token issued in this flow keeps the authorization details of its code, as RFC 9396 describes, so access
tokens obtained with it may request the same credentials; scopes are honored as before.

## Pre-Authorized Code Flow

In pre-authorized code flows, CAS or a trusted backend prepares the issuance transaction
before the wallet starts the OAuth exchange.

The general flow is:

- A trusted caller creates an issuance transaction.
- CAS stores the transaction and issues a pre-authorized code.
- CAS returns a wallet-facing `credential_offer_uri`.
- The wallet resolves the offer.
- The wallet exchanges the pre-authorized code at the token endpoint.
- CAS returns an access token.
- The wallet calls the credential endpoint with the access token and proof.
- CAS validates the request and issues the credential.

The pre-authorized code is single use, as OpenID4VCI requires. It is redeemed and deleted before the
access token is minted, so of several concurrent exchanges of the same code exactly one succeeds and the
rest are refused; the issuance transaction is removed along with it.

The [stateless ticket registry](../ticketing/Stateless-Ticket-Registry.html) cannot delete anything, so
there the pre-authorized code and the nonces handed out by the nonce endpoint are not single use: each
stays usable until it expires, which departs from OpenID4VCI. Keep their expiration short. Verifiable
presentations are not supported with that registry.

## Verifiable Presentations

CAS can also act as a verifier and ask a wallet to present a credential. A relying party creates a
presentation request, and CAS returns a deep link the wallet can open, usually rendered as a QR code.

{% include_cached casproperties.html properties="cas.authn.oidc.vc.presentation" %}

The relying party creates the request with:

```bash
POST /oidc/oidcVcPresentationRequest
```

```json
{
  "credentials": [
    {
      "id": "university-degree",
      "format": "dc+sd-jwt",
      "vct_values": [
        "https://sso.example.org/cas/oidc/oidcVcCredentialType/UniversityDegreeCredential"
      ],
      "claims": [
        {
          "path": [
            "given_name"
          ]
        },
        {
          "path": [
            "email"
          ],
          "required": false
        }
      ]
    }
  ],
  "redirect_uri": "https://app.example.org/presentation/callback"
}
```

Claims are required unless marked otherwise. DCQL has no per-claim flag, so optional claims are expressed
with `claim_sets`: every claim first, then the required ones alone, or each optional claim on its own when
none is required. The wallet returns the first combination it can satisfy, and CAS accepts any of them.

Wallets may bind credentials to EC, RSA or Ed25519 keys. CAS advertises and accepts the key binding
algorithms `ES256`, `ES384`, `ES512`, `RS256`, `RS384`, `RS512`, `PS256`, `PS384`, `PS512` and `Ed25519`
(also accepted as `EdDSA`), and advertises the signing algorithms of its `dc+sd-jwt` credential
configurations for the credential itself.

The optional `redirect_uri` enables a same-device flow and must be registered for the client creating the
request. After the wallet answers, CAS sends it to that URI with a fresh `response_code` in the fragment,
as OpenID4VP recommends against session fixation, and releases the outcome only when the relying party
presents that code. Without a `redirect_uri`, as in a cross-device flow with a QR code, the relying party
polls for the outcome instead.

With `cas.authn.oidc.vc.presentation.client-identifier-prefix` set to `X509_SAN_DNS`, the request object is
signed and served by reference. The signing key must carry an `x5c` certificate chain whose leaf names the
issuer host as a DNS subject alternative name. The request carries that chain in its `x5c` header without a
trailing self-signed trust anchor, as HAIP 1.0 requires. HAIP also requires the leaf not to be self-signed; a
self-signed leaf is accepted with a warning in the logs.

The relying party that created the request collects the outcome from:

```bash
GET /oidc/oidcVcPresentationResult?requestId=...&response_code=...
```

This endpoint requires the same client authentication as the request creation endpoint, and only the
client that created the request may collect its outcome; any other client, or a same-device request
without its `response_code`, gets `404`. It answers
`{"status": "pending"}` while the wallet has not responded, and once it has, `{"status": "verified"}`
together with the claims that were disclosed, keyed by credential query id. A wallet that declines or
cannot answer posts an error response instead. Nothing authenticates that response, so anyone who saw the
request could send one; it is recorded but does not end the request. The request keeps reporting `pending`
until it expires, a valid presentation that arrives meanwhile takes precedence, and only a request that
expired without one reports the error:

```json
{
  "status": "error",
  "error": "access_denied",
  "error_description": "The user declined"
}
```

The outcome is delivered once and then removed, so a second poll reports `404`, as does a request that
expired unanswered.

CAS as a verifier trusts only itself. A presented credential is accepted when its `iss` is this
deployment's own issuer, its `vct` resolves to one of the credential configurations above, and its
signature verifies against this deployment's own signing key; `iat` and `exp` are both required, and a
credential carrying a `status` claim is refused rather than accepted unchecked, since CAS evaluates no
status list. There is no external issuer trust list, no `x5c` chain validation, no DID resolution, no
OpenID Federation and no Token Status List, so credentials issued elsewhere are rejected.

This is a trust policy rather than a protocol limitation: OpenID4VP leaves issuer trust to the verifier,
noting that "Verifiers must verify that the issuer of a received presentation is trusted on their own".

<div class="alert alert-info">:information_source: <strong>Note</strong><p>Verified presentations are not connected to
<a href="../authorization/Heimdall-Authorization-Overview.html">Heimdall</a> authorization yet. Passing the claims of a verified
presentation to AuthZEN requests as subject properties, so that authorization policies can decide on them, may be supported
in the future. Until then, a relying party can send the disclosed claims it collected as <code>subject.properties</code>
of its AuthZEN requests.</p></div>

## Authorized Credential Types

The credential configurations above describe what the issuer is able to mint. They say nothing about
which relying party may ask for what, so by default every registered client may obtain every credential
the deployment defines. A service narrows that down with a verifiable credentials policy:

```json
{
  "@class": "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client",
  "clientSecret": "secret",
  "serviceId": "^https://app.example.org/.*",
  "name": "Example",
  "id": 1,
  "verifiableCredentialsPolicy": {
    "@class": "org.apereo.cas.oidc.vc.services.DefaultRegisteredServiceOidcVerifiableCredentialsPolicy",
    "allowedCredentialTypes": [ "java.util.HashSet", [ "myorg" ] ]
  }
}
```

The entries in `allowedCredentialTypes` are credential configuration ids. A service that defines no
policy, or whose policy lists no credential types, may obtain every credential configuration the issuer
publishes; only a policy that actually names types restricts the service to those types. A policy can
never widen a service beyond what the issuer publishes, so naming a credential configuration that does
not exist grants nothing.

The policy is enforced wherever a credential type is claimed: when a credential offer transaction is
created, when authorization details are turned into an authorization code, and again at the credential
endpoint when the token is spent. That last check reads the policy afresh, so tightening a service takes
effect against access tokens that are already outstanding.

## Credential Validity

Each credential configuration controls how long the credentials it issues remain valid via
`credential-validity`, which defaults to thirty days. The value sets the `exp` claim of the
issued credential, and the `validUntil` property for formats that carry one. A wallet stores a
credential long after the issuance exchange has finished, so this period describes the useful
life of the credential itself and is unrelated to the lifetime of the offer, the pre-authorized
code, the nonce or the access token used to obtain it.

Claim values come from principal attributes. A value that reads as a number, such as `95.5`, is issued as a
number; one whose number would not read back the same way, such as `02134`, is issued as text exactly as released.

## Credential Formats

Each credential configuration is described in the issuer metadata the way its format requires. A
`dc+sd-jwt` configuration publishes its `vct`. A `jwt_vc_json` configuration publishes a
`credential_definition` with the credential `type`, and a `jwt_vc_json-ld` configuration adds its
`@context`. The type is `VerifiableCredential` plus the configuration's `scope`, or its id when no scope
is set, and the context is the W3C Verifiable Credentials Data Model 2.0 base context alone, whose
vocabulary covers the credential's claims. Issued credentials carry exactly what the metadata publishes:

```json
{
  "credential_configurations_supported": {
    "employee": {
      "format": "jwt_vc_json-ld",
      "scope": "EmployeeCredential",
      "credential_definition": {
        "@context": [
          "https://www.w3.org/ns/credentials/v2"
        ],
        "type": [
          "VerifiableCredential",
          "EmployeeCredential"
        ]
      }
    }
  }
}
```

## Credential Signing

After claims are collected and validated, CAS signs the credential with its own issuer key, selected the
same way as for other OpenID Connect artifacts and honoring the service's `jwksKeyId` when one is set.
The credential names that key with `kid` and does not say which client it was issued to, so verifiers
cannot correlate the holder with the relying party; CAS as a verifier finds the key by `kid` as well.

A credential is not an ID token, and the relying party's ID token settings do not apply to it. The
algorithm is the first entry of the credential configuration's `credential-signing-alg-values-supported`
that the issuer's signing key can perform, so the order of that list is a preference the deployment
expresses, and that list is also the permitted set, so no other algorithm can be used. This is what keeps
issuance consistent with the issuer metadata and with what a verifier, CAS included, accepts.

When the issuer signing key in the keystore carries an `x5c` certificate chain, the credential carries it as
its `x5c` header, leaf certificate first, as HAIP 1.0 requires, so a verifier can take the issuer key from the
leaf and validate the chain against its trust list. A trailing self-signed certificate is treated as the trust
anchor and left out, as HAIP requires. HAIP also requires the leaf not to be self-signed; a self-signed signing
certificate is still sent, and CAS logs a warning since wallets that follow HAIP may refuse it. A key without a chain
produces credentials without `x5c`.

A service may narrow the algorithms used for its own credentials through its verifiable credentials
policy:

```json
{
  "@class": "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client",
  "serviceId": "^https://app.example.org/.*",
  "name": "Example",
  "id": 1,
  "verifiableCredentialsPolicy": {
    "@class": "org.apereo.cas.oidc.vc.services.DefaultRegisteredServiceOidcVerifiableCredentialsPolicy",
    "credentialSigningAlgValuesSupported": [ "java.util.HashSet", [ "ES256" ] ]
  }
}
```

As with `allowedCredentialTypes`, this can only narrow: the result is the intersection with what the
credential configuration advertises, so naming an algorithm the configuration does not offer leaves the
service with nothing and the request is refused. A policy that names no algorithms leaves the service
with everything the configuration advertises.
