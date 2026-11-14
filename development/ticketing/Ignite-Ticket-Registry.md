# Apache Ignite Ticket Registry

Source: https://apereo.github.io/cas/development/ticketing/Ignite-Ticket-Registry.html

Apache Ignite integration is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-ignite-ticket-registry"
}
```

This registry stores tickets in an [Apache Ignite](http://ignite.apache.org/) instance.

## Distributed Cache

Distributed caches are recommended for HA architectures since they offer fault tolerance in the ticket storage subsystem.

## TLS Replication

Apache Ignite supports replication over TLS for distributed caches composed of two or more nodes. To learn more about TLS replication with Ignite, [see this resource](https://apacheignite.readme.io/docs/ssltls).

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.registry.ignite.crypto.encryption.key` (Required; type `String`): The encryption key. The encryption key by default and unless specified otherwise must be randomly-generated string whose length is defined by the encryption key size setting.
- `cas.ticket.registry.ignite.crypto.signing.key` (Required; type `String`): The signing key is a string whose length is defined by the signing key size setting.
- `cas.ticket.registry.ignite.ignite-servers` (Required; type `List<String>`): A comma-separated list of address:port pairs representing the entry points for the cluster discovery. This maps to the `netClusterNodes` configuration in the Node Finder. When this node starts, it will attempt to contact these addresses to find existing peers and join the topology.
- `cas.ticket.registry.ignite.cluster-name` (Optional; type `String`; default `cas-ignite-cluster`): The human-readable logical name of the cluster. This value is passed to the cluster builder during the initialization phase (see `#initializeCluster`). It serves as the identifier for the cluster topology management group (CMG). **Note:** This is only strictly required if `#initializeCluster` is true, as joining nodes will inherit the cluster name from the active topology.
- `cas.ticket.registry.ignite.crypto.alg` (Optional; type `String`; default `AES`): The signing/encryption algorithm to use.
- `cas.ticket.registry.ignite.crypto.enabled` (Optional; type `Boolean`; default `true`): Whether crypto operations are enabled.
- `cas.ticket.registry.ignite.crypto.encryption.key-size` (Optional; type `Integer`; default `16`): Encryption key size.
- `cas.ticket.registry.ignite.crypto.signing-enabled` (Optional; type `Boolean`; default `true`): Whether signing encryption operations are enabled.
- `cas.ticket.registry.ignite.crypto.signing.key-size` (Optional; type `Integer`; default `512`): The signing key size.
- `cas.ticket.registry.ignite.initialize-cluster` (Optional; type `Boolean`; default `true`): A flag indicating whether this specific node instance should attempt to initialize the cluster topology upon startup. A cluster is not operational until `initializeCluster` is set to `true` once. This flag should be `true` for only **one** node ("Leader" node) during the initial deployment.
- `cas.ticket.registry.ignite.node-name` (Optional; type `String`): The unique identifier for this specific Ignite node instance. This name is used for: - Log identification - Consistent Hashing (topology placement) - Raft voting identity - MetaStorage leadership election **Requirement:** This must be unique across all nodes in the cluster. Duplicate names will result in the node being rejected during the join phase.
- `cas.ticket.registry.ignite.port` (Optional; type `Integer`; default `47500`): The local network port that this Ignite node will bind to and listen on. This is a unified port used for both node discovery and data communication. If you are running multiple nodes on the same physical machine (or the same container network), each node **must** have a unique port to avoid `BindException`.

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

## Troubleshooting

- You will need to ensure that network communication across CAS nodes is allowed and no firewall or other component is blocking traffic.
- If nodes external to CAS instances are utilized, ensure that each cache manager specifies a name that matches the Apache Ignite configuration itself.
- You may also need to adjust your expiration policy to allow for a larger time span, especially for service tickets depending on network traffic and communication delay across CAS nodes particularly in the event that a node is trying to join the cluster.
