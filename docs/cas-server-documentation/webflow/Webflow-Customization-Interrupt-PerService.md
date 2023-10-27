---
layout: default
title: CAS - Authentication Interrupt
category: Webflow Management
---

{% include variables.html %}

# Tracking Authentication Interrupts Per Service

Application definitions may be assigned a dedicated webflow interrupt policy. A sample JSON file follows:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "sample service",
  "id" : 100,
  "webflowInterruptPolicy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceWebflowInterruptPolicy",
    "enabled": true,
    "forceExecution": "TRUE",
    "attributeName": "mem...of",
    "attributeValue": "^st[a-z]ff$"
  }
}
```

<div class="alert alert-info">:information_source: <strong>Protocol Support</strong>
<p>Authentication interrupts should work for all client application types supported by CAS, regardless of authentication protocol.
Whether your application speaks CAS, SAML2, OpenID Connect, etc, the interrupt policy should equally apply and its method of
configuration in the application definition remains the same.</p></div>

The following policy settings are supported:

| Field             | Description                                                                                                       |
|-------------------|-------------------------------------------------------------------------------------------------------------------|
| `enabled`         | Whether interrupt notifications are enabled for this application. Default is `true`.                              |
| `forceExecution`  | Whether execution should proceed anyway, regardless. Accepted values are `TRUE`, `FALSE` or `UNDEFINED`.          |
| `attributeName`   | Regular expression pattern to compare against authentication and principal attribute names to trigger interrupt.  |
| `attributeValue`  | Regular expression pattern to compare against authentication and principal attribute values to trigger interrupt. |

