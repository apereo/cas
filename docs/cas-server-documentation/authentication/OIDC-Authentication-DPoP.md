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

Note that there is no special configuration required in CAS to enable support for DPoP tokens; however you should note that at this time,
support for DPoP only covers access tokens. Support for refresh tokens may be worked out in future versions.

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
WWW-Authenticate: DPoP error="use_dpop_nonce", error_description="Use of DPoP nonce required"
DPoP-Nonce: TST-1-mD3m...
```

Nonces are also handed out ahead of time, in the `DPoP-Nonce` header of the OpenID4VCI nonce endpoint and of the client
attestation challenge endpoint. A nonce may be used for any number of proofs until it expires; each proof still carries its
own `jti`, which may be used only once. Nonces are kept in the ticket registry, so they are shared by all CAS nodes that share
it. Browser-based clients can only read the `DPoP-Nonce` header when CORS settings list it among the exposed headers.

{% include_cached casproperties.html properties="cas.authn.oidc.dpop" %}
