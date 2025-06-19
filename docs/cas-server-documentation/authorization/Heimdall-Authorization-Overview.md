---
layout: default
title: CAS - Heimdall Authorization
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

Heimdall also supports the OpenID Connect AuthZEN Access Evaluation API. Using this strategy, the authorization
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

{% endtab %}

{% endtabs %}

## Authorization Principal
       
The authorization request is expected to provide an `Authorization` header using the `Bearer` or `Basic` schemes (`Authorization: Bearer/Basic ...`). 
The token in the header must indicate the *who*, the subject or the authorization principal that wants to access the resource
using the details specified in the request.

The authorization header value can be *one* of the following:

- An OpenID Connect **ID token**, passed as a `Bearer` token, produced by CAS when acting as a [OpenID Connect Provider](../authentication/OIDC-Authentication.html).
- A **JWT access token**, passed as a `Bearer` token, produced by CAS when acting as an [OAuth](../authentication/OAuth-Authentication.html) or [OpenID Connect](../authentication/OIDC-Authentication.html) identity provider.
- An **opaque access token** (i.e. `AT-1-...`), passed as a `Bearer` token, produced by CAS when acting an [OAuth](../authentication/OAuth-Authentication.html) or [OpenID Connect](../authentication/OIDC-Authentication.html) identity provider.
- A valid Base64-encoded `username:password`, passed as a `Basic` token, that can be accepted by the CAS authentication engine.

Claims or attributes from all token types are extracted and attached to the final principal, which is then
passed to the authorization policy engine to make decisions. However, when using OpenID Connect ID AuthZEN protocol
CAS will attempt to resolve claims and attributes based on the `subject` ID in the authorization request.
        
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

<div class="alert alert-info">:information_source: <strong>AuthZEN Namespace</strong>
<p>Please note that when using the AuthZEN protocol, the authorization resource's <code>namespace</code> field is 
expected to be set to the resource ID field for relevant resources and policies to be found and activated.</p></div>

The authorization policies owned by the indicated namespace and resource support the following elements:

| Field                | Description                                                                                                              |
|----------------------|--------------------------------------------------------------------------------------------------------------------------|
| `id`                 | Unique numeric identifier for this resource.                                                                             |
| `pattern`            | <sup>[1]</sup> The URI regular expression pattern that describes the resource or API endpoint.                           |
| `method`             | <sup>[1]</sup> The HTTP method (as a regular expression pattern, or `*` for all) that is allowed to access the resource. |
| `policies`           | A list of policies that are attached to the resource to allow or deny access.                                            |
| `enforceAllPolicies` | Whether all policies must be consulted to authorize the request. Default is `false`.                                     |
| `properties`         | Arbitrary key-value pairs attached to the resource for advanced decision making.                                         |

<sub><i>[1] This field is not necessary when using the AuthZEN protocol.</i></sub>

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

{% endtab %}

{% tab heimdallauthzpolicies Rejected Attributes %}

An authorization policy that checks for the **absence** of indicated attributes in the authorization principal's profile:

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
    
- The request body will contain a map to present the `request` and the `resource` JSON payloads.
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

| Parameter  | Description                                                                                |
|------------|--------------------------------------------------------------------------------------------|
| `query`    | The SQL query that is executed. Supports named parameters such as `:parameter`. See below. |
| `url`      | <sup>[1]</sup> The database connection string, i.e. `jdbc:mysql://localhost:3306/cas`      |
| `username` | <sup>[1]</sup> The username when building a database connection.                           |
| `password` | <sup>[1]</sup> The password when building a database connection.                           |

<sub><i>[1] This field supports the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax.</i></sub>

The SQL query is preprocessed to receive the following named parameters:

- `:method` from the authorization request.
- `:uri` from the authorization request.
- `:namespace` from the authorization request.
- `:principal` from the authorization request.

Furthermore, all context attributes from the authorization request as well as all principal attributes are passed as named parameters
and can be used and referenced in the query.

{% endtab %}

{% endtabs %}

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="heimdall" %}
