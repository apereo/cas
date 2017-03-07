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

## Groovy

Returns a username attribute value as the final result of a groovy script's execution.
Groovy scripts whether inlined or external will receive and have access to the following variable bindings:

- `id`: The existing identifier for the authenticated principal.
- `attributes`: A map of attributes currently resolved for the principal.

### Inline

Embed the groovy script directly inside the service configuration.

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 600,
  "description" : "sample",
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.GroovyRegisteredServiceUsernameProvider",
    "groovyScript" : "groovy { return attributes['uid'] + '123456789' }",
    "canonicalizationMode" : "UPPER"
  }
}
```

### External

Reference the groovy script as an external resource outside the service configuration.
The script must return a single `String` value.

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 600,
  "description" : "sample",
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.GroovyRegisteredServiceUsernameProvider",
    "groovyScript" : "file:/etc/cas/sampleService.groovy",
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
