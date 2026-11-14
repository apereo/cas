# RADIUS Authentication

Source: https://apereo.github.io/cas/development/mfa/RADIUS-Authentication.html

Authenticate users against a RADIUS server, either as the primary login or as a second factor with one-time codes from RSA SecurID or another RADIUS-backed token system.

RADIUS support is enabled by only including the following dependency in the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-radius"
}
```

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.radius.client.inet-address` (Required; type `String`; default `localhost`): Server address to connect and establish a session.
- `cas.authn.radius.client.shared-secret` (Required; type `String`; default `N0Sh@ar3d$ecReT`): Secret/password to use for the initial bind.
- `cas.authn.radius.password-encoder.encoding-algorithm` (Required; type `String`): The encoding algorithm to use such as `MD5`. Relevant when the type used is `DEFAULT` or `GLIBC_CRYPT`. When used with `PasswordEncoderTypes#PBKDF2`, it should be one of `PBKDF2WithHmacSHA1`, `PBKDF2WithHmacSHA256` or `PBKDF2WithHmacSHA512`.
- `cas.authn.radius.password-encoder.type` (Required; type `String`; default `NONE`): Define the password encoder type to use. Type may be specified as blank or `NONE` to disable password encoding. It may also refer to a fully-qualified class name that implements the Spring Security's `PasswordEncoder` interface if you wish you define your own encoder. The following types may be used: - `NONE`: No password encoding (i.e. plain-text) takes place. - `DEFAULT`: Use the `DefaultPasswordEncoder` of CAS. For message-digest algorithms via `character-encoding` and `encoding-algorithm`. - `BCRYPT`: Use the `BCryptPasswordEncoder` based on the strength provided and an optional secret. - `SCRYPT`: Use the `SCryptPasswordEncoder`. - `PBKDF2`: Use the `Pbkdf2PasswordEncoder` based on the strength provided and an optional secret. - `STANDARD`: Use the `StandardPasswordEncoder` based on the secret provided. - `SSHA`: Use the `LdapShaPasswordEncoder` supports Ldap SHA and SSHA (salted-SHA). The values are base-64 encoded and have the label {SHA} or {SSHA} prepended to the encoded hash. - `GLIBC_CRYPT`: Use the `GlibcCryptPasswordEncoder` based on the `encoding-algorithm`, strength provided and an optional secret. - `org.example.MyEncoder`: An implementation of `PasswordEncoder` of your own choosing. - `file:///path/to/script.groovy`: Path to a Groovy script charged with handling password encoding operations.
- `cas.authn.radius.principal-transformation.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.radius.client.accounting-port` (Optional; type `Integer`; default `1813`): The accounting port.
- `cas.authn.radius.client.authentication-port` (Optional; type `Integer`; default `1812`): The authentication port.
- `cas.authn.radius.client.socket-timeout` (Optional; type `Integer`; default `0`): Socket connection timeout in milliseconds.
- `cas.authn.radius.client.transport-type` (Optional; type `RadiusClientProperties.RadiusClientTransportTypes`; default `UDP`): Transport type to use by this client to connect to the server. Available values are as follows: - `UDP`: Default. UDP client transport type. - `RADSEC`: RadSec is a protocol which allows RADIUS servers to transfer data over TCP and TLS for increased security.
- `cas.authn.radius.failover-on-authentication-failure` (Optional; type `Boolean`; default `false`): Whether authentication errors should be skipped and fail over to the next server.
- `cas.authn.radius.failover-on-exception` (Optional; type `Boolean`; default `false`): Whether catastrophic errors should be skipped and fail over to the next server.
- `cas.authn.radius.name` (Optional; type `String`): The name of the authentication handler.
- `cas.authn.radius.password-encoder.character-encoding` (Optional; type `String`; default `UTF-8`): The encoding algorithm to use such as 'UTF-8'. Relevant when the type used is `DEFAULT`.
- `cas.authn.radius.password-encoder.hash-length` (Optional; type `Integer`; default `16`): When used by `PasswordEncoderTypes#ARGON2`, it indicates the hash strength/length.
- `cas.authn.radius.password-encoder.iterations` (Optional; type `Integer`; default `310000`): When used by `PasswordEncoderTypes#PBKDF2`, it indicates the required number of iterations.
- `cas.authn.radius.password-encoder.secret` (Optional; type `String`): Secret to use with `PasswordEncoderTypes#STANDARD`, `PasswordEncoderTypes#PBKDF2`, `PasswordEncoderTypes#BCRYPT`, `PasswordEncoderTypes#GLIBC_CRYPT` password encoders. Secret usually is an optional setting.
- `cas.authn.radius.password-encoder.strength` (Optional; type `Integer`; default `16`): Strength or number of iterations to use for password hashing. Usually relevant when dealing with `PasswordEncoderTypes#BCRYPT`, `PasswordEncoderTypes#PBKDF2` or `PasswordEncoderTypes#GLIBC_CRYPT`. When used by `PasswordEncoderTypes#ARGON2` or `PasswordEncoderTypes#PBKDF2`, it indicates the salt strength.
- `cas.authn.radius.principal-transformation.blocking-pattern` (Optional; type `String`): A regular expression that will be used against the username to match for blocking/forbidden values. If a match is found, an exception will be thrown and principal transformation will fail.
- `cas.authn.radius.principal-transformation.case-conversion` (Optional; type `PrincipalTransformationProperties.CaseConversion`; default `NONE`): Indicate whether the principal identifier should be transformed into upper-case, lower-case, etc. Available values are as follows: - `NONE`: No conversion. - `LOWERCASE`: Lowercase conversion. - `UPPERCASE`: Uppercase conversion.
- `cas.authn.radius.principal-transformation.pattern` (Optional; type `String`): A regular expression that will be used against the provided username for username extractions. On a successful match, the first matched group in the pattern will be used as the extracted username.
- `cas.authn.radius.principal-transformation.prefix` (Optional; type `String`): Prefix to add to the principal id prior to authentication.
- `cas.authn.radius.principal-transformation.suffix` (Optional; type `String`): Suffix to add to the principal id prior to authentication.
- `cas.authn.radius.server.nas-identifier` (Optional; type `String`): The NAS identifier.
- `cas.authn.radius.server.nas-ip-address` (Optional; type `String`): The NAS IP address.
- `cas.authn.radius.server.nas-ipv6-address` (Optional; type `String`): The NAS IPv6 address.
- `cas.authn.radius.server.nas-port` (Optional; type `Long`; default `-1`): The NAS port.
- `cas.authn.radius.server.nas-port-id` (Optional; type `Long`; default `-1`): The NAS port id.
- `cas.authn.radius.server.nas-port-type` (Optional; type `Integer`; default `-1`): The NAS port type.
- `cas.authn.radius.server.nas-real-port` (Optional; type `Long`; default `-1`): The NAS real port.
- `cas.authn.radius.server.protocol` (Optional; type `String`; default `EAP_MSCHAPv2`): Radius protocol to use when communicating with the server.
- `cas.authn.radius.server.retries` (Optional; type `Integer`; default `3`): Number of re-try attempts when dealing with connection and authentication failures.
- `cas.authn.radius.state` (Optional; type `AuthenticationHandlerStates`; default `active`): Define the scope and state of this authentication handler and the lifecycle in which it can be invoked or activated. Available values are as follows: - `ACTIVE`: Active authentication handler, and is invoked by default automatically to verify credentials globally. - `STANDBY`: Authentication handler is in a semi-enabled state, waiting to be called only on-demand when explicitly asked for.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

