---
layout: default
title: CAS - OpenID Connect Federation
category: Protocols
---
{% include variables.html %}

# OpenID Connect Federation Protocol

The CAS server supports the [OpenID Federation protocol](https://openid.net/specs/openid-federation-1_0.html).

It can act as only one of the following roles (mutually exclusive):
- Trust Anchor (TA)
- Intermediate
- OpenID Provider (OP)

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-oidc-federation" %}

## Configuration

{% include_cached casproperties.html properties="cas.authn.oidc.federation" %}

## Subordinates

In OpenID Connect Federation, a subordinate is an entity that is “below” 
another federation entity in the trust hierarchy. Simply put, a subordinate is an entity whose 
federation metadata and trust relationship are vouched for by a 
higher-level entity in the federation.

More concretely, a subordinate is an entity for which a superior entity, such 
as a Trust Anchor or Intermediate Entity, can issue a Subordinate Statement. 
That statement is a signed Entity Statement saying, in effect:

> “I, the issuer, make authoritative claims about this subject entity.” 

The `iss` is the superior, and the `sub` is the subordinate. 
   
```
Trust Anchor
  └── Intermediate Entity
        └── OpenID Provider
        └── Relying Party
```

For a trust anchor or intermediate, the supported subordinates must be defined 
as JSON files with the `OidcFederationSubordinate` format:

### Relying Party Subordinate

Here is an example of an RP subordinate definition:

```json
{
  "entityId" : "http://rp",
  "metadata": {
    "openid_relying_party": {
      "redirect_uris": [
        "http://rp/callback"
      ],
      "application_type": "web",
      "response_types": [
        "code"
      ],
      "grant_types": [
        "authorization_code"
      ],
      "scope": "openid email profile",
      "token_endpoint_auth_method": "private_key_jwt",
      "token_endpoint_auth_signing_alg": "RS256",
      "request_object_signing_alg": "RS256",
      "jwks": {
        "keys": [
          {
            "kty": "RSA",
            "e": "AQAB",
            "use": "sig",
            "kid": "keyid",
            "n": "2moVQ...2aq7Q"
          }
        ]
      },
      "client_registration_types": [
        "explicit", "automatic"
      ],
      "client_name": "RP test"
    }
  },
  "federationKeys": [
    {
      "kty": "EC",
      "use": "sig",
      "crv": "P-256",
      "kid": "TJns",
      "x": "iLPMKMGqb7HeCnov5xBwEnq2zUbX5gNbyoVn1EHpvSI",
      "y": "1kcVTdS1_Y2_NnL0E9LpWhPWsoCNOFQR2Dd7VAJBYKY"
    }
  ]
}
```
       
### OpenId Provider Subordinate

Here is an example of an OP subordinate definition:

```json
{
  "entityId" : "http://op",
  "metadata": {
    "openid_provider": {
      "authorization_endpoint": "http://op/login",
      "token_endpoint": "http://op/token",
      "registration_endpoint": "http://op/clients",
      "introspection_endpoint": "http://op/token/introspect",
      "revocation_endpoint": "http://op/token/revoke",
      "pushed_authorization_request_endpoint": "http://op/par",
      "federation_registration_endpoint": "http://op/federation/clients",
      "issuer": "http://op/oidc",
      "jwks_uri": "http://op/jwks.json",
      "scopes_supported": [
        "openid"
      ],
      "response_types_supported": [
        "code", "token", "id_token", "id_token token", 
        "code id_token", "code id_token token"
      ],
      "response_modes_supported": [
        "query", "fragment", "form_post", "query.jwt", 
        "fragment.jwt", "form_post.jwt", "jwt"
      ],
      "grant_types_supported": [
        "implicit", "authorization_code", "refresh_token", "password", 
        "client_credentials", "urn:ietf:params:oauth:grant-type:jwt-bearer"
      ],
      "code_challenge_methods_supported": [
        "plain", "S256"
      ],
      "token_endpoint_auth_methods_supported": [
        "client_secret_basic", "client_secret_post", 
        "client_secret_jwt", "private_key_jwt", "none"
      ],
      "token_endpoint_auth_signing_alg_values_supported": [
        "HS256", "HS384", "HS512", "RS256", "RS384", "RS512", 
        "PS256", "PS384", "PS512", "ES256", "ES256K", "ES384", "ES512"
      ],
      
      ...TRUNCATED...
      
      "userinfo_encryption_enc_values_supported": [
        "A128CBC-HS256", "A192CBC-HS384", "A256CBC-HS512", 
        "A128GCM", "A192GCM", "A256GCM", "XC20P"
      ],
      "display_values_supported": [
        "page"
      ],
      "claim_types_supported": [
        "normal"
      ],
      "claims_supported": [
        "sub"
      ],
      "claims_parameter_supported": true,
      "frontchannel_logout_supported": true,
      "frontchannel_logout_session_supported": true,
      "backchannel_logout_supported": true,
      "backchannel_logout_session_supported": true
    }
  },
  "federationKeys": [
    {
      "kty": "RSA",
      "e": "AQAB",
      "use": "sig",
      "kid": "PRRz",
      "n": "vPZc7Y..."
    }
  ]
}
```

The `metadata` and `federationKeys` properties come both from the 
data in the `.well-known/openid-federation` endpoint. Both sections are **mandatory** in the federation entity definitions.
Metadata in a subordinate statement takes precedence over the metadata the entity publishes itself, so an empty object
for an entity type, such as `"openid_relying_party": {}`, leaves the entity's own metadata in effect.

Signed entity statements are returned from the `/oidc/fetch` endpoint, 
depending on the requested subordinate (`sub` request parameter). A subordinate statement carries the keys of the
subordinate only. An unknown subordinate is answered with `404` and `not_found`, a missing `sub` or the entity
identifier of CAS itself with `400` and `invalid_request`, and a subordinate defined without metadata or keys with
`500` and `server_error`; entities that issue no subordinate statements answer `404`.

The `/oidc/list` endpoint lists the subordinates, and can filter them by entity type, with one or more `entity_type`
request parameters, and to intermediates only, with `intermediate=true`, based on the metadata of their definitions.
Filtering by trust marks is not supported and is answered with `unsupported_parameter`.

## Entity Configuration

CAS publishes its entity configuration at `.well-known/openid-federation` under its issuer, signed with its federation
keys, whatever its role. As an OpenID Provider, it describes itself as `openid_provider` with its discovery metadata,
which advertises `automatic` as the only client registration type; plain OpenID Connect deployments without the
federation role do not advertise client registration types. When CAS also
[issues verifiable credentials](OIDC-Authentication-Verifiable-Credentials.html), the entity configuration describes it
as `openid_credential_issuer`, with the same metadata as its credential issuer metadata endpoint, and as
`oauth_authorization_server`, which the credential issuer names as its authorization server, so that wallets of
federation-based ecosystems find and trust the credential issuer through its trust chain
([OpenID Federation for Wallet Architectures](https://openid.net/specs/openid-federation-wallet-1_0.html)). When CAS
presents itself to wallets as a verifier with the `openid_federation` client identifier prefix, the entity
configuration also describes it as `openid_credential_verifier`, with its keys and response URI, as described in
[verifiable presentations](OIDC-Authentication-Verifiable-Credentials.html#openid-federation-verifier). This
requires the issuer of CAS to be the entity identifier; otherwise these entity types are left out, with a warning.

Trust marks that trust mark issuers have issued to CAS are published in the entity configuration as `trust_marks`. Each
trust mark is a signed JWT, given as a file, a URL or the JWT itself; files and URLs are read again each time the entity
configuration is produced, so a renewed trust mark is published without a restart. The trust mark type is taken from
the trust mark, and a trust mark that is about another entity, names no type or has expired is not published and is
reported in the logs. Trust marks are not verified by CAS, which leaves that to the parties that resolve it, and can be
defined using CAS properties.

## Key Rollover

CAS signs its entity configuration and the statements it issues with its federation keys, which are kept in their own
keystore and go through the same lifecycle as the [OpenID Connect keys](OIDC-Authentication-JWKS-Rotation.html), as
[OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-11.2) describes for rolling keys over:

- A new keystore starts with a current key, which signs, and a future key, which is only published. A keystore that
  holds a single key without a lifecycle state keeps working; its key is current.
- The entity configuration publishes the current, future and previous keys, so a key is known to the federation before
  it starts signing and after it stopped.
- Rotation makes the future key current, the current key previous, and publishes a new future key. When the keystore
  has no future key yet, rotation only publishes one, and the next rotation makes it current.
- Revocation retires the previous keys: they leave the entity configuration and are kept, without their private keys,
  as historical keys, with the time they were retired as their expiration.
- Every role publishes its historical keys at the federation historical keys endpoint, advertised in its
  entity configuration, as a JWK set signed with the current key.

Rotation and revocation can run on a schedule, which can be defined using CAS properties, or be triggered on demand:

{% include_cached actuators.html endpoints="oidcFederationJwks" casModule="cas-server-support-oidc-federation" %}

The keystore must be writable for rotation and revocation. Superiors verify the entity configuration of CAS with the
keys of their subordinate statement about it: when CAS is a subordinate, its future key should be added to the
subordinate definition at each superior before the rotation makes it current. Likewise, entities that trust a CAS trust
anchor with keys obtained out of band should be given its future key ahead of time.

## Trust Anchors

An OpenID Provider accepts the trust chains of relying parties that end at one of its trust anchors. Each trust anchor
is defined by its entity identifier and its keys, which should be obtained out of band, as
[OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-11.3) recommends, and may be given as
a file, a URL or the JSON web key set itself; private key members are ignored. A trust anchor defined without keys is
trusted with the keys of its own entity configuration, that is with whoever answers at its entity identifier.
Trust anchors and their keys can be defined using CAS properties.

The authority hints are the entities directly above CAS, which issue subordinate statements about it; they are not
trust anchors unless they are also defined as such. When no trust anchor is defined, CAS uses the authority hints as
trust anchors, with the keys of their own entity configuration, and logs a warning.

## Automatic Registration

As an OpenID Provider, CAS registers relying parties of the federation automatically, as described by
[OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-12.1), without any prior
registration step:

- The relying party uses its entity identifier as its client id, which must be an `https` URL. On the first
  authorization or pushed authorization request, CAS resolves the trust chains of the relying party up to one of the
  [trust anchors](#trust-anchors), and registers the resolved `openid_relying_party` metadata as an application that
  expires with the trust chain and is resolved again shortly before.
- The relying party authenticates with `private_key_jwt`, assumed when its metadata names no method, or with
  `tls_client_auth`. A relying party whose metadata names any other method is not registered.
- Every authorization request proves that the relying party controls its keys. It either carries a request object
  signed with a key of the relying party's metadata, whose `iss` and `client_id` are the entity identifier, whose `aud`
  is only the issuer of CAS, with `jti` and `exp` and without `sub`, or it refers with `request_uri` to a
  [pushed authorization request](OIDC-Authentication-PAR.html) for which the relying party authenticated itself.
  Client assertions may be addressed to the issuer of CAS as well as to its token endpoint.
- A request object is used once: CAS remembers its `jti` for the lifetime of transient session tickets, during which only
  the very same request object is accepted again, as happens when the browser returns from the login.
- A request object may also be [encrypted to CAS](OIDC-Authentication-JWKS.html#encrypted-request-objects); it is
  decrypted first, and the signed request object inside is checked as above.
- Requests that fail these checks are refused with `invalid_request` and are never redirected to the relying party.
- Resolving trust chains is bounded: CAS follows a limited number of authority hints from any entity configuration and
  accepts a limited number of intermediates between the relying party and a trust anchor, and an entity identifier
  whose trust chains could not be resolved is not resolved again for a while. Meanwhile, a registration that could not
  be refreshed is kept as it is. These limits can be tuned using CAS properties.

An authorization request passed by value travels through the CAS login as part of its service URL, and CAS does not
match service URLs beyond a maximum length, which can be adjusted using CAS properties. A request object that carries
its `trust_chain` is several kilobytes long: relying parties should send it with a
[pushed authorization request](OIDC-Authentication-PAR.html), as OpenID Federation suggests, or the limit must be
raised.
