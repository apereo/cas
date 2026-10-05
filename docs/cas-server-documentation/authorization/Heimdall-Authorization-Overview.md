---
layout: default
title: CAS - Heimdall Authorization
description: "Heimdall, the CAS authorization engine for APIs: policies, resources, the AuthZEN evaluation API and integration with gateways and proxies."
category: Authorization
---

{% include variables.html %}

# Heimdall Authorization

Heimdall is a simple rule-based authorization engine whose main responsibility is to accept an authorization request
in form of an HTTP payload and return a decision whether the request is allowed or denied in form of an HTTP response code. You
can put this authorization engine behind API gateways, and reverse proxies to protect your APIs and services and allow them
to formulate an authorization request to CAS, receive a response and translate that back to the caller.
       
> In Norse mythology, Heimdall is a god and gatekeeper who keeps watch for invaders and is 
> attested as possessing foreknowledge and keen senses. As gatekeeper, he is responsible for
> the rainbow bridge Bifrost and keeps a watchful eye on passengers.

The general flow can be summarized using the following steps:
   
- Authorizable resources are registered with CAS
  - ...with the appropriate method, URI, namespace and context
  - ...with the appropriate authorization policies
- Authorization request is submitted to CAS
  - ...with the appropriate principal/subject
  - ...with the appropriate method, URI, namespace and context
- CAS locates the matching authorizable resource based on the request
- ...and then determines the principal/subject based on the request
- CAS then consults the authorization engine to make a decision based on the resource, the principal and the request
- CAS returns a response to the caller, either accepting or denying the request

<div class="alert alert-info">:information_source: <strong>Usage</strong>
<p>Note that CAS is simply acting as the policy definition point (PDP) as well as the policy information point (PIP).
The authorization enforcement (PEP) must happen somewhere else by the calling party, which typically happens to be
an API gateway or nginx reverse proxy, etc.</p></div>

## Configuration

Heimdall authorization support is enabled by including the following dependency in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-heimdall" %}

{% include_cached casproperties.html properties="cas.heimdall" %}

## Authorization Request

{% tabs authorizationrequest %}

{% tab authorizationrequest Heimdall %}

The authorization request is a simple payload that is sent to the Heimdall authorization
engine using the endpoint `/heimdall/authorize` via a `POST`. The payload has the following structure:

```json
{
  "method" : "POST",
  "uri" : "/api/example?hello=world",
  "namespace" : "API_EXAMPLE",
  "context" : {
    "key" : "value"
  }
}
```

...which is trying to ask CAS:

> Is the request to `/api/example?hello=world`, owned by `API_EXAMPLE`, using the HTTP method `POST`, allowed?

The following elements are supported:

| Field       | Description                                                                               |
|-------------|-------------------------------------------------------------------------------------------|
| `method`    | The requested HTTP method to allow or deny.                                               |
| `uri`       | The request URI intended for access and invocation by the caller.                         |
| `namespace` | Logical name for the owner of the API or resource in question.                            |
| `context`   | Free-form key-value pairs for more advanced decisions based on arbitrary contextual data. |

Typical responses include `200`, `401` or `403`.

{% endtab %}

{% tab authorizationrequest AuthZEN %}

