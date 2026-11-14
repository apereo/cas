# Hazelcast Ticket Registry - AWS EC2 Auto Discovery

Source: https://apereo.github.io/cas/development/ticketing/Hazelcast-Ticket-Registry-AutoDiscovery-AWS-EC2.html

Hazelcast support in CAS may handle EC2 auto-discovery automatically. It is useful when you do not want to provide or you cannot provide the list of possible IP addresses for the members of the cluster. You optionally also have the ability to specify partitioning group that would be zone aware. When using the zone-aware configuration, backups are created in the other AZs. Each zone will be accepted as one partition group. Using the AWS Discovery capability requires that you turn off and disable multicast and TCP/IP config in the CAS settings, which should be done automatically by CAS at runtime.

Support is enabled by the following module:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-hazelcast-discovery-aws"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.registry.hazelcast.cluster.discovery.aws.access-key` (Required; type `String`): AWS access key.
- `cas.ticket.registry.hazelcast.cluster.discovery.aws.secret-key` (Required; type `String`): AWS secret key.
- `cas.ticket.registry.hazelcast.cluster.discovery.aws.cluster` (Optional; type `String`): ECS cluster short name or ARN; default is the current cluster.
- `cas.ticket.registry.hazelcast.cluster.discovery.aws.connection-timeout-seconds` (Optional; type `Integer`; default `5`): The maximum amount of time Hazelcast will try to connect to a well known member before giving up. Setting this value too low could mean that a member is not able to connect to a cluster. Setting the value too high means that member startup could slow down because of longer timeouts (for example, when a well known member is not up). Increasing this value is recommended if you have many IPs listed and the members cannot properly build up the cluster. Its default value is 5.
- `cas.ticket.registry.hazelcast.cluster.discovery.aws.family` (Optional; type `String`): Filter to look only for ECS tasks with the given family name; mutually exclusive with `#getServiceName()`.
- `cas.ticket.registry.hazelcast.cluster.discovery.aws.host-header` (Optional; type `String`): Host header. i.e. `ec2.amazonaws.com`. The URL that is the entry point for a web service.
- `cas.ticket.registry.hazelcast.cluster.discovery.aws.iam-role` (Optional; type `String`): If you do not want to use access key and secret key, you can specify iam-role. Hazelcast fetches your credentials by using your IAM role. This setting only affects deployments on Amazon EC2. If you are deploying CAS in an Amazon ECS environment, the role should not be specified. The role is fetched from the task definition that is assigned to run CAS.
- `cas.ticket.registry.hazelcast.cluster.discovery.aws.port` (Optional; type `Integer`; default `-1`): Hazelcast port. Typically may be set to `5701`. You can set searching for other ports rather than 5701 if you've members on different ports.
- `cas.ticket.registry.hazelcast.cluster.discovery.aws.region` (Optional; type `String`; default `us-east-1`): AWS region. i.e. `us-east-1`. The region where your members are running.
- `cas.ticket.registry.hazelcast.cluster.discovery.aws.security-group-name` (Optional; type `String`): If a security group is configured, only instances within that security group are selected.
- `cas.ticket.registry.hazelcast.cluster.discovery.aws.service-name` (Optional; type `String`): Filter to look only for ECS tasks from the given service; mutually exclusive with `#getFamily()`.
- `cas.ticket.registry.hazelcast.cluster.discovery.aws.tag-key` (Optional; type `String`): If a tag key/value is set, only instances with that tag key/value will be selected.
- `cas.ticket.registry.hazelcast.cluster.discovery.aws.tag-value` (Optional; type `String`): If a tag key/value is set, only instances with that tag key/value will be selected.
