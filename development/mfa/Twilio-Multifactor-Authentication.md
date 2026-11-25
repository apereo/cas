# Twilio Multifactor Authentication

Source: https://apereo.github.io/cas/development/mfa/Twilio-Multifactor-Authentication.html

Multifactor authentication via [Twilio](https://www.twilio.com/) allows CAS to use the Twilio verification service and APIs to send passcodes to end-users via SMS, WhatsApp or voice calls, etc and verify the attempt subsequently by asking the user to provide the code back.

The recipient is determined as a principal attribute configured in CAS settings. Multiple verification channels are supported.

## Configuration

Support is enabled by including the following module in the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-twilio-mfa"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.twilio.bypass.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.mfa.twilio.bypass.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.authn.mfa.twilio.core.account-id` (Required; type `String`): Twilio account identifier used for authentication.
- `cas.authn.mfa.twilio.core.recipient-attribute-name` (Required; type `String`; default `phoneNumber`): The attribute name that contains the contact info (phone, etc) to send the verification attempt.
- `cas.authn.mfa.twilio.core.service-sid` (Required; type `String`): The identifier of the verification service typically in the `^VA[0-9a-fA-F]{32`$} format. A Verification Service is the set of common configurations used to create and check verifications. You can create a service with the API or in the Console.
- `cas.authn.mfa.twilio.core.token` (Required; type `String`): Twilio secret token used for authentication.
- `cas.authn.mfa.twilio.bucket4j.bandwidth` (Optional; type `List<Bucket4jBandwidthLimitProperties>`): Describe the available bandwidth and the overall limitations. Multiple bandwidths allow for different policies per unit of measure. (i.e. allows 1000 tokens per 1 minute, but not often then 50 tokens per 1 second).
- `cas.authn.mfa.twilio.bucket4j.bandwidth[0].capacity` (Optional; type `long`; default `120`): Number of tokens/requests that can be used within the time window.
- `cas.authn.mfa.twilio.bucket4j.bandwidth[0].duration` (Optional; type `String`; default `PT60S`): Time window in which capacity can be allowed.
- `cas.authn.mfa.twilio.bucket4j.bandwidth[0].initial-tokens` (Optional; type `long`): By default initial size of bucket equals to capacity. But sometimes, you may want to have lesser initial size, for example for case of cold start in order to prevent denial of service.
- `cas.authn.mfa.twilio.bucket4j.bandwidth[0].refill-count` (Optional; type `long`; default `10`): The number of tokens that should be used to refill the bucket given the specified refill duration.
- `cas.authn.mfa.twilio.bucket4j.bandwidth[0].refill-duration` (Optional; type `String`; default `PT30S`): Duration to use to refill the bucket.
- `cas.authn.mfa.twilio.bucket4j.bandwidth[0].refill-strategy` (Optional; type `Bucket4jBandwidthLimitProperties.BandwidthRefillStrategies`; default `GREEDY`): Describes how the bucket should be refilled. Specifies the speed of tokens regeneration. Available values are as follows: - `GREEDY`: This type of refill regenerates tokens in a greedy manner; it tries to add the tokens to bucket as soon as possible. For example refill "10 tokens per 1 second" adds 1 token per each 100 millisecond; in other words refill will not wait 1 second to regenerate 10 tokens. - `INTERVALLY`: This type of refill regenerates tokens in intervally manner. "Intervally" in opposite to "greedy" will wait until whole period would be elapsed before regenerating tokens.
- `cas.authn.mfa.twilio.bucket4j.blocking` (Optional; type `Boolean`; default `true`): Whether the request should block until capacity becomes available. When enabled, a request that finds the bucket empty parks its thread until the refill adds a token. On a request-serving path that turns a burst into thread-pool exhaustion, so throttling rejects the request immediately by default and only waits when this is explicitly turned on.
- `cas.authn.mfa.twilio.bucket4j.enabled` (Optional; type `Boolean`; default `true`): Decide whether bucket4j functionality should be enabled.
- `cas.authn.mfa.twilio.bypass.authentication-attribute-name` (Optional; type `String`): Skip multifactor authentication based on designated authentication attribute names.
- `cas.authn.mfa.twilio.bypass.authentication-attribute-value` (Optional; type `String`): Optionally, skip multifactor authentication based on designated authentication attribute values. Multiple values may be separated by a comma.
- `cas.authn.mfa.twilio.bypass.authentication-handler-name` (Optional; type `String`): Skip multifactor authentication depending on form of primary authentication execution. Specifically, skip multifactor if the a particular authentication handler noted by its name successfully is able to authenticate credentials in the primary factor. Multiple values may be separated by a comma.
- `cas.authn.mfa.twilio.bypass.authentication-method-name` (Optional; type `String`): Skip multifactor authentication depending on method/form of primary authentication execution. Specifically, skip multifactor if the authentication method attribute collected as part of authentication metadata matches a certain value. Multiple values may be separated by a comma.
- `cas.authn.mfa.twilio.bypass.credential-class-type` (Optional; type `String`): Skip multifactor authentication depending on form of primary credentials. Value must equal the fully qualified class name of the credential type.
- `cas.authn.mfa.twilio.bypass.http-request-headers` (Optional; type `String`): Skip multifactor authentication if the http request contains the defined header names. Header names may be comma-separated and can be regular expressions; values are ignored.
- `cas.authn.mfa.twilio.bypass.http-request-remote-address` (Optional; type `String`): Skip multifactor authentication if the http request's remote address or host matches the value defined here. The value may be specified as a regular expression.
- `cas.authn.mfa.twilio.bypass.principal-attribute-name` (Optional; type `String`): Skip multifactor authentication based on designated principal attribute names.
- `cas.authn.mfa.twilio.bypass.principal-attribute-value` (Optional; type `String`): Optionally, skip multifactor authentication based on designated principal attribute values.
- `cas.authn.mfa.twilio.bypass.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.authn.mfa.twilio.bypass.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.authn.mfa.twilio.bypass.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.authn.mfa.twilio.bypass.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.
- `cas.authn.mfa.twilio.bypass.rest.method` (Optional; type `String`; default `GET`): HTTP method to use when contacting the rest endpoint. Examples include `GET, POST`, etc.
- `cas.authn.mfa.twilio.core.verification-channels` (Optional; type `List<String>`): The list of channels to use for verification. Supported valued are: `sms, call, email, whatsapp, sna`. Default is `sms`.
- `cas.authn.mfa.twilio.failure-mode` (Optional; type `BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes`; default `CLOSED`): The failure mode policy for this MFA provider. The authentication policy by default supports fail-closed mode, which means that if you attempt to exercise a particular provider available to CAS and the provider cannot be reached, authentication will be stopped and an error will be displayed. You can of course change this behavior so that authentication proceeds without exercising the provider functionality, if that provider cannot respond. Each defined multifactor authentication provider can set its own failure mode policy. Failure modes set at this location will override the global failure mode, but defer to any failure mode set by the registered service. Available values are as follows: - `OPEN`: Disallow MFA, proceed with authentication but don't communicate MFA to the RP. - `CLOSED`: Disallow MFA, block with authentication. - `PHANTOM`: Disallow MFA, proceed with authentication and communicate MFA to the RP. - `NONE`: Do not check for failure at all. - `UNDEFINED`: The default one indicating that no failure mode is set at all.
- `cas.authn.mfa.twilio.id` (Optional; type `String`): The identifier for the multifactor provider. In most cases, this need not be configured explicitly, unless multiple instances of the same provider type are configured in CAS.
- `cas.authn.mfa.twilio.name` (Optional; type `String`): The name of the authentication handler used to verify credentials in MFA. Remember that if you have more than one authentication handler of the same type, the names must be defined uniquely for each authentication scheme. Failing to do so may force CAS to not register authentication handlers with a duplicate name.
- `cas.authn.mfa.twilio.order` (Optional; type `Integer`): The order of the authentication handler in the chain.
- `cas.authn.mfa.twilio.rank` (Optional; type `Integer`; default `0`): At times, CAS needs to determine the correct provider when step-up authentication is required. Consider for a moment that CAS already has established an SSO session with/without a provider and has reached a level of authentication. Another incoming request attempts to exercise that SSO session with a different and often competing authentication requirement that may differ from the authentication level CAS has already established. Concretely, examples may be: - CAS has achieved an SSO session, but a separate request now requires step-up authentication with DuoSecurity. - CAS has achieved an SSO session with an authentication level satisfied by DuoSecurity, but a separate request now requires step-up authentication with YubiKey. In certain scenarios, CAS will attempt to rank authentication levels and compare them with each other. If CAS already has achieved a level that is higher than what the incoming request requires, no step-up authentication will be performed. If the opposite is true, CAS will route the authentication flow to the required authentication level and upon success, will adjust the SSO session with the new higher authentication level now satisfied. Ranking of authentication methods is done per provider via specific properties for each. Note that the higher the rank value is, the higher on the security scale it remains. A provider that ranks higher with a larger weight value trumps and override others with a lower value.
- `cas.authn.mfa.twilio.trusted-device-enabled` (Optional; type `Boolean`; default `false`): Indicates whether this provider should support trusted devices.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

### Bypass

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.twilio.bypass.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.mfa.twilio.bypass.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.authn.mfa.twilio.bypass.authentication-attribute-name` (Optional; type `String`): Skip multifactor authentication based on designated authentication attribute names.
- `cas.authn.mfa.twilio.bypass.authentication-attribute-value` (Optional; type `String`): Optionally, skip multifactor authentication based on designated authentication attribute values. Multiple values may be separated by a comma.
- `cas.authn.mfa.twilio.bypass.authentication-handler-name` (Optional; type `String`): Skip multifactor authentication depending on form of primary authentication execution. Specifically, skip multifactor if the a particular authentication handler noted by its name successfully is able to authenticate credentials in the primary factor. Multiple values may be separated by a comma.
- `cas.authn.mfa.twilio.bypass.authentication-method-name` (Optional; type `String`): Skip multifactor authentication depending on method/form of primary authentication execution. Specifically, skip multifactor if the authentication method attribute collected as part of authentication metadata matches a certain value. Multiple values may be separated by a comma.
- `cas.authn.mfa.twilio.bypass.credential-class-type` (Optional; type `String`): Skip multifactor authentication depending on form of primary credentials. Value must equal the fully qualified class name of the credential type.
- `cas.authn.mfa.twilio.bypass.http-request-headers` (Optional; type `String`): Skip multifactor authentication if the http request contains the defined header names. Header names may be comma-separated and can be regular expressions; values are ignored.
- `cas.authn.mfa.twilio.bypass.http-request-remote-address` (Optional; type `String`): Skip multifactor authentication if the http request's remote address or host matches the value defined here. The value may be specified as a regular expression.
- `cas.authn.mfa.twilio.bypass.principal-attribute-name` (Optional; type `String`): Skip multifactor authentication based on designated principal attribute names.
- `cas.authn.mfa.twilio.bypass.principal-attribute-value` (Optional; type `String`): Optionally, skip multifactor authentication based on designated principal attribute values.
- `cas.authn.mfa.twilio.bypass.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.authn.mfa.twilio.bypass.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.authn.mfa.twilio.bypass.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.authn.mfa.twilio.bypass.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.
- `cas.authn.mfa.twilio.bypass.rest.method` (Optional; type `String`; default `GET`): HTTP method to use when contacting the rest endpoint. Examples include `GET, POST`, etc.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.
