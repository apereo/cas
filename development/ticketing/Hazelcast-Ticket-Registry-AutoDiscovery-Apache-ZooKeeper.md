# Hazelcast Ticket Registry - Apache ZooKeeper Auto Discovery

Source: https://apereo.github.io/cas/development/ticketing/Hazelcast-Ticket-Registry-AutoDiscovery-Apache-ZooKeeper.html

This plugin provides a service-based discovery by using Apache Curator to communicate with your Zookeeper server. Support is enabled by the following module:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-hazelcast-discovery-zookeeper"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.registry.hazelcast.cluster.discovery.zookeeper.group` (Required; type `String`): Name of this Hazelcast cluster. You can have multiple distinct clusters to use the same ZooKeeper installation
- `cas.ticket.registry.hazelcast.cluster.discovery.zookeeper.path` (Required; type `String`; default `/discovery/hazelcast`): Path in zookeeper to be used for auto-discovery of members where members are tracked.
- `cas.ticket.registry.hazelcast.cluster.discovery.zookeeper.url` (Required; type `String`): Zookeeper url address typically in the format of `ip-address:port`.

## Ticket Registry Locking

This ticket registry implementation automatically supports [distributed locking](https://apereo.github.io/cas/development/ticketing/Ticket-Registry-Locking.html). The database schemas and structures that track locking operations should be automatically created by CAS using [Spring Integration](https://spring.io/projects/spring-integration) ZooKeeper support.
