# Delegated Authentication Request Customization

Source: https://apereo.github.io/cas/development/integration/Delegate-Authentication-Request-Customization.html

Authentication (HTTP) requests that are sent from CAS to configured identity providers can be customized at runtime. This customization phase of the authentication request happens right before the request is constructed and passed onto the client browser. Before the customizer proceeds, it also must declare its support for the given identity provider and/or request, and it also is given the chance to determine if the identity provider is authorized for the given request.

## Groovy Customization

The authentication request can be customized using an external Groovy script.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.pac4j.core.groovy-authentication-request-customizer.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

The outline of the Groovy script would be as follows:

```groovy
import org.apereo.cas.authentication.principal.*
import org.apereo.cas.web.*
import org.apereo.cas.web.support.*
import org.pac4j.core.client.*
import org.pac4j.core.context.*
import org.springframework.context.*

def run(Object[] args) {
    def (client,webContext,appContext,logger) = args
    logger.info("Checking ${client.name}...")
    webContext.setRequestAttribute("customAttribute", "value")
}

def supports(Object[] args) {
    def (client,webContext,appContext,logger) = args
    logger.info("Checking support for ${client.name}...")
    return true
}

def isAuthorized(Object[] args) {
    def (client,webContext,service,appContext,logger) = args
    logger.info("Checking authorization for ${client.name}...")
    return true
}
```

The parameters passed are as follows:

| Parameter | Description |
| --- | --- |
| `client` | The object representing the identity provider as `IndirectClient`. |
| `webContext` | The object representing the HTTP request/response as `WebContext`. |
| `service` | The `WebApplicationService` for the incoming application request. |
| `appContext` | The Spring `ApplicationContext`. |
| `logger` | The object responsible for issuing log messages such as `logger.info(...)`. |

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).

## Custom

For more advanced scenarios, you can also design your webflow exception handler and register it with the CAS at runtime:

```java
@Bean
public DelegatedClientAuthenticationRequestCustomizer myCustomizer() {
    return new DelegatedClientAuthenticationRequestCustomizer();
}
```

[See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