**Password encoding**

If you need to design your own password encoding scheme where the type is specified as a fully qualified Java class name, the structure of the class would be similar to the following:

```java
package org.example.cas;

import org.springframework.security.crypto.codec.*;
import org.springframework.security.crypto.password.*;

public class MyEncoder extends AbstractPasswordEncoder {
    @Override
    protected byte[] encode(CharSequence rawPassword, byte[] salt) {
        return ...
    }
}
```

If you need to design your own password encoding scheme where the type is specified as a path to a Groovy script, the structure of the script would be similar to the following:

```groovy
import java.util.*

byte[] run(final Object... args) {
    def (rawPassword,generatedSalt,logger,applicationContext) = args
    logger.debug("Encoding password...")
    return ...
}

Boolean matches(final Object... args) {
    def (rawPassword,encodedPassword,logger,applicationContext) = args
    logger.debug("Does match or not ?");
    return ...
```

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development//integration/Apache-Groovy-Scripting.html).

**Principal transformation**

Authentication handlers that generally deal with username-password credentials can be configured to transform the user id prior to executing the authentication sequence. Each authentication strategy in CAS provides settings to properly transform the principal. Refer to the relevant settings for the authentication strategy at hand to learn more.

Authentication handlers as part of principal transformation may also be provided a path to a Groovy script to transform the provided username. The outline of the script may take on the following form:

