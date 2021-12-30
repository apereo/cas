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

<div class="alert alert-info mt-3"><strong>Usage</strong><p>The configuration modules provide here may also be used verbatim inside a CAS server overlay and do not exclusively belong to a Spring Cloud Configuration server. While this module is primarily useful when inside the Spring Cloud Configuration server, it nonetheless may also be used inside a CAS server overlay directly to fetch settings from a source.</p></div>

The REST endpoint is expected to produce a `Map` in the payload with keys as the setting names
and values as the setting value.

{% include_cached casproperties.html properties="cas.spring.cloud.rest" %}
