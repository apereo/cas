# OpenID Connect Federation Protocol

Source: https://apereo.github.io/cas/development/authentication/OIDC-Authentication-Federation.html

The CAS server supports the [OpenID Federation protocol](https://openid.net/specs/openid-federation-1_0.html).

It can act as only one of the following roles (mutually exclusive):

- Trust Anchor (TA)
- Intermediate
- OpenID Provider (OP)

Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-oidc-federation"
}
```

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oidc.federation.authority-hints` (Required; type `List<String>`): The List of federation authorities where this entity is enrolled, i.e. its immediate superiors. This is used to generate the federation metadata. When no trust anchors are defined, the entries are also considered trust anchors when validating incoming federation requests from relying parties.
- `cas.authn.oidc.federation.contacts` (Required; type `List<String>`): List of contacts to include in the federation metadata.
- `cas.authn.oidc.federation.jwks-file` (Required; type `String`; default `file:/etc/cas/config/federation-keystore.jwks`): Path to the JWKS file resource used to handle signing of federation metadata. Note that if the keystore file does not exist at the specified path, one will be generated for you, with a current and a future key. Keys are rotated and retired in this file, so it must be writable when rotation or revocation is used.
- `cas.authn.oidc.federation.organization` (Required; type `String`; default `Apereo CAS`): Organization name to include in the federation metadata.
- `cas.authn.oidc.federation.role` (Required; type `OidcFederationRole`): The role of this server. Available values are as follows: - `TRUST_ANCHOR`: The root of trust entity issuing statements about other entities. - `INTERMEDIATE`: Intermediate entity between the trust anchor and the leaf entities. - `OPENID_PROVIDER`: Leaf entity playing the OpenID Connect provider protocol role.
- `cas.authn.oidc.federation.entity-statement-expiration` (Optional; type `String`; default `P90D`): Sets the time period during which the entity statement is not expired.
- `cas.authn.oidc.federation.issued-trust-marks` (Optional; type `List<OidcFederationIssuedTrustMarkProperties>`)
- `cas.authn.oidc.federation.issued-trust-marks[0].delegation` (Optional; type `String`)
- `cas.authn.oidc.federation.issued-trust-marks[0].expiration` (Optional; type `String`; default `P30D`)
- `cas.authn.oidc.federation.issued-trust-marks[0].logo-uri` (Optional; type `String`)
- `cas.authn.oidc.federation.issued-trust-marks[0].ref` (Optional; type `String`)
- `cas.authn.oidc.federation.issued-trust-marks[0].subjects` (Optional; type `List<String>`)
- `cas.authn.oidc.federation.issued-trust-marks[0].trust-mark-type` (Optional; type `String`)
- `cas.authn.oidc.federation.max-authority-hints` (Optional; type `Integer`; default `5`)
- `cas.authn.oidc.federation.max-concurrent-resolutions` (Optional; type `Integer`; default `10`)
- `cas.authn.oidc.federation.max-path-length` (Optional; type `Integer`; default `3`)
- `cas.authn.oidc.federation.max-requests-per-resolution` (Optional; type `Integer`; default `50`)
- `cas.authn.oidc.federation.required-trust-marks` (Optional; type `List<String>`)
- `cas.authn.oidc.federation.resolution-timeout` (Optional; type `String`; default `PT10S`)
- `cas.authn.oidc.federation.resolve-endpoint-authentication` (Optional; type `OidcFederationEndpointAuthenticationModes`; default `none`)
- `cas.authn.oidc.federation.revocation.schedule.cron-expression` (Optional; type `String`)
- `cas.authn.oidc.federation.revocation.schedule.cron-time-zone` (Optional; type `String`)
- `cas.authn.oidc.federation.revocation.schedule.enabled` (Optional; type `Boolean`; default `true`)
- `cas.authn.oidc.federation.revocation.schedule.enabled-on-host` (Optional; type `String`; default `.*`)
- `cas.authn.oidc.federation.revocation.schedule.repeat-interval` (Optional; type `String`; default `PT2M`)
- `cas.authn.oidc.federation.revocation.schedule.start-delay` (Optional; type `String`; default `PT15S`)
- `cas.authn.oidc.federation.rotation.schedule.cron-expression` (Optional; type `String`)
- `cas.authn.oidc.federation.rotation.schedule.cron-time-zone` (Optional; type `String`)
- `cas.authn.oidc.federation.rotation.schedule.enabled` (Optional; type `Boolean`; default `true`)
- `cas.authn.oidc.federation.rotation.schedule.enabled-on-host` (Optional; type `String`; default `.*`)
- `cas.authn.oidc.federation.rotation.schedule.repeat-interval` (Optional; type `String`; default `PT2M`)
- `cas.authn.oidc.federation.rotation.schedule.start-delay` (Optional; type `String`; default `PT15S`)
- `cas.authn.oidc.federation.subordinate-directory` (Optional; type `String`): For a TA or intermediate, the file directory where the subordinates are stored.
- `cas.authn.oidc.federation.trust-anchors` (Optional; type `List<OidcFederationTrustAnchorProperties>`)
- `cas.authn.oidc.federation.trust-anchors[0].entity-id` (Optional; type `String`)
- `cas.authn.oidc.federation.trust-anchors[0].jwks` (Optional; type `String`)
- `cas.authn.oidc.federation.trust-mark-endpoint-authentication` (Optional; type `OidcFederationEndpointAuthenticationModes`; default `none`)
- `cas.authn.oidc.federation.trust-mark-issuers` (Optional; type `List<OidcFederationTrustMarkIssuerProperties>`)
- `cas.authn.oidc.federation.trust-mark-issuers[0].issuers` (Optional; type `List<String>`)
- `cas.authn.oidc.federation.trust-mark-issuers[0].trust-mark-type` (Optional; type `String`)
- `cas.authn.oidc.federation.trust-mark-owners` (Optional; type `List<OidcFederationTrustMarkOwnerProperties>`)
- `cas.authn.oidc.federation.trust-mark-owners[0].jwks` (Optional; type `String`)
- `cas.authn.oidc.federation.trust-mark-owners[0].owner` (Optional; type `String`)
- `cas.authn.oidc.federation.trust-mark-owners[0].trust-mark-type` (Optional; type `String`)
- `cas.authn.oidc.federation.trust-marks` (Optional; type `List<String>`)
- `cas.authn.oidc.federation.unresolved-entity-retention` (Optional; type `String`; default `PT5M`)

## Subordinates

In OpenID Connect Federation, a subordinate is an entity that is “below” another federation entity in the trust hierarchy. Simply put, a subordinate is an entity whose federation metadata and trust relationship are vouched for by a higher-level entity in the federation.

More concretely, a subordinate is an entity for which a superior entity, such as a Trust Anchor or Intermediate Entity, can issue a Subordinate Statement. That statement is a signed Entity Statement saying, in effect:

> “I, the issuer, make authoritative claims about this subject entity.”

The `iss` is the superior, and the `sub` is the subordinate.

```
Trust Anchor
  └── Intermediate Entity
        └── OpenID Provider
        └── Relying Party
```

For a trust anchor or intermediate, the supported subordinates must be defined as JSON files with the `OidcFederationSubordinate` format:

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

The `metadata` and `federationKeys` properties come both from the data in the `.well-known/openid-federation` endpoint. The `federationKeys` section is **mandatory** in the federation entity definitions, while `metadata` is optional. Metadata in a subordinate statement takes precedence over the metadata the entity publishes itself, so an empty object for an entity type, such as `"openid_relying_party": {}`, leaves the entity’s own metadata in effect.

### Metadata Policies

A subordinate definition may also carry a metadata policy, which CAS publishes in its subordinate statement as [OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-6.1) describes, to restrict or adjust the metadata of the subordinate and of every entity below it. Policies use the standard operators `value`, `add`, `default`, `one_of`, `subset_of`, `superset_of` and `essential`; operators beyond those are ignored by entities that do not understand them, unless they are listed as critical:

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

A metadata policy that CAS cannot read, such as one that combines operators in a way the specification forbids, is not published. Subordinates are listed for the entity types of their metadata as well as of their metadata policy.

A subordinate definition may also carry `constraints`, published as they are in its subordinate statement, as [OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-6.2) describes, to bound the trust chains that go through the subordinate:

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

The fetch, list and trust mark status endpoints of a subordinate are left out of the `federation_entity` metadata of its subordinate statement, since they may only be published in the entity configuration of the subordinate.

Signed entity statements are returned from the `/oidc/fetch` endpoint, depending on the requested subordinate (`sub` request parameter). A subordinate statement carries the keys of the subordinate only. An unknown subordinate is answered with `404` and `not_found`, a missing `sub` or the entity identifier of CAS itself with `400` and `invalid_request`, and a subordinate defined without keys, with a metadata policy CAS cannot read, or with constraints that are not an object, with `500` and `server_error`; entities that issue no subordinate statements answer `404`.

The `/oidc/list` endpoint lists the subordinates, and can filter them by entity type, with one or more `entity_type` request parameters, and to intermediates only, with `intermediate=true`. A subordinate has the entity types its definition lists in `entityTypes`, or, when it lists none, the entity types of its `metadata`; a metadata policy for an entity type does not give the subordinate that type, so a definition that only carries a policy should list its types. Filtering by trust marks is not supported and is answered with `unsupported_parameter`.

### Resolve Endpoint

As a trust anchor or an intermediate, CAS resolves entities of the federation for others at its `/oidc/resolve` endpoint, advertised as `federation_resolve_endpoint`, as described by [OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-8.3). A `GET` request names the entity with `sub`, one or more trust anchors with `trust_anchor` and, optionally, the entity types whose metadata is wanted with one or more `entity_type`; all entity types are returned otherwise.

