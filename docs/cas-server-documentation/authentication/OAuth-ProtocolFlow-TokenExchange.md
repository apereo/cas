---
layout: default
title: CAS - OAuth Protocol Flow - Token Exchange
category: Authentication
---

{% include variables.html %}

# OAuth Protocol Flow - Token Exchange

The token exchange protocol is an extension to OAuth 2.0 that allows one OAuth 2.0 token to be
exchanged for another type of token. This exchange typically occurs between different entities in a
system, such as a client exchanging an access token for another access token with different scopes, etc.

| Endpoint                | Parameters                                                                                              | Response                          |
|-------------------------|---------------------------------------------------------------------------------------------------------|-----------------------------------|
| `/oauth2.0/accessToken` | `grant_type=...&resource=https://...&subject_token=...&subject_token_type=...&requested_token_type=...` | New exchanged token and its type. |

## Application Configuration

The requested grant type must be `urn:ietf:params:oauth:grant-type:token-exchange`. This grant type must also be
specified for
the relevant registered service definition.

{% tabs tokenexchangesvc %}

{% tab tokenexchangesvc Impersonation %}

```json
{
  "@class" : "org.apereo.cas.support.oauth.services.OAuthRegisteredService",
  "clientId": "clientid",
  "clientSecret": "clientSecret",
  "serviceId" : "^(https|imaps)://<redirect-uri>.*",
  "name" : "OAuthService",
  "id" : 100,
  "supportedGrantTypes": [ "java.util.HashSet", [ "urn:ietf:params:oauth:grant-type:token-exchange" ] ],
  "tokenExchangePolicy": {
    "@class": "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthTokenExchangePolicy",
    "allowedResources": [ "java.util.HashSet", [ "..." ] ],
    "allowedAudience": [ "java.util.HashSet", [ "..." ] ],
    "allowedTokenTypes": [ "java.util.HashSet", [
      "urn:ietf:params:oauth:token-type:access_token",
      "urn:ietf:params:oauth:token-type:jwt"
    ] ]
  }
}
```

Furthermore, as it's shown the above example, one may define a dedicated token exchange policy that would control
the allowed resources, audience and token types for the token exchange operation. All fields defined for the
token exchange policy can accept regular expression patterns.

{% endtab %}

{% tab tokenexchangesvc Delegation %}

The following configuration is applicable to delegation token exchange flows where the client would provide
both a `subject_token` and an `actor_token`.

```json
{
  "@class" : "org.apereo.cas.support.oauth.services.OAuthRegisteredService",
  "clientId": "clientid",
  "clientSecret": "clientSecret",
  "serviceId" : "^(https|imaps)://<redirect-uri>.*",
  "name" : "OAuthService",
  "id" : 100,
  "supportedGrantTypes": [ "java.util.HashSet", [ "urn:ietf:params:oauth:grant-type:token-exchange" ] ],
  "tokenExchangePolicy": {
    "@class": "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthTokenExchangePolicy",
    "allowedActorTokenTypes": [ "java.util.HashSet", [
      "urn:ietf:params:oauth:token-type:access_token",
      "urn:ietf:params:oauth:token-type:jwt"
    ]],
    "requiredActorTokenAttributes" : {
      "@class" : "java.util.HashMap",
      "memberOf" : [ "java.util.HashSet", [ ".*can-act-as.*" ] ]
    }
  }
}
```

The subject (`sub` claim) of the subject_token is used to make the request. The actor (`act` claim) of the produced
token in the end
is the same as the subject of the `actor_token` used to make the request. This for example indicates delegation and
identifies an `admin`
as the current actor to whom authority has been delegated to act on behalf of a `user`.

```json
{
    "aud": "...",
    "iss":"...",
    "exp": 1234,
    "scope": "...",
    "sub": "user",
    "act": {
        "sub":"admin"
    }
}
```

{% endtab %}

{% endtabs %}

## Supported Exchanges

The following combinations of token types are supported for impersonation operations:

| Subject Token Type                              | Issued Token Type                               | Description                                                                                            |
|-------------------------------------------------|-------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| `urn:ietf:params:oauth:token-type:access_token` | `urn:ietf:params:oauth:token-type:access_token` | Exchange an access token for another.                                                                  |
| `urn:ietf:params:oauth:token-type:access_token` | `urn:ietf:params:oauth:token-type:jwt`          | Exchange an access token for another as JWT.                                                           |
| `urn:ietf:params:oauth:token-type:access_token` | `urn:ietf:params:oauth:token-type:id_token`     | Exchange an access token for an [OpenID Connect ID token](../authentication/OIDC-Authentication.html). |

The following combinations of token types are supported for delegation operations:

| Subject Token Type                              | Issued Token Type                               | Description                                  |
|-------------------------------------------------|-------------------------------------------------|----------------------------------------------|
| `urn:ietf:params:oauth:token-type:access_token` | `urn:ietf:params:oauth:token-type:access_token` | Exchange an access token for another.        |
| `urn:ietf:params:oauth:token-type:access_token` | `urn:ietf:params:oauth:token-type:jwt`          | Exchange an access token for another as JWT. |
