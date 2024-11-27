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

{% tab tokenexchangesvc <i class="fa fa-masks-theater px-1"></i> Impersonation %}

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

Delegation semantics are typically expressed in a token by including information about both the primary subject of 
the token as well as the actor to whom that subject has delegated some of its rights. Typically, in the request, 
the `subject_token` represents the identity of the party on behalf of whom the token is being requested while the 
`actor_token` represents the identity of the party to whom the access rights of the issued token are being delegated.

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
   
## Delegation vs. Impersonation

When principal `A` impersonates principal `B`, `A` is given all the rights that `B` has within some defined rights context and 
is indistinguishable from `B` in that context. Thus, when principal `A` impersonates principal `B`, then insofar as any 
entity receiving such a token is concerned, they are actually dealing with `B`. It is true that some members of the 
identity system might have awareness that impersonation is going on, but it is not a requirement. For all intents 
and purposes, when `A` is impersonating B, `A` is `B` within the context of the rights authorized by the token. 

With delegation semantics, principal `A` still has its own identity separate from `B`, and it is explicitly understood that while `B` 
may have delegated some of its rights to `A`, any actions taken are being taken by `A` representing `B`. 
In a sense, `A` is an agent for `B`.
