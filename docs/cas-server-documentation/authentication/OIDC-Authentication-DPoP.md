---
layout: default
title: CAS - OpenID Connect Authentication
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
- The SPA makes the usual token request to CAS but to trigger issue of a DPoP access token the proof JWT must be included in a HTTP request header called *DPoP*.
- If the DPoP proof is valid and signed with a supported JWS algorithms the token response will appear in the usual format, but with the token type set to *DPoP*.

To access a protected resource with a DPoP token (such as the `profile` endpoint in CAS) the client needs 
to generate a new DPoP proof, with one additional string claim - `ath`, set to the BASE64URL-encoded 
SHA-256 hash of the access token value. The `htm` (HTTP method) and `htu` (HTTP URI) claims must match those of the resource.

Note that there is no special configuration required in CAS to enable support for DPoP tokens; however you should note that at this time,
support for DPoP only covers access tokens. Support for refresh tokens may be worked out in future versions.


