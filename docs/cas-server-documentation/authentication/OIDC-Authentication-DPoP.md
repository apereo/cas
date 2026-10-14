---
layout: default
title: CAS - OpenID Connect Authentication - DPoP
category: Protocols
---
{% include variables.html %}

# OpenID Connect Authentication - DPoP

DPoP is an OAuth security extension for binding tokens to a private key that belongs to the client. The 
binding makes the DPoP access token sender-constrained and its replay, if leaked or stolen token, 
can be effectively detected and prevented, as opposed to the common Bearer token. DPoP is intended for securing 
the tokens of public clients, such as single-page applications (SPA) and mobile applications. 

Single-page applications (SPA) can now request the issue of DPoP access 
tokens from CAS when it is acting as an OpenID Connect provider. This is a new kind of token, with 
stronger security properties than the default *Bearer* access tokens. The DPoP token comes 
with a protection against unauthorised use in case it suffers an accidental or malicious leak. This 
is achieved by binding the token to a private key held by the client. To prevent a leak of the 
key itself the client should store it behind an API that renders its private parameters inaccessible to application code.

The SPA authentication flow with a DPoP token can be summarized as such:

- The SPA generates a new RSA or EC key pair in such a way so the private key parameters cannot be exported from the browser.
- To request a DPoP access token the SPA generates a one-time-use JWT signed with the private key. The function of this JWT is to demonstrate possession of the key. Its header includes the public parameters of the signing key in JWK format. 
- The SPA makes the usual token request to CAS but to trigger issue of a DPoP access token the proof JWT must be included in an HTTP request header called *DPoP*.
- If the DPoP proof is valid and signed with a supported JWS algorithms the token response will appear in the usual format, but with the token type set to *DPoP*.

To access a protected resource with a DPoP token (such as the `profile` endpoint in CAS) the client needs 
to generate a new DPoP proof, with one additional string claim - `ath`, set to the BASE64URL-encoded 
SHA-256 hash of the access token value. The `htm` (HTTP method) and `htu` (HTTP URI) claims must match those of the resource.

