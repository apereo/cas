---
layout: default
title: CAS - OAuth Authentication
category: Authentication
---
{% include variables.html %}


# OAuth Authentication

Allow CAS to act as an OAuth authentication provider. Please [review the specification](https://oauth.net/2/) to learn more.

<div class="alert alert-info"><strong>CAS as OAuth Server</strong><p>This page specifically describes how to enable
OAuth/OpenID server support for CAS. If you would like to have CAS act as an OAuth/OpenID client communicating with
other providers (such as Google, Facebook, etc), <a href="../integration/Delegate-Authentication.html">see this page</a>.</p></div>

## Actuator Endpoints
   
The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="oauthTokens" %}

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-oauth-webflow" %}

{% include_cached casproperties.html properties="cas.authn.oauth" excludes=".uma" %}

## Endpoints

After enabling OAuth support, the following endpoints will be available:

| Endpoint                                  | Description                                                                                                                                                                                                                                               | Method |
|-------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------|
| `/oauth2.0/authorize`                     | Authorize the user and start the CAS authentication flow.                                                                                                                                                                                                 | `GET`  |
| `/oauth2.0/accessToken`,`/oauth2.0/token` | Get an access token in plain-text or JSON                                                                                                                                                                                                                 | `POST` |
| `/oauth2.0/profile`                       | Get the authenticated user profile in JSON via `access_token` parameter.                                                                                                                                                                                  | `GET`  |
| `/oauth2.0/introspect`                    | Query CAS to detect the status of a given access token via [introspection](https://tools.ietf.org/html/rfc7662). This endpoint expects HTTP basic authentication with OAuth2 service `client_id` and `client_secret` associated as username and password. | `POST` |
| `/oauth2.0/device`                        | Approve device user codes via the [device flow protocol](https://tools.ietf.org/html/draft-denniss-oauth-device-flow).                                                                                                                                    | `POST` |
| `/oauth2.0/revoke`                        | [Revoke](https://tools.ietf.org/html/rfc7009) access or refresh tokens. This endpoint expects HTTP basic authentication with OAuth2 service `client_id` and `client_secret` associated as username and password.                                          |        |

## Response/Grant Types

The following types are supported; they allow you to get an access token representing the current user and OAuth
client application. With the access token, you'll be able to query the `/profile` endpoint and get the user profile.

### Authorization Code

The authorization code type is made for UI interactions: the user will enter credentials, shall receive a code and will exchange that code for an access token.

| Endpoint                | Parameters                                                                                               | Response                                         |
|-------------------------|----------------------------------------------------------------------------------------------------------|--------------------------------------------------|
| `/oauth2.0/authorize`   | `response_type=code&client_id=<ID>&redirect_uri=<CALLBACK>`                                              | OAuth code as a parameter of the `CALLBACK` url. |
| `/oauth2.0/accessToken` | `grant_type=authorization_code&client_id=ID`<br/>`&client_secret=SECRET&code=CODE&redirect_uri=CALLBACK` | The access token.                                |

#### Proof Key Code Exchange (PKCE)

The [Proof Key for Code Exchange](https://tools.ietf.org/html/rfc7636) (PKCE, pronounced pixie) extension describes a technique for public clients to mitigate the threat of having the authorization code intercepted. The technique involves the client first creating a secret, and then using that secret again when exchanging the authorization code for an access token. This way if the code is intercepted, it will not be useful since the token request relies on the initial secret.

The authorization code type at the authorization endpoint `/oauth2.0/authorize` is able to accept the following parameters to activate PKCE:

| Parameter               | Description                                                                       |
|-------------------------|-----------------------------------------------------------------------------------|
| `code_challenge`        | The code challenge generated using the method below.                              |
| `code_challenge_method` | `plain`, `S256`. This parameter is optional, where `plain` is assumed by default. |

The `/oauth2.0/accessToken`  endpoint is able to accept the following parameters to activate PKCE:

| Parameter       | Description                                                                                                          |
|-----------------|----------------------------------------------------------------------------------------------------------------------|
| `code_verifier` | The original code verifier for the PKCE request, that the app originally generated before the authorization request. |

If the method is `plain`, then the CAS needs only to check that the provided `code_verifier` matches the expected `code_challenge` string.
If the method is `S256`, then the CAS should take the provided `code_verifier` and transform it using the same method the client will have used initially. This means calculating the SHA256 hash of the verifier and base64-url-encoding it, then comparing it to the stored `code_challenge`.

If the verifier matches the expected value, then the CAS can continue on as normal, issuing an access token and responding appropriately.

### Token/Implicit

The `token` type is also made for UI interactions as well as indirect non-interactive (i.e. Javascript) applications.

| Endpoint              | Parameters                                               | Response                                                       |
|-----------------------|----------------------------------------------------------|----------------------------------------------------------------|
| `/oauth2.0/authorize` | `response_type=token&client_id=ID&redirect_uri=CALLBACK` | The access token as an anchor parameter of the `CALLBACK` url. |

### Resource Owner Credentials

The `password` grant type allows the OAuth client to directly send the user's credentials to the OAuth server.
This grant is a great user experience for trusted first party clients both on the web and in native device applications.

| Endpoint                | Parameters                                                                                                  | Response          |
|-------------------------|-------------------------------------------------------------------------------------------------------------|-------------------|
| `/oauth2.0/accessToken` | `grant_type=password&client_id=ID`<br/>`&client_secret=<SECRET>`<br/>`&username=USERNAME&password=PASSWORD` | The access token. |

Because there is no `redirect_uri` specified by this grant type, the service identifier recognized by CAS and matched in the service registry is taken as the `client_id` instead. You may optionally also pass along a `service` or `X-service` header value that identifies the target application url. The header value must match the OAuth service definition in the registry that is linked to the client id.

### Client Credentials

The simplest of all of the OAuth grants, this grant is suitable for machine-to-machine authentication
where a specific user’s permission to access data is not required.

| Endpoint                | Parameters                                                            | Response          |
|-------------------------|-----------------------------------------------------------------------|-------------------|
| `/oauth2.0/accessToken` | `grant_type=client_credentials&client_id=client&client_secret=secret` | The access token. |

Because there is no `redirect_uri` specified by this grant type, the service identifier recognized by CAS and matched in the service registry is taken as the `client_id` instead. You may optionally also pass along a `service` or `X-service` header value that identifies the target application url. The header value must match the OAuth service definition in the registry that is linked to the client id.

### Refresh Token

The refresh token grant type retrieves a new access token from a refresh token (emitted for a previous access token),
when this previous access token is expired.

| Endpoint                | Parameters                                                                                        | Response              |
|-------------------------|---------------------------------------------------------------------------------------------------|-----------------------|
| `/oauth2.0/accessToken` | `grant_type=refresh_token&client_id=<ID>`<br/>`&client_secret=SECRET&refresh_token=REFRESH_TOKEN` | The new access token. |

### Device Flow

| Endpoint                | Parameters                                                    | Response                                             |
|-------------------------|---------------------------------------------------------------|------------------------------------------------------|
| `/oauth2.0/accessToken` | `response_type=device_code&client_id=<ID>`                    | Device authorization url, device code and user code. |
| `/oauth2.0/accessToken` | `response_type=device_code&client_id=<ID>&code=<DEVICE_CODE>` | New access token once the user code is approved.     |

## Grant Type Selection

A grant is a method of acquiring an access token. Deciding which grants to implement 
depends on the type of client the end user will be using, and the experience you want for your users.

![](https://alexbilbie.com/images/oauth-grants.svg)

To learn more about profiles and grant types, please [review this guide](https://alexbilbie.com/guide-to-oauth-2-grants/).

## Client Registration

Please [see this guide](OAuth-Authentication-Clients.html).

## OAuth Token Expiration Policy

Please [see this guide](OAuth-Authentication-TokenExpirationPolicy.html).

## JWT Access Tokens

By default, OAuth access tokens are created as opaque identifiers. There is 
also the option to generate JWTs as access tokens on a per-service basis:

```json
{
    "@class" : "org.apereo.cas.support.oauth.services.OAuthRegisteredService",
    "clientId": "clientid",
    "clientSecret": "clientSecret",
    "serviceId" : "^(https|imaps)://<redirect-uri>.*",
    "name" : "OAuthService",
    "id" : 100,
    "jwtAccessToken": true,
    "properties" : {
      "@class" : "java.util.HashMap",
      "accessTokenAsJwtSigningKey" : {
         "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
         "values" : [ "java.util.HashSet", [ "..." ] ]
      },
      "accessTokenAsJwtEncryptionKey" : {
           "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
           "values" : [ "java.util.HashSet", [ "..." ] ]
      },
      "accessTokenAsJwtSigningEnabled" : {
         "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
         "values" : [ "java.util.HashSet", [ "true" ] ]
      },
      "accessTokenAsJwtEncryptionEnabled" : {
         "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
         "values" : [ "java.util.HashSet", [ "true" ] ]
      },
      "accessTokenAsJwtCipherStrategyType" : {
         "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
         "values" : [ "java.util.HashSet", [ "ENCRYPT_AND_SIGN" ] ]
      }
    }
}
```

Signing and encryption keys may also be defined on a per-service basis, or globally via CAS settings.

{% include_cached registeredserviceproperties.html groups="JWT_ACCESS_TOKENS" %}

## OAuth User Profile Structure

Please [see this guide](OAuth-Authentication-UserProfiles.html).

## Throttling

Authentication throttling may be enabled for the `/oauth2.0/accessToken` provided support 
is included in the overlay to [turn on authentication
throttling](Configuring-Authentication-Throttling.html) support. The throttling 
mechanism that handles the usual CAS server endpoints for authentication
and ticket validation, etc is then activated for the OAuth 
endpoints that are supported for throttling.

## CSRF Cookie Configuration

{% include_cached casproperties.html properties="cas.authn.oauth.csrf-cookie" %}

## Sample Client Applications

- [OAuth2 Sample Webapp](https://github.com/apereo/oauth2-sample-java-webapp)
