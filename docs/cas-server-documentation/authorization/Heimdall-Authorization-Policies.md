---
layout: default
title: CAS - Heimdall - Authorization Policies
description: "The policies Heimdall can attach to resources, from required attributes and scopes to Groovy, REST, JDBC and OpenFGA."
category: Authorization
---

{% include variables.html %}

# Authorization Policies - Heimdall

Policies are the rules that decide whether a request for a resource is allowed. Each resource lists one or more policies, which Heimdall evaluates in order.

Policies are the rules attached to resources to allow or deny access. Each authorizable resource may have one or more
policies assigned to it. Policies are evaluated in the order in which they are defined for the resource. 

<div class="alert alert-info">:information_source: <strong>Coverage</strong>
<p>Please note that not all policies support the AuthZEN protocol. Support in this area will gradually 
improve based on demand and use case discovery. YMMV.</p></div>                           

The following policies are supported by CAS:

{% tabs heimdallauthzpolicies %}

{% tab heimdallauthzpolicies <i class="fa fa-code px-1"></i> Groovy %}
     
An authorization policy that can accept an inline or external [Groovy script](../integration/Apache-Groovy-Scripting.html) to make decisions:

```json
{
  "@class": "org.apereo.cas.heimdall.authorizer.resource.policy.GroovyAuthorizationPolicy",
  "script" :
    '''
      groovy {
          def iAllowThis = true
          return iAllowThis
            ? AuthorizationResult.granted("OK")
            : AuthorizationResult.denied("NOPE")
      }
    '''
}
```

The following parameters are passed to the script:

| Parameter            | Description                                                                 |
|----------------------|-----------------------------------------------------------------------------|
| `resource`           | The matched `AuthorizableResource` object.                                  |
| `request`            | The supplied `AuthorizationRequest` object.                                 |
| `applicationContext` | Reference to the Spring `ApplicationContext` reference.                     |
| `logger`             | The object responsible for issuing log messages such as `logger.info(...)`. |

{% endtab %}

{% tab heimdallauthzpolicies <i class="fa fa-user-group px-1"></i> Grouper Groups %}

