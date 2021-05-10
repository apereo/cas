---
layout: default
title: CAS - Surrogate Authentication
category: Authentication
---
{% include variables.html %}


# REST Surrogate Authentication Registration

REST support for surrogate authentication is enabled by including the following dependencies in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-surrogate-authentication-rest" %}

| Method       | Description                                                   | Parameter(s)             | Response
|--------------|---------------------------------------------------------------|--------------------------|-----------
| `GET`        | Whether principal can authenticate as a surrogate account.    | `surrogate`, `principal` | `202`
| `GET`        | List of accounts principal is eligible to impersonate.        | `principal` | JSON list of usernames.

{% include casproperties.html properties="cas.authn.surrogate.rest" %}
