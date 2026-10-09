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

## Topics

- [Authorization Requests](Heimdall-Authorization-Requests.html): the payloads policy enforcement points send and the responses they get.
- [AuthZEN](Heimdall-Authorization-AuthZEN.html): decision context, access evaluations and policy decision point metadata.
- [Authorization Principal](Heimdall-Authorization-Principal.html): how Heimdall identifies the caller.
- [Authorization Resources](Heimdall-Authorization-Resources.html): registering the resources Heimdall protects.
- [Authorization Policies](Heimdall-Authorization-Policies.html): the rules that grant or deny access.
- [Gateway Integration](Heimdall-Authorization-Gateway.html): enforcing decisions with an nginx reverse proxy.

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="heimdall" %}
