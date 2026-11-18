# Sending Email - Twilio SendGrid

Source: https://apereo.github.io/cas/development/notifications/Sending-Email-Configuration-SendGrid.html

Send email from CAS through Twilio SendGrid instead of an SMTP server.

You may instruct CAS to use [Twilio SendGrid](https://sendgrid.com/) for sending emails. Support is enabled by including the following module:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-sendgrid"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `spring.sendgrid.api-key` (Third party; type `String`): SendGrid API key.
- `spring.sendgrid.proxy.host` (Third party; type `String`): SendGrid proxy host.
- `spring.sendgrid.proxy.port` (Third party; type `Integer`): SendGrid proxy port.
