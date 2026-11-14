# Hazelcast Ticket Registry

Source: https://apereo.github.io/cas/development/ticketing/Hazelcast-Ticket-Registry.html

Hazelcast Ticket Registry is a distributed ticket registry implementation based on [Hazelcast](https://hazelcast.com/). The registry implementation is cluster-aware and is able to auto-join a cluster of all the CAS nodes that expose this registry. Hazelcast will use port auto-increment feature to assign a TCP port to each member of a cluster starting from initially provided arbitrary port, which is typically `5701` by default.

Hazelcast will evenly distribute the ticket data among all the members of a cluster in a very efficient manner. Also, by default, the data collection on each node is configured with 1 backup copy, so that Hazelcast will use it to make strong data consistency guarantees i.e. the loss of data on live nodes will not occur should any other *primary data owner* members die. The data will be re-partitioned among the remaining live cluster members.

Support is enabled by the following module:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-hazelcast-ticket-registry"
}
```

## Configuration

This module has a configuration strategy which by default auto-configures a hazelcast instance used by the ticket registry implementation to build and retrieve Hazelcast maps for its distributed tickets storage. Some aspects of hazelcast configuration in this auto-configuration mode are controlled by CAS properties.

#### Hazelcast Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.registry.hazelcast.core.enable-compression` (Optional; type `Boolean`; default `false`): Enables compression when default java serialization is used. This applies to values Hazelcast stores with Java serialization, such as authentication throttling records and streamed service definitions. Tickets of the Hazelcast ticket registry are stored with compact serialization and are not compressed.
- `cas.ticket.registry.hazelcast.core.enable-jet` (Optional; type `Boolean`)
- `cas.ticket.registry.hazelcast.core.enable-management-center-scripting` (Optional; type `Boolean`; default `true`): Enables scripting from Management Center.
- `cas.ticket.registry.hazelcast.core.license-key` (Optional; type `String`): Hazelcast enterprise license key.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

