---
layout: default
title: CAS - Configuring Service Access Strategy
category: Services
---

{% include variables.html %}

# Service Access Strategy - HTTP Request

This strategy allows one to configure a service with the following properties:

| Field       | Description                                                                    |
|-------------|--------------------------------------------------------------------------------|
| `ipAddress` | (Optional) Regular expression pattern compared against the client IP address.  |
| `userAgent` | (Optional) Regular expression pattern compared against the browser user agent. |

The objective of this policy is examine specific properties of the HTTP request and make service access decisions by comparing those properties
with pre-defined rules and patterns, such as those that might be based on an IP address, user-agent, etc.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.HttpRequestRegisteredServiceAccessStrategy",
    "ipAddress" : "192.\\d\\d\\d.\\d\\d\\d.101",
    "userAgent": "Chrome.+"
  }
}
```

## Remote Endpoint

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
  "serviceId" : "^https://.+",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.RemoteEndpointServiceAccessStrategy",
    "endpointUrl" : "https://somewhere.example.org",
    "acceptableResponseCodes" : "200,202"
  }
}
```
