---
layout: default
title: CAS - Releasing Principal Id
category: Attributes
---

{% include variables.html %}

# Static Principal Id

Returns a static, hardcoded value for the username attribute. Values can use 
the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "usernameAttributeProvider" : {
    "@class" : "org.apereo.cas.services.StaticRegisteredServiceUsernameProvider",
    "value": "always-static-username"
  }
}
```
  
You may be able to carry out the same task with [Groovy](Attribute-Release-PrincipalId-Groovy.html), though this option
here is slightly more efficient performance-wise.
