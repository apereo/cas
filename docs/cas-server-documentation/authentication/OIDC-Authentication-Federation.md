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
  "entityId" : "https://rp.example.org",
  "metadata": {
    "openid_relying_party": {
      "redirect_uris": [
        "https://rp.example.org/callback"
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

Here is an example of an OP subordinate definition, with a shortened `openid_provider` metadata:

```json
{
  "entityId" : "https://op.example.org",
  "metadata": {
    "openid_provider": {
      "issuer": "https://op.example.org",
      "authorization_endpoint": "https://op.example.org/authorize",
      "token_endpoint": "https://op.example.org/token",
      "pushed_authorization_request_endpoint": "https://op.example.org/par",
      "jwks_uri": "https://op.example.org/jwks.json",
      "client_registration_types_supported": [ "automatic" ],
      "scopes_supported": [ "openid", "profile", "email" ],
      "response_types_supported": [ "code" ],
      "grant_types_supported": [ "authorization_code", "refresh_token" ],
      "token_endpoint_auth_methods_supported": [ "private_key_jwt" ],
      "request_object_signing_alg_values_supported": [ "RS256", "ES256" ]
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
data in the `.well-known/openid-federation` endpoint. The `federationKeys` section is **mandatory** in the federation
entity definitions, while `metadata` is optional. Metadata in a subordinate statement takes precedence over the
metadata the entity publishes itself, so an empty object for an entity type, such as `"openid_relying_party": {}`,
leaves the entity's own metadata in effect.

### Metadata Policies

A subordinate definition may also carry a metadata policy, which CAS publishes in its subordinate statement as
[OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-6.1) describes, to restrict or adjust
the metadata of the subordinate and of every entity below it. Policies use the standard operators `value`, `add`,
`default`, `one_of`, `subset_of`, `superset_of` and `essential`; operators beyond those are ignored by entities that
do not understand them, unless they are listed as critical:

```json
{
  "entityId" : "https://rp.example.org",
  "metadataPolicy": {
    "openid_relying_party": {
      "scope": {
        "subset_of": [ "openid", "email", "profile" ]
      },
      "contacts": {
        "add": [ "ops@example.org" ]
      },
      "token_endpoint_auth_method": {
        "one_of": [ "private_key_jwt", "tls_client_auth" ],
        "essential": true
      }
    }
  },
  "metadataPolicyCrit": [],
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

A metadata policy that CAS cannot read, such as one that combines operators in a way the specification forbids, is not
published. Subordinates are listed for the entity types of their metadata as well as of their metadata policy.

A subordinate definition may also carry `constraints`, published as they are in its subordinate statement, as [OpenID
Federation](https://openid.net/specs/openid-federation-1_0.html#section-6.2) describes, to bound the trust chains that
go through the subordinate:

```json
{
  "entityId" : "https://intermediate.example.org",
  "constraints": {
    "max_path_length": 1,
    "naming_constraints": {
      "permitted": [ ".example.org" ]
    },
    "allowed_entity_types": [ "openid_relying_party" ]
  },
  "federationKeys": [ ]
}
```

The fetch, list and trust mark status endpoints of a subordinate are left out of the `federation_entity` metadata of its
subordinate statement, since they may only be published in the entity configuration of the subordinate.

Signed entity statements are returned from the `/oidc/fetch` endpoint, depending on the requested subordinate (`sub`
request parameter). A subordinate statement carries the keys of the subordinate only. An unknown subordinate is answered
with `404` and `not_found`, a missing `sub` or the entity identifier of CAS itself with `400` and `invalid_request`, and
a subordinate defined without keys, with a metadata policy CAS cannot read, or with constraints that are not an object,
with `500` and `server_error`; entities that issue no subordinate statements answer `404`.

The `/oidc/list` endpoint lists the subordinates, and can filter them by entity type, with one or more `entity_type`
request parameters, and to intermediates only, with `intermediate=true`. A subordinate has the entity types its
definition lists in `entityTypes`, or, when it lists none, the entity types of its `metadata`; a metadata policy for an
entity type does not give the subordinate that type, so a definition that only carries a policy should list its types.
Filtering by trust marks is not supported and is answered with `unsupported_parameter`.

### Resolve Endpoint

As a trust anchor or an intermediate, CAS resolves entities of the federation for others at its `/oidc/resolve`
endpoint, advertised as `federation_resolve_endpoint`, as described by [OpenID
Federation](https://openid.net/specs/openid-federation-1_0.html#section-8.3). A `GET` request names the entity with
`sub`, one or more trust anchors with `trust_anchor` and, optionally, the entity types whose metadata is wanted with one
or more `entity_type`; all entity types are returned otherwise.

Requests are not authenticated by default. Client authentication can be made optional or required using CAS
properties, as described by [OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-8.8): an
authenticated request is a `POST` with the same parameters and a `private_key_jwt` client assertion, signed with a
federation key of a subordinate of CAS as its subordinate definition holds it, whose `iss` and `sub` are the
subordinate, whose `aud` is only the entity identifier of CAS, and which carries `jti` and `exp`; each client assertion
is accepted once. The resolve response is then addressed to the subordinate with `aud`. The entity configuration
advertises the accepted methods as `federation_resolve_endpoint_auth_methods`. A `GET` when authentication is required,
or a request that does not authenticate, is answered with `401` and `invalid_client`; a `POST` when authentication is
not supported with `400` and `invalid_request`.

- A trust anchor resolves entities to itself. An intermediate resolves them to its [trust anchors](#trust-anchors), or
  to its authority hints when no trust anchor is defined. Requested trust anchors that CAS does not trust are ignored.
- CAS resolves the trust chains of the entity with the same bounds on authority hints and path length as for automatic
  registration, and builds its own statements itself rather than requesting them from its own endpoints. The shortest
  trust chain that satisfies the constraints of its statements and is not signed with a key its trust anchor has
  revoked is used, ending with the entity configuration of the trust anchor.
- The metadata of each entity type is resolved with the metadata policies of the trust chain. Entity types the
  constraints of the chain do not allow are left out.
- Only the trust marks of the entity that are valid, issued by issuers the trust anchor accepts for their type and
  active, are included. The response expires with the trust chain, or with the first of those trust marks to expire. A
  trust mark that cannot be checked for now, such as when its status endpoint cannot be reached, is left out, and the
  response is then not remembered.
- The response is a JWT of type `resolve-response+jwt`, signed with the current federation key.
- Resolved trust chains, and entities that could not be resolved, are remembered per entity and trust anchors for the
  period that applies to entity identifiers that could not be resolved, or until the chain or a trust mark expires, so
  that repeated requests do not resolve them again. That period can be tuned using CAS properties.
- Errors are answered as JSON: a missing or malformed parameter with `400` and `invalid_request`; trust anchors CAS does
  not trust with `404` and `invalid_trust_anchor`; an entity without a usable trust chain to them with `400` and
  `invalid_trust_chain`; metadata the policies of the chain cannot apply to with `400` and `invalid_metadata`; a
  resolution that cannot start because too many are running, or that cannot tell about a trust anchor that cannot be
  reached, answers with a server error or whose historical keys cannot be read, with `503` and
  `temporarily_unavailable`, which is not remembered. An
  OpenID Provider answers `404` and `not_found`.

## Entity Configuration

The entity identifier of CAS is its issuer, without a trailing slash, in every role.

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
[verifiable presentations](OIDC-Authentication-Verifiable-Credentials.html#openid-federation-verifier). The credential
issuer and authorization server entity types require the credential issuer of CAS to be the entity identifier;
otherwise they are left out, with a warning.

Trust marks that trust mark issuers have issued to CAS are published in the entity configuration as `trust_marks`. Each
trust mark is a signed JWT, given as a file, a URL or the JWT itself; files and URLs are read again each time the entity
configuration is produced, so a renewed trust mark is published without a restart. The trust mark type is taken from
the trust mark, and a trust mark that is about another entity, names no type or has expired is not published and is
reported in the logs. Trust marks are not verified by CAS, which leaves that to the parties that resolve it, and can be
defined using CAS properties.

As a trust anchor, CAS publishes in its entity configuration the trust mark types of the federation and the entities it
accepts as their issuers, as `trust_mark_issuers`. A trust mark type listed without issuers may be issued by anyone.
Both can be defined using CAS properties. A trust anchor also publishes, as `trust_mark_owners`, the owners of trust
mark types whose issuance they delegate to other issuers, each with the keys their delegations are verified with; only
the public members of those keys are published.

### Trust Mark Issuer

In any role, CAS can issue trust marks, as described by
[OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-7). Each trust mark type is defined with
the entities it is issued to, how long its trust marks are valid, and optionally a logo, a reference to human-readable
information and a delegation from the owner of the type, all of which can be defined using CAS properties. Nothing is
stored: a trust mark is signed with the current federation key when it is requested. When CAS issues trust marks, its
entity configuration advertises three endpoints, which do not require authentication by default:

- The trust mark endpoint answers a request for a trust mark type and an entity with a trust mark of that type about
  that entity, typed `trust-mark+jwt`, or with `404` when the entity does not hold that type.
- The trust mark status endpoint answers the status of a trust mark it was sent, in a signed status response:
  `active`; `expired` once the trust mark has expired; `revoked` when its subject no longer holds the type; or `invalid`
  when it is not typed as a trust mark or does not verify with the federation keys CAS publishes. A trust mark issued by
  another entity, or of a type CAS does not issue, is answered with `404`.
- The trust marked entities listing endpoint lists the entities that hold a trust mark type, optionally only the one
  asked for.

Requests to the trust mark endpoint can also be authenticated, as
[OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-8.8) allows, so that entities only
obtain their own trust marks. Client authentication at that endpoint can be left off, which is the default, allowed, or
required, using CAS properties, and the entity configuration advertises the choice along with the signing algorithms
CAS accepts. An authenticated request is a `POST` that authenticates with `private_key_jwt`: the client assertion is
signed with a federation key of the requester, as its [subordinate definition](#subordinates) holds it; its issuer and
subject are the requester; its only audience is the entity identifier of CAS; and it is accepted once, until it
expires. Only subordinates of CAS as a trust anchor or intermediate can therefore authenticate, and they may only ask
for their own trust marks. A request that fails to authenticate, or asks for the trust mark of another entity, is
answered with `401` and `invalid_client`, as is an unauthenticated request when client authentication is required.

Removing an entity from a trust mark type revokes the trust marks of that type it holds. A trust mark signed with a
federation key that has since been retired stays `active` for the rest of its life, since the historical keys keep it
verifiable, as [OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-8.7) intends; it becomes
`invalid` once that key is revoked.

## Key Rollover

CAS signs its entity configuration and the statements it issues with its federation keys, which are kept in their own
keystore and go through the same lifecycle as the [OpenID Connect keys](OIDC-Authentication-JWKS-Rotation.html), as
[OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-11.2) describes for rolling keys over:

- A new keystore starts with a current key, which signs, and a future key, which is only published. A keystore that
  holds a single key without a lifecycle state keeps working; its key is current. A keystore that holds no keys is
  given new ones, and one that cannot be read stops CAS from starting rather than being replaced.
- The entity configuration publishes the current, future and previous keys, so a key is known to the federation before
  it starts signing and after it stopped.
- Rotation makes the future key current, the current key previous, and publishes a new future key. When the keystore
  has no future key yet, rotation only publishes one, and the next rotation makes it current.
- Revocation retires the previous keys: they leave the entity configuration and are kept, without their private keys,
  as historical keys, with the time they were retired as their expiration.
- Every role publishes its historical keys at the federation historical keys endpoint, advertised in its
  entity configuration, as a JWK set signed with the current key.
- A key can also be revoked, for instance when its private key is believed compromised. A key that is still published
  leaves the entity configuration at once and is published as a historical key that expires at the time of the
  revocation and is marked `revoked`, with that time and the reason, such as `compromised`, `superseded` or a reason
  defined by the federation. A key that was already retired keeps its expiration and is marked revoked as well. When the
  revoked key was signing, the future key starts signing, or a new key when there is none, and a new future key is
  published. A key that signs without having been published before is unknown to superiors until they update their
  subordinate statements about CAS.

Rotation and revocation can run on a schedule, which can be defined using CAS properties, or be triggered on demand,
which is also how a key is revoked. A scheduled run only changes keys once they are old enough, at half the time
between two runs of its schedule: a scheduled rotation makes the future key current only once that key has been
published that long, and a scheduled revocation retires a previous key only once it has been previous that long. A
schedule that runs again soon after another run leaves the keys as they are: after a restart, or on another node that
shares the keystore file and reads it after the change. Nodes whose schedules run at the very same moment may each
change the keys, so in a cluster the schedules are best enabled on a single node. On demand, rotation and revocation
apply at once:

{% include_cached actuators.html endpoints="oidcFederationJwks" casModule="cas-server-support-oidc-federation" %}

The keystore must be writable for rotation and revocation; CAS writes a new keystore file next to it, with the
permissions, owner and group of the keystore where the file system allows it, and not readable by a group other than the
keystore's; a new keystore is readable by its owner only where the file system has such permissions. The new file then
replaces the keystore at once,
so that the keystore is never read half written. A keystore that is a symbolic link is written where the link points.
Where the file system does not allow replacing the keystore while it is in use, as Windows may, the keys are not saved
and stay as they were. CAS reads the keystore again when it changes, so nodes that share the keystore file sign with the
same keys; nodes that do not share it each have their own keys, which superiors would all have to know. Superiors verify
the entity configuration of CAS with the keys of their subordinate statement about it: when CAS is a subordinate, its
future key should be added to the subordinate definition at each superior before the rotation makes it current.
Likewise, entities that trust a CAS trust anchor with keys obtained out of band should be given its future key ahead of
time.

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
- Each redirect URI of the relying party is matched exactly, whatever characters it contains.
- The relying party authenticates with `private_key_jwt`, assumed when its metadata names no method, or with
  `tls_client_auth`. A relying party whose metadata names any other method is not registered, and so is a relying
  party that publishes its keys at a `jwks_uri` that is not an `https` URL.
- Every authorization request proves that the relying party controls its keys. It either carries a request object
  signed with a key of the relying party's metadata, whose `iss` and `client_id` are the entity identifier, whose `aud`
  is only the issuer of CAS, with `jti` and `exp` and without `sub`, valid for 10 minutes at most, or it refers with
  `request_uri` to a
  [pushed authorization request](OIDC-Authentication-PAR.html) for which the relying party authenticated itself.
  Client assertions may be addressed to the issuer of CAS as well as to its token endpoint, except at the pushed
  authorization endpoint, where the client assertion of an automatically registered relying party must be addressed to
  the issuer of CAS only, as [OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-12.1.1.2)
  requires; otherwise the request is refused with `401` and `invalid_client`.
- A request object is used once: CAS remembers its `jti` for as long as the request object could be accepted, and at
  least for the lifetime of transient session tickets, during which only the very same request object is accepted again,
  as happens when the browser returns from the login.
- A request object may also be [encrypted to CAS](OIDC-Authentication-JWKS.html#encrypted-request-objects); it is
  decrypted first, and the signed request object inside is checked as above.
- Requests that fail these checks are refused with `invalid_request` and are never redirected to the relying party.
- The relying party that must prove its keys is the client of the authorization request: the `client_id` parameter or,
  when the request carries none, the `client_id` of its request object, on the OpenID Connect and the OAuth
  authorization endpoints alike, including the OpenID Connect authorization and pushed authorization endpoints that also
  answer outside the OpenID Connect base path, whatever the encoding of the path. A request object whose `client_id`
  differs from the `client_id` parameter is refused for every client, as [OpenID
  Connect](https://openid.net/specs/openid-connect-core-1_0.html#RequestObject) requires.
- The constraints that trust anchors and intermediates set in their subordinate statements apply to every trust chain
  of a relying party, whether resolved or sent, as
  [OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-6.2) requires: the maximum number of
  intermediates below the entity that sets them, the permitted and excluded domain names of the entities below it, and
  the entity types they may have, which must include `openid_relying_party`. A trust chain that breaks any of them is
  not used. The trust chains of trust mark issuers are held to the same constraints, except for entity types.
- The metadata of a relying party is resolved from its trust chain as
  [OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-6.1) describes: the metadata the
  immediate superior states about the relying party takes precedence over the metadata the relying party publishes,
  and the metadata policies of the trust anchor and intermediates are then merged, from the trust anchor down, and
  applied. `scope` is handled as a list of scope values. Operators beyond the standard ones are ignored, unless a
  statement of the chain lists them as critical, in which case the trust chain is not used. A trust chain whose
  policies conflict, or whose policies the metadata does not satisfy, such as a missing essential parameter, is not
  used either.
- The keys a trust anchor is trusted with out of band may still list a key the trust anchor has since revoked. When the
  trust anchor publishes its historical keys, CAS reads them, verified with those keys, and does not use a trust chain
  in which the trust anchor signed with a key it marks revoked, or with a key it had already retired when it issued the
  statement. The historical keys are always found through the entity configuration CAS fetches from the trust anchor,
  never through one a trust chain sent by a relying party carries; they must be served over `https`, and each must carry
  its expiration, as the specification requires. A trust anchor that advertises no historical keys has revoked none. CAS
  keeps the historical keys of a trust anchor for a minute. When its entity configuration or the historical keys it
  advertises cannot be obtained or verified, CAS cannot tell whether the key was revoked and does not use the trust
  chain for now; the relying party is not remembered as unresolved on that account, so it can be registered as soon as
  the trust anchor can be reached again. Every statement of a trust chain must name the key it is signed with, as the
  specification requires; a trust chain with a statement that does not is not used.
- The metadata superiors assert about a relying party, and their metadata policies, apply only to the entity types
  its entity configuration publishes: an entity that publishes no `openid_relying_party` metadata is not registered,
  whatever its superiors state.
- Resolving trust chains is bounded: CAS follows a limited number of authority hints from any entity configuration and
  accepts a limited number of intermediates between the relying party and a trust anchor, and an entity identifier whose
  trust chains could not be resolved is not resolved again for a while. Meanwhile, a registration that could not be
  refreshed is kept as it is. Each resolution sends a limited number of requests, 50 by default, counting entity
  configurations, subordinate statements, historical keys and trust mark status requests together, including those for
  the issuers of its trust marks; a statement fetched once is reused within the same resolution, and a resolution that
  needs more fails. The historical keys of trust anchors are not counted. Each resolution must also be done within a
  limited time, 10 seconds by default, all of its requests together, however slowly the other side answers, and every
  response is limited in size; the time limit covers each whole request, from the host name lookup and the TLS handshake
  to the last byte of the response. Requests are only sent over `https`, never to link-local, unspecified or multicast
  addresses such as the metadata service of a cloud provider, including such addresses embedded in IPv6 addresses, use
  the TLS settings of CAS and do not follow redirects. Addresses written in a URL are always checked, and host names
  when CAS resolves them, as it does to connect directly or through a SOCKS proxy; host names sent through an HTTP proxy
  of the JVM, or through a SOCKS proxy when CAS cannot resolve them, are resolved by the proxy, which then decides where
  they may go. A limited number of requests run at the
  same time, those abandoned at the deadline included. A trust anchor that does not accept a connection or answer in the
  time it is given, or that answers with a server error, is treated as unreachable. A resolution that runs out
  of time or requests, or finds no room among the running requests, cannot tell whether a relying party has a valid
  trust chain or trust mark: it is unavailable, and the relying party is not remembered as unresolved. A limited number
  of resolutions run at the same time, 10 by default, for automatic registration and the resolve endpoint together; one
  that cannot start within a second fails without the entity being remembered as unresolved, and so does a registration
  that cannot get its turn within a few seconds. Using the trust chain a relying party sent and, when it cannot be used,
  resolving its trust chains is a single resolution, within one time limit and request budget. A relying party that is
  not registered yet and cannot be registered for now is answered with `temporarily_unavailable` rather than as an
  unknown client. These limits can be tuned using CAS properties.
- A relying party whose metadata asks for pairwise subject identifiers receives them, calculated with a salt that can be
  defined using CAS properties, so that they stay the same whenever the relying party is registered again and are
  shared by relying parties of the same sector; the salt is never written into its registration. Without that salt, such
  a relying party is not registered. Its sector must be its own: its sector identifier URI, when it has one, must be an
  `https` URL, is fetched as a request of the resolution and must list all of its redirect URIs, and without one, its
  redirect URIs must all be in the same sector, read as they are when identifiers are calculated: an `http` or `https`
  redirect URI on a host other than a loopback one is in the sector of its host, and any other one in the relying
  party's own. A sector identifier
  document that cannot be fetched for now leaves the relying party unregistered for now, without remembering it as
  unresolved, and its other trust anchors are tried.
- A relying party may send its trust chain in the `trust_chain` header of its request object or client assertion, as
  [OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-12.1.1.1) allows. Whenever CAS would
  resolve the relying party, to register it, to refresh its registration or after a key rollover, it uses that chain
  instead, once fully verified: the chain must start with the entity configuration of the relying party, end at a trust
  anchor whose keys are defined out of band, verify statement by statement from those keys, not have expired and not
  exceed the maximum path length; the required trust marks and the metadata policies of the chain apply as usual.
  Statements are public, so a chain alone does not show who sends it: it is used only when its entity configuration
  was not issued before the one the current registration was built from, and when the request object or client assertion
  that carries it is signed with the keys it resolves to. A chain replayed by someone else, even a genuine one, cannot
  keep or bring back keys the relying party has replaced. CAS resolves the trust chains itself when the
  sent chain cannot be used, and always when the trust anchor is trusted with the keys of its own entity configuration,
  which a sent chain could forge.
- A relying party that rolls its keys over is resolved again before its registration expires, as
  [OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-12.3) allows: when a request object
  or a client assertion it sends, at any endpoint, verifies with none of the keys of its registration, CAS resolves its
  trust chains first and then checks the request with the keys the relying party now publishes. Each relying party is
  resolved this way at most once in the same period that applies to entity identifiers that could not be resolved, so
  objects signed with keys a relying party never published cannot make CAS fetch its statements over and over. Such
  objects count against that period whoever sends them, and the relying party is then resolved from what it publishes,
  so a relying party should publish a new key before it signs with it.

### Required Trust Marks

An OpenID Provider may only register relying parties that hold specific
[trust marks](https://openid.net/specs/openid-federation-1_0.html#section-7), such as a certification or a
membership the federation vouches for. The required trust mark types can be defined using CAS properties. For each of
them, the entity configuration of the relying party must carry a trust mark of that type that:

- is a signed JWT typed `trust-mark+jwt` that names its signing key, is about the relying party, and has been issued
  and has not expired;
- is issued by an entity that the trust anchor of the relying party's trust chain accepts for that type in its
  `trust_mark_issuers`;
- verifies with the federation keys of its issuer: the trust anchor's own keys, or the keys of the issuer's entity
  configuration, whose trust chain must end at the same trust anchor. A trust mark signed with a key the issuer has
  since retired verifies with that key as the issuer's historical keys publish it, provided the key was never revoked
  and the trust mark was issued before the key was retired. The historical keys of an issuer are kept for a minute,
  except when they could not be requested: a resolution that has used up its requests verifies the trust mark with the
  current keys of its issuer only, and one that runs out of time or finds no room among the running requests is
  unavailable;
- carries a valid `delegation` from the owner of the type, when the trust anchor names one in `trust_mark_owners`;
- is reported `active` by the issuer's trust mark status endpoint, when the issuer publishes one; a status endpoint or
  historical keys endpoint that cannot be reached for now or answers with a server error makes the resolution
  unavailable rather than the trust mark invalid, unless another trust mark of the type is valid, or the relying party
  lacks a valid trust mark of another required type; the endpoint must be an `https` URL. So does a trust anchor
  that cannot be reached while the trust chain of the issuer is resolved. An issuer whose endpoints keep answering
  with server errors thus keeps the relying parties holding its trust marks from being remembered as unresolved, so
  they are resolved again with each request, each time within the limits of a resolution.

A relying party that lacks a valid trust mark of a required type is not registered, and the reason is reported in the
logs. Trust marks are checked again whenever the registration is refreshed.

An authorization request passed by value travels through the CAS login as part of its service URL, and CAS does not
match service URLs beyond a maximum length, which can be adjusted using CAS properties. A request object that carries
its `trust_chain` is several kilobytes long: relying parties should send it with a
[pushed authorization request](OIDC-Authentication-PAR.html), as OpenID Federation suggests, or the limit must be
raised.
