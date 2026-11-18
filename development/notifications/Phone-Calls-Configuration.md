# Phone Calls

Source: https://apereo.github.io/cas/development/notifications/Phone-Calls-Configuration.html

CAS presents the ability to notify users on select actions via phone calls. Example actions include notification of risky authentication attempts, multifactor authentication OTPs or password reset tokens. Phone operators supported by CAS are listed below. Note that an active/professional subscription may be required for certain providers.

Default support for phone calls is automatically enabled/included by the relevant modules using the following module:

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
| Twilio | [See this guide](https://apereo.github.io/cas/development/notifications/SMS-Messaging-Configuration-Twilio.html). |
| Custom | [See this guide](https://apereo.github.io/cas/development/notifications/Phone-Calls-Configuration-Custom.html). |
