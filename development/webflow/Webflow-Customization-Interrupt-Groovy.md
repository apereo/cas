# Groovy Authentication Interrupt

Source: https://apereo.github.io/cas/development/webflow/Webflow-Customization-Interrupt-Groovy.html

This strategy reaches out to a Groovy resource whose job is to dynamically calculate whether the authentication flow should be interrupted given the provided username and certain number of other parameters.

The script may be defined as:

```groovy
import org.apereo.cas.interrupt.InterruptResponse

def run(final Object... args) {
    def (principal,attributes,service,registeredService,logger) = args
    ...
    def block = false
    def ssoEnabled = true

    return new InterruptResponse("Message", [link1:"google.com", link2:"yahoo.com"], block, ssoEnabled)
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.interrupt.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

The following parameters are passed to the script:

| Parameter | Description |
| --- | --- |
| `principal` | Authenticated principal. |
| `attributes` | A map of type `Map<String, Object>` that contains both principal and authentication attributes. |
| `service` | The `Service` object representing the requesting application. |
| `registeredService` | The `RegisteredService` object representing the service definition in the registry. |
| `logger` | The object responsible for issuing log messages such as `logger.info(...)`. |

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).
