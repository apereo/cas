---
layout: default
title: CAS - Attribute Release Policies
category: Attributes
---

{% include variables.html %}

# Attribute Release Policy - Deny All

Never ever return principal attributes to applications. Note that this policy
also skips and refuses to release default attributes, if any.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "description" : "sample",
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.DenyAllAttributeReleasePolicy"
  }
}
```
