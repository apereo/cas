# Delegated Authentication - Auto Redirection/Selection

Source: https://apereo.github.io/cas/development/integration/Delegate-Authentication-Redirection.html

By default, the list of available identity providers are displayed in a selection menu and the user is allowed to choose the provider. In certain scenarios, the browser may be instructed to auto-redirect to a preselected identity provider.

## Explicit Auto-Redirection

CAS can be instructed to explicitly redirect to an external identity provider via a dedicated link. For example, you may redirect to the identity provider that is identified as `Twitter` in the CAS configuration via:

```bash
https://sso.example.org/cas/clientredirect?client_name=Twitter
```

The example assumes that CAS is configured (by default) to recognize identity provider names as query parameters under the `client_name` parameter.

## Preselected Identity Provider

An identity provider can be instructed via CAS configuration to always perform an auto-redirect, regardless of the application type and/or authentication requests. The selected identity provider is considered by CAS to be the *primary* strategy for handling authentication requests.

## Identity Provider Exclusivity

Authentication requests from the following application will be auto-redirected to the identity provider that is identified as `Twitter` in the CAS configuration, since the delegated authentication policy only allows the single exclusive use of this provider, removing selection menu and the ability to choose other alternative authentication methods.

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
      "allowedProviders" : [ "java.util.ArrayList", [ "Twitter" ] ],
      "exclusive": true
    }
  }
}
```

## Identity Provider Cookie Selection

A chosen identity provider from the selection menu can be optionally tracked and stored using a dedicated cookie, which will then be used on subsequent attempts to auto-redirect to the identity provider, skipping the selection menu.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.pac4j.cookie.auto-configure-cookie-path` (Optional; type `Boolean`; default `true`): Decide if cookie paths should be automatically configured based on the application context path, when the cookie path is not configured.
- `cas.authn.pac4j.cookie.domain` (Optional; type `String`): Cookie domain. Specifies the domain within which this cookie should be presented. The form of the domain name is specified by RFC 2965. A domain name begins with a dot (.foo.com) and means that the cookie is visible to servers in a specified Domain Name System (DNS) zone (for example, www.foo.com, but not a.b.foo.com). By default, cookies are only returned to the server that sent them.
- `cas.authn.pac4j.cookie.enabled` (Optional; type `Boolean`; default `false`): Determine whether cookie settings should be enabled to track delegated authentication choices and identity providers.
- `cas.authn.pac4j.cookie.http-only` (Optional; type `Boolean`; default `true`): true if this cookie contains the HttpOnly attribute. This means that the cookie should not be accessible to scripting engines, like javascript.
- `cas.authn.pac4j.cookie.max-age` (Optional; type `String`; default `-1`): The maximum age of the cookie, specified in seconds. By default, `-1` indicating the cookie will persist until browser shutdown. A positive value indicates that the cookie will expire after that many seconds have passed. Note that the value is the maximum age when the cookie will expire, not the cookie's current age. A negative value means that the cookie is not stored persistently and will be deleted when the Web browser exits. A zero value causes the cookie to be deleted.
- `cas.authn.pac4j.cookie.name` (Optional; type `String`): Cookie name. Constructs a cookie with a specified name and value. The name must conform to RFC 2965. That means it can contain only ASCII alphanumeric characters and cannot contain commas, semicolons, or white space or begin with a `$` character. The cookie's name cannot be changed after creation. By default, cookies are created according to the RFC 2965 cookie specification. Cookie names are automatically calculated assigned by CAS at runtime, and there is usually no need to customize the name or assign it a different value unless a special use case warrants the change.
- `cas.authn.pac4j.cookie.path` (Optional; type `String`): Cookie path. Specifies a path for the cookie to which the client should return the cookie. The cookie is visible to all the pages in the directory you specify, and all the pages in that directory's subdirectories. A cookie's path must include the servlet that set the cookie, for example, /catalog, which makes the cookie visible to all directories on the server under /catalog. Consult RFC 2965 (available on the Internet) for more information on setting path names for cookies.
- `cas.authn.pac4j.cookie.same-site-policy` (Optional; type `String`): If a cookie is only intended to be accessed in a first party context, the developer has the option to apply one of settings `SameSite=Lax` or `SameSite=Strict` or `SameSite=None` to prevent external access. To safeguard more websites and their users, the new secure-by-default model assumes all cookies should be protected from external access unless otherwise specified. Developers must use a new cookie setting, `SameSite=None`, to designate cookies for cross-site access. When the `SameSite=None` attribute is present, an additional `Secure` attribute is used so cross-site cookies can only be accessed over HTTPS connections. Accepted values are: - `Lax` - `Strict` - `None` - `Off`: Disable the generation of the SameSite cookie attribute altogether. - Path to a Groovy script that is able to generate the SameSite cookie attribute dynamically. - Fully qualified name of a class that implements `org.apereo.cas.web.cookie.CookieSameSitePolicy`
- `cas.authn.pac4j.cookie.secure` (Optional; type `Boolean`; default `true`): True if sending this cookie should be restricted to a secure protocol, or false if the it can be sent using any protocol.

## Identity Provider Groovy Selection

The auto-redirection strategy of a given identity provider may also be decided dynamically via a Groovy resource whose path is defined via CAS settings.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.pac4j.core.groovy-redirection-strategy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

The Groovy script would have the following outline:

```groovy
import org.apereo.cas.web.*
import org.pac4j.core.context.*
import org.apereo.cas.pac4j.*
import org.apereo.cas.web.support.*
import java.util.stream.*
import java.util.*
import org.apereo.cas.configuration.model.support.delegation.*

def run(Object[] args) {
    def (requestContext,service,registeredService,providers,applicationContext,logger) = args
    providers.forEach(provider -> {
        logger.info("Checking ${provider.name}...")
        if (provider.name.equals("Twitter")) {
            provider.autoRedirectType = DelegationAutoRedirectTypes.CLIENT
            return provider
        }
    })
    return null
}
```

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).

The following parameters are passed to the script:

| Parameter | Description |
| --- | --- |
| `requestContext` | Reference to the Spring Webflow request context, as `RequestContext`. |
| `service` | Reference to the application authentication request as `Service`, if any. |
| `registeredService` | Reference to registered service definition, if any. |
| `providers` | Reference to the set of identity provider configuration identified as `DelegatedClientIdentityProviderConfiguration`. |
| `applicationContext` | Reference to the application context as `ApplicationContext`. |
| `logger` | The object responsible for issuing log messages such as `logger.info(...)`. |

## Identity Provider Selection Per Service

The auto-redirection strategy of a given identity provider may also be decided dynamically via a Groovy resource whose path specified directly in the service definition as part of the authentication policy’s provider selection strategy:

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
      "selectionStrategy": "file:/path/to/script.groovy"
    }
  }
}
```

> **Usage**
>
> If you wish, you may also use a *Groovy Inline* syntax using the `groovy {...}` construct.

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html). The collection of parameters and the script body are identical to the *Identity Provider Groovy Selection* option above.

## Identity Provider Custom Selection

If you wish to create your own redirection strategy, you will need to design a component and register it with CAS as such:

```java
@Bean
public DelegatedClientIdentityProviderRedirectionStrategy delegatedClientIdentityProviderRedirectionStrategy() {
    return new CustomDelegatedClientIdentityProviderRedirectionStrategy(); 
}
```

[See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
