# Google Cloud Firestore Service Registry

Source: https://apereo.github.io/cas/development/services/GCP-Firestore-Service-Management.html

[Firestore](https://cloud.google.com/firestore/docs) is a NoSQL document database built for automatic scaling, high performance, and ease of application development. While the Firestore interface has many of the same features as traditional databases, as a NoSQL database it differs from them in the way it describes relationships between data objects.

Support is enabled by including the following dependency in the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-gcp-firestore-service-registry"
}
```

Integration support is backed by the [Spring Cloud GCP project](https://cloud.google.com/java/docs/spring). Their [reference documentation](https://googlecloudplatform.github.io/spring-cloud-gcp/reference/html/index.html) provides detailed information on how to integrate Google Cloud APIs with CAS.

Note that CAS will automatically create the appropriate collections required for services. However, TTL expiration policies and field indexes for advanced use cases are not created automatically by CAS and may require manual intervention.

To understand how to set up application default credentials, please [review this page](https://cloud.google.com/docs/authentication/application-default-credentials).

## CAS Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.service-registry.google-cloud-firestore.collection` (Required; type `String`; default `RegisteredServices`): Database collection name to store and fetch registered service definitions.
- `spring.cloud.gcp.firestore.credentials.default-scopes` (Third party; type `String[]`)
- `spring.cloud.gcp.firestore.database-id` (Third party; type `String`)
- `spring.cloud.gcp.firestore.emulator.enabled` (Third party; type `Boolean`; default `false`): Enables autoconfiguration to use the Firestore emulator.
- `spring.cloud.gcp.firestore.enabled` (Third party; type `Boolean`; default `true`): Auto-configure Google Cloud Firestore components.
- `spring.cloud.gcp.firestore.host-port` (Third party; type `String`; default `firestore.googleapis.com:443`): The host and port of the Firestore emulator service; can be overridden to specify an emulator.
- `spring.cloud.gcp.firestore.project-id` (Third party; type `String`)

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

## Auto Initialization

Upon startup and configuration permitting, the registry is able to auto initialize itself from default JSON service definitions available to CAS. See [this guide](https://apereo.github.io/cas/development/services/AutoInitialization-Service-Management.html) for more info.
