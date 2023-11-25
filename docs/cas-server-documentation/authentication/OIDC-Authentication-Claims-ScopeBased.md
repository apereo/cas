---
layout: default
title: CAS - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# Scope-based Claims - OpenID Connect Authentication

You may chain various attribute release policies that authorize claim release based on specific scopes:

```json
{
  "@class" : "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "...",
  "clientSecret": "...",
  "serviceId" : "...",
  "name": "OIDC Test",
  "id": 10,
  "scopes" : [ "java.util.HashSet", 
    [ "openid", "profile", "email", "address", "phone", "offline_access" ]
  ]
}
```

Standard scopes that internally catalog pre-defined claims all belong to 
the namespace `org.apereo.cas.oidc.claims` and are described below:

| Policy                                               | Description                                                                                    |
|------------------------------------------------------|------------------------------------------------------------------------------------------------|
| `o.a.c.o.c.OidcProfileScopeAttributeReleasePolicy`   | Release claims mapped to the spec-predefined `profile` scope.                                  |
| `o.a.c.o.c.OidcEmailScopeAttributeReleasePolicy`     | Release claims mapped to the spec-predefined `email` scope.                                    |
| `o.a.c.o.c.OidcAddressScopeAttributeReleasePolicy`   | Release claims mapped to the spec-predefined `address` scope.                                  |
| `o.a.c.o.c.OidcPhoneScopeAttributeReleasePolicy`     | Release claims mapped to the spec-predefined `phone` scope.                                    |
| `o.a.c.o.c.OidcAssuranceScopeAttributeReleasePolicy` | Release claims [mapped to the `assurance` scope](OIDC-Authentication-Identity-Assurance.html). |
| `o.a.c.o.c.OidcCustomScopeAttributeReleasePolicy`    | Release claims mapped to the CAS-defined `custom` scope.                                       |

## User-Defined Scopes

Note that in addition to standard system scopes, you may define your own custom scope with a number of attributes within:

```json
{
  "@class" : "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "...",
  "clientSecret": "...",
  "serviceId" : "...",
  "name": "OIDC Test",
  "id": 10,
  "scopes" : [ "java.util.HashSet", [ "eduPerson" ] ]
}
```

<div class="alert alert-info">:information_source: <strong>Usage</strong><p>All user-defined custom scopes as well any custom claims
that would be mapped to those scopes must always be advertised via OpenID Connect discovery document and specified
in CAS settings for scopes and claims to be recognized as valid during claim processing.</p>
</div>

If you however wish to define your custom scopes as an extension of what OpenID Connect defines
such that you may bundle attributes together, then you need to first register your `scope`,
define its attribute bundle and then use it a given service definition such as `eduPerson` above.
Such user-defined scopes are also able to override the definition of system scopes.
