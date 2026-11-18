# Amazon Cognito Authentication

Source: https://apereo.github.io/cas/development/authentication/AWS-Cognito-Authentication.html

Verify and authenticate credentials using [Amazon Cognito](https://aws.amazon.com/cognito/).

Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-aws-cognito-authentication"
}
```

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.cognito.client-id` (Required; type `String`): The application client id, created in Cognito without a secret key.
- `cas.authn.cognito.credential-access-key` (Required; type `String`): Use access-key provided by AWS to authenticate.
- `cas.authn.cognito.credential-secret-key` (Required; type `String`): Use secret key provided by AWS to authenticate.
- `cas.authn.cognito.endpoint` (Required; type `String`): AWS custom endpoint.
- `cas.authn.cognito.password-encoder.encoding-algorithm` (Required; type `String`): The encoding algorithm to use such as `MD5`. Relevant when the type used is `DEFAULT` or `GLIBC_CRYPT`. When used with `PasswordEncoderTypes#PBKDF2`, it should be one of `PBKDF2WithHmacSHA1`, `PBKDF2WithHmacSHA256` or `PBKDF2WithHmacSHA512`.
- `cas.authn.cognito.password-encoder.type` (Required; type `String`; default `NONE`): Define the password encoder type to use. Type may be specified as blank or `NONE` to disable password encoding. It may also refer to a fully-qualified class name that implements the Spring Security's `PasswordEncoder` interface if you wish you define your own encoder. The following types may be used: - `NONE`: No password encoding (i.e. plain-text) takes place. - `DEFAULT`: Use the `DefaultPasswordEncoder` of CAS. For message-digest algorithms via `character-encoding` and `encoding-algorithm`. - `BCRYPT`: Use the `BCryptPasswordEncoder` based on the strength provided and an optional secret. - `SCRYPT`: Use the `SCryptPasswordEncoder`. - `PBKDF2`: Use the `Pbkdf2PasswordEncoder` based on the strength provided and an optional secret. - `STANDARD`: Use the `StandardPasswordEncoder` based on the secret provided. - `SSHA`: Use the `LdapShaPasswordEncoder` supports Ldap SHA and SSHA (salted-SHA). The values are base-64 encoded and have the label {SHA} or {SSHA} prepended to the encoded hash. - `GLIBC_CRYPT`: Use the `GlibcCryptPasswordEncoder` based on the `encoding-algorithm`, strength provided and an optional secret. - `org.example.MyEncoder`: An implementation of `PasswordEncoder` of your own choosing. - `file:///path/to/script.groovy`: Path to a Groovy script charged with handling password encoding operations.
- `cas.authn.cognito.principal-transformation.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.cognito.region` (Required; type `String`): AWS region used.
- `cas.authn.cognito.user-pool-id` (Required; type `String`): The user pool identifiers where accounts may be located.
- `cas.authn.cognito.client-execution-timeout` (Optional; type `String`; default `PT10S`): Client execution timeout.
- `cas.authn.cognito.connection-timeout` (Optional; type `String`; default `PT5S`): Connection timeout.
- `cas.authn.cognito.local-address` (Optional; type `String`): Local address.
- `cas.authn.cognito.mapped-attributes` (Optional; type `Map<String,String>`): Map of attributes to rename after fetching from the user pool. Mapped attributes are defined using a key-value structure where CAS allows the attribute name/key to be renamed virtually to a different attribute. The key is the attribute fetched from the user pool and the value is the attribute name CAS should use for virtual renames.
- `cas.authn.cognito.max-connections` (Optional; type `Integer`; default `10`): Maximum connections setting.
- `cas.authn.cognito.name` (Optional; type `String`): The name of the authentication handler.
- `cas.authn.cognito.order` (Optional; type `Integer`): The order of this authentication handler in the chain.
- `cas.authn.cognito.password-encoder.character-encoding` (Optional; type `String`; default `UTF-8`): The encoding algorithm to use such as 'UTF-8'. Relevant when the type used is `DEFAULT`.
- `cas.authn.cognito.password-encoder.hash-length` (Optional; type `Integer`; default `16`): When used by `PasswordEncoderTypes#ARGON2`, it indicates the hash strength/length.
- `cas.authn.cognito.password-encoder.iterations` (Optional; type `Integer`; default `310000`): When used by `PasswordEncoderTypes#PBKDF2`, it indicates the required number of iterations.
- `cas.authn.cognito.password-encoder.secret` (Optional; type `String`): Secret to use with `PasswordEncoderTypes#STANDARD`, `PasswordEncoderTypes#PBKDF2`, `PasswordEncoderTypes#BCRYPT`, `PasswordEncoderTypes#GLIBC_CRYPT` password encoders. Secret usually is an optional setting.
- `cas.authn.cognito.password-encoder.strength` (Optional; type `Integer`; default `16`): Strength or number of iterations to use for password hashing. Usually relevant when dealing with `PasswordEncoderTypes#BCRYPT`, `PasswordEncoderTypes#PBKDF2` or `PasswordEncoderTypes#GLIBC_CRYPT`. When used by `PasswordEncoderTypes#ARGON2` or `PasswordEncoderTypes#PBKDF2`, it indicates the salt strength.
- `cas.authn.cognito.principal-transformation.blocking-pattern` (Optional; type `String`): A regular expression that will be used against the username to match for blocking/forbidden values. If a match is found, an exception will be thrown and principal transformation will fail.
- `cas.authn.cognito.principal-transformation.case-conversion` (Optional; type `PrincipalTransformationProperties.CaseConversion`; default `NONE`): Indicate whether the principal identifier should be transformed into upper-case, lower-case, etc. Available values are as follows: - `NONE`: No conversion. - `LOWERCASE`: Lowercase conversion. - `UPPERCASE`: Uppercase conversion.
- `cas.authn.cognito.principal-transformation.pattern` (Optional; type `String`): A regular expression that will be used against the provided username for username extractions. On a successful match, the first matched group in the pattern will be used as the extracted username.
- `cas.authn.cognito.principal-transformation.prefix` (Optional; type `String`): Prefix to add to the principal id prior to authentication.
- `cas.authn.cognito.principal-transformation.suffix` (Optional; type `String`): Suffix to add to the principal id prior to authentication.
- `cas.authn.cognito.profile-name` (Optional; type `String`): Profile name to use.
- `cas.authn.cognito.profile-path` (Optional; type `String`): Profile path.
- `cas.authn.cognito.proxy-host` (Optional; type `String`): Optionally specifies the proxy host to connect through.
- `cas.authn.cognito.proxy-password` (Optional; type `String`): Optionally specifies the proxy password to connect through.
- `cas.authn.cognito.proxy-username` (Optional; type `String`): Optionally specifies the proxy username to connect through.
- `cas.authn.cognito.retry-mode` (Optional; type `String`; default `STANDARD`): Outline the requested retry mode. Accepted values are `STANDARD, LEGACY`.
- `cas.authn.cognito.socket-timeout` (Optional; type `String`; default `PT5S`): Socket timeout.
- `cas.authn.cognito.state` (Optional; type `AuthenticationHandlerStates`; default `active`): Define the scope and state of this authentication handler and the lifecycle in which it can be invoked or activated. Available values are as follows: - `ACTIVE`: Active authentication handler, and is invoked by default automatically to verify credentials globally. - `STANDBY`: Authentication handler is in a semi-enabled state, waiting to be called only on-demand when explicitly asked for.
- `cas.authn.cognito.use-reaper` (Optional; type `Boolean`; default `false`): Flag that indicates whether to use reaper.

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

When you create the *app client* entry in the Amazon Cognito management console, make sure the app is able to support the `ADMIN_NO_SRP_AUTH` authentication flow and it is *NOT* assigned a secret key.

## Troubleshooting

To enable additional logging, configure the log4j configuration file to add the following levels:

```xml
...
<Logger name="com.amazonaws" level="debug" additivity="false">
    <AppenderRef ref="casConsole"/>
    <AppenderRef ref="casFile"/>
</Logger>
...
```
