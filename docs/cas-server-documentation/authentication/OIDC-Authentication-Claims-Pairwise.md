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
first redirect URI, and when they name none, as with a native application that uses a private-use URI scheme, the
application is its own sector. For an application defined by hand, the host is read from its service id.

Applications of the same sector receive the same identifiers, so an application that CAS registers must show that its
sector is its own: one whose redirect URIs name several hosts must register a sector identifier URI, and a sector
identifier URI must be an `https` URL that CAS can fetch and that lists every redirect URI of the application, as
[OpenID Connect](https://openid.net/specs/openid-connect-core-1_0.html#PairwiseAlg) requires. Hosts are read the same way
when the application is registered and when its identifiers are calculated, and a redirect URI whose host cannot be read
is refused. Otherwise the application is not registered, and an update of its registration that does not meet these
rules is refused and leaves the registration as it was. CAS fetches the sector identifier document of an application
registered dynamically only from public addresses, without following redirects, in a single attempt, within a limited
time and up to a limited size; for a relying party of an OpenID Federation, the limits of federation requests apply.

Applications whose definition carries no salt of its own, which includes applications that CAS registers itself,
calculate their identifiers with a salt that can be defined using CAS properties. The salt is read when the identifiers
are calculated and is never written into application definitions. Without that salt, an application registered
dynamically is given a random salt of its own, kept with its registration, which it keeps using afterwards: its
identifiers are then not shared with the other applications of its sector. An update of a dynamic registration keeps the
generator of identifiers the application has, so that its identifiers do not change. A relying party of an OpenID
Federation that asks for pairwise identifiers is not registered without that salt; with it, its identifiers stay the same
whenever it is registered again.

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

