---
layout: default
title: CAS - JPA Service Registry
---

# JPA Service Registry
Stores registered service data in a database.

Support is enabled by adding the following module into the Maven overlay:

```xml
<dependency>
    <groupId>org.jasig.cas</groupId>
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
```


## Configuration

The following configuration assumes a `dataSource` bean is defined in the context.

In `cas.properties`:

```properties
#CAS components mappings
serviceRegistryDao=jpaServiceRegistryDao
```