An authorization policy that fetches group memberships for the principal from 
[Grouper](https://github.com/Internet2/grouper) and makes decisions based on required groups:

```json
{
  "@class": "org.apereo.cas.heimdall.authorizer.resource.policy.RequiredGrouperGroupsAuthorizationPolicy",
  "groups" : [ "java.util.HashSet", [ "a:b:c" ] ]
}
```

{% endtab %}

{% tab heimdallauthzpolicies Grouper Permissions %}

An authorization policy that fetches permissions for the principal from
[Grouper](https://github.com/Internet2/grouper) using attribute definitions or roles
and allows or denied access based on whether permissions are found:

```json
{
  "@class": "org.apereo.cas.heimdall.authorizer.resource.policy.RequiredGrouperPermissionsAuthorizationPolicy",
  "attributeDefinition" : "a:b:c",
  "roleName": "..."
}
```

{% endtab %}

{% tab heimdallauthzpolicies Required Attributes %}
              
An authorization policy that checks for the **presence** of required attributes in the authorization principal's profile:

```json
{
  "@class": "org.apereo.cas.heimdall.authorizer.resource.policy.RequiredAttributesAuthorizationPolicy",
  "attributes" : {
    "@class" : "java.util.HashMap",
    "memberOf" : [ "java.util.HashSet", [ ".*admin.*" ] ]
  }
}
```

Attribute names refer to the principal's attributes, except for the following qualified names that read the authorization request:

| Name                           | Value                                                             |
|--------------------------------|-------------------------------------------------------------------|
| `subject.id`, `subject.type`   | The AuthZEN subject identifier and type.                          |
| `resource.id`, `resource.type` | The AuthZEN resource identifier and type.                         |
| `action.name`                  | The AuthZEN action name.                                          |
| `subject.properties.<name>`    | A property of the AuthZEN subject, as supplied by the caller.     |
| `resource.properties.<name>`   | A property of the AuthZEN resource, as supplied by the caller.    |
| `action.properties.<name>`     | A property of the AuthZEN action, as supplied by the caller.      |
| `context.<name>`               | An entry of the request `context`.                                |

For example, `"subject.properties.department" : [ "java.util.HashSet", [ "^Finance$" ] ]` requires the caller to describe
the subject as a member of the finance department. Properties are never merged into principal attributes, so a caller
cannot override attributes that CAS resolves for the subject.

{% endtab %}

{% tab heimdallauthzpolicies Rejected Attributes %}

An authorization policy that checks for the **absence** of indicated attributes in the authorization principal's profile,
using the same attribute names as the required attributes policy:

```json
{
  "@class": "org.apereo.cas.heimdall.authorizer.resource.policy.RejectedAttributesAuthorizationPolicy",
  "attributes" : {
    "@class" : "java.util.HashMap",
    "memberOf" : [ "java.util.HashSet", [ ".*admin.*" ] ]
  }
}
```

{% endtab %}

{% tab heimdallauthzpolicies Required ACR %}

An authorization policy that requires a specific `acr` claim in the principal's profile: 

```json
{
  "@class": "org.apereo.cas.heimdall.authorizer.resource.policy.RequiredACRAuthorizationPolicy",
  "acrs" : [ "java.util.HashSet", [ ".*" ] ]
}
```

{% endtab %}

{% tab heimdallauthzpolicies Required AMR %}

An authorization policy that requires a specific `amr` claim in the principal's profile:

```json
{
  "@class": "org.apereo.cas.heimdall.authorizer.resource.policy.RequiredAMRAuthorizationPolicy",
  "amrs" : [ "java.util.HashSet", [ ".*" ] ]
}
```

{% endtab %}

{% tab heimdallauthzpolicies Required Audience %}

An authorization policy that requires a specific `aud` claim in the principal's profile:

```json
{
  "@class": "org.apereo.cas.heimdall.authorizer.resource.policy.RequiredAudienceAuthorizationPolicy",
  "audience" : [ "java.util.HashSet", [ ".*" ] ]
}
```

{% endtab %}

{% tab heimdallauthzpolicies Required Issuer %}

An authorization policy that requires a specific `iss` claim in the principal's profile:

```json
{
  "@class": "org.apereo.cas.heimdall.authorizer.resource.policy.RequiredIssuerAuthorizationPolicy",
  "issuer" : "^http://.*"
}
```

{% endtab %}

{% tab heimdallauthzpolicies Required Scopes %}

An authorization policy that requires the indicated scopes in the principal's profile:

```json
{
  "@class": "org.apereo.cas.heimdall.authorizer.resource.policy.RequiredScopesAuthorizationPolicy",
  "scopes" : [ "java.util.HashSet", [ "profile" ] ]
}
```

{% endtab %}

{% tab heimdallauthzpolicies Rest API %}

An authorization policy can be outsources to a REST API that can make decisions based on the request and the resource:

```json
{
    "@class": "org.apereo.cas.heimdall.authorizer.resource.policy.RestfulAuthorizationPolicy",
    "url": "https://api.example.org",
    "method": "POST",
    "headers": {
      "@class": "java.util.LinkedHashMap",
      "header": "value"
    }
}
```
    
- The request body will contain a map to present the `request` and the `resource` JSON payloads. The `resource` excludes its policies.
- Authorized requests are expected to receive a `200` response code.
- The `url` and header values can be constructed using the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html)

{% endtab %}

{% tab heimdallauthzpolicies OpenFGA %}

An authorization policy that passes the request to [OpenFGA](https://openfga.dev/) to make decisions:

```json
{
  "@class": "org.apereo.cas.heimdall.authorizer.resource.policy.OpenFGAAuthorizationPolicy",
  "token": "...",
  "apiUrl": "...",
  "storeId": "...",
  "relation": "...",
  "userType": "user", 
}
```

The following parameters are passed to OpenFGA:

| Parameter  | Description                                                                                        |
|------------|----------------------------------------------------------------------------------------------------|
| `token`    | <sup>[1]</sup> The bearer authorization token passed via the `Authorization` header.               |
| `apiUrl`   | <sup>[1]</sup> OpenFGA base API endpoint that ultimately invokes the `check` API.                  |
| `storeId`  | <sup>[1]</sup> The authorization store identifier.                                                 |
| `relation` | <sup>[1]</sup> The relation or the type of access in the authorization tuple; defaults to `owner`. |
| `userType` | Indicates the type of principal. Defaults to `user`.                                               |
  
The `object` field in the API request is composed of the following elements:

```bash
$REQUEST_NAMESPACE + ':' + $REQUEST_METHOD + ':' + $REQUEST_URI
```

For AuthZEN requests, the `object` field is composed of `$RESOURCE_TYPE + ':' + $RESOURCE_ID`, the `relation` defaults
to the AuthZEN action name, and the `userType` defaults to the AuthZEN subject type.

<sub><i>[1] This field supports the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax.</i></sub>

{% endtab %}

{% tab heimdallauthzpolicies JDBC %}

An authorization policy that executes a SQL query against a relation database. 
The query is expected to return an `authorized` column of a `boolean` type.

```json
{
  "@class": "org.apereo.cas.heimdall.authorizer.resource.policy.JdbcAuthorizationPolicy",
  "query": "...",
  "username": "...",
  "password": "...",
  "url": "..."
}
```

The following settings are available:

| Parameter        | Description                                                                               |
|------------------|-------------------------------------------------------------------------------------------|
| `query`          | The SQL query that is executed. Supports named parameters such as `parameter`. See below. |
| `url`            | <sup>[1]</sup> The database connection string, i.e. `jdbc:mysql://localhost:3306/cas`     |
| `username`       | <sup>[1]</sup> The username when building a database connection.                          |
| `password`       | <sup>[1]</sup> The password when building a database connection.                          |
| `dataSourceName` | Optional name of the data source bean to use; see below.                                  |
| `queryTimeout`   | Maximum time the query may run, i.e. `PT5S` (default). `0` or `INFINITE` disables it.     |

<sub><i>[1] This field supports the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax.</i></sub>

The policy looks up its data source as a bean in the application context, named `dataSourceName` when defined or
`heimdallJdbcDataSource-<hash>` derived from the URL and username otherwise. When no such bean exists, CAS creates a
connection pool with default settings that keeps no idle connections, registers it under that name, and shares it
across all policies with the same name until CAS shuts down. A deployment may define its own data source bean with that
name to control pooling.

<div class="alert alert-info">:information_source: <strong>Note</strong><p>The connection pool is keyed by the URL and
username only. A policy that changes only its <code>password</code> keeps using the existing pool, and its connections
keep the old password until CAS restarts. To rotate a password without a restart, give the policy a new
<code>dataSourceName</code>, or define and manage the data source bean yourself.</p></div>

A query that runs longer than `queryTimeout` is cancelled and the policy fails. A request that fails is not
authorized: the legacy endpoint returns `403` and the AuthZEN endpoint returns `500`. The timeout applies to the query only; waiting for a free
pooled connection follows the pool's own connection timeout.

The SQL query is preprocessed to receive the following named parameters:

- `method` from the authorization request.
- `uri` from the authorization request.
- `namespace` from the authorization request.
- `principal` from the authorization request.

For AuthZEN requests, the query also receives `subjectType`, `subjectId`, `resourceType`, `resourceId` and `action`.

Furthermore, all context attributes from the authorization request as well as all principal attributes are passed as named parameters
and can be used and referenced in the query. Context and principal attributes cannot replace any of the named parameters listed above.

{% endtab %}

{% endtabs %}
