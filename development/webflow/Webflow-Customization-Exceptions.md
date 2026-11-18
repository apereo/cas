# Webflow Errors Customization

Source: https://apereo.github.io/cas/development/webflow/Webflow-Customization-Exceptions.html

By default CAS is configured to recognize and handle a number of exceptions for web flow during authentication. Each exception has the specific message bundle mapping in `messages.properties`. So that a specific message could be presented to end users on the login form. Any un-recognized or un-mapped exceptions results in the `UNKNOWN` mapping with a generic `Invalid credentials.` message.

To map custom exceptions in the webflow, one would need map the exception in CAS settings and then define the relevant error in `messages.properties`:

```properties
authenticationFailure.MyAuthenticationException=Authentication has failed, but it did it my way!
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.errors.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.
- `cas.authn.errors.exceptions` (Optional; type `List<Class<? extends Throwable>>`): Define custom exceptions that can then be mapped to message bundles for custom error handling. By default CAS is configured to recognize and handle a number of exceptions for during authentication. Each exception has the specific message bundle mapping so that a specific message could be presented to end users on the login form. Any un-recognized or un-mapped exceptions results in a generic message. To map custom exceptions, one would need map the exception, they can be defined here and then linked to custom messages.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

## Groovy Error Handling

Exceptions and authentication errors that are reported back to the webflow may be caught and processed using an external Groovy script. If the script can support and handle the error, its outcome should be an event that the webflow should use as the final destination state:

The outline of the script should match the following:

```groovy
import org.apereo.cas.*
import org.springframework.context.*
import org.springframework.webflow.*
import org.springframework.webflow.action.*
import org.springframework.webflow.execution.*

def run(Object[] args) {
    def (exception,requestContext,applicationContext,logger) = args
    logger.info("Handling {}", exception)
    new EventFactorySupport().event(this, "customEvent")
}

def supports(Object[] args) {
    def (exception,requestContext,applicationContext,logger) = args
    logger.info("Checking to support {}", exception)
    true
}
```

The parameters passed are as follows:

| Parameter | Description |
| --- | --- |
| `exception` | The object representing the error. |
| `requestContext` | The object representing the Spring Webflow `RequestContext`. |
| `applicationContext` | The object representing the Spring `ApplicationContext`. |
| `logger` | The object responsible for issuing log messages such as `logger.info(...)`. |

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).

## Custom Error Handling

For more advanced scenarios, you can also design your webflow exception handler and register it with the CAS at runtime:

```java
@Bean
public CasWebflowExceptionHandler customExceptionHandler(){
    return new CustomCasWebflowExceptionHandler();
}
```

[See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