#### Hazelcast Cluster Core

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.registry.hazelcast.cluster.core.instance-name` (Required; type `String`): The instance name.
- `cas.ticket.registry.hazelcast.cluster.core.async-backup-count` (Optional; type `Integer`; default `0`): Hazelcast supports both synchronous and asynchronous backups. By default, backup operations are synchronous. In this case, backup operations block operations until backups are successfully copied to backup members (or deleted from backup members in case of remove) and acknowledgements are received. Therefore, backups are updated before a put operation is completed, provided that the cluster is stable. Asynchronous backups, on the other hand, do not block operations. They are fire and forget and do not require acknowledgements; the backup operations are performed at some point in time.
- `cas.ticket.registry.hazelcast.cluster.core.async-fillup` (Optional; type `Boolean`)
- `cas.ticket.registry.hazelcast.cluster.core.backup-count` (Optional; type `Integer`; default `1`): To provide data safety, Hazelcast allows you to specify the number of backup copies you want to have. That way, data on a cluster member will be copied onto other member(s). To create synchronous backups, select the number of backup copies. When this count is 1, a map entry will have its backup on one other member in the cluster. If you set it to 2, then a map entry will have its backup on two other members. You can set it to 0 if you do not want your entries to be backed up, e.g., if performance is more important than backing up. The maximum value for the backup count is 6. Sync backup operations have a blocking cost which may lead to latency issues.
- `cas.ticket.registry.hazelcast.cluster.core.cluster-name` (Optional; type `String`; default `cas-hazelcast-cluster`)
- `cas.ticket.registry.hazelcast.cluster.core.cp-member-count` (Optional; type `Integer`; default `0`): CP Subsystem is a component of a Hazelcast cluster that builds a strongly consistent layer for a set of distributed data structures. Its data structures are CP with respect to the CAP principle, i.e., they always maintain linearizability and prefer consistency over availability during network partitions. Besides network partitions, CP Subsystem withstands server and client failures. All members of a Hazelcast cluster do not necessarily take part in CP Subsystem. The number of Hazelcast members that take part in CP Subsystem is specified here. CP Subsystem must have at least 3 CP members.
- `cas.ticket.registry.hazelcast.cluster.core.eviction-policy` (Optional)
- `cas.ticket.registry.hazelcast.cluster.core.logging-type` (Optional; type `String`; default `slf4j`): Hazelcast has a flexible logging configuration and doesn't depend on any logging framework except JDK logging. It has in-built adaptors for a number of logging frameworks and also supports custom loggers by providing logging interfaces. To use built-in adaptors you should set this setting to one of predefined types below. - `jdk`: JDK logging - `log4j`: Log4j - `slf4j`: Slf4j - `none`: Disable logging
- `cas.ticket.registry.hazelcast.cluster.core.map-merge-policy` (Optional)
- `cas.ticket.registry.hazelcast.cluster.core.max-no-heartbeat-seconds` (Optional; type `Integer`; default `300`): Max timeout of heartbeat in seconds for a node to assume it is dead.
- `cas.ticket.registry.hazelcast.cluster.core.max-size` (Optional; type `Integer`; default `85`): Sets the maximum size of the map.
- `cas.ticket.registry.hazelcast.cluster.core.max-size-policy` (Optional; type `String`; default `USED_HEAP_PERCENTAGE`): Control the max size policy. - `FREE_HEAP_PERCENTAGE`: Policy based on minimum free JVM heap memory percentage per JVM. - `FREE_HEAP_SIZE`: Policy based on minimum free JVM heap memory in megabytes per JVM. - `FREE_NATIVE_MEMORY_PERCENTAGE`: Policy based on minimum free native memory percentage per Hazelcast instance. - `FREE_NATIVE_MEMORY_SIZE`: Policy based on minimum free native memory in megabytes per Hazelcast instance. - `PER_NODE`: Policy based on maximum number of entries stored per data structure (map, cache etc) on each Hazelcast instance. - `PER_PARTITION`: Policy based on maximum number of entries stored per data structure (map, cache etc) on each partition. - `USED_HEAP_PERCENTAGE`: Policy based on maximum used JVM heap memory percentage per data structure (map, cache etc) on each Hazelcast instance . - `USED_HEAP_SIZE`: Policy based on maximum used JVM heap memory in megabytes per data structure (map, cache etc) on each Hazelcast instance. - `USED_NATIVE_MEMORY_PERCENTAGE`: Policy based on maximum used native memory percentage per data structure (map, cache etc) on each Hazelcast instance. - `USED_NATIVE_MEMORY_SIZE`: Policy based on maximum used native memory in megabytes per data structure (map, cache etc) on each Hazelcast instance .
- `cas.ticket.registry.hazelcast.cluster.core.partition-member-group-type` (Optional; type `String`): With `PartitionGroupConfig`, you can control how primary and backup partitions are mapped to physical Members. Hazelcast will always place partitions on different partition groups so as to provide redundancy. Accepted value are: `PER_MEMBER, HOST_AWARE, CUSTOM, ZONE_AWARE, SPI`. In all cases a partition will never be created on the same group. If there are more partitions defined than there are partition groups, then only those partitions, up to the number of partition groups, will be created. For example, if you define 2 backups, then with the primary, that makes 3. If you have only two partition groups only two will be created. - {@code}PER_MEMBER Partition Groups}: This is the default partition scheme and is used if no other scheme is defined. Each Member is in a group of its own. - {@code}HOST_AWARE Partition Groups}: In this scheme, a group corresponds to a host, based on its IP address. Partitions will not be written to any other members on the same host. This scheme provides good redundancy when multiple instances are being run on the same host. - {@code}CUSTOM Partition Groups}: In this scheme, IP addresses, or IP address ranges, are allocated to groups. Partitions are not written to the same group. This is very useful for ensuring partitions are written to different racks or even availability zones. - {@code}ZONE_AWARE Partition Groups}: In this scheme, groups are allocated according to the metadata provided by Discovery SPI Partitions are not written to the same group. This is very useful for ensuring partitions are written to availability zones or different racks without providing the IP addresses to the config ahead. - {@code}SPI Partition Groups}: In this scheme, groups are allocated according to the implementation provided by Discovery SPI.
- `cas.ticket.registry.hazelcast.cluster.core.replicated` (Optional; type `Boolean`)
- `cas.ticket.registry.hazelcast.cluster.core.timeout` (Optional; type `Integer`; default `5`): Connection timeout in seconds for the TCP/IP config and members joining the cluster.

#### Hazelcast Cluster Networking

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.registry.hazelcast.cluster.network.members` (Required; type `List<String>`): Sets the well known members. If members is empty, calling this method will have the same effect as calling `clear()`. A member can be a comma separated string, e..g `10.11.12.1,10.11.12.2` which indicates multiple members are going to be added. The list of members must include ALL CAS server node, including the current node that owns this configuration.
- `cas.ticket.registry.hazelcast.cluster.network.port` (Required; type `Integer`; default `5701`): You can specify the ports which Hazelcast will use to communicate between cluster members. The name of the parameter for this is port and its default value is 5701. By default, Hazelcast will try 100 ports to bind. Meaning that, if you set the value of port as 5701, as members are joining to the cluster, Hazelcast tries to find ports between 5701 and 5801.
- `cas.ticket.registry.hazelcast.cluster.network.ipv4-enabled` (Optional; type `Boolean`; default `true`): IPv6 support has been switched off by default, since some platforms have issues in use of IPv6 stack. And some other platforms such as Amazon AWS have no support at all. To enable IPv6 support set this setting to false.
- `cas.ticket.registry.hazelcast.cluster.network.local-address` (Optional; type `String`): If this property is set, then this is the address where the server socket is bound to.
- `cas.ticket.registry.hazelcast.cluster.network.network-interfaces` (Optional; type `String`): You can specify which network interfaces that Hazelcast should use. Servers mostly have more than one network interface, so you may want to list the valid IPs. Range characters ('*' and '-') can be used for simplicity. For instance, 10.3.10.* refers to IPs between 10.3.10.0 and 10.3.10.255. Interface 10.3.10.4-18 refers to IPs between 10.3.10.4 and 10.3.10.18 (4 and 18 included). If network interface configuration is enabled (it is disabled by default) and if Hazelcast cannot find an matching interface, then it will print a message on the console and will not start on that node. Interfaces can be separated by a comma.
- `cas.ticket.registry.hazelcast.cluster.network.outbound-ports` (Optional; type `List<String>`): The outbound ports for the Hazelcast configuration.
- `cas.ticket.registry.hazelcast.cluster.network.port-auto-increment` (Optional; type `Boolean`; default `true`): You may also want to choose to use only one port. In that case, you can disable the auto-increment feature of port.
- `cas.ticket.registry.hazelcast.cluster.network.public-address` (Optional; type `String`): The default public address to be advertised to other cluster members and clients.
- `cas.ticket.registry.hazelcast.cluster.network.tcpip-enabled` (Optional; type `Boolean`; default `true`): Enable TCP/IP config. Contains the configuration for the Tcp/Ip join mechanism. The Tcp/Ip join mechanism relies on one or more well known members. So when a new member wants to join a cluster, it will try to connect to one of the well known members. If it is able to connect, it will now about all members in the cluster and doesn't rely on these well known members anymore.

