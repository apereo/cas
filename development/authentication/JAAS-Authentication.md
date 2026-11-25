# JAAS Authentication

Source: https://apereo.github.io/cas/development/authentication/JAAS-Authentication.html

[JAAS](https://docs.oracle.com/javase/9/security/java-authentication-and-authorization-service-jaas1.htm) is a Java standard authentication and authorization API. JAAS is configured via externalized plain text configuration file. Using JAAS with CAS allows modification of the authentication process without having to rebuild and redeploy CAS and allows for PAM-style multi-module “stacked” authentication.

## Configuration

JAAS components are provided in the CAS core module and require no additional dependencies to use. The JAAS handler delegates to the built-in JAAS subsystem to perform authentication according to the directives in the JAAS config file.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.jaas[0].password-encoder.encoding-algorithm` (Required; type `String`): The encoding algorithm to use such as `MD5`. Relevant when the type used is `DEFAULT` or `GLIBC_CRYPT`. When used with `PasswordEncoderTypes#PBKDF2`, it should be one of `PBKDF2WithHmacSHA1`, `PBKDF2WithHmacSHA256` or `PBKDF2WithHmacSHA512`.
- `cas.authn.jaas[0].password-encoder.type` (Required; type `String`; default `NONE`): Define the password encoder type to use. Type may be specified as blank or `NONE` to disable password encoding. It may also refer to a fully-qualified class name that implements the Spring Security's `PasswordEncoder` interface if you wish you define your own encoder. The following types may be used: - `NONE`: No password encoding (i.e. plain-text) takes place. - `DEFAULT`: Use the `DefaultPasswordEncoder` of CAS. For message-digest algorithms via `character-encoding` and `encoding-algorithm`. - `BCRYPT`: Use the `BCryptPasswordEncoder` based on the strength provided and an optional secret. - `SCRYPT`: Use the `SCryptPasswordEncoder`. - `PBKDF2`: Use the `Pbkdf2PasswordEncoder` based on the strength provided and an optional secret. - `STANDARD`: Use the `StandardPasswordEncoder` based on the secret provided. - `SSHA`: Use the `LdapShaPasswordEncoder` supports Ldap SHA and SSHA (salted-SHA). The values are base-64 encoded and have the label {SHA} or {SSHA} prepended to the encoded hash. - `GLIBC_CRYPT`: Use the `GlibcCryptPasswordEncoder` based on the `encoding-algorithm`, strength provided and an optional secret. - `org.example.MyEncoder`: An implementation of `PasswordEncoder` of your own choosing. - `file:///path/to/script.groovy`: Path to a Groovy script charged with handling password encoding operations.
- `cas.authn.jaas[0].password-policy.groovy.location` (Required; type `Resource`): Handle password policy via Groovy script. The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.jaas[0].principal-transformation.groovy.location` (Required; type `Resource`): Transform usernames using a Groovy resource. The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.jaas[0].principal.principal-transformation.groovy.location` (Required; type `Resource`): Transform usernames using a Groovy resource. The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.jaas[0].realm` (Required; type `String`): JAAS realm to use.
- `cas.authn.jaas` (Optional; type `List<JaasAuthenticationProperties>`): Collection of settings related to JAAS authentication. These settings are required to be indexed (i.e. jaas[0].xyz).
- `cas.authn.jaas[0].credential-criteria` (Optional; type `String`): A number of authentication handlers are allowed to determine whether they can operate on the provided credential and as such lend themselves to be tried and tested during the authentication handler selection phase. The credential criteria may be one of the following options: - 1) A regular expression pattern that is tested against the credential identifier. - 2) A fully qualified class name of your own design that implements `Predicate`. - 3) Path to an external Groovy script that implements the same interface.
- `cas.authn.jaas[0].kerberos-kdc-system-property` (Optional; type `String`): Typically, the default realm and the KDC for that realm are indicated in the Kerberos `krb5.conf` configuration file. However, if you like, you can instead specify the realm value by setting this following system property value. If you set the realm property, you SHOULD also configure the kerberos KDC system property. Also note that if you set these properties, then no cross-realm authentication is possible unless a `krb5.conf` file is also provided from which the additional information required for cross-realm authentication may be obtained. If you set values for these properties, then they override the default realm and KDC values specified in `krb5.conf` (if such a file is found). The `krb5.conf` file is still consulted if values for items other than the default realm and KDC are needed. If no `krb5.conf` file is found, then the default values used for these items are implementation-specific.
- `cas.authn.jaas[0].kerberos-realm-system-property` (Optional; type `String`): Typically, the default realm and the KDC for that realm are indicated in the Kerberos `krb5.conf` configuration file. However, if you like, you can instead specify the realm value by setting this following system property value. If you set the realm property, you SHOULD also configure the kerberos KDC system property. Also note that if you set these properties, then no cross-realm authentication is possible unless a `krb5.conf` file is also provided from which the additional information required for cross-realm authentication may be obtained. If you set values for these properties, then they override the default realm and KDC values specified in `krb5.conf` (if such a file is found). The `krb5.conf` file is still consulted if values for items other than the default realm and KDC are needed. If no `krb5.conf` file is found, then the default values used for these items are implementation-specific.
- `cas.authn.jaas[0].login-config-type` (Optional; type `String`): Typically set to `JavaLoginConfig` which is the default Configuration implementation from the SUN provider. This type accepts a URI/path to a configuration file as a valid parameter type specified via `#loginConfigurationFile`. If this parameter is not specified, then the configuration information is loaded from the sources described in the ConfigFile class specification. If this parameter is specified, the configuration information is loaded solely from the specified URI.
- `cas.authn.jaas[0].login-configuration-file` (Optional; type `String`): Path to the location of configuration file (i.e. jaas.conf) that contains the realms and login modules.
- `cas.authn.jaas[0].name` (Optional; type `String`): Name of the authentication handler.
- `cas.authn.jaas[0].order` (Optional; type `int`; default `2147483647`): Order of the authentication handler in the chain.
- `cas.authn.jaas[0].password-encoder.character-encoding` (Optional; type `String`; default `UTF-8`): The encoding algorithm to use such as 'UTF-8'. Relevant when the type used is `DEFAULT`.
- `cas.authn.jaas[0].password-encoder.hash-length` (Optional; type `int`; default `16`): When used by `PasswordEncoderTypes#ARGON2`, it indicates the hash strength/length.
- `cas.authn.jaas[0].password-encoder.iterations` (Optional; type `int`; default `310000`): When used by `PasswordEncoderTypes#PBKDF2`, it indicates the required number of iterations.
- `cas.authn.jaas[0].password-encoder.secret` (Optional; type `String`): Secret to use with `PasswordEncoderTypes#STANDARD`, `PasswordEncoderTypes#PBKDF2`, `PasswordEncoderTypes#BCRYPT`, `PasswordEncoderTypes#GLIBC_CRYPT` password encoders. Secret usually is an optional setting.
- `cas.authn.jaas[0].password-encoder.strength` (Optional; type `int`; default `16`): Strength or number of iterations to use for password hashing. Usually relevant when dealing with `PasswordEncoderTypes#BCRYPT`, `PasswordEncoderTypes#PBKDF2` or `PasswordEncoderTypes#GLIBC_CRYPT`. When used by `PasswordEncoderTypes#ARGON2` or `PasswordEncoderTypes#PBKDF2`, it indicates the salt strength.
- `cas.authn.jaas[0].password-policy.account-state-handling-enabled` (Optional; type `boolean`; default `true`): Indicates whether account state handling should be enabled to process warnings or errors reported back from the authentication response, produced by the source.
- `cas.authn.jaas[0].password-policy.display-warning-on-match` (Optional; type `boolean`; default `true`): Indicates if warning should be displayed, when the ldap attribute value matches the `#warningAttributeValue`.
- `cas.authn.jaas[0].password-policy.enabled` (Optional; type `boolean`; default `true`): Whether password policy should be enabled.
- `cas.authn.jaas[0].password-policy.login-failures` (Optional; type `int`; default `5`): When dealing with FreeIPA, indicates the number of allows login failures.
- `cas.authn.jaas[0].password-policy.policy-attributes` (Optional; type `Map<String, Class<? extends LoginException>>`): Key-value structure (Map) that indicates a list of boolean attributes as keys. If either attribute value is true, indicating an account state is flagged, the corresponding error can be thrown. Example `accountLocked=javax.security.auth.login.AccountLockedException`
- `cas.authn.jaas[0].password-policy.strategy` (Optional; type `PasswordPolicyProperties.PasswordPolicyHandlingOptions`; default `DEFAULT`): Decide how authentication should handle password policy changes. Available values are as follows: - `DEFAULT`: Default option to handle policy changes. - `GROOVY`: Handle account password policies via Groovy. - `REJECT_RESULT_CODE`: Strategy to only activate password policy if the authentication response code is not blocked.
- `cas.authn.jaas[0].password-policy.warn-all` (Optional; type `boolean`): Always display the password expiration warning regardless.
- `cas.authn.jaas[0].password-policy.warning-attribute-name` (Optional; type `String`): Used by an account state handling policy that only calculates account warnings in case the entry carries this attribute.
- `cas.authn.jaas[0].password-policy.warning-attribute-value` (Optional; type `String`): Used by an account state handling policy that only calculates account warnings in case the entry carries an attribute `#warningAttributeName` whose value matches this field.
- `cas.authn.jaas[0].password-policy.warning-days` (Optional; type `int`; default `30`): This is used to calculate a warning period to see if account expiry is within the calculated window.
- `cas.authn.jaas[0].principal-transformation.blocking-pattern` (Optional; type `String`): A regular expression that will be used against the username to match for blocking/forbidden values. If a match is found, an exception will be thrown and principal transformation will fail.
- `cas.authn.jaas[0].principal-transformation.case-conversion` (Optional; type `PrincipalTransformationProperties.CaseConversion`; default `NONE`): Indicate whether the principal identifier should be transformed into upper-case, lower-case, etc. Available values are as follows: - `NONE`: No conversion. - `LOWERCASE`: Lowercase conversion. - `UPPERCASE`: Uppercase conversion.
- `cas.authn.jaas[0].principal-transformation.pattern` (Optional; type `String`): A regular expression that will be used against the provided username for username extractions. On a successful match, the first matched group in the pattern will be used as the extracted username.
- `cas.authn.jaas[0].principal-transformation.prefix` (Optional; type `String`): Prefix to add to the principal id prior to authentication.
- `cas.authn.jaas[0].principal-transformation.suffix` (Optional; type `String`): Suffix to add to the principal id prior to authentication.
- `cas.authn.jaas[0].principal.active-attribute-repository-ids` (Optional; type `String`; default `*`): Activated attribute repository identifiers that should be used for fetching attributes if attribute resolution is enabled. The list here may include identifiers separated by comma.
- `cas.authn.jaas[0].principal.attribute-repository-selection` (Optional; type `Map<String, String>`): Control the behavior of the attribute repository selection by authentication method or handler. The map here is keyed by the authentication handler name, and the value is the attribute repository identifiers separated by comma. When the authentication handler is executed, the attribute repositories assigned to this handler will be selected to fetch attributes. Note that the resolution engine will always favor attribute repositories assigned to the service definition, if any and as part of its authentication policy, over this global setting.
- `cas.authn.jaas[0].principal.attribute-resolution-enabled` (Optional; type `TriStateBoolean`; default `UNDEFINED`): Whether attribute repositories should be contacted to fetch person attributes. Defaults to true if not set. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.authn.jaas[0].principal.principal-attribute` (Optional; type `String`): Attribute name to use to indicate the identifier of the principal constructed. If the attribute is blank or has no values, the default principal id will be used determined by the underlying authentication engine. The principal id attribute usually is removed from the collection of attributes collected, though this behavior depends on the schematics of the underlying authentication strategy.
- `cas.authn.jaas[0].principal.principal-resolution-conflict-strategy` (Optional; type `String`; default `last`): In the event that the principal resolution engine resolves more than one principal, (especially if such principals in the chain have different identifiers), this setting determines strategy by which the principal id would be chosen from the chain. Accepted values are: `last`, `first`.
- `cas.authn.jaas[0].principal.principal-resolution-failure-fatal` (Optional; type `TriStateBoolean`; default `UNDEFINED`): When true, throws an error back indicating that principal resolution has failed and no principal can be found based on the authentication requirements. Otherwise, logs the condition as an error without raising a catastrophic error. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.authn.jaas[0].principal.principal-transformation.blocking-pattern` (Optional; type `String`): A regular expression that will be used against the username to match for blocking/forbidden values. If a match is found, an exception will be thrown and principal transformation will fail.
- `cas.authn.jaas[0].principal.principal-transformation.case-conversion` (Optional; type `PrincipalTransformationProperties.CaseConversion`; default `NONE`): Indicate whether the principal identifier should be transformed into upper-case, lower-case, etc. Available values are as follows: - `NONE`: No conversion. - `LOWERCASE`: Lowercase conversion. - `UPPERCASE`: Uppercase conversion.
- `cas.authn.jaas[0].principal.principal-transformation.pattern` (Optional; type `String`): A regular expression that will be used against the provided username for username extractions. On a successful match, the first matched group in the pattern will be used as the extracted username.
- `cas.authn.jaas[0].principal.principal-transformation.prefix` (Optional; type `String`): Prefix to add to the principal id prior to authentication.
- `cas.authn.jaas[0].principal.principal-transformation.suffix` (Optional; type `String`): Suffix to add to the principal id prior to authentication.
- `cas.authn.jaas[0].principal.return-null` (Optional; type `TriStateBoolean`; default `UNDEFINED`): Return a null principal object if no attributes can be found for the principal. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.authn.jaas[0].principal.use-existing-principal-id` (Optional; type `TriStateBoolean`; default `UNDEFINED`): Uses an existing principal id that may have already been established in order to run person directory queries. This is generally useful in situations where authentication is delegated to an external identity provider and a principal is first established to then query an attribute source. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.authn.jaas[0].state` (Optional; type `AuthenticationHandlerStates`; default `ACTIVE`): Define the scope and state of this authentication handler and the lifecycle in which it can be invoked or activated. Available values are as follows: - `ACTIVE`: Active authentication handler, and is invoked by default automatically to verify credentials globally. - `STANDBY`: Authentication handler is in a semi-enabled state, waiting to be called only on-demand when explicitly asked for.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

**Authentication**

Certain authentication handlers are allowed to determine whether they can operate on the provided credential and as such lend themselves to be tried and tested during the authentication handler selection phase. The credential criteria may be one of the following options:

- A regular expression pattern that is tested against the credential identifier.
- A fully qualified class name of your own design that looks similar to the below example:

```java
import java.util.function.Predicate;
import org.apereo.cas.authentication.Credential;

public class PredicateExample implements Predicate<Credential> {
    @Override
    public boolean test(final Credential credential) {
        // Examine the credential and return true/false
    }
}
```

- Path to an external Groovy script that looks similar to the below example:

```groovy
import org.apereo.cas.authentication.Credential
import java.util.function.Predicate

class PredicateExample implements Predicate<Credential> {
    @Override
    boolean test(final Credential credential) {
        // test and return result
    }
}
```

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development//integration/Apache-Groovy-Scripting.html).

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

**Password policy**

**Password Policy Strategies**

If the password policy strategy is to be handed off to a Groovy script, the outline of the script may be as follows:

```groovy
import java.util.*
import org.ldaptive.auth.*
import org.apereo.cas.*
import org.apereo.cas.authentication.*
import org.apereo.cas.authentication.support.*

List<MessageDescriptor> run(final Object... args) {
    def (response,configuration,logger,applicationContext) = args
    logger.info("Handling password policy [{}] via ${configuration.getAccountStateHandler()}", response)

    def accountStateHandler = configuration.getAccountStateHandler()
    return accountStateHandler.handle(response, configuration)
}
```

The parameters passed are as follows:

| Parameter | Description |
| --- | --- |
| `response` | The LDAP authentication response of type `org.ldaptive.auth.AuthenticationResponse` |
| `configuration` | The LDAP password policy configuration carrying the account state handler defined. |
| `logger` | The object responsible for issuing log messages such as `logger.info(...)`. |
| `applicationContext` | The Spring `ApplicationContext` that allows one to interact with the runtime. |

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

### Password Policy

JAAS authentication components have limited support for detecting account status and password policy settings defined via CAS settings.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.jaas[0].password-policy.groovy.location` (Required; type `Resource`): Handle password policy via Groovy script. The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.jaas[0].password-policy.account-state-handling-enabled` (Optional; type `boolean`; default `true`): Indicates whether account state handling should be enabled to process warnings or errors reported back from the authentication response, produced by the source.
- `cas.authn.jaas[0].password-policy.display-warning-on-match` (Optional; type `boolean`; default `true`): Indicates if warning should be displayed, when the ldap attribute value matches the `#warningAttributeValue`.
- `cas.authn.jaas[0].password-policy.enabled` (Optional; type `boolean`; default `true`): Whether password policy should be enabled.
- `cas.authn.jaas[0].password-policy.login-failures` (Optional; type `int`; default `5`): When dealing with FreeIPA, indicates the number of allows login failures.
- `cas.authn.jaas[0].password-policy.policy-attributes` (Optional; type `Map<String, Class<? extends LoginException>>`): Key-value structure (Map) that indicates a list of boolean attributes as keys. If either attribute value is true, indicating an account state is flagged, the corresponding error can be thrown. Example `accountLocked=javax.security.auth.login.AccountLockedException`
- `cas.authn.jaas[0].password-policy.strategy` (Optional; type `PasswordPolicyProperties.PasswordPolicyHandlingOptions`; default `DEFAULT`): Decide how authentication should handle password policy changes. Available values are as follows: - `DEFAULT`: Default option to handle policy changes. - `GROOVY`: Handle account password policies via Groovy. - `REJECT_RESULT_CODE`: Strategy to only activate password policy if the authentication response code is not blocked.
- `cas.authn.jaas[0].password-policy.warn-all` (Optional; type `boolean`): Always display the password expiration warning regardless.
- `cas.authn.jaas[0].password-policy.warning-attribute-name` (Optional; type `String`): Used by an account state handling policy that only calculates account warnings in case the entry carries this attribute.
- `cas.authn.jaas[0].password-policy.warning-attribute-value` (Optional; type `String`): Used by an account state handling policy that only calculates account warnings in case the entry carries an attribute `#warningAttributeName` whose value matches this field.
- `cas.authn.jaas[0].password-policy.warning-days` (Optional; type `int`; default `30`): This is used to calculate a warning period to see if account expiry is within the calculated window.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

**Password policy**

**Password Policy Strategies**

If the password policy strategy is to be handed off to a Groovy script, the outline of the script may be as follows:

```groovy
import java.util.*
import org.ldaptive.auth.*
import org.apereo.cas.*
import org.apereo.cas.authentication.*
import org.apereo.cas.authentication.support.*

List<MessageDescriptor> run(final Object... args) {
    def (response,configuration,logger,applicationContext) = args
    logger.info("Handling password policy [{}] via ${configuration.getAccountStateHandler()}", response)

    def accountStateHandler = configuration.getAccountStateHandler()
    return accountStateHandler.handle(response, configuration)
}
```

The parameters passed are as follows:

| Parameter | Description |
| --- | --- |
| `response` | The LDAP authentication response of type `org.ldaptive.auth.AuthenticationResponse` |
| `configuration` | The LDAP password policy configuration carrying the account state handler defined. |
| `logger` | The object responsible for issuing log messages such as `logger.info(...)`. |
| `applicationContext` | The Spring `ApplicationContext` that allows one to interact with the runtime. |

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development//integration/Apache-Groovy-Scripting.html).

## JAAS Configuration File

The default JAAS configuration file is located at `$JRE_HOME/lib/security/java.security`. It’s important to note that JAAS configuration applies to the entire JVM. The path to the JAAS configuration file in effect may be altered by setting the `java.security.auth.login.config` system property to an alternate file path (i.e. `file:/etc/cas/config/jaas.config`).

A sample JAAS configuration file is provided for reference.

```
/**
  * Login Configuration for JAAS with the realm name defined as CAS.
  */
CAS {
  org.sample.jaas.login.SampleLoginModule sufficient
    debug=FALSE;
};
```

## Login Modules

The following login modules are available with CAS:

### JNDI

The module prompts for a username and password and then verifies the password against the password stored in a directory service configured under JNDI.

```
CAS {
  com.sun.security.auth.module.JndiLoginModule sufficient
    user.provider.url=name_service_url
    group.provider.url=name_service_url
    debug=FALSE;
};
```

The value of `name_service_url` specifies the directory service and path where this module can access the relevant user and group information. Because this module only performs one-level searches to find the relevant user information, the URL must point to a directory one level above where the user and group information is stored in the directory service.

For a list of all other options and more comprehensive documentation, please see [this guide](http://docs.oracle.com/javase/8/docs/jre/api/security/jaas/spec/com/sun/security/auth/module/JndiLoginModule.html) for more info.

### Kerberos

This module authenticates users using Kerberos protocols. The configuration entry for module has several options that control the authentication process and additions to the `Subject`’s private credential set. Irrespective of these options, the `Subject`’s principal set and private credentials set are updated only when commit is called. When commit is called, the `KerberosPrincipal` is added to the `Subject`’s principal set and KerberosTicket is added to the `Subject`’s private credentials.

If the configuration entry for module has the option `storeKey` set to true, then `KerberosKey` will also be added to the subject’s private credentials. `KerberosKey`, the principal’s key will be either obtained from the keytab or derived from user’s password.

This module also recognizes the `doNotPrompt` option. If set to true the user will not be prompted for the password. The user can specify the location of the ticket cache by using the option `ticketCache` in the configuration entry. The user can specify the keytab location by using the option `keyTab` in the configuration entry.

The principal name can be specified in the configuration entry by using the option `principal`. The principal name can either be a simple user name or a service name such as `host/mission.eng.sun.com`. The principal can also be set using the system property `sun.security.krb5.principal`. This property is checked during login. If this property is not set, then the principal name from the configuration is used. In the case where the principal property is not set and the principal entry also does not exist, the user is prompted for the name. When this property of entry is set, and `useTicketCache` is set to true, only TGT belonging to this principal is used.

Note that a valid `krb5.conf` must be supplied to the JVM for Kerberos auth via setting `-Djava.security.krb5.conf=/etc/krb5.conf`.

```
CAS {
  com.sun.security.auth.module.Krb5LoginModule sufficient
    refreshKrb5Config=TRUE/FALSE
    useTicketCache=TRUE/FALSE
    ticketCache=...
    renewTGT=TRUE/FALSE
    useKeyTab=TRUE/FALSE
    doNotPrompt=TRUE/FALSE
    keyTab=TRUE/FALSE
    storeKey=TRUE/FALSE
    principal=...
    debug=FALSE;
};
```

For a list of all other options and more comprehensive documentation, please see [this guide](http://docs.oracle.com/javase/8/docs/jre/api/security/jaas/spec/com/sun/security/auth/module/Krb5LoginModule.html) for more info.

### UNIX

This module imports a user’s Unix Principal information (`UnixPrincipal`, `UnixNumericUserPrincipal`, and `UnixNumericGroupPrincipal`) and associates them with the current `Subject`.

```
CAS {
  com.sun.security.auth.module.UnixLoginModule sufficient
    debug=FALSE;
};
```

For a list of all other options and more comprehensive documentation, please see [this guide](http://docs.oracle.com/javase/8/docs/jre/api/security/jaas/spec/com/sun/security/auth/module/UnixLoginModule.html) for more info.

### NT

This module renders a user’s NT security information as some number of `Principal`s and associates them with a `Subject`.

```
CAS {
  com.sun.security.auth.module.NTLoginModule sufficient
    debugNative=TRUE
    debug=FALSE;
};
```

For a list of all other options and more comprehensive documentation, please see [this guide](http://docs.oracle.com/javase/8/docs/jre/api/security/jaas/spec/com/sun/security/auth/module/NTLoginModule.html) for more info.

### LDAP

This module performs LDAP-based authentication. A username and password is verified against the corresponding user credentials stored in an LDAP directory. If authentication is successful then a new `LdapPrincipal` is created using the user’s distinguished name and a new `UserPrincipal` is created using the user’s username and both are associated with the current `Subject`.

For a list of all other options and more comprehensive documentation, please see [this guide](http://docs.oracle.com/javase/8/docs/jre/api/security/jaas/spec/com/sun/security/auth/module/LdapLoginModule.html) for more info.

This module operates in one of three modes. A mode is selected by specifying a particular set of options:

#### Search First

In search-first mode, the LDAP directory is searched to determine the user’s distinguished name and then authentication is attempted. An (anonymous) search is performed using the supplied username in conjunction with a specified search filter. If successful then authentication is attempted using the user’s distinguished name and the supplied password. To enable this mode, set the `userFilter` option and omit the `authIdentity` option. Use `search-first` mode when the user’s distinguished name is not known in advance.

The example below identifies the LDAP server and specifies that users’ entries be located by their `uid` and `objectClass` attributes. It also specifies that an identity based on the user’s `employeeNumber` attribute should be created.

```
CAS {
  com.sun.security.auth.module.LdapLoginModule REQUIRED
    userProvider="ldap://ldap-svr/ou=people,dc=example,dc=com"
    userFilter="(&(uid={USERNAME})(objectClass=inetOrgPerson))"
    authzIdentity="{EMPLOYEENUMBER}"
    debug=true;
};
```

#### Authentication First

In `authentication-first` mode, authentication is attempted using the supplied username and password and then the LDAP directory is searched. If authentication is successful then a search is performed using the supplied username in conjunction with a specified search filter. To enable this mode, set the `authIdentity` and the `userFilter` options. Use `authentication-first` mode when accessing an LDAP directory that has been configured to disallow anonymous searches.

The example below requests that the LDAP server be located dynamically, that authentication be performed using the supplied username directly but without the protection of SSL and that users’ entries be located by one of three naming attributes and their `objectClass` attribute.

```
CAS {
  com.sun.security.auth.module.LdapLoginModule REQUIRED
    userProvider="ldap:///cn=users,dc=example,dc=com"
    authIdentity="{USERNAME}"
    userFilter="(&(|(samAccountName={USERNAME})(userPrincipalName={USERNAME})(cn={USERNAME}))(objectClass=user))"
    useSSL=false
    debug=true;
};
```

#### Authentication Only

In `authentication-only` mode, authentication is attempted using the supplied username and password. The LDAP directory is not searched because the user’s distinguished name is already known. To enable this mode, set the authIdentity option to a valid distinguished name and omit the userFilter option. Use authentication-only mode when the user’s distinguished name is known in advance.

The example below identifies alternative LDAP servers, it specifies the distinguished name to use for authentication and a fixed identity to use for authorization. No directory search is performed.

```
CAS {
  com.sun.security.auth.module.LdapLoginModule REQUIRED
    userProvider="ldap://ldap-svr1 ldap://ldap-svr2"
    authIdentity="cn={USERNAME},ou=people,dc=example,dc=com"
    authzIdentity="staff"
    debug=true;
  };
```

### Ldaptive

Ldaptive provides several [login modules for authentication and authorization](http://www.ldaptive.org/docs/guide/jaas.html) against an LDAP. Each module accepts properties that correspond to the setters on objects in the ldaptive code base. If you are looking to set a specific configuration option that is available as a setter, the chances are that it will be accepted on the module. Any unknown options will be passed to the provider as a generic property.

In order to take advantage of the login modules provided by Ldaptive, the following dependency must be present and added to the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-ldap-core"
}
```

### Keystore

This module prompts for a key store alias and populates the subject with the alias’s principal and credentials. Stores an `X500Principal` for the subject distinguished name of the first certificate in the alias’s credentials in the subject’s principals, the alias’s certificate path in the subject’s public credentials, and a `X500PrivateCredential` whose certificate is the first certificate in the alias’s certificate path and whose private key is the alias’s private key in the subject’s private credentials.

```
CAS {
  com.sun.security.auth.module.KeyStoreLoginModule sufficient
    keyStoreURL=...
    keyStoreType=
    keyStoreProvider=...
    keyStoreAlias=...
    keyStorePasswordURL=...
    privateKeyPasswordURL=...
    protected=...
    debug=FALSE;
};
```

For a list of all other options and more comprehensive documentation, please see [this guide](http://docs.oracle.com/javase/8/docs/jre/api/security/jaas/spec/com/sun/security/auth/module/KeyStoreLoginModule.html) for more info.

## Deployments

If your deployment strategy ultimately uses an [embedded container](https://apereo.github.io/cas/development/installation/Configuring-Servlet-Container.html), you can pass along the location of the JAAS configuration file in form of a system property as such:

```bash
java -Djava.security.auth.login.config=file:/etc/cas/config/jaas.config -jar ...
```

Alternatively, you may activate the login configuration type to be `JavaLoginConfig` in the CAS settings and specify the path to the jaas configuration file there in the settings directly.

For more information on configuration management, please [review this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management.html).
