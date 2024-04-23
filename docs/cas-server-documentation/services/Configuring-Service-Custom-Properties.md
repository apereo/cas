---
layout: default
title: CAS - Configuring Service Custom Properties
category: Services
---

{% include variables.html %}

# Configure Service Custom Properties

CAS has ability to add arbitrary attributes to a registered service.
These attributes are considered extra metadata about the service that
indicate settings such as contact phone number, email, etc or
extra attributes and fields that may be used by extensions
for custom functionality on a per-service basis.

A sample JSON file follows:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "sample service",
  "id" : 100,
  "properties" : {
    "@class" : "java.util.HashMap",
    "email" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "person@place.edu", "admin@place.edu" ] ]
    }
  }
}
```

Registered service property values can use the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax.
        
Note that you may also extend the CAS configuration schema to define your own [custom properties](../webflow/Webflow-Customization-CustomProperties.html).

## Supported Properties

{% include_cached registeredserviceproperties.html %}

