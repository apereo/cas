# Default Ticket Registry

Source: https://apereo.github.io/cas/development/ticketing/Default-Ticket-Registry.html

The default registry uses a memory-backed internal concurrent map for ticket storage and retrieval, though there is also the option to use an implementation that is backed by a caching engine to gain slightly better performance when it comes to evicting expired tickets.

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.registry.in-memory.crypto.encryption.key` (Required; type `String`): The encryption key. The encryption key by default and unless specified otherwise must be randomly-generated string whose length is defined by the encryption key size setting.
- `cas.ticket.registry.in-memory.crypto.signing.key` (Required; type `String`): The signing key is a string whose length is defined by the signing key size setting.
- `cas.ticket.registry.in-memory.concurrency` (Optional; type `Integer`; default `20`): The estimated number of concurrently updating threads. The implementation performs internal sizing to try to accommodate this many threads.
- `cas.ticket.registry.in-memory.crypto.alg` (Optional; type `String`; default `AES`): The signing/encryption algorithm to use.
- `cas.ticket.registry.in-memory.crypto.enabled` (Optional; type `Boolean`; default `true`): Whether crypto operations are enabled.
- `cas.ticket.registry.in-memory.crypto.encryption.key-size` (Optional; type `Integer`; default `16`): Encryption key size.
- `cas.ticket.registry.in-memory.crypto.signing-enabled` (Optional; type `Boolean`; default `true`): Whether signing encryption operations are enabled.
- `cas.ticket.registry.in-memory.crypto.signing.key-size` (Optional; type `Integer`; default `512`): The signing key size.
- `cas.ticket.registry.in-memory.initial-capacity` (Optional; type `Integer`; default `1000`): The initial capacity of the underlying memory store. The implementation performs internal sizing to accommodate this many elements.
- `cas.ticket.registry.in-memory.load-factor` (Optional; type `Integer`; default `1`): The load factor threshold, used to control resizing. Resizing may be performed when the average number of elements per bin exceeds this threshold.
- `cas.ticket.registry.in-memory.properties` (Optional; type `Map<String,String>`)

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

## Eviction Policy

This ticket registry relies on a background job that is automatically scheduled to clean up after the registry and remove expired tickets. The cleaner will periodically examine the state of the registry to identify expired tickets, remove them from the registry and then execute relevant logout operations.

## Clustering

This registry does not by default preserve ticket state across restarts and is not a suitable solution for clustered CAS environments that are deployed in active/active mode. Tickets are managed and stored in the runtime memory that is bound to the CAS server node, which means a ticket object created and managed by CAS server `A` cannot be found and accepted when a request for the same ticket is received by CAS server `B`.

The registry does however provide extension points for broadcasting the results of ticket operations. A *Pub/Sub* type of setup can tap into such extension points to allow the registry to operate in clustered environments, and to share ticket state across all CAS server nodes keeping them all in sync. Some ticket registries such as the [AMQP Ticket Registry](https://apereo.github.io/cas/development/ticketing/Messaging-AMQP-Ticket-Registry.html) are able to do so.
