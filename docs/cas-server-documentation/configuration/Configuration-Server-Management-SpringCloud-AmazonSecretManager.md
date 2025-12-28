---
layout: default
title: CAS - Configuration Server
category: Configuration
---

{% include variables.html %}

# Spring Cloud Configuration Server - Spring Cloud Amazon Secret Manager

Spring Cloud Configuration Server is able to use [Amazon Secret Manager](https://aws.amazon.com/secrets-manager/) to locate properties and settings.

Support is provided via the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-configuration-cloud-aws-secretsmanager" %}

<div class="alert alert-info mt-3">:information_source: <strong>Usage</strong><p>The configuration modules provided here may also be used verbatim inside a CAS server overlay and do not exclusively belong to a Spring Cloud Configuration server. While this module is primarily useful when inside the Spring Cloud Configuration server, it nonetheless may also be used inside a CAS server overlay directly to fetch settings from a source.</p></div>

<div class="alert alert-info mt-3">:information_source: <strong>Usage</strong>
<p>This capability supports dynamic configuration updates at runtime.</p></div>

{% include_cached casproperties.html properties="cas.spring.cloud.aws.secrets-manager" %}

## Actuator Endpoints

The following endpoints are provided by CAS:

{% include_cached actuators.html endpoints="casConfig" casModule="cas-server-support-reports" %}
