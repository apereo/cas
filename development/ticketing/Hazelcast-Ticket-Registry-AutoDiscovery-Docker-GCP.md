# Hazelcast Ticket Registry - Google Cloud Platform (GCP) Auto Discovery

Source: https://apereo.github.io/cas/development/ticketing/Hazelcast-Ticket-Registry-AutoDiscovery-Docker-GCP.html

This hazelcast discovery plugin provides a Google Cloud Platform (GCP) discovery strategy.

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-hazelcast-discovery-gcp"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.registry.hazelcast.cluster.discovery.gcp.hz-port` (Optional; type `String`; default `5701-5708`): A range of ports where the plugin looks for Hazelcast members.
- `cas.ticket.registry.hazelcast.cluster.discovery.gcp.label` (Optional; type `String`): A filter to look only for instances labeled as specified; property format: `key=value`.
- `cas.ticket.registry.hazelcast.cluster.discovery.gcp.private-key-path` (Optional; type `String`): A filesystem path to the private key for GCP service account in the JSON format; if not set, the access token is fetched from the GCP VM instance.
- `cas.ticket.registry.hazelcast.cluster.discovery.gcp.projects` (Optional; type `String`): A list of projects where the plugin looks for instances; if not set, the current project is used.
- `cas.ticket.registry.hazelcast.cluster.discovery.gcp.region` (Optional; type `String`): A region where the plugin looks for instances; if not set, the `#getZones()` property is used; if it and `#getZones()` property not set, all zones of the current region are used.
- `cas.ticket.registry.hazelcast.cluster.discovery.gcp.zones` (Optional; type `String`): A list of zones where the plugin looks for instances; if not set, all zones of the current region are used.

Note that:

- Your GCP Service Account must have permissions to query for all the projects/zones specified in the configuration.
- If you don’t specify any of the properties, then CAS forms a cluster from all Hazelcast members running in the current project, in the current region.
