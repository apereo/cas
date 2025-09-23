---
layout: default
title: CAS - Configuring Service Access Strategy
category: Services
---

{% include variables.html %}

# Service Access Strategy - Permify

[Permify](https://github.com/Permify/permify) is an open-source authorization as a service inspired by Google Zanzibar, 
designed to build and manage fine-grained and scalable authorization systems for any application.

This access strategy builds an authorization request and submits it to Permify's `check` API endpoint. The specifics
of the authorization request are taught to CAS using the settings typically defined within the access strategy itself:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "test",
  "id" : 1000,
  "accessStrategy" : {
    "@class": "org.apereo.cas.services.PermifyRegisteredServiceAccessStrategy",
    "apiUrl": "http://localhost:3476",
    "tenantId": "...",
    "entityType": "...",
    "subjectType": "...",
    "subjectRelation": "...",
    "permission": "...",
    "token": "92d4a401-86b4-4636-b742-a7c8034756a0"
  }
}
```
 
Note that the entity id in the authorization request is by default set to the numeric identifier of the registered service
definition, i.e. `1000`. Furthermore, the context data in the authorization request contains available principal
attributes as well as the service URL/id itself.

The following fields are available to this access strategy:

| Field             | Purpose                                                                               |
|-------------------|---------------------------------------------------------------------------------------|
| `apiUrl`          | <sup>[1]</sup> The Permify endpoint URL.                                              |
| `token`           | <sup>[1]</sup> The bearer token to use in the `Authorization` header, if required.    |
| `tenantId`        | <sup>[1]</sup> Tenant id for this request evaluation.                                 |
| `entityType`      | <sup>[1]</sup> Entity `type` in the authorization request. Defaults to `application`. |
| `subjectType`     | <sup>[1]</sup> Subject `type` in the authorization request. Defaults to `user`.       |
| `subjectRelation` | <sup>[1]</sup> Subject `relation` in the authorization request. Defaults to `owner`.  |
| `permission`      | <sup>[1]</sup> The action the user wants to perform on the resource.                  |

<sub><i>[1] This field supports the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax.</i></sub>
