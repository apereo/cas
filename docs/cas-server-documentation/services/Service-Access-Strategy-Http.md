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