#### Hazelcast Network TLS Encryption

You can use the TLS (Transport Layer Security) protocol to establish an encrypted communication across your Hazelcast cluster with key stores and trust stores. Hazelcast allows you to encrypt socket level communication between Hazelcast members and between Hazelcast clients and members, for end to end encryption.

> **Usage**
>
> Hazelcast SSL is an enterprise feature. You will need a proper Hazelcast Enterprise License to use this facility.

Hazelcast provides a default SSL context factory implementation, which is guided and auto-configured by CAS when enabled to use the configured keystore to initialize SSL context.

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.registry.hazelcast.cluster.network.ssl.cipher-suites` (Optional; type `String`): Comma-separated list of cipher suite names allowed to be used. Its default value are all supported suites in your Java runtime.
- `cas.ticket.registry.hazelcast.cluster.network.ssl.key-manager-algorithm` (Optional; type `String`): Name of the algorithm based on which the authentication keys are provided.
- `cas.ticket.registry.hazelcast.cluster.network.ssl.key-store-type` (Optional; type `String`; default `JKS`): Type of the keystore. Its default value is JKS. Another commonly used type is the PKCS12. Available keystore/truststore types depend on your Operating system and the Java runtime. Only needed when the mutual authentication is used.
- `cas.ticket.registry.hazelcast.cluster.network.ssl.keystore` (Optional; type `String`): Path of your keystore file. Only needed when the mutual authentication is used.
- `cas.ticket.registry.hazelcast.cluster.network.ssl.keystore-password` (Optional; type `String`): Password to access the key from your keystore file. Only needed when the mutual authentication is used.
- `cas.ticket.registry.hazelcast.cluster.network.ssl.mutual-authentication` (Optional; type `String`): Mutual authentication configuration. It’s empty by default which means the client side of connection is not authenticated. Available values are: - `REQUIRED` - server forces usage of a trusted client certificate - `OPTIONAL` - server asks for a client certificate, but it doesn't require it
- `cas.ticket.registry.hazelcast.cluster.network.ssl.protocol` (Optional; type `String`; default `TLS`): Name of the algorithm which is used in your TLS/SSL. For the protocol property, we recommend you to provide TLS with its version information, e.g., TLSv1.2. Note that if you write only TLS, your application chooses the TLS version according to your Java version.
- `cas.ticket.registry.hazelcast.cluster.network.ssl.trust-manager-algorithm` (Optional; type `String`): Name of the algorithm based on which the trust managers are provided.
- `cas.ticket.registry.hazelcast.cluster.network.ssl.trust-store` (Optional; type `String`): Path of your truststore file. The file truststore is a keystore file that contains a collection of certificates trusted by your application.
- `cas.ticket.registry.hazelcast.cluster.network.ssl.trust-store-password` (Optional; type `String`): Password to unlock the truststore file.
- `cas.ticket.registry.hazelcast.cluster.network.ssl.trust-store-type` (Optional; type `String`; default `JKS`): Type of the truststore. Its default value is JKS. Another commonly used type is the PKCS12. Available keystore/truststore types depend on your Operating system and the Java runtime.
- `cas.ticket.registry.hazelcast.cluster.network.ssl.validate-identity` (Optional; type `Boolean`; default `false`): Flag which allows enabling endpoint identity validation. It means, during the TLS handshake client verifies if the server’s hostname (or IP address) matches the information in X.509 certificate (Subject Alternative Name extension).

> **Performance**
>
> Under Linux, the JVM automatically makes use of `/dev/random` for the generation of random numbers. If this entropy is insufficient to keep up with the rate requiring random numbers, it can slow down the encryption/decryption since it could block for minutes waiting for sufficient entropy . This can be fixed by setting the `-Djava.security.egd=file:/dev/./urandom` system property. Note that if there is a shortage of entropy, this option will not block and the returned random values could theoretically be vulnerable to a cryptographic attack.

> **Session Monitoring**
>
> Be aware that under very heavy load and given a very large collection of tickets over time, [session monitoring capabilities](https://apereo.github.io/cas/development/monitoring/Configuring-Monitoring.html) of CAS that report back ticket statistics based on the underlying Hazelcast ticket registry may end up timing out. This is due to the concern that Hazelcast attempts to run distributed queries across the entire network to collect, analyze and aggregate tickets which may be still active or in flux. If you do experience this behavior, it likely is preferable to turn off the session monitor.

For more information on the Hazelcast configuration options available, refer to [the Hazelcast documentation](https://docs.hazelcast.com/imdg/latest/)

### Security

Tokens and tickets that are managed by the Hazelcast ticket registry can be signed and encrypted.

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.registry.hazelcast.crypto.encryption.key` (Required; type `String`): The encryption key. The encryption key by default and unless specified otherwise must be randomly-generated string whose length is defined by the encryption key size setting.
- `cas.ticket.registry.hazelcast.crypto.signing.key` (Required; type `String`): The signing key is a string whose length is defined by the signing key size setting.
- `cas.ticket.registry.hazelcast.crypto.alg` (Optional; type `String`; default `AES`): The signing/encryption algorithm to use.
- `cas.ticket.registry.hazelcast.crypto.enabled` (Optional; type `Boolean`; default `true`): Whether crypto operations are enabled.
- `cas.ticket.registry.hazelcast.crypto.encryption.key-size` (Optional; type `Integer`; default `16`): Encryption key size.
- `cas.ticket.registry.hazelcast.crypto.signing-enabled` (Optional; type `Boolean`; default `true`): Whether signing encryption operations are enabled.
- `cas.ticket.registry.hazelcast.crypto.signing.key-size` (Optional; type `Integer`; default `512`): The signing key size.

