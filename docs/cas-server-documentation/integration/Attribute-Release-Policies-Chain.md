---
layout: default
title: CAS - Chaining Attribute Release Policies
category: Attributes
---

{% include variables.html %}

# Chaining Attribute Release Policies

Attribute release policies can be chained together to process multiple rules.
The order of policy invocation is the same as the definition order defined for the service itself.

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 300,
  "attributeReleasePolicy": {
    "@class": "org.apereo.cas.services.ChainingAttributeReleasePolicy",
    "mergingPolicy": "replace",
    "policies": [ "java.util.ArrayList",
      [
          {"@class": "..."},
          {"@class": "..."}
      ]
    ]
  }
}
```

The following merging policies are supported:

| Policy        | Description                                                                                                         |
|---------------|---------------------------------------------------------------------------------------------------------------------|
| `replace`     | Attributes are merged such that attributes from the source always replace principal attributes.                     |
| `add`         | Attributes are merged such that attributes from the source that don't already exist for the principal are produced. |
| `multivalued` | Attributes with the same name are merged into multi-valued attributes.                                              |

## Policy Execution Order

Note that each policy in the chain can be assigned a numeric `order` that would determine its position in the chain before execution. This
order may be important if you have attribute release policies that should calculate a value dynamically first before passing it onto
the next policy in the chain. 

For example, the policy chain below allows CAS to generate an attribute first using the `GeneratesFancyAttributeReleasePolicy` policy
where the attribute is next passed onto the next policy in the chain, that is `ReleaseFancyAttributeReleasePolicy`, to decide
whether or not the attribute should be released. Note the configuration of policy `order` determines the execution sequence.

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 300,
  "attributeReleasePolicy": {
    "@class": "org.apereo.cas.services.ChainingAttributeReleasePolicy",
    "policies": [ "java.util.ArrayList",
      [
          {
            "@class": "org.apereo.cas.ReleaseFancyAttributeReleasePolicy",
            "order": 1
          },
          {
            "@class": "org.apereo.cas.GeneratesFancyAttributeReleasePolicy", 
            "order": 0
          }
      ]
    ]
  }
}
```
        
## Reusing Attributes

Attribute release policies, when grouped and ordered together in a 
chain are now able to reuse and build on top of previously-selected
attributes, tagged for release by previous policies in the same 
chain. 

For example, consider the following chain where the first policy builds and authorizes the release of `uid-X`,
and the second policy *reuses* that attribute to build and authorize the release of `other-uid`.

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 1,
  "attributeReleasePolicy": {
    "@class": "org.apereo.cas.services.ChainingAttributeReleasePolicy",
    "policies": [ "java.util.ArrayList",
      [
        {
            "@class" : "org.apereo.cas.services.ReturnMappedAttributeReleasePolicy",
            "order": 0,
            "allowedAttributes" : {
              "@class" : "java.util.TreeMap",
              "uid-X" : "groovy { return attributes['uid'].get(0) + '-X' }"
            }
        },
        {
          "@class" : "org.apereo.cas.services.ReturnMappedAttributeReleasePolicy",
          "order": 1,
          "allowedAttributes" : {
            "@class" : "java.util.TreeMap",
            "other-uid" : "groovy { return attributes['uid-X'].get(0) + '-other' }"
          }
        }
      ]
    ]
  }
}
```