Requests are not authenticated by default. Client authentication can be made optional or required using CAS properties, as described by [OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-8.8): an authenticated request is a `POST` with the same parameters and a `private_key_jwt` client assertion, signed with a federation key of a subordinate of CAS as its subordinate definition holds it, whose `iss` and `sub` are the subordinate, whose `aud` is only the entity identifier of CAS, and which carries `jti` and `exp`; each client assertion is accepted once. The resolve response is then addressed to the subordinate with `aud`. The entity configuration advertises the accepted methods as `federation_resolve_endpoint_auth_methods`. A `GET` when authentication is required, or a request that does not authenticate, is answered with `401` and `invalid_client`; a `POST` when authentication is not supported with `400` and `invalid_request`.

- A trust anchor resolves entities to itself. An intermediate resolves them to its [trust anchors](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-Federation.html#trust-anchors), or to its authority hints when no trust anchor is defined. Requested trust anchors that CAS does not trust are ignored.
- CAS resolves the trust chains of the entity with the same bounds on authority hints and path length as for automatic registration, and builds its own statements itself rather than requesting them from its own endpoints. The shortest trust chain that satisfies the constraints of its statements and is not signed with a key its trust anchor has revoked is used, ending with the entity configuration of the trust anchor.
- The metadata of each entity type is resolved with the metadata policies of the trust chain. Entity types the constraints of the chain do not allow are left out.
- Only the trust marks of the entity that are valid, issued by issuers the trust anchor accepts for their type and active, are included. The response expires with the trust chain, or with the first of those trust marks to expire. A trust mark that cannot be checked for now, such as when its status endpoint cannot be reached, is left out, and the response is then not remembered.
- The response is a JWT of type `resolve-response+jwt`, signed with the current federation key.
- Resolved trust chains, and entities that could not be resolved, are remembered per entity and trust anchors for the period that applies to entity identifiers that could not be resolved, or until the chain or a trust mark expires, so that repeated requests do not resolve them again. That period can be tuned using CAS properties.
- Errors are answered as JSON: a missing or malformed parameter with `400` and `invalid_request`; trust anchors CAS does not trust with `404` and `invalid_trust_anchor`; an entity without a usable trust chain to them with `400` and `invalid_trust_chain`; metadata the policies of the chain cannot apply to with `400` and `invalid_metadata`; a resolution that cannot start because too many are running, or that cannot tell about a trust anchor that cannot be reached, answers with a server error or whose historical keys cannot be read, with `503` and `temporarily_unavailable`, which is not remembered. An OpenID Provider answers `404` and `not_found`.

## Entity Configuration

The entity identifier of CAS is its issuer, without a trailing slash, in every role.

CAS publishes its entity configuration at `.well-known/openid-federation` under its issuer, signed with its federation keys, whatever its role. As an OpenID Provider, it describes itself as `openid_provider` with its discovery metadata, which advertises `automatic` as the only client registration type; plain OpenID Connect deployments without the federation role do not advertise client registration types. When CAS also [issues verifiable credentials](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-Verifiable-Credentials.html), the entity configuration describes it as `openid_credential_issuer`, with the same metadata as its credential issuer metadata endpoint, and as `oauth_authorization_server`, which the credential issuer names as its authorization server, so that wallets of federation-based ecosystems find and trust the credential issuer through its trust chain ([OpenID Federation for Wallet Architectures](https://openid.net/specs/openid-federation-wallet-1_0.html)). When CAS presents itself to wallets as a verifier with the `openid_federation` client identifier prefix, the entity configuration also describes it as `openid_credential_verifier`, with its keys and response URI, as described in [verifiable presentations](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-Verifiable-Credentials.html#openid-federation-verifier). The credential issuer and authorization server entity types require the credential issuer of CAS to be the entity identifier; otherwise they are left out, with a warning.

Trust marks that trust mark issuers have issued to CAS are published in the entity configuration as `trust_marks`. Each trust mark is a signed JWT, given as a file, a URL or the JWT itself; files and URLs are read again each time the entity configuration is produced, so a renewed trust mark is published without a restart. The trust mark type is taken from the trust mark, and a trust mark that is about another entity, names no type or has expired is not published and is reported in the logs. Trust marks are not verified by CAS, which leaves that to the parties that resolve it, and can be defined using CAS properties.

As a trust anchor, CAS publishes in its entity configuration the trust mark types of the federation and the entities it accepts as their issuers, as `trust_mark_issuers`. A trust mark type listed without issuers may be issued by anyone. Both can be defined using CAS properties. A trust anchor also publishes, as `trust_mark_owners`, the owners of trust mark types whose issuance they delegate to other issuers, each with the keys their delegations are verified with; only the public members of those keys are published.

### Trust Mark Issuer

In any role, CAS can issue trust marks, as described by [OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-7). Each trust mark type is defined with the entities it is issued to, how long its trust marks are valid, and optionally a logo, a reference to human-readable information and a delegation from the owner of the type, all of which can be defined using CAS properties. Nothing is stored: a trust mark is signed with the current federation key when it is requested. When CAS issues trust marks, its entity configuration advertises three endpoints, which do not require authentication by default:

- The trust mark endpoint answers a request for a trust mark type and an entity with a trust mark of that type about that entity, typed `trust-mark+jwt`, or with `404` when the entity does not hold that type.
- The trust mark status endpoint answers the status of a trust mark it was sent, in a signed status response: `active`; `expired` once the trust mark has expired; `revoked` when its subject no longer holds the type; or `invalid` when it is not typed as a trust mark or does not verify with the federation keys CAS publishes. A trust mark issued by another entity, or of a type CAS does not issue, is answered with `404`.
- The trust marked entities listing endpoint lists the entities that hold a trust mark type, optionally only the one asked for.

Requests to the trust mark endpoint can also be authenticated, as [OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-8.8) allows, so that entities only obtain their own trust marks. Client authentication at that endpoint can be left off, which is the default, allowed, or required, using CAS properties, and the entity configuration advertises the choice along with the signing algorithms CAS accepts. An authenticated request is a `POST` that authenticates with `private_key_jwt`: the client assertion is signed with a federation key of the requester, as its [subordinate definition](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-Federation.html#subordinates) holds it; its issuer and subject are the requester; its only audience is the entity identifier of CAS; and it is accepted once, until it expires. Only subordinates of CAS as a trust anchor or intermediate can therefore authenticate, and they may only ask for their own trust marks. A request that fails to authenticate, or asks for the trust mark of another entity, is answered with `401` and `invalid_client`, as is an unauthenticated request when client authentication is required.

Removing an entity from a trust mark type revokes the trust marks of that type it holds. A trust mark signed with a federation key that has since been retired stays `active` for the rest of its life, since the historical keys keep it verifiable, as [OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-8.7) intends; it becomes `invalid` once that key is revoked.

## Key Rollover

CAS signs its entity configuration and the statements it issues with its federation keys, which are kept in their own keystore and go through the same lifecycle as the [OpenID Connect keys](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-JWKS-Rotation.html), as [OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-11.2) describes for rolling keys over:

- A new keystore starts with a current key, which signs, and a future key, which is only published. A keystore that holds a single key without a lifecycle state keeps working; its key is current. A keystore that holds no keys is given new ones, and one that cannot be read stops CAS from starting rather than being replaced.
- The entity configuration publishes the current, future and previous keys, so a key is known to the federation before it starts signing and after it stopped.
- Rotation makes the future key current, the current key previous, and publishes a new future key. When the keystore has no future key yet, rotation only publishes one, and the next rotation makes it current.
- Revocation retires the previous keys: they leave the entity configuration and are kept, without their private keys, as historical keys, with the time they were retired as their expiration.
- Every role publishes its historical keys at the federation historical keys endpoint, advertised in its entity configuration, as a JWK set signed with the current key.
- A key can also be revoked, for instance when its private key is believed compromised. A key that is still published leaves the entity configuration at once and is published as a historical key that expires at the time of the revocation and is marked `revoked`, with that time and the reason, such as `compromised`, `superseded` or a reason defined by the federation. A key that was already retired keeps its expiration and is marked revoked as well. When the revoked key was signing, the future key starts signing, or a new key when there is none, and a new future key is published. A key that signs without having been published before is unknown to superiors until they update their subordinate statements about CAS.

Rotation and revocation can run on a schedule, which can be defined using CAS properties, or be triggered on demand, which is also how a key is revoked. A scheduled run only changes keys once they are old enough, at half the time between two runs of its schedule: a scheduled rotation makes the future key current only once that key has been published that long, and a scheduled revocation retires a previous key only once it has been previous that long. A schedule that runs again soon after another run leaves the keys as they are: after a restart, or on another node that shares the keystore file and reads it after the change. Nodes whose schedules run at the very same moment may each change the keys, so in a cluster the schedules are best enabled on a single node. On demand, rotation and revocation apply at once:

`oidcFederationJwks`

CAS endpoint · 4 operations · Not exposed by default · cas-server-support-oidc-federation

**GET** `oidcFederationJwks`: Report the published and historical OpenID Federation keys. (no params)

Response

- **Returns**: `ResponseEntity`
- **Produces**: `application/json`
- **Java method**: `keys() `
- **Defined in**: `OidcFederationJwksEndpoint`

Example request

```bash
curl -u "casuser:password" \
  -H "Accept: application/json" \
  "https://sso.example.org/cas/actuator/oidcFederationJwks"
```

**POST** `oidcFederationJwks/revoke`: Retire the previous OpenID Federation keys, which move to the historical keys. (no params)

Response

- **Returns**: `ResponseEntity`
- **Produces**: `application/json`
- **Java method**: `revoke() `
- **Defined in**: `OidcFederationJwksEndpoint`

Example request

```bash
curl -u "casuser:password" \
  -X POST \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  "https://sso.example.org/cas/actuator/oidcFederationJwks/revoke"
```

**POST** `oidcFederationJwks/revoke/{kid}`: Revoke an OpenID Federation key, which is published as a revoked historical key. (2 params)

Parameters

| Name | In | Type | Description |
| --- | --- | --- | --- |
| `kid`required | path | – | The key identifier |
| `reason` | query | – | The reason: compromised, superseded, unspecified or a reason of the federation |

Response

- **Returns**: `ResponseEntity`
- **Produces**: `application/json`
- **Java method**: `revokeKey(String,String) `
- **Defined in**: `OidcFederationJwksEndpoint`

Example request

```bash
curl -u "casuser:password" \
  -X POST \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  "https://sso.example.org/cas/actuator/oidcFederationJwks/revoke/{kid}?reason=..."
```

**POST** `oidcFederationJwks/rotate`: Rotate the OpenID Federation keys: the future key becomes current, and a new future key is published. (no params)

Response

- **Returns**: `ResponseEntity`
- **Produces**: `application/json`
- **Java method**: `rotate() `
- **Defined in**: `OidcFederationJwksEndpoint`

Example request

```bash
curl -u "casuser:password" \
  -X POST \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  "https://sso.example.org/cas/actuator/oidcFederationJwks/rotate"
```

**Enable & expose**

Include the module that provides this endpoint in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-oidc-federation"
}
```

Turn the endpoint on and expose it over the web. One entry covers every operation. By default only `info`, `health` and `status` are exposed.

```properties
management.endpoint.oidcFederationJwks.access=UNRESTRICTED
management.endpoints.web.exposure.include=oidcFederationJwks
```

Endpoints may be mapped to other paths. For example, to serve `health` at `healthcheck`:

```properties
management.endpoints.web.path-mapping.health=healthcheck
```

**Security**

Once exposed, every call goes through [Spring Security](https://spring.io/projects/spring-security), and CAS decides access per endpoint. Without a rule, the special `defaults` endpoint applies, which denies access.

Who may call it

*Authenticated*

```properties
cas.monitor.endpoints.endpoint.oidcFederationJwks.access=AUTHENTICATED
```

*Role*

```properties
cas.monitor.endpoints.endpoint.oidcFederationJwks.access=ROLE
cas.monitor.endpoints.endpoint.oidcFederationJwks.required-roles=ADMIN
```

*IP address*

```properties
cas.monitor.endpoints.endpoint.oidcFederationJwks.access=IP_ADDRESS
cas.monitor.endpoints.endpoint.oidcFederationJwks.required-ip-addresses=127.0.0.1
```

*Anyone*

```properties
cas.monitor.endpoints.endpoint.oidcFederationJwks.access=PERMIT
```

Where credentials are checked

*Basic*

A single user defined in CAS settings. If the name and password are left undefined, CAS generates them at startup.

- `spring.security.user.name` (Third party; type `String`; default `user`): Default user name.
- `spring.security.user.password` (Third party; type `String`): Password for the default user name.
- `spring.security.user.roles` (Third party; type `List<String>`): Granted roles for the default user name.

*JAAS*

- `cas.monitor.endpoints.jaas.login-config` (Optional; type `Resource`): JAAS login resource file.
- `cas.monitor.endpoints.jaas.login-context-name` (Optional; type `String`): The login context name should coincide with a given index in the login config specified. This name is used as the index to the configuration specified in the login config property. ``` JAASTest { org.springframework.security.authentication.jaas.TestLoginModule required; }; ``` In the above example, `JAASTest` should be set as the context name.
- `cas.monitor.endpoints.jaas.refresh-configuration-on-startup` (Optional; type `Boolean`; default `true`): If set, a call to `Configuration#refresh()` will be made by `#configureJaas(Resource)` method.

*LDAP*

- `cas.monitor.endpoints.ldap.base-dn` (Required; type `String`): Base DN to use. There may be scenarios where different parts of a single LDAP tree could be considered as base-dns. Rather than duplicating the LDAP configuration block for each individual base-dn, each entry can be specified and joined together using a special delimiter character. The user DN is retrieved using the combination of all base-dn and DN resolvers in the order defined. DN resolution should fail if multiple DNs are found. Otherwise the first DN found is returned. Usual syntax is: `subtreeA,dc=example,dc=net|subtreeC,dc=example,dc=net`.
- `cas.monitor.endpoints.ldap.bind-credential` (Required; type `String`): The bind credential to use when connecting to LDAP.
- `cas.monitor.endpoints.ldap.bind-dn` (Required; type `String`): The bind DN to use when connecting to LDAP. LDAP connection configuration injected into the LDAP connection pool can be initialized with the following parameters: - `bindDn/bindCredential` provided - Use the provided credentials to bind when initializing connections. - `bindDn/bindCredential` set to `*` - Use a fast-bind strategy to initialize the pool. - `bindDn/bindCredential` set to blank - Skip connection initializing; perform operations anonymously. - SASL mechanism provided - Use the given SASL mechanism to bind when initializing connections.
- `cas.monitor.endpoints.ldap.ldap-url` (Required; type `String`): The LDAP url to the server. More than one may be specified, separated by space and/or comma.
- `cas.monitor.endpoints.ldap.search-filter` (Required; type `String`): User filter to use for searching. Syntax is `cn={user}` or `cn={0}`. You may also provide an external groovy script in the syntax of `file:/path/to/GroovyScript.groovy` to fully build the final filter template dynamically.
- `cas.monitor.endpoints.ldap.type` (Required; type `AbstractLdapAuthenticationProperties.AuthenticationTypes`; default `AUTHENTICATED`): The authentication type. - `AD` - Users authenticate with `sAMAccountName`. - `AUTHENTICATED` - Manager bind/search type of authentication. If {@code} principalAttributePassword} is empty then a user simple bind is done to validate credentials. Otherwise the given attribute is compared with the given `principalAttributePassword` using the `SHA` encrypted value of it. - `ANONYMOUS`: Similar semantics as `AUTHENTICATED` except no `bindDn` and `bindCredential` may be specified to initialize the connection. If `principalAttributePassword` is empty then a user simple bind is done to validate credentials. Otherwise the given attribute is compared with the given `principalAttributePassword` using the `SHA` encrypted value of it. - DIRECT: Direct Bind - Compute user DN from format string and perform simple bind. This is relevant when no search is required to compute the DN needed for a bind operation. Use cases for this type are: 1) All users are under a single branch in the directory, `e.g. ou=Users,dc=example,dc=org.` 2) The username provided on the CAS login form is part of the DN, e.g. `uid=%s,ou=Users,dc=example,dc=org`. Available values are as follows: - `AD`: Active Directory. - `AUTHENTICATED`: Authenticated Search. - `DIRECT`: Direct Bind. - `ANONYMOUS`: Anonymous Search.
- `cas.monitor.endpoints.ldap.allow-multiple-dns` (Optional; type `Boolean`; default `false`): Whether search/query results are allowed to match on multiple DNs, or whether a single unique DN is expected for the result.
- `cas.monitor.endpoints.ldap.allow-multiple-entries` (Optional; type `Boolean`; default `false`): Set if multiple Entries are allowed.
- `cas.monitor.endpoints.ldap.binary-attributes` (Optional; type `List<String>`): Indicate the collection of attributes that are to be tagged and processed as binary attributes by the underlying search resolver.
- `cas.monitor.endpoints.ldap.block-wait-time` (Optional; type `String`; default `PT3S`): The length of time the pool will block. By default the pool will block indefinitely and there is no guarantee that waiting threads will be serviced in the order in which they made their request. This option should be used with a blocking connection pool when you need to control the exact number of connections that can be created
- `cas.monitor.endpoints.ldap.connect-timeout` (Optional; type `String`; default `PT5S`): Sets the maximum amount of time that connects will block.
- `cas.monitor.endpoints.ldap.connection-strategy` (Optional; type `String`): If multiple URLs are provided as the ldapURL this describes how each URL will be processed. - `ACTIVE_PASSIVE` First LDAP will be used for every request unless it fails and then the next shall be used. - `ROUND_ROBIN` For each new connection the next url in the list will be used. - `RANDOM` For each new connection a random LDAP url will be selected. - `DNS_SRV` LDAP urls based on DNS SRV records of the configured/given LDAP url will be used.
- `cas.monitor.endpoints.ldap.deref-aliases` (Optional; type `String`): Define how aliases are de-referenced. Accepted values are: - `NEVER` - `SEARCHING`: dereference when searching the entries beneath the starting point but not when searching for the starting entry. - `FINDING`: dereference when searching for the starting entry but not when searching the entries beneath the starting point. - `ALWAYS`: dereference when searching for the starting entry and when searching the entries beneath the starting point.
- `cas.monitor.endpoints.ldap.disable-pooling` (Optional; type `Boolean`; default `false`): Whether to use a pooled connection factory in components.
- `cas.monitor.endpoints.ldap.dn-format` (Optional; type `String`): Specify the dn format accepted by the AD authenticator, etc. Example format might be `uid=%s,ou=people,dc=example,dc=org`.
- `cas.monitor.endpoints.ldap.enhance-with-entry-resolver` (Optional; type `Boolean`; default `true`): Whether specific search entry resolvers need to be set on the authenticator, or the default should be used.
- `cas.monitor.endpoints.ldap.fail-fast` (Optional; type `Boolean`; default `true`): Attempt to populate the connection pool early on startup and fail quickly if something goes wrong.
- `cas.monitor.endpoints.ldap.follow-referrals` (Optional; type `Boolean`; default `true`): Set if search referrals should be followed.
- `cas.monitor.endpoints.ldap.hostname-verifier` (Optional; type `AbstractLdapProperties.LdapHostnameVerifierOptions`; default `DEFAULT`): Hostname verification options. Available values are as follows: - `DEFAULT`: Default option, forcing verification. - `ANY`: Skip hostname verification and allow all.
- `cas.monitor.endpoints.ldap.idle-time` (Optional; type `String`; default `PT10M`): Removes connections from the pool based on how long they have been idle in the available queue. Prunes connections that have been idle for more than the indicated amount.
- `cas.monitor.endpoints.ldap.keystore` (Optional; type `String`): Path to the keystore used for SSL connections. Typically contains SSL certificates for the LDAP server.
- `cas.monitor.endpoints.ldap.keystore-password` (Optional; type `String`): Keystore password.
- `cas.monitor.endpoints.ldap.keystore-type` (Optional; type `String`): The type of keystore. `PKCS12` or `JKS`. If left blank, defaults to the default keystore type indicated by the underlying Java platform.
- `cas.monitor.endpoints.ldap.ldap-authz.allow-multiple-results` (Optional; type `Boolean`; default `false`): Indicate whether the LDAP search query is allowed to return multiple entries.
- `cas.monitor.endpoints.ldap.ldap-authz.base-dn` (Optional; type `String`): Base DN to start the search.
- `cas.monitor.endpoints.ldap.ldap-authz.group-attribute` (Optional; type `String`): Attribute expected to be found on the entry resulting from the group search whose value is going to be used to construct roles. The final value is always prefixed with `#groupPrefix`. This is useful in scenarios where you wish to grant access to a resource to all users who a member of a given group.
- `cas.monitor.endpoints.ldap.ldap-authz.group-base-dn` (Optional; type `String`): Base DN to start the search looking for groups.
- `cas.monitor.endpoints.ldap.ldap-authz.group-filter` (Optional; type `String`): Search filter to begin looking for groups.
- `cas.monitor.endpoints.ldap.ldap-authz.group-prefix` (Optional; type `String`): A prefix that is prepended to the group attribute value to construct an authorized role.
- `cas.monitor.endpoints.ldap.ldap-authz.role-attribute` (Optional; type `String`; default `uugid`): Attribute expected to be found on the entry whose value is going to be used to construct roles. The final value is always prefixed with `#rolePrefix`. This is useful in scenarios where you wish to grant access to a resource to all users who carry a special attribute.
- `cas.monitor.endpoints.ldap.ldap-authz.role-prefix` (Optional; type `String`; default `ROLE_`): Prefix for the role.
- `cas.monitor.endpoints.ldap.ldap-authz.search-filter` (Optional; type `String`): LDAP search filter to locate accounts.
- `cas.monitor.endpoints.ldap.max-pool-size` (Optional; type `Integer`; default `10`): Maximum LDAP connection pool size which the pool can use to grow.
- `cas.monitor.endpoints.ldap.min-pool-size` (Optional; type `Integer`; default `3`): Minimum LDAP connection pool size. Size the pool should be initialized to and pruned to
- `cas.monitor.endpoints.ldap.name` (Optional; type `String`): Name of the LDAP handler.
- `cas.monitor.endpoints.ldap.page-size` (Optional; type `Integer`; default `0`): Request that the server return results in batches of a specific size. See [RFC 2696](http://www.ietf.org/rfc/rfc2696.txt). This control is often used to work around server result size limits. A negative/zero value disables paged requests.
- `cas.monitor.endpoints.ldap.pool-passivator` (Optional; type `String`; default `BIND`): You may receive unexpected LDAP failures, when CAS is configured to authenticate using `DIRECT` or `AUTHENTICATED` types and LDAP is locked down to not allow anonymous binds/searches. Every second attempt with a given LDAP connection from the pool would fail if it was on the same connection as a failed login attempt, and the regular connection validator would similarly fail. When a connection is returned back to a pool, it still may contain the principal and credentials from the previous attempt. Before the next bind attempt using that connection, the validator tries to validate the connection again but fails because it’s no longer trying with the configured bind credentials but with whatever user DN was used in the previous step. Given the validation failure, the connection is closed and CAS would deny access by default. Passivators attempt to reconnect to LDAP with the configured bind credentials, effectively resetting the connection to what it should be after each bind request. Furthermore if you are seeing errors in the logs that resemble a 'Operation exception encountered, reopening connection' type of message, this usually is an indication that the connection pool’s validation timeout established and created by CAS is greater than the timeout configured in the LDAP server, or more likely, in the load balancer in front of the LDAP servers. You can adjust the LDAP server session’s timeout for connections, or you can teach CAS to use a validity period that is equal or less than the LDAP server session’s timeout. Accepted values are: - `NONE`: No passivation takes place. - `BIND`: The default behavior which passivates a connection by performing a bind operation on it. This option requires the availability of bind credentials when establishing connections to LDAP.
- `cas.monitor.endpoints.ldap.principal-attribute-password` (Optional; type `String`): If principalAttributePassword is empty then a user simple bind is done to validate credentials otherwise the given attribute is compared with the given principalAttributePassword using the SHA encrypted value of it. For the anonymous authentication type, if principalAttributePassword is empty then a user simple bind is done to validate credentials otherwise the given attribute is compared with the given principalAttributePassword using the SHA encrypted value of it.
- `cas.monitor.endpoints.ldap.prune-period` (Optional; type `String`; default `PT2H`): Removes connections from the pool based on how long they have been idle in the available queue. Run the pruning process at the indicated interval.
- `cas.monitor.endpoints.ldap.resolve-from-attribute` (Optional; type `String`): If this attribute is set, the value found in the first attribute value will be used in place of the DN.
- `cas.monitor.endpoints.ldap.response-timeout` (Optional; type `String`; default `PT5S`): Duration of time to wait for responses.
- `cas.monitor.endpoints.ldap.sasl-authorization-id` (Optional; type `String`): SASL authorization id.
- `cas.monitor.endpoints.ldap.sasl-mechanism` (Optional; type `String`): The SASL mechanism.
- `cas.monitor.endpoints.ldap.sasl-mutual-auth` (Optional; type `Boolean`): Whether SASL mutual authentication is enabled.
- `cas.monitor.endpoints.ldap.sasl-quality-of-protection` (Optional; type `String`): SASL quality of protected.
- `cas.monitor.endpoints.ldap.sasl-realm` (Optional; type `String`): The SASL realm.
- `cas.monitor.endpoints.ldap.sasl-security-strength` (Optional; type `String`): SASL security strength.
- `cas.monitor.endpoints.ldap.search-entry-handlers` (Optional; type `List<LdapSearchEntryHandlersProperties>`): Search handlers.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].case-change.attribute-name-case-change` (Optional; type `String`): The Attribute name case change.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].case-change.attribute-names` (Optional; type `List<String>`): The Attribute names.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].case-change.attribute-value-case-change` (Optional; type `String`): The Attribute value case change.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].case-change.dn-case-change` (Optional; type `String`): The Dn case change.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].dn-attribute.add-if-exists` (Optional; type `boolean`): The Add if exists.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].dn-attribute.dn-attribute-name` (Optional; type `String`; default `entryDN`): The Dn attribute name.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].merge-attribute.attribute-names` (Optional; type `List<String>`): The Attribute names.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].merge-attribute.merge-attribute-name` (Optional; type `String`): The Merge attribute name.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].primary-group-id.base-dn` (Optional; type `String`): The Base dn.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].primary-group-id.group-filter` (Optional; type `String`; default `(&(objectClass=group)(objectSid={0}))`): The Group filter.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].recursive.merge-attributes` (Optional; type `List<String>`): The Merge attributes.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].recursive.search-attribute` (Optional; type `String`): The Search attribute.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].search-referral.limit` (Optional; type `int`; default `10`): The default referral limit.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].search-result.limit` (Optional; type `int`; default `10`): The default referral limit.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].type` (Optional; type `LdapSearchEntryHandlersProperties.SearchEntryHandlerTypes`): The type of search entry handler to choose. Available values are as follows: - `FOLLOW_SEARCH_REFERRAL`: Provides handling of an ldap referral for search operations. - `FOLLOW_SEARCH_RESULT_REFERENCE`: Provides handling of an ldap continuation reference for search operations. - `ACTIVE_DIRECTORY`: Process the entry results fetched from active directory and check for account status controls for disabled/expired accounts, etc. - `OBJECT_GUID`: Object guid search entry handler. Handles the `objectGUID` attribute fetching and conversion. - `OBJECT_SID`: Object sid search entry handler. Handles the `objectSid` attribute fetching and conversion. - `CASE_CHANGE`: Case change search entry handler. Provides the ability to modify the case of search entry DNs, attribute names, and attribute values. - `DN_ATTRIBUTE_ENTRY`: DN attribute entry handler. Adds the entry DN as an attribute to the result set. Provides a client side implementation of RFC 5020. - `MERGE`: Merge search entry handler. Merges the values of one or more attributes into a single attribute. - `PRIMARY_GROUP`: Primary group search handler. Constructs the primary group SID and then searches for that group and puts it's DN in the `memberOf` attribute of the original search entry. - `RANGE_ENTRY`: Range entry search handler. Rewrites attributes returned from Active Directory to include all values by performing additional searches. - `RECURSIVE_ENTRY`: Recursive entry search handler. This recursively searches based on a supplied attribute and merges those results into the original entry. - `MERGE_ENTRIES`: Merge entries handler. Merges the values of one or more attributes in all entries into a single attribute. The merged attribute may or may not already exist on the entry. If it does exist it's existing values will remain intact.
- `cas.monitor.endpoints.ldap.subtree-search` (Optional; type `Boolean`; default `true`): Whether subtree searching is allowed.
- `cas.monitor.endpoints.ldap.trust-certificates` (Optional; type `String`): Path of the trust certificates to use for the SSL connection. Ignores keystore-related settings when activated and used.
- `cas.monitor.endpoints.ldap.trust-manager` (Optional; type `String`): Trust Manager options. Trust managers are responsible for managing the trust material that is used when making LDAP trust decisions, and for deciding whether credentials presented by a peer should be accepted. Accepted values are: - `DEFAULT`: Enable and force the default JVM trust managers. - `ANY`: Trust any client or server.
- `cas.monitor.endpoints.ldap.trust-store` (Optional; type `String`): Path to the keystore used to determine which certificates or certificate authorities should be trusted. Used when connecting to an LDAP server via LDAPS or startTLS connection. If left blank, the default truststore for the Java runtime is used.
- `cas.monitor.endpoints.ldap.trust-store-password` (Optional; type `String`): Password needed to open the truststore.
- `cas.monitor.endpoints.ldap.trust-store-type` (Optional; type `String`): The type of trust keystore that determines which certificates or certificate authorities are trusted. Types depend on underlying java platform, typically `PKCS12` or `JKS`. If left blank, defaults to the default keystore type indicated by the underlying Java platform.
- `cas.monitor.endpoints.ldap.use-start-tls` (Optional; type `Boolean`; default `false`): Whether TLS should be used and enabled when establishing the connection.
- `cas.monitor.endpoints.ldap.validate-on-checkout` (Optional; type `Boolean`; default `true`): Whether connections should be validated when loaned out from the pool.
- `cas.monitor.endpoints.ldap.validate-period` (Optional; type `String`; default `PT5M`): Period at which pool should be validated.
- `cas.monitor.endpoints.ldap.validate-periodically` (Optional; type `Boolean`; default `true`): Whether connections should be validated periodically when the pool is idle.
- `cas.monitor.endpoints.ldap.validate-timeout` (Optional; type `String`; default `PT5S`): Period at which validation operations may time out.
- `cas.monitor.endpoints.ldap.validator.attribute-name` (Optional; type `String`; default `objectClass`): Attribute name to use for the compare validator.
- `cas.monitor.endpoints.ldap.validator.attribute-value` (Optional; type `String`; default `top`): Attribute values to use for the compare validator.
- `cas.monitor.endpoints.ldap.validator.base-dn` (Optional; type `String`): Base DN to use for the search request of the search validator.
- `cas.monitor.endpoints.ldap.validator.dn` (Optional; type `String`): DN to compare to use for the compare validator.
- `cas.monitor.endpoints.ldap.validator.scope` (Optional; type `String`; default `OBJECT`): Search scope to use for the search request of the search validator.
- `cas.monitor.endpoints.ldap.validator.search-filter` (Optional; type `String`; default `(objectClass=*)`): Search filter to use for the search request of the search validator.
- `cas.monitor.endpoints.ldap.validator.type` (Optional; type `String`; default `search`): Determine the LDAP validator type. The following LDAP validators can be used to test connection health status: - `search`: Validates a connection is healthy by performing a search operation. Validation is considered successful if the search result size is greater than zero. - `none`: No validation takes place. - `compare`: Validates a connection is healthy by performing a compare operation.

**LDAP & Active Directory**

**LDAP Scriptable Search Filter**

LDAP search filters can point to an external Groovy script to dynamically construct the final filter template.

The script itself may be designed as:

```groovy
import org.ldaptive.*
import org.springframework.context.*

