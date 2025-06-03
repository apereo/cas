---
layout: default
title: CAS - Configuring Service Access Strategy
category: Services
---

{% include variables.html %}

# Service Access Strategy - OpenFGA

[OpenFGA](https://github.com/openfga/openfga) is a fast, flexible Fine-Grained Authorization system 
that has been designed for reliability and low latency at a high scale. It’s designed, built, and sponsored by Okta/Auth0.

This access strategy builds an authorization request and submits it to OpenFGA's `check` API endpoint. The specifics
of the authorization request are taught to CAS using the settings typically defined within the access strategy itself:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "test",
  "id" : 1,
  "accessStrategy" : {
    "@class": "org.apereo.cas.services.OpenFGARegisteredServiceAccessStrategy",
    "apiUrl": "http://localhost:8080",
    "object": "my-document",
    "relation": "owner",
    "storeId": "Y75hgyt75mhp",
    "token": "92d4a401-86b4-4636-b742-a7c8034756a0"
  }
}
```

The following fields are available to this access strategy:

| Field      | Purpose                                                                                            |
|------------|----------------------------------------------------------------------------------------------------|
| `userType` | <sup>[1]</sup> The user type in the authorization tuple; defaults to `user`.                       |
| `relation` | <sup>[1]</sup> The relation or the type of access in the authorization tuple; defaults to `owner`. |
| `object`   | <sup>[1]</sup> The *object* of the authorization tuple; defaults to the service URL if undefined.  |
| `storeId`  | <sup>[1]</sup> The authorization store identifier.                                                 |
| `apiUrl`   | <sup>[1]</sup> The OpenFGA endpoint URL.                                                           |
| `token`    | <sup>[1]</sup> The bearer token to use in the `Authorization` header, if required.                 |

<sub><i>[1] This field supports the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax.</i></sub>
