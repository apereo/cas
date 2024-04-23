---
layout: default
title: CAS - Attribute Release Policies
category: Attributes
---

{% include variables.html %}

# Attribute Release Policy - Return Allowed

Only return the principal attributes that are explicitly allowed by the service definition.

{% tabs attrreleaseallowed %}

{% tab attrreleaseallowed Default %}

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://app.example.org",
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

{% endtab %}

{% tab attrreleaseallowed Groovy %}

Allowed attributes may also contain inline Groovy script that would be tasked to build attributes
dynamically on the fly and return back a `Map<String, List<Object>>` of results:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://app.example.org",
  "name" : "Sample",
  "id" : 1,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
    "allowedAttributes" : [ "java.util.ArrayList", 
      [ 
        "cn", 
        "groovy { [ fullName: [ 'FN/' + attributes['fullName'][0] ] ] }", 
        "sn" 
      ] 
    ]
  }
}
```

{% endtab %}

{% endtabs %}

