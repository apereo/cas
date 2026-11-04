---
layout: default
title: CAS - Pairwise Identifiers - OpenID Connect Authentication
category: Protocols
---
{% include variables.html %}

# Pairwise Identifiers - OpenID Connect Authentication

When `pairwise` subject type is used, CAS will calculate a unique `sub` value for each sector identifier. This identifier
should not be reversible by any party other than CAS and is somewhat akin to CAS generating persistent anonymous user
identifiers. Each value provided to every relying party is different so as not
to enable clients to correlate the user's activities without permission.

The sector identifier is the host of the sector identifier URI of the application, when it has one, or else the host of
its redirect URI. For an application that CAS registered, dynamically or through a federation, that is the host of its
first redirect URI; when its redirect URIs name several hosts, it should register a sector identifier URI, and when they
name none, as with a native application that uses a private-use URI scheme, the application is its own sector. For an
application defined by hand, the host is read from its service id.

```json
{
  "@class" : "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client",
  "clientSecret": "secret",
  "serviceId" : "^<https://the-redirect-uri>",
  "subjectType": "pairwise",
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.PairwiseOidcRegisteredServiceUsernameAttributeProvider",
    "persistentIdGenerator" : {
      "@class" : "org.apereo.cas.authentication.principal.OidcPairwisePersistentIdGenerator",
      "salt" : "aGVsbG93b3JsZA=="
    }
  }
}
```

