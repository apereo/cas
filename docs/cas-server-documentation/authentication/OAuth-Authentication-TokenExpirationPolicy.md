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

{% include_cached casproperties.html properties="cas.authn.oauth.code" %}

## OAuth Access Tokens

{% include_cached casproperties.html properties="cas.authn.oauth.access-token" %}

## OAuth Device Tokens

{% include_cached casproperties.html properties="cas.authn.oauth.device-token" %}

## OAuth Refresh Tokens

{% include_cached casproperties.html properties="cas.authn.oauth.refresh-token" %}

## OAuth Device User Codes

{% include_cached casproperties.html properties="cas.authn.oauth.device-user-code" %}

## Per Service

The expiration policy of certain OAuth tokens can be conditionally decided on a per-application basis. The candidate service
whose token expiration policy is to deviate from the default configuration must be designed as the following snippets demonstrate.

{% tabs oauthexppolicy %}

{% tab oauthexppolicy OAuth Code %}

The expiration policy of codes can be defined on a per application basis:

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
    "timeToLive": "PT10S"
  }
}
```

{% endtab %}

{% tab oauthexppolicy OAuth Access Tokens %}

The expiration policy of access tokens can be defined on a per application basis:

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
    "maxTimeToLive": "PT1000S",
    "timeToKill": "PT100S",
    "maxActiveTokens": 0
  }
}
```

{% endtab %}

{% tab oauthexppolicy OAuth Device Tokens %}

The expiration policy of device tokens can be defined on a per application basis:

```json
{
  "@class" : "org.apereo.cas.support.oauth.services.OAuthRegisteredService",
  "clientId": "clientid",
  "clientSecret": "clientSecret",
  "serviceId" : "^(https|imaps)://<redirect-uri>.*",
  "name" : "OAuthService",
  "id" : 100,
  "deviceTokenExpirationPolicy": {
    "@class": "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthDeviceTokenExpirationPolicy",
    "timeToKill": "PT100S"
  }
}
```

{% endtab %}

{% tab oauthexppolicy OAuth Refresh Tokens %}

The expiration policy of refresh tokens can be defined on a per application basis:

```json
{
  "@class" : "org.apereo.cas.support.oauth.services.OAuthRegisteredService",
  "clientId": "clientid",
  "clientSecret": "clientSecret",
  "serviceId" : "^(https|imaps)://<redirect-uri>.*",
  "name" : "OAuthService",
  "id" : 100,
  "refreshTokenExpirationPolicy": {
    "@class": "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthRefreshTokenExpirationPolicy",
    "timeToKill": "PT100S",
    "maxActiveTokens": 0
  }
}
```

{% endtab %}

{% endtabs %}
