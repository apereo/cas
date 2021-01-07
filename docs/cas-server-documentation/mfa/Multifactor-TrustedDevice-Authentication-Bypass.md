---
layout: default
title: CAS - Trusted Device Multifactor Authentication
category: Multifactor Authentication
---

{% include variables.html %}

# Bypass - Multifactor Authentication Trusted Device/Browser

Users are allowed to optionally opt out of registering a trusted 
device with CAS as part of the MFA workflow. Furthermore, 
trusted device workflow for MFA can be bypassed on a per application basis:

```json
{
  "@class": "org.apereo.cas.services.RegexRegisteredService",
  "serviceId": "^(https|imaps)://app.example.org",
  "name": "Example",
  "id": 1,
  "multifactorPolicy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
    "bypassTrustedDeviceEnabled" : true
  }
}
```
