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

### Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="authenticationPolicies" casModule="cas-server-support-reports" %}

## Policies

Authentication policies can be managed via the following strategies.

| Storage          | Description                                         
|-----------------------------------------------------------
| All              | [See this guide](Configuring-Authentication-Policy-All.html).
| Any              | [See this guide](Configuring-Authentication-Policy-Any.html).
| Global           | [See this guide](Configuring-Authentication-Policy-Global.html).
| Groovy           | [See this guide](Configuring-Authentication-Policy-Groovy.html).
| Not Prevented    | [See this guide](Configuring-Authentication-Policy-NotPrevented.html).
| Required         | [See this guide](Configuring-Authentication-Policy-Required.html).
| REST             | [See this guide](Configuring-Authentication-Policy-REST.html).
| Source Selection | [See this guide](Configuring-Authentication-Policy-SourceSelection.html).
| Unique Principal | [See this guide](Configuring-Authentication-Policy-UniquePrincipal.html).

Authentication policies may also be defined on a per application 
basis. See [this guide](../services/Configuring-Service-AuthN-Policy.html) for more info.
