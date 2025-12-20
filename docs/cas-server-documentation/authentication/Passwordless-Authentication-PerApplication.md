---
layout: default
title: CAS - Passwordless Authentication
category: Authentication
---
{% include variables.html %}

# Passwordless Authentication - Per Application

Passwordless authentication can be selectively controlled for specific applications. By default,
all services and applications are eligible for passwordless authentication.

```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "serviceId": "^https://app.example.org",
  "name": "App",
  "id": 1,
  "passwordlessPolicy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServicePasswordlessPolicy",
    "enabled": false
  }
}
```

The following passwordless policy settings are supported:

| Name      | Description                                                                        |
|-----------|------------------------------------------------------------------------------------|
| `enabled` | Boolean to define whether passwordless authentication is allowed for this service. |

