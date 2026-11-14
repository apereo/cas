# Throttling Authentication Attempts - Failure

Source: https://apereo.github.io/cas/development/authentication/Configuring-Authentication-Throttling-Failure.html

CAS provides a facility for limiting failed login attempts to support password guessing and related abuse scenarios. A couple strategies are provided for tracking failed attempts:

1. Source IP - Limit successive failed logins against any username from the same IP address.
2. Source IP and username - Limit successive failed logins against a particular user from the same IP address.

All login throttling components that ship with CAS limit successive failed login attempts that exceed a threshold rate, which is a time in seconds between two failures. The following properties are provided to define the failure rate.

- `threshold` - Number of failed login attempts.
- `rangeSeconds` - Period of time in seconds.

A request counts as a failed attempt when its response has the status `401`, which is how CAS endpoints answer a failed authentication; endpoints that must answer with another status, such as SAML2 ECP with a SOAP fault, mark the request as a failed attempt explicitly. Other responses, such as malformed requests (`400`), authorization denials (`403`), missing resources (`404`) or server errors (`500`), are not counted.

A failure rate of more than 1 per 3 seconds is indicative of an automated authentication attempt, which is a reasonable basis for throttling policy. Regardless of policy care should be taken to weigh security against access; overly restrictive policies may prevent legitimate authentication attempts.

> **Threshold Rate**
>
> The failure threshold rate is calculated as: `threshold / rangeSeconds`. For instance, the failure rate for the above scenario would be `0.333333`. An authentication attempt may be considered throttled if the request submission rate (calculated as the difference between the current date and the last submission date) exceeds the failure threshold rate.

