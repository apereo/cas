# Apache Cassandra Authentication

Source: https://apereo.github.io/cas/development/authentication/Cassandra-Authentication.html

Verify and authenticate credentials using [Apache Cassandra](http://cassandra.apache.org/).

Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-cassandra-authentication"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.cassandra.contact-points` (Required; type `List<String>`): The list of contact points to use for the new cluster. Each contact point should be defined using the syntax `address:port`.
- `cas.authn.cassandra.keyspace` (Required; type `String`): Keyspace address to use where the cluster would connect.
- `cas.authn.cassandra.password` (Required; type `String`): Password to bind and establish a connection to cassandra.
- `cas.authn.cassandra.password-attribute` (Required; type `String`): Password attribute to fetch and compare.
- `cas.authn.cassandra.password-encoder.encoding-algorithm` (Required; type `String`): The encoding algorithm to use such as `MD5`. Relevant when the type used is `DEFAULT` or `GLIBC_CRYPT`. When used with `PasswordEncoderTypes#PBKDF2`, it should be one of `PBKDF2WithHmacSHA1`, `PBKDF2WithHmacSHA256` or `PBKDF2WithHmacSHA512`.
- `cas.authn.cassandra.password-encoder.type` (Required; type `String`; default `NONE`): Define the password encoder type to use. Type may be specified as blank or `NONE` to disable password encoding. It may also refer to a fully-qualified class name that implements the Spring Security's `PasswordEncoder` interface if you wish you define your own encoder. The following types may be used: - `NONE`: No password encoding (i.e. plain-text) takes place. - `DEFAULT`: Use the `DefaultPasswordEncoder` of CAS. For message-digest algorithms via `character-encoding` and `encoding-algorithm`. - `BCRYPT`: Use the `BCryptPasswordEncoder` based on the strength provided and an optional secret. - `SCRYPT`: Use the `SCryptPasswordEncoder`. - `PBKDF2`: Use the `Pbkdf2PasswordEncoder` based on the strength provided and an optional secret. - `STANDARD`: Use the `StandardPasswordEncoder` based on the secret provided. - `SSHA`: Use the `LdapShaPasswordEncoder` supports Ldap SHA and SSHA (salted-SHA). The values are base-64 encoded and have the label {SHA} or {SSHA} prepended to the encoded hash. - `GLIBC_CRYPT`: Use the `GlibcCryptPasswordEncoder` based on the `encoding-algorithm`, strength provided and an optional secret. - `org.example.MyEncoder`: An implementation of `PasswordEncoder` of your own choosing. - `file:///path/to/script.groovy`: Path to a Groovy script charged with handling password encoding operations.
- `cas.authn.cassandra.principal-transformation.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.cassandra.table-name` (Required; type `String`): Table name to fetch credentials.
- `cas.authn.cassandra.username` (Required; type `String`): Username to bind and establish a connection to cassandra.
- `cas.authn.cassandra.username-attribute` (Required; type `String`): Username attribute to fetch and compare.
- `cas.authn.cassandra.consistency-level` (Optional; type `String`; default `LOCAL_QUORUM`): Query option consistency level. The consistency level set through this method will be use for queries that don't explicitly have a consistency level. Accepted values are:`ALL, ANY, EACH_QUORUM, LOCAL_ONE, LOCAL_QUORUM, LOCAL_SERIAL, ONE, QUORUM, SERIAL, THREE, TWO`.
- `cas.authn.cassandra.local-dc` (Optional; type `String`): Used by a DC-ware round-robin load balancing policy. This policy provides round-robin queries over the node of the local data center. It also includes in the query plans returned a configurable number of hosts in the remote data centers, but those are always tried after the local nodes. In other words, this policy guarantees that no host in a remote data center will be queried unless no host in the local data center can be reached.
- `cas.authn.cassandra.name` (Optional; type `String`): Name of the authentication handler.
- `cas.authn.cassandra.order` (Optional; type `Integer`): The authentication handler order in the chain.
- `cas.authn.cassandra.password-encoder.character-encoding` (Optional; type `String`; default `UTF-8`): The encoding algorithm to use such as 'UTF-8'. Relevant when the type used is `DEFAULT`.
- `cas.authn.cassandra.password-encoder.hash-length` (Optional; type `Integer`; default `16`): When used by `PasswordEncoderTypes#ARGON2`, it indicates the hash strength/length.
- `cas.authn.cassandra.password-encoder.iterations` (Optional; type `Integer`; default `310000`): When used by `PasswordEncoderTypes#PBKDF2`, it indicates the required number of iterations.
- `cas.authn.cassandra.password-encoder.secret` (Optional; type `String`): Secret to use with `PasswordEncoderTypes#STANDARD`, `PasswordEncoderTypes#PBKDF2`, `PasswordEncoderTypes#BCRYPT`, `PasswordEncoderTypes#GLIBC_CRYPT` password encoders. Secret usually is an optional setting.
- `cas.authn.cassandra.password-encoder.strength` (Optional; type `Integer`; default `16`): Strength or number of iterations to use for password hashing. Usually relevant when dealing with `PasswordEncoderTypes#BCRYPT`, `PasswordEncoderTypes#PBKDF2` or `PasswordEncoderTypes#GLIBC_CRYPT`. When used by `PasswordEncoderTypes#ARGON2` or `PasswordEncoderTypes#PBKDF2`, it indicates the salt strength.
- `cas.authn.cassandra.principal-transformation.blocking-pattern` (Optional; type `String`): A regular expression that will be used against the username to match for blocking/forbidden values. If a match is found, an exception will be thrown and principal transformation will fail.
- `cas.authn.cassandra.principal-transformation.case-conversion` (Optional; type `PrincipalTransformationProperties.CaseConversion`; default `NONE`): Indicate whether the principal identifier should be transformed into upper-case, lower-case, etc. Available values are as follows: - `NONE`: No conversion. - `LOWERCASE`: Lowercase conversion. - `UPPERCASE`: Uppercase conversion.
- `cas.authn.cassandra.principal-transformation.pattern` (Optional; type `String`): A regular expression that will be used against the provided username for username extractions. On a successful match, the first matched group in the pattern will be used as the extracted username.
- `cas.authn.cassandra.principal-transformation.prefix` (Optional; type `String`): Prefix to add to the principal id prior to authentication.
- `cas.authn.cassandra.principal-transformation.suffix` (Optional; type `String`): Suffix to add to the principal id prior to authentication.
- `cas.authn.cassandra.query` (Optional; type `String`; default `SELECT * FROM %s WHERE %s = ? ALLOW FILTERING`): The authentication query to use when searching for users.
- `cas.authn.cassandra.serial-consistency-level` (Optional; type `String`; default `LOCAL_SERIAL`): Query option serial consistency level. The serial consistency level set through this method will be use for queries that don't explicitly have a serial consistency level. Accepted values are:`ALL, ANY, EACH_QUORUM, LOCAL_ONE, LOCAL_QUORUM, LOCAL_SERIAL, ONE, QUORUM, SERIAL, THREE, TWO`.
- `cas.authn.cassandra.ssl-cipher-suites` (Optional; type `String[]`): The cipher suites to use, or empty/null to use the default ones. Note that host name validation will always be done using HTTPS algorithm.
- `cas.authn.cassandra.ssl-protocols` (Optional; type `String[]`): Set the protocol versions enabled for use on this engine. Once the setting is set, only protocols listed in the protocols parameter are enabled for use.
- `cas.authn.cassandra.timeout` (Optional; type `String`; default `PT5S`): The request timeout. This defines how long the driver will wait for a given Cassandra node to answer a query.

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
