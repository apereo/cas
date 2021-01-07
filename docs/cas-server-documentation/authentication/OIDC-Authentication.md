---
layout: default
title: CAS - OpenID Connect Authentication
category: Authentication
---
{% include variables.html %}

# OpenID Connect Authentication

Allow CAS to act as an [OpenId Connect Provider (OP)](http://openid.net/connect/).

<div class="alert alert-info"><strong>Remember</strong><p>OpenId Connect is a continuation of the <a href="OAuth-OpenId-Authentication.html">OAuth protocol</a> with some additional variations. If you enable OpenId Connect, you will have automatically enabled OAuth as well. Options and behaviors that are documented for the <a href="OAuth-OpenId-Authentication.html">OAuth protocol</a> support may apply here just the same.</p></div>

Support is enabled by including the following dependency in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-oidc" %}

To learn more about OpenId Connect, please [review this guide](http://openid.net/specs/openid-connect-basic-1_0.html).

The current implementation provides support for:

- [Authorization Code Flow](http://openid.net/specs/openid-connect-basic-1_0.html)
- [Implicit Flow](https://openid.net/specs/openid-connect-implicit-1_0.html)
- [Dynamic Discovery](https://openid.net/specs/openid-connect-discovery-1_0.html)
- [WebFinger Issuer Discovery](https://openid.net/specs/openid-connect-discovery-1_0-21.html)
- Administration and registration of [OIDC clients and relying parties](../services/Service-Management.html).
- Administration and registration of [OIDC clients and relying parties](../services/Service-Management.html) via [Dynamic Client Registration protocol](https://tools.ietf.org/html/draft-ietf-oauth-dyn-reg-management-01).
- Ability to [resolve, map and release claims](../integration/Attribute-Release-Policies.html).
- Ability to configure expiration policies for various tokens.

## Endpoints

| Field                                     | Description
|-------------------------------------------|-------------------------------------------------------
| `/oidc/.well-known`                       | The discovery endpoint used to query for CAS OIDC configuration information and metadata.
| `/oidc/.well-known/openid-configuration`  | Same as `.well-known` discovery endpoint.
| `/oidc/.well-known/webfinger`             | [WebFinger](http://tools.ietf.org/html/rfc7033) discovery endpoint
| `/oidc/jwks`                              | Contains the server’s public signing keys, which clients may use to verify the digital signatures of access tokens and ID tokens issued by CAS.
| `/oidc/authorize`                         | Authorization requests are handled here.
| `/oidc/profile`                           | User profile requests are handled here.
| `/oidc/introspect`                        | Query CAS to detect the status of a given access token via [introspection](https://tools.ietf.org/html/rfc7662). This endpoint expects HTTP basic authentication with OIDC service `client_id` and `client_secret` associated as username and password.
| `/oidc/accessToken`, `/oidc/token`        | Produces authorized access tokens.
| `/oidc/revoke`                            | [Revoke](https://tools.ietf.org/html/rfc7009) access or refresh tokens. This endpoint expects HTTP basic authentication with OIDC service `client_id` and `client_secret` associated as username and password.
| `/oidc/register`                          | Register clients via the [dynamic client registration](https://tools.ietf.org/html/draft-ietf-oauth-dyn-reg-management-01) protocol.

## Register Clients

Clients can be registered with CAS in the following ways.

### Statically 

OpenID Connect clients can be *statically* registered with CAS as such:

```json
{
  "@class" : "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client",
  "clientSecret": "secret",
  "serviceId" : "^<https://the-redirect-uri>",
  "name": "OIDC",
  "id": 1000
}
```

Note that OpenID connect clients as service definitions are an extension of [OAuth services](OAuth-OpenId-Authentication.html) in CAS. All settings that apply to an OAuth service definition should equally apply here as well. 
The following fields are specifically available for OpenID connect services:

| Field                         | Description
|-------------------------------|---------------------------------------------------------------------------------------
| `clientId`                    | Required. The identifier for this client application.
| `clientSecret`                | Required. The secret for this client application.
| `serviceId`                   | Required. The authorized redirect URI for this OIDC client.
| `supportedGrantTypes`         | Optional. Collection of supported grant types for this service.
| `supportedResponseTypes`      | Optional. Collection of supported response types for this service.
| `signIdToken`                 | Optional. Whether ID tokens should be signed. Default is `true`.
| `jwks`                        | Optional. Resource path to the keystore location that holds the keys for this application.
| `jwksCacheDuration`           | Optional. The expiration policy time value applied to loaded/cached keys for this application.
| `jwksCacheTimeUnit`           | Optional. The expiration policy time unit of measure (i.e. `seconds`, `minutes`, etc) applied to loaded/cached keys.
| `encryptIdToken`              | Optional. Whether ID tokens should be encrypted. Default is `false`.
| `idTokenEncryptionAlg`        | Optional. The algorithm header value used to encrypt the id token.
| `idTokenSigningAlg`           | Optional. The algorithm header value used to sign the id token.
| `userInfoSigningAlg`          | Optional. The algorithm header value used to sign user profile responses.
| `userInfoEncryptedResponseAlg`   | Optional. The algorithm header value used to encrypt user profile responses.
| `tokenEndpointAuthenticationMethod`    | Optional. The requested client authentication method to the token endpoint. Default is `client_secret_basic`.
| `applicationType`             | Optional. `web`, `native`, or blank. Defined the kind of the application. The default, if omitted, is `web`. 
| `idTokenEncryptionEncoding`   | Optional. The algorithm method header value used to encrypt the id token.
| `userInfoEncryptedResponseEncoding`   | Optional. The algorithm method header value used to encrypt the user profile response.
| `subjectType`                 | Optional value chosen from `public` or `pairwise`. Type to use when generating principal identifiers. Default is `public`.
| `sectorIdentifierUri`         | Optional. Host value of this URL is used as the sector identifier for the pairwise identifier calculation. If left undefined, the host value of the `serviceId` will be used instead.

<div class="alert alert-info"><strong>Keep What You Need!</strong><p>You are encouraged to only keep and maintain properties and settings needed for a 
particular integration. It is UNNECESSARY to grab a copy of all service fields and try to configure them yet again based on their default. While 
you may wish to keep a copy as a reference, this strategy would ultimately lead to poor upgrades increasing chances of breaking changes and a messy 
deployment at that.</p></div>

Service definitions are typically managed and registered with CAS by the [service management](../services/Service-Management.html) facility.

<div class="alert alert-warning"><strong>Usage Warning!</strong><p>CAS today does not strictly enforce the collection of authorized supported 
response/grant types for backward compatibility reasons if left blank. This means that if left undefined, all grant and response types may be allowed by 
the service definition and related policies. Do please note that this behavior is <strong>subject to change</strong> in future releases 
and thus, it is strongly recommended that all authorized grant/response types for each profile be declared in the service definition 
immediately to avoid surprises in the future.</p></div>

### Dynamically

Client applications may dynamically be registered with CAS for authentication. By default, CAS operates 
in a `PROTECTED` mode where the registration endpoint requires user authentication. This behavior may be relaxed via 
CAS settings to allow CAS to operate in an `OPEN` mode.

## Configuration

{% include {{ version }}/oidc-configuration.md %}
   
### JWKS

{% include {{ version }}/jwks-oidc-configuration.md %}

### Scopes & Claims

{% include {{ version }}/claims-oidc-configuration.md %}

### Logout

{% include {{ version }}/logout-oidc-configuration.md %}

## Server Configuration

Remember that OpenID Connect features of CAS require session affinity (and optionally session replication),
as the authorization responses throughout the login flow are stored via server-backed session storage mechanisms. 
You will need to configure your deployment environment and load-balancers accordingly.

## Session Replication

{% include {{ version }}/session-replication-configuration.md %}

## Sample Client Applications

- [MITREid Sample Java Webapp](https://github.com/cas-projects/oidc-sample-java-webapp)

## Claims

Please [see this guide](OIDC-Authentication-Claims.html).

## Authentication Context Class

Support for authentication context class references is implemented in form of `acr_values` as part of the original 
authorization request, which is mostly taken into account by 
the [multifactor authentication features](../mfa/Configuring-Multifactor-Authentication.html) of CAS. 
Once successful, `acr` and `amr` values are passed back to the relying party as part of the id token.

## Pairwise Identifiers

When `pairwise` subject type is used, CAS will calculate a unique `sub` value for each sector identifier. This identifier 
should not be reversible by any party other than CAS and is somewhat akin to CAS generating persistent anonymous user 
identifiers. Each value provided to every relying party is different so as not 
to enable clients to correlate the user's activities without permission.

```json
{
  "@class" : "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client",
  "clientSecret": "secret",
  "serviceId" : "^<https://the-redirect-uri>",
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.PairwiseOidcRegisteredServiceUsernameAttributeProvider",
    "persistentIdGenerator" : {
      "@class" : "org.apereo.cas.authentication.principal.OidcPairwisePersistentIdGenerator",
      "salt" : "aGVsbG93b3JsZA=="
    }
  }
}
```

## Keystores

Each registered application in CAS can contain its own keystore as a `jwks` resource. By default,
a global keystore can be expected and defined via CAS properties. The format of the keystore
file is similar to the following:

```json
{
  "keys": [
    {
      "d": "...",
      "e": "AQAB",
      "n": "...",
      "kty": "RSA",
      "kid": "cas"
    }
  ]
}
```

CAS will attempt to auto-generate a keystore if it can't find one, but if you wish to generate one manually, 
a JWKS can be generated using [this tool](https://mkjwk.org/)
or [this tool](http://connect2id.com/products/nimbus-jose-jwt/generator).

## WebFinger Issuer Discovery

Please see [this guide](OIDC-Authentication-WebFinger.html).
