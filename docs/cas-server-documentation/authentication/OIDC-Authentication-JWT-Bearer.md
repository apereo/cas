---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# OpenID Connect - JWT Authorization Grant

The JWT Authorization grant (also known as JWT Bearer Token grant identified as `urn:ietf:params:oauth:client-assertion-type:jwt-bearer`) 
is a grant type that allows a client application to obtain an access token by presenting a JWT assertion to the CAS server, 
instead of using a username/password or client credentials. In the JWT Bearer flow, the client application creates 
and signs a JWT that asserts its identity and then exchanges that token for an access token by sending a `POST`
request to the CAS token endpoint URL passing the JWT under the `assertion` parameter.

A few considerations:

1. The JWT **MUST** be signed. CAS must be able to verify the signature using the public key associated with the client.
2. The JWT **MUST** include claims such as `client_id`, `iss`, `sub`, `aud`, and `exp`.
3. The `aud` claim **MUST** match the token endpoint URL of the CAS server.
4. The client application **MUST** ensure the JWT is valid and has not expired.
5. CAS may optionally be configured to also create and share ID tokens and refresh tokens as part of the response.

## Configuration

{% include_cached casproperties.html properties="cas.authn.oidc.id-token" %}

## Applications

Applications that wish to take advantage of this capability can be registered with CAS as such:

```json
{
  "@class": "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client-id",
  "clientSecret": "secret",
  "serviceId": "^https://app.example.org/oidc",
  "name": "MyApplication",
  "id": 1,
  "generateRefreshToken": true,
  "jwks": "file:/path/to/application-keystore.jwks",
  "supportedGrantTypes": [ "java.util.HashSet", [ "urn:ietf:params:oauth:client-assertion-type:jwt-bearer" ] ]
}
```
