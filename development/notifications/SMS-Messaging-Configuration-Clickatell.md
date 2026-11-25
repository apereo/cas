# Clickatell SMS Messaging

Source: https://apereo.github.io/cas/development/notifications/SMS-Messaging-Configuration-Clickatell.html

Send SMS messages from CAS through Clickatell.

To learn more, [visit this site](http://www.clickatell.com/).

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-sms-clickatell"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.sms-provider.clickatell.server-url` (Required; type `String`; default `https://platform.clickatell.com/messages`): URL to contact and send messages.
- `cas.sms-provider.clickatell.token` (Required; type `String`): Secure token used to establish a handshake with the service.
