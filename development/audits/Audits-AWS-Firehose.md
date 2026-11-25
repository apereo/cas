# Amazon Kinesis Firehose Audits

Source: https://apereo.github.io/cas/development/audits/Audits-AWS-Firehose.html

AWS Security Lake is a managed service that centralizes security-related logs and analytics from various AWS and third-party sources. You can send CAS audit events to Security Lake using AWS Firehose.

Support is enabled by including the following module in the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-audit-aws-firehose"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.audit.amazon-firehose.credential-access-key` (Required; type `String`): Use access-key provided by AWS to authenticate.
- `cas.audit.amazon-firehose.credential-secret-key` (Required; type `String`): Use secret key provided by AWS to authenticate.
- `cas.audit.amazon-firehose.delivery-stream-name` (Required; type `String`): The Firehose delivery stream that writes logs to a destination such as an S3 bucket.
- `cas.audit.amazon-firehose.endpoint` (Required; type `String`): AWS custom endpoint.
- `cas.audit.amazon-firehose.region` (Required; type `String`): AWS region used.
- `cas.audit.amazon-firehose.asynchronous` (Optional; type `Boolean`; default `true`): Make storage requests asynchronously.
- `cas.audit.amazon-firehose.client-execution-timeout` (Optional; type `String`; default `PT10S`): Client execution timeout.
- `cas.audit.amazon-firehose.connection-timeout` (Optional; type `String`; default `PT5S`): Connection timeout.
- `cas.audit.amazon-firehose.local-address` (Optional; type `String`): Local address.
- `cas.audit.amazon-firehose.max-connections` (Optional; type `Integer`; default `10`): Maximum connections setting.
- `cas.audit.amazon-firehose.profile-name` (Optional; type `String`): Profile name to use.
- `cas.audit.amazon-firehose.profile-path` (Optional; type `String`): Profile path.
- `cas.audit.amazon-firehose.proxy-host` (Optional; type `String`): Optionally specifies the proxy host to connect through.
- `cas.audit.amazon-firehose.proxy-password` (Optional; type `String`): Optionally specifies the proxy password to connect through.
- `cas.audit.amazon-firehose.proxy-username` (Optional; type `String`): Optionally specifies the proxy username to connect through.
- `cas.audit.amazon-firehose.retry-mode` (Optional; type `String`; default `STANDARD`): Outline the requested retry mode. Accepted values are `STANDARD, LEGACY`.
- `cas.audit.amazon-firehose.socket-timeout` (Optional; type `String`; default `PT5S`): Socket timeout.
- `cas.audit.amazon-firehose.use-reaper` (Optional; type `Boolean`; default `false`): Flag that indicates whether to use reaper.
