---
layout: default
title: CAS - OAuth Authentication
category: Authentication
---
{% include variables.html %}


# OAuth Authentication

Allow CAS to act as an OAuth authentication provider. Please [review the specification](https://oauth.net/2/) to learn more.

<div class="alert alert-info">:information_source: <strong>CAS as OAuth Server</strong><p>This page specifically describes how to enable
OAuth identity provider server support for CAS. If you would like to have CAS act as an OAuth/OpenID client communicating with
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

| Endpoint                                  | Description                                                                                                                                                                                                      | Method |
|-------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------|
| `/oauth2.0/authorize`                     | Authorize the user and start the CAS authentication flow.                                                                                                                                                        | `GET`  |
| `/oauth2.0/accessToken`,`/oauth2.0/token` | Get an access token in plain-text or JSON                                                                                                                                                                        | `POST` |
| `/oauth2.0/profile`                       | Get the authenticated user profile in JSON via `access_token` parameter.                                                                                                                                         | `GET`  |
| `/oauth2.0/introspect`                    | Query CAS to detect the status of a given access token via [introspection](OAuth-Authentication-TokenIntrospection.html)                                                                                         | `POST` |
| `/oauth2.0/device`                        | Approve device user codes via the [device flow protocol](https://tools.ietf.org/html/draft-denniss-oauth-device-flow).                                                                                           | `POST` |
| `/oauth2.0/revoke`                        | [Revoke](https://tools.ietf.org/html/rfc7009) access or refresh tokens. This endpoint expects HTTP basic authentication with OAuth2 service `client_id` and `client_secret` associated as username and password. | `POST` |

## Protocol Flows

The following protocol flows, response and grant types are supported.

| Flow                      | Resource                                                      | 
|---------------------------|---------------------------------------------------------------|
| Authorization Code / PKCE | [See this page](OAuth-ProtocolFlow-AuthorizationCode.html).   |
| Client Credentials        | [See this page](OAuth-ProtocolFlow-ClientCredentials.html).   |
| Device Authorization      | [See this page](OAuth-ProtocolFlow-DeviceAuthorization.html). |
| Token Exchange            | [See this page](OAuth-ProtocolFlow-TokenExchange.html).       |
| Token / Implicit          | [See this page](OAuth-ProtocolFlow-Implicit.html).            |
| Refresh Token             | [See this page](OAuth-ProtocolFlow-RefreshToken.html).        |
| Resource Owner            | [See this page](OAuth-ProtocolFlow-ResourceOwner.html).       |

## Throttling

Authentication throttling may be enabled for the `/oauth2.0/accessToken` provided support 
is included in the overlay to [turn on authentication throttling](Configuring-Authentication-Throttling.html) support. The throttling 
mechanism that handles the usual CAS server endpoints for authentication
and ticket validation, etc is then activated for the OAuth endpoints that are supported for throttling.

## Sample Client Applications

- [OAuth2 Sample Webapp](https://github.com/apereo/oauth2-sample-java-webapp)

## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="org.apereo.cas.oauth" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
<Logger name="PROTOCOL_MESSAGE" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
...
```
