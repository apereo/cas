# Verifiable Credentials - OpenID Connect Authentication

Source: https://apereo.github.io/cas/development/authentication/OIDC-Authentication-Verifiable-Credentials.html

OpenID Connect Authentication can be used in conjunction with Verifiable Credentials to provide a secure and decentralized way of verifying user identities. Verifiable Credentials are digital credentials that can be issued by trusted authorities and can be presented by users to prove their identity or attributes.

In this role, CAS acts as an **OpenID Credential Issuer** and extends its existing OpenID Connect capabilities with support for credential issuance, credential metadata publication, proof validation, nonce generation, and wallet-facing issuance flows.

The following capabilities are in place:

- Issuer metadata is published via the `.well-known/openid-credential-issuer` endpoint.
- CAS may issue credentials formats such as `SD-JWT VC` (selective disclosure) and ISO/IEC 18013-5 mdocs.
- Dedicated endpoints for credential issuance and nonce generation are available.
- Credential offers may be produced and shared with wallets.
- Pre-authorized code flows may be used to obtain issuance-scoped access tokens.
- Access tokens issued for verifiable credential flows may carry authorization context for one or more credential configurations.
- Proofs may be validated to ensure possession of holder key material and to prevent replay.

Supported formats are:

- `dc+sd-jwt`
- `jwt_vc_json`
- `jwt_vc_json-ld`
- `mso_mdoc`

## Overview

Support is enabled by including the following module in the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-oidc-vc"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oidc.vc.issuer.batch-size` (Optional; type `Integer`; default `10`)
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].claims.[key].disclosable` (Optional; type `boolean`; default `true`)
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].claims.[key].display` (Optional; type `List<OidcVerifiableCredentialClaimProperties.CredentialClaimDisplay>`)
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].claims.[key].display[0].locale` (Optional; type `String`; default `en-US`)
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].claims.[key].display[0].name` (Optional; type `String`)
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].claims.[key].mandatory` (Optional; type `boolean`)
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].claims.[key].namespace` (Optional; type `String`)
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].credential-signing-alg-values-supported` (Optional; type `List<String>`; default `ES256,RS256`): Lists the signing algorithms supported by the issuer when producing the credential for this configuration. These algorithm identifiers are typically JOSE signature algorithms such as `ES256` or `RS256`, and are advertised in issuer metadata so wallets know what credential signature formats are supported.
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].credential-validity` (Optional; type `String`; default `P30D`)
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].cryptographic-binding-methods-supported` (Optional; type `List<String>`; default `jwk`): Lists the supported cryptographic binding methods for this credential. These values indicate how the issued credential may be bound to key material controlled by the wallet or holder. A common value is `jwk`, which indicates binding via a JSON Web Key.
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].deferred-issuance` (Optional; type `boolean`)
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].display` (Optional; type `List<OidcVerifiableCredentialConfigurationProperties.CredentialConfigurationDisplay>`; default `org.apereo.cas.configuration.model.support.oidc.OidcVerifiableCredentialConfigurationProperties$CredentialConfigurationDisplay@3af356f`)
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].display[0].background-color` (Optional; type `String`)
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].display[0].description` (Optional; type `String`; default `My verifiable credential that belongs to my organization`)
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].display[0].locale` (Optional; type `String`; default `en-US`)
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].display[0].logo` (Optional; type `String`; default `https://apereo.github.io/cas/images/cas_logo.png`)
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].display[0].name` (Optional; type `String`; default `My Verifiable Credential`)
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].display[0].text-color` (Optional; type `String`)
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].doctype` (Optional; type `String`)
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].format` (Optional; type `OidcVerifiableCredentialConfigurationProperties.CredentialConfigurationFormats`; default `OidcVerifiableCredentialConfigurationProperties.CredentialConfigurationFormats.DC_SD_JWT(dc+sd-jwt)`): Defines the credential format issued for this configuration. Typical values depend on the verifiable credential profile supported by the issuer, such as JWT-based or SD-JWT-based credentials. This value is published in issuer metadata and is used by wallets to determine how the credential request and response should be processed. Available values are as follows: - `DC_SD_JWT`: A Data Integrity Claims (DC) credential encoded as an SD-JWT, supporting selective disclosure of claims while preserving cryptographic integrity. - `JWT_VC_JSON`: It is a JSON-based string that represents the credential as a base64url-encoded JWT. - `JWT_VC_JSON_LD`: This format includes a `@context` URLs that maps data properties to universal schemas. Every field has a strict, globally defined semantic meaning. - `MSO_MDOC`: An ISO/IEC 18013-5 mdoc: CBOR claims grouped by name space, signed by the issuer through a mobile security object (a `COSE_Sign1` structure) and bound to the holder's device key.
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].key-attestations.key-storage` (Optional; type `List<String>`)
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].key-attestations.required` (Optional; type `boolean`)
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].key-attestations.user-authentication` (Optional; type `List<String>`)
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].proof-signing-alg-values-supported` (Optional; type `List<String>`; default `ES256,RS256`): Lists the signing algorithms supported for proof validation when the wallet submits proof material as part of a credential request. These values represent the algorithms CAS accepts when verifying proof-of-possession tokens or JWT-based proofs presented by the holder.
- `cas.authn.oidc.vc.issuer.credential-configurations.[key].scope` (Optional; type `String`): OAuth scope associated with this credential configuration. This scope may be used during authorization and token issuance to indicate that the client or wallet is requesting permission to obtain this specific type of verifiable credential.
- `cas.authn.oidc.vc.issuer.deferred-issuance.interval` (Optional; type `String`; default `PT5M`)
- `cas.authn.oidc.vc.issuer.deferred-issuance.time-to-live` (Optional; type `String`; default `P7D`)
- `cas.authn.oidc.vc.issuer.display` (Optional; type `List<OidcVerifiableCredentialsIssuerProperties.IssuerDisplay>`)
- `cas.authn.oidc.vc.issuer.display[0].locale` (Optional; type `String`)
- `cas.authn.oidc.vc.issuer.display[0].logo` (Optional; type `String`)
- `cas.authn.oidc.vc.issuer.display[0].logo-alt-text` (Optional; type `String`)
- `cas.authn.oidc.vc.issuer.display[0].name` (Optional; type `String`)
- `cas.authn.oidc.vc.issuer.encryption.alg-values-supported` (Optional; type `List<String>`)
- `cas.authn.oidc.vc.issuer.encryption.enabled` (Optional; type `Boolean`; default `false`)
- `cas.authn.oidc.vc.issuer.encryption.enc-values-supported` (Optional; type `List<String>`)
- `cas.authn.oidc.vc.issuer.encryption.request-encryption-required` (Optional; type `Boolean`; default `false`)
- `cas.authn.oidc.vc.issuer.encryption.response-encryption-required` (Optional; type `Boolean`; default `false`)
- `cas.authn.oidc.vc.issuer.key-attestation.trust-anchors` (Optional; type `List<String>`)
- `cas.authn.oidc.vc.issuer.status-list.enabled` (Optional; type `Boolean`; default `false`)
- `cas.authn.oidc.vc.issuer.status-list.expiration` (Optional; type `String`; default `PT24H`)
- `cas.authn.oidc.vc.issuer.status-list.size` (Optional; type `Integer`; default `131072`)
- `cas.authn.oidc.vc.issuer.status-list.time-to-live` (Optional; type `String`; default `PT10M`)
- `cas.authn.oidc.vc.metadata.include-claims` (Optional; type `Boolean`; default `true`)
- `cas.authn.oidc.vc.metadata.signed-metadata-expiration` (Optional; type `String`; default `P1D`)
- `cas.authn.oidc.vc.offer.required-principal-attribute` (Optional; type `String`)
- `cas.authn.oidc.vc.offer.required-principal-attribute-value` (Optional; type `String`)
- `cas.authn.oidc.vc.offer.transaction-code-enabled` (Optional; type `Boolean`; default `true`)
- `cas.authn.oidc.vc.presentation.client-identifier-prefix` (Optional; type `OidcVerifiableCredentialsPresentationProperties.ClientIdentifierPrefixes`; default `REDIRECT_URI`)
- `cas.authn.oidc.vc.presentation.response-mode` (Optional; type `OidcVerifiableCredentialsPresentationProperties.ResponseModes`; default `DIRECT_POST`)

