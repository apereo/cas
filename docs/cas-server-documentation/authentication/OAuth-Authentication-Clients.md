---
layout: default
title: CAS - OAuth Authentication
category: Authentication
---
{% include variables.html %}


# Client Registration - OAuth Authentication

Every OAuth relying party must be defined as a CAS service:

```json
{
  "@class" : "org.apereo.cas.support.oauth.services.OAuthRegisteredService",
  "clientId": "clientid",
  "clientSecret": "clientSecret",
  "serviceId" : "^(https|imaps)://<redirect-uri>.*",
  "name" : "OAuthService",
  "id" : 100,
  "supportedGrantTypes": [ "java.util.HashSet", [ "...", "..." ] ],
  "supportedResponseTypes": [ "java.util.HashSet", [ "...", "..." ] ],
  "scopes": [ "java.util.HashSet", [ "MyCustomScope" ] ],
  "audience": [ "java.util.HashSet", [ "MyAudience" ] ],
}
```

The following fields are supported:

| Field                                    | Description                                                                                                                                                                                                                                                                                                                                                                           |
|------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `serviceId`                              | The pattern that authorizes the redirect URI(s), or same as `clientId` in case `redirect_uri` is not required by the grant type (i.e `client_credentials`, etc). Note that the `redirect_uri` parameter that is ultimately matched against pattern must not have URL fragments, invalid schemes such as `javascript` or `data` or suspicious parameters such as `code`, `state`, etc. |
| `clientId`                               | The client identifier for the application/service.                                                                                                                                                                                                                                                                                                                                    |
| `clientSecret`                           | The client secret for the application/service. The client secret received from the service will be URL decoded before being compared to the secret in the CAS service definition.                                                                                                                                                                                                     |
| `userProfileViewType`                    | Formatting options for the [user profiles](OAuth-Authentication-UserProfiles.html); Default is undefined. Options are `NESTED`, `FLAT`.                                                                                                                                                                                                                                               |
| `scopes`                                 | Collection of authorized scopes for this service that act as a filter for the requested scopes in the authorization request.                                                                                                                                                                                                                                                          |
| `supportedGrantTypes`                    | Collection of supported grant types for this service.                                                                                                                                                                                                                                                                                                                                 |
| `supportedResponseTypes`                 | Collection of supported response types for this service.                                                                                                                                                                                                                                                                                                                              |
| `bypassApprovalPrompt`                   | Whether approval prompt/consent screen should be bypassed. Default is `false`.                                                                                                                                                                                                                                                                                                        |
| `generateRefreshToken`                   | Whether a refresh token should be generated along with the access token. Default is `false`.                                                                                                                                                                                                                                                                                          |
| `renewRefreshToken`                      | Whether the existing refresh token should be expired and a new one generated (and sent along) whenever a new access token is requested (with `grant_type` = `refresh_token`). Only possible if `generateRefreshToken` is set to `true`. Default is `false`.                                                                                                                           |
| `jwtAccessToken`                         | Whether access tokens should be created as JWTs. Default is `false`.                                                                                                                                                                                                                                                                                                                  |
| `jwtRefreshToken`                        | Whether refresh tokens should be created as JWTs. Default is `false`.                                                                                                                                                                                                                                                                                                                 |
| `jwtAccessTokenSigningAlg`               | The JWT signing algorithm to use for JWT access tokens. Defaults to the signing key's algorithm.                                                                                                                                                                                                                                                                                      |
| `introspectionSignedResponseAlg`         | Optional. The algorithm header value used to sign the JWT introspection response. Default is `RS512`.                                                                                                                                                                                                                                                                                 |
| `introspectionEncryptedResponseAlg`      | Optional. The algorithm header value used for content key encryption relevant for introspection JWT responses.                                                                                                                                                                                                                                                                        |
| `introspectionEncryptedResponseEncoding` | Optional. The algorithm method header value used to content encryption relevant for introspection JWT responses.                                                                                                                                                                                                                                                                      |
| `responseMode`                           | Allow CAS to alter the mechanism used for returning responses back to the client. [See this](OAuth-Authentication-Clients-ResponseMode.html).                                                                                                                                                                                                                                         |
| `audience`                               | Optional. Set of values that can control the `aud` field in JWT access tokens or ID tokens. If left undefined, the client ID will typically be used instead.                                                                                                                                                                                                                          |

<div class="alert alert-info">:information_source: <strong>Keep What You Need!</strong><p>You are encouraged to only keep and maintain 
properties and settings needed for a particular integration. It is <strong>UNNECESSARY</strong> to grab a copy of 
all service fields and try to configure them yet again based on their default. While you may wish to keep a copy as 
a reference, this strategy would ultimately lead to poor upgrades increasing chances of breaking changes and a messy deployment at that.</p></div>

Service definitions are typically managed by the [service management](../services/Service-Management.html) facility.

## Encryptable Client Secrets

Client secrets for OAuth relying parties may be defined as encrypted values prefixed with `{cas-cipher}`:

```json
{
  "@class": "org.apereo.cas.support.oauth.services.OAuthRegisteredService",
  "clientId": "clientid",
  "clientSecret": "{cas-cipher}eyJhbGciOiJIUzUxMiIs...",
  "serviceId" : "^(https|imaps)://<redirect-uri>.*",
  "name": "Sample",
  "id": 100
}
```

Client secrets may be encrypted using CAS-provided cipher operations 
either manually or via the [CAS Command-line shell](../installation/Configuring-Commandline-Shell.html).

{% include_cached casproperties.html properties="cas.authn.oauth" %}

## Attribute Release

Attribute/claim filtering and release policies are defined per OAuth service.
See [this guide](../integration/Attribute-Release-Policies.html) for more info.
