### Database Configuration

The following options related to JPA/JDBC support in CAS apply equally to a number of CAS components (ticket registries, etc):

```properties
# {{ include.configKey }}.user=sa
# {{ include.configKey }}.password=
# {{ include.configKey }}.driver-class=org.hsqldb.jdbcDriver
# {{ include.configKey }}.url=jdbc:hsqldb:mem:cas-hsql-database
# {{ include.configKey }}.dialect=org.hibernate.dialect.HSQLDialect

# {{ include.configKey }}.fail-fast-timeout=1
# {{ include.configKey }}.isolation-level-name=ISOLATION_READ_COMMITTED 
# {{ include.configKey }}.health-query=
# {{ include.configKey }}.isolate-internal-queries=false
# {{ include.configKey }}.leak-threshold=10
# {{ include.configKey }}.propagation-behaviorName=PROPAGATION_REQUIRED
# {{ include.configKey }}.batchSize=1
# {{ include.configKey }}.default-catalog=
# {{ include.configKey }}.default-schema=
# {{ include.configKey }}.ddl-auto=create-drop
# {{ include.configKey }}.physical-naming-strategy-class-name=org.apereo.cas.hibernate.CasHibernatePhysicalNamingStrategy

# {{ include.configKey }}.autocommit=false
# {{ include.configKey }}.idle-timeout=5000

# {{ include.configKey }}.data-source-name=
# {{ include.configKey }}.data-source-roxy=false

# Hibernate-specific properties (i.e. `hibernate.globally_quoted_identifiers`)
# {{ include.configKey }}.properties.property-name=propertyValue

# {{ include.configKey }}.pool.suspension=false
# {{ include.configKey }}.pool.mi-size=6
# {{ include.configKey }}.pool.max-size=18
# {{ include.configKey }}.pool.max-wait=2000
# {{ include.configKey }}.pool.timeout-millis=1000
```


{% include {{ version }}/hibernate-configuration.md %}

### DDL Configuration

Note that the default value for Hibernate's DDL setting is `create-drop` which may not be appropriate
for use in production. Setting the value to `validate` may be more desirable, but any of the following options can be used:

| Type                 | Description
|----------------------|----------------------------------------------------------
| `validate`           | Validate the schema, but make no changes to the database.
| `update`             | Update the schema.
| `create`             | Create the schema, destroying previous data.
| `create-drop`        | Drop the schema at the end of the session.
| `none`               | Do nothing.

Note that during a version migration where any schema has changed `create-drop` will result
in the loss of all data as soon as CAS is started. For transient data like tickets this is probably
not an issue, but in cases like the audit table important data could be lost. Using `update`, while safe
for data, is confirmed to result in invalid database state. `validate` or `none` settings
are likely the only safe options for production use.

For more information on configuration of transaction levels and propagation behaviors,
please review [this guide](http://docs.spring.io/spring-framework/docs/current/javadoc-api/).

### Container-based JDBC Connections

If you are planning to use a container-managed JDBC connection with CAS (i.e. JPA Ticket/Service Registry, etc)
then you can set the `data-source-name` property on any of the configuration items that require a database
connection. When using a container configured data source, many of the pool related parameters will not be used.
If `data-source-name` is specified but the JNDI lookup fails, a data source will be created with the configured
(or default) CAS pool parameters.

If you experience classloading errors while trying to use a container datasource, you can try
setting the `data-source-proxy` setting to true which will wrap the container datasource in
a way that may resolve the error.

The `data-source-name` property can be either a JNDI name for the datasource or a resource name prefixed with
`java:/comp/env/`. If it is a resource name then you need an entry in a `web.xml` that you can add to your
CAS overlay. It should contain an entry like this:

```xml
<resource-ref>
    <res-ref-name>jdbc/casDataSource</res-ref-name>
    <res-type>javax.sql.DataSource</res-type>
    <res-auth>Container</res-auth>
</resource-ref>
```

In Apache Tomcat a container datasource can be defined like this in the `context.xml`:

```xml
<Resource name="jdbc/casDataSource"
    auth="Container"
    type="javax.sql.DataSource"
    driverClassName="org.postgresql.Driver"
    url="jdbc:postgresql://casdb.example.com:5432/xyz_db"
    username="cas"
    password="xyz"
    testWhileIdle="true"
    testOnBorrow="true"
    testOnReturn="false"
    validationQuery="select 1"
    validationInterval="30000"
    timeBetweenEvictionRunsMillis="30000"
    factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"
    minIdle="0"
    maxIdle="5"
    initialSize="0"
    maxActive="20"
    maxWait="10000" />
```

In Jetty, a pool can be put in JNDI with a `jetty.xml` or `jetty-env.xml` file like this:

```xml
<?xml version="1.0"?>
<!DOCTYPE Configure PUBLIC "-//Jetty//Configure//EN" "http://www.eclipse.org/jetty/configure_9_3.dtd">

<Configure class="org.eclipse.jetty.webapp.WebAppContext">
<New id="datasource.cas" class="org.eclipse.jetty.plus.jndi.Resource">
    <Arg></Arg> <!-- empty scope arg is JVM scope -->
    <Arg>jdbc/casDataSource</Arg> <!-- name that matches resource in web.xml-->
    <Arg>
        <New class="org.apache.commons.dbcp.BasicDataSource">
            <Set name="driverClassName">oracle.jdbc.OracleDriver</Set>
            <Set name="url">jdbc:oracle:thin:@//casdb.example.com:1521/ntrs"</Set>
            <Set name="username">cas</Set>
            <Set name="password">xyz</Set>
            <Set name="validationQuery">select dummy from dual</Set>
            <Set name="testOnBorrow">true</Set>
            <Set name="testOnReturn">false</Set>
            <Set name="testWhileIdle">false</Set>
            <Set name="defaultAutoCommit">false</Set>
            <Set name="initialSize">0</Set>
            <Set name="maxActive">15</Set>
            <Set name="minIdle">0</Set>
            <Set name="maxIdle">5</Set>
            <Set name="maxWait">2000</Set>
        </New>
    </Arg>
</New>
</Configure>
```
