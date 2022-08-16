---
layout: default
title: CAS - Attribute Release Policies
category: Attributes
---

{% include variables.html %}

# Attribute Release Policy - Return Allowed

Only return the principal attributes that are explicitly allowed by the service definition.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
    "allowedAttributes" : [ "java.util.ArrayList", [ "cn", "mail", "sn" ] ]
  }
}
```

Attributes authorized and allowed for release by this policy may not necessarily be available
as resolved principal attributes and can be resolved on the fly dynamically
using the [attribute definition store](Attribute-Definitions.html).


