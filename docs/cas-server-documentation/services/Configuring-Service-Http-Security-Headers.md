---
layout: default
title: CAS - Configuring Service HTTP Security Headers
category: Services
---

{% include variables.html %}

# Service HTTP Security Headers

CAS has ability to control, on a per-service basis, whether certain security-related HTTP headers should be injected into the response. While headers are typically enabled and defined globally as part of the [CAS Security Filter](../planning/Security-Guide.html#cas-security-filter), the strategy described here allows one to disable/enable the injection of these headers for certain applications and service requests and override the global defaults.
 
## HTTP Headers

Supported HTTP headers in form of service properties are:

| Header                                      | Description
|-----------------------|-----------------------------------------------------------------------
| `httpHeaderEnableCacheControl`      | Insert `Cache-Control` headers into the response for this service.
| `httpHeaderEnableXContentOptions`      | Insert `X-Content-Type-Options` headers into the response for this service.
| `httpHeaderEnableStrictTransportSecurity`   | Insert `Strict-Transport-Security` headers into the response for this service.
| `httpHeaderEnableXFrameOptions`      | Insert `X-Frame-Options` headers into the response for this service.
| `httpHeaderEnableContentSecurityPolicy`      | Insert `Content-Security-Policy` headers into the response for this service.
| `httpHeaderEnableXSSProtection`      | Insert `X-XSS-Protection` headers into the response for this service.
| `httpHeaderXFrameOptions`      | Override the `X-Frame-Options` header of the response for this service.

{% include {{ version }}/http-webrequests-configuration.md %}

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

Cross-origin resource sharing (CORS) policies can also be defined per application in form of service properties. The
following properties are supported:

| Name                      | Description
|---------------------------|-----------------------------------------------------------------------
| `corsAllowCredentials`    | Whether user credentials are supported. 
| `corsMaxAge`              | Configure how long, as a duration, the response from a pre-flight request can be cached by clients. 
| `corsAllowedOrigins`      | Set the origins to allow. The special value `*` allows all domains.
| `corsAllowedMethods`      | Set the HTTP methods to allow, e.g. `GET`, etc. The special value `*` allows all methods.
| `corsAllowedHeaders`      | Set the list of headers that a pre-flight request can list as allowed for use during an actual request.
The special value `*` allows actual requests to send any header. 
| `corsExposedHeaders`      | List of response headers that a response might have and can be exposed. The special value `*` allows all headers to be exposed for non-credentialed requests.

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

