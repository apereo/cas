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
its redirect URI; a sector identifier URI that names no host makes the application its own sector. For an application
that CAS registered, dynamically or through a federation, that is the host of its first `http` or `https` redirect URI
on a host other than a loopback one, such as `localhost` or `127.0.0.1`; an application without one, such as a native
application that uses a private-use URI scheme or a loopback redirect URI, is its own sector, whatever its redirect URIs
name.

An application registered dynamically by an earlier CAS version is read as an application defined by hand, below,
until its registration is next updated. The update then gives it the sector described above, and so new identifiers for
all of its users, even when its redirect URIs stay the same. Until then, an application whose first redirect URI is a
native one, such as `com.example.app://callback`, is in the sector of what follows the scheme, `callback` here, and one
whose first redirect URI is a loopback one, such as `http://127.0.0.1:3000/cb`, in the sector of that host; other
applications may share either. One whose first redirect URI names no host at all is its own sector already, which
changes its identifiers, since such applications used to share one sector.

For an application defined by hand, the host is read from its service id, a regular expression such as
`^https://app.example.org/.*` included. A service id that names no host makes the application its own sector; that
includes `^https?://app.example.org/.*`, in which the `?` ends what is read as the host, so such an application should
name its sector with a sector identifier URI. A service id whose host is itself a pattern, such as `^https://.*`, gives
that pattern as its sector, shared by every application whose service id does the same. A service id written in the
quoted form CAS builds for the applications it registers is read as theirs are.

Applications of the same sector receive the same identifiers, so an application that CAS registers must show that its
sector is its own: one whose redirect URIs name several hosts must register a sector identifier URI, and a sector
identifier URI must be an `https` URL that CAS can fetch and that lists every redirect URI of the application, as
[OpenID Connect](https://openid.net/specs/openid-connect-core-1_0.html#PairwiseAlg) requires. Sectors are read the same
way when the application is registered and when its identifiers are calculated: an application whose redirect URIs mix
web and native ones spans several sectors too, and an `http` or `https` redirect URI whose host cannot be read is
refused. Otherwise the application is not registered, and an update of its registration that does not meet these
rules is refused and leaves the registration as it was. CAS fetches the sector identifier document of an application
registered dynamically only from public addresses, without following redirects, in a single attempt, within a limited
time and up to a limited size; the document must be a JSON array, whatever type it is served as. For a relying party of
an OpenID Federation, the limits of federation requests apply.

Applications whose definition carries no salt of its own, which includes applications that CAS registers itself,
calculate their identifiers with a salt that can be defined using CAS properties. The salt is read when the identifiers
are calculated and is never written into application definitions. Without that salt, an application registered
dynamically is given a random salt of its own, kept with its registration, which it keeps using afterwards: its
identifiers are then not shared with the other applications of its sector. An update of a dynamic registration keeps the
generator of identifiers the application has, so that its identifiers do not change. A custom generator without a salt
of its own that cannot be copied, such as one that holds state of its own, calculates identifiers with that salt as the
default generator does, with the same attribute. A relying party of an OpenID
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

