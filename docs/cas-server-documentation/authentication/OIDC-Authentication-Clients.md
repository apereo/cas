---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# Client Registration - OpenID Connect Authentication

Clients can be registered with CAS in the following ways.

## Static Registration 

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

Note that OpenID connect clients as service definitions are an 
extension of [OAuth services](OAuth-Authentication.html) in CAS. All settings 
that apply to an OAuth service definition should equally apply here as well. 
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
| `jwksKeyId`                   | Optional. JSON web key id to find in the keystore. 
| `jwksCacheDuration`           | Optional. The expiration policy time value applied to loaded/cached keys for this application.
| `jwksCacheTimeUnit`           | Optional. The expiration policy time unit of measure (i.e. `seconds`, `minutes`, etc) applied to loaded/cached keys.
| `encryptIdToken`              | Optional. Whether ID tokens should be encrypted. Default is `false`.
| `idTokenIssuer`               | Optional. Override the `iss` claim in the ID Token, which should only be used in special circumstances. Do **NOT** use this setting carelessly as the ID token's issuer **MUST ALWAYS** match the identity provider's issuer.
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

<div class="alert alert-info"><strong>Keep What You Need!</strong><p>You are encouraged to 
only keep and maintain properties and settings needed for a 
particular integration. It is UNNECESSARY to grab a copy of all service fields and try to 
configure them yet again based on their default. While 
you may wish to keep a copy as a reference, this strategy would ultimately lead to poor 
upgrades increasing chances of breaking changes and a messy 
deployment at that.</p></div>

Service definitions are typically managed and registered 
with CAS by the [service management](../services/Service-Management.html) facility.

<div class="alert alert-warning"><strong>Usage Warning!</strong><p>CAS today does not strictly 
enforce the collection of authorized supported 
response/grant types for backward compatibility reasons if left blank. This means that if left 
undefined, all grant and response types may be allowed by 
the service definition and related policies. Do please note that this behavior 
is <strong>subject to change</strong> in future releases 
and thus, it is strongly recommended that all authorized grant/response types for 
each profile be declared in the service definition 
immediately to avoid surprises in the future.</p></div>

## Dynamic Registration

Client applications may dynamically be registered with CAS for authentication. By default, CAS operates 
in a `PROTECTED` mode where the registration endpoint requires user authentication. This behavior may be relaxed via 
CAS settings to allow CAS to operate in an `OPEN` mode.

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
