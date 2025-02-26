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

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-pm-rest" %}

{% include_cached casproperties.html properties="cas.authn.pm.rest" %}

| Endpoint                  | Method | Query Parameter(s) | Request Body                          | Expected Response                          |
|---------------------------|--------|--------------------|---------------------------------------|--------------------------------------------|
| Get Email Address         | `GET`  | `username`         | None                                  | `200`. Email address in the body.          |
| Get Phone Number          | `GET`  | `username`         | None                                  | `200`. Phone number in the body.           |
| Get Security Questions    | `GET`  | `username`         | None                                  | `200`. Security questions map in the body. |
| Update Security Questions | `POST` | `username`         | Security questions map in the body.   | `200`. `true/false` in the body.           |
| Update Password           | `POST` | None               | `username`, `password`, `oldPassword` | `200`. `true/false` in the body.           |
| Unlock Account            | `POST` | `username`         | None                                  | `200`. `true/false` in the body.           |
          
By default, all requests are submitted using the `Accept` header `application/json`, `Content-Type` header set to `application/json`
and `Accept-Charset` header set to `UTF-8`.
