# Inwebo/TrustBuilder Multifactor Authentication

Source: https://apereo.github.io/cas/development/mfa/Inwebo-Authentication.html

You can secure your CAS server with a second factor provided by [Inwebo](https://trustbuilder.com/).

> **TrustBuilder vs inWebo**
>
> In 2023 and one year after the strategic acquisition of TrustBuilder by inWebo, the two companies have now officially become one and are rebranded as TrustBuilder

A complete documentation of this integration can be found [on the Inwebo documentation website](https://docs.inwebo.com/documentation/cas-apereo-inwebo-integration).

Apart from this CAS integration, notice that this identity provider can provide more general authentication solutions.

Support is enabled by including the following module in the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-inwebo-mfa"
}
```

The integration adds support for both push mobile/desktop and browser authentications (Virtual Authenticator or mAccess WEB).

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.inwebo.bypass.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.mfa.inwebo.bypass.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.authn.mfa.inwebo.client-certificate.certificate.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.mfa.inwebo.client-certificate.passphrase` (Required; type `String`): The passphrase of the client certificate.
- `cas.authn.mfa.inwebo.service-id` (Required; type `Long`): The Inwebo service id.
- `cas.authn.mfa.inwebo.site-alias` (Required; type `String`): The alias of the secured site.
- `cas.authn.mfa.inwebo.browser-authenticator` (Optional; type `InweboMultifactorAuthenticationProperties.BrowserAuthenticatorTypes`; default `VIRTUAL_AUTHENTICATOR`): The browser authenticator to use (or none). Available values are as follows: - `NONE`: No browser authentication. - `VIRTUAL_AUTHENTICATOR`: Virtual Authenticator browser authentication. - `M_ACCESS_WEB`: mAccessWeb browser authentication.
- `cas.authn.mfa.inwebo.bypass.authentication-attribute-name` (Optional; type `String`): Skip multifactor authentication based on designated authentication attribute names.
- `cas.authn.mfa.inwebo.bypass.authentication-attribute-value` (Optional; type `String`): Optionally, skip multifactor authentication based on designated authentication attribute values. Multiple values may be separated by a comma.
- `cas.authn.mfa.inwebo.bypass.authentication-handler-name` (Optional; type `String`): Skip multifactor authentication depending on form of primary authentication execution. Specifically, skip multifactor if the a particular authentication handler noted by its name successfully is able to authenticate credentials in the primary factor. Multiple values may be separated by a comma.
- `cas.authn.mfa.inwebo.bypass.authentication-method-name` (Optional; type `String`): Skip multifactor authentication depending on method/form of primary authentication execution. Specifically, skip multifactor if the authentication method attribute collected as part of authentication metadata matches a certain value. Multiple values may be separated by a comma.
- `cas.authn.mfa.inwebo.bypass.credential-class-type` (Optional; type `String`): Skip multifactor authentication depending on form of primary credentials. Value must equal the fully qualified class name of the credential type.
- `cas.authn.mfa.inwebo.bypass.http-request-headers` (Optional; type `String`): Skip multifactor authentication if the http request contains the defined header names. Header names may be comma-separated and can be regular expressions; values are ignored.
- `cas.authn.mfa.inwebo.bypass.http-request-remote-address` (Optional; type `String`): Skip multifactor authentication if the http request's remote address or host matches the value defined here. The value may be specified as a regular expression.
- `cas.authn.mfa.inwebo.bypass.principal-attribute-name` (Optional; type `String`): Skip multifactor authentication based on designated principal attribute names.
- `cas.authn.mfa.inwebo.bypass.principal-attribute-value` (Optional; type `String`): Optionally, skip multifactor authentication based on designated principal attribute values.
- `cas.authn.mfa.inwebo.bypass.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.authn.mfa.inwebo.bypass.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.authn.mfa.inwebo.bypass.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.authn.mfa.inwebo.bypass.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.
- `cas.authn.mfa.inwebo.bypass.rest.method` (Optional; type `String`; default `GET`): HTTP method to use when contacting the rest endpoint. Examples include `GET, POST`, etc.
- `cas.authn.mfa.inwebo.console-admin-url` (Optional; type `String`; default `https://api.myinwebo.com/v2/services/ConsoleAdmin`): Console admin API url.
- `cas.authn.mfa.inwebo.failure-mode` (Optional; type `BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes`; default `CLOSED`): The failure mode policy for this MFA provider. The authentication policy by default supports fail-closed mode, which means that if you attempt to exercise a particular provider available to CAS and the provider cannot be reached, authentication will be stopped and an error will be displayed. You can of course change this behavior so that authentication proceeds without exercising the provider functionality, if that provider cannot respond. Each defined multifactor authentication provider can set its own failure mode policy. Failure modes set at this location will override the global failure mode, but defer to any failure mode set by the registered service. Available values are as follows: - `OPEN`: Disallow MFA, proceed with authentication but don't communicate MFA to the RP. - `CLOSED`: Disallow MFA, block with authentication. - `PHANTOM`: Disallow MFA, proceed with authentication and communicate MFA to the RP. - `NONE`: Do not check for failure at all. - `UNDEFINED`: The default one indicating that no failure mode is set at all.
- `cas.authn.mfa.inwebo.id` (Optional; type `String`): The identifier for the multifactor provider. In most cases, this need not be configured explicitly, unless multiple instances of the same provider type are configured in CAS.
- `cas.authn.mfa.inwebo.name` (Optional; type `String`): The name of the authentication handler used to verify credentials in MFA. Remember that if you have more than one authentication handler of the same type, the names must be defined uniquely for each authentication scheme. Failing to do so may force CAS to not register authentication handlers with a duplicate name.
- `cas.authn.mfa.inwebo.order` (Optional; type `Integer`): The order of the authentication handler in the chain.
- `cas.authn.mfa.inwebo.push-auto` (Optional; type `Boolean`; default `true`): Whether the push authentication should happen directly (without proposing the browser authentication if defined).
- `cas.authn.mfa.inwebo.push-enabled` (Optional; type `Boolean`; default `true`): Whether the push notification (mobile/desktop) is enabled.
- `cas.authn.mfa.inwebo.rank` (Optional; type `Integer`; default `0`): At times, CAS needs to determine the correct provider when step-up authentication is required. Consider for a moment that CAS already has established an SSO session with/without a provider and has reached a level of authentication. Another incoming request attempts to exercise that SSO session with a different and often competing authentication requirement that may differ from the authentication level CAS has already established. Concretely, examples may be: - CAS has achieved an SSO session, but a separate request now requires step-up authentication with DuoSecurity. - CAS has achieved an SSO session with an authentication level satisfied by DuoSecurity, but a separate request now requires step-up authentication with YubiKey. In certain scenarios, CAS will attempt to rank authentication levels and compare them with each other. If CAS already has achieved a level that is higher than what the incoming request requires, no step-up authentication will be performed. If the opposite is true, CAS will route the authentication flow to the required authentication level and upon success, will adjust the SSO session with the new higher authentication level now satisfied. Ranking of authentication methods is done per provider via specific properties for each. Note that the higher the rank value is, the higher on the security scale it remains. A provider that ranks higher with a larger weight value trumps and override others with a lower value.
- `cas.authn.mfa.inwebo.service-api-url` (Optional; type `String`; default `https://api.myinwebo.com/FS?`): The service API url.
- `cas.authn.mfa.inwebo.site-description` (Optional; type `String`; default `my secured site`): The description of the secured site.
- `cas.authn.mfa.inwebo.trusted-device-enabled` (Optional; type `Boolean`; default `false`): Indicates whether this provider should support trusted devices.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

### Bypass

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.inwebo.bypass.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.mfa.inwebo.bypass.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.authn.mfa.inwebo.bypass.authentication-attribute-name` (Optional; type `String`): Skip multifactor authentication based on designated authentication attribute names.
- `cas.authn.mfa.inwebo.bypass.authentication-attribute-value` (Optional; type `String`): Optionally, skip multifactor authentication based on designated authentication attribute values. Multiple values may be separated by a comma.
- `cas.authn.mfa.inwebo.bypass.authentication-handler-name` (Optional; type `String`): Skip multifactor authentication depending on form of primary authentication execution. Specifically, skip multifactor if the a particular authentication handler noted by its name successfully is able to authenticate credentials in the primary factor. Multiple values may be separated by a comma.
- `cas.authn.mfa.inwebo.bypass.authentication-method-name` (Optional; type `String`): Skip multifactor authentication depending on method/form of primary authentication execution. Specifically, skip multifactor if the authentication method attribute collected as part of authentication metadata matches a certain value. Multiple values may be separated by a comma.
- `cas.authn.mfa.inwebo.bypass.credential-class-type` (Optional; type `String`): Skip multifactor authentication depending on form of primary credentials. Value must equal the fully qualified class name of the credential type.
- `cas.authn.mfa.inwebo.bypass.http-request-headers` (Optional; type `String`): Skip multifactor authentication if the http request contains the defined header names. Header names may be comma-separated and can be regular expressions; values are ignored.
- `cas.authn.mfa.inwebo.bypass.http-request-remote-address` (Optional; type `String`): Skip multifactor authentication if the http request's remote address or host matches the value defined here. The value may be specified as a regular expression.
- `cas.authn.mfa.inwebo.bypass.principal-attribute-name` (Optional; type `String`): Skip multifactor authentication based on designated principal attribute names.
- `cas.authn.mfa.inwebo.bypass.principal-attribute-value` (Optional; type `String`): Optionally, skip multifactor authentication based on designated principal attribute values.
- `cas.authn.mfa.inwebo.bypass.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.authn.mfa.inwebo.bypass.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.authn.mfa.inwebo.bypass.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.authn.mfa.inwebo.bypass.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.
- `cas.authn.mfa.inwebo.bypass.rest.method` (Optional; type `String`; default `GET`): HTTP method to use when contacting the rest endpoint. Examples include `GET, POST`, etc.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.
