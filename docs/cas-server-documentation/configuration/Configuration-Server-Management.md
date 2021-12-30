---
layout: default
title: CAS - Configuration Server
category: Configuration
---

{% include variables.html %}

# Configuration Server

As your CAS deployment moves through the deployment pipeline from dev to test and into production
you can manage the configuration between those environments and be certain that applications
have everything they need to run when they migrate through the use of an external configuration server
provided by the [Spring Cloud](https://github.com/spring-cloud/spring-cloud-config) project. As an alternative,
you may decide to run CAS in a standalone mode removing the need for external configuration server deployment,
though at the cost of losing features and capabilities relevant for a cloud deployment.

## Configuration Strategies

The CAS server web application responds to the following strategies that dictate how settings should be consumed.

| Strategy                                   | Description                                                                  |
|--------------------------------------------|------------------------------------------------------------------------------|
| [Standalone](Configuration-Server-Management-Standalone.html) | Default strategy.                                              |
| [Spring Cloud](Configuration-Server-Management-SpringCloud.html) | Externalized strategy using Spring Cloud configuration server. |

## Configuration Security

To learn how sensitive CAS settings can be secured 
via encryption, [please review this guide](Configuration-Properties-Security.html).

## Configuration Reloadability

To lean more about how CAS allows you to reload configuration changes,
please [review this guide](Configuration-Management-Reload.html).

## Clustered Deployments

CAS uses the [Spring Cloud Bus](http://cloud.spring.io/spring-cloud-static/spring-cloud.html)
to manage configuration in a distributed deployment. Spring Cloud Bus links nodes of a
distributed system with a lightweight message broker. This can then be used to broadcast state
changes (e.g. configuration changes) or other management instructions.

To learn how sensitive CAS settings can be secured via 
encryption, [please review this guide](Configuration-Management-Clustered.html).
