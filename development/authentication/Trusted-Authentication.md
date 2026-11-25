# Trusted Authentication

Source: https://apereo.github.io/cas/development/authentication/Trusted-Authentication.html

The trusted authentication handler provides support for trusting authentication performed by some other component in the HTTP request handling chain. Proxies (including Apache in a reverse proxy scenario) are the most common components that perform authentication in front of CAS.

Trusted authentication handler support is enabled by including the following dependency in the WAR overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-trusted-webflow"
}
```

Trusted authentication is able to extract the remote authenticated user via the following ways:

1. Username may be extracted from `HttpServletRequest#getRemoteUser()`
2. Username may be extracted from `HttpServletRequest#getUserPrincipal()`
3. Username may be extracted from a request header whose name is defined in CAS settings.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.trusted.person-directory.principal-transformation.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.trusted.attribute-header-patterns` (Optional; type `List<String>`): Regular expression that is applied to all request headers to extract them as principal attributes. The comparison is case insensitive. The pattern should use regular expression groups to extract inner matches from a header value.
- `cas.authn.trusted.name` (Optional; type `String`): Indicates the name of the authentication handler.
- `cas.authn.trusted.order` (Optional; type `Integer`): Order of the authentication handler in the chain.
- `cas.authn.trusted.person-directory.active-attribute-repository-ids` (Optional; type `String`; default `*`): Activated attribute repository identifiers that should be used for fetching attributes if attribute resolution is enabled. The list here may include identifiers separated by comma.
- `cas.authn.trusted.person-directory.attribute-repository-selection` (Optional; type `Map<String,String>`): Control the behavior of the attribute repository selection by authentication method or handler. The map here is keyed by the authentication handler name, and the value is the attribute repository identifiers separated by comma. When the authentication handler is executed, the attribute repositories assigned to this handler will be selected to fetch attributes. Note that the resolution engine will always favor attribute repositories assigned to the service definition, if any and as part of its authentication policy, over this global setting.
- `cas.authn.trusted.person-directory.attribute-resolution-enabled` (Optional; type `TriStateBoolean`; default `undefined`): Whether attribute repositories should be contacted to fetch person attributes. Defaults to true if not set. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.authn.trusted.person-directory.principal-attribute` (Optional; type `String`): Attribute name to use to indicate the identifier of the principal constructed. If the attribute is blank or has no values, the default principal id will be used determined by the underlying authentication engine. The principal id attribute usually is removed from the collection of attributes collected, though this behavior depends on the schematics of the underlying authentication strategy.
- `cas.authn.trusted.person-directory.principal-resolution-conflict-strategy` (Optional; type `String`; default `last`): In the event that the principal resolution engine resolves more than one principal, (especially if such principals in the chain have different identifiers), this setting determines strategy by which the principal id would be chosen from the chain. Accepted values are: `last`, `first`.
- `cas.authn.trusted.person-directory.principal-resolution-failure-fatal` (Optional; type `TriStateBoolean`; default `undefined`): When true, throws an error back indicating that principal resolution has failed and no principal can be found based on the authentication requirements. Otherwise, logs the condition as an error without raising a catastrophic error. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.authn.trusted.person-directory.principal-transformation.blocking-pattern` (Optional; type `String`): A regular expression that will be used against the username to match for blocking/forbidden values. If a match is found, an exception will be thrown and principal transformation will fail.
- `cas.authn.trusted.person-directory.principal-transformation.case-conversion` (Optional; type `PrincipalTransformationProperties.CaseConversion`; default `NONE`): Indicate whether the principal identifier should be transformed into upper-case, lower-case, etc. Available values are as follows: - `NONE`: No conversion. - `LOWERCASE`: Lowercase conversion. - `UPPERCASE`: Uppercase conversion.
- `cas.authn.trusted.person-directory.principal-transformation.pattern` (Optional; type `String`): A regular expression that will be used against the provided username for username extractions. On a successful match, the first matched group in the pattern will be used as the extracted username.
- `cas.authn.trusted.person-directory.principal-transformation.prefix` (Optional; type `String`): Prefix to add to the principal id prior to authentication.
- `cas.authn.trusted.person-directory.principal-transformation.suffix` (Optional; type `String`): Suffix to add to the principal id prior to authentication.
- `cas.authn.trusted.person-directory.return-null` (Optional; type `TriStateBoolean`; default `undefined`): Return a null principal object if no attributes can be found for the principal. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.authn.trusted.person-directory.use-existing-principal-id` (Optional; type `TriStateBoolean`; default `undefined`): Uses an existing principal id that may have already been established in order to run person directory queries. This is generally useful in situations where authentication is delegated to an external identity provider and a principal is first established to then query an attribute source. Available values are as follows: - `TRUE`: Constant to represent the true state. - `FALSE`: Constant to represent the false state. - `UNDEFINED`: Constant to represent the undefined state.
- `cas.authn.trusted.remote-principal-header` (Optional; type `String`): Indicates the name of the request header that may be extracted from the request as the indicated authenticated userid from the remote authn system.

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

## Header Extraction

By default, request header names that match a certain regular expression pattern whose value also matches a defined pattern are extracted as CAS attributes. For example, you may instruct CAS to extract header names that match the pattern `OPT_(.+)` whose value is defined as `SYS_(.+)`. With this setup, if a request header is defined as `OPT_BUSINESS:SYS_ADMIN`, then final CAS attribute that is extracted would be tagged under the attribute name `BUSINESS` with value(s) `ADMIN`.

If you wish to create your own attribute extraction logic, you will need to design a component and register it with CAS to handle the extraction task:

```java
package org.apereo.cas;

@AutoConfiguration
@EnableConfigurationProperties(CasConfigurationProperties.class)
public class MyConfiguration {

    @Bean
    @RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
    public RemoteRequestPrincipalAttributesExtractor remoteRequestPrincipalAttributesExtractor() {
        return new MyRemoteRequestPrincipalAttributesExtractor();
    }
}
```

[See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
