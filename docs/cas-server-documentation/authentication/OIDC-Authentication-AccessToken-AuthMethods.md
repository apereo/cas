---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# OpenID Connect - Access Token Authentication Methods

Access token requests must be authenticated using any of the client authentication strategies
specified in the [OpenID Connect discovery](OIDC-Authentication-Discovery.html). The following methods are supported by CAS:

| Method                | Description                                                                                                                                                                                                                                                                                                   |
|-----------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `client_secret_basic` | Default. The client id and client secret are used to create a HTTP Basic authentication scheme.                                                                                                                                                                                                               |
| `client_secret_post`  | NOT RECOMMENDED. The `client-id` and `client_secret` are only supplied and accepted in the request body.                                                                                                                                                                                                      |
| `client_secret_jwt`   | Clients with a client secret can create a JWT using an HMAC SHA algorithm, which is calculated using the client secret as the shared key. The JWT is passed as a `client_assertion` request parameter and `client_assertion_type` parameter MUST be `urn:ietf:params:oauth:client-assertion-type:jwt-bearer`. |
| `private_key_jwt`     | Clients with a registered public key build and sign a JWT using that key. The JWT is passed as a `client_assertion` request parameter and `client_assertion_type` parameter MUST be `urn:ietf:params:oauth:client-assertion-type:jwt-bearer`.                                                                 |

Please study [the specification](https://openid.net/specs/openid-connect-core-1_0.html) to learn more.
                         
Enforced and required client authentication methods may be tuned and controlled for each relying party:

```json
{
  "@class": "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client-id",
  "clientSecret": "secret",
  "serviceId": "^https://app.example.org/oidc",
  "name": "MyApplication",
  "id": 1,
  "tokenEndpointAuthenticationMethod": "client_secret_basic"
}
```

If the `tokenEndpointAuthenticationMethod` field is left blank, all available authentication methods are evaluated for access token requests
and authentication method enforcement should effectively be disabled.
