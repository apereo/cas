---
layout: default
title: CAS - Configuring Service Access Strategy
category: Services
---

{% include variables.html %}

# Service Access Strategy - REST

This strategy allows one to configure a service access strategy with the following properties:

| Field                     | Description                                                                                |
|---------------------------|--------------------------------------------------------------------------------------------|
| `endpointUrl`             | Endpoint that receives the authorization request from CAS for the authenticated principal. |
| `acceptableResponseCodes` | Comma-separated response codes that are considered accepted for service access.            |

The objective of this policy is to ensure a remote endpoint can make service access decisions by
receiving the CAS authenticated principal as url parameter of a `GET` request. The response code that
the endpoint returns is then compared against the policy setting and if a match is found, access is granted.

Here is an example of the remote endpoint access strategy authorizing service access based on response code:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://app.example.org",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.RemoteEndpointServiceAccessStrategy",
    "endpointUrl" : "https://somewhere.example.org",
    "acceptableResponseCodes" : "200,202",
    "method": "GET",
    "headers": {
      "@class":"java.util.LinkedHashMap",
      "Content-Type": "application/json"
    }
  }
}
```
