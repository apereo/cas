# Vonage (Nexmo) SMS Messaging

Source: https://apereo.github.io/cas/development/notifications/SMS-Messaging-Configuration-Nexmo.html

Send SMS messages through the Vonage SMS API, formerly known as Nexmo. The module and its settings still use the `nexmo` name.

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-sms-nexmo"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.sms-provider.nexmo.api-token` (Required; type `String`): Nexmo API token obtained from Nexmo.
- `cas.sms-provider.nexmo.api-secret` (Optional; type `String`): Nexmo API secret obtained from Nexmo.
- `cas.sms-provider.nexmo.signature-secret` (Optional; type `String`): Nexmo Signature secret obtained from Nexmo.
