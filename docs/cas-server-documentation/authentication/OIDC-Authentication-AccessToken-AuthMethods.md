---
layout: default
title: CAS - OpenID Connect - Access Token Authentication Methods
category: Protocols
---
{% include variables.html %}

# OpenID Connect - Access Token Authentication Methods

Access token requests must be authenticated using any of the client authentication strategies
specified in the [OpenID Connect discovery](OIDC-Authentication-Discovery.html). The following methods are supported by CAS:

| Method                   | Description                                                                                                                                                                                                                                                                                                   |
|--------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `client_secret_basic`    | Default. The client id and client secret are used to create a HTTP Basic authentication scheme.                                                                                                                                                                                                               |
| `client_secret_post`     | NOT RECOMMENDED. The `client_id` and `client_secret` are only supplied and accepted in the request body.                                                                                                                                                                                                      |
| `client_secret_jwt`      | Clients with a client secret can create a JWT using an HMAC SHA algorithm, which is calculated using the client secret as the shared key. The JWT is passed as a `client_assertion` request parameter and `client_assertion_type` parameter MUST be `urn:ietf:params:oauth:client-assertion-type:jwt-bearer`. |
| `private_key_jwt`        | Clients with a registered public key build and sign a JWT using that key. The JWT is passed as a `client_assertion` request parameter and `client_assertion_type` parameter MUST be `urn:ietf:params:oauth:client-assertion-type:jwt-bearer`.                                                                 |
| `tls_client_auth`        | Mutual TLS utilizing the PKI method of associating a certificate to a client.                                                                                                                                                                                                                                 |
| `attest_jwt_client_auth` | A client attestation, such as a wallet attestation, in the `OAuth-Client-Attestation` header with its proof of possession in the `OAuth-Client-Attestation-PoP` header. See [below](#attestation-based-client-authentication).                                                                                |

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

## Attestation-Based Client Authentication

Clients such as native wallet apps may authenticate with a client attestation, following
[OAuth 2.0 Attestation-Based Client Authentication](https://datatracker.ietf.org/doc/draft-ietf-oauth-attestation-based-client-auth/).
This is how wallets authenticate with a wallet attestation, as described by
[OpenID4VCI 1.0 Appendix E](https://openid.net/specs/openid-4-verifiable-credential-issuance-1_0.html#appendix-E)
and required by [HAIP 1.0](https://openid.net/specs/openid4vc-high-assurance-interoperability-profile-1_0.html),
at the token and the [pushed authorization request](OIDC-Authentication-PAR.html) endpoints.

{% include_cached casproperties.html properties="cas.authn.oidc.client-attestation" %}

The client attestation is signed by the client attester, such as the wallet provider, and is sent in the
`OAuth-Client-Attestation` header. It carries its signing certificate, and any intermediate certificates, in its `x5c`
header; the chain must lead to one of the trust anchors defined in CAS settings, without including it, and the signer must
not be self-signed. Its `sub` is the client identifier and must match the `client_id` request parameter when one is sent,
it must carry an `exp` that has not passed, and its `cnf` claim names the key of the client instance. A `status` claim is
accepted and logged as a warning, since its status is not checked.

The proof of possession is sent in the `OAuth-Client-Attestation-PoP` header, signed by the key in `cnf`. Its `aud` must be
the issuer, it must carry an `iat` within the last few minutes and a `jti` that may be used only once. Each header must be sent
exactly once. CAS does not issue challenges, and the DPoP combined mode (`attest_jwt_client_auth_dpop`) is not supported.

Once trust anchors are configured, the [discovery document](OIDC-Authentication-Discovery.html) lists `attest_jwt_client_auth`
among `token_endpoint_auth_methods_supported`, along with the accepted signing algorithms:

```json
{
  "token_endpoint_auth_methods_supported": [
    "client_secret_basic",
    "private_key_jwt",
    "attest_jwt_client_auth"
  ],
  "client_attestation_signing_alg_values_supported": [
    "ES256",
    "ES384",
    "ES512"
  ],
  "client_attestation_pop_signing_alg_values_supported": [
    "ES256",
    "ES384",
    "ES512"
  ]
}
```

A relying party that must authenticate this way at the token endpoint names the method:

```json
{
  "@class": "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "https://wallet.example.org",
  "serviceId": "^https://wallet.example.org/.*",
  "name": "Wallet",
  "id": 1,
  "tokenEndpointAuthenticationMethod": "attest_jwt_client_auth"
}
```

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

Access tokens issued to a client that authenticates with mutual TLS are bound to its certificate, as described
in [RFC 8705](https://www.rfc-editor.org/rfc/rfc8705#section-3). JWT access tokens and introspection responses carry
the certificate thumbprint in the `cnf` claim as `x5t#S256`, the base64url-encoded SHA-256 hash of the DER-encoded certificate.
               
### SPIFFE

CAS supports the SPIFFE standard for mutual TLS client authentication. To use SPIFFE, the client must present a certificate 
with a URI SAN entry that follows the SPIFFE format, i.e. `spiffe://...`. CAS will validate the certificate and 
extract the client identifier from the SPIFFE URI. The client identifier is then used to locate the service definition.

```json
{
  "@class": "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "spiffe://example.org/ns/payments/sa/service-sample",
  "serviceId" : "^https://localhost:9859/anything/cas.*",
  "name": "MyApplication",
  "id": 1,
  "tokenEndpointAuthenticationMethod": "tls_client_auth",
  "tlsClientAuthSubjectDn": "(.+)",
  "supportedGrantTypes": [ "java.util.HashSet", [ "client_credentials" ] ]
}
```
      
An example request with `curl` would look like the following:

```bash
curl --cert /var/run/secrets/svid.pem \
     --key /var/run/secrets/key.pem \
     --cacert /var/run/secrets/bundle.pem \
     -X POST https://sso.example.org/cas/oidc/token \
     -d "grant_type=client_credentials&scope=xyz"
```

You can verify SPIFFE ID is present in your certificate:

```bash
openssl x509 -in svid.pem -text -noout
```

Look for:
    
```bash
X509v3 Subject Alternative Name:
    URI:spiffe://example.org/ns/payments/sa/service-sample
```
