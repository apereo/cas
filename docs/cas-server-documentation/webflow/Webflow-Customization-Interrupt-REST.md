---
layout: default
title: CAS - Authentication Interrupt
category: Webflow Management
---

{% include variables.html %}

# REST Authentication Interrupt

This strategy reaches out to a REST endpoint resource whose job is to 
dynamically calculate whether the authentication flow should be interrupted given the following parameters:

| Parameter             | Description
|-------------------------------------------------------------------------------------------------------
| `username`            | Authenticated principal id.
| `service`             | The identifier (URL) for the requesting application.
| `registeredService`   | The identifier of the registered service matched and found in the registry. 

On a successful operation with a status code of `200`, the response body is 
expected to contain the JSON payload whose syntax and structure is identical to what is described above.

{% include casproperties.html properties="cas.interrupt.rest" %}
