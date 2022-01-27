---
layout: default
title: CAS - Configuration Server
category: Configuration
---

{% include variables.html %}

# Spring Cloud Configuration Server - Spring Cloud HashiCorp Consul

Spring Cloud Configuration Server is able to use [Consul](https://www.consul.io/) to
locate properties and settings.

Consul provides a [Key/Value Store](https://www.consul.io/api/kv) for storing configuration and other metadata. Configuration is loaded into the CAS environment during the special "bootstrap" phase at runtime. Configuration is stored in the `/config` folder by default. Multiple `PropertySource` instances are created based on the application’s name and the active profiles that mimics
the Spring Cloud Config order of resolving properties. For example, an application with the name `cas` and with the `dev` profile will have the following property sources created:

```bash
config/cas,dev/
config/cas/
config/application,dev/
config/application/
```

The most specific property source is at the top, with the least specific at the bottom. Properties in the `config/application` folder are applicable to all applications using consul for configuration. Properties in the `config/cas` folder are only available to the instances of the service named `cas`.

Configuration is currently read on startup of the application. Sending a HTTP POST to `/refresh` will cause the configuration to be reloaded. Watching the key value store (which Consul supports) is not currently possible, but will be a future addition to this project.

The Consul Config Watch takes advantage of the ability of consul to [watch a key prefix](https://www.consul.io/docs/agent). The Config Watch makes a blocking Consul HTTP API call to determine if any relevant configuration data has changed for the current application. If there is new configuration data a `Refresh Event` is published. This is equivalent to calling the `/refresh` Spring Boot actuator endpoint.

{% include_cached casproperties.html
thirdPartyStartsWith="spring.cloud.consul.config"
%}

<div class="alert alert-info mt-3"><strong>Usage</strong><p>The configuration modules provide here may also be used verbatim inside a CAS server overlay and do not exclusively belong to a Spring Cloud Configuration server. While this module is primarily useful when inside the Spring Cloud Configuration server, it nonetheless may also be used inside a CAS server overlay directly to fetch settings from a source.</p></div>

The following endpoints are provided by Spring Cloud:

{% include_cached actuators.html endpoints="consul" %}
