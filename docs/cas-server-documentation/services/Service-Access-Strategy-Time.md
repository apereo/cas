---
layout: default
title: CAS - Configuring Service Access Strategy
category: Services
---

{% include variables.html %}

# Service Access Strategy - Time

The time-based access strategy allows one to configure a service with the following properties:

| Field              | Description                                                                                                    |
|--------------------|----------------------------------------------------------------------------------------------------------------|
| `startingDateTime` | Indicates the starting date/time whence service access may be granted.  (i.e. `2015-10-11T09:55:16.552-07:00`) |
| `endingDateTime`   | Indicates the ending date/time whence service access may be granted.  (i.e. `2015-10-20T09:55:16.552-07:00`)   |

Service access is only allowed within `startingDateTime` and `endingDateTime`:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "test",
  "id" : 62,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.TimeBasedRegisteredServiceAccessStrategy",
    "startingDateTime" : "2015-11-01T13:19:54.132-07:00",
    "endingDateTime" : "2015-11-10T13:19:54.248-07:00",
    "zoneId" : "UTC"
  }
}
```

The configuration of the public key component qualifies to use 
the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax.
