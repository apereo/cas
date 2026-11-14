# Delegated Authentication - Authentication Policy

Source: https://apereo.github.io/cas/development/integration/Delegate-Authentication-AuthenticationPolicy.html

Service definitions may be conditionally authorized to use an external identity provider by defining their own access strategy and authentication policy:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 100,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "delegatedAuthenticationPolicy" : {
      "@class" : "org.apereo.cas.services.DefaultRegisteredServiceDelegatedAuthenticationPolicy",
      "allowedProviders" : [ "java.util.ArrayList", [ "Facebook", "Twitter" ] ],
      "permitUndefined": true,
      "exclusive": false,
      "selectionStrategy": "file:/path/to/script.groovy"
    }
  }
}
```

The following fields are supported for the authentication policy:

| Type | Description |
| --- | --- |
| `allowedProviders` | The list of allowed providers should contain the external identity provider names (i.e. client names). |
| `permitUndefined` | Decides whether access should be granted in the event that no allowed providers are defined explicitly. |
| `exclusive` | Decides whether authentication should be exclusively limited to allowed providers, disabling other methods such as username/password, etc. |
| `selectionStrategy` | Decides how to [select and redirect](https://apereo.github.io/cas/development/integration/Delegate-Authentication-Redirection.html) to the identity provider in a scripted fashion. |

## Configuration

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.pac4j.core.discovery-selection.json.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.pac4j.core.discovery-selection.selection-type` (Required; type `Pac4jDelegatedAuthenticationDiscoverySelectionProperties.Pac4jDelegatedAuthenticationSelectionTypes`; default `MENU`): Indicate how the selection and presentation of identity providers would be controlled. Available values are as follows: - `MENU`: Defined identity providers will be listed for the user to select. - `DYNAMIC`: Defined identity providers are pre-built first, and one is chosen dynamically at runtime based on user attributes properties, domain identifier, etc.
- `cas.authn.pac4j.core.groovy-authentication-request-customizer.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.pac4j.core.groovy-provider-post-processor.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.pac4j.core.groovy-redirection-strategy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.pac4j.core.principal-transformation.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.pac4j.core.session-replication.cookie.crypto.encryption.key` (Required; type `String`): The encryption key is a string whose length is defined by the encryption key size setting.
- `cas.authn.pac4j.core.session-replication.cookie.crypto.signing.key` (Required; type `String`): The signing key is a string whose length is defined by the signing key size setting.
- `cas.authn.pac4j.core.allow-impersonation` (Optional; type `Boolean`; default `false`): When set to `true`, delegated authentication attrmpts that return to CAS and have successfully completed will be put through the account selection process of impersonation/surrogacy flow, assuming the feature is turned on and available to CAS. By default, this is off which means no externally-authenticated account is put through the impersonation flow.
- `cas.authn.pac4j.core.cache-duration` (Optional; type `String`; default `PT8H`): Control the expiration policy of the cache that holds onto the results.
- `cas.authn.pac4j.core.cache-size` (Optional; type `Long`; default `100`): Control the size of the delegated identity provider cache that holds identity providers. This setting specifies the maximum number of entries the cache may contain. Note that the cache **may evict an entry before this limit is exceeded or temporarily exceed the threshold while evicting**. As the cache size grows close to the maximum, the cache evicts entries that are less likely to be used again. For example, the cache may evict an entry because it hasn't been used recently or very often.
- `cas.authn.pac4j.core.discovery-selection.json.principal-attribute` (Optional; type `String`): The name of the principal attribute whose values will be compared against the key pattern defined in the configuration rules. If a match is found, then the provider configuration block will be used as the selected provider. The matching routine will examine all attribute values linked to the principal attribute to find any acceptable match. When this setting left undefined, then the resolved principal id for the given user identifier will be used to locate the provider.
- `cas.authn.pac4j.core.lazy-init` (Optional; type `Boolean`; default `true`): Whether initialization of delegated identity providers should be done eagerly typically during startup.
- `cas.authn.pac4j.core.name` (Optional; type `String`): The name of the authentication handler in CAS used for delegation.
- `cas.authn.pac4j.core.order` (Optional; type `Integer`): Order of the authentication handler in the chain.
- `cas.authn.pac4j.core.principal-id-attribute` (Optional; type `String`): The attribute to use as the principal identifier built during and upon a successful authentication attempt.
- `cas.authn.pac4j.core.principal-transformation.blocking-pattern` (Optional; type `String`): A regular expression that will be used against the username to match for blocking/forbidden values. If a match is found, an exception will be thrown and principal transformation will fail.
- `cas.authn.pac4j.core.principal-transformation.case-conversion` (Optional; type `PrincipalTransformationProperties.CaseConversion`; default `NONE`): Indicate whether the principal identifier should be transformed into upper-case, lower-case, etc. Available values are as follows: - `NONE`: No conversion. - `LOWERCASE`: Lowercase conversion. - `UPPERCASE`: Uppercase conversion.
- `cas.authn.pac4j.core.principal-transformation.pattern` (Optional; type `String`): A regular expression that will be used against the provided username for username extractions. On a successful match, the first matched group in the pattern will be used as the extracted username.
- `cas.authn.pac4j.core.principal-transformation.prefix` (Optional; type `String`): Prefix to add to the principal id prior to authentication.
- `cas.authn.pac4j.core.principal-transformation.suffix` (Optional; type `String`): Suffix to add to the principal id prior to authentication.
- `cas.authn.pac4j.core.session-replication.cookie.allowed-ip-addresses-pattern` (Optional; type `String`): A regular expression pattern that indicates the set of allowed IP addresses, when `#isPinToSession()` is configured. In the event that there is a mismatch between the cookie IP address and the current request-provided IP address (i.e. network switches, VPN, etc), the cookie can still be considered valid if the new IP address matches the pattern specified here.
- `cas.authn.pac4j.core.session-replication.cookie.auto-configure-cookie-path` (Optional; type `Boolean`; default `true`): Decide if cookie paths should be automatically configured based on the application context path, when the cookie path is not configured. @deprecated since 7.3.0
- `cas.authn.pac4j.core.session-replication.cookie.crypto.alg` (Optional; type `String`): The signing/encryption algorithm to use.
- `cas.authn.pac4j.core.session-replication.cookie.crypto.enabled` (Optional; type `Boolean`; default `true`): Whether crypto operations are enabled.
- `cas.authn.pac4j.core.session-replication.cookie.crypto.encryption.key-size` (Optional; type `Integer`; default `512`): The encryption key size.
- `cas.authn.pac4j.core.session-replication.cookie.crypto.signing.key-size` (Optional; type `Integer`; default `512`): The signing key size.
- `cas.authn.pac4j.core.session-replication.cookie.crypto.strategy-type` (Optional; type `String`; default `ENCRYPT_AND_SIGN`): Control the cipher sequence of operations. The accepted values are: - `ENCRYPT_AND_SIGN`: Encrypt the value first, and then sign. - `SIGN_AND_ENCRYPT`: Sign the value first, and then encrypt.
- `cas.authn.pac4j.core.session-replication.cookie.domain` (Optional; type `String`): Cookie domain. Specifies the domain within which this cookie should be presented. The form of the domain name is specified by RFC 2965. A domain name begins with a dot (.foo.com) and means that the cookie is visible to servers in a specified Domain Name System (DNS) zone (for example, www.foo.com, but not a.b.foo.com). By default, cookies are only returned to the server that sent them.
- `cas.authn.pac4j.core.session-replication.cookie.geo-locate-client-session` (Optional; type `Boolean`; default `false`): When set to `true` and assuming `#isPinToSession()` is also `true`, client sessions (using the client IP address) are geo-located using a geolocation service when/if configured. The resulting session is either pinned to the client geolocation, or the default client address.
- `cas.authn.pac4j.core.session-replication.cookie.http-only` (Optional; type `Boolean`; default `true`): true if this cookie contains the HttpOnly attribute. This means that the cookie should not be accessible to scripting engines, like javascript.
- `cas.authn.pac4j.core.session-replication.cookie.max-age` (Optional; type `String`; default `-1`): The maximum age of the cookie, specified in seconds. By default, `-1` indicating the cookie will persist until browser shutdown. A positive value indicates that the cookie will expire after that many seconds have passed. Note that the value is the maximum age when the cookie will expire, not the cookie's current age. A negative value means that the cookie is not stored persistently and will be deleted when the Web browser exits. A zero value causes the cookie to be deleted.
- `cas.authn.pac4j.core.session-replication.cookie.name` (Optional; type `String`): Cookie name. Constructs a cookie with a specified name and value. The name must conform to RFC 2965. That means it can contain only ASCII alphanumeric characters and cannot contain commas, semicolons, or white space or begin with a `$` character. The cookie's name cannot be changed after creation. By default, cookies are created according to the RFC 2965 cookie specification. Cookie names are automatically calculated assigned by CAS at runtime, and there is usually no need to customize the name or assign it a different value unless a special use case warrants the change.
- `cas.authn.pac4j.core.session-replication.cookie.path` (Optional; type `String`): Cookie path. Specifies a path for the cookie to which the client should return the cookie. The cookie is visible to all the pages in the directory you specify, and all the pages in that directory's subdirectories. A cookie's path must include the servlet that set the cookie, for example, /catalog, which makes the cookie visible to all directories on the server under /catalog. Consult RFC 2965 (available on the Internet) for more information on setting path names for cookies.
- `cas.authn.pac4j.core.session-replication.cookie.pin-to-session` (Optional; type `Boolean`; default `true`): When generating cookie values, determine whether the value should be compounded and signed with the properties of the current session, such as IP address, user-agent, etc. Turning off session pinning is not exactly ideal. This is a barrier to prevent cookie replay attacks. A cookie that was created with an IP given a certain location cannot be replayed back in a different browser with a different IP from a different location. There may however be legitimate reasons to turn it off and relax this after careful reviews, if you intend to support a type of user who intends to switch their IP quite frequently. A classic example is the type of user that is on VPN at the time of login, but decides to resume their work a few hours later off VPN or at a different location.
- `cas.authn.pac4j.core.session-replication.cookie.same-site-policy` (Optional; type `String`): If a cookie is only intended to be accessed in a first party context, the developer has the option to apply one of settings `SameSite=Lax` or `SameSite=Strict` or `SameSite=None` to prevent external access. To safeguard more websites and their users, the new secure-by-default model assumes all cookies should be protected from external access unless otherwise specified. Developers must use a new cookie setting, `SameSite=None`, to designate cookies for cross-site access. When the `SameSite=None` attribute is present, an additional `Secure` attribute is used so cross-site cookies can only be accessed over HTTPS connections. Accepted values are: - `Lax` - `Strict` - `None` - `Off`: Disable the generation of the SameSite cookie attribute altogether. - Path to a Groovy script that is able to generate the SameSite cookie attribute dynamically. - Fully qualified name of a class that implements `org.apereo.cas.web.cookie.CookieSameSitePolicy`
- `cas.authn.pac4j.core.session-replication.cookie.secure` (Optional; type `Boolean`; default `true`): True if sending this cookie should be restricted to a secure protocol, or false if the it can be sent using any protocol.
- `cas.authn.pac4j.core.session-replication.replicate-sessions` (Optional; type `Boolean`; default `false`): Indicates whether profiles and other session data, collected as part of authentication flows and protocol requests that are kept by the container session, should be replicated across the cluster using CAS and its own ticket registry. Without this option, profile data and other related pieces of information should be manually replicated via means and libraries outside of CAS. @deprecated since 7.3.0
- `cas.authn.pac4j.core.typed-id-used` (Optional; type `Boolean`; default `false`): When constructing the final user profile from the delegated provider, determines if the provider id should be combined with the principal id.

**Signing & encryption**

This CAS feature is able to accept signing and encryption crypto keys. In most scenarios if keys are not provided, CAS will auto-generate them. The following instructions apply if you wish to manually and beforehand create the signing and encryption keys.

Note that if you are asked to create a [JWK](https://tools.ietf.org/html/rfc7517) of a certain size for the key, you are to use the following set of commands to generate the token:

```bash
wget https://raw.githubusercontent.com/apereo/cas/master/etc/jwk-gen.jar
java -jar jwk-gen.jar -t oct -s [size]
```

The outcome would be similar to:

```json
{
  "kty": "oct",
  "kid": "...",
  "k": "..."
}
```

The generated value for `k` needs to be assigned to the relevant CAS settings. Note that keys generated via the above algorithm are processed by CAS using the Advanced Encryption Standard (`AES`) algorithm which is a specification for the encryption of electronic data established by the U.S. National Institute of Standards and Technology.

---

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

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

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).

## Auto Redirection

[See this guide](https://apereo.github.io/cas/development/integration/Delegate-Authentication-Redirection.html) for better details.
