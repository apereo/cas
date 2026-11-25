# TextMagic SMS Messaging

Source: https://apereo.github.io/cas/development/notifications/SMS-Messaging-Configuration-TextMagic.html

Send SMS messages from CAS through TextMagic.

To learn more, [visit this site](https://www.textmagic.com/).

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-sms-textmagic"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.sms-provider.text-magic.token` (Required; type `String`): Secure token used to establish a handshake.
- `cas.sms-provider.text-magic.username` (Required; type `String`): Username authorized to use the service as the bind account.
- `cas.sms-provider.text-magic.api-key` (Optional; type `String`): set API key value for the first API key authentication.
- `cas.sms-provider.text-magic.api-key-prefix` (Optional; type `String`): set API key prefix for the first API key authentication.
- `cas.sms-provider.text-magic.connect-timeout` (Optional; type `Integer`; default `5000`): connect timeout (in milliseconds).
- `cas.sms-provider.text-magic.debugging` (Optional; type `Boolean`; default `false`): Check that whether debugging is enabled for this API client.
- `cas.sms-provider.text-magic.password` (Optional; type `String`): set password for the first HTTP basic authentication.
- `cas.sms-provider.text-magic.read-timeout` (Optional; type `Integer`; default `5000`): read timeout (in milliseconds).
- `cas.sms-provider.text-magic.user-agent` (Optional; type `String`): Set the User-Agent header's value (by adding to the default header map).
- `cas.sms-provider.text-magic.verifying-ssl` (Optional; type `Boolean`; default `true`): Whether SSL connections should be verified.
- `cas.sms-provider.text-magic.write-timeout` (Optional; type `Integer`; default `0`): write timeout (in milliseconds).