A DPoP-bound access token must be sent in the `Authorization` header with the `DPoP` scheme, as in `Authorization: DPoP <token>`.
Sent any other way, whether as a bearer token in the `Authorization` header or as a request parameter, it is refused with `401`
and `invalid_token`, as [RFC 9449](https://www.rfc-editor.org/rfc/rfc9449#section-7.2) requires, even when a valid proof comes with it:

```json
{
  "error": "invalid_token",
  "error_description": "DPoP-bound access token must be presented in the Authorization header with the DPoP scheme"
}
```

A request carries exactly one `DPoP` header, holding a well-formed JWT, as
[RFC 9449](https://www.rfc-editor.org/rfc/rfc9449#section-4.3) requires. A token request with several, or with one that is not
a JWT, is answered with `400` and `invalid_dpop_proof`. A protected resource answers a DPoP-bound access token that comes with
no proof, several proofs or a proof that does not verify with `401` and a `DPoP` challenge, whose `algs` parameter lists the
algorithms CAS accepts for DPoP proofs, the same list the discovery document advertises as `dpop_signing_alg_values_supported`:

```bash
HTTP/1.1 401 Unauthorized
WWW-Authenticate: DPoP error="invalid_dpop_proof", error_description="...", algs="RS256 RS384 RS512 ES256 ES384 ES512"
```

Note that there is no special configuration required in CAS to enable support for DPoP tokens.

## Refresh Tokens

A refresh token issued to a public client, one with no client secret and no token endpoint authentication method other
than `none`, is bound to the key of the DPoP proof that came with the token request, as
[RFC 9449](https://www.rfc-editor.org/rfc/rfc9449#section-5) requires. Every request that uses it must carry a DPoP proof made
with that key, or it is answered with `400` and `invalid_dpop_proof`, and the refresh token is not used up. A renewed refresh
token stays bound to the same key. Refresh tokens issued to confidential clients are not bound, since client authentication
already ties them to the client, and such a client may change its DPoP key without losing them.

## DPoP-Bound Access Tokens

A client may be required to always use DPoP, as the `dpop_bound_access_tokens` client metadata of
[RFC 9449](https://www.rfc-editor.org/rfc/rfc9449#section-5.2) defines. Every token request from such a client must carry a `DPoP`
proof header, whatever the grant; one that does not is answered with `400` and `invalid_dpop_proof`, and the grant it presents,
such as an authorization code, is not redeemed. The setting is turned on in the client's service definition:

```json
{
  "@class": "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client",
  "clientSecret": "secret",
  "serviceId": "^https://app.example.org/.*",
  "name": "Sample",
  "id": 1,
  "dpopBoundAccessTokens": true
}
```

A client may also ask for it by sending `"dpop_bound_access_tokens": true` in its
[dynamic registration request](OIDC-Authentication-Dynamic-Registration.html); the registration response echoes the setting
when it is on. Token requests made by polling with a device code and with the JWT bearer grant verify their proofs as well, and
the access tokens they issue are bound to the proof's key.

## Authorization Code Binding

An authorization request may bind the authorization code to the client's DPoP key with the `dpop_jkt` parameter, the
base64url-encoded SHA-256 [JWK thumbprint](https://www.rfc-editor.org/rfc/rfc7638) of its public key, as
[RFC 9449](https://www.rfc-editor.org/rfc/rfc9449#section-10) defines. The parameter is accepted at the authorization endpoint
and in the body of a [pushed authorization request](OIDC-Authentication-PAR.html). A pushed authorization request may carry a
`DPoP` proof header instead: CAS verifies it as it would at the token endpoint, nonce included, and binds the code to its key as if
its thumbprint had been sent as `dpop_jkt`. When both are sent, they must name the same key, or the request is refused with
`invalid_dpop_proof`.

The token request that redeems a bound code must carry a DPoP proof made with that key. Otherwise it is answered with `400`:

```json
{
  "error": "invalid_dpop_proof"
}
```

The code is not redeemed by such a request, so the client that holds the key can still use it.

## Single-Use Checking

DPoP proofs are designed to be used exactly once. Each proof JWT carries a unique `jti` (JWT ID) claim 
alongside its `iat` timestamp, and any endpoint validating the proof such as the token or profile endpoints 
are expected to track previously-seen `jti` values and reject a proof whose `jti` has already been presented.
To enforce single-use DPoP proofs are tracked in the CAS ticket registry as CAS tickets and will auto-expire.

## Server-Provided Nonces

CAS can require DPoP proofs to carry a nonce it handed out, as [RFC 9449](https://www.rfc-editor.org/rfc/rfc9449#section-8)
allows, which limits how long a proof that a compromised client generated in advance stays usable. Once turned on in CAS
settings, every DPoP proof must carry, in its `nonce` claim, a nonce that CAS handed out and that has not expired. A proof
without one, or with an unknown or expired one, is refused with `use_dpop_nonce` and a fresh nonce in the `DPoP-Nonce` header,
which the client puts in a new proof to retry the request. The token endpoint, and client authentication in the
[DPoP combined mode](OIDC-Authentication-AccessToken-AuthMethods.html#attestation-based-client-authentication), answer with `400`:

```bash
HTTP/1.1 400 Bad Request
DPoP-Nonce: TST-1-mD3m...
Cache-Control: no-store
```

```json
{
  "error": "use_dpop_nonce",
  "error_description": "DPoP proof carries no valid server-provided nonce"
}
```

Protected resources, such as the `profile` endpoint, the
[verifiable credential endpoint](OIDC-Authentication-Verifiable-Credentials.html) and
[Heimdall](../authorization/Heimdall-Authorization-Principal.html), answer with `401` and a `DPoP` challenge:

```bash
HTTP/1.1 401 Unauthorized
WWW-Authenticate: DPoP error="use_dpop_nonce", error_description="Use of DPoP nonce required", algs="RS256 RS384 RS512 ES256 ES384 ES512"
DPoP-Nonce: TST-1-mD3m...
```

Nonces are also handed out ahead of time, in the `DPoP-Nonce` header of the OpenID4VCI nonce endpoint and of the client
attestation challenge endpoint. A nonce may be used for any number of proofs until it expires; each proof still carries its
own `jti`, which may be used only once. Nonces are kept in the ticket registry, so they are shared by all CAS nodes that share
it. Browser-based clients can only read the `DPoP-Nonce` header when CORS settings list it among the exposed headers.

{% include_cached casproperties.html properties="cas.authn.oidc.dpop" %}
