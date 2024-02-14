---
layout: default
title: CAS - JDBC Drivers
category: Configuration
---
{% include variables.html %}

# JDBC Drivers

While in most cases this is unnecessary and handled by CAS automatically,
you may need to also include the following module to account for various database drivers:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-jdbc-drivers" %}

## JPA Implementations
                     
The following JPA implementations are provided by CAS. In most cases, the indicated modules do not need
to be included in a CAS build and the implementation would be automatically chosen by the feature module.
The details below are listed for reference and may be of use in advanced scenarios where development may be required.

### Hibernate

[Hibernate ORM](https://hibernate.org/) is an object–relational mapping tool for the Java programming 
language. It provides a framework for mapping an object-oriented domain model to a relational database.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-jpa-hibernate" %}

### EclipseLink

[EclipseLink](https://projects.eclipse.org/projects/ee4j.eclipselink) is the open source Eclipse Persistence Services Project from the 
Eclipse Foundation. The software provides an extensible framework that allows 
Java developers to interact with various data services, including databases, 
web services, Object XML mapping, and enterprise information systems.

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-jpa-eclipselink" %}

## Database Support

Automatic support for drivers includes the following databases.
All other drivers need to be manually added to the build configuration.

{% include_cached {{ version }}/hibernate-configuration.md %}

### H2
    
Available drivers are:

1. `org.h2.Driver`

| Dialects                          |
|-----------------------------------|
| `org.hibernate.dialect.H2Dialect` |

### HSQLDB

Available drivers are:

1. `org.hsqldb.jdbcDriver`

| Dialects                            |
|-------------------------------------|
| `org.hibernate.dialect.HSQLDialect` |

### Oracle

Available drivers are:

1. `oracle.jdbc.driver.OracleDriver`

| Dialects                              |
|---------------------------------------|
| `org.hibernate.dialect.OracleDialect` |


### MYSQL

Available drivers are:

1. `com.mysql.jdbc.Driver`
2. `com.mysql.cj.jdbc.Driver`

| Dialects                                     |
|----------------------------------------------|
| `org.hibernate.dialect.MySQLDialect`         |

### PostgreSQL

Available drivers are:

1. `org.postgresql.Driver`

| Dialects                                  |
|-------------------------------------------|
| `org.hibernate.dialect.PostgreSQLDialect` |

### MariaDB

Available drivers are:

1. `org.mariadb.jdbc.Driver`

| Dialects                                  |
|-------------------------------------------|
| `org.hibernate.dialect.MariaDBDialect`    |

### Microsoft SQL Server 

Available drivers are:

1. `net.sourceforge.jtds.jdbc.Driver`
2. `com.microsoft.sqlserver.jdbc.SQLServerDriver`

| Dialects                                     |
|----------------------------------------------|
| `org.hibernate.dialect.SQLServerDialect`     |
