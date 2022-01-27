---
layout: default
title: CAS - Consul Service Discovery
category: High Availability
---
{% include variables.html %}


# Consul Server Discovery Service

[HashiCorp Consul](https://www.consul.io) has multiple components, but as a whole, it is a tool for discovering and configuring services in your infrastructure. It provides key features:

- **Service Discovery**: Clients of Consul can provide a service, such as api or mysql, and other clients can use Consul to discover providers of a given service. Using either DNS or HTTP, applications can easily find the services they depend upon.

- **Health Checking**: Consul clients can provide any number of health checks, either associated with a given service ("is the webserver returning 200 OK"), or with the local node ("is memory utilization below 90%"). This information can be used by an operator to monitor cluster health, and it is used by the service discovery components to route traffic away from unhealthy hosts.

- **KV Store**: Applications can make use of Consul's hierarchical key/value store for any number of purposes, including dynamic configuration, feature flagging, coordination, leader election, and more. The simple HTTP API makes it easy to use.

- **Multi Datacenter**: Consul supports multiple datacenters out of the box. This means users of Consul do not have to worry about building additional layers of abstraction to grow to multiple regions.

CAS provides a Consul-enabled service discovery server that is based on [Spring Cloud Consul](https://cloud.spring.io/spring-cloud-consul/) and bootstrapped via [Spring Cloud](http://cloud.spring.io/spring-cloud-static/spring-cloud.html).

### Installation

- To run the Consul discovery server, please [see this guide](https://www.consul.io/) for installation instructions. A simple Consul installation may be run as `consul agent -dev -ui`
- Look for a suitable and relevant ready-made Docker image via `docker search consul`.

When deployed and assuming default settings, the Consul dashboard would be available at: `http://localhost:8500/ui`.

Note that a Consul Agent client must be available to all CAS server nodes. By default, the agent client is expected to be at `localhost:8500`. See the [Agent documentation](https://www.consul.io/docs/agent) for specifics on how to start an Agent client.

## CAS Discovery Service Clients

Each individual CAS server is given the ability to auto-register itself with the discovery server, provided configuration is made available to instruct the CAS server how to locate and connect to the discover server service.

Support is added by including the following dependency in the WAR overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-consul-client" %}

{% include_cached casproperties.html thirdParty="spring.cloud.consul,spring.cloud.config.discovery" %}

### Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
<Logger name="org.springframework.cloud.consul" level="debug" additivity="false">
  <AppenderRef ref="casConsole"/>
  <AppenderRef ref="casFile"/>
</Logger>
```
