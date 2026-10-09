---
layout: default
title: CAS - Heimdall - Authorization Requests
description: "How to send an authorization request to Heimdall, with the request payload, the responses and the AuthZEN evaluation API."
category: Authorization
---

{% include variables.html %}

# Authorization Requests - Heimdall

Policy enforcement points ask Heimdall for a decision by posting an authorization request, either in the Heimdall format to `/heimdall/authorize` or as an AuthZEN evaluation. This page describes both payloads and the responses Heimdall returns.

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
`"decision": false` and a [decision context](Heimdall-Authorization-AuthZEN.html#decision-context). If the request carries an `X-Request-ID` header, the same value is returned in the response.

Note that `resource.id` identifies the resource instance being accessed, such as a specific account or document,
and is not the name of a policy namespace. AuthZEN requests are matched against authorizable resources in *every* namespace
using their `resourceType`, `actions` and optional `resourceIdPattern` fields; the URI pattern, method and namespace fields
are ignored for AuthZEN requests. Likewise, the `/heimdall/authorize` endpoint rejects requests that carry AuthZEN `subject`,
`resource` or `action` fields with a `400` status code.

See [AuthZEN](Heimdall-Authorization-AuthZEN.html) for the [decision context](Heimdall-Authorization-AuthZEN.html#decision-context), [access evaluations](Heimdall-Authorization-AuthZEN.html#access-evaluations)
and [policy decision point metadata](Heimdall-Authorization-AuthZEN.html#policy-decision-point-metadata).

{% endtab %}

{% endtabs %}
