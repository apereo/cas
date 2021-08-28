---
layout: default
title: CAS - CAS REST Protocol
category: Protocols
---

{% include variables.html %}

# Create Service - REST Protocol

Support is enabled by including the following in your overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-rest-services" %}

{% include_cached casproperties.html properties="cas.rest.services" %}

Invoke CAS to register applications into its own service registry. The REST call must 
be authenticated using basic authentication where credentials are authenticated and 
accepted by the existing CAS authentication strategy, and furthermore the authenticated 
principal must be authorized with a pre-configured role/attribute name and value that 
is designated in the CAS configuration via the CAS properties. The body of the request 
must be the service definition that shall be registered in JSON format and of 
course, CAS must be configured to accept the particular service type defined in 
the body. The accepted media type for this request is `application/json`.

```bash
POST /cas/v1/services HTTP/1.0
```

...where body of the request may be:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "...",
  "name" : "...",
  "id" : 1,
  "description": "..."
}
```

A successful response will produce a `200` status code in return.
