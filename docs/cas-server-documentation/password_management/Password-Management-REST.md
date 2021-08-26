---
layout: default
title: CAS - Password Management
category: Password Management
---

{% include variables.html %}

# Password Management - REST

Tasks such as locating users' email and security questions as well as management
and updating of the password are delegated to user-defined rest endpoints.

REST support is enabled by including the following dependencies in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-pm-rest" %}

{% include casproperties.html properties="cas.authn.pm.rest" %}

| Endpoint                  | Method    | Headers             | Expected Response
|---------------------------|-----------|------------------------------------------------------------------------
| Get Email Address         | `GET`     | `username`          | `200`. Email address in the body.
| Get Phone Number          | `GET`     | `username`          | `200`. Phone number in the body.
| Get Security Questions    | `GET`     | `username`          | `200`. Security questions map in the body.
| Update Password           | `POST`    | `username`, `password`, `oldPassword` | `200`. `true/false` in the body.
