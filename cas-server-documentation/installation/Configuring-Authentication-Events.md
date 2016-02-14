---
layout: default
title: CAS - Configuring Authentication Events
---

# Authentication Events
CAS provides a facility for consuming and recording authentication events into persistent storage. This functionality is similar to the records
kept by the [Audit log](Audits.html) except that the functionality and storage format is controlled via CAS itself rather than the audit engine.
Additionally, while audit data may be used for reporting and monitoring, events stored into storage via this functionality may later be assessed
in a historical fashion to assess authentication requests, evaluate risk associated with them and take further action upon them. Events are primarily
designed to be consumed by the developer and subsequent CAS modules, while audit data is targeted at deployers for end-user functionality and reporting.

By default, no events are recorded by this functionality.

## Configuration
The following storage backends are available for consumption of events:

### MongoDb
Stores authentication events into a MongoDb NoSQL database.

```xml
<dependency>
  <groupId>org.jasig.cas</groupId>
  <artifactId>cas-server-core-events-mongo</artifactId>
  <version>${cas.version}</version>
</dependency>
```

Configuration consists of:

```properties
# mongodb.events.clienturi=mongodb://uri
# mongodb.events.dropcollection=false
# mongodb.events.collection=collectionName
```

### JPA
Stores authentication events into a RDBMS.

```xml
<dependency>
  <groupId>org.jasig.cas</groupId>
  <artifactId>cas-server-core-events-jpa</artifactId>
  <version>${cas.version}</version>
</dependency>
```

Configuration consists of:

```properties
# events.jpa.database.dialect=org.hibernate.dialect.HSQLDialect
# events.jpa.database.ddl.auto=create-drop
# events.jpa.database.batchSize=1
# events.jpa.database.driverClass=org.hsqldb.jdbcDriver
# events.jpa.database.url=jdbc:hsqldb:mem:cas-events
# events.jpa.database.user=sa
# events.jpa.database.password=
# events.jpa.database.pool.minSize=6
# events.jpa.database.pool.minSize=6
# events.jpa.database.pool.maxSize=18
# events.jpa.database.pool.maxIdleTime=1000
# events.jpa.database.pool.maxWait=2000
# events.jpa.database.pool.acquireIncrement=16
# events.jpa.database.pool.acquireRetryAttempts=5
# events.jpa.database.pool.acquireRetryDelay=2000
# events.jpa.database.pool.idleConnectionTestPeriod=30
# events.jpa.database.pool.connectionHealthQuery=select 1
```
