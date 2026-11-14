# Mailjet SMS Messaging

Source: https://apereo.github.io/cas/development/notifications/SMS-Messaging-Configuration-Mailjet.html

Send SMS messages from CAS through Mailjet.

To learn more, [Mailjet](https://www.mailjet.com/). The CAS integration with Mailjet to support SMS messages and phone calls via dedicated configuration settings.

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

- `cas.sms-provider.mailjet.api-key` (Required; type `String`): API key used to authenticate to the mailjet service.
- `cas.sms-provider.mailjet.bearer-access-token` (Required; type `String`): Bearer access token used to authenticate to the mailjet service. Mainly requires for the SMS integration.
- `cas.sms-provider.mailjet.secret-key` (Required; type `String`): Secret key used to authenticate to the mailjet service.
- `cas.sms-provider.mailjet.sandbox-mode` (Optional; type `Boolean`; default `false`): Indicates whether the mailjet integration should be run in sandbox mode. By setting this property to a `true` value, you will turn off the delivery of the message while still getting back the full range of error messages that could be related to your message processing. If the message is processed without error, the response will follow the normal response payload format, omitting only the `MessageID` and `MessageUUID`.

**Email servers**

The following settings may also need to be defined to describe the mail server settings:

- `spring.mail.default-encoding` (Third party; type `Charset`; default `UTF-8`): Default MimeMessage encoding.
- `spring.mail.host` (Third party; type `String`): SMTP server host. For instance, 'smtp.example.com'.
- `spring.mail.jndi-name` (Third party; type `String`): Session JNDI name. When set, takes precedence over other Session settings.
- `spring.mail.password` (Third party; type `String`): Login password of the SMTP server.
- `spring.mail.port` (Third party; type `Integer`): SMTP server port.
- `spring.mail.properties` (Third party; type `Map<String,String>`): Additional JavaMail Session properties.
- `spring.mail.protocol` (Third party; type `String`; default `smtp`): Protocol used by the SMTP server.
- `spring.mail.ssl.bundle` (Third party; type `String`): SSL bundle name. If set, 'mail.(protocol).ssl.socketFactory' property is set to an SSLSocketFactory obtained from the corresponding SSL bundle. Note that the STARTTLS command can use the corresponding SSLSocketFactory, even if the 'mail.(protocol).ssl.enable' property is not set.
- `spring.mail.ssl.enabled` (Third party; type `Boolean`; default `false`): Whether to enable SSL support. If enabled, 'mail.(protocol).ssl.enable' property is set to 'true'.
- `spring.mail.ssl.verify-hostname` (Third party; type `Boolean`; default `true`): Whether to enable hostname verification.
- `spring.mail.test-connection` (Third party; type `Boolean`; default `false`): Whether to test that the mail server is available on startup.
- `spring.mail.username` (Third party; type `String`): Login user of the SMTP server.