Heimdall also supports the OpenID [AuthZEN Authorization API 1.0](https://openid.net/specs/authorization-api-1_0.html) Access Evaluation API. Using this strategy, the authorization
request is composed of the following entities:

```json
{
  "subject": {
    "type": "user",
    "id": "alice@acmecorp.com"
  },
  "resource": {
    "type": "account",
    "id": "123"
  },
  "action": {
    "name": "can_read",
    "properties": {
      "hello": "world"
    }
  },
  "context": {
    "field": "value"
  }
}
```

This authorization request is sent to the Heimdall authorization engine using the endpoint `/heimdall/authzen` 
via a `POST`. Once the request is evaluated, the typical response may match the following:

```json
{
  "decision": true
}
```

The `subject`, `resource` and `action` objects are required, along with `subject.type`, `subject.id`, `resource.type`,
`resource.id` and `action.name`. A request that is missing any of these is rejected with a `400` status code, and a caller that
cannot be authenticated receives a `401` status code. A request that is evaluated and denied receives a `200` status code with
`"decision": false` and a [decision context](#decision-context). If the request carries an `X-Request-ID` header, the same value is returned in the response.

Note that `resource.id` identifies the resource instance being accessed, such as a specific account or document,
and is not the name of a policy namespace. AuthZEN requests are matched against authorizable resources in *every* namespace
using their `resourceType`, `actions` and optional `resourceIdPattern` fields; the URI pattern, method and namespace fields
are ignored for AuthZEN requests. Likewise, the `/heimdall/authorize` endpoint rejects requests that carry AuthZEN `subject`,
`resource` or `action` fields with a `400` status code.

See [AuthZEN](#authzen) for the [decision context](#decision-context), [access evaluations](#access-evaluations)
and [policy decision point metadata](#policy-decision-point-metadata).

{% endtab %}

{% endtabs %}

## AuthZEN

The following capabilities are specific to the [AuthZEN](https://openid.net/specs/authorization-api-1_0.html) protocol.

### Decision Context

A denied decision carries a `context` with a `reason` code that tells the policy enforcement point why access was denied:

```json
{
  "decision": false,
  "context": {
    "reason": "policy_denied"
  }
}
```

| Reason                 | Description                                                                 |
|------------------------|-----------------------------------------------------------------------------|
| `no_matching_resource` | No authorizable resource matches the resource type, action and resource id. |
| `no_policies`          | A matching resource defines no authorization policies.                      |
| `policy_denied`        | The authorization policies of a matching resource denied access.            |
| `subject_unresolved`   | No principal could be resolved for the subject.                             |

A granted decision carries no `context`. The reason names the stage of the evaluation that denied access, never the policy
or the attributes involved, so that callers learn nothing about how policies are built; the details are logged by CAS at
the `DEBUG` level for the `org.apereo.cas.heimdall` package.

### Access Evaluations

Several requests can be evaluated in one call to `/heimdall/authzen/evaluations` via a `POST`. The top-level `subject`, `resource`,
`action` and `context` are defaults for each entry of `evaluations`, and any of them set on an entry replaces the default:

```json
{
  "subject": { "type": "user", "id": "alice@acmecorp.com" },
  "action": { "name": "can_read" },
  "options": { "evaluations_semantic": "execute_all" },
  "evaluations": [
    { "resource": { "type": "document", "id": "1" } },
    { "resource": { "type": "document", "id": "2" }, "action": { "name": "can_edit" } }
  ]
}
```

The response lists the decisions in the order of the requested evaluations:

```json
{
  "evaluations": [
    { "decision": true },
    { "decision": false }
  ]
}
```

The caller is authenticated once for the whole request, so a single-use JWT bearer assertion covers every evaluation.
An entry that is missing a required field, or that cannot be evaluated, is denied with an error in its `context`, for example
`{"decision": false, "context": {"error": {"status": 400, "message": "Resource id is required"}}}`, while the other entries
are still evaluated. The following `evaluations_semantic` values are supported:

| Value                    | Description                                                                           |
|--------------------------|---------------------------------------------------------------------------------------|
| `execute_all`            | Default. Evaluate every entry and return every decision.                              |
| `deny_on_first_deny`     | Stop at the first denial or error; the decisions up to and including it are returned. |
| `permit_on_first_permit` | Stop at the first permit; the decisions up to and including it are returned.          |

A request without `evaluations`, or with an empty list, is evaluated as a single access evaluation and receives a single decision.
Denied entries carry the same [decision context](#decision-context) as single evaluations.

### Search

The optional AuthZEN search APIs for subjects, resources and actions are not supported yet, and may be worked out in the future.
Most Heimdall policies evaluate a single request, such as attribute, REST, JDBC or Groovy policies, rather than store the relationships
a search would enumerate.

### Policy Decision Point Metadata

The policy decision point is identified by `${cas.server.prefix}/heimdall`, for example `https://sso.example.org/cas/heimdall`,
and publishes its [metadata](https://openid.net/specs/authorization-api-1_0.html#name-policy-decision-point-metadata)
at `/cas/heimdall/.well-known/authzen-configuration`:

```json
{
  "policy_decision_point": "https://sso.example.org/cas/heimdall",
  "access_evaluation_endpoint": "https://sso.example.org/cas/heimdall/authzen",
  "access_evaluations_endpoint": "https://sso.example.org/cas/heimdall/authzen/evaluations"
}
```

The specification locates metadata by inserting `/.well-known/authzen-configuration` between the host and the path of
the policy decision point identifier, so a policy enforcement point asks for
`https://sso.example.org/.well-known/authzen-configuration/cas/heimdall`. That path lies outside the CAS web application
context, and must be rewritten by the proxy that fronts CAS or by the
[embedded Apache Tomcat rewrite valve](../installation/Servlet-Container-Embedded-Tomcat-RewriteValve.html) if that is the container you are using. 
The valve must be registered on the engine which sees requests before a web application context is selected, with a rewrite configuration such as:

```bash
RewriteRule ^/\.well-known/authzen-configuration(/.+)$ $1/.well-known/authzen-configuration [L]
```

## Authorization Principal
       
The authorization request is expected to provide an `Authorization` header using the `Bearer` or `Basic` schemes (`Authorization: Bearer/Basic ...`). 
The token in the header must indicate the *who*, the subject or the authorization principal that wants to access the resource
using the details specified in the request.

The authorization header value can be *one* of the following:

- An OpenID Connect **ID token**, passed as a `Bearer` token, produced by CAS when acting as a [OpenID Connect Provider](../authentication/OIDC-Authentication.html).
- A **JWT access token**, passed as a `Bearer` token, produced by CAS when acting as an [OAuth](../authentication/OAuth-Authentication.html) or [OpenID Connect](../authentication/OIDC-Authentication.html) identity provider.
- An **opaque access token** (i.e. `AT-1-...`), passed as a `Bearer` token, produced by CAS when acting an [OAuth](../authentication/OAuth-Authentication.html) or [OpenID Connect](../authentication/OIDC-Authentication.html) identity provider.
- A **JWT bearer token** passed as a `Bearer` token and one that follows the semantics of the [JWT Authorization grant](../authentication/OIDC-Authentication-JWT-Bearer.html).
- A valid base64-encoded `username:password`, passed as a `Basic` token, that can be accepted by the CAS authentication engine.
  For AuthZEN requests, `Basic` credentials are instead the `client_id:client_secret` of an OAuth or OpenID Connect application
  registered with CAS; CAS user credentials are rejected there.

<div class="alert alert-warning">:warning: <strong>Usage Warning</strong><p>On <code>/heimdall/authorize</code>,
<code>Basic</code> credentials go through a complete CAS authentication on every request: the authentication handlers
verify the password (for example, an LDAP bind or a deliberately slow password hash), and the attempt is audited,
throttled and may count toward account lockout like any other login. Behind a gateway that asks Heimdall about every
API call, this means one full login per call, and the user's password travels with every request. Prefer access
tokens for gateways, and keep <code>Basic</code> for low-volume callers.</p></div>

Claims or attributes from all token types are extracted and attached to the final principal, which is then
passed to the authorization policy engine to make decisions. However, when using the AuthZEN protocol
CAS will attempt to resolve claims and attributes based on the `subject` ID in the authorization request, but only for
the `user` subject type. Subjects of any other type, such as services
or devices, are evaluated by their identifier and the properties supplied in the request without any lookup.

The request `context` of an AuthZEN request is exactly what the caller sends. For `/heimdall/authorize`, the HTTP request headers
are also added to the `context`, except for credential and protocol headers such as `Authorization`, `Cookie`, `Host`
and `Content-Type`; entries sent in the request body take precedence over headers with the same name.

The claims-based policies (required scopes, ACR, AMR, audience and issuer) evaluate the principal's attributes. For
AuthZEN requests, where the principal describes the subject rather than a token, use the qualified names of the
required attributes policy (for example `subject.properties.acr` or `context.acr`) instead.

Tokens are further subject to the following rules:

- The token must be issued to an OAuth or OpenID Connect application that is registered with CAS and whose access strategy allows access.
- A token that is bound to a key via [DPoP](../authentication/OIDC-Authentication-DPoP.html) must be presented using the `DPoP`
  authorization scheme along with a valid DPoP proof for the Heimdall endpoint; it is rejected when presented as a `Bearer` token.
- A token that is bound to a client certificate via mutual TLS is only accepted when the same client certificate is presented on the request.
- A JWT bearer token must carry `jti` and `iat` claims, may be presented only once, and its lifetime between `iat` and `exp`
  may not exceed a configurable maximum that defaults to five minutes.

When [authentication throttling](../authentication/Configuring-Authentication-Throttling.html) is enabled, failed caller
authentication attempts (`401`) on `/heimdall/authorize` and `/heimdall/authzen` are throttled; authorization denials
and malformed requests are not counted.

Applications can be individually prevented from calling Heimdall with their tokens using a dedicated access strategy:

```json
{
  "@class": "org.apereo.cas.services.OidcRegisteredService",
  "clientId": "client",
  "serviceId": "^https://app.example.org/.+",
  "name": "Sample",
  "id": 1,
  "accessStrategy": {
    "@class": "org.apereo.cas.heimdall.services.HeimdallRegisteredServiceAccessStrategy",
    "allowed": false
  }
}
```

The Heimdall access strategy may also be used as part of a chain of access strategies. Applications without this
access strategy are allowed to call Heimdall, as long as their access strategy allows access.
        
## Authorization Resources

Authorizable resources and APIs that are to be supported and protected by Heimdall are expected to be registered with CAS. This is done
by defining and configuring a list of resources and their associated owners via flat JSON files. For easier discovery, files are named and thus
categorized by API owner or group (i.e. `API_EXAMPLE.json`) that describe a collection of APIs in that namespace:

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
    
Note that policies are loaded, sorted and evaluated using the order in which they are defined in the file. If you have policies
that operate on patterns, you may want to ensure that the most specific policies are listed first.

<div class="alert alert-info">:information_source: <strong>Usage</strong>
<p>Remember that the file name is mostly irrelevant. While we recommend reasonable naming conventions,
the <code>namespace</code> field inside the policy is really the piece that determines its owner.</p></div>

<div class="alert alert-info">:information_source: <strong>AuthZEN Resources</strong>
<p>An AuthZEN request is matched against resources in all namespaces. When more than one resource matches the request,
every matching resource must grant access for the decision to be allowed.</p></div>

The authorization policies owned by the indicated namespace and resource support the following elements:

| Field                | Description                                                                                                                |
|----------------------|----------------------------------------------------------------------------------------------------------------------------|
| `id`                 | Unique numeric identifier for this resource.                                                                               |
| `pattern`            | <sup>[1]</sup> The URI regular expression pattern that describes the resource or API endpoint.                             |
| `method`             | <sup>[1]</sup> The HTTP method (as a regular expression pattern, or `*` for all) that is allowed to access the resource.   |
| `policies`           | A list of policies that are attached to the resource to allow or deny access. A resource without policies denies access.   |
| `enforceAllPolicies` | Whether all policies must grant access. When `false`, the default, any one policy granting access is enough.               |
| `properties`         | Arbitrary key-value pairs attached to the resource for advanced decision making.                                           |
| `resourceType`       | <sup>[2]</sup> The AuthZEN resource type, matched exactly against `resource.type`.                                         |
| `actions`            | <sup>[2]</sup> The set of AuthZEN action names, one of which must match `action.name` exactly.                             |
| `resourceIdPattern`  | <sup>[2]</sup> Optional regular expression that must match the entire AuthZEN `resource.id`; all ids match when undefined. |

<sub><i>[1] This field is not necessary when using the AuthZEN protocol.</i></sub>
<sub><i>[2] This field is only used by the AuthZEN protocol; a resource without a `resourceType` never matches AuthZEN requests.</i></sub>

Policies are evaluated in the order they are defined. When `enforceAllPolicies` is `true`, evaluation stops at the first
policy that denies access or fails with an error. Otherwise, a policy that fails, for example because its database or
REST endpoint is unavailable, is logged and skipped so that a later policy can still grant access. If no policy grants
access and at least one policy failed, the request fails with an error rather than a denial.

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

### Custom

You can also build your own repository implementation to register and load authorizable resources.
This may be done by providing a dedicated implementation of `AuthorizableResourceRepository`
and registering it with the runtime:

```java
@Bean
public AuthorizableResourceRepository authorizableResourceRepository(
    return new MyResourceRepository();
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn
more about how to register configurations into the CAS runtime.

## Authorization Policies

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

## Gateway Example

An nginx reverse proxy can act as the policy enforcement point with its `auth_request` module, sending each request
to `/heimdall/authorize` before passing it upstream:

```nginx
map $request_uri $heimdall_unsafe_uri {
    default     0;
    '~["\\\\]'  1;
}

server {
    location /api {
        if ($heimdall_unsafe_uri) {
            return 400;
        }
        auth_request /authorize;
        proxy_pass https://api.example.org;
    }

    location = /authorize {
        internal;
        proxy_method POST;
        proxy_pass_request_body off;
        proxy_pass https://sso.example.org/cas/heimdall/authorize;
        proxy_set_header Content-Type application/json;
        proxy_set_body '{"namespace": "API_EXAMPLE", "method": "$request_method", "uri": "$request_uri", "context": {"client_ip": "$remote_addr"}}';
    }
}
```

The subrequest carries the client's headers, including `Authorization`, and must use `POST`; see the warning above
about `Basic` credentials behind a gateway. nginx does not escape
variables in the request body, so the `map` rejects URIs that contain quotes or backslashes, which could otherwise
change the namespace or other fields. `auth_request` allows the request on a `2xx` response and refuses it on `401`
or `403`; any other status, such as `404` when no resource matches, becomes a `500`.

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="heimdall" %}
