---
layout: default
title: CAS - Mongo Service Registry
---

# Mongo Service Registry
This DAO uses a [MongoDb](https://www.mongodb.org/) instance to load and persist service definitions.
Support is enabled by adding the following module into the Maven overlay:

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-mongo-service-registry</artifactId>
    <version>${cas.version}</version>
</dependency>
```

## Configuration
This implementation auto-configures most of the internal details.

Enable the registry in `application.properties` via:

```properties
#CAS components mappings
serviceRegistryDao=mongoServiceRegistryDao
```


The following configuration in `application.properties` is required.

```properties
# mongodb.host=mongodb database url
# mongodb.port=mongodb database port
# mongodb.userId=mongodb userid to bind
# mongodb.userPassword=mongodb password to bind
# cas.service.registry.mongo.db=Collection name to store service definitions
```

## Auto Initialization

Upon startup and if the services registry database is blank, 
the registry is able to auto initialize itself from default 
JSON service definitions available to CAS. This behavior can be controlled via:

```properties
# svcreg.database.from.json=false
```

