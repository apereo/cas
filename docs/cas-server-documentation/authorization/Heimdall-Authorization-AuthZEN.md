---
layout: default
title: CAS - Heimdall - AuthZEN
description: "AuthZEN support in Heimdall: decision context, access evaluations, search and policy decision point metadata."
category: Authorization
---

{% include variables.html %}

# AuthZEN - Heimdall

Heimdall implements the OpenID [AuthZEN Authorization API](https://openid.net/specs/authorization-api-1_0.html), so any AuthZEN policy enforcement point can use CAS as its policy decision point. This page covers the parts specific to AuthZEN.

The following capabilities are specific to the [AuthZEN](https://openid.net/specs/authorization-api-1_0.html) protocol.

## Decision Context

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

## Access Evaluations

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

## Search

The optional AuthZEN search APIs for subjects, resources and actions are not supported yet, and may be worked out in the future.
Most Heimdall policies evaluate a single request, such as attribute, REST, JDBC or Groovy policies, rather than store the relationships
a search would enumerate.

## Policy Decision Point Metadata

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
