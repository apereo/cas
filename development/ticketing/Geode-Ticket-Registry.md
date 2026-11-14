# Apache Geode Ticket Registry

Source: https://apereo.github.io/cas/development/ticketing/Geode-Ticket-Registry.html

Apache Geode integration is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-geode-ticket-registry"
}
```

This registry stores tickets in an [Apache Geode](http://geode.apache.org/) instance. On startup, CAS created dedicated regions in `REPLICATED` mode for each ticket type. Additional indexes are created on ticket documents stored to improve performance.

## Replication

With `REPLICATED` mode, the entire dataset in that region is copied and stored on every participating member that hosts the region. Any change made to the region (such as a put or update) is propagated to all members. Since every member holds an identical copy of the data, read operations can be performed locally on any member, and the data is consistent across the cluster. This makes replicated regions particularly useful for read-intensive applications where having low-latency, local access to all the data is important.

This option provides the following advantages:

- As every member has the complete dataset, reads are extremely fast because they occur locally without the need for inter-node communication.
- Data is available on all nodes. If one node fails, other nodes still have the entire dataset, ensuring high availability.
- Queries that involve looking up data from the entire dataset are simplified because the full data is present locally on each member.

You should also note that with this mode:

- Since the data is fully replicated on every node, the memory footprint multiplies with the number of nodes. This approach is best suited for datasets that are moderate in size.
- Every write operation (such as an update or insert) must be propagated to all members, which can incur additional network overhead and latency, especially as the size of the cluster increases.
- `REPLICATED` mode is most beneficial in scenarios where the workload is primarily read-heavy and the size of the dataset remains manageable.

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.registry.geode.crypto.encryption.key` (Required; type `String`): The encryption key. The encryption key by default and unless specified otherwise must be randomly-generated string whose length is defined by the encryption key size setting.
- `cas.ticket.registry.geode.crypto.signing.key` (Required; type `String`): The signing key is a string whose length is defined by the signing key size setting.
- `cas.ticket.registry.geode.crypto.alg` (Optional; type `String`; default `AES`): The signing/encryption algorithm to use.
- `cas.ticket.registry.geode.crypto.enabled` (Optional; type `Boolean`; default `true`): Whether crypto operations are enabled.
- `cas.ticket.registry.geode.crypto.encryption.key-size` (Optional; type `Integer`; default `16`): Encryption key size.
- `cas.ticket.registry.geode.crypto.signing-enabled` (Optional; type `Boolean`; default `true`): Whether signing encryption operations are enabled.
- `cas.ticket.registry.geode.crypto.signing.key-size` (Optional; type `Integer`; default `512`): The signing key size.
- `cas.ticket.registry.geode.locators` (Optional; type `String`; default `localhost[10334]`): When a Geode member starts up, it contacts one or more locators. The locator(s) maintain a list of all live members and help new members discover one another. They manage the membership view and broadcast updates when nodes join or leave the cluster. The locator acts as a bootstrap service. A member that does not yet know about the rest of the cluster can reach out to the locator to obtain the current membership list and configuration details. This is particularly useful when multicast is disabled (commonly the case in many environments), and static locator configuration is required. Locators continuously monitor the health of the cluster members. If a member becomes unresponsive, the locator updates the membership view, which can trigger re-balancing or other recovery actions. Specify one or more locators in `host[port]` format. Multiple locators can be comma separated. A blank value or `none` disables the locator support. You typically disable multicast by settings its port to zero to rely exclusively on locators for discovery. This ensures that all members explicitly contact the known locator addresses. For a real cluster, you’ll usually start locators as separate processes using Geode’s command-line tool, `gfsh`.
- `cas.ticket.registry.geode.multicast-port` (Optional; type `Integer`; default `0`): Members broadcast their presence over the multicast group so that any new or existing member on the same network can detect them automatically. The multicast mechanism helps build the initial membership view of the cluster and continuously updates it as nodes join or leave, which simplifies the network configuration in environments where nodes are frequently added or removed. You typically disable multicast by settings its port to zero.

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
