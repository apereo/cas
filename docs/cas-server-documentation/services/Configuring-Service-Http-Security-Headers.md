---
layout: default
title: CAS - Configuring Service HTTP Security Headers
category: Services
---

{% include variables.html %}

# Service HTTP Security Headers

CAS has ability to control, on a per-service basis, whether certain security-related HTTP headers should be 
injected into the response. While headers are typically enabled and defined globally as part 
of the [CAS Security Filter](../planning/Security-Guide.html#cas-security-filter), the strategy 
described here allows one to disable/enable the injection of these headers for certain 
applications and service requests and override the global defaults.
           
## Global Configuration

{% include_cached casproperties.html properties="cas.http-web-request" %}

## Service HTTP Headers

{% include_cached registeredserviceproperties.html groups="HTTP_HEADERS" %}

A sample JSON file follows:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "sample service",
  "id" : 100,
  "properties" : {
    "@class" : "java.util.HashMap",
    "httpHeaderEnableXContentOptions" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "true" ] ]
    }
  }
}
```

## CORS Policies

Cross-origin resource sharing (CORS) policies can also be defined per application in form of service properties.

{% include_cached registeredserviceproperties.html groups="CORS" %}

A sample JSON file follows:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "sample service",
  "id" : 100,
  "properties" : {
    "@class" : "java.util.HashMap",
    "corsMaxAge" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceProperty",
      "values" : [ "java.util.HashSet", [ "1000" ] ]
    }
  }
}
```

