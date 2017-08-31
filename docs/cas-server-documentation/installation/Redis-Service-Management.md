---
layout: default
title: CAS - Redis Service Registry
---

# Redis Service Registry

This service registry stores tickets in one or more [Redis](http://redis.io/) instances. The
[spring data redis](http://projects.spring.io/spring-data-redis/) library used by this component presents Redis as a
key/value store that accepts `String` keys and CAS service definition objects as values. The key is started with `CAS_SERVICE:`.

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-redis-service-registry</artifactId>
    <version>${cas.version}</version>
</dependency>
```

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#redis-service-registry).

## Auto Initialization

Upon startup and configuration permitting, the registry is able to auto initialize itself from default JSON service definitions available to CAS.

To see the relevant list of CAS properties, please [review this guide](Configuration-Properties.html#service-registry).
