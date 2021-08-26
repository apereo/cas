---
layout: default
title: CAS - OAuth Authentication
category: Authentication
---
{% include variables.html %}

# Token Expiration Policy - OAuth Authentication

The expiration policy for OAuth tokens is controlled by CAS settings and properties. Note that
while access and refresh tokens may have their own lifetime and expiration policy, they are 
typically upper-bound to the length of the CAS single sign-on session.
  
## OAuth Codes

{% include casproperties.html properties="cas.authn.oauth.code" %}

## OAuth Access Tokens

{% include casproperties.html properties="cas.authn.oauth.access-token" %}

## OAuthJWT Access Tokens

{% include casproperties.html properties="cas.authn.oauth.access-token" %}

## OAuth Device Tokens

{% include casproperties.html properties="cas.authn.oauth.device-token" %}

## OAuth Refresh Tokens

{% include casproperties.html properties="cas.authn.oauth.refresh-token" %}

## OAuth Device User Codes

{% include casproperties.html properties="cas.authn.oauth.device-user-code" %}

## Per Service

The expiration policy of certain OAuth tokens can be conditionally decided on a per-application basis. The candidate service
whose token expiration policy is to deviate from the default configuration must be designed as the following snippets demonstrate.

### OAuth Code

```json
{
  "@class" : "org.apereo.cas.support.oauth.services.OAuthRegisteredService",
  "clientId": "clientid",
  "clientSecret": "clientSecret",
  "serviceId" : "^(https|imaps)://<redirect-uri>.*",
  "name" : "OAuthService",
  "id" : 100,
  "codeExpirationPolicy": {
    "@class": "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthCodeExpirationPolicy",
    "numberOfUses": 1,
    "timeToLive": "10"
  }
}
```

### OAuth Access Token

```json
{
  "@class" : "org.apereo.cas.support.oauth.services.OAuthRegisteredService",
  "clientId": "clientid",
  "clientSecret": "clientSecret",
  "serviceId" : "^(https|imaps)://<redirect-uri>.*",
  "name" : "OAuthService",
  "id" : 100,
  "accessTokenExpirationPolicy": {
    "@class": "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthAccessTokenExpirationPolicy",
    "maxTimeToLive": "1000",
    "timeToKill": "100"
  }
}
```

### OAuth Device Token

```json
{
  "@class" : "org.apereo.cas.support.oauth.services.OAuthRegisteredService",
  "clientId": "clientid",
  "clientSecret": "clientSecret",
  "serviceId" : "^(https|imaps)://<redirect-uri>.*",
  "name" : "OAuthService",
  "id" : 100,
  "accessTokenExpirationPolicy": {
    "@class": "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthDeviceTokenExpirationPolicy",
    "timeToKill": "100"
  }
}
```

### OAuth Refresh Token

```json
{
  "@class" : "org.apereo.cas.support.oauth.services.OAuthRegisteredService",
  "clientId": "clientid",
  "clientSecret": "clientSecret",
  "serviceId" : "^(https|imaps)://<redirect-uri>.*",
  "name" : "OAuthService",
  "id" : 100,
  "accessTokenExpirationPolicy": {
    "@class": "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthRefreshTokenExpirationPolicy",
    "timeToKill": "100"
  }
}
```
