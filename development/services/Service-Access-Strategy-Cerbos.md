# Service Access Strategy - Cerbos

Source: https://apereo.github.io/cas/development/services/Service-Access-Strategy-Cerbos.html

[Cerbos](https://github.com/cerbos/cerbos) is the open core, language-agnostic, scalable authorization solution that makes user permissions and authorization simple to implement and manage by writing context-aware access control policies for your application resources.

This access strategy builds an authorization request and submits it to Cerbos’s `check/resources` API endpoint. Key points about the authorization request:

1. The resource ID is assigned to the numeric identifier of CAS registered service.
2. All principal attributes are packed and included in the authorization request.
3. The following details about the CAS registered service are included in the authorization request:
   - `serviceUrl`: Application URL.
   - `serviceName`: Registered service name.
   - `serviceId`: Registered service id.
   - `serviceFriendlyName`: Registered service friendly name.
   - `serviceType`: Registered service type.

The specifics of the authorization request are taught to CAS using the settings typically defined within the access strategy itself:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "test",
  "id" : 1,
  "accessStrategy" : {
    "@class": "org.apereo.cas.services.CerbosRegisteredServiceAccessStrategy",
    "apiUrl": "http://localhost:3592",
    "token": "...",
    "scope": "scope1",
    "requestId": "...",
    "rolesAttribute": "memberOf",
    "kind": "kind1",
    "actions": [ "java.util.ArrayList", [ "read", "write", "view"] ],
    "auxData": {
        "@class": "java.util.HashMap"
        "jwt": {
            "@class": "java.util.HashMap",
            "token": "...",
            "keySetId": "..."
        }
    }
  }
}
```

The following fields are available to this access strategy:

| Field | Purpose |
| --- | --- |
| `apiUrl` | [1] The Cerbos endpoint URL, defaults to `http://localhost:3592`. |
| `token` | [1] The bearer token to use in the `Authorization` header, if required. |
| `requestId` | [1] Request ID can be anything that uniquely identifies a request. |
| `kind` | [1] Resource kind. Required. This value is used to determine the resource policy to evaluate. |
| `scope` | [1] Resource scope. Optional. |
| `rolesAttribute` | [1] Attribute name, defaulted to `memberOf`, that will indicate a list of roles assigned to the CAS principal. |
| `actions` | List of actions being performed on the resource. |
| `auxData` | Optional. Block for providing auxiliary data. See [Cerbos](https://github.com/cerbos/cerbos) for more info. |

*[1] This field supports the [Spring Expression Language](https://apereo.github.io/cas/development/configuration/Configuration-Spring-Expressions.html) syntax.*
