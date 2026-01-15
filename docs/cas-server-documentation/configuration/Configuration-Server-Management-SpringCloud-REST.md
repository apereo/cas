---
layout: default
title: CAS - Configuration Server
category: Configuration
---

{% include variables.html %}

# Spring Cloud Configuration Server - Spring Cloud REST

Spring Cloud Configuration Server is able to locate properties and settings using a REST API.

Support is provided via the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-configuration-cloud-rest" %}

<div class="alert alert-info mt-3">:information_source: <strong>Usage</strong><p>The configuration 
modules provided here may also be used verbatim inside a CAS server overlay and do not exclusively 
belong to a Spring Cloud Configuration server. While this module is primarily useful when inside the
Spring Cloud Configuration server, it nonetheless may also be used inside a CAS server overlay 
directly to fetch settings from a source.</p></div>

<div class="alert alert-info mt-3">:information_source: <strong>Usage</strong>
<p>This capability supports dynamic configuration updates at runtime.</p></div>


{% include_cached casproperties.html properties="cas.spring.cloud.rest" %}

## REST API
  
The REST endpoints expected by this capability are as follows:

| Endpoint | Method | Description                                                                                                                                                                            |
|----------|--------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `/`      | `GET`  | Fetch a specific property requested by the `name` query parameter. Produce `200` status code with the property value in the response body, or `4xx` for unknown/unaailable properties. |
| `/names` | `GET`  | Produce a list of all supported and available property names.                                                                                                                          |
| `/`      | `POST` | Receive a `name` and `value` in the requesty body to update a property value.                                                                                                          |

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="casConfig" casModule="cas-server-support-reports" %}
