---
layout: default
title: CAS - Configuring SSO Sessions
category: SSO & SLO
---
{% include variables.html %}

# SSO Sessions

The SSO cookie, also known as the ticket-granting cookie (`TGC`) is an HTTP cookie set by CAS upon the establishment of a single sign-on session. 
To learn more about this cookie, please [visit this guide](Configuring-SSO-Cookie.html).

{% include_cached casproperties.html properties="cas.sso" %}

Remember that the creation of an SSO session is entirely separate and has nothing to do with the authentication protocol used to establish
said session. Regardless of the type exchange between the client application and the CAS server, an SSO session will be created, used,
maintained and shared between all application types that are integrated with CAS, regardless of their type or requested protocol.

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="ssoSessions,sso" casModule="cas-server-support-reports" %}
