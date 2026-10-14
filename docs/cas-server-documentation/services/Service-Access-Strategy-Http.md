---
layout: default
title: CAS - Service Access Strategy - HTTP Request
category: Services
---

{% include variables.html %}

# Service Access Strategy - HTTP Request

This strategy allows one to configure a service with the following properties:

| Field       | Description                                                                                                    |
|-------------|----------------------------------------------------------------------------------------------------------------|
| `ipAddress` | (Optional) Regular expression pattern that must match the entire client IP address.                            |
| `userAgent` | (Optional) Regular expression pattern that must match the entire browser user agent.                           |
| `headers`   | (Optional) Map of request header names to regular expression patterns that must match the entire header value. |

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
    "userAgent": ".*Chrome.+",
    "headers":{
      "@class": "java.util.TreeMap",
      "Header1": "Value.+Pattern"
    }
  }
}
```

Patterns are compared case-insensitively and must match the whole value, so `10\.0\.0\.1` matches only that address and not `110.0.0.15`;
use `.*` to match part of a value, as the `userAgent` pattern above does. Header names are matched case-insensitively, and
access is denied when a configured header is missing from the request. CAS can only evaluate headers it records for the
request.
