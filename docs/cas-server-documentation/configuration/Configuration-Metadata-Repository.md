---
layout: default
title: CAS Configuration Metadata Repository
category: Configuration
---

{% include variables.html %}

# Configuration Metadata

CAS ships with meta-data files that provide details of all supported configuration properties and settings. The repository of all configuration metadata
is generated automatically at build and release time by processing all items annotated with `@ConfigurationProperties` within the codebase. This repository
is then made available for additional querying and filtering to look up definitions of a given property or locate relevant settings 
that apply to a particular group of functionality in CAS, such as LDAP authentication.

Support is enabled by including the following dependency in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-core-configuration-metadata-repository" %}

Configuration metadata may also be accessed and queried using the
CAS actuator endpoints. [See this guide](../monitoring/Monitoring-Statistics.html) to learn more.

## Actuator Endpoints
      
The following endpoints are provided by CAS:

{% include actuators.html endpoints="configurationMetadata,casModules" %}

## Metadata via Commandline

The metadata repository can also be examined using the command-line as a separate utility. 
[See this guide](../installation/Configuring-Commandline-Shell.html) for more info.
