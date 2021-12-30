---
layout: default
title: CAS - Configuration Server
category: Configuration
---

{% include variables.html %}

# Spring Cloud Configuration Server - Spring Cloud Amazon Systems Manager Parameter Store (SSM)

Spring Cloud Configuration Server is able to use [AWS Systems Manager Parameter Store](https://docs.aws.amazon.com/systems-manager/latest/userguide/systems-manager-parameter-store.html)
to locate properties and settings.

Support is provided via the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-configuration-cloud-aws-ssm" %}

<div class="alert alert-info mt-3"><strong>Usage</strong><p>The configuration modules provide here may also be used verbatim inside a CAS server overlay and do not exclusively belong to a Spring Cloud Configuration server. While this module is primarily useful when inside the Spring Cloud Configuration server, it nonetheless may also be used inside a CAS server overlay directly to fetch settings from a source.</p></div>

{% include_cached casproperties.html properties="cas.spring.cloud.aws.ssm" %}

