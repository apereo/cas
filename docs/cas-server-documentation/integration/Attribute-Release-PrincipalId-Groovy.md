---
layout: default
title: CAS - Releasing Principal Id
category: Attributes
---

{% include variables.html %}

# Groovy Principal Id

Returns a username attribute value as the final result of a groovy script's execution.
Groovy scripts whether inlined or external will receive and have access to the following variable bindings:

- `id`: The existing identifier for the authenticated principal.
- `attributes`: A map of attributes currently resolved for the principal.
- `service`: The service object that is matched by the registered service definition.
- `logger`: A logger object, able to provide `logger.info(...)` operations, etc.

## Inline

Embed the groovy script directly inside the service configuration.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 600,
  "description" : "sample",
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.GroovyRegisteredServiceUsernameProvider",
    "groovyScript" : "groovy { return attributes['uid'][0] + '123456789' }",
    "canonicalizationMode" : "UPPER"
  }
}
```

Note that the `uid` attribute in the above example is resolved internally as a multivalued attribute, as should all attributes when fetched by CAS. So 
the above example uses the `[0]` syntax to fetch the first value of the attribute.

## External

Reference the groovy script as an external resource outside the service configuration.
The script must return a single `String` value.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 600,
  "description" : "sample",
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.GroovyRegisteredServiceUsernameProvider",
    "groovyScript" : "file:///etc/cas/sampleService.groovy",
    "canonicalizationMode" : "UPPER"
  }
}
```

Sample Groovy script follows:

```groovy
logger.info("Choosing username attribute out of attributes $attributes")
return "newPrincipalId"
```

The configuration of this component qualifies to use the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax.
