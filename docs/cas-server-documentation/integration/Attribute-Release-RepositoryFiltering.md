---
layout: default
title: CAS - Attribute Release Caching
category: Attributes
---

{% include variables.html %}

# Attribute Repository Filtering

Principal attribute repositories can consult attribute sources defined and controlled by [Person Directory](Attribute-Resolution.html). Assuming a JSON attribute 
repository source is defined with the identifier `MyJsonRepository`, the following definition disregards all previously-resolved attributes and contacts `MyJsonRepository`
again to fetch attributes and cache them for `30` minutes.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^(https|imaps)://.*",
  "name" : "HTTPS and IMAPS",
  "id" : 1,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnAllAttributeReleasePolicy",
    "principalAttributesRepository" : {
        "@class" : "org.apereo.cas.authentication.principal.cache.CachingPrincipalAttributesRepository",
        "timeUnit" : "MINUTES",
        "expiration" : 30,
        "ignoreResolvedAttributes": true,
        "attributeRepositoryIds": ["java.util.HashSet", [ "MyJsonRepository" ]],
        "mergingStrategy" : "MULTIVALUED"
    }
  }
}
```

Here is a similar example with caching turned off for the service where CAS attempts to combine previously-resolved attributes with the results from the attribute
repository identified as `MyJsonRepository`. The expectation is that the attribute source `MyJsonRepository` is excluded from principal resolution during the authentication phase
and should only be contacted at release time for this service:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^(https|imaps)://.*",
  "name" : "HTTPS and IMAPS",
  "id" : 1,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnAllAttributeReleasePolicy",
    "principalAttributesRepository" : {
        "@class" : "org.apereo.cas.authentication.principal.DefaultPrincipalAttributesRepository",
        "ignoreResolvedAttributes": false,
        "attributeRepositoryIds": ["java.util.HashSet", [ "MyJsonRepository" ]],
        "mergingStrategy" : "MULTIVALUED"
    }
  }
}
```
