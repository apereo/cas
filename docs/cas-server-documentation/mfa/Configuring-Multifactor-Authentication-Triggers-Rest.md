---
layout: default
title: CAS - Multifactor Authentication Triggers
category: Multifactor Authentication
---

{% include variables.html %}

# REST - Multifactor Authentication Triggers

MFA can be triggered based on the results of a remote REST endpoint of your design. If the endpoint is configured,
CAS shall issue a `POST`, providing the authenticated username as `principalId` and `serviceId` as the service 
url in the body of the request.

Endpoints must be designed to accept/process `application/json`. The body of the response in 
the event of a successful `200` status code is expected to be the MFA provider id which CAS should activate.

{% include_cached casproperties.html properties="cas.authn.mfa.triggers.rest" %}
