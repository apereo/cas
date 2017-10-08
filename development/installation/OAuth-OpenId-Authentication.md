---
layout: default
title: CAS - OAuth Authentication
---

# OAuth/OpenID Authentication

Allow CAS to act as an OAuth/OpenID authentication provider. Please [review the specification](https://oauth.net/2/) to learn more.

<div class="alert alert-info"><strong>CAS as OAuth Server</strong><p>This page specifically describes how to enable
OAuth/OpenID server support for CAS. If you would like to have CAS act as an OAuth/OpenID client communicating with
other providers (such as Google, Facebook, etc), <a href="../integration/Delegate-Authentication.html">see this page</a>.</p></div>

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

```xml
<dependency>
  <groupId>org.apereo.cas</groupId>
  <artifactId>cas-server-support-oauth-webflow</artifactId>
  <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#oauth2).

After enabling OAuth support, the following endpoints will be available:

## Endpoints

| Endpoint                        | Description                                                               | Method
|---------------------------------|--------------------------------------------------------------------------------------------------------------------
| `/oauth2.0/authorize`       | Authorize the user and start the CAS authentication flow.                 | `GET`
| `/oauth2.0/accessToken`     | Get an access token in plain-text or JSON                                 | `POST`
| `/oauth2.0/profile`         | Get the authenticated user profile in JSON via `access_token` parameter.  | `GET`


## Response/Grant Types

The following types are supported; they allow you to get an access token representing the current user and OAuth client application. With the access token, you'll be able to query the `/profile` endpoint and get the user profile.

### Authorization Code

The authorization code type is made for UI interactions: the user will enter his own credentials.

- `/oauth2.0/authorize?response_type=code&client_id=ID&redirect_uri=CALLBACK` returns the code as a parameter of the `CALLBACK` url
- `/oauth2.0/accessToken?grant_type=authorization_code&client_id=ID&client_secret=SECRET&code=CODE&redirect_uri=CALLBACK` returns the access token

### Token

The `token` type is also made for UI interactions as well as indirect non-interactive (i.e. Javascript) applications.

- `/oauth2.0/authorize?response_type=token&client_id=ID&redirect_uri=CALLBACK` returns the access token as an anchor parameter of the `CALLBACK` url.

### Resource Owner Credentials

The `password` grant type allows the OAuth client to directly send the user's credentials to the OAuth server.
This grant is a great user experience for trusted first party clients both on the web and in native device applications.

- `/oauth2.0/authorize?grant_type=password&client_id=ID&username=USERNAME&password=PASSWORD` returns the access token.

You may also pass along a `service` or `X-service` header value that identifies the target application url. The header value
must match the OAuth service definition in the registry that is linked to the client id.

### Client Credentials

The simplest of all of the OAuth grants, this grant is suitable for machine-to-machine authentication 
where a specific user’s permission to access data is not required.

- `/oauth2.0/authorize?grant_type=client_credentials&client_id=client&secret=secret`

### Refresh Token

The refresh token grant type retrieves a new access token from a refresh token (emitted for a previous access token),
when this previous access token is expired.

- `/oauth2.0/accessToken?grant_type=refresh_token&client_id=ID&client_secret=SECRET&refresh_token=REFRESH_TOKEN` returns the access token.


## Grant Type Selection

A grant is a method of acquiring an access token. Deciding which grants to implement depends on the type of client the end user will be using, and the experience you want for your users.

![](https://alexbilbie.com/images/oauth-grants.svg) 

To learn more about profiles and grant types, please [review this guide](https://alexbilbie.com/guide-to-oauth-2-grants/).

## Register Clients

Every OAuth client must be defined as a CAS service (notice the new *clientId* and *clientSecret* properties, specific to OAuth):

```json
{
  "@class" : "org.apereo.cas.support.oauth.services.OAuthRegisteredService",
  "clientId": "clientid",
  "clientSecret": "clientSecret",
  "serviceId" : "^(https|imaps)://<redirect-uri>.*",
  "name" : "My OAuth service ",
  "description" : "This is the description for this OAuth service.",
  "id" : 100
}
```

The following fields are supported:

| Field                             | Description
|-----------------------------------|---------------------------------------------------------------------------------
| `clientId`                        | The client identifier for the application/service.
| `clientSecret`                    | The client secret for the application/service.
| `supportedGrantTypes`             | Collection of supported grant types for this service.
| `supportedResponseTypes`          | Collection of supported response types for this service.
| `bypassApprovalPrompt`            | Whether approval prompt/consent screen should be bypassed. Default is `false`.
| `generateRefreshToken`            | Whether a refresh token should be generated along with the access token. Default is `false`.
| `jsonFormat`                      | Whether oauth responses for access tokens, etc should be produced as JSON. Default is `false`.
| `serviceId`                       | The pattern that authorizes the redirect URI(s), or same as `clientId` in case `redirect_uri` is not required by the grant type.

Service definitions are typically managed by the [service management](Service-Management.html) facility.

<div class="alert alert-warning"><strong>Usage Warning!</strong><p>CAS today does not strictly enforce the collection of authorized supported response/grant types for backward compatibility reasons. This means that if left undefined, all grant and response types may be allowed by the service definition and related policies. Do please note that this behavior is <strong>subject to change</strong> in future releases and thus, it is strongly recommended that all authorized grant/response types for each profile be declared in the service definition immediately to avoid surprises in the future.</p></div>

### Attribute Release

Attribute/claim filtering and release policies are defined per OAuth service.
See [this guide](../integration/Attribute-Release-Policies.html) for more info.

## OAuth Expiration Policy

The expiration policy for OAuth tokens is controlled by CAS settings and properties. Note that while access and refresh tokens may have their own lifetime and expiration policy, they are typically upper-bound to the length of the CAS single sign-on session.

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#oauth2).

## OAuth User Profile

The requested user profile may be rendered and consumed by the application using the following options.

The following alternative options are available.

### Nested

By default, the requested user profile is rendered using a `NESTED` format where the authenticated principal and attributes are placed inside `id` and `attributes` tags respectively in the final structure.

```json
{
  "id": "casuser",
  "attributes": {
    "email": "casuser@example.org",
    "name": "CAS"
  },
  "something": "else"
}
```

### Flat

This option flattens principal attributes by one degree, putting them at the same level as `id`. Other nested elements in the final payload are left untouched.

```json
{
  "id": "casuser",
  "email": "casuser@example.org",
  "name": "CAS",
  "something": "else"
}
```

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#oauth2).

### Custom

If you wish to create your own profile structure, you will need to design a component and register it with CAS to handle the rendering of the user profile:

```java
package org.apereo.cas.support.oauth;

@Configuration("MyOAuthConfiguration")
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MyOAuthConfiguration {

    @Bean
    @RefreshScope
    public OAuth20UserProfileViewRenderer oauthUserProfileViewRenderer() {
        ...
    }
}
```

[See this guide](Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.

## Server Configuration

Remember that OAuth features of CAS require session affinity (and optionally session replication),
as the authorization responses throughout the login flow
are stored via server-backed session storage mechanisms. You will need to configure your deployment environment and load balancers accordinngly.

# OpenID Authentication

To configure CAS to act as an OpenID provider, please [see this page](../protocol/OpenID-Protocol.html).
