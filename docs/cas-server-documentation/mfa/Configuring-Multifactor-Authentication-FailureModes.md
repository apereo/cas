---
layout: default
title: CAS - Multifactor Authentication - Failure Modes
category: Multifactor Authentication
---

{% include variables.html %}

# Multifactor Authentication - Failure Modes

CAS will consult the current configuration in the event that the provider being requested is unreachable to determine how to proceed.  
The failure mode can be configured at these locations and CAS will use the first defined failure mode in this order:

- Registered Service Multifactor Authentication Policy
- [Multifactor Authentication Provider Configuration](Configuring-Multifactor-Authentication.html)
- Global Multifactor Authentication Configuration  

If no actionable failure mode is encountered the user will be shown a generic "Authentication Failed" message.

# Per Service

Failure mode for a given application can be set as part of the `multifactorPolicy` of the service definition, which will override a failure a mode set at any other location.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^(https|imaps)://.*",
  "id" : 100,
  "multifactorPolicy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
    "multifactorAuthenticationProviders" : [ "java.util.LinkedHashSet", [ "mfa-duo" ] ],
    "failureMode" : "CLOSED"
  }
}
```
      
The following failure modes can be accepted:

| Mode        | Description                                                                                              |
|-------------|----------------------------------------------------------------------------------------------------------|
| `OPEN`      | Disallow MFA, proceed with authentication but don't communicate MFA status/context to the relying party. |
| `CLOSED`    | Disallow MFA and block authentication.                                                                   |
| `PHANTOM`   | Disallow MFA, proceed with authentication and communicate MFA to the relying party.                      |
| `NONE`      | "I am Feeling lucky" option where CAS would not check for failure at all.                                |
| `UNDEFINED` | The default one indicating that no failure mode is set at all.                                           |
