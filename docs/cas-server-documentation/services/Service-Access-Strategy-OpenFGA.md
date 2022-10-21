---
layout: default
title: CAS - Configuring Service Access Strategy
category: Services
---

{% include variables.html %}

# Service Access Strategy - OpenFGA

[OpenFGA](https://github.com/openfga/openfga) is a fast, flexible Fine-Grained Authorization System 
has been designed for reliability and low latency at a high scale. It makes it easy for application developers 
to model their access control layer, and to add and integrate fine-grained authorization in a way that is consistent 
across all of their applications. It’s designed, built, and sponsored by Okta/Auth0.

This access strategy builds an authorization request and submits to OpenFGA's `check` API endpoint. The specifics
of the authorization request are taught to CAS using the settings typically defined within the access strategy itself:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "test",
  "id" : 1,
  "accessStrategy" : {
    "@class": "org.apereo.cas.services.OpenFGARegisteredServiceAccessStrategy",
    "relation": "owner",
    "object": "my-document",
    "storeId": "75hgyt75mhp",
    "apiUrl": "https://localhost:8080",
    "token": "92d4a401-86b4-4636-b742-a7c8034756a0"
  }
}
```

The following fields are available to this access strategy:

| Field      | Description                                                                                      |
|------------|--------------------------------------------------------------------------------------------------|
| `relation` | <sup>*</sup> The relation or access request in the authorization tuple; defaults to `owner`.     |
| `object`   | <sup>*</sup> The **object** of the authorization tuple; default to the service URL if undefined. |
| `storeId`  | <sup>*</sup> The authorization store identifier.                                                 |
| `apiUrl`   | <sup>*</sup> The OpenFGA endpoint URL.                                                           |
| `token`    | <sup>*</sup> The bearer token to use in the `Authorization` header, if required.                 |
  

<sup>*</sup> *This fields supports the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax*
