---
layout: default
title: CAS - Client Registration - OAuth Authentication
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
  "allowedResources": [ "java.util.HashSet", [ "https://api.example.org/" ] ],
  "audience": [ "java.util.HashSet", [ "MyAudience" ] ],
}
```

The following fields are supported:

| Field                                    | Description                                                                                                                                                                                                                                                                                                                                                                           |
|------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `serviceId`                              | The pattern that authorizes the redirect URI(s), or same as `clientId` in case `redirect_uri` is not required by the grant type (i.e `client_credentials`, etc). Note that the `redirect_uri` parameter that is ultimately matched against pattern must not have URL fragments, invalid schemes such as `javascript` or `data` or suspicious parameters such as `code`, `state`, etc. |
| `clientId`                               | The client identifier for the application/service.                                                                                                                                                                                                                                                                                                                                    |
| `clientSecret`                           | The [client secret](./OAuth-ClientSecret-Management.html) for the application/service. The client secret received from the service will be URL decoded before being compared to the secret in the CAS service definition.                                                                                                                                                             |
| `userProfileViewType`                    | Formatting options for the [user profiles](OAuth-Authentication-UserProfiles.html); Default is undefined. Options are `NESTED`, `FLAT`.                                                                                                                                                                                                                                               |
| `scopes`                                 | Collection of authorized scopes for this service that act as a filter for the requested scopes in the authorization request.                                                                                                                                                                                                                                                          |
| `allowedResources`                       | Collection of regular expressions for the resource URIs the client may request with the RFC 8707 `resource` parameter. [See this section](#resource-indicators).                                                                                                                                                                                                                      |
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
| `audience`                               | Optional. Set of values that can control the `aud` field in JWT access tokens, token introspection responses or ID tokens. If left undefined, the client ID will typically be used instead.                                                                                                                                                                                           |

<div class="alert alert-info">:information_source: <strong>Keep What You Need!</strong><p>You are encouraged to only keep and maintain 
properties and settings needed for a particular integration. It is <strong>UNNECESSARY</strong> to grab a copy of 
all service fields and try to configure them yet again based on their default. While you may wish to keep a copy as 
a reference, this strategy would ultimately lead to poor upgrades increasing chances of breaking changes and a messy deployment at that.</p></div>

Service definitions are typically managed by the [service management](../services/Service-Management.html) facility.

## Resource Indicators

A client may name the resources it needs an access token for with the [RFC 8707](https://www.rfc-editor.org/rfc/rfc8707) `resource`
parameter, once or more, on the authorization request, a pushed authorization request or request object, the device
authorization request, the CIBA backchannel authentication request, and the token request of the `password`, `client_credentials`,
`urn:ietf:params:oauth:grant-type:jwt-bearer` and `urn:ietf:params:oauth:grant-type:pre-authorized_code` grants. Each value must be
an absolute URI without a fragment that matches one of the regular expressions in the service's `allowedResources`,
or in the resources CAS allows for every client, in full, case-sensitively; otherwise, the request is refused with `invalid_target`. Since every entry is a regular
expression, escape characters such as `?` or `+` to match them literally, i.e. `"https://api\\.example\\.org/(orders|reports)"` in a JSON service definition.
The `allowedResources` of a [token exchange policy](OAuth-ProtocolFlow-TokenExchange.html) follow the same rule.

- Requested resources are added to the access token audience, after the `audience` of the service or the client ID.
  The JWT access token and the token introspection response report the same audience.
- The consent screen lists the requested resources, and a consent decision covers them: a decision remembered for some
  resources does not approve a request for others.
- When redeeming an authorization code or a refresh token, the token request may name some of the resources granted
  to the code or refresh token, and the access token is issued for those only. Naming any other resource is refused with
  `invalid_target`. A refresh token keeps all the resources of its grant. When the grant carries no resources, the
  token request may name any resource that matches the service's `allowedResources` or the resources CAS allows for every client.
- A [token exchange](OAuth-ProtocolFlow-TokenExchange.html) issues tokens for its requested `audience` or `resource`
  only, without the resources of the exchanged token.

### Resources For Every Client

Resources defined in CAS settings may be requested by every client, in addition to the
`allowedResources` of its service. This suits resources hosted by CAS itself, such as the [CAS MCP server](../installation/Admin-MCP-Server.html),
which clients that [register themselves dynamically](OIDC-Authentication-Dynamic-Registration.html) cannot list in their own registration.
Entries are regular expressions and follow the same matching rule. By default, CAS allows its MCP server endpoint,
i.e. `\Qhttps://sso.example.org/cas/mcp\E`, built from the CAS server prefix and the MCP endpoint path; defining these resources
replaces that default, so list the MCP server endpoint again if clients should still request it.

{% include_cached casproperties.html properties="cas.authn.oauth.core.allowed-resources" %}

## Attribute Release

Attribute/claim filtering and release policies are defined per OAuth service.
See [this guide](../integration/Attribute-Release-Policies.html) for more info.
