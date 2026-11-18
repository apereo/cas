# Sending Email - Mailgun

Source: https://apereo.github.io/cas/development/notifications/Sending-Email-Configuration-Mailgun.html

Send email from CAS through Mailgun instead of an SMTP server.

You may instruct CAS to use [Mailgun](https://www.mailgun.com/) for sending emails. Support is enabled by including the following module:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-mailgun"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.email-provider.mailgun.api-key` (Required; type `String`): API key used to authenticate to the service.
- `cas.email-provider.mailgun.base-url` (Required; type `String`; default `https://api.mailgun.net`): Base URL for the service. - For US regions (Default): `https://api.mailgun.net` - For EU regions: `https://api.eu.mailgun.net`
- `cas.email-provider.mailgun.domain` (Optional; type `String`): Your Mailgun account may contain multiple sending domains. Requests must include the address of the domain you're interested in. Example: `my.mailgun.org`
- `cas.email-provider.mailgun.test-mode` (Optional; type `Boolean`; default `false`): Whether the service is operating in test/developer mode.
