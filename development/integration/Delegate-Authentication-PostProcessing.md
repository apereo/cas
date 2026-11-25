# Delegated Authentication Post Processing

Source: https://apereo.github.io/cas/development/integration/Delegate-Authentication-PostProcessing.html

Configured identity providers can go through a post-processing phase before they are presented to the user interface. Post processing operations have the ability to update and adjust the markup and settings for a delegated identity provider to change the title, redirect URL, etc. A common example for a post processor would be execute a server-side auto-redirect to the external identity provider conditionally, without having to wait for the browser to make that redirect.

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.pac4j.core.groovy-provider-post-processor.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

The outline of the Groovy script would be as follows:

```groovy
import org.apereo.cas.web.*
import org.apereo.cas.web.support.*
import org.springframework.webflow.execution.*

def run(Object[] args) {
    def (requestContext,providers,logger) = args
    def provider = providers[0]
    logger.info("Checking provider ${provider.name}...")
    def response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext)
    logger.debug("Redirecting to ${provider.redirectUrl}")
    response.sendRedirect(provider.redirectUrl);
}
```

The parameters passed are as follows:

| Parameter | Description |
| --- | --- |
| `requestContext` | The object representing Spring Webflow’s `RequestContext`. |
| `providers` | The set of delegated identity provider configurations. |
| `logger` | The object responsible for issuing log messages such as `logger.info(...)`. |

## Preprocessing Authentication

You may also opt into preprocessing the delegated authentication event prior to the final step in the process and before an SSO session is created. Preprocessing the authentication here allows one to manipulate and update the final authenticated principal before it’s fully baked into the SSO session. This can be done using the following bean definition and by implementing `DelegatedAuthenticationPreProcessor`:

```java
@Bean
public DelegatedAuthenticationPreProcessor myProcessor() {
    return new MyDelegatedAuthenticationPreProcessor();
}
```

[See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
