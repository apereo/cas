# Sending Email - Amazon Simple Email Service (SES)

Source: https://apereo.github.io/cas/development/notifications/Sending-Email-Configuration-AmazonSES.html

Send email from CAS through Amazon Simple Email Service instead of an SMTP server.

You may instruct CAS to use [Amazon SES](https://aws.amazon.com/ses/) for sending emails. Support is enabled by including the following module:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-aws-ses"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.email-provider.ses.credential-access-key` (Required; type `String`): Use access-key provided by AWS to authenticate.
- `cas.email-provider.ses.credential-secret-key` (Required; type `String`): Use secret key provided by AWS to authenticate.
- `cas.email-provider.ses.endpoint` (Required; type `String`): AWS custom endpoint.
- `cas.email-provider.ses.region` (Required; type `String`): AWS region used.
- `cas.email-provider.ses.client-execution-timeout` (Optional; type `String`; default `PT10S`): Client execution timeout.
- `cas.email-provider.ses.configuration-set-name` (Optional; type `String`): Configures configuration set name. Configuration sets let you create groups of rules that you can apply to the emails you send using Amazon SES.
- `cas.email-provider.ses.connection-timeout` (Optional; type `String`; default `PT5S`): Connection timeout.
- `cas.email-provider.ses.local-address` (Optional; type `String`): Local address.
- `cas.email-provider.ses.max-connections` (Optional; type `Integer`; default `10`): Maximum connections setting.
- `cas.email-provider.ses.profile-name` (Optional; type `String`): Profile name to use.
- `cas.email-provider.ses.profile-path` (Optional; type `String`): Profile path.
- `cas.email-provider.ses.proxy-host` (Optional; type `String`): Optionally specifies the proxy host to connect through.
- `cas.email-provider.ses.proxy-password` (Optional; type `String`): Optionally specifies the proxy password to connect through.
- `cas.email-provider.ses.proxy-username` (Optional; type `String`): Optionally specifies the proxy username to connect through.
- `cas.email-provider.ses.retry-mode` (Optional; type `String`; default `STANDARD`): Outline the requested retry mode. Accepted values are `STANDARD, LEGACY`.
- `cas.email-provider.ses.socket-timeout` (Optional; type `String`; default `PT5S`): Socket timeout.
- `cas.email-provider.ses.source-arn` (Optional; type `String`): Configures source ARN. Used only for sending authorization. The ARN of the identity that is associated with the sending authorization policy that permits you to send for the email address specified.
- `cas.email-provider.ses.use-reaper` (Optional; type `Boolean`; default `false`): Flag that indicates whether to use reaper.
