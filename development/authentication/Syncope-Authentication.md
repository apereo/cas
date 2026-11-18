# Apache Syncope Authentication

Source: https://apereo.github.io/cas/development/authentication/Syncope-Authentication.html

CAS support handling the authentication event via [Apache Syncope](https://syncope.apache.org/). This is done by using the `rest/users/self` REST API that is exposed by a running Syncope instance. As part of a successful authentication attempt, the properties of the provided user object are transformed into CAS attributes that can then be released to applications, etc.

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-syncope-authentication"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.syncope.domain` (Required; type `String`; default `Master`): Syncope domain used for authentication, etc. Multiple domains can be separated via comma. Each domain entry results in a separate authentication attempt and transaction by CAS.
- `cas.authn.syncope.password-encoder.encoding-algorithm` (Required; type `String`): The encoding algorithm to use such as `MD5`. Relevant when the type used is `DEFAULT` or `GLIBC_CRYPT`. When used with `PasswordEncoderTypes#PBKDF2`, it should be one of `PBKDF2WithHmacSHA1`, `PBKDF2WithHmacSHA256` or `PBKDF2WithHmacSHA512`.
- `cas.authn.syncope.password-encoder.type` (Required; type `String`; default `NONE`): Define the password encoder type to use. Type may be specified as blank or `NONE` to disable password encoding. It may also refer to a fully-qualified class name that implements the Spring Security's `PasswordEncoder` interface if you wish you define your own encoder. The following types may be used: - `NONE`: No password encoding (i.e. plain-text) takes place. - `DEFAULT`: Use the `DefaultPasswordEncoder` of CAS. For message-digest algorithms via `character-encoding` and `encoding-algorithm`. - `BCRYPT`: Use the `BCryptPasswordEncoder` based on the strength provided and an optional secret. - `SCRYPT`: Use the `SCryptPasswordEncoder`. - `PBKDF2`: Use the `Pbkdf2PasswordEncoder` based on the strength provided and an optional secret. - `STANDARD`: Use the `StandardPasswordEncoder` based on the secret provided. - `SSHA`: Use the `LdapShaPasswordEncoder` supports Ldap SHA and SSHA (salted-SHA). The values are base-64 encoded and have the label {SHA} or {SSHA} prepended to the encoded hash. - `GLIBC_CRYPT`: Use the `GlibcCryptPasswordEncoder` based on the `encoding-algorithm`, strength provided and an optional secret. - `org.example.MyEncoder`: An implementation of `PasswordEncoder` of your own choosing. - `file:///path/to/script.groovy`: Path to a Groovy script charged with handling password encoding operations.
- `cas.authn.syncope.principal-transformation.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.syncope.provisioning.basic-auth-password` (Required; type `String`): Specify the password for REST authentication.
- `cas.authn.syncope.provisioning.basic-auth-username` (Required; type `String`): Specify the username for REST authentication.
- `cas.authn.syncope.provisioning.domain` (Required; type `String`; default `Master`): Syncope domain used for authentication, etc. Multiple domains can be separated via comma. Each domain entry results in a separate authentication attempt and transaction by CAS.
- `cas.authn.syncope.provisioning.realm` (Required; type `String`; default `/`): Syncope realm used for principal provisioning. Realms define a hierarchical security domain tree, primarily meant for containing users. The root realm contains everything, and other realms can be seen as containers that split up the total number of entities into smaller pools.
- `cas.authn.syncope.provisioning.search-filter` (Required; type `String`; default `username=={user}`): User FIQL filter to use for searching. Syntax is `username=={user}` or `username=={0}`.
- `cas.authn.syncope.provisioning.url` (Required; type `String`): Syncope instance URL primary used for REST.
- `cas.authn.syncope.url` (Required; type `String`): Syncope instance URL primary used for REST.
- `cas.authn.syncope.attribute-mappings` (Optional; type `Map<String,String>`): Map of attributes that optionally may be used to control the names of the collected attributes from Syncope. If an attribute is provided by Syncope, it can be listed here as the key of the map with a value that should be the name of that attribute as collected and recorded by CAS. For example, the convention `lastLoginDate->lastDate` will process the Syncope attribute `lastLoginDate` and will internally rename that to `lastDate`. If no mapping is specified, CAS defaults will be used instead. In other words, this settings allows one to virtually rename and remap Syncope attributes during the authentication event.
- `cas.authn.syncope.credential-criteria` (Optional; type `String`): A number of authentication handlers are allowed to determine whether they can operate on the provided credential and as such lend themselves to be tried and tested during the authentication handler selection phase. The credential criteria may be one of the following options: - 1) A regular expression pattern that is tested against the credential identifier. - 2) A fully qualified class name of your own design that implements `Predicate`. - 3) Path to an external Groovy script that implements the same interface.
- `cas.authn.syncope.max-retry-attempts` (Optional; type `Integer`; default `0`): Max **retry attempts** for the authentication.
- `cas.authn.syncope.name` (Optional; type `String`): Name of the authentication handler.
- `cas.authn.syncope.order` (Optional; type `Integer`): The order of this authentication handler in the chain.
- `cas.authn.syncope.password-encoder.character-encoding` (Optional; type `String`; default `UTF-8`): The encoding algorithm to use such as 'UTF-8'. Relevant when the type used is `DEFAULT`.
- `cas.authn.syncope.password-encoder.hash-length` (Optional; type `Integer`; default `16`): When used by `PasswordEncoderTypes#ARGON2`, it indicates the hash strength/length.
- `cas.authn.syncope.password-encoder.iterations` (Optional; type `Integer`; default `310000`): When used by `PasswordEncoderTypes#PBKDF2`, it indicates the required number of iterations.
- `cas.authn.syncope.password-encoder.secret` (Optional; type `String`): Secret to use with `PasswordEncoderTypes#STANDARD`, `PasswordEncoderTypes#PBKDF2`, `PasswordEncoderTypes#BCRYPT`, `PasswordEncoderTypes#GLIBC_CRYPT` password encoders. Secret usually is an optional setting.
- `cas.authn.syncope.password-encoder.strength` (Optional; type `Integer`; default `16`): Strength or number of iterations to use for password hashing. Usually relevant when dealing with `PasswordEncoderTypes#BCRYPT`, `PasswordEncoderTypes#PBKDF2` or `PasswordEncoderTypes#GLIBC_CRYPT`. When used by `PasswordEncoderTypes#ARGON2` or `PasswordEncoderTypes#PBKDF2`, it indicates the salt strength.
- `cas.authn.syncope.principal-transformation.blocking-pattern` (Optional; type `String`): A regular expression that will be used against the username to match for blocking/forbidden values. If a match is found, an exception will be thrown and principal transformation will fail.
- `cas.authn.syncope.principal-transformation.case-conversion` (Optional; type `PrincipalTransformationProperties.CaseConversion`; default `NONE`): Indicate whether the principal identifier should be transformed into upper-case, lower-case, etc. Available values are as follows: - `NONE`: No conversion. - `LOWERCASE`: Lowercase conversion. - `UPPERCASE`: Uppercase conversion.
- `cas.authn.syncope.principal-transformation.pattern` (Optional; type `String`): A regular expression that will be used against the provided username for username extractions. On a successful match, the first matched group in the pattern will be used as the extracted username.
- `cas.authn.syncope.principal-transformation.prefix` (Optional; type `String`): Prefix to add to the principal id prior to authentication.
- `cas.authn.syncope.principal-transformation.suffix` (Optional; type `String`): Suffix to add to the principal id prior to authentication.
- `cas.authn.syncope.provisioning.attribute-mappings` (Optional; type `Map<String,String>`): Map of attributes that optionally may be used to control the names of the collected attributes from Syncope. If an attribute is provided by Syncope, it can be listed here as the key of the map with a value that should be the name of that attribute as collected and recorded by CAS. For example, the convention `lastLoginDate->lastDate` will process the Syncope attribute `lastLoginDate` and will internally rename that to `lastDate`. If no mapping is specified, CAS defaults will be used instead.
- `cas.authn.syncope.provisioning.enabled` (Optional; type `Boolean`; default `false`): Whether or not provisioning should be enabled with Syncope.
- `cas.authn.syncope.provisioning.headers` (Optional; type `Map<String,String>`): Headers, defined as a Map, to include in the request when making the REST call. Will overwrite any header that CAS is pre-defined to send and include in the request. Key in the map should be the header name and the value in the map should be the header value.
- `cas.authn.syncope.state` (Optional; type `AuthenticationHandlerStates`; default `active`): Define the scope and state of this authentication handler and the lifecycle in which it can be invoked or activated. Available values are as follows: - `ACTIVE`: Active authentication handler, and is invoked by default automatically to verify credentials globally. - `STANDBY`: Authentication handler is in a semi-enabled state, waiting to be called only on-demand when explicitly asked for.

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