Enable the following module in your configuration overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-throttle"
}
```

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.throttle.hazelcast.cluster.core.instance-name` (Required; type `String`): The instance name.
- `cas.authn.throttle.hazelcast.cluster.discovery.aws.access-key` (Required; type `String`): AWS access key.
- `cas.authn.throttle.hazelcast.cluster.discovery.aws.secret-key` (Required; type `String`): AWS secret key.
- `cas.authn.throttle.hazelcast.cluster.discovery.azure.client-id` (Required; type `String`): The Azure Active Directory Service Principal client ID.
- `cas.authn.throttle.hazelcast.cluster.discovery.azure.client-secret` (Required; type `String`): The Azure Active Directory Service Principal client secret.
- `cas.authn.throttle.hazelcast.cluster.discovery.azure.cluster-id` (Required; type `String`): The name of the tag on the hazelcast vm resources. With every Hazelcast Virtual Machine you deploy in your resource group, you need to ensure that each VM is tagged with the value of cluster-id defined in your Hazelcast configuration. The only requirement is that every VM can access each other either by private or public IP address.
- `cas.authn.throttle.hazelcast.cluster.discovery.azure.group-name` (Required; type `String`): The Azure resource group name of the cluster. You can find this in the Azure portal or CLI.
- `cas.authn.throttle.hazelcast.cluster.discovery.azure.subscription-id` (Required; type `String`): The Azure subscription ID.
- `cas.authn.throttle.hazelcast.cluster.discovery.azure.tenant-id` (Required; type `String`): The Azure Active Directory tenant ID.
- `cas.authn.throttle.hazelcast.cluster.discovery.jclouds.credential` (Required; type `String`): Cloud Provider credential, can be thought of as a password for cloud services.
- `cas.authn.throttle.hazelcast.cluster.discovery.jclouds.identity` (Required; type `String`): Cloud Provider identity, can be thought of as a user name for cloud services.
- `cas.authn.throttle.hazelcast.cluster.discovery.jclouds.provider` (Required; type `String`): String value that is used to identify ComputeService provider. For example, "google-compute-engine" is used for Google Cloud services. See [here](https://jclouds.apache.org/reference/providers/#compute) for more info.
- `cas.authn.throttle.hazelcast.cluster.discovery.zookeeper.group` (Required; type `String`): Name of this Hazelcast cluster. You can have multiple distinct clusters to use the same ZooKeeper installation
- `cas.authn.throttle.hazelcast.cluster.discovery.zookeeper.path` (Required; type `String`; default `/discovery/hazelcast`): Path in zookeeper to be used for auto-discovery of members where members are tracked.
- `cas.authn.throttle.hazelcast.cluster.discovery.zookeeper.url` (Required; type `String`): Zookeeper url address typically in the format of `ip-address:port`.
- `cas.authn.throttle.hazelcast.cluster.network.members` (Required; type `List<String>`): Sets the well known members. If members is empty, calling this method will have the same effect as calling `clear()`. A member can be a comma separated string, e..g `10.11.12.1,10.11.12.2` which indicates multiple members are going to be added. The list of members must include ALL CAS server node, including the current node that owns this configuration.
- `cas.authn.throttle.hazelcast.cluster.network.port` (Required; type `Integer`; default `5701`): You can specify the ports which Hazelcast will use to communicate between cluster members. The name of the parameter for this is port and its default value is 5701. By default, Hazelcast will try 100 ports to bind. Meaning that, if you set the value of port as 5701, as members are joining to the cluster, Hazelcast tries to find ports between 5701 and 5801.
- `cas.authn.throttle.core.app-code` (Optional; type `String`; default `CAS`): Application code used to identify this application in the audit logs.
- `cas.authn.throttle.core.header-name` (Optional; type `String`): Request header name to use in order to extract a header value from the request.
- `cas.authn.throttle.core.username-parameter` (Optional; type `String`): Username parameter to use in order to extract the username from the request.
- `cas.authn.throttle.failure.code` (Optional; type `String`; default `AUTHENTICATION_FAILED`): Failure code to record in the audit log. Generally this indicates an authentication failure event.
- `cas.authn.throttle.failure.range-seconds` (Optional; type `Integer`; default `-1`): Period of time in seconds for the threshold rate.
- `cas.authn.throttle.failure.threshold` (Optional; type `Integer`; default `-1`): Number of failed login attempts for the threshold rate.
- `cas.authn.throttle.failure.throttle-window-seconds` (Optional; type `String`; default `0`): Indicate the number of seconds the account should remain in a locked/throttled state before it can be released to continue again. If no value is specified, the failure threshold and rate that is calculated would hold.
- `cas.authn.throttle.hazelcast.cluster.core.async-backup-count` (Optional; type `Integer`; default `0`): Hazelcast supports both synchronous and asynchronous backups. By default, backup operations are synchronous. In this case, backup operations block operations until backups are successfully copied to backup members (or deleted from backup members in case of remove) and acknowledgements are received. Therefore, backups are updated before a put operation is completed, provided that the cluster is stable. Asynchronous backups, on the other hand, do not block operations. They are fire and forget and do not require acknowledgements; the backup operations are performed at some point in time.
- `cas.authn.throttle.hazelcast.cluster.core.async-fillup` (Optional; type `Boolean`)
- `cas.authn.throttle.hazelcast.cluster.core.backup-count` (Optional; type `Integer`; default `1`): To provide data safety, Hazelcast allows you to specify the number of backup copies you want to have. That way, data on a cluster member will be copied onto other member(s). To create synchronous backups, select the number of backup copies. When this count is 1, a map entry will have its backup on one other member in the cluster. If you set it to 2, then a map entry will have its backup on two other members. You can set it to 0 if you do not want your entries to be backed up, e.g., if performance is more important than backing up. The maximum value for the backup count is 6. Sync backup operations have a blocking cost which may lead to latency issues.
- `cas.authn.throttle.hazelcast.cluster.core.cluster-name` (Optional; type `String`; default `cas-hazelcast-cluster`)
- `cas.authn.throttle.hazelcast.cluster.core.cp-member-count` (Optional; type `Integer`; default `0`): CP Subsystem is a component of a Hazelcast cluster that builds a strongly consistent layer for a set of distributed data structures. Its data structures are CP with respect to the CAP principle, i.e., they always maintain linearizability and prefer consistency over availability during network partitions. Besides network partitions, CP Subsystem withstands server and client failures. All members of a Hazelcast cluster do not necessarily take part in CP Subsystem. The number of Hazelcast members that take part in CP Subsystem is specified here. CP Subsystem must have at least 3 CP members.
- `cas.authn.throttle.hazelcast.cluster.core.eviction-policy` (Optional)
- `cas.authn.throttle.hazelcast.cluster.core.logging-type` (Optional; type `String`; default `slf4j`): Hazelcast has a flexible logging configuration and doesn't depend on any logging framework except JDK logging. It has in-built adaptors for a number of logging frameworks and also supports custom loggers by providing logging interfaces. To use built-in adaptors you should set this setting to one of predefined types below. - `jdk`: JDK logging - `log4j`: Log4j - `slf4j`: Slf4j - `none`: Disable logging
- `cas.authn.throttle.hazelcast.cluster.core.map-merge-policy` (Optional)
- `cas.authn.throttle.hazelcast.cluster.core.max-no-heartbeat-seconds` (Optional; type `Integer`; default `300`): Max timeout of heartbeat in seconds for a node to assume it is dead.
- `cas.authn.throttle.hazelcast.cluster.core.max-size` (Optional; type `Integer`; default `85`): Sets the maximum size of the map.
- `cas.authn.throttle.hazelcast.cluster.core.max-size-policy` (Optional; type `String`; default `USED_HEAP_PERCENTAGE`): Control the max size policy. - `FREE_HEAP_PERCENTAGE`: Policy based on minimum free JVM heap memory percentage per JVM. - `FREE_HEAP_SIZE`: Policy based on minimum free JVM heap memory in megabytes per JVM. - `FREE_NATIVE_MEMORY_PERCENTAGE`: Policy based on minimum free native memory percentage per Hazelcast instance. - `FREE_NATIVE_MEMORY_SIZE`: Policy based on minimum free native memory in megabytes per Hazelcast instance. - `PER_NODE`: Policy based on maximum number of entries stored per data structure (map, cache etc) on each Hazelcast instance. - `PER_PARTITION`: Policy based on maximum number of entries stored per data structure (map, cache etc) on each partition. - `USED_HEAP_PERCENTAGE`: Policy based on maximum used JVM heap memory percentage per data structure (map, cache etc) on each Hazelcast instance . - `USED_HEAP_SIZE`: Policy based on maximum used JVM heap memory in megabytes per data structure (map, cache etc) on each Hazelcast instance. - `USED_NATIVE_MEMORY_PERCENTAGE`: Policy based on maximum used native memory percentage per data structure (map, cache etc) on each Hazelcast instance. - `USED_NATIVE_MEMORY_SIZE`: Policy based on maximum used native memory in megabytes per data structure (map, cache etc) on each Hazelcast instance .
- `cas.authn.throttle.hazelcast.cluster.core.partition-member-group-type` (Optional; type `String`): With `PartitionGroupConfig`, you can control how primary and backup partitions are mapped to physical Members. Hazelcast will always place partitions on different partition groups so as to provide redundancy. Accepted value are: `PER_MEMBER, HOST_AWARE, CUSTOM, ZONE_AWARE, SPI`. In all cases a partition will never be created on the same group. If there are more partitions defined than there are partition groups, then only those partitions, up to the number of partition groups, will be created. For example, if you define 2 backups, then with the primary, that makes 3. If you have only two partition groups only two will be created. - {@code}PER_MEMBER Partition Groups}: This is the default partition scheme and is used if no other scheme is defined. Each Member is in a group of its own. - {@code}HOST_AWARE Partition Groups}: In this scheme, a group corresponds to a host, based on its IP address. Partitions will not be written to any other members on the same host. This scheme provides good redundancy when multiple instances are being run on the same host. - {@code}CUSTOM Partition Groups}: In this scheme, IP addresses, or IP address ranges, are allocated to groups. Partitions are not written to the same group. This is very useful for ensuring partitions are written to different racks or even availability zones. - {@code}ZONE_AWARE Partition Groups}: In this scheme, groups are allocated according to the metadata provided by Discovery SPI Partitions are not written to the same group. This is very useful for ensuring partitions are written to availability zones or different racks without providing the IP addresses to the config ahead. - {@code}SPI Partition Groups}: In this scheme, groups are allocated according to the implementation provided by Discovery SPI.
- `cas.authn.throttle.hazelcast.cluster.core.replicated` (Optional; type `Boolean`)
- `cas.authn.throttle.hazelcast.cluster.core.timeout` (Optional; type `Integer`; default `5`): Connection timeout in seconds for the TCP/IP config and members joining the cluster.
- `cas.authn.throttle.hazelcast.cluster.discovery.aws.cluster` (Optional; type `String`): ECS cluster short name or ARN; default is the current cluster.
- `cas.authn.throttle.hazelcast.cluster.discovery.aws.connection-timeout-seconds` (Optional; type `Integer`; default `5`): The maximum amount of time Hazelcast will try to connect to a well known member before giving up. Setting this value too low could mean that a member is not able to connect to a cluster. Setting the value too high means that member startup could slow down because of longer timeouts (for example, when a well known member is not up). Increasing this value is recommended if you have many IPs listed and the members cannot properly build up the cluster. Its default value is 5.
- `cas.authn.throttle.hazelcast.cluster.discovery.aws.family` (Optional; type `String`): Filter to look only for ECS tasks with the given family name; mutually exclusive with `#getServiceName()`.
- `cas.authn.throttle.hazelcast.cluster.discovery.aws.host-header` (Optional; type `String`): Host header. i.e. `ec2.amazonaws.com`. The URL that is the entry point for a web service.
- `cas.authn.throttle.hazelcast.cluster.discovery.aws.iam-role` (Optional; type `String`): If you do not want to use access key and secret key, you can specify iam-role. Hazelcast fetches your credentials by using your IAM role. This setting only affects deployments on Amazon EC2. If you are deploying CAS in an Amazon ECS environment, the role should not be specified. The role is fetched from the task definition that is assigned to run CAS.
- `cas.authn.throttle.hazelcast.cluster.discovery.aws.port` (Optional; type `Integer`; default `-1`): Hazelcast port. Typically may be set to `5701`. You can set searching for other ports rather than 5701 if you've members on different ports.
- `cas.authn.throttle.hazelcast.cluster.discovery.aws.region` (Optional; type `String`; default `us-east-1`): AWS region. i.e. `us-east-1`. The region where your members are running.
- `cas.authn.throttle.hazelcast.cluster.discovery.aws.security-group-name` (Optional; type `String`): If a security group is configured, only instances within that security group are selected.
- `cas.authn.throttle.hazelcast.cluster.discovery.aws.service-name` (Optional; type `String`): Filter to look only for ECS tasks from the given service; mutually exclusive with `#getFamily()`.
- `cas.authn.throttle.hazelcast.cluster.discovery.aws.tag-key` (Optional; type `String`): If a tag key/value is set, only instances with that tag key/value will be selected.
- `cas.authn.throttle.hazelcast.cluster.discovery.aws.tag-value` (Optional; type `String`): If a tag key/value is set, only instances with that tag key/value will be selected.
- `cas.authn.throttle.hazelcast.cluster.discovery.enabled` (Optional; type `Boolean`; default `false`): Whether discovery should be enabled via the configured strategies below.
- `cas.authn.throttle.hazelcast.cluster.discovery.gcp.hz-port` (Optional; type `String`; default `5701-5708`): A range of ports where the plugin looks for Hazelcast members.
- `cas.authn.throttle.hazelcast.cluster.discovery.gcp.label` (Optional; type `String`): A filter to look only for instances labeled as specified; property format: `key=value`.
- `cas.authn.throttle.hazelcast.cluster.discovery.gcp.private-key-path` (Optional; type `String`): A filesystem path to the private key for GCP service account in the JSON format; if not set, the access token is fetched from the GCP VM instance.
- `cas.authn.throttle.hazelcast.cluster.discovery.gcp.projects` (Optional; type `String`): A list of projects where the plugin looks for instances; if not set, the current project is used.
- `cas.authn.throttle.hazelcast.cluster.discovery.gcp.region` (Optional; type `String`): A region where the plugin looks for instances; if not set, the `#getZones()` property is used; if it and `#getZones()` property not set, all zones of the current region are used.
- `cas.authn.throttle.hazelcast.cluster.discovery.gcp.zones` (Optional; type `String`): A list of zones where the plugin looks for instances; if not set, all zones of the current region are used.
- `cas.authn.throttle.hazelcast.cluster.discovery.jclouds.credential-path` (Optional; type `String`): Used for cloud providers which require an extra JSON or P12 key file. This denotes the path of that file. Only tested with Google Compute Engine. (Required if Google Compute Engine is used.)
- `cas.authn.throttle.hazelcast.cluster.discovery.jclouds.endpoint` (Optional; type `String`): Defines the endpoint for a generic API such as OpenStack or CloudStack (optional).
- `cas.authn.throttle.hazelcast.cluster.discovery.jclouds.group` (Optional; type `String`): Filters instance groups (optional). When used with AWS it maps to security group.
- `cas.authn.throttle.hazelcast.cluster.discovery.jclouds.port` (Optional; type `Integer`; default `-1`): Port which the hazelcast instance service uses on the cluster member. Default value is 5701. (optional)
- `cas.authn.throttle.hazelcast.cluster.discovery.jclouds.regions` (Optional; type `String`): Defines region for a cloud service (optional). Can be used with comma separated values for multiple values.
- `cas.authn.throttle.hazelcast.cluster.discovery.jclouds.role-name` (Optional; type `String`): Used for IAM role support specific to AWS (optional, but if defined, no identity or credential should be defined in the configuration).
- `cas.authn.throttle.hazelcast.cluster.discovery.jclouds.tag-keys` (Optional; type `String`): Filters cloud instances with tags (optional). Can be used with comma separated values for multiple values.
- `cas.authn.throttle.hazelcast.cluster.discovery.jclouds.tag-values` (Optional; type `String`): Filters cloud instances with tags (optional) Can be used with comma separated values for multiple values.
- `cas.authn.throttle.hazelcast.cluster.discovery.jclouds.zones` (Optional; type `String`): Defines zone for a cloud service (optional). Can be used with comma separated values for multiple values.
- `cas.authn.throttle.hazelcast.cluster.discovery.kubernetes.api-retries` (Optional; type `Integer`; default `3`): Defines the number of retries to Kubernetes API. Defaults to: 3.
- `cas.authn.throttle.hazelcast.cluster.discovery.kubernetes.api-token` (Optional; type `String`): Defines an oauth token for the kubernetes client to access the kubernetes REST API. Defaults to reading the token from the auto-injected file at: `/var/run/secrets/kubernetes.io/serviceaccount/token`.
- `cas.authn.throttle.hazelcast.cluster.discovery.kubernetes.ca-certificate` (Optional; type `String`): CA Authority certificate from Kubernetes Master. Defaults to reading the certificate from the auto-injected file at: `/var/run/secrets/kubernetes.io/serviceaccount/ca.crt`.
- `cas.authn.throttle.hazelcast.cluster.discovery.kubernetes.kubernetes-master` (Optional; type `String`): Defines an alternative address for the kubernetes master. Defaults to: `https://kubernetes.default.svc`
- `cas.authn.throttle.hazelcast.cluster.discovery.kubernetes.namespace` (Optional; type `String`): Defines the namespace of the application POD through the Service Discovery REST API of Kubernetes.
- `cas.authn.throttle.hazelcast.cluster.discovery.kubernetes.pod-label-name` (Optional; type `String`): Defines the pod label to lookup through the Service Discovery REST API of Kubernetes.
- `cas.authn.throttle.hazelcast.cluster.discovery.kubernetes.pod-label-value` (Optional; type `String`): Defines the pod label value to lookup through the Service Discovery REST API of Kubernetes.
- `cas.authn.throttle.hazelcast.cluster.discovery.kubernetes.resolve-not-ready-addresses` (Optional; type `Boolean`; default `false`): Defines if not ready addresses should be evaluated to be discovered on startup.
- `cas.authn.throttle.hazelcast.cluster.discovery.kubernetes.service-dns` (Optional; type `String`): Defines the DNS service lookup domain. This is defined as something similar to `my-svc.my-namespace.svc.cluster.local`.
- `cas.authn.throttle.hazelcast.cluster.discovery.kubernetes.service-dns-timeout` (Optional; type `Integer`; default `-1`): Defines the DNS service lookup timeout in seconds. Defaults to 5 secs.
- `cas.authn.throttle.hazelcast.cluster.discovery.kubernetes.service-label-name` (Optional; type `String`): Defines the service label to lookup through the Service Discovery REST API of Kubernetes.
- `cas.authn.throttle.hazelcast.cluster.discovery.kubernetes.service-label-value` (Optional; type `String`): Defines the service label value to lookup through the Service Discovery REST API of Kubernetes.
- `cas.authn.throttle.hazelcast.cluster.discovery.kubernetes.service-name` (Optional; type `String`): Defines the service name of the POD to lookup through the Service Discovery REST API of Kubernetes.
- `cas.authn.throttle.hazelcast.cluster.discovery.kubernetes.service-port` (Optional; type `Integer`; default `0`): If specified with a value greater than 0, its value defines the endpoint port of the service (overriding the default).
- `cas.authn.throttle.hazelcast.cluster.discovery.kubernetes.use-node-name-as-external-address` (Optional; type `Boolean`; default `false`): Defines if the node name should be used as external address, instead of looking up the external IP using the `/nodes` resource. Default is false.
- `cas.authn.throttle.hazelcast.cluster.discovery.multicast.enabled` (Optional; type `Boolean`; default `false`): Enables a multicast configuration using a group address and port. Contains the configuration for the multicast discovery mechanism. With the multicast discovery mechanism Hazelcast allows Hazelcast members to find each other using multicast. So Hazelcast members do not need to know concrete addresses of members, they just multicast to everyone listening. It depends on your environment if multicast is possible or allowed; otherwise you need to have a look at the tcp/ip cluster
- `cas.authn.throttle.hazelcast.cluster.discovery.multicast.group` (Optional; type `String`): The multicast group address used for discovery. With the multicast auto-discovery mechanism, Hazelcast allows cluster members to find each other using multicast communication. The cluster members do not need to know the concrete addresses of the other members, as they just multicast to all the other members for listening. Whether multicast is possible or allowed depends on your environment.
- `cas.authn.throttle.hazelcast.cluster.discovery.multicast.port` (Optional; type `Integer`; default `0`): The multicast port used for discovery.
- `cas.authn.throttle.hazelcast.cluster.discovery.multicast.time-to-live` (Optional; type `Integer`; default `32`): Gets the time to live for the multicast package in seconds. This is the default time-to-live for multicast packets sent out on the socket
- `cas.authn.throttle.hazelcast.cluster.discovery.multicast.timeout` (Optional; type `Integer`; default `2`): specifies the time in seconds that a member should wait for a valid multicast response from another member running in the network before declaring itself the leader member (the first member joined to the cluster) and creating its own cluster. This only applies to the startup of members where no leader has been assigned yet. If you specify a high value, such as 60 seconds, it means that until a leader is selected, each member will wait 60 seconds before moving on. Be careful when providing a high value. Also, be careful not to set the value too low, or the members might give up too early and create their own cluster.
- `cas.authn.throttle.hazelcast.cluster.discovery.multicast.trusted-interfaces` (Optional; type `String`): Multicast trusted interfaces for discovery. With the multicast auto-discovery mechanism, Hazelcast allows cluster members to find each other using multicast communication. The cluster members do not need to know the concrete addresses of the other members, as they just multicast to all the other members for listening. Whether multicast is possible or allowed depends on your environment.
- `cas.authn.throttle.hazelcast.cluster.network.ipv4-enabled` (Optional; type `Boolean`; default `true`): IPv6 support has been switched off by default, since some platforms have issues in use of IPv6 stack. And some other platforms such as Amazon AWS have no support at all. To enable IPv6 support set this setting to false.
- `cas.authn.throttle.hazelcast.cluster.network.local-address` (Optional; type `String`): If this property is set, then this is the address where the server socket is bound to.
- `cas.authn.throttle.hazelcast.cluster.network.network-interfaces` (Optional; type `String`): You can specify which network interfaces that Hazelcast should use. Servers mostly have more than one network interface, so you may want to list the valid IPs. Range characters ('*' and '-') can be used for simplicity. For instance, 10.3.10.* refers to IPs between 10.3.10.0 and 10.3.10.255. Interface 10.3.10.4-18 refers to IPs between 10.3.10.4 and 10.3.10.18 (4 and 18 included). If network interface configuration is enabled (it is disabled by default) and if Hazelcast cannot find an matching interface, then it will print a message on the console and will not start on that node. Interfaces can be separated by a comma.
- `cas.authn.throttle.hazelcast.cluster.network.outbound-ports` (Optional; type `List<String>`): The outbound ports for the Hazelcast configuration.
- `cas.authn.throttle.hazelcast.cluster.network.port-auto-increment` (Optional; type `Boolean`; default `true`): You may also want to choose to use only one port. In that case, you can disable the auto-increment feature of port.
- `cas.authn.throttle.hazelcast.cluster.network.public-address` (Optional; type `String`): The default public address to be advertised to other cluster members and clients.
- `cas.authn.throttle.hazelcast.cluster.network.ssl.cipher-suites` (Optional; type `String`): Comma-separated list of cipher suite names allowed to be used. Its default value are all supported suites in your Java runtime.
- `cas.authn.throttle.hazelcast.cluster.network.ssl.key-manager-algorithm` (Optional; type `String`): Name of the algorithm based on which the authentication keys are provided.
- `cas.authn.throttle.hazelcast.cluster.network.ssl.key-store-type` (Optional; type `String`; default `JKS`): Type of the keystore. Its default value is JKS. Another commonly used type is the PKCS12. Available keystore/truststore types depend on your Operating system and the Java runtime. Only needed when the mutual authentication is used.
- `cas.authn.throttle.hazelcast.cluster.network.ssl.keystore` (Optional; type `String`): Path of your keystore file. Only needed when the mutual authentication is used.
- `cas.authn.throttle.hazelcast.cluster.network.ssl.keystore-password` (Optional; type `String`): Password to access the key from your keystore file. Only needed when the mutual authentication is used.
- `cas.authn.throttle.hazelcast.cluster.network.ssl.mutual-authentication` (Optional; type `String`): Mutual authentication configuration. It’s empty by default which means the client side of connection is not authenticated. Available values are: - `REQUIRED` - server forces usage of a trusted client certificate - `OPTIONAL` - server asks for a client certificate, but it doesn't require it
- `cas.authn.throttle.hazelcast.cluster.network.ssl.protocol` (Optional; type `String`; default `TLS`): Name of the algorithm which is used in your TLS/SSL. For the protocol property, we recommend you to provide TLS with its version information, e.g., TLSv1.2. Note that if you write only TLS, your application chooses the TLS version according to your Java version.
- `cas.authn.throttle.hazelcast.cluster.network.ssl.trust-manager-algorithm` (Optional; type `String`): Name of the algorithm based on which the trust managers are provided.
- `cas.authn.throttle.hazelcast.cluster.network.ssl.trust-store` (Optional; type `String`): Path of your truststore file. The file truststore is a keystore file that contains a collection of certificates trusted by your application.
- `cas.authn.throttle.hazelcast.cluster.network.ssl.trust-store-password` (Optional; type `String`): Password to unlock the truststore file.
- `cas.authn.throttle.hazelcast.cluster.network.ssl.trust-store-type` (Optional; type `String`; default `JKS`): Type of the truststore. Its default value is JKS. Another commonly used type is the PKCS12. Available keystore/truststore types depend on your Operating system and the Java runtime.
- `cas.authn.throttle.hazelcast.cluster.network.ssl.validate-identity` (Optional; type `Boolean`; default `false`): Flag which allows enabling endpoint identity validation. It means, during the TLS handshake client verifies if the server’s hostname (or IP address) matches the information in X.509 certificate (Subject Alternative Name extension).
- `cas.authn.throttle.hazelcast.cluster.network.tcpip-enabled` (Optional; type `Boolean`; default `true`): Enable TCP/IP config. Contains the configuration for the Tcp/Ip join mechanism. The Tcp/Ip join mechanism relies on one or more well known members. So when a new member wants to join a cluster, it will try to connect to one of the well known members. If it is able to connect, it will now about all members in the cluster and doesn't rely on these well known members anymore.
- `cas.authn.throttle.hazelcast.cluster.wan-replication.enabled` (Optional; type `Boolean`; default `false`): Whether WAN should be enabled.
- `cas.authn.throttle.hazelcast.cluster.wan-replication.replication-name` (Optional; type `String`; default `apereo-cas`): Name of this replication group.
- `cas.authn.throttle.hazelcast.cluster.wan-replication.targets` (Optional; type `List<HazelcastWANReplicationTargetClusterProperties>`): List of target clusters to be used for synchronization and replication.
- `cas.authn.throttle.hazelcast.cluster.wan-replication.targets[0].acknowledge-type` (Optional; type `String`; default `ACK_ON_OPERATION_COMPLETE`): Accepted acknowledgement-type values are listed below. - `ACK_ON_RECEIPT`: ACK after WAN operation is received by the target cluster (without waiting the result of actual operation invocation). - `ACK_ON_OPERATION_COMPLETE`: Wait till the operation is complete on target cluster.
- `cas.authn.throttle.hazelcast.cluster.wan-replication.targets[0].batch-maximum-delay-milliseconds` (Optional; type `int`; default `1000`): Maximum amount of time, in milliseconds, to be waited before sending a batch of events in case batch.size is not reached.
- `cas.authn.throttle.hazelcast.cluster.wan-replication.targets[0].batch-size` (Optional; type `int`; default `500`): Maximum size of events that are sent to the target cluster in a single batch.
- `cas.authn.throttle.hazelcast.cluster.wan-replication.targets[0].cluster-name` (Optional; type `String`): Sets the cluster name used as an endpoint group password for authentication on the target endpoint. If there is no separate publisher ID property defined, this cluster name will also be used as a WAN publisher ID. This ID is then used for identifying the publisher.
- `cas.authn.throttle.hazelcast.cluster.wan-replication.targets[0].consistency-check-strategy` (Optional; type `String`; default `NONE`): Strategy for checking the consistency of data between replicas.
- `cas.authn.throttle.hazelcast.cluster.wan-replication.targets[0].endpoints` (Optional; type `String`): Comma separated list of endpoints in this replication group. IP addresses and ports of the cluster members for which the WAN replication is implemented. These endpoints are not necessarily the entire target cluster and WAN does not perform the discovery of other members in the target cluster. It only expects that these IP addresses (or at least some of them) are available.
- `cas.authn.throttle.hazelcast.cluster.wan-replication.targets[0].executor-thread-count` (Optional; type `int`; default `2`): The number of threads that the replication executor will have. The executor is used to send WAN events to the endpoints and ideally you want to have one thread per endpoint. If this property is omitted and you have specified the endpoints property, this will be the case. If necessary you can manually define the number of threads that the executor will use. Once the executor has been initialized there is thread affinity between the discovered endpoints and the executor threads - all events for a single endpoint will go through a single executor thread, preserving event order. It is important to determine which number of executor threads is a good value. Failure to do so can lead to performance issues - either contention on a too small number of threads or wasted threads that will not be performing any work.
- `cas.authn.throttle.hazelcast.cluster.wan-replication.targets[0].properties` (Optional; type `Map<String, Comparable>`): The WAN publisher properties.
- `cas.authn.throttle.hazelcast.cluster.wan-replication.targets[0].publisher-class-name` (Optional; type `String`; default `com.hazelcast.enterprise.wan.replication.WanBatchReplication`): Publisher class name for WAN replication.
- `cas.authn.throttle.hazelcast.cluster.wan-replication.targets[0].publisher-id` (Optional; type `String`): Returns the publisher ID used for identifying the publisher.
- `cas.authn.throttle.hazelcast.cluster.wan-replication.targets[0].queue-capacity` (Optional; type `int`; default `10000`): For huge clusters or high data mutation rates, you might need to increase the replication queue size. The default queue size for replication queues is 10,000. This means, if you have heavy put/update/remove rates, you might exceed the queue size so that the oldest, not yet replicated, updates might get lost.
- `cas.authn.throttle.hazelcast.cluster.wan-replication.targets[0].queue-full-behavior` (Optional; type `String`; default `THROW_EXCEPTION`): Accepted queue-full behavior values are listed below. - `THROW_EXCEPTION`: Instruct WAN replication implementation to throw an exception and doesn't allow further processing. - `DISCARD_AFTER_MUTATION`: Instruct WAN replication implementation to drop new events when WAN event queues are full. - `THROW_EXCEPTION_ONLY_IF_REPLICATION_ACTIVE`: Similar to `THROW_EXCEPTION` but only throws exception when WAN replication is active. * Discards the new events if WAN replication is stopped.
- `cas.authn.throttle.hazelcast.cluster.wan-replication.targets[0].response-timeout-milliseconds` (Optional; type `int`; default `60000`): Time, in milliseconds, to be waited for the acknowledgment of a sent WAN event to target cluster.
- `cas.authn.throttle.hazelcast.cluster.wan-replication.targets[0].snapshot-enabled` (Optional; type `boolean`): When set to true, only the latest events (based on key) are selected and sent in a batch.
- `cas.authn.throttle.hazelcast.core.enable-compression` (Optional; type `Boolean`; default `false`): Enables compression when default java serialization is used. This applies to values Hazelcast stores with Java serialization, such as authentication throttling records and streamed service definitions. Tickets of the Hazelcast ticket registry are stored with compact serialization and are not compressed.
- `cas.authn.throttle.hazelcast.core.enable-jet` (Optional; type `Boolean`)
- `cas.authn.throttle.hazelcast.core.enable-management-center-scripting` (Optional; type `Boolean`; default `true`): Enables scripting from Management Center.
- `cas.authn.throttle.hazelcast.core.license-key` (Optional; type `String`): Hazelcast enterprise license key.
- `cas.authn.throttle.schedule.cron-expression` (Optional; type `String`): A cron-like expression, extending the usual UN*X definition to include triggers on the second, minute, hour, day of month, month, and day of week. For example, `0 * * * * MON-FRI` means once per minute on weekdays (at the top of the minute - the 0th second) or `0 0 0 * * *` means every day at midnight. * Note that when cron expressions are defined, the start delay and repeat interval settings are must be removed and set to blank.
- `cas.authn.throttle.schedule.cron-time-zone` (Optional; type `String`): A time zone for which the cron expression will be resolved. By default, this attribute is empty (i.e. the scheduler's time zone will be used).
- `cas.authn.throttle.schedule.enabled` (Optional; type `Boolean`; default `true`): Whether scheduler should be enabled to schedule the job to run.
- `cas.authn.throttle.schedule.enabled-on-host` (Optional; type `String`; default `.*`): Overrides `SchedulingProperties#enabled` property value of true if this property does not match hostname of CAS server. This can be useful if deploying CAS with an image in a statefulset where all names are predictable but where having different configurations for different servers is hard. The value can be an exact hostname or a regular expression that will be used to match the hostname.
- `cas.authn.throttle.schedule.repeat-interval` (Optional; type `String`; default `PT2M`): String representation of a repeat interval of re-loading data for a data store implementation. This is the timeout between consecutive job’s executions.
- `cas.authn.throttle.schedule.start-delay` (Optional; type `String`; default `PT15S`): String representation of a start delay of loading data for a data store implementation. This is the delay between scheduler startup and first job’s execution

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

## Actuator Endpoints

The following endpoints are provided by CAS:

`throttles`

CAS endpoint · 2 operations · Not exposed by default

**GET** `throttles`: Get throttled authentication records, optionally only those of a username. (1 param)

Parameters

| Name | In | Type | Description |
| --- | --- | --- | --- |
| `username` | query | – | Return only the records submitted for this username, matched regardless of case |

Response

- **Returns**: `List`
- **Produces**: `application/json`
- **Java method**: `getRecords(String)`
- **Defined in**: `ThrottledSubmissionHandlerEndpoint`

Example request

```bash
curl -u "casuser:password" \
  -H "Accept: application/json" \
  "https://sso.example.org/cas/actuator/throttles?username=..."
```

**DELETE** `throttles`: Clean or release all throttled interceptors or remove a throttled authentication record by key. (2 params)

Parameters

| Name | In | Type | Description |
| --- | --- | --- | --- |
| `clear` | query | – | Whether to clear/remove the records or simply release them |
| `key` | query | – | Selected key for removal |

Response

- **Returns**: nothing
- **Produces**: `application/json`
- **Java method**: `deleteByKeyOrRelease(boolean,String)`
- **Defined in**: `ThrottledSubmissionHandlerEndpoint`

Example request

```bash
curl -u "casuser:password" \
  -X DELETE \
  -H "Accept: application/json" \
  "https://sso.example.org/cas/actuator/throttles?clear=...&key=..."
```

**Enable & expose**

Turn the endpoint on and expose it over the web. One entry covers every operation. By default only `info`, `health` and `status` are exposed.

```properties
management.endpoint.throttles.access=UNRESTRICTED
management.endpoints.web.exposure.include=throttles
```

Endpoints may be mapped to other paths. For example, to serve `health` at `healthcheck`:

```properties
management.endpoints.web.path-mapping.health=healthcheck
```

**Security**

Once exposed, every call goes through [Spring Security](https://spring.io/projects/spring-security), and CAS decides access per endpoint. Without a rule, the special `defaults` endpoint applies, which denies access.

Who may call it

*Authenticated*

```properties
cas.monitor.endpoints.endpoint.throttles.access=AUTHENTICATED
```

*Role*

```properties
cas.monitor.endpoints.endpoint.throttles.access=ROLE
cas.monitor.endpoints.endpoint.throttles.required-roles=ADMIN
```

*IP address*

```properties
cas.monitor.endpoints.endpoint.throttles.access=IP_ADDRESS
cas.monitor.endpoints.endpoint.throttles.required-ip-addresses=127.0.0.1
```

*Anyone*

```properties
cas.monitor.endpoints.endpoint.throttles.access=PERMIT
```

Where credentials are checked

*Basic*

A single user defined in CAS settings. If the name and password are left undefined, CAS generates them at startup.

- `spring.security.user.name` (Third party; type `String`; default `user`): Default user name.
- `spring.security.user.password` (Third party; type `String`): Password for the default user name.
- `spring.security.user.roles` (Third party; type `List<String>`): Granted roles for the default user name.

*JAAS*

- `cas.monitor.endpoints.jaas.login-config` (Optional; type `Resource`): JAAS login resource file.
- `cas.monitor.endpoints.jaas.login-context-name` (Optional; type `String`): The login context name should coincide with a given index in the login config specified. This name is used as the index to the configuration specified in the login config property. ``` JAASTest { org.springframework.security.authentication.jaas.TestLoginModule required; }; ``` In the above example, `JAASTest` should be set as the context name.
- `cas.monitor.endpoints.jaas.refresh-configuration-on-startup` (Optional; type `Boolean`; default `true`): If set, a call to `Configuration#refresh()` will be made by `#configureJaas(Resource)` method.

*LDAP*

- `cas.monitor.endpoints.ldap.base-dn` (Required; type `String`): Base DN to use. There may be scenarios where different parts of a single LDAP tree could be considered as base-dns. Rather than duplicating the LDAP configuration block for each individual base-dn, each entry can be specified and joined together using a special delimiter character. The user DN is retrieved using the combination of all base-dn and DN resolvers in the order defined. DN resolution should fail if multiple DNs are found. Otherwise the first DN found is returned. Usual syntax is: `subtreeA,dc=example,dc=net|subtreeC,dc=example,dc=net`.
- `cas.monitor.endpoints.ldap.bind-credential` (Required; type `String`): The bind credential to use when connecting to LDAP.
- `cas.monitor.endpoints.ldap.bind-dn` (Required; type `String`): The bind DN to use when connecting to LDAP. LDAP connection configuration injected into the LDAP connection pool can be initialized with the following parameters: - `bindDn/bindCredential` provided - Use the provided credentials to bind when initializing connections. - `bindDn/bindCredential` set to `*` - Use a fast-bind strategy to initialize the pool. - `bindDn/bindCredential` set to blank - Skip connection initializing; perform operations anonymously. - SASL mechanism provided - Use the given SASL mechanism to bind when initializing connections.
- `cas.monitor.endpoints.ldap.ldap-url` (Required; type `String`): The LDAP url to the server. More than one may be specified, separated by space and/or comma.
- `cas.monitor.endpoints.ldap.search-filter` (Required; type `String`): User filter to use for searching. Syntax is `cn={user}` or `cn={0}`. You may also provide an external groovy script in the syntax of `file:/path/to/GroovyScript.groovy` to fully build the final filter template dynamically.
- `cas.monitor.endpoints.ldap.type` (Required; type `AbstractLdapAuthenticationProperties.AuthenticationTypes`; default `AUTHENTICATED`): The authentication type. - `AD` - Users authenticate with `sAMAccountName`. - `AUTHENTICATED` - Manager bind/search type of authentication. If {@code} principalAttributePassword} is empty then a user simple bind is done to validate credentials. Otherwise the given attribute is compared with the given `principalAttributePassword` using the `SHA` encrypted value of it. - `ANONYMOUS`: Similar semantics as `AUTHENTICATED` except no `bindDn` and `bindCredential` may be specified to initialize the connection. If `principalAttributePassword` is empty then a user simple bind is done to validate credentials. Otherwise the given attribute is compared with the given `principalAttributePassword` using the `SHA` encrypted value of it. - DIRECT: Direct Bind - Compute user DN from format string and perform simple bind. This is relevant when no search is required to compute the DN needed for a bind operation. Use cases for this type are: 1) All users are under a single branch in the directory, `e.g. ou=Users,dc=example,dc=org.` 2) The username provided on the CAS login form is part of the DN, e.g. `uid=%s,ou=Users,dc=example,dc=org`. Available values are as follows: - `AD`: Active Directory. - `AUTHENTICATED`: Authenticated Search. - `DIRECT`: Direct Bind. - `ANONYMOUS`: Anonymous Search.
- `cas.monitor.endpoints.ldap.allow-multiple-dns` (Optional; type `Boolean`; default `false`): Whether search/query results are allowed to match on multiple DNs, or whether a single unique DN is expected for the result.
- `cas.monitor.endpoints.ldap.allow-multiple-entries` (Optional; type `Boolean`; default `false`): Set if multiple Entries are allowed.
- `cas.monitor.endpoints.ldap.binary-attributes` (Optional; type `List<String>`): Indicate the collection of attributes that are to be tagged and processed as binary attributes by the underlying search resolver.
- `cas.monitor.endpoints.ldap.block-wait-time` (Optional; type `String`; default `PT3S`): The length of time the pool will block. By default the pool will block indefinitely and there is no guarantee that waiting threads will be serviced in the order in which they made their request. This option should be used with a blocking connection pool when you need to control the exact number of connections that can be created
- `cas.monitor.endpoints.ldap.connect-timeout` (Optional; type `String`; default `PT5S`): Sets the maximum amount of time that connects will block.
- `cas.monitor.endpoints.ldap.connection-strategy` (Optional; type `String`): If multiple URLs are provided as the ldapURL this describes how each URL will be processed. - `ACTIVE_PASSIVE` First LDAP will be used for every request unless it fails and then the next shall be used. - `ROUND_ROBIN` For each new connection the next url in the list will be used. - `RANDOM` For each new connection a random LDAP url will be selected. - `DNS_SRV` LDAP urls based on DNS SRV records of the configured/given LDAP url will be used.
- `cas.monitor.endpoints.ldap.deref-aliases` (Optional; type `String`): Define how aliases are de-referenced. Accepted values are: - `NEVER` - `SEARCHING`: dereference when searching the entries beneath the starting point but not when searching for the starting entry. - `FINDING`: dereference when searching for the starting entry but not when searching the entries beneath the starting point. - `ALWAYS`: dereference when searching for the starting entry and when searching the entries beneath the starting point.
- `cas.monitor.endpoints.ldap.disable-pooling` (Optional; type `Boolean`; default `false`): Whether to use a pooled connection factory in components.
- `cas.monitor.endpoints.ldap.dn-format` (Optional; type `String`): Specify the dn format accepted by the AD authenticator, etc. Example format might be `uid=%s,ou=people,dc=example,dc=org`.
- `cas.monitor.endpoints.ldap.enhance-with-entry-resolver` (Optional; type `Boolean`; default `true`): Whether specific search entry resolvers need to be set on the authenticator, or the default should be used.
- `cas.monitor.endpoints.ldap.fail-fast` (Optional; type `Boolean`; default `true`): Attempt to populate the connection pool early on startup and fail quickly if something goes wrong.
- `cas.monitor.endpoints.ldap.follow-referrals` (Optional; type `Boolean`; default `true`): Set if search referrals should be followed.
- `cas.monitor.endpoints.ldap.hostname-verifier` (Optional; type `AbstractLdapProperties.LdapHostnameVerifierOptions`; default `DEFAULT`): Hostname verification options. Available values are as follows: - `DEFAULT`: Default option, forcing verification. - `ANY`: Skip hostname verification and allow all.
- `cas.monitor.endpoints.ldap.idle-time` (Optional; type `String`; default `PT10M`): Removes connections from the pool based on how long they have been idle in the available queue. Prunes connections that have been idle for more than the indicated amount.
- `cas.monitor.endpoints.ldap.keystore` (Optional; type `String`): Path to the keystore used for SSL connections. Typically contains SSL certificates for the LDAP server.
- `cas.monitor.endpoints.ldap.keystore-password` (Optional; type `String`): Keystore password.
- `cas.monitor.endpoints.ldap.keystore-type` (Optional; type `String`): The type of keystore. `PKCS12` or `JKS`. If left blank, defaults to the default keystore type indicated by the underlying Java platform.
- `cas.monitor.endpoints.ldap.ldap-authz.allow-multiple-results` (Optional; type `Boolean`; default `false`): Indicate whether the LDAP search query is allowed to return multiple entries.
- `cas.monitor.endpoints.ldap.ldap-authz.base-dn` (Optional; type `String`): Base DN to start the search.
- `cas.monitor.endpoints.ldap.ldap-authz.group-attribute` (Optional; type `String`): Attribute expected to be found on the entry resulting from the group search whose value is going to be used to construct roles. The final value is always prefixed with `#groupPrefix`. This is useful in scenarios where you wish to grant access to a resource to all users who a member of a given group.
- `cas.monitor.endpoints.ldap.ldap-authz.group-base-dn` (Optional; type `String`): Base DN to start the search looking for groups.
- `cas.monitor.endpoints.ldap.ldap-authz.group-filter` (Optional; type `String`): Search filter to begin looking for groups.
- `cas.monitor.endpoints.ldap.ldap-authz.group-prefix` (Optional; type `String`): A prefix that is prepended to the group attribute value to construct an authorized role.
- `cas.monitor.endpoints.ldap.ldap-authz.role-attribute` (Optional; type `String`; default `uugid`): Attribute expected to be found on the entry whose value is going to be used to construct roles. The final value is always prefixed with `#rolePrefix`. This is useful in scenarios where you wish to grant access to a resource to all users who carry a special attribute.
- `cas.monitor.endpoints.ldap.ldap-authz.role-prefix` (Optional; type `String`; default `ROLE_`): Prefix for the role.
- `cas.monitor.endpoints.ldap.ldap-authz.search-filter` (Optional; type `String`): LDAP search filter to locate accounts.
- `cas.monitor.endpoints.ldap.max-pool-size` (Optional; type `Integer`; default `10`): Maximum LDAP connection pool size which the pool can use to grow.
- `cas.monitor.endpoints.ldap.min-pool-size` (Optional; type `Integer`; default `3`): Minimum LDAP connection pool size. Size the pool should be initialized to and pruned to
- `cas.monitor.endpoints.ldap.name` (Optional; type `String`): Name of the LDAP handler.
- `cas.monitor.endpoints.ldap.page-size` (Optional; type `Integer`; default `0`): Request that the server return results in batches of a specific size. See [RFC 2696](http://www.ietf.org/rfc/rfc2696.txt). This control is often used to work around server result size limits. A negative/zero value disables paged requests.
- `cas.monitor.endpoints.ldap.pool-passivator` (Optional; type `String`; default `BIND`): You may receive unexpected LDAP failures, when CAS is configured to authenticate using `DIRECT` or `AUTHENTICATED` types and LDAP is locked down to not allow anonymous binds/searches. Every second attempt with a given LDAP connection from the pool would fail if it was on the same connection as a failed login attempt, and the regular connection validator would similarly fail. When a connection is returned back to a pool, it still may contain the principal and credentials from the previous attempt. Before the next bind attempt using that connection, the validator tries to validate the connection again but fails because it’s no longer trying with the configured bind credentials but with whatever user DN was used in the previous step. Given the validation failure, the connection is closed and CAS would deny access by default. Passivators attempt to reconnect to LDAP with the configured bind credentials, effectively resetting the connection to what it should be after each bind request. Furthermore if you are seeing errors in the logs that resemble a 'Operation exception encountered, reopening connection' type of message, this usually is an indication that the connection pool’s validation timeout established and created by CAS is greater than the timeout configured in the LDAP server, or more likely, in the load balancer in front of the LDAP servers. You can adjust the LDAP server session’s timeout for connections, or you can teach CAS to use a validity period that is equal or less than the LDAP server session’s timeout. Accepted values are: - `NONE`: No passivation takes place. - `BIND`: The default behavior which passivates a connection by performing a bind operation on it. This option requires the availability of bind credentials when establishing connections to LDAP.
- `cas.monitor.endpoints.ldap.principal-attribute-password` (Optional; type `String`): If principalAttributePassword is empty then a user simple bind is done to validate credentials otherwise the given attribute is compared with the given principalAttributePassword using the SHA encrypted value of it. For the anonymous authentication type, if principalAttributePassword is empty then a user simple bind is done to validate credentials otherwise the given attribute is compared with the given principalAttributePassword using the SHA encrypted value of it.
- `cas.monitor.endpoints.ldap.prune-period` (Optional; type `String`; default `PT2H`): Removes connections from the pool based on how long they have been idle in the available queue. Run the pruning process at the indicated interval.
- `cas.monitor.endpoints.ldap.resolve-from-attribute` (Optional; type `String`): If this attribute is set, the value found in the first attribute value will be used in place of the DN.
- `cas.monitor.endpoints.ldap.response-timeout` (Optional; type `String`; default `PT5S`): Duration of time to wait for responses.
- `cas.monitor.endpoints.ldap.sasl-authorization-id` (Optional; type `String`): SASL authorization id.
- `cas.monitor.endpoints.ldap.sasl-mechanism` (Optional; type `String`): The SASL mechanism.
- `cas.monitor.endpoints.ldap.sasl-mutual-auth` (Optional; type `Boolean`): Whether SASL mutual authentication is enabled.
- `cas.monitor.endpoints.ldap.sasl-quality-of-protection` (Optional; type `String`): SASL quality of protected.
- `cas.monitor.endpoints.ldap.sasl-realm` (Optional; type `String`): The SASL realm.
- `cas.monitor.endpoints.ldap.sasl-security-strength` (Optional; type `String`): SASL security strength.
- `cas.monitor.endpoints.ldap.search-entry-handlers` (Optional; type `List<LdapSearchEntryHandlersProperties>`): Search handlers.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].case-change.attribute-name-case-change` (Optional; type `String`): The Attribute name case change.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].case-change.attribute-names` (Optional; type `List<String>`): The Attribute names.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].case-change.attribute-value-case-change` (Optional; type `String`): The Attribute value case change.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].case-change.dn-case-change` (Optional; type `String`): The Dn case change.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].dn-attribute.add-if-exists` (Optional; type `boolean`): The Add if exists.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].dn-attribute.dn-attribute-name` (Optional; type `String`; default `entryDN`): The Dn attribute name.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].merge-attribute.attribute-names` (Optional; type `List<String>`): The Attribute names.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].merge-attribute.merge-attribute-name` (Optional; type `String`): The Merge attribute name.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].primary-group-id.base-dn` (Optional; type `String`): The Base dn.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].primary-group-id.group-filter` (Optional; type `String`; default `(&(objectClass=group)(objectSid={0}))`): The Group filter.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].recursive.merge-attributes` (Optional; type `List<String>`): The Merge attributes.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].recursive.search-attribute` (Optional; type `String`): The Search attribute.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].search-referral.limit` (Optional; type `int`; default `10`): The default referral limit.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].search-result.limit` (Optional; type `int`; default `10`): The default referral limit.
- `cas.monitor.endpoints.ldap.search-entry-handlers[0].type` (Optional; type `LdapSearchEntryHandlersProperties.SearchEntryHandlerTypes`): The type of search entry handler to choose. Available values are as follows: - `FOLLOW_SEARCH_REFERRAL`: Provides handling of an ldap referral for search operations. - `FOLLOW_SEARCH_RESULT_REFERENCE`: Provides handling of an ldap continuation reference for search operations. - `ACTIVE_DIRECTORY`: Process the entry results fetched from active directory and check for account status controls for disabled/expired accounts, etc. - `OBJECT_GUID`: Object guid search entry handler. Handles the `objectGUID` attribute fetching and conversion. - `OBJECT_SID`: Object sid search entry handler. Handles the `objectSid` attribute fetching and conversion. - `CASE_CHANGE`: Case change search entry handler. Provides the ability to modify the case of search entry DNs, attribute names, and attribute values. - `DN_ATTRIBUTE_ENTRY`: DN attribute entry handler. Adds the entry DN as an attribute to the result set. Provides a client side implementation of RFC 5020. - `MERGE`: Merge search entry handler. Merges the values of one or more attributes into a single attribute. - `PRIMARY_GROUP`: Primary group search handler. Constructs the primary group SID and then searches for that group and puts it's DN in the `memberOf` attribute of the original search entry. - `RANGE_ENTRY`: Range entry search handler. Rewrites attributes returned from Active Directory to include all values by performing additional searches. - `RECURSIVE_ENTRY`: Recursive entry search handler. This recursively searches based on a supplied attribute and merges those results into the original entry. - `MERGE_ENTRIES`: Merge entries handler. Merges the values of one or more attributes in all entries into a single attribute. The merged attribute may or may not already exist on the entry. If it does exist it's existing values will remain intact.
- `cas.monitor.endpoints.ldap.subtree-search` (Optional; type `Boolean`; default `true`): Whether subtree searching is allowed.
- `cas.monitor.endpoints.ldap.trust-certificates` (Optional; type `String`): Path of the trust certificates to use for the SSL connection. Ignores keystore-related settings when activated and used.
- `cas.monitor.endpoints.ldap.trust-manager` (Optional; type `String`): Trust Manager options. Trust managers are responsible for managing the trust material that is used when making LDAP trust decisions, and for deciding whether credentials presented by a peer should be accepted. Accepted values are: - `DEFAULT`: Enable and force the default JVM trust managers. - `ANY`: Trust any client or server.
- `cas.monitor.endpoints.ldap.trust-store` (Optional; type `String`): Path to the keystore used to determine which certificates or certificate authorities should be trusted. Used when connecting to an LDAP server via LDAPS or startTLS connection. If left blank, the default truststore for the Java runtime is used.
- `cas.monitor.endpoints.ldap.trust-store-password` (Optional; type `String`): Password needed to open the truststore.
- `cas.monitor.endpoints.ldap.trust-store-type` (Optional; type `String`): The type of trust keystore that determines which certificates or certificate authorities are trusted. Types depend on underlying java platform, typically `PKCS12` or `JKS`. If left blank, defaults to the default keystore type indicated by the underlying Java platform.
- `cas.monitor.endpoints.ldap.use-start-tls` (Optional; type `Boolean`; default `false`): Whether TLS should be used and enabled when establishing the connection.
- `cas.monitor.endpoints.ldap.validate-on-checkout` (Optional; type `Boolean`; default `true`): Whether connections should be validated when loaned out from the pool.
- `cas.monitor.endpoints.ldap.validate-period` (Optional; type `String`; default `PT5M`): Period at which pool should be validated.
- `cas.monitor.endpoints.ldap.validate-periodically` (Optional; type `Boolean`; default `true`): Whether connections should be validated periodically when the pool is idle.
- `cas.monitor.endpoints.ldap.validate-timeout` (Optional; type `String`; default `PT5S`): Period at which validation operations may time out.
- `cas.monitor.endpoints.ldap.validator.attribute-name` (Optional; type `String`; default `objectClass`): Attribute name to use for the compare validator.
- `cas.monitor.endpoints.ldap.validator.attribute-value` (Optional; type `String`; default `top`): Attribute values to use for the compare validator.
- `cas.monitor.endpoints.ldap.validator.base-dn` (Optional; type `String`): Base DN to use for the search request of the search validator.
- `cas.monitor.endpoints.ldap.validator.dn` (Optional; type `String`): DN to compare to use for the compare validator.
- `cas.monitor.endpoints.ldap.validator.scope` (Optional; type `String`; default `OBJECT`): Search scope to use for the search request of the search validator.
- `cas.monitor.endpoints.ldap.validator.search-filter` (Optional; type `String`; default `(objectClass=*)`): Search filter to use for the search request of the search validator.
- `cas.monitor.endpoints.ldap.validator.type` (Optional; type `String`; default `search`): Determine the LDAP validator type. The following LDAP validators can be used to test connection health status: - `search`: Validates a connection is healthy by performing a search operation. Validation is considered successful if the search result size is greater than zero. - `none`: No validation takes place. - `compare`: Validates a connection is healthy by performing a compare operation.

