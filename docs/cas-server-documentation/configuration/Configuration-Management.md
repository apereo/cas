---
layout: default
title: CAS - Configuration Management
category: Configuration
---

{% include variables.html %}

# Configuration Management

The core foundations of CAS that deal with configuration management, settings and replication of changes
across multiple CAS nodes are all entirely handled automatically via the
[Spring Cloud](https://github.com/spring-cloud/spring-cloud-config) project. The strategies listed below
present a very flexible and powerful way to manage CAS configuration for production deployments, by
allowing the CAS adopter to **ONLY** keep track of settings required for their specific deployment concerns
and leaving all else behind to be handled by the default CAS configuration.

The following strategies may be used to fully extend the CAS configuration model.

<div class="alert alert-info"><strong>YAML or Properties?</strong><p>CAS configuration allows for both
YAML and Properties syntax in any of the below strategies used. It generally does not matter which syntax 
is used, but when working with Unicode strings as properties values it does matter. Spring loads properties
files using the `ISO-8859-1` encoding. YAML files are loaded with UTF-8 encoding. If you are setting Unicode
values try using a YAML configuration file.</p></div>

{% include casproperties.html properties="cas.server.name,cas.server.prefix,cas.server.scope,cas.host." %}

On startup, CAS will display a banner along with some diagnostics info.
In order to skip this step and summarize, set the system property `-DCAS_BANNER_SKIP=true`.

On startup, CAS will perform many tasks related to the application lifecycle, the beans lifecycle
or even processing application events. Such events can be tracked at startup and collected
for profiling purposes to have a better understanding of the application startup process.
Startup event tracking can be controlled using a system property `-DCAS_APP_STARTUP`
that can be assigned the following values:

| Type                 | Description
|----------------------|-------------------------------------------------------------------------------------------------------
| `default`            | Default startup type which acts as a no-op.
| `buffering`          | Record events into memory using a pre-defined capacity and expose them via the `startup` [actuator endpoint](../monitoring/Monitoring-Statistics.html).
| `jfr`                | Add startup events to a Java Flight Recorder session for profiling applications and correlating their Spring context lifecycle.

## Overview

CAS allows you to externalize your configuration so you can work with the same CAS instance in
different environments. You can use properties files, YAML files, environment variables and
command-line arguments to externalize configuration.

CAS uses a very particular order that is designed to allow 
sensible overriding of values. Properties passed to the CAS web application 
are considered in the following order:

1. Command line arguments, starting with `--` (e.g. `--server.port=9000`)
2. Properties from `SPRING_APPLICATION_JSON` (inline JSON embedded in an environment variable/system property)
3. JNDI attributes from `java:comp/env`.
4. Java System properties.
5. OS environment variables.
6. Configuration files (i.e. `application.properties|yml`) indicated by the [configuration server](#configuration-server) and profile.

<div class="alert alert-info"><strong>Managing Configuration</strong><p>In order to manage
the CAS configuration, you should configure access
to <a href="../monitoring/Monitoring-Statistics.html">CAS administration panels.</a></p></div>

## Actuator Endpoints

The following endpoints are provided:

{% include actuators.html endpoints="configProps,env,beans,conditions" %}

## Configuration Server

CAS provides a built-in configuration server that is responsible for bootstrapping the configuration
environment and loading of externalized settings in a distributed system. You may have a central
place to manage external properties for CAS nodes across all environments. To learn 
more about how to manage the CAS configuration, please [review this guide](Configuration-Server-Management.html).

## Extending CAS Configuration

To learn more about how to extend and customize the CAS 
configuration, please [review this guide](Configuration-Management-Extensions.html).

## Custom CAS Settings

{% include casproperties.html properties="cas.custom.properties" %}

## Auto Configuration Strategy

To see the relevant list of CAS properties for this
feature, please [review this guide](Configuration-Storage.html).

Note that CAS in most if not all cases will attempt to auto-configure the context based on the declaration
and presence of feature-specific dedicated modules. This generally SHOULD relieve the deployer
from manually massaging the application context via XML configuration files.

The idea is twofold:

- Declare your intention for a given CAS feature by declaring the appropriate module in your overlay.
- Optionally, configure the appropriate properties and settings.

CAS will automatically take care of injecting appropriate beans and other components into the runtime application context,
Depending on the presence of a module and/or its settings configured by the deployer.

<div class="alert alert-info"><strong>No XML</strong><p>Again, the entire point of
the auto-configuration strategy is ensure deployers aren't swimming in a sea of XML files
configuring beans and such. CAS should take care of it all. If you find an instance where
this claim does not hold, consider that a "bug" and file a feature request.</p></div>


