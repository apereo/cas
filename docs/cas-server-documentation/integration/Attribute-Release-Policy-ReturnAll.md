---
layout: default
title: CAS - Attribute Release Policies
category: Attributes
---

{% include variables.html %}

# Attribute Release Policy - Return All

Return all resolved principal attributes to the service,
and optionally exclude attributes from the final collection.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnAllAttributeReleasePolicy",
    "excludedAttributes": ["java.util.LinkedHashSet", ["cn"]]
  }
}
```
