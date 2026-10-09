---
layout: default
title: CAS - Bypass - Multifactor Authentication Trusted Device/Browser
category: Multifactor Authentication
---

{% include variables.html %}

# Bypass - Multifactor Authentication Trusted Device/Browser

Skip the trusted device step for selected applications, or let users decline to register a trusted device during MFA.

Users are allowed to optionally opt out of registering a trusted 
device with CAS as part of the MFA workflow. Furthermore, 
trusted device workflow for MFA can be bypassed on a per application basis:

```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "serviceId": "^(https|imaps)://app.example.org",
  "name": "Example",
  "id": 1,
  "multifactorPolicy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
    "bypassTrustedDeviceEnabled" : true
  }
}
```
