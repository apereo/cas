---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# Clients - OpenID Connect Authentication

Clients can be registered with CAS in the following ways.

Note that OpenID connect clients as service definitions are an
extension of [OAuth services](OAuth-Authentication-Clients.html) in CAS. All settings
that apply to an OAuth service definition should equally apply here as well.

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

<div class="alert alert-info">:information_source: <strong>Redirect URIs</strong><p>Client application redirect URIs are specified
using the <code>serviceId</code> field which supports regular expression patterns. If you need to support multiple URIs, you can
try to <i>OR</i> them together or you may be able to construct the pattern that supports and matches all URIs with minor changes.</p></div>

The following fields are specifically available for OpenID connect services:

| Version                             | Reference                                                                                                                                                                                                                     |
|-------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `clientId`                          | Required. The identifier for this client application.                                                                                                                                                                         |     
| `clientSecret`                      | Required. The secret for this client application. The client secret received from the service will be URL decoded before being compared to the secret in the CAS service definition.                                          |     
| `clientSecretExpiration`            | Optional. Time, measured in UTC epoch, at which the `client_secret` will expire or 0 if it will not expire.                                                                                                                   |     
| `serviceId`                         | Required. The authorized redirect URI for this OIDC client.                                                                                                                                                                   |     
| `supportedGrantTypes`               | Optional. Collection of [supported grant types](OAuth-Authentication-Clients-ResponsesGrants.html) for this service.                                                                                                          |
| `supportedResponseTypes`            | Optional. Collection of [supported response types](OAuth-Authentication-Clients-ResponsesGrants.html) for this service.                                                                                                       |
| `signIdToken`                       | Optional. Whether ID tokens should be signed. Default is `true`.                                                                                                                                                              |     
| `jwks`                              | Optional. Resource path to the keystore location that holds the keys for this application.                                                                                                                                    |     
| `jwksKeyId`                         | Optional. JSON web key id to find in the keystore.                                                                                                                                                                            |     
| `jwksCacheDuration`                 | Optional. The expiration policy duration, i.e. `PT1S`, applied to loaded/cached keys for this application.<br/>                                                                                                               |
| `encryptIdToken`                    | Optional. Whether ID tokens should be encrypted. Default is `false`.                                                                                                                                                          |     
| `idTokenEncryptionOptional`         | Optional. Whether ID tokens encryption should be skipped if no keystore or encryption key is available. Default is `false`.                                                                                                   |
| `includeIdTokenClaims`              | Optional. Whether ID token claims should forcefully be included regardless of requested/allowed response or grant type. Default is `false`.                                                                                   |     
| `idTokenIssuer`                     | Optional. Override the `iss` claim in the ID Token, which should only be used in special circumstances. Do **NOT** use this setting carelessly as the ID token's issuer **MUST ALWAYS** match the identity provider's issuer. |     
| `idTokenSigningAlg`                 | Optional. The algorithm header value used to sign the id token.                                                                                                                                                               |     
| `idTokenEncryptionAlg`              | Optional. The algorithm header value used to encrypt the id token.                                                                                                                                                            |     
| `idTokenEncryptionEncoding`         | Optional. The algorithm method header value used to encrypt the id token.                                                                                                                                                     |     
| `userInfoSigningAlg`                | Optional. The algorithm header value used to sign user profile responses.                                                                                                                                                     |     
| `userInfoEncryptedResponseAlg`      | Optional. The algorithm header value used to encrypt user profile responses.                                                                                                                                                  |     
| `userInfoEncryptedResponseEncoding` | Optional. The algorithm method header value used to encrypt the user profile response.                                                                                                                                        |     
| `tokenEndpointAuthenticationMethod` | Optional. The requested [client authentication method](OIDC-Authentication-AccessToken-AuthMethods.html) to the token endpoint.                                                                                               |
| `applicationType`                   | Optional. `web`, `native`, or blank. Defined the kind of the application. The default, if omitted, is `web`.                                                                                                                  |     
| `subjectType`                       | Optional value chosen from `public` or `pairwise`. Type to use when generating principal identifiers. Default is `public`.                                                                                                    |     
| `sectorIdentifierUri`               | Optional. Host value of this URL is used as the sector identifier for the pairwise identifier calculation. If left undefined, the host value of the `serviceId` will be used instead.                                         |     

<div class="alert alert-info">:information_source: <strong>Keep What You Need!</strong><p>You are encouraged to 
only keep and maintain properties and settings needed for a particular integration. It is UNNECESSARY to grab a copy of all service fields and try to 
configure them yet again based on their default. While you may wish to keep a copy as a reference, this strategy would ultimately lead to poor 
upgrades increasing chances of breaking changes and a messy deployment at that.</p></div>

Service definitions are typically managed and registered with CAS by the [service management](../services/Service-Management.html) facility.

{% include_cached casproperties.html properties="cas.authn.oidc.services" %}

## Example

An example registration record for an OpenID Connect relying party follows that allows the application with the redirect URI `https://app.example.org/oidc`
to send authorization requests to CAS using the *authorization code* authentication flow. The registration record also instructs CAS to bypass the 
approval/consent screen and to assume access to requested scopes and claims should be granted automatically without the user's explicit permission.

```json
{
  "@class": "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client-id",
  "clientSecret": "secret",
  "serviceId": "^https://app.example.org/oidc",
  "name": "MyApplication",
  "id": 1,
  "bypassApprovalPrompt": true,
  "supportedResponseTypes": [ "java.util.HashSet", [ "code" ] ],
  "supportedGrantTypes": [ "java.util.HashSet", [ "authorization_code" ] ],
  "scopes" : [ "java.util.HashSet", [ "profile", "openid", "email" ] ]
}
```

## Dynamic Registration

Client applications may dynamically be registered with CAS for authentication. 
[See this guide](OIDC-Authentication-Dynamic-Registration.html) for more info.
