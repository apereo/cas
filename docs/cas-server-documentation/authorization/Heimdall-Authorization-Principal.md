---
layout: default
title: CAS - Heimdall - Authorization Principal
description: "How Heimdall identifies who is asking for access: the tokens and credentials an authorization request may carry."
category: Authorization
---

{% include variables.html %}

# Authorization Principal - Heimdall

Every authorization request has to say who wants access. Heimdall reads the subject from the `Authorization` header of the request, which can carry one of several kinds of tokens or credentials issued by CAS.

       
The authorization request is expected to provide an `Authorization` header using the `Bearer` or `Basic` schemes (`Authorization: Bearer/Basic ...`). 
The token in the header must indicate the *who*, the subject or the authorization principal that wants to access the resource
using the details specified in the request.

The authorization header value can be *one* of the following:

- An OpenID Connect **ID token**, passed as a `Bearer` token, produced by CAS when acting as a [OpenID Connect Provider](../authentication/OIDC-Authentication.html).
- A **JWT access token**, passed as a `Bearer` token, produced by CAS when acting as an [OAuth](../authentication/OAuth-Authentication.html) or [OpenID Connect](../authentication/OIDC-Authentication.html) identity provider.
- An **opaque access token** (i.e. `AT-1-...`), passed as a `Bearer` token, produced by CAS when acting an [OAuth](../authentication/OAuth-Authentication.html) or [OpenID Connect](../authentication/OIDC-Authentication.html) identity provider.
- A **JWT bearer token** passed as a `Bearer` token and one that follows the semantics of the [JWT Authorization grant](../authentication/OIDC-Authentication-JWT-Bearer.html).
- A valid base64-encoded `username:password`, passed as a `Basic` token, that can be accepted by the CAS authentication engine.
  For AuthZEN requests, `Basic` credentials are instead the `client_id:client_secret` of an OAuth or OpenID Connect application
  registered with CAS; CAS user credentials are rejected there.

<div class="alert alert-warning">:warning: <strong>Usage Warning</strong><p>On <code>/heimdall/authorize</code>,
<code>Basic</code> credentials go through a complete CAS authentication on every request: the authentication handlers
verify the password (for example, an LDAP bind or a deliberately slow password hash), and the attempt is audited,
throttled and may count toward account lockout like any other login. Behind a gateway that asks Heimdall about every
API call, this means one full login per call, and the user's password travels with every request. Prefer access
tokens for gateways, and keep <code>Basic</code> for low-volume callers.</p></div>

Claims or attributes from all token types are extracted and attached to the final principal, which is then
passed to the authorization policy engine to make decisions. However, when using the AuthZEN protocol
CAS will attempt to resolve claims and attributes based on the `subject` ID in the authorization request, but only for
the `user` subject type. Subjects of any other type, such as services
or devices, are evaluated by their identifier and the properties supplied in the request without any lookup.

The request `context` of an AuthZEN request is exactly what the caller sends. For `/heimdall/authorize`, the HTTP request headers
are also added to the `context`, except for credential and protocol headers such as `Authorization`, `Cookie`, `Host`
and `Content-Type`; entries sent in the request body take precedence over headers with the same name.

The claims-based policies (required scopes, ACR, AMR, audience and issuer) evaluate the principal's attributes. For
AuthZEN requests, where the principal describes the subject rather than a token, use the qualified names of the
required attributes policy (for example `subject.properties.acr` or `context.acr`) instead.

Tokens are further subject to the following rules:

- The token must be issued to an OAuth or OpenID Connect application that is registered with CAS and whose access strategy allows access.
- A token that is bound to a key via [DPoP](../authentication/OIDC-Authentication-DPoP.html) must be presented using the `DPoP`
  authorization scheme along with a valid DPoP proof for the Heimdall endpoint; it is rejected when presented as a `Bearer` token.
  Once [DPoP nonces](../authentication/OIDC-Authentication-DPoP.html#server-provided-nonces) are turned on, the proof must carry
  one, or the request is answered with `401`, `WWW-Authenticate: DPoP error="use_dpop_nonce"` and a fresh nonce.
- A token that is bound to a client certificate via mutual TLS is only accepted when the same client certificate is presented on the request.
- A JWT bearer token must carry `jti` and `iat` claims, may be presented only once, and its lifetime between `iat` and `exp`
  may not exceed a configurable maximum that defaults to five minutes.

When [authentication throttling](../authentication/Configuring-Authentication-Throttling.html) is enabled, failed caller
authentication attempts (`401`) on `/heimdall/authorize` and `/heimdall/authzen` are throttled; authorization denials
and malformed requests are not counted.

Applications can be individually prevented from calling Heimdall with their tokens using a dedicated access strategy:

```json
{
  "@class": "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client",
  "serviceId": "^https://app.example.org/.+",
  "name": "Sample",
  "id": 1,
  "accessStrategy": {
    "@class": "org.apereo.cas.heimdall.services.HeimdallRegisteredServiceAccessStrategy",
    "allowed": false
  }
}
```

The Heimdall access strategy may also be used as part of a chain of access strategies. Applications without this
access strategy are allowed to call Heimdall, as long as their access strategy allows access.
        
