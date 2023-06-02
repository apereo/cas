---
layout: default
title: CAS - Attribute Release Policies
category: Attributes
---

{% include variables.html %}

# Attribute Release Policy - Return MultiMapped

The same policy may allow attribute definitions to be renamed and remapped to multiple attribute names,
with duplicate attributes values mapped to different names.

For example, the following configuration will recognize the resolved attribute `eduPersonAffiliation` and will then
release `affiliation` and `personAffiliation` whose values stem from the original `eduPersonAffiliation` attribute
while `groupMembership` is released as `group`. In other words, the `eduPersonAffiliation` attribute is
released twice under two different names each sharing the same value.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 300,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnMappedAttributeReleasePolicy",
    "allowedAttributes" : {
      "@class" : "java.util.TreeMap",
      "eduPersonAffiliation" : [ "java.util.ArrayList", [ "affiliation", "personAffiliation" ] ],
      "groupMembership" : "group"
    }
  }
}
```

Attributes authorized and allowed for release by this policy may not necessarily be available
as resolved principal attributes and can be resolved on the fly dynamically
using the [attribute definition store](Attribute-Definitions.html).
