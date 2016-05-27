---
layout: default
title: CAS - JPA Service Registry
---

# JPA Service Registry
Stores registered service data in a database.

Support is enabled by adding the following module into the Maven overlay:

```xml
<dependency>
    <groupId>org.apereo.cas</groupId>
    <artifactId>cas-server-support-jpa-service-registry</artifactId>
    <version>${cas.version}</version>
</dependency>
```

The following settings are expected:

```properties
# svcreg.database.ddl.auto=create-drop
# svcreg.database.hibernate.dialect=org.hibernate.dialect.OracleDialect|MySQLInnoDBDialect|HSQLDialect
# svcreg.database.hibernate.batchSize=10
# svcreg.database.driverClass=org.hsqldb.jdbcDriver
# svcreg.database.url=jdbc:hsqldb:mem:cas-ticket-registry
# svcreg.database.user=sa
# svcreg.database.password=
# svcreg.database.pool.maxSize=18
# svcreg.database.pool.maxWait=10000
# svcreg.database.pool.maxIdleTime=120
# svcreg.database.pool.maxWait=3000
# svcreg.database.idle.timeout=3000
# svcreg.database.leak.threshold=10
# svcreg.database.fail.fast=true
# svcreg.database.isolate.internal.queries=false
# svcreg.database.health.query=select 1
# svcreg.database.pool.suspension=false
# svcreg.database.autocommit=false
```


## Configuration

In `application.properties`:

```properties
#CAS components mappings
serviceRegistryDao=jpaServiceRegistryDao
```

## Auto Initialization

Upon startup and if the services registry database is blank, 
the registry is able to auto initialize itself from default 
JSON service definitions available to CAS. This behavior can be controlled via:

```properties
# svcreg.database.from.json=false
```
