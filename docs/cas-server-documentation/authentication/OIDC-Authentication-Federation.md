---
layout: default
title: CAS - OpenID Connect Federation
category: Protocols
---
{% include variables.html %}

# OpenID Connect Authentication

The CAS server supports the [OpenID Federation protocol](https://openid.net/specs/openid-federation-1_0.html).

It can act as only one of the following roles (mutually exclusive):
- Trust Anchor (TA)
- Intermediate
- OpenID Provider (OP).

Support is enabled by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-oidc-federation" %}

## Configuration

{% include_cached casproperties.html properties="cas.authn.oidc.federation" %}

## Subordinates

For a trust anchor or intermediate, the suported entites, called subordinates, must be defined as `org.apereo.cas.oidc.federation.service.OidcFederationEntityService` in the service registry:

*Example of a RP subordinate:*

```json
{
  "@class" : "org.apereo.cas.oidc.federation.service.OidcFederationEntityService",
  "id": 2,
  "name": "My RP",
  "serviceId" : "http://rp",
  "metadata": {
    "openid_relying_party": {
      "redirect_uris": [
        "http://rp/callback"
      ],
      "application_type": "web",
      "response_types": [
        "code"
      ],
      "grant_types": [
        "authorization_code"
      ],
      "scope": "openid email profile",
      "token_endpoint_auth_method": "private_key_jwt",
      "token_endpoint_auth_signing_alg": "RS256",
      "request_object_signing_alg": "RS256",
      "jwks": {
        "keys": [
          {
            "kty": "RSA",
            "e": "AQAB",
            "use": "sig",
            "kid": "keyid",
            "n": "2moVQ...2aq7Q"
          }
        ]
      },
      "client_registration_types": [
        "explicit", "automatic"
      ],
      "client_name": "RP test"
    }
  }
}
```

*Example of an OP subordinate:*

```json
{
  "@class" : "org.apereo.cas.oidc.federation.service.OidcFederationEntityService",
  "id": 1,
  "name": "My OP",
  "serviceId" : "http://op",
  "metadata": {
    "openid_provider": {
      "authorization_endpoint": "http://op/login",
      "token_endpoint": "http://op/token",
      "registration_endpoint": "http://op/clients",
      "introspection_endpoint": "http://op/token/introspect",
      "revocation_endpoint": "http://op/token/revoke",
      "pushed_authorization_request_endpoint": "http://op/par",
      "federation_registration_endpoint": "http://op/federation/clients",
      "issuer": "http://op/oidc",
      "jwks_uri": "http://op/jwks.json",
      "scopes_supported": [
        "openid"
      ],
      "response_types_supported": [
        "code", "token", "id_token", "id_token token", "code id_token", "code id_token token"
      ],
      "response_modes_supported": [
        "query", "fragment", "form_post", "query.jwt", "fragment.jwt", "form_post.jwt", "jwt"
      ],
      "grant_types_supported": [
        "implicit", "authorization_code", "refresh_token", "password", "client_credentials", "urn:ietf:params:oauth:grant-type:jwt-bearer"
      ],
      "code_challenge_methods_supported": [
        "plain", "S256"
      ],
      "token_endpoint_auth_methods_supported": [
        "client_secret_basic", "client_secret_post", "client_secret_jwt", "private_key_jwt", "none"
      ],
      "token_endpoint_auth_signing_alg_values_supported": [
        "HS256", "HS384", "HS512", "RS256", "RS384", "RS512", "PS256", "PS384", "PS512", "ES256", "ES256K", "ES384", "ES512"
      ],
      
      ...
      
      "userinfo_encryption_enc_values_supported": [
        "A128CBC-HS256", "A192CBC-HS384", "A256CBC-HS512", "A128GCM", "A192GCM", "A256GCM", "XC20P"
      ],
      "display_values_supported": [
        "page"
      ],
      "claim_types_supported": [
        "normal"
      ],
      "claims_supported": [
        "sub"
      ],
      "claims_parameter_supported": true,
      "frontchannel_logout_supported": true,
      "frontchannel_logout_session_supported": true,
      "backchannel_logout_supported": true,
      "backchannel_logout_session_supported": true
    }
  }
}
```

The `metadata` section contains the metadata of the OP (from the `.well-known/openid-configuration` endpoint) or the metadata of the RP (from the `.well-known/openid-federation` endpoint).

Signed entity statements are returned from the `/oidc/fetch` endpoint, depending on the requested subordinate (`sub` request parameter).