**Signing & encryption**

This CAS feature is able to accept signing and encryption crypto keys. In most scenarios if keys are not provided, CAS will auto-generate them. The following instructions apply if you wish to manually and beforehand create the signing and encryption keys.

Note that if you are asked to create a [JWK](https://tools.ietf.org/html/rfc7517) of a certain size for the key, you are to use the following set of commands to generate the token:

```bash
wget https://raw.githubusercontent.com/apereo/cas/master/etc/jwk-gen.jar
java -jar jwk-gen.jar -t oct -s [size]
```

The outcome would be similar to:

```json
{
  "kty": "oct",
  "kid": "...",
  "k": "..."
}
```

The generated value for `k` needs to be assigned to the relevant CAS settings. Note that keys generated via the above algorithm are processed by CAS using the Advanced Encryption Standard (`AES`) algorithm which is a specification for the encryption of electronic data established by the U.S. National Institute of Standards and Technology.

---

## Hazelcast Map Customization

The Hazelcast ticket registry implementation allows you to customize the Hazelcast `Map` that is used to store tickets. CAS automatically creates instances of such maps for ticket storage for each known ticket type. If you do need to customize the map instance before it’s registered with Hazelcast, you can do so by providing a bean of type `HazelcastMapCustomizer` in the application context.

```java
@Bean
public HazelcastMapCustomizer myHazelcastMapCustomizer() {
    return new MyHazelcastMapCustomizer();
}
```

[See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.

## Logging

To enable additional logging for the registry, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="com.hazelcast" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
...
```
