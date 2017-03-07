---
layout: default
title: CAS - Releasing Principal Id
---

# Principal-Id Attribute

The service registry component of CAS has the ability to allow for configuration of a
`usernameAttributeProvider` to be returned for the given registered service. This component controls
what should be the designated user identifier that is returned to the application.

* Ensure the attribute is available and resolved for the principal
* Set the `usernameAttributeProvider` property of the given service to once of the attribute providers.
* A number of providers are able to perform canonicalization on the final user id returned to transform it
into uppercase/lowercase. This is noted by the `canonicalizationMode` whose allowed values are `UPPER`, `LOWER` or `NONE`.

## Default

The default configuration which need not explicitly be defined, simply returns the resolved
principal id as the username for this service.

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "description" : "sample",
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceUsernameProvider",
    "canonicalizationMode" : "NONE"
  }
}
```

If you do not need to adjust the behavior of this provider (i.e. to modify the canonicalization mode),
then you can leave out this block entirely.

## Attribute

Returns an attribute that is already resolved for the principal as the username for this service. If the attribute
is not available, the default principal id will be used.

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 600,
  "description" : "sample",
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.PrincipalAttributeRegisteredServiceUsernameProvider",
    "usernameAttribute" : "cn",
    "canonicalizationMode" : "UPPER"
  }
}
```

## Anonymous

Provides an opaque identifier for the username. The opaque identifier by default conforms to the requirements
of the [eduPersonTargetedID](http://www.incommon.org/federation/attributesummary.html#eduPersonTargetedID) attribute.

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 500,
  "description" : "sample",
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.AnonymousRegisteredServiceUsernameAttributeProvider",
    "persistentIdGenerator" : {
      "@class" : "org.apereo.cas.authentication.principal.ShibbolethCompatiblePersistentIdGenerator",
      "salt" : "aGVsbG93b3JsZA=="
    }
  }
}
```
