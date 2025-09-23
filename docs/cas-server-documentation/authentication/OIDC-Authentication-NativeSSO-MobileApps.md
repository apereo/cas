---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# OpenID Connect - Native SSO for Mobile Apps

Native SSO is a new OpenID Connect extension designed to streamline the user experience 
mobile or desktop app suites by utilising a device session. If you are a vendor that offers a suite of mobile or desktop 
applications that require the user to be authenticated, this may relevant for you. Single-app vendors are not concerned 
with this OpenID Connect extension for device-based single sign-on (SSO) as 
it relies on the sharing of an ID token and a device-bound secret between the applications 
participating in the SSO. This requires a level of trust between the apps that is only 
practical when they belong to the same vendor.

The flow sequence in summary is as follows:

- The client app #1 performs a regular `code` flow to authenticate the end-user, using the special `device_sso` scope value to signal its intent to obtain a `device_secret` for the purposes of native SSO.
- The client app #2 uses the native SSO to sign-in the end-user and obtain tokens by making a direct back-channel token request, saving the end-user from the redirection to the OpenID provider to login into the app.
         
## Application Configuration

Client applications and relying parties must be configured to support the `device_sso` scope. 
This scope is used to indicate that the application intends to use the native SSO feature.

```json
{
  "@class" : "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "...",
  "clientSecret": "...",
  "serviceId" : "...",
  "name": "OIDC",
  "id": 1,
  "scopes" : [ "java.util.HashSet", 
    [ "openid", "profile", "device_sso" ]
  ]
}
```
        
Once tokens are exchanged, the above scope allows the authentication flow to eventually generate a `device_secret`:

```json
{
  "access_token"  : "...",
  "token_type"    : "Bearer",
  "expires_in"    : 600,
  "scope"         : "openid profile",
  "id_token"      : "eyJraWQiOiIxZTlnZGs3IiwiYWxnIjoiUl...",
  "device_secret" : "..."
}
```

Client app #2 that finds an ID token and a `device_secret` available in its 
credential store shared with the vendor’s other apps may assume the 
user is already signed in and thus proceed with a direct back-channel token exchange request to CAS.
                                                                                          
## Token Exchange Request
       
A [token exchange request](OAuth-ProtocolFlow-TokenExchange.html) requires the following:

- The client application must be configured to support the `urn:ietf:params:oauth:grant-type:token-exchange` grant type.
- The token exchange policy for the client application must allow the `urn:openid:params:token-type:device-secret` token type.

```json
{
  "@class" : "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "...",
  "clientSecret": "...",
  "serviceId" : "...",
  "name": "OIDC",
  "id": 1,
  "scopes" : [ "java.util.HashSet", [ "openid", "email", "profile", "device_sso" ] ],
  "supportedGrantTypes": [ "java.util.HashSet", 
    [ "authorization_code", "urn:ietf:params:oauth:grant-type:token-exchange" ] ],
  "tokenExchangePolicy": {
    "@class": "org.apereo.cas.support.oauth.services.DefaultRegisteredServiceOAuthTokenExchangePolicy",
    "allowedTokenTypes": [ "java.util.HashSet", [
      "urn:openid:params:token-type:device-secret",
      "urn:ietf:params:oauth:token-type:access_token",
      "urn:ietf:params:oauth:token-type:id_token"
    ] ]
  }
}
```

When making the request,

- The `subject_token` request parameter must be set to the ID token obtained.
- The `subject_token_type` request parameter must be set to `urn:ietf:params:oauth:token-type:id_token`.
- The `actor_token` request parameter must be set to the `device_secret` obtained.
- The `actor_token_type` request parameter must be set to `urn:openid:params:token-type:device-secret`.
- The `requested_token_type` request parameter must be set to `urn:ietf:params:oauth:token-type:access_token` or `urn:ietf:params:oauth:token-type:id_token`.