## Attributes

As part of a successful authentication attempt, the following attributes provided by Apache Syncope are collected by CAS:

| Attribute Name |
| --- |
| `syncopeUserRoles` |
| `syncopeUserSecurityQuestion` |
| `syncopeUserStatus` |
| `syncopeUserKey` |
| `syncopeUserRealm` |
| `syncopeUserCreator` |
| `syncopeUserCreationDate` |
| `syncopeUserChangePwdDate` |
| `syncopeUserLastLoginDate` |
| `syncopeUserDynRoles` |
| `syncopeUserDynRealms` |
| `syncopeUserMemberships` |
| `syncopeUserDynMemberships` |
| `syncopeUserDynRelationships` |
| `syncopeUserAttrs` |

Note that attributes are only collected if they contain a value.

## Multitenancy

Configuration settings for database authentication can be specified in a multitenant environment. Please [review this guide](https://apereo.github.io/cas/development/multitenancy/Multitenancy-Tenant-Properties.html) for more information.

## Passwordless Authentication

The integration with Apache Syncope can also act as an account store for [Passwordless Authentication](https://apereo.github.io/cas/development/authentication/Passwordless-Authentication-Storage-Syncope.html).

## Password Management

The integration with Apache Syncope can also support [password management operations](https://apereo.github.io/cas/development/password_management/Password-Management-ApacheSyncope.html).
