---
layout: default
title: CAS - Attribute Release Policies
category: Attributes
---

{% include variables.html %}

# Attribute Release Policy - Activation Criteria

Almost all attribute release policies can be modified to conditionally activate the release policy. This allows the
release policy to dynamically decide whether attributes should be released at all to the target application.

You can implement the conditions using the following strategies.
        
{% tabs attrreleaseconditions %}

{% tab attrreleaseconditions Attributes %}

The release policy can be conditionally activated based on available principal attributes.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "https://app.example.org",
  "name" : "Example",
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy",
    "allowedAttributes" : [ "java.util.ArrayList", [ "cn", "mail", "sn" ] ],
    "activationCriteria": {
        "@class": "org.apereo.cas.services.AttributeBasedRegisteredServiceAttributeReleaseActivationCriteria",
        "operator": "AND",
        "reverseMatch": false,
        "requiredAttributes": {
          "@class" : "java.util.HashMap",
          "firstName": [ "java.util.ArrayList", [ "John", "Jon" ] ],
          "lastName": [ "java.util.ArrayList", [ "Holdoor", "Hodor" ] ]
        }
    }
  }
}
```

In the above example, the attribute release policy is only activated if the current 
principal has a `lastName` attribute with values `Jon` or `John`, AND has a `firstName` attribute 
with values `Holdoor` or `Hodor`. The operator field can also be modified to use `OR`.
    
The `reverseMatch` field can be used to invert the logic of the condition. When set to `true`,   
principal must not carry any of the required attributes for the policy to activate.

{% endtab %}

{% endtabs %}


