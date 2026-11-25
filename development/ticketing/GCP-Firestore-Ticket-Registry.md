# Google Cloud Firestore Ticket Registry

Source: https://apereo.github.io/cas/development/ticketing/GCP-Firestore-Ticket-Registry.html

[Firestore](https://cloud.google.com/firestore/docs) is a NoSQL document database built for automatic scaling, high performance, and ease of application development. While the Firestore interface has many of the same features as traditional databases, as a NoSQL database it differs from them in the way it describes relationships between data objects.

Support is enabled by including the following dependency in the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-gcp-firestore-ticket-registry"
}
```

Integration support is backed by the [Spring Cloud GCP project](https://cloud.google.com/java/docs/spring). Their [reference documentation](https://googlecloudplatform.github.io/spring-cloud-gcp/reference/html/index.html) provides detailed information on how to integrate Google Cloud APIs with CAS.

Note that CAS will automatically create the appropriate collections required for each ticket type. However, TTL expiration policies and field indexes for advanced use cases are not created automatically by CAS and may require manual intervention.

To understand how to set up application default credentials, please [review this page](https://cloud.google.com/docs/authentication/application-default-credentials).

## CAS Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.ticket.registry.google-cloud-firestore.crypto.encryption.key` (Required; type `String`): The encryption key. The encryption key by default and unless specified otherwise must be randomly-generated string whose length is defined by the encryption key size setting.
- `cas.ticket.registry.google-cloud-firestore.crypto.signing.key` (Required; type `String`): The signing key is a string whose length is defined by the signing key size setting.
- `cas.ticket.registry.google-cloud-firestore.crypto.alg` (Optional; type `String`; default `AES`): The signing/encryption algorithm to use.
- `cas.ticket.registry.google-cloud-firestore.crypto.enabled` (Optional; type `Boolean`; default `true`): Whether crypto operations are enabled.
- `cas.ticket.registry.google-cloud-firestore.crypto.encryption.key-size` (Optional; type `Integer`; default `16`): Encryption key size.
- `cas.ticket.registry.google-cloud-firestore.crypto.signing-enabled` (Optional; type `Boolean`; default `true`): Whether signing encryption operations are enabled.
- `cas.ticket.registry.google-cloud-firestore.crypto.signing.key-size` (Optional; type `Integer`; default `512`): The signing key size.
- `spring.cloud.gcp.firestore.credentials.default-scopes` (Third party; type `String[]`)
- `spring.cloud.gcp.firestore.database-id` (Third party; type `String`)
- `spring.cloud.gcp.firestore.emulator.enabled` (Third party; type `Boolean`; default `false`): Enables autoconfiguration to use the Firestore emulator.
- `spring.cloud.gcp.firestore.enabled` (Third party; type `Boolean`; default `true`): Auto-configure Google Cloud Firestore components.
- `spring.cloud.gcp.firestore.host-port` (Third party; type `String`; default `firestore.googleapis.com:443`): The host and port of the Firestore emulator service; can be overridden to specify an emulator.
- `spring.cloud.gcp.firestore.project-id` (Third party; type `String`)

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

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="com.google.cloud" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
...
```