**LDAP & Active Directory**

**LDAP Scriptable Search Filter**

LDAP search filters can point to an external Groovy script to dynamically construct the final filter template.

The script itself may be designed as:

```groovy
import org.ldaptive.*
import org.springframework.context.*

def run(Object[] args) {
    def (filter,parameters,applicationContext,logger) = args

    logger.info("Configuring LDAP filter")
    filter.setFilter("uid=something")
}
```

The following parameters are passed to the script:

| Parameter | Description |
| --- | --- |
| `filter` | `FilterTemplate` to be updated by the script and used for the LDAP query. |
| `parameters` | Map of query parameters which may be used to construct the final filter. |
| `applicationContext` | Reference to the Spring `ApplicationContext` reference. |
| `logger` | The object responsible for issuing log messages such as `logger.info(...)`. |

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development//integration/Apache-Groovy-Scripting.html).

*JDBC*

- `cas.monitor.endpoints.jdbc.driver-class` (Required; type `String`; default `org.hsqldb.jdbcDriver`): The JDBC driver used to connect to the database.
- `cas.monitor.endpoints.jdbc.password` (Required; type `String`): The database connection password.
- `cas.monitor.endpoints.jdbc.password-encoder.encoding-algorithm` (Required; type `String`): The encoding algorithm to use such as `MD5`. Relevant when the type used is `DEFAULT` or `GLIBC_CRYPT`. When used with `PasswordEncoderTypes#PBKDF2`, it should be one of `PBKDF2WithHmacSHA1`, `PBKDF2WithHmacSHA256` or `PBKDF2WithHmacSHA512`.
- `cas.monitor.endpoints.jdbc.password-encoder.type` (Required; type `String`; default `NONE`): Define the password encoder type to use. Type may be specified as blank or `NONE` to disable password encoding. It may also refer to a fully-qualified class name that implements the Spring Security's `PasswordEncoder` interface if you wish you define your own encoder. The following types may be used: - `NONE`: No password encoding (i.e. plain-text) takes place. - `DEFAULT`: Use the `DefaultPasswordEncoder` of CAS. For message-digest algorithms via `character-encoding` and `encoding-algorithm`. - `BCRYPT`: Use the `BCryptPasswordEncoder` based on the strength provided and an optional secret. - `SCRYPT`: Use the `SCryptPasswordEncoder`. - `PBKDF2`: Use the `Pbkdf2PasswordEncoder` based on the strength provided and an optional secret. - `STANDARD`: Use the `StandardPasswordEncoder` based on the secret provided. - `SSHA`: Use the `LdapShaPasswordEncoder` supports Ldap SHA and SSHA (salted-SHA). The values are base-64 encoded and have the label {SHA} or {SSHA} prepended to the encoded hash. - `GLIBC_CRYPT`: Use the `GlibcCryptPasswordEncoder` based on the `encoding-algorithm`, strength provided and an optional secret. - `org.example.MyEncoder`: An implementation of `PasswordEncoder` of your own choosing. - `file:///path/to/script.groovy`: Path to a Groovy script charged with handling password encoding operations.
- `cas.monitor.endpoints.jdbc.url` (Required; type `String`; default `jdbc:hsqldb:mem:cas-hsql-database`): The database connection URL.
- `cas.monitor.endpoints.jdbc.user` (Required; type `String`; default `sa`): The database user. The database user must have sufficient permissions to be able to handle schema changes and updates, when needed.
- `cas.monitor.endpoints.jdbc.autocommit` (Optional; type `Boolean`; default `false`): The default auto-commit behavior of connections in the pool. Determined whether queries such as update/insert should be immediately executed without waiting for an underlying transaction.
- `cas.monitor.endpoints.jdbc.batch-size` (Optional; type `Integer`; default `100`): A non-zero value enables use of JDBC2 batch updates by Hibernate. e.g. recommended values between 5 and 30.
- `cas.monitor.endpoints.jdbc.connection-timeout` (Optional; type `String`; default `PT30S`): Indicates the maximum number of milliseconds that the service can wait to obtain a connection.
- `cas.monitor.endpoints.jdbc.data-source-name` (Optional; type `String`): Attempts to do a JNDI data source look up for the data source name specified. Will attempt to locate the data source object as is.
- `cas.monitor.endpoints.jdbc.ddl-auto` (Optional; type `String`; default `update`): Hibernate feature to automatically validate and exports DDL to the schema. By default, creates and drops the schema automatically when a session is starts and ends. Setting the value to `validate` or `none` may be more desirable for production, but any of the following options can be used: - `validate`: Validate the schema, but make no changes to the database. - `update`: Update the schema. - `create`: Create the schema, destroying previous data. - `create-drop`: Drop the schema at the end of the session. - `none`: Do nothing. Note that during a version migration where any schema has changed `create-drop` will result in the loss of all data as soon as CAS is started. For transient data like tickets this is probably not an issue, but in cases like the audit table important data could be lost. Using `update`, while safe for data, is confirmed to result in invalid database state. `validate` or `none` settings are likely the only safe options for production use. For more info, [see this](http://docs.spring.io/spring-framework/docs/current/javadoc-api).
- `cas.monitor.endpoints.jdbc.default-catalog` (Optional; type `String`): Qualifies unqualified table names with the given catalog in generated SQL.
- `cas.monitor.endpoints.jdbc.default-schema` (Optional; type `String`): Qualify unqualified table names with the given schema/tablespace in generated SQL.
- `cas.monitor.endpoints.jdbc.dialect` (Optional; type `String`; default `org.hibernate.dialect.HSQLDialect`): The database dialect is a configuration setting for platform independent software (JPA, Hibernate, etc) which allows such software to translate its generic SQL statements into vendor specific DDL, DML.
- `cas.monitor.endpoints.jdbc.fail-fast-timeout` (Optional; type `Long`; default `1`): Set the pool initialization failure timeout. - Any value greater than zero will be treated as a timeout for pool initialization. The calling thread will be blocked from continuing until a successful connection to the database, or until the timeout is reached. If the timeout is reached, then a `PoolInitializationException` will be thrown. - A value of zero will *not* prevent the pool from starting in the case that a connection cannot be obtained. However, upon start the pool will attempt to obtain a connection and validate that the `connectionTestQuery` and `connectionInitSql` are valid. If those validations fail, an exception will be thrown. If a connection cannot be obtained, the validation is skipped and the pool will start and continue to try to obtain connections in the background. This can mean that callers to `DataSource#getConnection()` may encounter exceptions. - A value less than zero will *not* bypass any connection attempt and validation during startup, and therefore the pool will start immediately. The pool will continue to try to obtain connections in the background. This can mean that callers to `DataSource#getConnection()` may encounter exceptions. Note that if this timeout value is greater than or equal to zero (0), and therefore an initial connection validation is performed, this timeout does not override the `connectionTimeout` or `validationTimeout`; they will be honored before this timeout is applied. The default value is one millisecond.
- `cas.monitor.endpoints.jdbc.fetch-size` (Optional; type `Integer`; default `100`): Used to specify number of rows to be fetched in a select query.
- `cas.monitor.endpoints.jdbc.generate-statistics` (Optional; type `Boolean`; default `false`): Allow hibernate to generate query statistics.
- `cas.monitor.endpoints.jdbc.health-query` (Optional; type `String`): The SQL query to be executed to test the validity of connections. This is for "legacy" databases that do not support the JDBC4 `Connection.isValid()` API.
- `cas.monitor.endpoints.jdbc.idle-timeout` (Optional; type `String`; default `PT10M`): Controls the maximum amount of time that a connection is allowed to sit idle in the pool.
- `cas.monitor.endpoints.jdbc.isolate-internal-queries` (Optional; type `Boolean`; default `false`): This property determines whether data source isolates internal pool queries, such as the connection alive test, in their own transaction. Since these are typically read-only queries, it is rarely necessary to encapsulate them in their own transaction. This property only applies if `#autocommit` is disabled.
- `cas.monitor.endpoints.jdbc.isolation-level-name` (Optional; type `String`; default `ISOLATION_READ_COMMITTED`): Defines the isolation level for transactions. @see org.springframework.transaction.TransactionDefinition
- `cas.monitor.endpoints.jdbc.leak-threshold` (Optional; type `String`; default `PT6S`): Controls the amount of time that a connection can be out of the pool before a message is logged indicating a possible connection leak.
- `cas.monitor.endpoints.jdbc.password-encoder.character-encoding` (Optional; type `String`; default `UTF-8`): The encoding algorithm to use such as 'UTF-8'. Relevant when the type used is `DEFAULT`.
- `cas.monitor.endpoints.jdbc.password-encoder.hash-length` (Optional; type `Integer`; default `16`): When used by `PasswordEncoderTypes#ARGON2`, it indicates the hash strength/length.
- `cas.monitor.endpoints.jdbc.password-encoder.iterations` (Optional; type `Integer`; default `310000`): When used by `PasswordEncoderTypes#PBKDF2`, it indicates the required number of iterations.
- `cas.monitor.endpoints.jdbc.password-encoder.secret` (Optional; type `String`): Secret to use with `PasswordEncoderTypes#STANDARD`, `PasswordEncoderTypes#PBKDF2`, `PasswordEncoderTypes#BCRYPT`, `PasswordEncoderTypes#GLIBC_CRYPT` password encoders. Secret usually is an optional setting.
- `cas.monitor.endpoints.jdbc.password-encoder.strength` (Optional; type `Integer`; default `16`): Strength or number of iterations to use for password hashing. Usually relevant when dealing with `PasswordEncoderTypes#BCRYPT`, `PasswordEncoderTypes#PBKDF2` or `PasswordEncoderTypes#GLIBC_CRYPT`. When used by `PasswordEncoderTypes#ARGON2` or `PasswordEncoderTypes#PBKDF2`, it indicates the salt strength.
- `cas.monitor.endpoints.jdbc.physical-naming-strategy-class-name` (Optional; type `String`; default `org.apereo.cas.hibernate.CasHibernatePhysicalNamingStrategy`): Fully-qualified name of the class that can control the physical naming strategy of hibernate.
- `cas.monitor.endpoints.jdbc.pool.keep-alive-time` (Optional; type `String`; default `0`): This property controls the keepalive interval for a connection in the pool. An in-use connection will never be tested by the keepalive thread, only when it is idle will it be tested. Default is zero, which disables this feature.
- `cas.monitor.endpoints.jdbc.pool.max-size` (Optional; type `Integer`; default `18`): Controls the maximum number of connections to keep in the pool, including both idle and in-use connections.
- `cas.monitor.endpoints.jdbc.pool.max-wait` (Optional; type `String`; default `PT2S`): Sets the maximum time in seconds that this data source will wait while attempting to connect to a database. A value of zero specifies that the timeout is the default system timeout if there is one; otherwise, it specifies that there is no timeout.
- `cas.monitor.endpoints.jdbc.pool.maximum-lifetime` (Optional; type `String`; default `PT10M`): This property controls the maximum lifetime of a connection in the pool. When a connection reaches this timeout, even if recently used, it will be retired from the pool. An in-use connection will never be retired, only when it is idle will it be removed.
- `cas.monitor.endpoints.jdbc.pool.min-size` (Optional; type `Integer`; default `6`): Controls the minimum size that the pool is allowed to reach, including both idle and in-use connections.
- `cas.monitor.endpoints.jdbc.pool.name` (Optional; type `String`): Set the name of the connection pool. This is primarily used for the MBean to uniquely identify the pool configuration.
- `cas.monitor.endpoints.jdbc.pool.suspension` (Optional; type `Boolean`; default `false`): Whether or not pool suspension is allowed. There is a performance impact when pool suspension is enabled. Unless you need it (for a redundancy system for example) do not enable it.
- `cas.monitor.endpoints.jdbc.pool.timeout-millis` (Optional; type `Long`; default `1000`): The maximum number of milliseconds that the pool will wait for a connection to be validated as alive.
- `cas.monitor.endpoints.jdbc.propagation-behavior-name` (Optional; type `String`; default `PROPAGATION_REQUIRED`): Defines the propagation behavior for transactions. @see org.springframework.transaction.TransactionDefinition
- `cas.monitor.endpoints.jdbc.properties` (Optional; type `Map<String,String>`): Additional settings provided by Hibernate (or the connection provider) in form of key-value pairs.
- `cas.monitor.endpoints.jdbc.query` (Optional; type `String`): Query to execute in order to authenticate users via JDBC. Example: `SELECT username,password,enabled FROM users WHERE username=?`
- `cas.monitor.endpoints.jdbc.read-only` (Optional; type `Boolean`; default `false`): Configures the Connections to be added to the pool as read-only Connections.
- `cas.monitor.endpoints.jdbc.role-prefix` (Optional; type `String`): Prefix to add to the role.

**Hibernate & JDBC**

Control global properties that are relevant to Hibernate, when CAS attempts to employ and utilize database resources, connections and queries.

- `cas.jdbc.gen-ddl` (Optional; type `Boolean`; default `true`): Whether to generate DDL after the EntityManagerFactory has been initialized creating/updating all relevant tables.
- `cas.jdbc.physical-table-names` (Optional; type `Map<String,String>`): Indicate a physical table name to be used by the hibernate naming strategy in case table names need to be customized for the specific type of database. The key here indicates the CAS-provided table name and the value is the translate physical name for the database. If a match is not found for the CAS-provided table name, then that name will be used by default.
- `cas.jdbc.show-sql` (Optional; type `Boolean`; default `false`): Whether SQL queries should be displayed in the console/logs.

**Password encoding**

If you need to design your own password encoding scheme where the type is specified as a fully qualified Java class name, the structure of the class would be similar to the following:

```java
package org.example.cas;

import org.springframework.security.crypto.codec.*;
import org.springframework.security.crypto.password.*;

public class MyEncoder extends AbstractPasswordEncoder {
    @Override
    protected byte[] encode(CharSequence rawPassword, byte[] salt) {
        return ...
    }
}
```

If you need to design your own password encoding scheme where the type is specified as a path to a Groovy script, the structure of the script would be similar to the following:

```groovy
import java.util.*

byte[] run(final Object... args) {
    def (rawPassword,generatedSalt,logger,applicationContext) = args
    logger.debug("Encoding password...")
    return ...
}

Boolean matches(final Object... args) {
    def (rawPassword,encodedPassword,logger,applicationContext) = args
    logger.debug("Does match or not ?");
    return ...
```

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development//integration/Apache-Groovy-Scripting.html).

*JSON*

A static list of users, passwords and roles in a JSON file. Supported password encodings are `{sha512}`, `{sha256}`, `{bcrypt}`, `{noop}`, `{pbkdf2}`, `{scrypt}` and `{argon2}`.

```json
[
    {
        "username": "casuser",
        "password": "{sha512}<hashed-password>",
        "authorities": [ "ROLE_ADMIN" ]
    }
]
```

- `cas.monitor.endpoints.json.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.

**Settings**

- `cas.monitor.endpoints.endpoint.throttles.access` (Optional; type `ActuatorEndpointProperties.EndpointAccessLevel`; default `DENY`): Define the security access level of the endpoint. Available values are as follows: - `PERMIT`: Allow open access to the endpoint. - `ANONYMOUS`: Allow anonymous access to the endpoint. - `DENY`: Block access to the endpoint. - `AUTHENTICATED`: Require authenticated access to the endpoint. - `ROLE`: Require authenticated access to the endpoint along with a role requirement. - `AUTHORITY`: Require authenticated access to the endpoint along with an authority requirement. - `IP_ADDRESS`: Require authenticated access to the endpoint using a collection of IP addresses.
- `cas.monitor.endpoints.endpoint.throttles.required-authorities` (Optional; type `List<String>`): Required user authorities.
- `cas.monitor.endpoints.endpoint.throttles.required-ip-addresses` (Optional; type `List<String>`): Required IP addresses. CIDR ranges are accepted.
- `cas.monitor.endpoints.endpoint.throttles.required-roles` (Optional; type `List<String>`): Required user roles.
- `cas.monitor.endpoints.form-login-enabled` (Optional; type `Boolean`; default `false`): Control whether access to endpoints can be controlled via form-based login over the web via a special admin login endpoint.

**Spring Boot settings**

- `management.endpoint.health.access` (Third party; type `Access`; default `unrestricted`): Permitted level of access for the health endpoint.
- `management.endpoint.health.cache.time-to-live` (Third party; type `Duration`; default `0ms`): Maximum time that a response can be cached.
- `management.endpoint.health.group` (Third party; type `Map<String,HealthEndpointProperties.Group>`): Health endpoint groups.
- `management.endpoint.health.logging.slow-indicator-threshold` (Third party; type `Duration`; default `10s`): Threshold after which a warning will be logged for slow health indicators.
- `management.endpoint.health.probes.add-additional-paths` (Third party; type `Boolean`; default `false`): Whether to make the liveness and readiness health groups available on the main server port.
- `management.endpoint.health.probes.enabled` (Third party; type `Boolean`; default `true`): Whether to enable liveness and readiness probes.
- `management.endpoint.health.roles` (Third party; type `Set<String>`): Roles used to determine whether a user is authorized to be shown details. When empty, all authenticated users are authorized.
- `management.endpoint.health.show-components` (Third party; type `Show`): When to show components. If not specified the 'show-details' setting will be used.
- `management.endpoint.health.show-details` (Third party; type `Show`; default `never`): When to show full health details.
- `management.endpoint.health.status.http-mapping` (Third party; type `Map<String,Integer>`): Mapping of health statuses to HTTP status codes. By default, registered health statuses map to sensible defaults (for example, UP maps to 200).
- `management.endpoint.health.status.order` (Third party; type `List<String>`; default `["DOWN", "OUT_OF_SERVICE", "UP", "UNKNOWN"]`): List of health statuses in order of severity.
- `management.endpoint.health.validate-group-membership` (Third party; type `Boolean`; default `true`): Whether to validate health group membership on startup. Validation fails if a group includes or excludes a health contributor that does not exist.
- `management.endpoints.access.default` (Third party; type `Access`): Default access level for all endpoints.
- `management.endpoints.access.max-permitted` (Third party; default `unrestricted`): Maximum level of endpoint access that is permitted. Caps an endpoint's individual access level (management.endpoint.\<id>.access) and the default access (management.endpoints.access.default).'
- `management.endpoints.enabled-by-default` (Third party; type `Boolean`; deprecation `WARNING`, replaced by `management.endpoints.access.default`): Whether to enable or disable all endpoints by default.
- `management.endpoints.jackson.isolated-json-mapper` (Third party; type `Boolean`; default `true`): Whether to use an isolated JsonMapper to serialize endpoint JSON.
- `management.endpoints.jackson2.isolated-object-mapper` (Third party; type `Boolean`; deprecation `WARNING`, no replacement; default `true`): Whether to use an isolated object mapper to serialize endpoint JSON.
- `management.endpoints.jmx.domain` (Third party; type `String`; default `org.springframework.boot`): Endpoints JMX domain name. Fallback to 'spring.jmx.default-domain' if set.
- `management.endpoints.jmx.exposure.exclude` (Third party; type `Set<String>`): Endpoint IDs that should be excluded or '*' for all.
- `management.endpoints.jmx.exposure.include` (Third party; type `Set<String>`; default `health`): Endpoint IDs that should be included or '*' for all.
- `management.endpoints.jmx.static-names` (Third party; type `Properties`): Additional static properties to append to all ObjectNames of MBeans representing Endpoints.
- `management.endpoints.jmx.unique-names` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `spring.jmx.unique-names`): Whether unique runtime object names should be ensured.
- `management.endpoints.migrate-legacy-ids` (Third party; type `Boolean`; default `false`): Whether to transparently migrate legacy endpoint IDs.
- `management.endpoints.web.base-path` (Third party; type `String`; default `/actuator`): Base path for Web endpoints. Relative to the servlet context path (server.servlet.context-path) or WebFlux base path (spring.webflux.base-path) when the management server is sharing the main server port. Relative to the management server base path (management.server.base-path) when a separate management server port (management.server.port) is configured.
- `management.endpoints.web.cors.allow-credentials` (Third party; type `Boolean`): Whether credentials are supported. When not set, credentials are not supported.
- `management.endpoints.web.cors.allowed-headers` (Third party; type `List<String>`): List of headers to allow in a request. '*' allows all headers.
- `management.endpoints.web.cors.allowed-methods` (Third party; type `List<String>`): List of methods to allow. '*' allows all methods. When not set, defaults to GET.
- `management.endpoints.web.cors.allowed-origin-patterns` (Third party; type `List<String>`): List of origin patterns to allow. Unlike allowed origins which only supports '*', origin patterns are more flexible (for example 'https://*.example.com') and can be used when credentials are allowed. When no allowed origin patterns or allowed origins are set, CORS support is disabled.
- `management.endpoints.web.cors.allowed-origins` (Third party; type `List<String>`): List of origins to allow. '*' allows all origins. When credentials are allowed, '*' cannot be used and origin patterns should be configured instead. When no allowed origins or allowed origin patterns are set, CORS support is disabled.
- `management.endpoints.web.cors.exposed-headers` (Third party; type `List<String>`): List of headers to include in a response.
- `management.endpoints.web.cors.max-age` (Third party; type `Duration`; default `1800s`): How long the response from a pre-flight request can be cached by clients. If a duration suffix is not specified, seconds will be used.
- `management.endpoints.web.discovery.enabled` (Third party; type `Boolean`; default `true`): Whether the discovery page is enabled.
- `management.endpoints.web.exposure.exclude` (Third party; type `Set<String>`): Endpoint IDs that should be excluded or '*' for all.
- `management.endpoints.web.exposure.include` (Third party; type `Set<String>`; default `["health"]`): Endpoint IDs that should be included or '*' for all.
- `management.endpoints.web.path-mapping` (Third party; type `Map<String,String>`): Mapping between endpoint IDs and the path that should expose them.
- `management.health.binders.enabled` (Third party; type `Boolean`; default `true`): Allows to enable/disable binder's' health indicators. If you want to disable health indicator completely, then set it to `false`.
- `management.health.db.enabled` (Third party; type `Boolean`; default `true`): Whether to enable database health check.
- `management.health.db.ignore-routing-data-sources` (Third party; type `Boolean`; default `false`): Whether to ignore AbstractRoutingDataSources when creating database health indicators.
- `management.health.defaults.enabled` (Third party; type `Boolean`; default `true`): Whether to enable default health indicators.
- `management.health.diskspace.enabled` (Third party; type `Boolean`; default `true`): Whether to enable disk space health check.
- `management.health.diskspace.path` (Third party; type `File`): Path used to compute the available disk space.
- `management.health.diskspace.threshold` (Third party; type `DataSize`; default `10MB`): Minimum disk space that should be available.
- `management.health.influxdb.enabled` (Third party; type `Boolean`; deprecation `ERROR`, no replacement)
- `management.health.livenessstate.enabled` (Third party; type `Boolean`; default `false`): Whether to enable liveness state health check.
- `management.health.mail.enabled` (Third party; type `Boolean`; default `true`): Whether to enable Mail health check.
- `management.health.mongo.enabled` (Third party; deprecation `ERROR`, replaced by `management.health.mongodb.enabled`)
- `management.health.mongodb.enabled` (Third party; type `Boolean`; default `true`): Whether to enable MongoDB health check.
- `management.health.ping.enabled` (Third party; type `Boolean`; default `true`): Whether to enable ping health check.
- `management.health.probes.enabled` (Third party; type `Boolean`; deprecation `ERROR`, replaced by `management.endpoint.health.probes.enabled`; default `false`): Whether to enable liveness and readiness probes.
- `management.health.pubsub.enabled` (Third party; type `Boolean`; default `true`): Whether to enable the Pub/Sub health indicator when used with Spring Boot Actuator.
- `management.health.rabbit.enabled` (Third party; type `Boolean`; default `true`): Whether to enable RabbitMQ health check.
- `management.health.readinessstate.enabled` (Third party; type `Boolean`; default `false`): Whether to enable readiness state health check.
- `management.health.redis.enabled` (Third party; type `Boolean`; default `true`): Whether to enable Redis health check.
- `management.health.refresh.enabled` (Third party; type `Boolean`; default `true`): Enable the health endpoint for the refresh scope.
- `management.health.ssl.certificate-validity-warning-threshold` (Third party; type `Duration`; default `14d`): If an SSL Certificate will be invalid within the time span defined by this threshold, it should trigger a warning.
- `management.health.ssl.enabled` (Third party; type `Boolean`; default `true`): Whether to enable SSL certificate health check.
- `management.health.zookeeper.enabled` (Third party; type `Boolean`; default `true`): Enable the health endpoint for zookeeper.

**Troubleshooting**

- `404`: the endpoint is not enabled or not exposed; check the *Enable & expose* tab.
- `401` or `403`: the access rule or the credentials rejected the call; check the *Security* tab.

For more detail, raise these log levels in the log4j configuration:

```xml
<Logger name="org.apereo.cas.throttle" level="debug" additivity="false">
    <AppenderRef ref="console" />
    <AppenderRef ref="file" />
</Logger>
<Logger name="org.springframework.security" level="debug" additivity="false">
    <AppenderRef ref="console" />
    <AppenderRef ref="file" />
</Logger>
```

## Throttling Strategies

The following throttling strategies are offered by CAS.

| Storage | Description |
| --- | --- |
| IP Address | Uses a memory map to prevent successive failed login attempts from the same IP address. |
| IP Address and Username | Uses a memory map to prevent successive failed login attempts for a username from the same IP address. |
| JDBC | [See this guide](https://apereo.github.io/cas/development/authentication/Configuring-Authentication-Throttling-JDBC.html). |
| MongoDb | [See this guide](https://apereo.github.io/cas/development/authentication/Configuring-Authentication-Throttling-MongoDb.html). |
| Redis | [See this guide](https://apereo.github.io/cas/development/authentication/Configuring-Authentication-Throttling-Redis.html). |
| Hazelcast | [See this guide](https://apereo.github.io/cas/development/authentication/Configuring-Authentication-Throttling-Hazelcast.html). |

## High Availability

All of the throttling components are suitable for a CAS deployment that satisfies the [recommended HA architecture](https://apereo.github.io/cas/development/high_availability/High-Availability-Guide.html). In particular deployments with multiple CAS nodes behind a load balancer configured with session affinity can use either in-memory or *inspektr* components. It is instructive to discuss the rationale. Since load balancer session affinity is determined by source IP address, which is the same criterion by which throttle policy is applied, an attacker from a fixed location should be bound to the same CAS server node for successive authentication attempts. A distributed attack, on the other hand, where successive request would be routed indeterminately, would cause haphazard tracking for in-memory CAS components since attempts would be split across N systems. However, since the source varies, accurate accounting would be pointless since the throttling components themselves assume a constant source IP for tracking purposes. The login throttling components are not sufficient for detecting or preventing a distributed password brute force attack.
