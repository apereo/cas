---
layout: default
title: CAS - Redis Service Registry
category: Services
---

{% include variables.html %}

# Redis Service Registry

This service registry stores tickets in one or more [Redis](http://redis.io/) instances. The
[spring data redis](http://projects.spring.io/spring-data-redis/) library used by this component presents Redis as a
key/value store that accepts `String` keys and CAS service definition objects as values. The key is started with `CAS_SERVICE:`.

The Redis service registry supports Redis Sentinel, which provides high availability for Redis. In practical terms this means that using Sentinel you can create a Redis deployment that resists without human intervention to certain kind of failures. Redis Sentinel also provides other collateral tasks such as monitoring, notifications and acts as a configuration provider for clients.

Support is enabled by including the following dependency in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-redis-service-registry" %}

{% include_cached casproperties.html properties="cas.service-registry.redis" %}

## Auto Initialization

Upon startup and configuration permitting, the registry is able to auto initialize itself from default JSON service definitions available to CAS. See [this guide](AutoInitialization-Service-Management.html) for more info.
