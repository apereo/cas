---
layout: default
title: CAS - Attribute Release Policies
category: Attributes
---

{% include variables.html %}

# Attribute Release Policy - Return Static

Return static attributes to the service with values hard-coded in the service definition.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "attributeReleasePolicy" : {
    "@class": "org.apereo.cas.services.ReturnStaticAttributeReleasePolicy",
    "allowedAttributes": {
      "@class": "java.util.LinkedHashMap",
      "permissions": [ "java.util.ArrayList", [ "read", "write", "admin" ] ]
    }
  }
}
```

Attribute values can use the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax.
