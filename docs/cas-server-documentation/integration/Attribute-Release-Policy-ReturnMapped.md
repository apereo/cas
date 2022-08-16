---
layout: default
title: CAS - Attribute Release Policies
category: Attributes
---

{% include variables.html %}

# Attribute Release Policy - Return Mapped

Similar to above, this policy will return a collection of allowed principal attributes for the
service, but also allows those principal attributes to be mapped and "renamed" at the more granular service level.

For example, the following configuration will recognize the resolved
attributes `eduPersonAffiliation` and `groupMembership` and will then
release `affiliation` and `group` to the web application configured.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 300,
  "description" : "sample",
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnMappedAttributeReleasePolicy",
    "allowedAttributes" : {
      "@class" : "java.util.TreeMap",
      "eduPersonAffiliation" : "affiliation",
      "groupMembership" : "group"
    }
  }
}
```

Attributes authorized and allowed for release by this policy may not necessarily be available
as resolved principal attributes and can be resolved on the fly dynamically
using the [attribute definition store](Attribute-Definitions.html).
