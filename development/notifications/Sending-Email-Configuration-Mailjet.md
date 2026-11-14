# Sending Email - Mailjet

Source: https://apereo.github.io/cas/development/notifications/Sending-Email-Configuration-Mailjet.html

Send email from CAS through Mailjet instead of an SMTP server.

You may instruct CAS to use [Mailjet](https://www.mailjet.com/) for sending emails. Support is enabled by including the following module:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-mailjet"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.email-provider.mailjet.api-key` (Required; type `String`): API key used to authenticate to the mailjet service.
- `cas.email-provider.mailjet.bearer-access-token` (Required; type `String`): Bearer access token used to authenticate to the mailjet service. Mainly requires for the SMS integration.
- `cas.email-provider.mailjet.secret-key` (Required; type `String`): Secret key used to authenticate to the mailjet service.
- `cas.email-provider.mailjet.sandbox-mode` (Optional; type `Boolean`; default `false`): Indicates whether the mailjet integration should be run in sandbox mode. By setting this property to a `true` value, you will turn off the delivery of the message while still getting back the full range of error messages that could be related to your message processing. If the message is processed without error, the response will follow the normal response payload format, omitting only the `MessageID` and `MessageUUID`.
