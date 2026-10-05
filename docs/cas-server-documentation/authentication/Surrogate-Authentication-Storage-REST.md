---
layout: default
title: CAS - REST Surrogate Authentication
category: Authentication
---
{% include variables.html %}


# REST Surrogate Authentication

Decide who may impersonate whom by calling a REST API that answers whether a user may log in as another account and lists the accounts allowed.

REST support for surrogate authentication is enabled by including the following dependencies in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-surrogate-authentication-rest" %}

| Method | Description                                                | Parameter(s)             | Response                |
|--------|------------------------------------------------------------|--------------------------|-------------------------|
| `GET`  | Whether principal can authenticate as a surrogate account. | `surrogate`, `principal` | `202`                   |
| `GET`  | List of accounts principal is eligible to impersonate.     | `principal`              | JSON list of usernames. |

{% include_cached casproperties.html properties="cas.authn.surrogate.rest" %}
