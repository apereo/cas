---
layout: default
title: CAS - Attribute Release Policies
category: Attributes
---

{% include variables.html %}

# Attribute Release Policy - Return Linked

This policy will release a collection of allowed principal attributes for the
service, allowing those attributes to be built off a series of resolved, available principal attributes
that are assigned and linked to the attribute definition as the source for values. If none of the defined attributes
can produce a value, then the defined attribute will not be released. 

For example, the following configuration will attempt to release an attribute `component`,
sourcing its values from what is assigned to its entry (i.e `cn`, `givenName`, etc). Each assigned attribute (i.e `cn`, `givenName`, etc)
will be tried one by one and the first attribute that can produce a value will be used as the attribute value source for `component`. If no match 
is found, then `component` will not be released.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 300,
  "description" : "sample",
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnLinkedAttributeReleasePolicy",
    "allowedAttributes" : {
      "@class" : "java.util.TreeMap",
      "component" : ["java.util.ArrayList", ["cn", "givenName", "unknown", "firstName"]]
    }
  }
}
```
     
If CAS has the attribute `firstName` in its pool of available, resolved attributes with values `bob` and `robert`,
and no other attribute is available or can produce a value, then the outcome of the above attribute policy would be to 
authorize the release of `component`  with values for `bob` and `robert`.
