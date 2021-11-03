---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# JWKS - OpenID Connect Authentication

The JWKS endpoint and functionality returns a JWKS containing public keys that enable 
clients to validate a JSON Web Token (JWT) issued by CAS as an OpenID Connect Provider.

{% include_cached casproperties.html properties="cas.authn.oidc.jwks" %}

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
           
## Key Rotation

Key rotation is when a key is retired and replaced by generating a 
new cryptographic key. Rotating keys on a regular basis is an industry 
standard and follows cryptographic best practices.

You can manually rotate keys periodically to change the JSON web key (JWK) key, or you can configure the appropriate schedule
in CAS configuration so it would automatically rotate keys for you. 

CAS always signs with only one signing key at a time, typically the very first key loaded from the keystore.
The dynamic discovery endpoint will always include both the current key and 
the next key, and it may also include the previous key if the previous 
key has not yet been revoked. To provide a seamless experience in 
case of an emergency, client applications should be able to use any of 
the keys specified in the discovery document. 
