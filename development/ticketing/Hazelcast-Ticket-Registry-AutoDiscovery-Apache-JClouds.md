# Hazelcast Ticket Registry - Apache JClouds Auto Discovery

Source: https://apereo.github.io/cas/development/ticketing/Hazelcast-Ticket-Registry-AutoDiscovery-Apache-JClouds.html

Hazelcast support in CAS may handle auto-discovery automatically via [Apache jclouds®](https://jclouds.apache.org/). It is useful when you do not want to provide or you cannot provide the list of possible IP addresses for the members of the cluster. Apache jclouds® is an open source multi-cloud toolkit for the Java platform that gives you the freedom to create applications that are portable across clouds while giving you full control to use cloud-specific features. To see the full list of supported cloud environments, [please see this link](https://jclouds.apache.org/reference/providers/#compute).

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-hazelcast-discovery-jclouds"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.registry.hazelcast.cluster.discovery.jclouds.credential` (Required; type `String`): Cloud Provider credential, can be thought of as a password for cloud services.
- `cas.ticket.registry.hazelcast.cluster.discovery.jclouds.identity` (Required; type `String`): Cloud Provider identity, can be thought of as a user name for cloud services.
- `cas.ticket.registry.hazelcast.cluster.discovery.jclouds.provider` (Required; type `String`): String value that is used to identify ComputeService provider. For example, "google-compute-engine" is used for Google Cloud services. See [here](https://jclouds.apache.org/reference/providers/#compute) for more info.
- `cas.ticket.registry.hazelcast.cluster.discovery.jclouds.credential-path` (Optional; type `String`): Used for cloud providers which require an extra JSON or P12 key file. This denotes the path of that file. Only tested with Google Compute Engine. (Required if Google Compute Engine is used.)
- `cas.ticket.registry.hazelcast.cluster.discovery.jclouds.endpoint` (Optional; type `String`): Defines the endpoint for a generic API such as OpenStack or CloudStack (optional).
- `cas.ticket.registry.hazelcast.cluster.discovery.jclouds.group` (Optional; type `String`): Filters instance groups (optional). When used with AWS it maps to security group.
- `cas.ticket.registry.hazelcast.cluster.discovery.jclouds.port` (Optional; type `Integer`; default `-1`): Port which the hazelcast instance service uses on the cluster member. Default value is 5701. (optional)
- `cas.ticket.registry.hazelcast.cluster.discovery.jclouds.regions` (Optional; type `String`): Defines region for a cloud service (optional). Can be used with comma separated values for multiple values.
- `cas.ticket.registry.hazelcast.cluster.discovery.jclouds.role-name` (Optional; type `String`): Used for IAM role support specific to AWS (optional, but if defined, no identity or credential should be defined in the configuration).
- `cas.ticket.registry.hazelcast.cluster.discovery.jclouds.tag-keys` (Optional; type `String`): Filters cloud instances with tags (optional). Can be used with comma separated values for multiple values.
- `cas.ticket.registry.hazelcast.cluster.discovery.jclouds.tag-values` (Optional; type `String`): Filters cloud instances with tags (optional) Can be used with comma separated values for multiple values.
- `cas.ticket.registry.hazelcast.cluster.discovery.jclouds.zones` (Optional; type `String`): Defines zone for a cloud service (optional). Can be used with comma separated values for multiple values.
