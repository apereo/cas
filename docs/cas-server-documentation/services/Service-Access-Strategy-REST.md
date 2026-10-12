---
layout: default
title: CAS - Service Access Strategy - REST
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

The request sent to the remote endpoint carries the principal id as a `username` parameter, and its body is a JSON
document that describes the principal and the requested application:

```json
{
  "principalId": "casuser",
  "attributes": { "role": [ "staff" ] },
  "service": { "id": "https://app.example.org/login" },
  "registeredService": {
    "id": 1,
    "name": "Example",
    "serviceId": "^https://app.example.org",
    "friendlyName": "CAS Client"
  }
}
```

The application definition itself is not sent, so that client secrets, keys and other credentials that it may
carry are never disclosed to the remote endpoint.
