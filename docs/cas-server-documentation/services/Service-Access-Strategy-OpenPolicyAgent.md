---
layout: default
title: CAS - Configuring Service Access Strategy
category: Services
---

{% include variables.html %}

# Service Access Strategy - Open Policy Agent (OPA)

The [Open Policy Agent](https://www.openpolicyagent.org/) is an open source, 
general-purpose policy engine that enables unified, fine-grained and 
context-aware policy enforcement across the entire stack. Policies are expressed in a high-level, 
declarative language with a given context that promotes safe, performant, fine-grained controls.

This access strategy builds an authorization request and submits it to OPA via a `POST`. The specifics
of the authorization request are taught to CAS using the settings typically defined within the access strategy itself:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+.example.org",
  "name" : "test",
  "id" : 1,
  "accessStrategy" : {
    "@class": "org.apereo.cas.services.OpenPolicyAgentRegisteredServiceAccessStrategy",
    "apiUrl": "http://localhost:8080",
    "decision": "example/authz/allow",
    "token": "92d4a401q26o0",
    "context" : {
      "@class" : "java.util.TreeMap",
      "param1" : "value1"
    }
  }
}
```

The following fields are available to this access strategy:

| Field      | Purpose                                                                            |
|------------|------------------------------------------------------------------------------------|
| `apiUrl`   | <sup>[1]</sup> The OPA endpoint URL.                                               |
| `decision` | The name of the policy decision defined in OPA.                                    |
| `token`    | <sup>[1]</sup> The bearer token to use in the `Authorization` header, if required. |
| `context`  | Custom context to carry data to assist with the policy decision making.            |
  
<sub><i>[1] This field supports the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax.</i></sub>
                       
The authorization request body under the `input` parameter matches the following structure:

```json
{
  "input": {
    "principal": "casuser",
    "service": "https://myapp.example.com",
    "attributes": {
      "email": ["user@example.org"]
    },
    "context": { "parameter1": "value1" }
  }
}
```

OPA returns an HTTP `200` response code if the policy was evaluated successfully. 
Non-HTTP `200` response codes indicate configuration or runtime errors. The policy 
decision outcome is contained in the `result` key of the response message body:

```json
{
  "result": true
}
```
