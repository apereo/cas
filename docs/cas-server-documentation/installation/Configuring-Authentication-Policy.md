---
layout: default
title: CAS - Configuring Authentication Policy
category: Authentication
---
{% include variables.html %}


# Authentication Policy

CAS presents a number of strategies for handling authentication 
security policies. Policies in general control the following:

1. Should the authentication chain be stopped after a certain kind of authentication failure?
2. Given multiple authentication handlers in a chain, what constitutes a successful authentication event?

Policies are typically activated after:

1. An authentication failure has occurred.
2. The authentication chain has finished execution.

Typical use cases of authentication policies may include:

1. Enforce a specific authentication's successful execution, for the entire authentication event to be considered successful.
2. Ensure a specific class of failure is not evident in the authentication chain's execution log.
3. Ensure that all authentication schemes in the chain are executed successfully, for the entire authentication event to be considered successful.

{% include {{ version }}/authentication-policy-configuration.md %}

### Administrative Endpoints

The following endpoints are provided by CAS:
 
| Endpoint                  | Description
|---------------------------|------------------------------------------------
| `authenticationPolicies`  | A `GET` request presents the collection of registered authentication policies. An individual authentication policy can be queried via `GET` by its name using a selector path (i.e. `authenticationPolicies/{name}`).
