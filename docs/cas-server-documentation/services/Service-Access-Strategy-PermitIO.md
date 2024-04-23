---
layout: default
title: CAS - Configuring Service Access Strategy
category: Services
---

{% include variables.html %}

# Service Access Strategy - Permit.io

[Permit.io](https://github.com/permitio) offers permissions as a service, 
allowing developers to bake-in permissions and access control into applications quickly. It offers a centralized control 
panel, SDKs, APIs and microservices developers need to add to create a decision and enforcement points.

This access strategy attempts to sync the user with Permit.io, and then builds an authorization request and submits it to Permit.io. The specifics
of the authorization request are taught to CAS using the settings typically defined within the access strategy itself:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "test",
  "id" : 1,
  "accessStrategy" : {
    "@class": "org.apereo.cas.services.PermitRegisteredServiceAccessStrategy",
    "action": "...",
    "resource": "...",
    "apiKey": "...",
    "tenant": "default",
    "pdpAddress": "https://cloudpdp.api.permit.io",
    "emailAttributeName": "email",
    "firstNameAttributeName": "firstname",
    "lastNameAttributeName": "lastname",
    "context" : {
      "@class" : "java.util.TreeMap",
      "param1" : "value1"
    }
  }
}
```

The following fields are available to this access strategy:

| Field                    | Purpose                                                                                                        |
|--------------------------|----------------------------------------------------------------------------------------------------------------|
| `apiKey`                 | <sup>[1]</sup> The Permit.io API SDK key that allows the SDK to authenticate, sync accounts, etc.              |
| `action`                 | The action or permission that needs to be performed or evaluated.                                              |
| `resource`               | The resource which is being requested for access.                                                              |
| `tenant`                 | Optional. Tenant id or key is defined in Permit.io and used in scenarios when you might have multiple tenants. |
| `pdpAddress`             | <sup>[1]</sup> Optional. The Permit.io API endpoint.                                                           |
| `emailAttributeName`     | Optional. Name of the attribute used to identify the principal's email when syncing accounts.                  |
| `firstNameAttributeName` | Optional. Name of the attribute used to identify the principal's firstname when syncing accounts.              |
| `lastNameAttributeName`  | Optional. Name of the attribute used to identify the principal's lastname when syncing accounts.               |

<sub><i>[1] This field supports the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax.</i></sub>
