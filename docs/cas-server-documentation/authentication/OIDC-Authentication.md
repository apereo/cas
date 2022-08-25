---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# OpenID Connect Authentication

Allow CAS to act as an [OpenId Connect Provider (OP)](http://openid.net/connect/).

<div class="alert alert-info"><strong>Remember</strong><p>OpenId Connect is a continuation of 
the <a href="OAuth-Authentication.html">OAuth protocol</a> with some additional variations. If 
you enable OpenId Connect, you will have automatically enabled OAuth as well. Options and 
behaviors that are documented for the <a href="OAuth-Authentication.html">OAuth protocol</a> 
support may apply here just the same.</p></div>

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-oidc" %}

To learn more about OpenId Connect, please [review this guide](http://openid.net/specs/openid-connect-basic-1_0.html).

The current implementation provides support for:

- [Authorization Code Flow](http://openid.net/specs/openid-connect-basic-1_0.html)
- [Implicit Flow](https://openid.net/specs/openid-connect-implicit-1_0.html)
- [Dynamic Discovery](https://openid.net/specs/openid-connect-discovery-1_0.html)
- [WebFinger Issuer Discovery](https://openid.net/specs/openid-connect-discovery-1_0-21.html)
- Administration and registration of [OIDC clients and relying parties](../services/Service-Management.html).
- Administration and registration of [OIDC clients and relying parties](../services/Service-Management.html) via [Dynamic Client Registration protocol](https://tools.ietf.org/html/draft-ietf-oauth-dyn-reg-management-01).
- Ability to [resolve, map and release claims](../integration/Attribute-Release-Policies.html).
- Ability to configure expiration policies for various tokens.

## Endpoints

| Field                                    | Description                                                                                                                                                                                                                                                                                             |
|------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `/oidc/.well-known`                      | The discovery endpoint used to query for CAS OIDC configuration information and metadata.                                                                                                                                                                                                               |
| `/oidc/.well-known/openid-configuration` | Same as `.well-known` discovery endpoint.                                                                                                                                                                                                                                                               |
| `/oidc/.well-known/webfinger`            | [WebFinger](https://tools.ietf.org/html/rfc7033) discovery endpoint                                                                                                                                                                                                                                     |
| `/oidc/jwks`                             | Contains the [server’s public keys](OIDC-Authentication-JWKS.html), which clients may use to verify the digital signatures of access tokens and ID tokens issued by CAS. Accepts an optional `state` query parameter to narrow down keys by their current state (i.e. `current`, `previous`, `future`). |
| `/oidc/authorize`                        | Authorization requests are handled here.                                                                                                                                                                                                                                                                |
| `/oidc/profile`                          | User profile requests are handled here.                                                                                                                                                                                                                                                                 |
| `/oidc/logout`                           | Logout requests are handled here.                                                                                                                                                                                                                                                                       |
| `/oidc/introspect`                       | Query CAS to detect the status of a given access token via [introspection](https://tools.ietf.org/html/rfc7662). This endpoint expects HTTP basic authentication with OIDC service `client_id` and `client_secret` associated as username and password.                                                 |
| `/oidc/accessToken`, `/oidc/token`       | Produces authorized access tokens.                                                                                                                                                                                                                                                                      |
| `/oidc/revoke`                           | [Revoke](https://tools.ietf.org/html/rfc7009) access or refresh tokens. This endpoint expects HTTP basic authentication with OIDC service `client_id` and `client_secret` associated as username and password.                                                                                          |
| `/oidc/register`                         | Register clients via the [dynamic client registration](https://tools.ietf.org/html/draft-ietf-oauth-dyn-reg-management-01) protocol.                                                                                                                                                                    |
| `/oidc/initToken`                        | Obtain an initial *master* access token required for dynamic client registration when operating in `PROTECTED` mode.                                                                                                                                                                                    |
| `/oidc/clientConfig`                     | [Update or retrieve client](OIDC-Authentication-Dynamic-Registration.html) application definitions, registered with the server.                                                                                                                                                                         |

<div class="alert alert-warning"><strong>Use Discovery</strong><p>The above endpoints
are not strictly defined in the OpenID Connect specification. The CAS software may choose to change URL endpoints
at any point in time. Do <strong>NOT</strong> hardcode these endpoints in your application configuration.
Instead, use the Dynamic Discovery endpoint and parse the discovery document to discover the endpoints.</p></div>

## Configuration

{% include_cached casproperties.html properties="cas.authn.oidc.core" %}

## Server Configuration

Remember that OpenID Connect features of CAS require session affinity (and optionally session replication),
by default as the authorization responses throughout the login flow are stored via server-backed session storage mechanisms. 
You will need to configure your deployment environment and load-balancers accordingly.

## Sample Client Applications

- [MITREid Sample Java Webapp](https://github.com/apereo/oidc-sample-java-webapp)

## Authentication Context Class

Support for authentication context class references is implemented in form of `acr_values` as part of the original 
authorization request, which is mostly taken into account by 
the [multifactor authentication features](../mfa/Configuring-Multifactor-Authentication.html) of CAS. 
Once successful, `acr` and `amr` values are passed back to the relying party as part of the id token.
