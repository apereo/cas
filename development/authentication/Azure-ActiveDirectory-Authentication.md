# Microsoft Entra ID Authentication

Source: https://apereo.github.io/cas/development/authentication/Azure-ActiveDirectory-Authentication.html

[Microsoft Entra ID](https://learn.microsoft.com/en-us/entra/fundamentals/what-is-entra) is Microsoft’s cloud-based identity and access management service. The functionality described here allows one to authenticate credentials using Microsoft Entra ID as the account store and optionally fetch user attributes using Microsoft Graph.

## Configuration

Support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-azuread-authentication"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.azure-active-directory.client-id` (Required; type `String`): Client id of the application.
- `cas.authn.azure-active-directory.client-secret` (Required; type `String`): Client secret of the registered app in microsoft azure portal.
- `cas.authn.azure-active-directory.password-encoder.encoding-algorithm` (Required; type `String`): The encoding algorithm to use such as `MD5`. Relevant when the type used is `DEFAULT` or `GLIBC_CRYPT`. When used with `PasswordEncoderTypes#PBKDF2`, it should be one of `PBKDF2WithHmacSHA1`, `PBKDF2WithHmacSHA256` or `PBKDF2WithHmacSHA512`.
- `cas.authn.azure-active-directory.password-encoder.type` (Required; type `String`; default `NONE`): Define the password encoder type to use. Type may be specified as blank or `NONE` to disable password encoding. It may also refer to a fully-qualified class name that implements the Spring Security's `PasswordEncoder` interface if you wish you define your own encoder. The following types may be used: - `NONE`: No password encoding (i.e. plain-text) takes place. - `DEFAULT`: Use the `DefaultPasswordEncoder` of CAS. For message-digest algorithms via `character-encoding` and `encoding-algorithm`. - `BCRYPT`: Use the `BCryptPasswordEncoder` based on the strength provided and an optional secret. - `SCRYPT`: Use the `SCryptPasswordEncoder`. - `PBKDF2`: Use the `Pbkdf2PasswordEncoder` based on the strength provided and an optional secret. - `STANDARD`: Use the `StandardPasswordEncoder` based on the secret provided. - `SSHA`: Use the `LdapShaPasswordEncoder` supports Ldap SHA and SSHA (salted-SHA). The values are base-64 encoded and have the label {SHA} or {SSHA} prepended to the encoded hash. - `GLIBC_CRYPT`: Use the `GlibcCryptPasswordEncoder` based on the `encoding-algorithm`, strength provided and an optional secret. - `org.example.MyEncoder`: An implementation of `PasswordEncoder` of your own choosing. - `file:///path/to/script.groovy`: Path to a Groovy script charged with handling password encoding operations.
- `cas.authn.azure-active-directory.principal-transformation.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.azure-active-directory.tenant` (Required; type `String`): The microsoft tenant id.
- `cas.authn.azure-active-directory.credential-criteria` (Optional; type `String`): A number of authentication handlers are allowed to determine whether they can operate on the provided credential and as such lend themselves to be tried and tested during the authentication handler selection phase. The credential criteria may be one of the following options: - 1) A regular expression pattern that is tested against the credential identifier. - 2) A fully qualified class name of your own design that implements `Predicate`. - 3) Path to an external Groovy script that implements the same interface.
- `cas.authn.azure-active-directory.enabled` (Optional; type `Boolean`; default `true`): Enable authentication against Azure active directory.
- `cas.authn.azure-active-directory.login-url` (Optional; type `String`; default `https://login.microsoftonline.com/common/`): This URL of the security token service that CAS goes to for acquiring tokens for resources and users This URL allows CAS to establish what is called an 'authority'. You can think of the authority as the directory issuing the identities/tokens. The login URL here is then composed of `https://<instance>/<tenant>`, where 'instance' is the Azure AD host (such as `https://login.microsoftonline.com`) and 'tenant' is the domain name (such as `contoso.onmicrosoft.com`) or tenant ID of the directory. Examples of authority URL are: - `https://login.microsoftonline.com/f31e6716-26e8-4651-b323-2563936b4163`: for a single tenant application defined in the tenant - `https://login.microsoftonline.com/contoso.onmicrosoft.com`: This representation is like the previous one, but uses the tenant domain name instead of the tenant Id. - `https://login.microsoftonline.de/contoso.de`: also uses a domain name, but in this case the Azure AD tenant admins have set a custom domain for their tenant, and the instance URL here is for the German national cloud. - `https://login.microsoftonline.com/common`: in the case of a multi-tenant application, that is an application available in several Azure AD tenants. - It can finally be an Active Directory Federation Services (ADFS) URL, which is recognized with the convention that the URL should contain adfs like `https://contoso.com/adfs`.
- `cas.authn.azure-active-directory.name` (Optional; type `String`): The name of the authentication handler.
- `cas.authn.azure-active-directory.order` (Optional; type `Integer`): The order of this authentication handler in the chain.
- `cas.authn.azure-active-directory.password-encoder.character-encoding` (Optional; type `String`; default `UTF-8`): The encoding algorithm to use such as 'UTF-8'. Relevant when the type used is `DEFAULT`.
- `cas.authn.azure-active-directory.password-encoder.hash-length` (Optional; type `Integer`; default `16`): When used by `PasswordEncoderTypes#ARGON2`, it indicates the hash strength/length.
- `cas.authn.azure-active-directory.password-encoder.iterations` (Optional; type `Integer`; default `310000`): When used by `PasswordEncoderTypes#PBKDF2`, it indicates the required number of iterations.
- `cas.authn.azure-active-directory.password-encoder.secret` (Optional; type `String`): Secret to use with `PasswordEncoderTypes#STANDARD`, `PasswordEncoderTypes#PBKDF2`, `PasswordEncoderTypes#BCRYPT`, `PasswordEncoderTypes#GLIBC_CRYPT` password encoders. Secret usually is an optional setting.
- `cas.authn.azure-active-directory.password-encoder.strength` (Optional; type `Integer`; default `16`): Strength or number of iterations to use for password hashing. Usually relevant when dealing with `PasswordEncoderTypes#BCRYPT`, `PasswordEncoderTypes#PBKDF2` or `PasswordEncoderTypes#GLIBC_CRYPT`. When used by `PasswordEncoderTypes#ARGON2` or `PasswordEncoderTypes#PBKDF2`, it indicates the salt strength.
- `cas.authn.azure-active-directory.principal-transformation.blocking-pattern` (Optional; type `String`): A regular expression that will be used against the username to match for blocking/forbidden values. If a match is found, an exception will be thrown and principal transformation will fail.
- `cas.authn.azure-active-directory.principal-transformation.case-conversion` (Optional; type `PrincipalTransformationProperties.CaseConversion`; default `NONE`): Indicate whether the principal identifier should be transformed into upper-case, lower-case, etc. Available values are as follows: - `NONE`: No conversion. - `LOWERCASE`: Lowercase conversion. - `UPPERCASE`: Uppercase conversion.
- `cas.authn.azure-active-directory.principal-transformation.pattern` (Optional; type `String`): A regular expression that will be used against the provided username for username extractions. On a successful match, the first matched group in the pattern will be used as the extracted username.
- `cas.authn.azure-active-directory.principal-transformation.prefix` (Optional; type `String`): Prefix to add to the principal id prior to authentication.
- `cas.authn.azure-active-directory.principal-transformation.suffix` (Optional; type `String`): Suffix to add to the principal id prior to authentication.
- `cas.authn.azure-active-directory.resource` (Optional; type `String`; default `https://graph.microsoft.com/`): Resource url for the graph API to fetch attributes.
- `cas.authn.azure-active-directory.scope` (Optional; type `String`; default `openid,email,profile,address`): Scope used when fetching access tokens. Multiple scopes may be separated using a comma.
- `cas.authn.azure-active-directory.state` (Optional; type `AuthenticationHandlerStates`; default `active`): Define the scope and state of this authentication handler and the lifecycle in which it can be invoked or activated. Available values are as follows: - `ACTIVE`: Active authentication handler, and is invoked by default automatically to verify credentials globally. - `STANDBY`: Authentication handler is in a semi-enabled state, waiting to be called only on-demand when explicitly asked for.

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

