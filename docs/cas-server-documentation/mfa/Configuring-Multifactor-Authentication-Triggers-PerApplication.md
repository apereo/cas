---
layout: default
title: CAS - Multifactor Authentication Triggers
category: Multifactor Authentication
---

{% include variables.html %}

# Per Application - Multifactor Authentication Triggers

MFA can be triggered for a specific application registered inside the CAS service registry.

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "^(https|imaps)://.*",
  "id" : 100,
  "name": "test",
  "multifactorPolicy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
    "multifactorAuthenticationProviders" : [ "java.util.LinkedHashSet", [ "mfa-duo" ] ],
    "bypassEnabled": false,
    "forceExecution": true
  }
}
```

The following fields are accepted by the policy definition

| Field                 | Description
|-----------------------|----------------------------------------------------------------------------
| `multifactorAuthenticationProviders` | Set of multifactor provider ids that should trigger for this application.
| `script`              | Path to a script, whether external or internal, to trigger multifactor authentication dynamically.
| `bypassEnabled`       | Whether multifactor authentication should be [bypassed](Configuring-Multifactor-Authentication-Bypass.html) for this service.
| `forceExecution`      | Whether multifactor authentication should forcefully trigger, even if the existing authentication context can be satisfied without MFA.
