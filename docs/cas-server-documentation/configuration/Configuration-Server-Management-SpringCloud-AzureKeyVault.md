---
layout: default
title: CAS - Configuration Server
category: Configuration
---

{% include variables.html %}

# Spring Cloud Configuration Server - Spring Cloud Azure KeyVault

Spring Cloud Configuration Server is able to use Microsoft Azure's KeyVault Secrets to locate
properties and settings. Support is provided via the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-configuration-cloud-azure-keyvault" %}

**IMPORTANT**: The allowed  name pattern in Azure Key Vault is `^[0-9a-zA-Z-]+$`. For properties that contain
that contain `.` in the name (i.e. `cas.some.property`),  replace `.` with `-` when
you store the setting in Azure Key Vault (i.e. `cas-some-property`).
The module will handle the transformation for you.

<div class="alert alert-info mt-3"><strong>Usage</strong><p>The configuration modules provide here may also be used verbatim inside a CAS server overlay and do not exclusively belong to a Spring Cloud Configuration server. While this module is primarily useful when inside the Spring Cloud Configuration server, it nonetheless may also be used inside a CAS server overlay directly to fetch settings from a source.</p></div>

{% include_cached casproperties.html thirdPartyStartsWith="azure.keyvault" %}
