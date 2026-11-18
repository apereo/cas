# Service Access Strategy - Permify

Source: https://apereo.github.io/cas/development/services/Service-Access-Strategy-Permify.html

[Permify](https://github.com/Permify/permify) is an open-source authorization as a service inspired by Google Zanzibar, designed to build and manage fine-grained and scalable authorization systems for any application.

This access strategy builds an authorization request and submits it to Permify’s `check` API endpoint. The specifics of the authorization request are taught to CAS using the settings typically defined within the access strategy itself:

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

Note that the entity id in the authorization request is by default set to the numeric identifier of the registered service definition, i.e. `1000`. Furthermore, the context data in the authorization request contains available principal attributes as well as the service URL/id itself.

The following fields are available to this access strategy:

| Field | Purpose |
| --- | --- |
| `apiUrl` | [1] The Permify endpoint URL. |
| `token` | [1] The bearer token to use in the `Authorization` header, if required. |
| `tenantId` | [1] Tenant id for this request evaluation. |
| `entityType` | [1] Entity `type` in the authorization request. Defaults to `application`. |
| `subjectType` | [1] Subject `type` in the authorization request. Defaults to `user`. |
| `subjectRelation` | [1] Subject `relation` in the authorization request. Defaults to `owner`. |
| `permission` | [1] The action the user wants to perform on the resource. |

*[1] This field supports the [Spring Expression Language](https://apereo.github.io/cas/development/configuration/Configuration-Spring-Expressions.html) syntax.*
