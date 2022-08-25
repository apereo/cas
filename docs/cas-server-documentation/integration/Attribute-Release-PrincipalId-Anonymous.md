---
layout: default
title: CAS - Releasing Principal Id
category: Attributes
---

{% include variables.html %}

# Anonymous Principal Id
   
The following options are available to produce anonymous usernames. 

## Transient

Provides an opaque identifier for the username. 

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 500,
  "description" : "sample",
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.AnonymousRegisteredServiceUsernameAttributeProvider"
  }
}
```

## Persistent

Provides an opaque identifier for the username. The opaque identifier by default conforms to the requirements
of the `eduPersonTargetedID` attribute. The generated id may be based off of an existing principal
attribute. If left unspecified or attribute not found, the authenticated principal id is used.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 500,
  "description" : "sample",
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.AnonymousRegisteredServiceUsernameAttributeProvider",
    "persistentIdGenerator" : {
      "@class" : "org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator",
      "salt" : "aGVsbG93b3JsZA==",
      "attribute": ""
    }
  }
}
```

To simulate the behavior, you may also try the following command:

```bash
perl -e 'use Digest::SHA qw(sha1_base64); \
    $digest = sha1_base64("$SERVICE!$USER!$SALT"); \
    $eqn = length($digest) % 4; print $digest; print "=" x (4-$eqn) . "\n"' 
```

Replace `$SERVICE` (the url of the application under test), `$USER` and `$SALT` with the appropriate values for the test.
