---
layout: default
title: CAS - JPA Ticket Registry
---


# JPA Ticket Registry
The JPA Ticket Registry allows CAS to store client authenticated state
data (tickets) in a database back-end such as MySQL.

<div class="alert alert-warning"><strong>Usage Warning!</strong><p>Using a RDBMS as
the back-end persistence choice for Ticket Registry state management is a fairly unnecessary and complicated
process. Ticket registries generally do not need the durability that comes with RDBMS and unless
you are already outfitted with clustered RDBMS technology and the resources to manage it,
the complexity is likely not worth the trouble. Given the proliferation of hardware virtualization
and the redundancy and vertical scaling they often provide, more suitable recommendation would be
the default in-memory ticket registry for a single node CAS deployment and distributed cache-based
registries for higher availability.</p></div>

Support is enabled by adding the following module into the Maven overlay:

```xml
<dependency>
    <groupId>org.jasig.cas</groupId>
    <artifactId>cas-server-support-jpa-ticket-registry</artifactId>
    <version>${cas.version}</version>
</dependency>
```


## Configuration

```xml
<alias name="jpaTicketRegistry" alias="ticketRegistry" />
```

The following settings are expected:

```properties
# ticketreg.database.ddl.auto=create-drop
# ticketreg.database.hibernate.dialect=org.hibernate.dialect.OracleDialect|MySQLInnoDBDialect|HSQLDialect
# ticketreg.database.hibernate.batchSize=10
# ticketreg.database.driverClass=org.hsqldb.jdbcDriver
# ticketreg.database.url=jdbc:hsqldb:mem:cas-ticket-registry
# ticketreg.database.user=sa
# ticketreg.database.password=
# ticketreg.database.pool.maxSize=18
# ticketreg.database.pool.maxWait=10000
# ticketreg.database.pool.maxWait=10000# ticketreg.database.pool.maxIdleTime=120
# ticketreg.database.jpa.locking.tgt.enabled=true
```


## TicketGrantingTicket Locking

TicketGrantingTickets are almost always updated within the same transaction they are loaded from the database in, but
after some processing delays. Because of this, the JPA Ticket Registry utilizes write locks on all loads of
TicketGrantingTickets from the database to prevent deadlocks and ensure usage meta-data consistency when a single
TicketGrantingTicket is used concurrently by multiple requests.

This reduces performance of the JPA Ticket Registry and may not be desirable or necessary for some deployments depending
the database in use, it's configured transaction isolation level, and expected concurrency of a single
TicketGrantingTicket.

The following setting can disable this locking behavior:

```properties
# ticketreg.database.jpa.locking.tgt.enabled=false
```
