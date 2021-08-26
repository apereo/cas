---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# JWKS - OpenID Connect Authentication

The JWKS endpoint and functionality returns a JWKS containing public keys that enable 
clients to validate a JSON Web Token (JWT) issued by CAS as an OpenID Connect Provider.

{% include casproperties.html properties="cas.authn.oidc.jwks" %}

## Keystores

Each [registered application in CAS](OIDC-Authentication-Clients.html) can contain its 
own keystore as a `jwks` resource. By default,
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
