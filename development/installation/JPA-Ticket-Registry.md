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

{% highlight xml %}
<dependency>
    <groupId>org.jasig.cas</groupId>
    <artifactId>cas-server-support-jpa-ticket-registry</artifactId>
    <version>${cas.version}</version>
</dependency>
{% endhighlight %}


# Configuration

{% highlight xml %}
<alias name="jpaTicketRegistry" alias="ticketRegistry" />
{% endhighlight %}

The following settings are expected:

{% highlight properties %}
# ticketreg.database.ddl.auto=create-drop
# ticketreg.database.hibernate.dialect=org.hibernate.dialect.OracleDialect|MySQLInnoDBDialect|HSQLDialect
# ticketreg.database.hibernate.batchSize=10
# ticketreg.database.driverClass=org.hsqldb.jdbcDriver
# ticketreg.database.url=jdbc:hsqldb:mem:cas-ticket-registry
# ticketreg.database.user=sa
# ticketreg.database.password=
# ticketreg.database.pool.minSize=6
# ticketreg.database.pool.maxSize=18
# ticketreg.database.pool.maxWait=10000
# ticketreg.database.pool.maxIdleTime=120
# ticketreg.database.pool.acquireIncrement=6
# ticketreg.database.pool.idleConnectionTestPeriod=30
# ticketreg.database.pool.connectionHealthQuery=select 1
# ticketreg.database.pool.acquireRetryAttempts=5
# ticketreg.database.pool.acquireRetryDelay=2000
# ticketreg.database.pool.connectionHealthQuery=select 1
{% endhighlight %}


##Cleaner Locking Strategy
The above shows a JPA 2.0 implementation of an exclusive, non-reentrant lock,
`JpaLockingStrategy`, to be used with the JPA-backed ticket registry.

This will configure the cleaner with the following defaults:

| Field                             | Default
|-----------------------------------+---------------------------------------+
| `tableName`                       | `LOCKS`
| `uniqueIdColumnName`              | `UNIQUE_ID`
| `applicationIdColumnName`         | `APPLICATION_ID`
| `expirationDataColumnName`        | `EXPIRATION_DATE`
| `platform`                        | SQL92
| `lockTimeout`                     | 3600 (1 hour)


# Database Configuration

## JDBC Driver
CAS must have access to the appropriate JDBC driver for the database. Once you have obtained
the appropriate driver and configured the data source, place the JAR inside the lib directory
of your web server environment (i.e. `$TOMCAT_HOME/lib`)


## Schema
If the user has sufficient privileges on start up, the database tables should be created.
The database user MUST have `CREATE/ALTER` privileges to take advantage of automatic
schema generation and schema updates.


## Deadlocks
The Hibernate SchemaExport DDL creation tool *may* fail to create two very import indices
when generating the ticket tables. The absence of these indices dramatically increases the
potential for database deadlocks under load.
If the indices were not created you should manually create them before placing your CAS
configuration into a production environment.

To review indices, you may use the following MYSQL-based sample code below:

{% highlight sql %}
show index from SERVICETICKET where column_name='ticketGrantingTicket_ID';
show index from TICKETGRANTINGTICKET where column_name='ticketGrantingTicket_ID';
{% endhighlight %}

To create indices that are missing, you may use the following sample code below:


### MYSQL
{% highlight sql %}
CREATE INDEX ST_TGT_FK_I ON SERVICETICKET (ticketGrantingTicket_ID);
CREATE INDEX ST_TGT_FK_I ON TICKETGRANTINGTICKET (ticketGrantingTicket_ID);
{% endhighlight %}


###ORACLE
{% highlight sql %}
CREATE INDEX "ST_TGT_FK_I"
  ON SERVICETICKET ("TICKETGRANTINGTICKET_ID")
  COMPUTE STATISTICS;

/** Create index on TGT self-referential foreign-key constraint */
CREATE INDEX "TGT_TGT_FK_I"
  ON TICKETGRANTINGTICKET ("TICKETGRANTINGTICKET_ID")
  COMPUTE STATISTICS;
{% endhighlight %}


## Ticket Cleanup

The use of `JpaLockingStrategy` is strongly recommended for HA environments where
multiple nodes are attempting ticket cleanup on a shared database.
`JpaLockingStrategy` can auto-generate the schema for the target platform.  
A representative schema is provided below that applies to PostgreSQL:


{% highlight sql %}
CREATE TABLE locks (
 application_id VARCHAR(50) NOT NULL,
 unique_id VARCHAR(50) NULL,
 expiration_date TIMESTAMP NULL
);

ALTER TABLE locks ADD CONSTRAINT pk_locks
 PRIMARY KEY (application_id);
{% endhighlight %}

<div class="alert alert-warning"><strong>Platform-Specific Issues</strong><p>The exact DDL to create
the LOCKS table may differ from the above. For example, on Oracle platforms
the `expiration_date` column must be of type `DAT`E.  Use the `JpaLockingStrategy`
which can create and update the schema automatically to avoid platform-specific schema issues.</p></div>
