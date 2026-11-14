# Cassandra Service Registry

Source: https://apereo.github.io/cas/development/services/Cassandra-Service-Management.html

Stores registered service data in [Apache Cassandra](http://cassandra.apache.org/) instances. Services are expected to be found/stored in a `casservices` table with a default write consistency of `LOCAL_QUORUM` and read consistency of `ONE`.

Support is enabled by adding the following module into the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-cassandra-service-registry"
}
```

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.service-registry.cassandra.contact-points` (Required; type `List<String>`): The list of contact points to use for the new cluster. Each contact point should be defined using the syntax `address:port`.
- `cas.service-registry.cassandra.keyspace` (Required; type `String`): Keyspace address to use where the cluster would connect.
- `cas.service-registry.cassandra.password` (Required; type `String`): Password to bind and establish a connection to cassandra.
- `cas.service-registry.cassandra.username` (Required; type `String`): Username to bind and establish a connection to cassandra.
- `cas.service-registry.cassandra.consistency-level` (Optional; type `String`; default `LOCAL_QUORUM`): Query option consistency level. The consistency level set through this method will be use for queries that don't explicitly have a consistency level. Accepted values are:`ALL, ANY, EACH_QUORUM, LOCAL_ONE, LOCAL_QUORUM, LOCAL_SERIAL, ONE, QUORUM, SERIAL, THREE, TWO`.
- `cas.service-registry.cassandra.local-dc` (Optional; type `String`): Used by a DC-ware round-robin load balancing policy. This policy provides round-robin queries over the node of the local data center. It also includes in the query plans returned a configurable number of hosts in the remote data centers, but those are always tried after the local nodes. In other words, this policy guarantees that no host in a remote data center will be queried unless no host in the local data center can be reached.
- `cas.service-registry.cassandra.serial-consistency-level` (Optional; type `String`; default `LOCAL_SERIAL`): Query option serial consistency level. The serial consistency level set through this method will be use for queries that don't explicitly have a serial consistency level. Accepted values are:`ALL, ANY, EACH_QUORUM, LOCAL_ONE, LOCAL_QUORUM, LOCAL_SERIAL, ONE, QUORUM, SERIAL, THREE, TWO`.
- `cas.service-registry.cassandra.ssl-cipher-suites` (Optional; type `String[]`): The cipher suites to use, or empty/null to use the default ones. Note that host name validation will always be done using HTTPS algorithm.
- `cas.service-registry.cassandra.ssl-protocols` (Optional; type `String[]`): Set the protocol versions enabled for use on this engine. Once the setting is set, only protocols listed in the protocols parameter are enabled for use.
- `cas.service-registry.cassandra.timeout` (Optional; type `String`; default `PT5S`): The request timeout. This defines how long the driver will wait for a given Cassandra node to answer a query.

## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="com.datastax.driver" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
...
```

## Auto Initialization

Upon startup and configuration permitting, the registry is able to auto initialize itself from default JSON service definitions available to CAS. See [this guide](https://apereo.github.io/cas/development/services/AutoInitialization-Service-Management.html) for more info.
