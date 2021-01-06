---
layout: default
title: CAS - Multifactor Authentication Triggers
category: Multifactor Authentication
---

{% include variables.html %}

# Adaptive - Multifactor Authentication Triggers

MFA can be triggered based on the specific nature of a request that may be considered outlawed. For instance,
you may want all requests that are submitted from a specific IP pattern, or from a particular geographical location
to be forced to go through MFA. CAS is able to adapt itself to various properties of the incoming request
and will route the flow to execute MFA. See [this guide](Configuring-Adaptive-Authentication.html) for more info.
