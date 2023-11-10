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
| `client_secret_post`  | NOT RECOMMENDED. The `client_id` and `client_secret` are only supplied and accepted in the request body.                                                                                                                                                                                                      |
| `client_secret_jwt`   | Clients with a client secret can create a JWT using an HMAC SHA algorithm, which is calculated using the client secret as the shared key. The JWT is passed as a `client_assertion` request parameter and `client_assertion_type` parameter MUST be `urn:ietf:params:oauth:client-assertion-type:jwt-bearer`. |
| `private_key_jwt`     | Clients with a registered public key build and sign a JWT using that key. The JWT is passed as a `client_assertion` request parameter and `client_assertion_type` parameter MUST be `urn:ietf:params:oauth:client-assertion-type:jwt-bearer`.                                                                 |
| `tls_client_auth`     | Mutual TLS utilizing the PKI method of associating a certificate to a client.                                                                                                                                                                                                                                 |

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

## Mutual TLS Client Authentication

In order to utilize TLS for client authentication, the TLS connection between the client and CAS MUST have been established 
or re-established with mutual-TLS X.509 certificate authentication during the TLS handshake. For all requests to CAS 
utilizing mutual-TLS client authentication, the client MUST include the `client_id` parameter which enables the 
CAS server to easily identify the client independently from the content of the certificate and 
locate the client configuration using the client identifier and check the certificate presented in 
the TLS handshake against the expected credentials for that client.

In order to convey the expected subject of the certificate and other validation requirements, 
the following parameters can be assigned to a service definition in support of the PKI method of 
mutual-TLS client authentication. Such parameters may also be passed at the time of registering the client
dynamically via [dynamic client registration](OIDC-Authentication-Dynamic-Registration.html). A relying party 
using the `tls_client_auth` authentication method MUST use exactly one of the below metadata parameters to 
indicate the certificate subject value that the authorization server is to expect when authenticating the respective client.

```json
{
  "@class": "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client-id",
  "clientSecret": "secret",
  "serviceId": "^https://app.example.org/oidc",
  "name": "MyApplication",
  "id": 1,
  "tokenEndpointAuthenticationMethod": "tls_client_auth",
  "tlsClientAuthSubjectDn": "...",
  "tlsClientAuthSanDns": "...",
  "tlsClientAuthSanUri": "...",
  "tlsClientAuthSanIp": "...",
  "tlsClientAuthSanEmail": "..."
}
```

The following parameters are supported:

| Field                    | Description                                                                                                       |
|--------------------------|-------------------------------------------------------------------------------------------------------------------|
| `tlsClientAuthSubjectDn` | The expected subject distinguished name of the certificate that the client will use in mutual-TLS authentication. |
| `tlsClientAuthSanDns`    | The expected dNSName SAN entry in the certificate that the client will use in mutual-TLS authentication.          |
| `tlsClientAuthSanUri`    | The expected uniformResourceIdentifier SAN entry in the certificate.                                              |
| `tlsClientAuthSanIp`     | The expected iPAddress SAN entry in the certificate in either for IPv4 or IPv6.                                   |
| `tlsClientAuthSanEmail`  | The expected rfc822Name SAN entry in the certificate.                                                             |
