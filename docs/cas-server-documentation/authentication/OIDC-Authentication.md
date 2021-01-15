---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# OpenID Connect Authentication

Allow CAS to act as an [OpenId Connect Provider (OP)](http://openid.net/connect/).

<div class="alert alert-info"><strong>Remember</strong><p>OpenId Connect is a continuation of the <a href="OAuth-Authentication.html">OAuth protocol</a> with some additional variations. If you enable OpenId Connect, you will have automatically enabled OAuth as well. Options and behaviors that are documented for the <a href="OAuth-OpenId-Authentication.html">OAuth protocol</a> support may apply here just the same.</p></div>

Support is enabled by including the following dependency in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-oidc" %}

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

| Field                                     | Description
|-------------------------------------------|-------------------------------------------------------
| `/oidc/.well-known`                       | The discovery endpoint used to query for CAS OIDC configuration information and metadata.
| `/oidc/.well-known/openid-configuration`  | Same as `.well-known` discovery endpoint.
| `/oidc/.well-known/webfinger`             | [WebFinger](http://tools.ietf.org/html/rfc7033) discovery endpoint
| `/oidc/jwks`                              | Contains the server’s public signing keys, which clients may use to verify the digital signatures of access tokens and ID tokens issued by CAS.
| `/oidc/authorize`                         | Authorization requests are handled here.
| `/oidc/profile`                           | User profile requests are handled here.
| `/oidc/introspect`                        | Query CAS to detect the status of a given access token via [introspection](https://tools.ietf.org/html/rfc7662). This endpoint expects HTTP basic authentication with OIDC service `client_id` and `client_secret` associated as username and password.
| `/oidc/accessToken`, `/oidc/token`        | Produces authorized access tokens.
| `/oidc/revoke`                            | [Revoke](https://tools.ietf.org/html/rfc7009) access or refresh tokens. This endpoint expects HTTP basic authentication with OIDC service `client_id` and `client_secret` associated as username and password.
| `/oidc/register`                          | Register clients via the [dynamic client registration](https://tools.ietf.org/html/draft-ietf-oauth-dyn-reg-management-01) protocol.

## Register Clients

Please [see this guide](OIDC-Authentication-Claims.html).

## Configuration

{% include {{ version }}/oidc-configuration.md %}
   
### JWKS

{% include {{ version }}/jwks-oidc-configuration.md %}

### Scopes & Claims

{% include {{ version }}/claims-oidc-configuration.md %}

### Logout

{% include casproperties.html properties="cas.authn.oidc.logout" %}

## Server Configuration

Remember that OpenID Connect features of CAS require session affinity (and optionally session replication),
as the authorization responses throughout the login flow are stored via server-backed session storage mechanisms. 
You will need to configure your deployment environment and load-balancers accordingly.

## Session Replication

{% include {{ version }}/session-replication-configuration.md %}

## Sample Client Applications

- [MITREid Sample Java Webapp](https://github.com/cas-projects/oidc-sample-java-webapp)

## Claims

Please [see this guide](OIDC-Authentication-Claims.html).

## Authentication Context Class

Support for authentication context class references is implemented in form of `acr_values` as part of the original 
authorization request, which is mostly taken into account by 
the [multifactor authentication features](../mfa/Configuring-Multifactor-Authentication.html) of CAS. 
Once successful, `acr` and `amr` values are passed back to the relying party as part of the id token.

## Pairwise Identifiers

When `pairwise` subject type is used, CAS will calculate a unique `sub` value for each sector identifier. This identifier 
should not be reversible by any party other than CAS and is somewhat akin to CAS generating persistent anonymous user 
identifiers. Each value provided to every relying party is different so as not 
to enable clients to correlate the user's activities without permission.

```json
{
  "@class" : "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client",
  "clientSecret": "secret",
  "serviceId" : "^<https://the-redirect-uri>",
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.PairwiseOidcRegisteredServiceUsernameAttributeProvider",
    "persistentIdGenerator" : {
      "@class" : "org.apereo.cas.authentication.principal.OidcPairwisePersistentIdGenerator",
      "salt" : "aGVsbG93b3JsZA=="
    }
  }
}
```

## Keystores

Each registered application in CAS can contain its own keystore as a `jwks` resource. By default,
a global keystore can be expected and defined via CAS properties. The format of the keystore
file is similar to the following:

```json
{
  "keys": [
    {
      "d": "...",
      "e": "AQAB",
      "n": "...",
      "kty": "RSA",
      "kid": "cas"
    }
  ]
}
```

CAS will attempt to auto-generate a keystore if it can't find one, but if you wish to generate one manually, 
a JWKS can be generated using [this tool](https://mkjwk.org/)
or [this tool](http://connect2id.com/products/nimbus-jose-jwt/generator).

## WebFinger Issuer Discovery

Please see [this guide](OIDC-Authentication-WebFinger.html).
