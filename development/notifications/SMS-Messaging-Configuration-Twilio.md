# Twilio Messaging & Calls

Source: https://apereo.github.io/cas/development/notifications/SMS-Messaging-Configuration-Twilio.html

Send SMS messages and make phone calls from CAS through Twilio.

To learn more, [visit this site](https://www.twilio.com/). The CAS integration with Twilio to support SMS messages and phone calls via dedicated configuration settings.

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-twilio"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.sms-provider.twilio.account-id` (Required; type `String`): Twilio account identifier used for authentication.
- `cas.sms-provider.twilio.enabled` (Required; type `Boolean`; default `true`): Whether the module is enabled or not, defaults to true.
- `cas.sms-provider.twilio.token` (Required; type `String`): Twilio secret token used for authentication.
- `cas.sms-provider.twilio.phone-calls-enabled` (Optional; type `Boolean`; default `false`): Controls whether Twilio support should also handle making phone calls.
