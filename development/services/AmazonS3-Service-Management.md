# Amazon S3 Service Registry

Source: https://apereo.github.io/cas/development/services/AmazonS3-Service-Management.html

Stores registered service data in a [Amazon S3](https://aws.amazon.com/s3/) buckets. Each service definition is managed inside its own separate bucket and the body of the service definition is managed as a JSON blob, similar to that of [JSON service registry](https://apereo.github.io/cas/development/services/JSON-Service-Management.html). The bucket name for each service definition is determined based on the service numeric identifier and its name, which makes it **IMPORTANT** for you to ensure that service definitions do not have duplicate values in such fields. The bucket is named `cas-<name>-<id>`: the name is lowercased, every character that S3 does not allow in bucket names becomes a hyphen, and the name is shortened as needed so that the bucket name fits the 63 character limit. Renaming a service definition moves it to a new bucket and removes the old one.

Support is enabled by adding the following module into the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-aws-s3-service-registry"
}
```

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.service-registry.amazon-s3.credential-access-key` (Required; type `String`): Use access-key provided by AWS to authenticate.
- `cas.service-registry.amazon-s3.credential-secret-key` (Required; type `String`): Use secret key provided by AWS to authenticate.
- `cas.service-registry.amazon-s3.endpoint` (Required; type `String`): AWS custom endpoint.
- `cas.service-registry.amazon-s3.region` (Required; type `String`): AWS region used.
- `cas.service-registry.amazon-s3.client-execution-timeout` (Optional; type `String`; default `PT10S`): Client execution timeout.
- `cas.service-registry.amazon-s3.connection-timeout` (Optional; type `String`; default `PT5S`): Connection timeout.
- `cas.service-registry.amazon-s3.local-address` (Optional; type `String`): Local address.
- `cas.service-registry.amazon-s3.max-connections` (Optional; type `Integer`; default `10`): Maximum connections setting.
- `cas.service-registry.amazon-s3.order` (Optional; type `Integer`)
- `cas.service-registry.amazon-s3.path-style-enabled` (Optional; type `Boolean`; default `false`): Enable path-style access for S3 buckets over virtual-hosted–style access. In Amazon S3, path-style URLs use the following format: `https://s3.region-code.amazonaws.com/bucket-name/key-name` Currently, Amazon S3 supports both virtual-hosted–style and path-style URL access in all AWS Regions. However, path-style URLs will be discontinued in the future.
- `cas.service-registry.amazon-s3.profile-name` (Optional; type `String`): Profile name to use.
- `cas.service-registry.amazon-s3.profile-path` (Optional; type `String`): Profile path.
- `cas.service-registry.amazon-s3.proxy-host` (Optional; type `String`): Optionally specifies the proxy host to connect through.
- `cas.service-registry.amazon-s3.proxy-password` (Optional; type `String`): Optionally specifies the proxy password to connect through.
- `cas.service-registry.amazon-s3.proxy-username` (Optional; type `String`): Optionally specifies the proxy username to connect through.
- `cas.service-registry.amazon-s3.retry-mode` (Optional; type `String`; default `STANDARD`): Outline the requested retry mode. Accepted values are `STANDARD, LEGACY`.
- `cas.service-registry.amazon-s3.socket-timeout` (Optional; type `String`; default `PT5S`): Socket timeout.
- `cas.service-registry.amazon-s3.use-reaper` (Optional; type `Boolean`; default `false`): Flag that indicates whether to use reaper.

## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="com.amazonaws" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
...
```

## Auto Initialization

Upon startup and configuration permitting, the registry is able to auto initialize itself from default JSON service definitions available to CAS. See [this guide](https://apereo.github.io/cas/development/services/AutoInitialization-Service-Management.html) for more info.