## Principal Attributes

The above dependency may also be used, in the event that principal attributes need to be fetched from Microsoft Entra ID without necessarily authenticating credentials .

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.attribute-repository.azure-active-directory[0].client-id` (Required; type `String`): Client id of the registered app in microsoft azure portal.
- `cas.authn.attribute-repository.azure-active-directory[0].client-secret` (Required; type `String`): Client secret of the registered app in microsoft azure portal.
- `cas.authn.attribute-repository.azure-active-directory` (Optional; type `List<AzureActiveDirectoryAttributesProperties>`): Retrieve attributes from multiple Microsoft Graph instances.
- `cas.authn.attribute-repository.azure-active-directory[0].api-base-url` (Optional; type `String`): Base API url used to contact microsoft graph for calls.
- `cas.authn.attribute-repository.azure-active-directory[0].attributes` (Optional; type `String`): Comma-separated attributes and user properties to fetch from microsoft graph. If attributes are specified here, they would be the only ones requested and fetched. If this field is left blank, a default set of attributes are fetched and returned.
- `cas.authn.attribute-repository.azure-active-directory[0].case-insensitive` (Optional; type `boolean`): Whether attribute repository should consider the underlying attribute names in a case-insensitive manner.
- `cas.authn.attribute-repository.azure-active-directory[0].domain` (Optional; type `String`): Domain that is appended to usernames when doing lookups. The `@` is automatically included.
- `cas.authn.attribute-repository.azure-active-directory[0].grant-type` (Optional; type `String`): Grant type used to fetch access tokens; defaults to `client_credentials`.
- `cas.authn.attribute-repository.azure-active-directory[0].id` (Optional; type `String`): A value can be assigned to this field to uniquely identify this resolver.
- `cas.authn.attribute-repository.azure-active-directory[0].logging-level` (Optional; type `String`): Adjust the logging level of the API calls. Defaults to `basic`. Accepted values are `none,headers,basic,body`.
- `cas.authn.attribute-repository.azure-active-directory[0].login-base-url` (Optional; type `String`): Base login url used to fetch access tokens.
- `cas.authn.attribute-repository.azure-active-directory[0].order` (Optional; type `int`): The order of this attribute repository in the chain of repositories. Can be used to explicitly position this source in chain and affects merging strategies.
- `cas.authn.attribute-repository.azure-active-directory[0].resource` (Optional; type `String`): Resource to fetch access tokens for; defaults to the graph api url.
- `cas.authn.attribute-repository.azure-active-directory[0].scope` (Optional; type `String`): Scope used when fetching access tokens.
- `cas.authn.attribute-repository.azure-active-directory[0].tenant` (Optional; type `String`): The microsoft tenant id.
