# JDBC Drivers

Source: https://apereo.github.io/cas/development/installation/JDBC-Drivers.html

While in most cases this is unnecessary and handled by CAS automatically, you may need to also include the following module to account for various database drivers:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-jdbc-drivers"
}
```

## JPA Implementations

The following JPA implementations are provided by CAS. In most cases, the indicated modules do not need to be included in a CAS build and the implementation would be automatically chosen by the feature module. The details below are listed for reference and may be of use in advanced scenarios where development may be required.

### Hibernate

[Hibernate ORM](https://github.com/hibernate/hibernate-orm) is an object–relational mapping tool for the Java programming language. It provides a framework for mapping an object-oriented domain model to a relational database.

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-jpa-hibernate"
}
```

### EclipseLink

[EclipseLink](https://projects.eclipse.org/projects/ee4j.eclipselink) is the open source Eclipse Persistence Services Project from the Eclipse Foundation. The software provides an extensible framework that allows Java developers to interact with various data services, including databases, web services, Object XML mapping, and enterprise information systems.

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-jpa-eclipselink"
}
```

## Database Support

Automatic support for drivers includes the following databases. All other drivers need to be manually added to the build configuration.

Control global properties that are relevant to Hibernate, when CAS attempts to employ and utilize database resources, connections and queries.

- `cas.jdbc.gen-ddl` (Optional; type `Boolean`; default `true`): Whether to generate DDL after the EntityManagerFactory has been initialized creating/updating all relevant tables.
- `cas.jdbc.physical-table-names` (Optional; type `Map<String,String>`): Indicate a physical table name to be used by the hibernate naming strategy in case table names need to be customized for the specific type of database. The key here indicates the CAS-provided table name and the value is the translate physical name for the database. If a match is not found for the CAS-provided table name, then that name will be used by default.
- `cas.jdbc.show-sql` (Optional; type `Boolean`; default `false`): Whether SQL queries should be displayed in the console/logs.

### H2

Available drivers are:

1. `org.h2.Driver`

| Dialects |
| --- |
| `org.hibernate.dialect.H2Dialect` |

### HSQLDB

Available drivers are:

1. `org.hsqldb.jdbcDriver`

| Dialects |
| --- |
| `org.hibernate.dialect.HSQLDialect` |

### Oracle

Available drivers are:

1. `oracle.jdbc.driver.OracleDriver`

| Dialects |
| --- |
| `org.hibernate.dialect.OracleDialect` |

### MYSQL

Available drivers are:

1. `com.mysql.jdbc.Driver`
2. `com.mysql.cj.jdbc.Driver`

| Dialects |
| --- |
| `org.hibernate.dialect.MySQLDialect` |

### PostgreSQL

Available drivers are:

1. `org.postgresql.Driver`

| Dialects |
| --- |
| `org.hibernate.dialect.PostgreSQLDialect` |

### MariaDB

Available drivers are:

1. `org.mariadb.jdbc.Driver`

| Dialects |
| --- |
| `org.hibernate.dialect.MariaDBDialect` |

### Microsoft SQL Server

Available drivers are:

1. `net.sourceforge.jtds.jdbc.Driver`
2. `com.microsoft.sqlserver.jdbc.SQLServerDriver`

| Dialects |
| --- |
| `org.hibernate.dialect.SQLServerDialect` |
