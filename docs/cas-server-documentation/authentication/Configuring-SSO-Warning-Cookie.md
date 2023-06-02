---
layout: default
title: CAS - Configuring SSO Sessions
category: SSO & SLO
---
{% include variables.html %}

# SSO Warning Session Cookie

A warning cookie set by CAS upon the establishment of the SSO session at the request of the user on the CAS login page.
The cookie is used later to warn and prompt the user before a service ticket is generated and access to the service application is granted.

{% include_cached casproperties.html properties="cas.warning-cookie" %}
