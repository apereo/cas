---
layout: default
title: CAS - Configuring Service Access Strategy
category: Services
---

{% include variables.html %}

# Service Access Strategy - Basic


The default strategy allows one to configure a service with the following properties:

| Field                     | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
|---------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `enabled`                 | Flag to toggle whether the entry is active; a disabled entry produces behavior equivalent to a non-existent entry.                                                                                                                                                                                                                                                                                                                                                              |
| `ssoEnabled`              | Set to `false` to force users to authenticate to the service regardless of protocol flags (e.g. `renew=true`).                                                                                                                                                                                                                                                                                                                                                                  |

Service is not allowed to use CAS:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "enabled" : false
  }
}
```

Service is not allowed to use CAS for single sign-on and users will be prompted for credentials every time:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "ssoEnabled" : false
  }
}
```
