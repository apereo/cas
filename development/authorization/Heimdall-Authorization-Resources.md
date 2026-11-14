# Authorization Resources - Heimdall

Source: https://apereo.github.io/cas/development/authorization/Heimdall-Authorization-Resources.html

Heimdall only makes decisions for resources it knows about. Each protected API or resource is registered with CAS, grouped by namespace, together with the policies that decide access to it.

Authorizable resources and APIs that are to be supported and protected by Heimdall are expected to be registered with CAS. This is done by defining and configuring a list of resources and their associated owners via flat JSON files. For easier discovery, files are named and thus categorized by API owner or group (i.e. `API_EXAMPLE.json`) that describe a collection of APIs in that namespace:

```json
{
  "@class": "org.apereo.cas.heimdall.authorizer.resource.AuthorizableResources",
  "resources": [
    "java.util.ArrayList",
    [
      {
        "@class": "org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource",
        "id": 1,
        "pattern": "/api/example.*",
        "method": "PUT",
        "enforceAllPolicies": false,
        "policies": [ "java.util.ArrayList", [
            {}
        ]],
        "properties" : {
            "@class" : "java.util.HashMap",
            "key" : "value"
        }
      }
    ]
  ],
  "namespace": "API_EXAMPLE"
}
```

Note that policies are loaded, sorted and evaluated using the order in which they are defined in the file. If you have policies that operate on patterns, you may want to ensure that the most specific policies are listed first.

> **Usage**
>
> Remember that the file name is mostly irrelevant. While we recommend reasonable naming conventions, the `namespace` field inside the policy is really the piece that determines its owner.

> **AuthZEN Resources**
>
> An AuthZEN request is matched against resources in all namespaces. When more than one resource matches the request, every matching resource must grant access for the decision to be allowed.

The authorization policies owned by the indicated namespace and resource support the following elements:

| Field | Description |
| --- | --- |
| `id` | Unique numeric identifier for this resource. |
| `pattern` | [1] The URI regular expression pattern that describes the resource or API endpoint. |
| `method` | [1] The HTTP method (as a regular expression pattern, or `*` for all) that is allowed to access the resource. |
| `policies` | A list of policies that are attached to the resource to allow or deny access. A resource without policies denies access. |
| `enforceAllPolicies` | Whether all policies must grant access. When `false`, the default, any one policy granting access is enough. |
| `properties` | Arbitrary key-value pairs attached to the resource for advanced decision making. |
| `resourceType` | [2] The AuthZEN resource type, matched exactly against `resource.type`. |
| `actions` | [2] The set of AuthZEN action names, one of which must match `action.name` exactly. |
| `resourceIdPattern` | [2] Optional regular expression that must match the entire AuthZEN `resource.id`; all ids match when undefined. |

*[1] This field is not necessary when using the AuthZEN protocol.* *[2] This field is only used by the AuthZEN protocol; a resource without a `resourceType` never matches AuthZEN requests.*

Policies are evaluated in the order they are defined. When `enforceAllPolicies` is `true`, evaluation stops at the first policy that denies access or fails with an error. Otherwise, a policy that fails, for example because its database or REST endpoint is unavailable, is logged and skipped so that a later policy can still grant access. If no policy grants access and at least one policy failed, the request fails with an error rather than a denial.

For example, the following resource grants AuthZEN `can_read` and `can_write` requests for documents whose id starts with `doc-`:

```json
{
  "@class": "org.apereo.cas.heimdall.authorizer.resource.AuthorizableResource",
  "id": 2,
  "resourceType": "document",
  "resourceIdPattern": "doc-.+",
  "actions": [ "java.util.HashSet", [ "can_read", "can_write" ] ],
  "policies": [ "java.util.ArrayList", [
      {}
  ]]
}
```

## Custom

You can also build your own repository implementation to register and load authorizable resources. This may be done by providing a dedicated implementation of `AuthorizableResourceRepository` and registering it with the runtime:

```java
@Bean
public AuthorizableResourceRepository authorizableResourceRepository(
    return new MyResourceRepository();
}
```

[See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
