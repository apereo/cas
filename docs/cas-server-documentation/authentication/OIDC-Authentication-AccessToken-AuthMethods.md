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

## mTLS Client Authentication

In order to utilize TLS for client authentication, the TLS connection between the client and CAS MUST have been established 
or re-established with mutual-TLS X.509 certificate authentication during the TLS handshake. For all requests to CAS 
utilizing mutual-TLS client authentication, the client MUST include the `client_id` parameter which enables the 
CAS server to easily identify the client independently from the content of the certificate and 
locate the client configuration using the client identifier and check the certificate presented in 
the TLS handshake against the expected credentials for that client. 
