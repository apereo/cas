# SMS Messaging

Source: https://apereo.github.io/cas/development/notifications/SMS-Messaging-Configuration.html

CAS presents the ability to notify users on select actions via SMS messaging. Example actions include notification of risky authentication attempts or password reset links/tokens. SMS providers supported by CAS are listed below. Note that an active/professional subscription may be required for certain providers.

Default support for SMS notifications is automatically enabled/included by the relevant modules using the following module:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-core-notifications"
}
```

You need not explicitly include this module in WAR Overlay configurations, except when there is a need to access components and APIs at compile-time. See below on how to customize or override the default behavior with specific providers.

| Provider | Reference |
| --- | --- |
| Custom | [See this guide](https://apereo.github.io/cas/development/notifications/SMS-Messaging-Configuration-Custom.html). |
| Groovy | [See this guide](https://apereo.github.io/cas/development/notifications/SMS-Messaging-Configuration-Groovy.html). |
| REST | [See this guide](https://apereo.github.io/cas/development/notifications/SMS-Messaging-Configuration-REST.html). |
| Twilio | [See this guide](https://apereo.github.io/cas/development/notifications/SMS-Messaging-Configuration-Twilio.html). |
| Mailjet | [See this guide](https://apereo.github.io/cas/development/notifications/SMS-Messaging-Configuration-Mailjet.html). |
| TextMagic | [See this guide](https://apereo.github.io/cas/development/notifications/SMS-Messaging-Configuration-TextMagic.html). |
| Clickatell | [See this guide](https://apereo.github.io/cas/development/notifications/SMS-Messaging-Configuration-Clickatell.html). |
| SmsMode | [See this guide](https://apereo.github.io/cas/development/notifications/SMS-Messaging-Configuration-SmsMode.html). |
| Amazon SNS | [See this guide](https://apereo.github.io/cas/development/notifications/SMS-Messaging-Configuration-AmazonSNS.html). |
| Vonage (Nexmo) | [See this guide](https://apereo.github.io/cas/development/notifications/SMS-Messaging-Configuration-Nexmo.html). |