def run(Object[] args) {
    def (filter,parameters,applicationContext,logger) = args

    logger.info("Configuring LDAP filter")
    filter.setFilter("uid=something")
}
```

The following parameters are passed to the script:

| Parameter | Description |
| --- | --- |
| `filter` | `FilterTemplate` to be updated by the script and used for the LDAP query. |
| `parameters` | Map of query parameters which may be used to construct the final filter. |
| `applicationContext` | Reference to the Spring `ApplicationContext` reference. |
| `logger` | The object responsible for issuing log messages such as `logger.info(...)`. |

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development//integration/Apache-Groovy-Scripting.html).

*JDBC*

- `cas.monitor.endpoints.jdbc.driver-class` (Required; type `String`; default `org.hsqldb.jdbcDriver`): The JDBC driver used to connect to the database.
- `cas.monitor.endpoints.jdbc.password` (Required; type `String`): The database connection password.
- `cas.monitor.endpoints.jdbc.password-encoder.encoding-algorithm` (Required; type `String`): The encoding algorithm to use such as `MD5`. Relevant when the type used is `DEFAULT` or `GLIBC_CRYPT`. When used with `PasswordEncoderTypes#PBKDF2`, it should be one of `PBKDF2WithHmacSHA1`, `PBKDF2WithHmacSHA256` or `PBKDF2WithHmacSHA512`.
- `cas.monitor.endpoints.jdbc.password-encoder.type` (Required; type `String`; default `NONE`): Define the password encoder type to use. Type may be specified as blank or `NONE` to disable password encoding. It may also refer to a fully-qualified class name that implements the Spring Security's `PasswordEncoder` interface if you wish you define your own encoder. The following types may be used: - `NONE`: No password encoding (i.e. plain-text) takes place. - `DEFAULT`: Use the `DefaultPasswordEncoder` of CAS. For message-digest algorithms via `character-encoding` and `encoding-algorithm`. - `BCRYPT`: Use the `BCryptPasswordEncoder` based on the strength provided and an optional secret. - `SCRYPT`: Use the `SCryptPasswordEncoder`. - `PBKDF2`: Use the `Pbkdf2PasswordEncoder` based on the strength provided and an optional secret. - `STANDARD`: Use the `StandardPasswordEncoder` based on the secret provided. - `SSHA`: Use the `LdapShaPasswordEncoder` supports Ldap SHA and SSHA (salted-SHA). The values are base-64 encoded and have the label {SHA} or {SSHA} prepended to the encoded hash. - `GLIBC_CRYPT`: Use the `GlibcCryptPasswordEncoder` based on the `encoding-algorithm`, strength provided and an optional secret. - `org.example.MyEncoder`: An implementation of `PasswordEncoder` of your own choosing. - `file:///path/to/script.groovy`: Path to a Groovy script charged with handling password encoding operations.
- `cas.monitor.endpoints.jdbc.url` (Required; type `String`; default `jdbc:hsqldb:mem:cas-hsql-database`): The database connection URL.
- `cas.monitor.endpoints.jdbc.user` (Required; type `String`; default `sa`): The database user. The database user must have sufficient permissions to be able to handle schema changes and updates, when needed.
- `cas.monitor.endpoints.jdbc.autocommit` (Optional; type `Boolean`; default `false`): The default auto-commit behavior of connections in the pool. Determined whether queries such as update/insert should be immediately executed without waiting for an underlying transaction.
- `cas.monitor.endpoints.jdbc.batch-size` (Optional; type `Integer`; default `100`): A non-zero value enables use of JDBC2 batch updates by Hibernate. e.g. recommended values between 5 and 30.
- `cas.monitor.endpoints.jdbc.connection-timeout` (Optional; type `String`; default `PT30S`): Indicates the maximum number of milliseconds that the service can wait to obtain a connection.
- `cas.monitor.endpoints.jdbc.data-source-name` (Optional; type `String`): Attempts to do a JNDI data source look up for the data source name specified. Will attempt to locate the data source object as is.
- `cas.monitor.endpoints.jdbc.ddl-auto` (Optional; type `String`; default `update`): Hibernate feature to automatically validate and exports DDL to the schema. By default, creates and drops the schema automatically when a session is starts and ends. Setting the value to `validate` or `none` may be more desirable for production, but any of the following options can be used: - `validate`: Validate the schema, but make no changes to the database. - `update`: Update the schema. - `create`: Create the schema, destroying previous data. - `create-drop`: Drop the schema at the end of the session. - `none`: Do nothing. Note that during a version migration where any schema has changed `create-drop` will result in the loss of all data as soon as CAS is started. For transient data like tickets this is probably not an issue, but in cases like the audit table important data could be lost. Using `update`, while safe for data, is confirmed to result in invalid database state. `validate` or `none` settings are likely the only safe options for production use. For more info, [see this](http://docs.spring.io/spring-framework/docs/current/javadoc-api).
- `cas.monitor.endpoints.jdbc.default-catalog` (Optional; type `String`): Qualifies unqualified table names with the given catalog in generated SQL.
- `cas.monitor.endpoints.jdbc.default-schema` (Optional; type `String`): Qualify unqualified table names with the given schema/tablespace in generated SQL.
- `cas.monitor.endpoints.jdbc.dialect` (Optional; type `String`; default `org.hibernate.dialect.HSQLDialect`): The database dialect is a configuration setting for platform independent software (JPA, Hibernate, etc) which allows such software to translate its generic SQL statements into vendor specific DDL, DML.
- `cas.monitor.endpoints.jdbc.fail-fast-timeout` (Optional; type `Long`; default `1`): Set the pool initialization failure timeout. - Any value greater than zero will be treated as a timeout for pool initialization. The calling thread will be blocked from continuing until a successful connection to the database, or until the timeout is reached. If the timeout is reached, then a `PoolInitializationException` will be thrown. - A value of zero will *not* prevent the pool from starting in the case that a connection cannot be obtained. However, upon start the pool will attempt to obtain a connection and validate that the `connectionTestQuery` and `connectionInitSql` are valid. If those validations fail, an exception will be thrown. If a connection cannot be obtained, the validation is skipped and the pool will start and continue to try to obtain connections in the background. This can mean that callers to `DataSource#getConnection()` may encounter exceptions. - A value less than zero will *not* bypass any connection attempt and validation during startup, and therefore the pool will start immediately. The pool will continue to try to obtain connections in the background. This can mean that callers to `DataSource#getConnection()` may encounter exceptions. Note that if this timeout value is greater than or equal to zero (0), and therefore an initial connection validation is performed, this timeout does not override the `connectionTimeout` or `validationTimeout`; they will be honored before this timeout is applied. The default value is one millisecond.
- `cas.monitor.endpoints.jdbc.fetch-size` (Optional; type `Integer`; default `100`): Used to specify number of rows to be fetched in a select query.
- `cas.monitor.endpoints.jdbc.generate-statistics` (Optional; type `Boolean`; default `false`): Allow hibernate to generate query statistics.
- `cas.monitor.endpoints.jdbc.health-query` (Optional; type `String`): The SQL query to be executed to test the validity of connections. This is for "legacy" databases that do not support the JDBC4 `Connection.isValid()` API.
- `cas.monitor.endpoints.jdbc.idle-timeout` (Optional; type `String`; default `PT10M`): Controls the maximum amount of time that a connection is allowed to sit idle in the pool.
- `cas.monitor.endpoints.jdbc.isolate-internal-queries` (Optional; type `Boolean`; default `false`): This property determines whether data source isolates internal pool queries, such as the connection alive test, in their own transaction. Since these are typically read-only queries, it is rarely necessary to encapsulate them in their own transaction. This property only applies if `#autocommit` is disabled.
- `cas.monitor.endpoints.jdbc.isolation-level-name` (Optional; type `String`; default `ISOLATION_READ_COMMITTED`): Defines the isolation level for transactions. @see org.springframework.transaction.TransactionDefinition
- `cas.monitor.endpoints.jdbc.leak-threshold` (Optional; type `String`; default `PT6S`): Controls the amount of time that a connection can be out of the pool before a message is logged indicating a possible connection leak.
- `cas.monitor.endpoints.jdbc.password-encoder.character-encoding` (Optional; type `String`; default `UTF-8`): The encoding algorithm to use such as 'UTF-8'. Relevant when the type used is `DEFAULT`.
- `cas.monitor.endpoints.jdbc.password-encoder.hash-length` (Optional; type `Integer`; default `16`): When used by `PasswordEncoderTypes#ARGON2`, it indicates the hash strength/length.
- `cas.monitor.endpoints.jdbc.password-encoder.iterations` (Optional; type `Integer`; default `310000`): When used by `PasswordEncoderTypes#PBKDF2`, it indicates the required number of iterations.
- `cas.monitor.endpoints.jdbc.password-encoder.secret` (Optional; type `String`): Secret to use with `PasswordEncoderTypes#STANDARD`, `PasswordEncoderTypes#PBKDF2`, `PasswordEncoderTypes#BCRYPT`, `PasswordEncoderTypes#GLIBC_CRYPT` password encoders. Secret usually is an optional setting.
- `cas.monitor.endpoints.jdbc.password-encoder.strength` (Optional; type `Integer`; default `16`): Strength or number of iterations to use for password hashing. Usually relevant when dealing with `PasswordEncoderTypes#BCRYPT`, `PasswordEncoderTypes#PBKDF2` or `PasswordEncoderTypes#GLIBC_CRYPT`. When used by `PasswordEncoderTypes#ARGON2` or `PasswordEncoderTypes#PBKDF2`, it indicates the salt strength.
- `cas.monitor.endpoints.jdbc.physical-naming-strategy-class-name` (Optional; type `String`; default `org.apereo.cas.hibernate.CasHibernatePhysicalNamingStrategy`): Fully-qualified name of the class that can control the physical naming strategy of hibernate.
- `cas.monitor.endpoints.jdbc.pool.keep-alive-time` (Optional; type `String`; default `0`): This property controls the keepalive interval for a connection in the pool. An in-use connection will never be tested by the keepalive thread, only when it is idle will it be tested. Default is zero, which disables this feature.
- `cas.monitor.endpoints.jdbc.pool.max-size` (Optional; type `Integer`; default `18`): Controls the maximum number of connections to keep in the pool, including both idle and in-use connections.
- `cas.monitor.endpoints.jdbc.pool.max-wait` (Optional; type `String`; default `PT2S`): Sets the maximum time in seconds that this data source will wait while attempting to connect to a database. A value of zero specifies that the timeout is the default system timeout if there is one; otherwise, it specifies that there is no timeout.
- `cas.monitor.endpoints.jdbc.pool.maximum-lifetime` (Optional; type `String`; default `PT10M`): This property controls the maximum lifetime of a connection in the pool. When a connection reaches this timeout, even if recently used, it will be retired from the pool. An in-use connection will never be retired, only when it is idle will it be removed.
- `cas.monitor.endpoints.jdbc.pool.min-size` (Optional; type `Integer`; default `6`): Controls the minimum size that the pool is allowed to reach, including both idle and in-use connections.
- `cas.monitor.endpoints.jdbc.pool.name` (Optional; type `String`): Set the name of the connection pool. This is primarily used for the MBean to uniquely identify the pool configuration.
- `cas.monitor.endpoints.jdbc.pool.suspension` (Optional; type `Boolean`; default `false`): Whether or not pool suspension is allowed. There is a performance impact when pool suspension is enabled. Unless you need it (for a redundancy system for example) do not enable it.
- `cas.monitor.endpoints.jdbc.pool.timeout-millis` (Optional; type `Long`; default `1000`): The maximum number of milliseconds that the pool will wait for a connection to be validated as alive.
- `cas.monitor.endpoints.jdbc.propagation-behavior-name` (Optional; type `String`; default `PROPAGATION_REQUIRED`): Defines the propagation behavior for transactions. @see org.springframework.transaction.TransactionDefinition
- `cas.monitor.endpoints.jdbc.properties` (Optional; type `Map<String,String>`): Additional settings provided by Hibernate (or the connection provider) in form of key-value pairs.
- `cas.monitor.endpoints.jdbc.query` (Optional; type `String`): Query to execute in order to authenticate users via JDBC. Example: `SELECT username,password,enabled FROM users WHERE username=?`
- `cas.monitor.endpoints.jdbc.read-only` (Optional; type `Boolean`; default `false`): Configures the Connections to be added to the pool as read-only Connections.
- `cas.monitor.endpoints.jdbc.role-prefix` (Optional; type `String`): Prefix to add to the role.

**Hibernate & JDBC**

Control global properties that are relevant to Hibernate, when CAS attempts to employ and utilize database resources, connections and queries.

- `cas.jdbc.gen-ddl` (Optional; type `Boolean`; default `true`): Whether to generate DDL after the EntityManagerFactory has been initialized creating/updating all relevant tables.
- `cas.jdbc.physical-table-names` (Optional; type `Map<String,String>`): Indicate a physical table name to be used by the hibernate naming strategy in case table names need to be customized for the specific type of database. The key here indicates the CAS-provided table name and the value is the translate physical name for the database. If a match is not found for the CAS-provided table name, then that name will be used by default.
- `cas.jdbc.show-sql` (Optional; type `Boolean`; default `false`): Whether SQL queries should be displayed in the console/logs.

**Password encoding**

If you need to design your own password encoding scheme where the type is specified as a fully qualified Java class name, the structure of the class would be similar to the following:

```java
package org.example.cas;

import org.springframework.security.crypto.codec.*;
import org.springframework.security.crypto.password.*;

public class MyEncoder extends AbstractPasswordEncoder {
    @Override
    protected byte[] encode(CharSequence rawPassword, byte[] salt) {
        return ...
    }
}
```

If you need to design your own password encoding scheme where the type is specified as a path to a Groovy script, the structure of the script would be similar to the following:

```groovy
import java.util.*

byte[] run(final Object... args) {
    def (rawPassword,generatedSalt,logger,applicationContext) = args
    logger.debug("Encoding password...")
    return ...
}

Boolean matches(final Object... args) {
    def (rawPassword,encodedPassword,logger,applicationContext) = args
    logger.debug("Does match or not ?");
    return ...
```

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development//integration/Apache-Groovy-Scripting.html).

*JSON*

A static list of users, passwords and roles in a JSON file. Supported password encodings are `{sha512}`, `{sha256}`, `{bcrypt}`, `{noop}`, `{pbkdf2}`, `{scrypt}` and `{argon2}`.

```json
[
    {
        "username": "casuser",
        "password": "{sha512}<hashed-password>",
        "authorities": [ "ROLE_ADMIN" ]
    }
]
```

- `cas.monitor.endpoints.json.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.

**Settings**

- `cas.monitor.endpoints.endpoint.oidcFederationJwks.access` (Optional; type `ActuatorEndpointProperties.EndpointAccessLevel`; default `DENY`): Define the security access level of the endpoint. Available values are as follows: - `PERMIT`: Allow open access to the endpoint. - `ANONYMOUS`: Allow anonymous access to the endpoint. - `DENY`: Block access to the endpoint. - `AUTHENTICATED`: Require authenticated access to the endpoint. - `ROLE`: Require authenticated access to the endpoint along with a role requirement. - `AUTHORITY`: Require authenticated access to the endpoint along with an authority requirement. - `IP_ADDRESS`: Require authenticated access to the endpoint using a collection of IP addresses.
- `cas.monitor.endpoints.endpoint.oidcFederationJwks.required-authorities` (Optional; type `List<String>`): Required user authorities.
- `cas.monitor.endpoints.endpoint.oidcFederationJwks.required-ip-addresses` (Optional; type `List<String>`): Required IP addresses. CIDR ranges are accepted.
- `cas.monitor.endpoints.endpoint.oidcFederationJwks.required-roles` (Optional; type `List<String>`): Required user roles.
- `cas.monitor.endpoints.form-login-enabled` (Optional; type `Boolean`; default `false`): Control whether access to endpoints can be controlled via form-based login over the web via a special admin login endpoint.

**Spring Boot settings**

- `management.endpoint.health.access` (Third party; type `Access`; default `unrestricted`): Permitted level of access for the health endpoint.
- `management.endpoint.health.cache.time-to-live` (Third party; type `Duration`; default `0ms`): Maximum time that a response can be cached.
- `management.endpoint.health.group` (Third party; type `Map<String,HealthEndpointProperties.Group>`): Health endpoint groups.
- `management.endpoint.health.logging.slow-indicator-threshold` (Third party; type `Duration`; default `10s`): Threshold after which a warning will be logged for slow health indicators.
- `management.endpoint.health.probes.add-additional-paths` (Third party; type `Boolean`; default `false`): Whether to make the liveness and readiness health groups available on the main server port.
- `management.endpoint.health.probes.enabled` (Third party; type `Boolean`; default `true`): Whether to enable liveness and readiness probes.
- `management.endpoint.health.roles` (Third party; type `Set<String>`): Roles used to determine whether a user is authorized to be shown details. When empty, all authenticated users are authorized.
- `management.endpoint.health.show-components` (Third party; type `Show`): When to show components. If not specified the 'show-details' setting will be used.
- `management.endpoint.health.show-details` (Third party; type `Show`; default `never`): When to show full health details.
- `management.endpoint.health.status.http-mapping` (Third party; type `Map<String,Integer>`): Mapping of health statuses to HTTP status codes. By default, registered health statuses map to sensible defaults (for example, UP maps to 200).
- `management.endpoint.health.status.order` (Third party; type `List<String>`; default `["DOWN", "OUT_OF_SERVICE", "UP", "UNKNOWN"]`): List of health statuses in order of severity.
- `management.endpoint.health.validate-group-membership` (Third party; type `Boolean`; default `true`): Whether to validate health group membership on startup. Validation fails if a group includes or excludes a health contributor that does not exist.
- `management.endpoints.access.default` (Third party; type `Access`): Default access level for all endpoints.
- `management.endpoints.access.max-permitted` (Third party; default `unrestricted`): Maximum level of endpoint access that is permitted. Caps an endpoint's individual access level (management.endpoint.\<id>.access) and the default access (management.endpoints.access.default).'
- `management.endpoints.enabled-by-default` (Third party; type `Boolean`; deprecation `WARNING`, replaced by `management.endpoints.access.default`): Whether to enable or disable all endpoints by default.
- `management.endpoints.jackson.isolated-json-mapper` (Third party; type `Boolean`; default `true`): Whether to use an isolated JsonMapper to serialize endpoint JSON.
- `management.endpoints.jackson2.isolated-object-mapper` (Third party; type `Boolean`; deprecation `WARNING`, no replacement; default `true`): Whether to use an isolated object mapper to serialize endpoint JSON.
- `management.endpoints.jmx.domain` (Third party; type `String`; default `org.springframework.boot`): Endpoints JMX domain name. Fallback to 'spring.jmx.default-domain' if set.
- `management.endpoints.jmx.exposure.exclude` (Third party; type `Set<String>`): Endpoint IDs that should be excluded or '*' for all.
- `management.endpoints.jmx.exposure.include` (Third party; type `Set<String>`; default `health`): Endpoint IDs that should be included or '*' for all.
- `management.endpoints.jmx.static-names` (Third party; type `Properties`): Additional static properties to append to all ObjectNames of MBeans representing Endpoints.
- `management.endpoints.jmx.unique-names` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `spring.jmx.unique-names`): Whether unique runtime object names should be ensured.
- `management.endpoints.migrate-legacy-ids` (Third party; type `Boolean`; default `false`): Whether to transparently migrate legacy endpoint IDs.
- `management.endpoints.web.base-path` (Third party; type `String`; default `/actuator`): Base path for Web endpoints. Relative to the servlet context path (server.servlet.context-path) or WebFlux base path (spring.webflux.base-path) when the management server is sharing the main server port. Relative to the management server base path (management.server.base-path) when a separate management server port (management.server.port) is configured.
- `management.endpoints.web.cors.allow-credentials` (Third party; type `Boolean`): Whether credentials are supported. When not set, credentials are not supported.
- `management.endpoints.web.cors.allowed-headers` (Third party; type `List<String>`): List of headers to allow in a request. '*' allows all headers.
- `management.endpoints.web.cors.allowed-methods` (Third party; type `List<String>`): List of methods to allow. '*' allows all methods. When not set, defaults to GET.
- `management.endpoints.web.cors.allowed-origin-patterns` (Third party; type `List<String>`): List of origin patterns to allow. Unlike allowed origins which only supports '*', origin patterns are more flexible (for example 'https://*.example.com') and can be used when credentials are allowed. When no allowed origin patterns or allowed origins are set, CORS support is disabled.
- `management.endpoints.web.cors.allowed-origins` (Third party; type `List<String>`): List of origins to allow. '*' allows all origins. When credentials are allowed, '*' cannot be used and origin patterns should be configured instead. When no allowed origins or allowed origin patterns are set, CORS support is disabled.
- `management.endpoints.web.cors.exposed-headers` (Third party; type `List<String>`): List of headers to include in a response.
- `management.endpoints.web.cors.max-age` (Third party; type `Duration`; default `1800s`): How long the response from a pre-flight request can be cached by clients. If a duration suffix is not specified, seconds will be used.
- `management.endpoints.web.discovery.enabled` (Third party; type `Boolean`; default `true`): Whether the discovery page is enabled.
- `management.endpoints.web.exposure.exclude` (Third party; type `Set<String>`): Endpoint IDs that should be excluded or '*' for all.
- `management.endpoints.web.exposure.include` (Third party; type `Set<String>`; default `["health"]`): Endpoint IDs that should be included or '*' for all.
- `management.endpoints.web.path-mapping` (Third party; type `Map<String,String>`): Mapping between endpoint IDs and the path that should expose them.
- `management.health.binders.enabled` (Third party; type `Boolean`; default `true`): Allows to enable/disable binder's' health indicators. If you want to disable health indicator completely, then set it to `false`.
- `management.health.db.enabled` (Third party; type `Boolean`; default `true`): Whether to enable database health check.
- `management.health.db.ignore-routing-data-sources` (Third party; type `Boolean`; default `false`): Whether to ignore AbstractRoutingDataSources when creating database health indicators.
- `management.health.defaults.enabled` (Third party; type `Boolean`; default `true`): Whether to enable default health indicators.
- `management.health.diskspace.enabled` (Third party; type `Boolean`; default `true`): Whether to enable disk space health check.
- `management.health.diskspace.path` (Third party; type `File`): Path used to compute the available disk space.
- `management.health.diskspace.threshold` (Third party; type `DataSize`; default `10MB`): Minimum disk space that should be available.
- `management.health.influxdb.enabled` (Third party; type `Boolean`; deprecation `ERROR`, no replacement)
- `management.health.livenessstate.enabled` (Third party; type `Boolean`; default `false`): Whether to enable liveness state health check.
- `management.health.mail.enabled` (Third party; type `Boolean`; default `true`): Whether to enable Mail health check.
- `management.health.mongo.enabled` (Third party; deprecation `ERROR`, replaced by `management.health.mongodb.enabled`)
- `management.health.mongodb.enabled` (Third party; type `Boolean`; default `true`): Whether to enable MongoDB health check.
- `management.health.ping.enabled` (Third party; type `Boolean`; default `true`): Whether to enable ping health check.
- `management.health.probes.enabled` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `management.endpoint.health.probes.enabled`; default `false`): Whether to enable liveness and readiness probes.
- `management.health.pubsub.enabled` (Third party; type `Boolean`; default `true`): Whether to enable the Pub/Sub health indicator when used with Spring Boot Actuator.
- `management.health.rabbit.enabled` (Third party; type `Boolean`; default `true`): Whether to enable RabbitMQ health check.
- `management.health.readinessstate.enabled` (Third party; type `Boolean`; default `false`): Whether to enable readiness state health check.
- `management.health.redis.enabled` (Third party; type `Boolean`; default `true`): Whether to enable Redis health check.
- `management.health.refresh.enabled` (Third party; type `Boolean`; default `true`): Enable the health endpoint for the refresh scope.
- `management.health.ssl.certificate-validity-warning-threshold` (Third party; type `Duration`; default `14d`): If an SSL Certificate will be invalid within the time span defined by this threshold, it should trigger a warning.
- `management.health.ssl.enabled` (Third party; type `Boolean`; default `true`): Whether to enable SSL certificate health check.
- `management.health.zookeeper.enabled` (Third party; type `Boolean`; default `true`): Enable the health endpoint for zookeeper.

**Troubleshooting**

- `404`: the endpoint is not enabled or not exposed; check the *Enable & expose* tab.
- `401` or `403`: the access rule or the credentials rejected the call; check the *Security* tab.

For more detail, raise these log levels in the log4j configuration:

```xml
<Logger name="org.apereo.cas.oidc.federation.web" level="debug" additivity="false">
    <AppenderRef ref="console" />
    <AppenderRef ref="file" />
</Logger>
<Logger name="org.springframework.security" level="debug" additivity="false">
    <AppenderRef ref="console" />
    <AppenderRef ref="file" />
</Logger>
```

The keystore must be writable for rotation and revocation; CAS writes a new keystore file next to it, with the permissions, owner and group of the keystore where the file system allows it, and not readable by a group other than the keystore’s; a new keystore is readable by its owner only where the file system has such permissions. The new file then replaces the keystore at once, so that the keystore is never read half written. A keystore that is a symbolic link is written where the link points. Where the file system does not allow replacing the keystore while it is in use, as Windows may, the keys are not saved and stay as they were. CAS reads the keystore again when it changes, so nodes that share the keystore file sign with the same keys; nodes that do not share it each have their own keys, which superiors would all have to know. Superiors verify the entity configuration of CAS with the keys of their subordinate statement about it: when CAS is a subordinate, its future key should be added to the subordinate definition at each superior before the rotation makes it current. Likewise, entities that trust a CAS trust anchor with keys obtained out of band should be given its future key ahead of time.

## Trust Anchors

An OpenID Provider accepts the trust chains of relying parties that end at one of its trust anchors. Each trust anchor is defined by its entity identifier and its keys, which should be obtained out of band, as [OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-11.3) recommends, and may be given as a file, a URL or the JSON web key set itself; private key members are ignored. A trust anchor defined without keys is trusted with the keys of its own entity configuration, that is with whoever answers at its entity identifier. Trust anchors and their keys can be defined using CAS properties.

The authority hints are the entities directly above CAS, which issue subordinate statements about it; they are not trust anchors unless they are also defined as such. When no trust anchor is defined, CAS uses the authority hints as trust anchors, with the keys of their own entity configuration, and logs a warning.

## Automatic Registration

As an OpenID Provider, CAS registers relying parties of the federation automatically, as described by [OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-12.1), without any prior registration step:

- The relying party uses its entity identifier as its client id, which must be an `https` URL. On the first authorization or pushed authorization request, CAS resolves the trust chains of the relying party up to one of the [trust anchors](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-Federation.html#trust-anchors), and registers the resolved `openid_relying_party` metadata as an application that expires with the trust chain and is resolved again shortly before.
- Each redirect URI of the relying party is matched exactly, whatever characters it contains.
- The relying party authenticates with `private_key_jwt`, assumed when its metadata names no method, or with `tls_client_auth`. A relying party whose metadata names any other method is not registered, and so is a relying party that publishes its keys at a `jwks_uri` that is not an `https` URL.
- Every authorization request proves that the relying party controls its keys. It either carries a request object signed with a key of the relying party’s metadata, whose `iss` and `client_id` are the entity identifier, whose `aud` is only the issuer of CAS, with `jti` and `exp` and without `sub`, valid for 10 minutes at most, or it refers with `request_uri` to a [pushed authorization request](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-PAR.html) for which the relying party authenticated itself. Client assertions may be addressed to the issuer of CAS as well as to its token endpoint, except at the pushed authorization endpoint, where the client assertion of an automatically registered relying party must be addressed to the issuer of CAS only, as [OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-12.1.1.2) requires; otherwise the request is refused with `401` and `invalid_client`.
- A request object is used once: CAS remembers its `jti` for as long as the request object could be accepted, and at least for the lifetime of transient session tickets, during which only the very same request object is accepted again, as happens when the browser returns from the login.
- A request object may also be [encrypted to CAS](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-JWKS.html#encrypted-request-objects); it is decrypted first, and the signed request object inside is checked as above.
- Requests that fail these checks are refused with `invalid_request` and are never redirected to the relying party.
- The relying party that must prove its keys is the client of the authorization request: the `client_id` parameter or, when the request carries none, the `client_id` of its request object, on the OpenID Connect and the OAuth authorization endpoints alike, including the OpenID Connect authorization and pushed authorization endpoints that also answer outside the OpenID Connect base path, whatever the encoding of the path. A request object whose `client_id` differs from the `client_id` parameter is refused for every client, as [OpenID Connect](https://openid.net/specs/openid-connect-core-1_0.html#RequestObject) requires.
- The constraints that trust anchors and intermediates set in their subordinate statements apply to every trust chain of a relying party, whether resolved or sent, as [OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-6.2) requires: the maximum number of intermediates below the entity that sets them, the permitted and excluded domain names of the entities below it, and the entity types they may have, which must include `openid_relying_party`. A trust chain that breaks any of them is not used. The trust chains of trust mark issuers are held to the same constraints, except for entity types.
- The metadata of a relying party is resolved from its trust chain as [OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-6.1) describes: the metadata the immediate superior states about the relying party takes precedence over the metadata the relying party publishes, and the metadata policies of the trust anchor and intermediates are then merged, from the trust anchor down, and applied. `scope` is handled as a list of scope values. Operators beyond the standard ones are ignored, unless a statement of the chain lists them as critical, in which case the trust chain is not used. A trust chain whose policies conflict, or whose policies the metadata does not satisfy, such as a missing essential parameter, is not used either.
- The keys a trust anchor is trusted with out of band may still list a key the trust anchor has since revoked. When the trust anchor publishes its historical keys, CAS reads them, verified with those keys, and does not use a trust chain in which the trust anchor signed with a key it marks revoked, or with a key it had already retired when it issued the statement. The historical keys are always found through the entity configuration CAS fetches from the trust anchor, never through one a trust chain sent by a relying party carries; they must be served over `https`, and each must carry its expiration, as the specification requires. A trust anchor that advertises no historical keys has revoked none. CAS keeps the historical keys of a trust anchor for a minute. When its entity configuration or the historical keys it advertises cannot be obtained or verified, CAS cannot tell whether the key was revoked and does not use the trust chain for now; the relying party is not remembered as unresolved on that account, so it can be registered as soon as the trust anchor can be reached again. Every statement of a trust chain must name the key it is signed with, as the specification requires; a trust chain with a statement that does not is not used.
- The metadata superiors assert about a relying party, and their metadata policies, apply only to the entity types its entity configuration publishes: an entity that publishes no `openid_relying_party` metadata is not registered, whatever its superiors state.
- Resolving trust chains is bounded: CAS follows a limited number of authority hints from any entity configuration and accepts a limited number of intermediates between the relying party and a trust anchor, and an entity identifier whose trust chains could not be resolved is not resolved again for a while. Meanwhile, a registration that could not be refreshed is kept as it is. Each resolution sends a limited number of requests, 50 by default, counting entity configurations, subordinate statements, historical keys and trust mark status requests together, including those for the issuers of its trust marks; a statement fetched once is reused within the same resolution, and a resolution that needs more fails. The historical keys of trust anchors are not counted. Each resolution must also be done within a limited time, 10 seconds by default, all of its requests together, however slowly the other side answers, and every response is limited in size; the time limit covers each whole request, from the host name lookup and the TLS handshake to the last byte of the response. Requests are only sent over `https`, never to link-local, unspecified or multicast addresses such as the metadata service of a cloud provider, including such addresses embedded in IPv6 addresses, use the TLS settings of CAS and do not follow redirects. Addresses written in a URL are always checked, and host names when CAS resolves them, as it does to connect directly or through a SOCKS proxy; host names sent through an HTTP proxy of the JVM, or through a SOCKS proxy when CAS cannot resolve them, are resolved by the proxy, which then decides where they may go. A limited number of requests run at the same time, those abandoned at the deadline included. A trust anchor that does not accept a connection or answer in the time it is given, or that answers with a server error, is treated as unreachable. A resolution that runs out of time or requests, or finds no room among the running requests, cannot tell whether a relying party has a valid trust chain or trust mark: it is unavailable, and the relying party is not remembered as unresolved. A limited number of resolutions run at the same time, 10 by default, for automatic registration and the resolve endpoint together; one that cannot start within a second fails without the entity being remembered as unresolved, and so does a registration that cannot get its turn within a few seconds. Using the trust chain a relying party sent and, when it cannot be used, resolving its trust chains is a single resolution, within one time limit and request budget. A relying party that is not registered yet and cannot be registered for now is answered with `temporarily_unavailable` rather than as an unknown client. These limits can be tuned using CAS properties.
- A relying party whose metadata asks for pairwise subject identifiers receives them, calculated with a salt that can be defined using CAS properties, so that they stay the same whenever the relying party is registered again and are shared by relying parties of the same sector; the salt is never written into its registration. Without that salt, such a relying party is not registered. Its sector must be its own: its sector identifier URI, when it has one, must be an `https` URL, is fetched as a request of the resolution and must list all of its redirect URIs, and without one, its redirect URIs must all be in the same sector, read as they are when identifiers are calculated: an `http` or `https` redirect URI on a host other than a loopback one is in the sector of its host, and any other one in the relying party’s own. A sector identifier document that cannot be fetched for now leaves the relying party unregistered for now, without remembering it as unresolved, and its other trust anchors are tried.
- A relying party may send its trust chain in the `trust_chain` header of its request object or client assertion, as [OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-12.1.1.1) allows. Whenever CAS would resolve the relying party, to register it, to refresh its registration or after a key rollover, it uses that chain instead, once fully verified: the chain must start with the entity configuration of the relying party, end at a trust anchor whose keys are defined out of band, verify statement by statement from those keys, not have expired and not exceed the maximum path length; the required trust marks and the metadata policies of the chain apply as usual. Statements are public, so a chain alone does not show who sends it: it is used only when its entity configuration was not issued before the one the current registration was built from, and when the request object or client assertion that carries it is signed with the keys it resolves to. A chain replayed by someone else, even a genuine one, cannot keep or bring back keys the relying party has replaced. CAS resolves the trust chains itself when the sent chain cannot be used, and always when the trust anchor is trusted with the keys of its own entity configuration, which a sent chain could forge.
- A relying party that rolls its keys over is resolved again before its registration expires, as [OpenID Federation](https://openid.net/specs/openid-federation-1_0.html#section-12.3) allows: when a request object or a client assertion it sends, at any endpoint, verifies with none of the keys of its registration, CAS resolves its trust chains first and then checks the request with the keys the relying party now publishes. Each relying party is resolved this way at most once in the same period that applies to entity identifiers that could not be resolved, so objects signed with keys a relying party never published cannot make CAS fetch its statements over and over. Such objects count against that period whoever sends them, and the relying party is then resolved from what it publishes, so a relying party should publish a new key before it signs with it.

### Required Trust Marks

An OpenID Provider may only register relying parties that hold specific [trust marks](https://openid.net/specs/openid-federation-1_0.html#section-7), such as a certification or a membership the federation vouches for. The required trust mark types can be defined using CAS properties. For each of them, the entity configuration of the relying party must carry a trust mark of that type that:

- is a signed JWT typed `trust-mark+jwt` that names its signing key, is about the relying party, and has been issued and has not expired;
- is issued by an entity that the trust anchor of the relying party’s trust chain accepts for that type in its `trust_mark_issuers`;
- verifies with the federation keys of its issuer: the trust anchor’s own keys, or the keys of the issuer’s entity configuration, whose trust chain must end at the same trust anchor. A trust mark signed with a key the issuer has since retired verifies with that key as the issuer’s historical keys publish it, provided the key was never revoked and the trust mark was issued before the key was retired. The historical keys of an issuer are kept for a minute, except when they could not be requested: a resolution that has used up its requests verifies the trust mark with the current keys of its issuer only, and one that runs out of time or finds no room among the running requests is unavailable;
- carries a valid `delegation` from the owner of the type, when the trust anchor names one in `trust_mark_owners`;
- is reported `active` by the issuer’s trust mark status endpoint, when the issuer publishes one; a status endpoint or historical keys endpoint that cannot be reached for now or answers with a server error makes the resolution unavailable rather than the trust mark invalid, unless another trust mark of the type is valid, or the relying party lacks a valid trust mark of another required type; the endpoint must be an `https` URL. So does a trust anchor that cannot be reached while the trust chain of the issuer is resolved. An issuer whose endpoints keep answering with server errors thus keeps the relying parties holding its trust marks from being remembered as unresolved, so they are resolved again with each request, each time within the limits of a resolution.

A relying party that lacks a valid trust mark of a required type is not registered, and the reason is reported in the logs. Trust marks are checked again whenever the registration is refreshed.

An authorization request passed by value travels through the CAS login as part of its service URL, and CAS does not match service URLs beyond a maximum length, which can be adjusted using CAS properties. A request object that carries its `trust_chain` is several kilobytes long: relying parties should send it with a [pushed authorization request](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-PAR.html), as OpenID Federation suggests, or the limit must be raised.