```groovy
String run(final Object... args) {
    def (providedUsername,logger) = args
    return providedUsername.concat("SomethingElse")
}
```

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development//integration/Apache-Groovy-Scripting.html).

# RSA RADIUS MFA

RSA RADIUS OTP support for MFA is enabled by only including the following dependency in the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-radius-mfa"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.radius.bypass.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.mfa.radius.bypass.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.authn.mfa.radius.client.inet-address` (Required; type `String`; default `localhost`): Server address to connect and establish a session.
- `cas.authn.mfa.radius.client.shared-secret` (Required; type `String`; default `N0Sh@ar3d$ecReT`): Secret/password to use for the initial bind.
- `cas.authn.mfa.radius.allowed-authentication-attempts` (Optional; type `Long`; default `-1`): Total number of allowed authentication attempts with the radius mfa server before the authentication event is considered cancelled. A negative/zero value indicates that no limit is enforced.
- `cas.authn.mfa.radius.bypass.authentication-attribute-name` (Optional; type `String`): Skip multifactor authentication based on designated authentication attribute names.
- `cas.authn.mfa.radius.bypass.authentication-attribute-value` (Optional; type `String`): Optionally, skip multifactor authentication based on designated authentication attribute values. Multiple values may be separated by a comma.
- `cas.authn.mfa.radius.bypass.authentication-handler-name` (Optional; type `String`): Skip multifactor authentication depending on form of primary authentication execution. Specifically, skip multifactor if the a particular authentication handler noted by its name successfully is able to authenticate credentials in the primary factor. Multiple values may be separated by a comma.
- `cas.authn.mfa.radius.bypass.authentication-method-name` (Optional; type `String`): Skip multifactor authentication depending on method/form of primary authentication execution. Specifically, skip multifactor if the authentication method attribute collected as part of authentication metadata matches a certain value. Multiple values may be separated by a comma.
- `cas.authn.mfa.radius.bypass.credential-class-type` (Optional; type `String`): Skip multifactor authentication depending on form of primary credentials. Value must equal the fully qualified class name of the credential type.
- `cas.authn.mfa.radius.bypass.http-request-headers` (Optional; type `String`): Skip multifactor authentication if the http request contains the defined header names. Header names may be comma-separated and can be regular expressions; values are ignored.
- `cas.authn.mfa.radius.bypass.http-request-remote-address` (Optional; type `String`): Skip multifactor authentication if the http request's remote address or host matches the value defined here. The value may be specified as a regular expression.
- `cas.authn.mfa.radius.bypass.principal-attribute-name` (Optional; type `String`): Skip multifactor authentication based on designated principal attribute names.
- `cas.authn.mfa.radius.bypass.principal-attribute-value` (Optional; type `String`): Optionally, skip multifactor authentication based on designated principal attribute values.
- `cas.authn.mfa.radius.bypass.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.authn.mfa.radius.bypass.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.authn.mfa.radius.bypass.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.authn.mfa.radius.bypass.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.
- `cas.authn.mfa.radius.bypass.rest.method` (Optional; type `String`; default `GET`): HTTP method to use when contacting the rest endpoint. Examples include `GET, POST`, etc.
- `cas.authn.mfa.radius.client.accounting-port` (Optional; type `Integer`; default `1813`): The accounting port.
- `cas.authn.mfa.radius.client.authentication-port` (Optional; type `Integer`; default `1812`): The authentication port.
- `cas.authn.mfa.radius.client.socket-timeout` (Optional; type `Integer`; default `0`): Socket connection timeout in milliseconds.
- `cas.authn.mfa.radius.client.transport-type` (Optional; type `RadiusClientProperties.RadiusClientTransportTypes`; default `UDP`): Transport type to use by this client to connect to the server. Available values are as follows: - `UDP`: Default. UDP client transport type. - `RADSEC`: RadSec is a protocol which allows RADIUS servers to transfer data over TCP and TLS for increased security.
- `cas.authn.mfa.radius.failover-on-authentication-failure` (Optional; type `Boolean`; default `false`): In the event that radius authentication fails, fail over to the next server in the list.
- `cas.authn.mfa.radius.failover-on-exception` (Optional; type `Boolean`; default `false`): In the event that radius authentication fails due to a catastrophic event, fail over to the next server in the list.
- `cas.authn.mfa.radius.failure-mode` (Optional; type `BaseMultifactorAuthenticationProviderProperties.MultifactorAuthenticationProviderFailureModes`; default `CLOSED`): The failure mode policy for this MFA provider. The authentication policy by default supports fail-closed mode, which means that if you attempt to exercise a particular provider available to CAS and the provider cannot be reached, authentication will be stopped and an error will be displayed. You can of course change this behavior so that authentication proceeds without exercising the provider functionality, if that provider cannot respond. Each defined multifactor authentication provider can set its own failure mode policy. Failure modes set at this location will override the global failure mode, but defer to any failure mode set by the registered service. Available values are as follows: - `OPEN`: Disallow MFA, proceed with authentication but don't communicate MFA to the RP. - `CLOSED`: Disallow MFA, block with authentication. - `PHANTOM`: Disallow MFA, proceed with authentication and communicate MFA to the RP. - `NONE`: Do not check for failure at all. - `UNDEFINED`: The default one indicating that no failure mode is set at all.
- `cas.authn.mfa.radius.id` (Optional; type `String`): The identifier for the multifactor provider. In most cases, this need not be configured explicitly, unless multiple instances of the same provider type are configured in CAS.
- `cas.authn.mfa.radius.name` (Optional; type `String`): The name of the authentication handler used to verify credentials in MFA. Remember that if you have more than one authentication handler of the same type, the names must be defined uniquely for each authentication scheme. Failing to do so may force CAS to not register authentication handlers with a duplicate name.
- `cas.authn.mfa.radius.order` (Optional; type `Integer`): The order of the authentication handler in the chain.
- `cas.authn.mfa.radius.rank` (Optional; type `Integer`; default `0`): At times, CAS needs to determine the correct provider when step-up authentication is required. Consider for a moment that CAS already has established an SSO session with/without a provider and has reached a level of authentication. Another incoming request attempts to exercise that SSO session with a different and often competing authentication requirement that may differ from the authentication level CAS has already established. Concretely, examples may be: - CAS has achieved an SSO session, but a separate request now requires step-up authentication with DuoSecurity. - CAS has achieved an SSO session with an authentication level satisfied by DuoSecurity, but a separate request now requires step-up authentication with YubiKey. In certain scenarios, CAS will attempt to rank authentication levels and compare them with each other. If CAS already has achieved a level that is higher than what the incoming request requires, no step-up authentication will be performed. If the opposite is true, CAS will route the authentication flow to the required authentication level and upon success, will adjust the SSO session with the new higher authentication level now satisfied. Ranking of authentication methods is done per provider via specific properties for each. Note that the higher the rank value is, the higher on the security scale it remains. A provider that ranks higher with a larger weight value trumps and override others with a lower value.
- `cas.authn.mfa.radius.server.nas-identifier` (Optional; type `String`): The NAS identifier.
- `cas.authn.mfa.radius.server.nas-ip-address` (Optional; type `String`): The NAS IP address.
- `cas.authn.mfa.radius.server.nas-ipv6-address` (Optional; type `String`): The NAS IPv6 address.
- `cas.authn.mfa.radius.server.nas-port` (Optional; type `Long`; default `-1`): The NAS port.
- `cas.authn.mfa.radius.server.nas-port-id` (Optional; type `Long`; default `-1`): The NAS port id.
- `cas.authn.mfa.radius.server.nas-port-type` (Optional; type `Integer`; default `-1`): The NAS port type.
- `cas.authn.mfa.radius.server.nas-real-port` (Optional; type `Long`; default `-1`): The NAS real port.
- `cas.authn.mfa.radius.server.protocol` (Optional; type `String`; default `EAP_MSCHAPv2`): Radius protocol to use when communicating with the server.
- `cas.authn.mfa.radius.server.retries` (Optional; type `Integer`; default `3`): Number of re-try attempts when dealing with connection and authentication failures.
- `cas.authn.mfa.radius.trusted-device-enabled` (Optional; type `Boolean`; default `false`): Indicates whether this provider should support trusted devices.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

## Bypass

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.radius.bypass.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.mfa.radius.bypass.rest.url` (Required; type `String`): The endpoint URL to contact and retrieve attributes.
- `cas.authn.mfa.radius.bypass.authentication-attribute-name` (Optional; type `String`): Skip multifactor authentication based on designated authentication attribute names.
- `cas.authn.mfa.radius.bypass.authentication-attribute-value` (Optional; type `String`): Optionally, skip multifactor authentication based on designated authentication attribute values. Multiple values may be separated by a comma.
- `cas.authn.mfa.radius.bypass.authentication-handler-name` (Optional; type `String`): Skip multifactor authentication depending on form of primary authentication execution. Specifically, skip multifactor if the a particular authentication handler noted by its name successfully is able to authenticate credentials in the primary factor. Multiple values may be separated by a comma.
- `cas.authn.mfa.radius.bypass.authentication-method-name` (Optional; type `String`): Skip multifactor authentication depending on method/form of primary authentication execution. Specifically, skip multifactor if the authentication method attribute collected as part of authentication metadata matches a certain value. Multiple values may be separated by a comma.
- `cas.authn.mfa.radius.bypass.credential-class-type` (Optional; type `String`): Skip multifactor authentication depending on form of primary credentials. Value must equal the fully qualified class name of the credential type.
- `cas.authn.mfa.radius.bypass.http-request-headers` (Optional; type `String`): Skip multifactor authentication if the http request contains the defined header names. Header names may be comma-separated and can be regular expressions; values are ignored.
- `cas.authn.mfa.radius.bypass.http-request-remote-address` (Optional; type `String`): Skip multifactor authentication if the http request's remote address or host matches the value defined here. The value may be specified as a regular expression.
- `cas.authn.mfa.radius.bypass.principal-attribute-name` (Optional; type `String`): Skip multifactor authentication based on designated principal attribute names.
- `cas.authn.mfa.radius.bypass.principal-attribute-value` (Optional; type `String`): Optionally, skip multifactor authentication based on designated principal attribute values.
- `cas.authn.mfa.radius.bypass.rest.basic-auth-password` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the password for authentication.
- `cas.authn.mfa.radius.bypass.rest.basic-auth-username` (Optional; type `String`): If REST endpoint is protected via basic authentication, specify the username for authentication.
- `cas.authn.mfa.radius.bypass.rest.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.authn.mfa.radius.bypass.rest.maximum-retry-attempts` (Optional; type `Integer`; default `3`): When attempting to reach the endpoint, this setting controls the number of **retry attempts** that CAS should execute Setting this value to a zero or negative value will disable the retry policy.
- `cas.authn.mfa.radius.bypass.rest.method` (Optional; type `String`; default `GET`): HTTP method to use when contacting the rest endpoint. Examples include `GET, POST`, etc.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.