**Signing & encryption**

This CAS feature is able to accept signing and encryption crypto keys. In most scenarios if keys are not provided, CAS will auto-generate them. The following instructions apply if you wish to manually and beforehand create the signing and encryption keys.

Note that if you are asked to create a [JWK](https://tools.ietf.org/html/rfc7517) of a certain size for the key, you are to use the following set of commands to generate the token:

```bash
wget https://raw.githubusercontent.com/apereo/cas/master/etc/jwk-gen.jar
java -jar jwk-gen.jar -t oct -s [size]
```

The outcome would be similar to:

```json
{
  "kty": "oct",
  "kid": "...",
  "k": "..."
}
```

The generated value for `k` needs to be assigned to the relevant CAS settings. Note that keys generated via the above algorithm are processed by CAS using the Advanced Encryption Standard (`AES`) algorithm which is a specification for the encryption of electronic data established by the U.S. National Institute of Standards and Technology.

---

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

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
- How wallets should present the issuer (`display`: name, language and logo), taken from issuer display settings in CAS configuration; without it wallets show the issuer as unnamed.

#### Metadata Location

OpenID4VCI locates the credential issuer metadata by inserting `/.well-known/openid-credential-issuer` into the issuer identifier *between the host and the path*, rather than by appending it the way OpenID Connect Discovery does; [RFC 8414](https://www.rfc-editor.org/rfc/rfc8414) locates the authorization server metadata the same way. A wallet issued against `https://sso.example.org/cas/oidc` therefore asks for:

```bash
GET https://sso.example.org/.well-known/openid-credential-issuer/cas/oidc
GET https://sso.example.org/.well-known/oauth-authorization-server/cas/oidc
```

Verifiers other than CAS locate the keys that sign issued credentials the same way, through the [JWT VC Issuer Metadata](https://datatracker.ietf.org/doc/draft-ietf-oauth-sd-jwt-vc/) at `https://sso.example.org/.well-known/jwt-vc-issuer/cas/oidc`, which names the issuer and points to the OpenID Connect JWKS:

```json
{
  "issuer": "https://sso.example.org/cas/oidc",
  "jwks_uri": "https://sso.example.org/cas/oidc/jwks"
}
```

CAS is normally deployed under the `/cas` context path, so neither request reaches the application at all and the servlet container answers with its own `404`. Route them onto the paths CAS serves, either in the proxy that fronts CAS or with the [embedded Tomcat rewrite valve](https://apereo.github.io/cas/development/installation/Servlet-Container-Embedded-Tomcat-RewriteValve.html). The valve must be registered on the engine, which runs before a context is selected.

The rewrite rule would be similar to:

```
RewriteRule ^/\.well-known/(openid-credential-issuer|oauth-authorization-server|openid-configuration|jwt-vc-issuer)(/.+)$ $2/.well-known/$1 [L]
```

Naming the documents explicitly, rather than matching every well-known path, leaves unrelated ones such as `/.well-known/acme-challenge/<token>` untouched.

A wallet that cannot resolve this metadata may not begin issuance at all.

#### Signed Metadata

A wallet that asks for `application/jwt` in its `Accept` header, preferring it at least as much as JSON, receives the issuer metadata as a JWT of type `openidvci-issuer-metadata+jwt`, signed with the issuer signing key and carrying its `x5c` certificate chain, if any, without the trust anchor, as the [High Assurance Interoperability Profile](https://openid.net/specs/openid4vc-high-assurance-interoperability-profile-1_0.html) requires. Every metadata parameter is a top-level claim, next to `sub` and `iss`, both set to the credential issuer, `iat` and `exp`:

```json
{
  "alg": "ES256",
  "typ": "openidvci-issuer-metadata+jwt",
  "kid": "cas-vc-signing",
  "x5c": [
    "MIIB..."
  ]
}
```

```json
{
  "sub": "https://sso.example.org/cas/oidc",
  "iss": "https://sso.example.org/cas/oidc",
  "iat": 1791238400,
  "exp": 1791324800,
  "credential_issuer": "https://sso.example.org/cas/oidc",
  "credential_endpoint": "https://sso.example.org/cas/oidc/oidcVcCredential",
  "credential_configurations_supported": {
    "UniversityDegree": {
      "format": "dc+sd-jwt"
    }
  }
}
```

Other requests receive the unsigned JSON document. How long signed metadata remains valid is controlled in CAS settings:

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oidc.vc.metadata.include-claims` (Optional; type `Boolean`; default `true`)
- `cas.authn.oidc.vc.metadata.signed-metadata-expiration` (Optional; type `String`; default `P1D`)

#### Token Endpoint Authentication

The pre-authorized code grant carries no client credentials. CAS authenticates the exchange from the `pre-authorized_code` and `grant_type` request parameters themselves, and the client bound to the credential offer is recorded on the pre-authorization code when the offer is created.

A wallet decides how to authenticate from the authorization server metadata, so the token endpoint has to advertise that it accepts requests with no client authentication. CAS includes `none` in authentication methods supported for the token endpoint by default for this reason. If the list is narrowed, keep `none` in it; without it a wallet picks one of the credentialed methods it sees instead and the exchange is rejected, because the wallet has no client registration to authenticate with.

For the same reason the authorization server metadata advertises `pre-authorized_grant_anonymous_access_supported` as `true` whenever the pre-authorized code grant is listed in `grant_types_supported`. A wallet that finds no such value assumes `false` and may refuse to redeem the code without a `client_id` it does not have.

In the authorization code flow, wallets authenticate at the pushed authorization request and token endpoints with a wallet attestation, as the High Assurance Interoperability Profile requires, using [attestation-based client authentication](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-AccessToken-AuthMethods.html#attestation-based-client-authentication).

### Credential Endpoint

Issues a verifiable credential to the wallet once the access token, proof, and requested credential configuration have been validated.

```bash
POST /oidc/oidcVcCredential
```

This endpoint expects:

- An access token, presented in the `Authorization` header as `Bearer ...` or, when the token response named the token type `DPoP`, as `DPoP ...` per [RFC 9449](https://www.rfc-editor.org/rfc/rfc9449). An `access_token` or `token` request parameter is accepted as well, as it is elsewhere in CAS. A `DPoP`-bound token must be accompanied by a `DPoP` proof header bound to that token; a request without one, or with a proof that does not verify, is answered with `401` and `WWW-Authenticate: DPoP error="invalid_dpop_proof"`. A proof may not be reused. Once [DPoP nonces](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-DPoP.html#server-provided-nonces) are turned on, the proof must also carry one, or the request is answered with `401`, `use_dpop_nonce` and a fresh nonce in the `DPoP-Nonce` header.
- The requested credential. When the token response returned `credential_identifiers` in its authorization details, the request must name one of them with `credential_identifier`; otherwise it names a `credential_configuration_id`. The two are mutually exclusive, and using the one that does not apply is `invalid_credential_request`. Pre-authorized code token responses return no authorization details, so those requests use `credential_configuration_id`.
- A `proofs` object holding one or more proof JWTs, each carrying a `nonce` claim, or exactly one key attestation as an `attestation` proof (see [Key Attestations](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-Verifiable-Credentials.html#key-attestations)).

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

Each proof must be signed with one of the `proof-signing-alg-values-supported` of the requested credential configuration and name the holder key by one of its `cryptographic-binding-methods-supported`:

| Proof header | Binding method | Holder key |
| --- | --- | --- |
| `jwk` | `jwk` | The key itself. A `kid` sent alongside it is ignored. |
| `x5c` | `jwk` | The public key of the first certificate, which must be within its validity period. |
| `kid` | `did:jwk` | The key encoded in the `did:jwk` DID URL. Other DID methods cannot be resolved and are refused. |

RSA, EC and Ed25519 (`EdDSA`) keys are accepted. A proof that does not satisfy the configuration is answered with `invalid_proof`. The defaults are `ES256` and `RS256` with the `jwk` binding method.

There is no separate batch credential endpoint. A batch is a single credential request carrying several proofs, and the response holds one credential per proof, all of the same credential configuration. How many proofs are accepted is advertised as `batch_credential_issuance` in the issuer metadata and controlled by a batch size limit defined in CAS configuration.

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

Errors carry the codes of [OpenID4VCI 1.0 section 8.3.1.2](https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#section-8.3.1.2): a `credential_identifier` the token response did not return is `unknown_credential_identifier`, a credential configuration CAS does not publish is `unknown_credential_configuration`, one it publishes but will not issue to this client or access token is `credential_request_denied`, and a proof that cannot be accepted is `invalid_proof`, or `invalid_nonce` when only its nonce is stale.

#### Encrypted Requests and Responses

Credential requests and responses may be encrypted on top of TLS, as described by [OpenID4VCI 1.0 section 10](https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-encrypted-credential-reques). Once turned on in CAS settings, the issuer metadata advertises both directions:

```json
{
  "credential_request_encryption": {
    "jwks": {
      "keys": [
        {
          "kty": "RSA",
          "kid": "cas-4bKkzQdW",
          "use": "enc",
          "alg": "RSA-OAEP-256",
          "n": "sLx5PUdqNoSl...",
          "e": "AQAB"
        }
      ]
    },
    "enc_values_supported": [
      "A128GCM",
      "A256GCM"
    ],
    "encryption_required": false
  },
  "credential_response_encryption": {
    "alg_values_supported": [
      "ECDH-ES",
      "RSA-OAEP-256"
    ],
    "enc_values_supported": [
      "A128GCM",
      "A256GCM"
    ],
    "encryption_required": false
  }
}
```

Requests are encrypted to the current encryption keys of the CAS OpenID Connect keystore: an RSA key is published for `RSA-OAEP-256` and an elliptic curve key for `ECDH-ES`. An encrypted request is sent as `application/jwt`, a JWE whose payload is the credential request and whose `kid` header names the key. To receive an encrypted response, the wallet adds the key to encrypt it to, with its `alg`, and the content encryption algorithm:

```json
{
  "credential_configuration_id": "myorg",
  "proofs": {
    "jwt": [
      "eyJ0eXAiOiJvcGVuaWQ0dmNpL..."
    ]
  },
  "credential_response_encryption": {
    "jwk": {
      "kty": "EC",
      "crv": "P-256",
      "kid": "wallet",
      "alg": "ECDH-ES",
      "x": "N5rsOYN3J44MRbUT...",
      "y": "IZKX5LyZlKGTHHE8..."
    },
    "enc": "A256GCM"
  }
}
```

A request asking for an encrypted response must itself be encrypted, so that the response key cannot be swapped on the way. The response is then returned as `application/jwt`, encrypted to that key and carrying its `kid`; error responses are never encrypted. Parameters that cannot be used, compression (`zip`) which CAS does not support, or a missing `credential_response_encryption` when encrypted responses are required are answered with `invalid_encryption_parameters`; a request that cannot be decrypted, or a plain request when encrypted requests are required, with `invalid_credential_request`.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oidc.vc.issuer.encryption.alg-values-supported` (Optional; type `List<String>`)
- `cas.authn.oidc.vc.issuer.encryption.enabled` (Optional; type `Boolean`; default `false`)
- `cas.authn.oidc.vc.issuer.encryption.enc-values-supported` (Optional; type `List<String>`)
- `cas.authn.oidc.vc.issuer.encryption.request-encryption-required` (Optional; type `Boolean`; default `false`)
- `cas.authn.oidc.vc.issuer.encryption.response-encryption-required` (Optional; type `Boolean`; default `false`)

### Nonce Endpoint

Produces a fresh c_nonce that may be used by the wallet in a later proof for the credential request.

```bash
POST /oidc/oidcVcNonce
```

This endpoint returns `c_nonce`. The challenge is never returned from the token endpoint. Once [DPoP nonces](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-DPoP.html#server-provided-nonces) are turned on, the response also carries a DPoP nonce in its `DPoP-Nonce` header, for the DPoP proof of the credential request.

### Notification Endpoint

Receives notifications from the wallet about the credentials of a credential response, as described by [OpenID4VCI 1.0 section 11](https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-notification-endpoint). Each credential response carries a `notification_id`, and the issuer metadata advertises the endpoint as `notification_endpoint`.

```bash
POST /oidc/oidcVcNotification
```

The request carries the access token that obtained the credentials, checked as it is at the credential endpoint, and a JSON body:

```json
{
  "notification_id": "TST-1-...",
  "event": "credential_failure",
  "event_description": "Could not store the Credential. Out of storage."
}
```

The `event` is one of `credential_accepted`, `credential_failure` or `credential_deleted`. A notification is answered with `204` and recorded in the CAS audit log as `OIDC_VERIFIABLE_CREDENTIAL_NOTIFICATION`; sending the same notification again succeeds again. A notification id that is unknown, has expired with the access token, or was issued to another client or user is answered with `invalid_notification_id`, and a malformed request, an unknown event or an `event_description` with characters outside the permitted ASCII set with `invalid_notification_request`.

### Deferred Credential Endpoint

Hands out credentials whose issuance was deferred, as described by [OpenID4VCI 1.0 section 9](https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#name-deferred-credential-endpoin). A credential configuration opts in with its `deferred-issuance` setting, and the issuer metadata then advertises the endpoint as `deferred_credential_endpoint`. A credential request for such a configuration is validated as usual, proofs included, and answered with `202`:

```json
{
  "transaction_id": "TST-1-...",
  "interval": 300
}
```

The transaction is kept in the ticket registry, bound to the client and the user of the access token, with the holder public keys of the validated proofs and nothing else. It stays pending until it is approved or denied through the actuator endpoint below. The wallet asks for the credentials with an access token of the same client and user that still authorizes the credential configuration, such as the original token or one refreshed from it, waiting at least `interval` seconds between attempts:

```bash
POST /oidc/oidcVcDeferredCredential
```

```json
{
  "transaction_id": "TST-1-..."
}
```

A pending transaction is answered with `202` and the same `transaction_id`. Once approved, the answer is `200` with the `credentials` and a `notification_id`, built at that moment from the user’s attributes, and the `transaction_id` can no longer be used. A denied transaction is answered with `credential_request_denied`. A `transaction_id` that is unknown, expired, already used, or was started by another client or user is answered with `invalid_transaction_id`. The request may be encrypted and may carry its own `credential_response_encryption`, as at the credential endpoint, whatever the credential request asked for.

The stateless ticket registry cannot keep transactions, so with it credentials of these configurations are issued immediately.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oidc.vc.issuer.deferred-issuance.interval` (Optional; type `String`; default `PT5M`)
- `cas.authn.oidc.vc.issuer.deferred-issuance.time-to-live` (Optional; type `String`; default `P7D`)

Pending transactions are listed, approved and denied through an actuator endpoint, and each decision is recorded in the CAS audit log as `OIDC_VERIFIABLE_CREDENTIAL_DEFERRED_ISSUANCE`:

`oidcVcDeferred`

CAS endpoint · 2 operations · Not exposed by default · cas-server-support-oidc-vc

**GET** `oidcVcDeferred`: Get the deferred verifiable credential transactions. (1 param)

Parameters

| Name | In | Type | Description |
| --- | --- | --- | --- |
| `principal` | query | – | The principal identifier |

Response

- **Returns**: `List`
- **Java method**: `getTransactions(String)`
- **Defined in**: `OidcVerifiableCredentialDeferredIssuanceEndpoint`

Example request

```bash
curl -u "casuser:password" \
  -H "Accept: application/json" \
  "https://sso.example.org/cas/actuator/oidcVcDeferred?principal=..."
```

**POST** `oidcVcDeferred/{transactionId}`: Approve or deny a deferred verifiable credential transaction. (2 params)

Parameters

| Name | In | Type | Description |
| --- | --- | --- | --- |
| `transactionId`required | path | – | The transaction id |
| `status`required | query | – | APPROVED or DENIED |

Response

- **Returns**: `WebEndpointResponse`
- **Java method**: `decide(String,String) `
- **Defined in**: `OidcVerifiableCredentialDeferredIssuanceEndpoint`

Example request

```bash
curl -u "casuser:password" \
  -X POST \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  "https://sso.example.org/cas/actuator/oidcVcDeferred/{transactionId}?status=..."
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
    implementation "org.apereo.cas:cas-server-support-oidc-vc"
}
```

Turn the endpoint on and expose it over the web. One entry covers every operation. By default only `info`, `health` and `status` are exposed.

```properties
management.endpoint.oidcVcDeferred.access=UNRESTRICTED
management.endpoints.web.exposure.include=oidcVcDeferred
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
cas.monitor.endpoints.endpoint.oidcVcDeferred.access=AUTHENTICATED
```

*Role*

```properties
cas.monitor.endpoints.endpoint.oidcVcDeferred.access=ROLE
cas.monitor.endpoints.endpoint.oidcVcDeferred.required-roles=ADMIN
```

*IP address*

```properties
cas.monitor.endpoints.endpoint.oidcVcDeferred.access=IP_ADDRESS
cas.monitor.endpoints.endpoint.oidcVcDeferred.required-ip-addresses=127.0.0.1
```

*Anyone*

```properties
cas.monitor.endpoints.endpoint.oidcVcDeferred.access=PERMIT
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

- `cas.monitor.endpoints.endpoint.oidcVcDeferred.access` (Optional; type `ActuatorEndpointProperties.EndpointAccessLevel`; default `DENY`): Define the security access level of the endpoint. Available values are as follows: - `PERMIT`: Allow open access to the endpoint. - `ANONYMOUS`: Allow anonymous access to the endpoint. - `DENY`: Block access to the endpoint. - `AUTHENTICATED`: Require authenticated access to the endpoint. - `ROLE`: Require authenticated access to the endpoint along with a role requirement. - `AUTHORITY`: Require authenticated access to the endpoint along with an authority requirement. - `IP_ADDRESS`: Require authenticated access to the endpoint using a collection of IP addresses.
- `cas.monitor.endpoints.endpoint.oidcVcDeferred.required-authorities` (Optional; type `List<String>`): Required user authorities.
- `cas.monitor.endpoints.endpoint.oidcVcDeferred.required-ip-addresses` (Optional; type `List<String>`): Required IP addresses. CIDR ranges are accepted.
- `cas.monitor.endpoints.endpoint.oidcVcDeferred.required-roles` (Optional; type `List<String>`): Required user roles.
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
<Logger name="org.apereo.cas.oidc.vc.issuer.deferred" level="debug" additivity="false">
    <AppenderRef ref="console" />
    <AppenderRef ref="file" />
</Logger>
<Logger name="org.springframework.security" level="debug" additivity="false">
    <AppenderRef ref="console" />
    <AppenderRef ref="file" />
</Logger>
```

### Credential Offer Endpoint

Exposes a prepared credential offer for a previously-created issuance transaction.

```bash
GET /oidc/oidcVcCredentialOffer/{transactionId}
```

This endpoint does not establish subject identity on its own. Instead, it dereferences a short-lived server-side issuance transaction and returns the corresponding credential offer document.

### Trusted Transaction Creation Endpoint

Creates a server-side issuance transaction for a known subject and returns an opaque transaction identifier and a wallet-facing offer URI.

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

Exchanges an authorization artifact, such as a pre-authorized code, for an access token that may later be used at the credential endpoint.

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

Proofs are expected to carry a `nonce` claim. The nonce lets CAS tell whether the wallet’s proof is fresh, instead of a replay. In OIDC4VCI, the wallet sends a proof showing it controls the key the credential should be bound to, and the c_nonce is the primary defense against replay of that proof.

Without a nonce, an attacker who somehow gets hold of a previously valid proof could try to send it again and get a duplicate credential issued to the same key.

In practical terms, the flow is:

- The wallet asks CAS for a fresh c_nonce from the nonce endpoint; the token endpoint never returns one.
- The wallet builds its proof and includes that nonce.
- CAS checks that the nonce matches one it issued, is still fresh, and has not already been used.
- CAS consumes it so the same proof cannot be replayed.

The token endpoint issues the nonce. The credential endpoint enforces it while validating the proof.

## Key Attestations

A wallet may vouch for the keys it wants credentials bound to with a key attestation, as described by [OpenID4VCI 1.0 Appendix D](https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#appendix-D): a `key-attestation+jwt` signed by the wallet provider that lists the `attested_keys` and how they are protected (`key_storage`, `user_authentication`). Key attestations are verified once trust anchors are configured in CAS settings as PEM certificates, typically those of the wallet providers that are trusted. Following [HAIP 1.0](https://openid.net/specs/openid4vc-high-assurance-interoperability-profile-1_0.html), the attestation carries its signing certificate, and any intermediate certificates, in its `x5c` header; the chain must lead to a configured trust anchor without including it, and the signer must not be self-signed. The attestation must carry `iat` and, when it carries `exp`, must not have expired. A `status` claim is accepted, and logged as a warning, since its revocation status is not checked.

A key attestation can be presented in two ways:

- In the `key_attestation` header of a `jwt` proof. The proof key must be one of the attested keys.
- As an `attestation` proof, standing in for proof JWTs. Its `nonce` claim must carry a c_nonce issued by CAS, and one credential is issued per attested key, up to the batch size limit.

```json
{
  "credential_configuration_id": "myorg",
  "proofs": {
    "attestation": [
      "eyJ0eXAiOiJrZXktYXR0ZXN0YXRpb24rand0Ii..."
    ]
  }
}
```

A credential configuration may require key attestations, and may list the `key_storage` and `user_authentication` values it accepts; an attestation must then name at least one accepted value of each list. A `jwt` proof without a key attestation is refused for such a configuration. The requirement is advertised in the issuer metadata as `key_attestations_required`, and the `attestation` proof type is advertised once trust anchors are configured:

```json
{
  "proof_types_supported": {
    "jwt": {
      "proof_signing_alg_values_supported": [
        "ES256",
        "RS256"
      ],
      "key_attestations_required": {
        "key_storage": [
          "iso_18045_high",
          "iso_18045_moderate"
        ]
      }
    },
    "attestation": {
      "proof_signing_alg_values_supported": [
        "ES256",
        "RS256"
      ],
      "key_attestations_required": {
        "key_storage": [
          "iso_18045_high",
          "iso_18045_moderate"
        ]
      }
    }
  }
}
```

A key attestation that fails any of these checks is answered with `invalid_proof`, while a missing, unknown or reused nonce in an `attestation` proof is answered with `invalid_nonce`.

## Authorization Code Flow

The Authorization Code Flow allows a wallet to obtain authorization to receive one or more verifiable credentials through a standard OAuth 2.0/OpenID Connect authorization process. Unlike the Pre-Authorized Code Flow, where a trusted backend initiates the issuance transaction, the Authorization Code Flow is wallet-driven. The wallet begins by sending an authorization request to the Credential Issuer’s authorization endpoint, requesting one or more credential configurations using the `authorization_details` parameter. The request may also include the `openid` scope if the wallet wishes to authenticate the end-user and receive an ID Token as part of the token response. The wallet exchanges the authorization code at the token endpoint to obtain an access token that is specifically authorized for credential issuance.

Instead of authorization details, the wallet may request a credential by the `scope` its credential configuration publishes in the issuer metadata, for example `scope=openid UniversityDegree`. Every granted scope that belongs to a credential configuration authorizes that configuration, narrowed by the service’s verifiable credentials policy. These scopes are accepted and advertised in `scopes_supported` without being listed among the discovery scopes. The token response then carries no credential identifiers, so the credential request names the configuration with `credential_configuration_id`. A wallet may use both mechanisms in one authorization request.

The wallet may also send the authorization request as a [pushed authorization request](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-PAR.html), with its `authorization_details`, `issuer_state` and PKCE parameters, and then send only its client ID and the returned `request_uri` to the authorization endpoint. CAS uses the pushed request alone and ignores any authorization details on the authorization endpoint request. Authorization details that name no credential the wallet may receive are refused when they are pushed, with `invalid_authorization_details`, rather than when the wallet is sent to the authorization endpoint.

A refresh token issued in this flow keeps the authorization details of its code, as RFC 9396 describes, so access tokens obtained with it may request the same credentials; scopes are honored as before.

## Pre-Authorized Code Flow

In pre-authorized code flows, CAS or a trusted backend prepares the issuance transaction before the wallet starts the OAuth exchange.

The general flow is:

- A trusted caller creates an issuance transaction.
- CAS stores the transaction and issues a pre-authorized code.
- CAS returns a wallet-facing `credential_offer_uri`.
- The wallet resolves the offer.
- The wallet exchanges the pre-authorized code at the token endpoint.
- CAS returns an access token.
- The wallet calls the credential endpoint with the access token and proof.
- CAS validates the request and issues the credential.

The pre-authorized code is single use, as OpenID4VCI requires. It is redeemed and deleted before the access token is minted, so of several concurrent exchanges of the same code exactly one succeeds and the rest are refused; the issuance transaction is removed along with it.

The [stateless ticket registry](https://apereo.github.io/cas/development/ticketing/Stateless-Ticket-Registry.html) cannot delete anything, so there the pre-authorized code and the nonces handed out by the nonce endpoint are not single use: each stays usable until it expires, which departs from OpenID4VCI. Keep their expiration short. Verifiable presentations are not supported with that registry.

## ISO mdoc Credentials

CAS can issue ISO/IEC 18013-5 mdocs, such as a mobile driving licence, in the `mso_mdoc` format of [OpenID4VCI 1.0](https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html), which the OpenID4VC High Assurance Interoperability Profile also covers. An mdoc credential configuration names its document type, such as `org.iso.18013.5.1.mDL`, and may place each claim in a name space, such as `org.iso.18013.5.1`; a claim without one goes in the name space named after the document type. Both are defined through CAS settings.

The credential is the base64url-encoded CBOR `IssuerSigned` structure. Each claim is a salted item, and the issuer signs their digests, the document type, the validity period and the holder’s device key in a mobile security object, a `COSE_Sign1` structure. The device key is the key the wallet’s proof is signed with; it must be an elliptic curve or EdDSA key, and a proof made with an RSA key is refused with `invalid_proof`, since an mdoc cannot bind one.

The signing key must be an elliptic curve key on `P-256`, `P-384` or `P-521`, whose curve sets the algorithm (`ES256`, `ES384` or `ES512`), which must be among the credential configuration’s signing algorithms. It must also carry an `x5c` certificate chain, which goes in the `x5chain` header of the signature; a trailing self-signed trust anchor is left out, as for the other formats. A service may pick such a key from the keystore by its key id.

The issuer metadata describes an mdoc configuration by its document type, with `cose_key` binding, signing algorithms as COSE algorithm identifiers, and claim paths made of the name space and the element:

```json
{
  "format": "mso_mdoc",
  "doctype": "org.iso.18013.5.1.mDL",
  "cryptographic_binding_methods_supported": ["cose_key"],
  "credential_signing_alg_values_supported": [-7],
  "credential_metadata": {
    "claims": [
      { "path": ["org.iso.18013.5.1", "given_name"] },
      { "path": ["org.iso.18013.5.1", "family_name"] }
    ]
  }
}
```

## Verifiable Presentations

CAS can also act as a verifier and ask a wallet to present a credential. A relying party creates a presentation request, and CAS returns a deep link the wallet can open, usually rendered as a QR code.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oidc.vc.presentation.client-identifier-prefix` (Optional; type `OidcVerifiableCredentialsPresentationProperties.ClientIdentifierPrefixes`; default `REDIRECT_URI`)
- `cas.authn.oidc.vc.presentation.response-mode` (Optional; type `OidcVerifiableCredentialsPresentationProperties.ResponseModes`; default `DIRECT_POST`)

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

Claims are required unless marked otherwise. DCQL has no per-claim flag, so optional claims are expressed with `claim_sets`: every claim first, then the required ones alone, or each optional claim on its own when none is required. The wallet returns the first combination it can satisfy, and CAS accepts any of them.

Wallets may bind credentials to EC, RSA or Ed25519 keys. CAS advertises and accepts the key binding algorithms `ES256`, `ES384`, `ES512`, `RS256`, `RS384`, `RS512`, `PS256`, `PS384`, `PS512` and `Ed25519` (also accepted as `EdDSA`), and advertises the signing algorithms of its `dc+sd-jwt` credential configurations for the credential itself.

The optional `redirect_uri` enables a same-device flow and must be registered for the client creating the request. After the wallet answers, CAS sends it to that URI with a fresh `response_code` in the fragment, as OpenID4VP recommends against session fixation, and releases the outcome only when the relying party presents that code. Without a `redirect_uri`, as in a cross-device flow with a QR code, the relying party polls for the outcome instead.

With response mode set to `DIRECT_POST_JWT`, the wallet encrypts its response, as the [OpenID4VC High Assurance Interoperability Profile](https://openid.net/specs/openid4vc-high-assurance-interoperability-profile-1_0-final.html) requires. CAS generates an ephemeral `P-256` key for each request and publishes it in the request’s client metadata, along with the content encryption algorithms it accepts:

```json
{
  "jwks": {
    "keys": [
      {
        "kty": "EC",
        "crv": "P-256",
        "kid": "TST-1-...",
        "use": "enc",
        "alg": "ECDH-ES",
        "x": "...",
        "y": "..."
      }
    ]
  },
  "encrypted_response_enc_values_supported": [
    "A128GCM",
    "A256GCM"
  ]
}
```

The wallet then posts a single `response` parameter: a JWE, encrypted with `ECDH-ES` to that key and naming it with `kid`, whose payload holds `vp_token` and `state`. A presentation posted in the clear is refused for such a request; an error response in the clear is still accepted from a wallet that cannot encrypt, as OpenID4VP allows. The default, `DIRECT_POST`, posts the response unencrypted.

A relying party may also choose per request by adding `"response_mode": "direct_post.jwt"` (or `"direct_post"`) to the presentation request. The setting acts as a floor: a request may ask for an encrypted response where the deployment does not require one, but asking for `direct_post` where `DIRECT_POST_JWT` is configured is refused.

With client identifier prefix set to `X509_SAN_DNS`, the request object is signed and served by reference. The signing key must carry an `x5c` certificate chain whose leaf names the issuer host as a DNS subject alternative name. The request carries that chain in its `x5c` header without a trailing self-signed trust anchor, as HAIP 1.0 requires. HAIP also requires the leaf not to be self-signed; a self-signed leaf is accepted with a warning in the logs.

With the client identifier prefix set to `X509_HASH`, which HAIP requires of verifiers that sign their requests, the request object is signed and served by reference the same way, and the client identifier is `x509_hash:` followed by the base64url-encoded SHA-256 hash of the DER-encoded leaf certificate, so the leaf needs no particular name. The signing key must still carry an `x5c` certificate chain.

### OpenID Federation Verifier

CAS can also identify itself to wallets with the `openid_federation` client identifier prefix of [OpenID4VP](https://openid.net/specs/openid-4-verifiable-presentations-1_0.html#section-5.9.3), as federation-based wallet ecosystems expect: the client identifier is `openid_federation:` followed by the issuer of CAS, without a trailing slash, which must be its [OpenID Federation](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-Federation.html) entity identifier, so CAS must take part in a federation as an OpenID Provider. The wallet resolves the trust chain of CAS up to a trust anchor it trusts and reads the verifier metadata, `openid_credential_verifier`, from the entity configuration of CAS ([OpenID Federation for Wallet Architectures](https://openid.net/specs/openid-federation-wallet-1_0.html)):

```json
{
  "openid_credential_verifier": {
    "client_name": "Apereo CAS",
    "vp_formats_supported": {
      "dc+sd-jwt": {
        "sd-jwt_alg_values": [
          "ES256"
        ],
        "kb-jwt_alg_values": [
          "ES256",
          "RS256",
          "Ed25519"
        ]
      }
    },
    "response_uris": [
      "https://sso.example.org/cas/oidc/oidcVcPresentationResponse"
    ],
    "jwks": {
      "keys": [
        {
          "kty": "EC",
          "crv": "P-256",
          "kid": "...",
          "use": "sig",
          "alg": "ES256",
          "x": "...",
          "y": "..."
        },
        {
          "kty": "EC",
          "crv": "P-256",
          "kid": "...",
          "use": "enc",
          "alg": "ECDH-ES",
          "x": "...",
          "y": "..."
        }
      ]
    },
    "encrypted_response_enc_values_supported": [
      "A128GCM",
      "A256GCM"
    ]
  }
}
```

- The request object is always signed and served by reference. It is signed with the OpenID Connect signing key, which must have a key id, and names it with `kid` instead of carrying a certificate chain; the wallet verifies it with the signing keys of the verifier metadata.
- The request carries no client metadata, which wallets must ignore under this prefix; they take the accepted formats and keys from the verifier metadata instead.
- Encrypted responses are encrypted to an encryption key of the OpenID Connect keystore, published in the verifier metadata with the algorithm to use with it: `ECDH-ES` for an elliptic curve key, as HAIP requires, or `RSA-OAEP-256` for an RSA key. Encrypted responses need such a key with a key id, or presentation requests are refused. Only current keys are published, but responses encrypted to a key that has since been rotated out of the keystore’s current state are still decrypted, since wallets may hold the verifier metadata from before the rotation.
- The response URI is the only trusted response URI. No request URIs are listed: each request object is served at a URI of its own, which a list could not name in advance, and wallets only refuse request URIs that are missing from the verifier metadata when it lists some.

The prefix can be chosen using CAS properties.

### ISO mdoc Presentations

An mdoc is requested by its document type, with claim paths made of the name space and the element:

```json
{
  "credentials": [
    {
      "id": "mdl",
      "format": "mso_mdoc",
      "doctype_value": "org.iso.18013.5.1.mDL",
      "claims": [
        {
          "path": [
            "org.iso.18013.5.1",
            "given_name"
          ]
        }
      ]
    }
  ]
}
```

The request then advertises `mso_mdoc` in its client metadata, with the COSE algorithms CAS accepts for the issuer and the device signatures. The wallet answers with a base64url-encoded CBOR `DeviceResponse`, as OpenID4VP 1.0 Appendix B.2 describes. CAS only accepts mdocs it issued: the leaf of the issuer signature’s `x5chain` must be the certificate of one of its signing keys, and the issuer-signed items must match the digests of the mobile security object, which must be within its validity period. The device signature must verify with the device key the mobile security object binds, over a session transcript built from the request’s client identifier, nonce and response URI, or from the origin and nonce of a Digital Credentials API request, and from the thumbprint of the key the response is encrypted to, if any. A presentation made for another request therefore does not verify. The disclosed claims are reported by name space.

### Digital Credentials API

A relying party may instead ask the wallet through the [W3C Digital Credentials API](https://www.w3.org/TR/digital-credentials/), as OpenID4VP 1.0 Appendix A describes, by setting `"response_mode"` to `dc_api` or `dc_api.jwt` (encrypted, as the High Assurance Interoperability Profile requires) and naming the origin of its page in the presentation request:

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
        }
      ]
    }
  ],
  "response_mode": "dc_api.jwt",
  "origin": "https://app.example.org"
}
```

The origin must be accepted by the client’s registered redirect URIs (as `https://app.example.org/`), since the wallet binds the presentation to it. CAS answers with a `digital_credentials_request` for the page to pass, as is, as a request to `navigator.credentials.get({digital: {requests: [...]}})`. It is unsigned (`openid4vp-v1-unsigned`) under the `REDIRECT_URI` client identifier prefix, and signed (`openid4vp-v1-signed`, with `expected_origins`) under an `x509` prefix. The page then posts what the API returned, with the request id, to the result endpoint, authenticated as the client that created the request:

```bash
POST /oidc/oidcVcPresentationResult
```

```json
{
  "request_id": "TST-1-...",
  "data": {
    "response": "eyJhbGciOiJFQ0RILUVTIiwi..."
  }
}
```

For `dc_api` the data holds the `vp_token` instead of the encrypted `response`. CAS verifies the presentation, which must be bound to `origin:` followed by the origin of the page, and answers with the outcome right away, as the result endpoint would; the request is consumed. Errors the wallet reports reach the page through the API and are not posted.

The relying party that created the request collects the outcome from:

```bash
GET /oidc/oidcVcPresentationResult?requestId=...&response_code=...
```

This endpoint requires the same client authentication as the request creation endpoint, and only the client that created the request may collect its outcome; any other client, or a same-device request without its `response_code`, gets `404`. It answers `{"status": "pending"}` while the wallet has not responded, and once it has, `{"status": "verified"}` together with the claims that were disclosed, keyed by credential query id. A wallet that declines or cannot answer posts an error response instead. Nothing authenticates that response, so anyone who saw the request could send one; it is recorded but does not end the request. The request keeps reporting `pending` until it expires, a valid presentation that arrives meanwhile takes precedence, and only a request that expired without one reports the error:

```json
{
  "status": "error",
  "error": "access_denied",
  "error_description": "The user declined"
}
```

The outcome is delivered once and then removed, so a second poll reports `404`, as does a request that expired unanswered.

CAS as a verifier trusts only itself. A presented credential is accepted when its `iss` is this deployment’s own issuer, its `vct` resolves to one of the credential configurations above, and its signature verifies against this deployment’s own signing key; `iat` and `exp` are both required. A credential carrying a `status` claim must point into a [status list](https://apereo.github.io/cas/development/authentication/OIDC-Authentication-Verifiable-Credentials.html#credential-status) CAS publishes, and its entry must be `VALID`; any other status reference is refused rather than accepted unchecked. There is no external issuer trust list, no `x5c` chain validation, no DID resolution and no OpenID Federation, so credentials issued elsewhere are rejected.

This is a trust policy rather than a protocol limitation: OpenID4VP leaves issuer trust to the verifier, noting that “Verifiers must verify that the issuer of a received presentation is trusted on their own”.

> **Note**
>
> Verified presentations are not connected to [Heimdall](https://apereo.github.io/cas/development/authorization/Heimdall-Authorization-Overview.html) authorization yet. Passing the claims of a verified presentation to AuthZEN requests as subject properties, so that authorization policies can decide on them, may be supported in the future. Until then, a relying party can send the disclosed claims it collected as `subject.properties` of its AuthZEN requests.

## Authorized Credential Types

The credential configurations above describe what the issuer is able to mint. They say nothing about which relying party may ask for what, so by default every registered client may obtain every credential the deployment defines. A service narrows that down with a verifiable credentials policy:

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

The entries in `allowedCredentialTypes` are credential configuration ids. A service that defines no policy, or whose policy lists no credential types, may obtain every credential configuration the issuer publishes; only a policy that actually names types restricts the service to those types. A policy can never widen a service beyond what the issuer publishes, so naming a credential configuration that does not exist grants nothing.

The policy is enforced wherever a credential type is claimed: when a credential offer transaction is created, when authorization details are turned into an authorization code, and again at the credential endpoint when the token is spent. That last check reads the policy afresh, so tightening a service takes effect against access tokens that are already outstanding.

## Credential Validity

Each credential configuration controls how long the credentials it issues remain valid via `credential-validity`, which defaults to thirty days. The value sets the `exp` claim of the issued credential, and the `validUntil` property for formats that carry one. A wallet stores a credential long after the issuance exchange has finished, so this period describes the useful life of the credential itself and is unrelated to the lifetime of the offer, the pre-authorized code, the nonce or the access token used to obtain it.

Claim values come from principal attributes. A value that reads as a number, such as `95.5`, is issued as a number; one whose number would not read back the same way, such as `02134`, is issued as text exactly as released.

## Credential Status

Issued credentials can carry a `status` claim, so they can be revoked or suspended after issuance, following the [Token Status List](https://datatracker.ietf.org/doc/draft-ietf-oauth-status-list/) specification as the [High Assurance Interoperability Profile](https://openid.net/specs/openid4vc-high-assurance-interoperability-profile-1_0.html) requires of SD-JWT VC credentials. Once turned on in CAS settings, every `dc+sd-jwt` credential references its own entry in a status list published by CAS:

```json
{
  "status": {
    "status_list": {
      "idx": 48213,
      "uri": "https://sso.example.org/cas/oidc/oidcVcStatusList/1"
    }
  }
}
```

Each credential gets a random, unpredictable index, unique among the unexpired credentials of its status list, and a new status list is started once a list has little room left. Entries are kept in the ticket registry and expire with their credential, so statuses survive restarts and are shared by all nodes that share the registry; an index may be reused once the credential that held it has expired. The stateless ticket registry cannot keep entries, so credentials are issued without status when it is used.

The status list is served by a public endpoint, which allows cross-origin requests:

```bash
GET /oidc/oidcVcStatusList/{id}
```

The response is a status list token, of type `application/statuslist+jwt`, signed with the issuer signing key and carrying its `x5c` certificate chain, if any. Each entry takes 2 bits, for the `VALID`, `INVALID` and `SUSPENDED` statuses, and verifiers may cache the token for its `ttl`; CAS caches the token it builds for as long, so a status change is visible to verifiers within that time.

```json
{
  "sub": "https://sso.example.org/cas/oidc/oidcVcStatusList/1",
  "iat": 1791238400,
  "exp": 1791324800,
  "ttl": 600,
  "status_list": {
    "bits": 2,
    "lst": "eNrtwTEBAAAAwqD1T20ND6AAAAAAAAAAAAAAAAAAAAAAAH4G...",
    "aggregation_uri": "https://sso.example.org/cas/oidc/oidcVcStatusListAggregation"
  }
}
```

Every status list token also names the status list aggregation as its `aggregation_uri`, and the authorization server metadata advertises it as `status_list_aggregation_endpoint`. The aggregation lists the URIs of all status lists that hold unexpired entries, so a verifier can fetch and cache them ahead of time; it is public and allows cross-origin requests as well:

```bash
GET /oidc/oidcVcStatusListAggregation
```

```json
{
  "status_lists": [
    "https://sso.example.org/cas/oidc/oidcVcStatusList/1",
    "https://sso.example.org/cas/oidc/oidcVcStatusList/2"
  ]
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.oidc.vc.issuer.status-list.enabled` (Optional; type `Boolean`; default `false`)
- `cas.authn.oidc.vc.issuer.status-list.expiration` (Optional; type `String`; default `PT24H`)
- `cas.authn.oidc.vc.issuer.status-list.size` (Optional; type `Integer`; default `131072`)
- `cas.authn.oidc.vc.issuer.status-list.time-to-live` (Optional; type `String`; default `PT10M`)

The status of issued credentials is managed through an actuator endpoint, which lists the credentials issued to a user and changes the status of one, to revoke, suspend or reinstate it:

`oidcVcStatus`

CAS endpoint · 2 operations · Not exposed by default · cas-server-support-oidc-vc

**GET** `oidcVcStatus/{principal}`: Get the status list entries of the verifiable credentials issued to a user. (1 param)

Parameters

| Name | In | Type | Description |
| --- | --- | --- | --- |
| `principal`required | path | – | The principal identifier |

Response

- **Returns**: `List`
- **Java method**: `getEntries(String)`
- **Defined in**: `OidcVerifiableCredentialStatusEndpoint`

Example request

```bash
curl -u "casuser:password" \
  -H "Accept: application/json" \
  "https://sso.example.org/cas/actuator/oidcVcStatus/{principal}"
```

**POST** `oidcVcStatus/{statusListId}`: Change the status of a verifiable credential. (3 params)

Parameters

| Name | In | Type | Description |
| --- | --- | --- | --- |
| `statusListId`required | path | – | The status list identifier |
| `index`required | path | – | The index in the status list |
| `status`required | query | – | VALID, INVALID or SUSPENDED |

Response

- **Returns**: `WebEndpointResponse`
- **Java method**: `updateStatus(String,long,String) `
- **Defined in**: `OidcVerifiableCredentialStatusEndpoint`

Example request

```bash
curl -u "casuser:password" \
  -X POST \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  "https://sso.example.org/cas/actuator/oidcVcStatus/{statusListId}?status=..."
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
    implementation "org.apereo.cas:cas-server-support-oidc-vc"
}
```

Turn the endpoint on and expose it over the web. One entry covers every operation. By default only `info`, `health` and `status` are exposed.

```properties
management.endpoint.oidcVcStatus.access=UNRESTRICTED
management.endpoints.web.exposure.include=oidcVcStatus
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
cas.monitor.endpoints.endpoint.oidcVcStatus.access=AUTHENTICATED
```

*Role*

```properties
cas.monitor.endpoints.endpoint.oidcVcStatus.access=ROLE
cas.monitor.endpoints.endpoint.oidcVcStatus.required-roles=ADMIN
```

*IP address*

```properties
cas.monitor.endpoints.endpoint.oidcVcStatus.access=IP_ADDRESS
cas.monitor.endpoints.endpoint.oidcVcStatus.required-ip-addresses=127.0.0.1
```

*Anyone*

```properties
cas.monitor.endpoints.endpoint.oidcVcStatus.access=PERMIT
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

- `cas.monitor.endpoints.endpoint.oidcVcStatus.access` (Optional; type `ActuatorEndpointProperties.EndpointAccessLevel`; default `DENY`): Define the security access level of the endpoint. Available values are as follows: - `PERMIT`: Allow open access to the endpoint. - `ANONYMOUS`: Allow anonymous access to the endpoint. - `DENY`: Block access to the endpoint. - `AUTHENTICATED`: Require authenticated access to the endpoint. - `ROLE`: Require authenticated access to the endpoint along with a role requirement. - `AUTHORITY`: Require authenticated access to the endpoint along with an authority requirement. - `IP_ADDRESS`: Require authenticated access to the endpoint using a collection of IP addresses.
- `cas.monitor.endpoints.endpoint.oidcVcStatus.required-authorities` (Optional; type `List<String>`): Required user authorities.
- `cas.monitor.endpoints.endpoint.oidcVcStatus.required-ip-addresses` (Optional; type `List<String>`): Required IP addresses. CIDR ranges are accepted.
- `cas.monitor.endpoints.endpoint.oidcVcStatus.required-roles` (Optional; type `List<String>`): Required user roles.
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
<Logger name="org.apereo.cas.oidc.vc.issuer.status" level="debug" additivity="false">
    <AppenderRef ref="console" />
    <AppenderRef ref="file" />
</Logger>
<Logger name="org.springframework.security" level="debug" additivity="false">
    <AppenderRef ref="console" />
    <AppenderRef ref="file" />
</Logger>
```

Key attestations and wallet attestations that carry a `status` claim are still accepted with a warning; their status lists are not fetched.

## Credential Formats

Each credential configuration is described in the issuer metadata the way its format requires. A `dc+sd-jwt` configuration publishes its `vct`. A `jwt_vc_json` configuration publishes a `credential_definition` with the credential `type`, and a `jwt_vc_json-ld` configuration adds its `@context`. The type is `VerifiableCredential` plus the configuration’s `scope`, or its id when no scope is set, and the context is the W3C Verifiable Credentials Data Model 2.0 base context alone, whose vocabulary covers the credential’s claims. Issued credentials carry exactly what the metadata publishes:

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

After claims are collected and validated, CAS signs the credential with its own issuer key, selected the same way as for other OpenID Connect artifacts and honoring the service’s `jwksKeyId` when one is set. The credential names that key with `kid` and does not say which client it was issued to, so verifiers cannot correlate the holder with the relying party; CAS as a verifier finds the key by `kid` as well.

A credential is not an ID token, and the relying party’s ID token settings do not apply to it. The algorithm is the first entry of the credential configuration’s `credential-signing-alg-values-supported` that the issuer’s signing key can perform, so the order of that list is a preference the deployment expresses, and that list is also the permitted set, so no other algorithm can be used. This is what keeps issuance consistent with the issuer metadata and with what a verifier, CAS included, accepts.

When the issuer signing key in the keystore carries an `x5c` certificate chain, the credential carries it as its `x5c` header, leaf certificate first, as HAIP 1.0 requires, so a verifier can take the issuer key from the leaf and validate the chain against its trust list. A trailing self-signed certificate is treated as the trust anchor and left out, as HAIP requires. HAIP also requires the leaf not to be self-signed; a self-signed signing certificate is still sent, and CAS logs a warning since wallets that follow HAIP may refuse it. A key without a chain produces credentials without `x5c`.

A service may narrow the algorithms used for its own credentials through its verifiable credentials policy:

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

As with `allowedCredentialTypes`, this can only narrow: the result is the intersection with what the credential configuration advertises, so naming an algorithm the configuration does not offer leaves the service with nothing and the request is refused. A policy that names no algorithms leaves the service with everything the configuration advertises.
