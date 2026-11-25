# Groovy - Multifactor Authentication Triggers

Source: https://apereo.github.io/cas/development/mfa/Configuring-Multifactor-Authentication-Triggers-Groovy.html

MFA can be triggered based on the results of a groovy script of your own design. The outcome of the script should determine the MFA provider id that CAS should attempt to activate.

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).

The outline of the Groovy script is shown below as a sample:

```groovy
import java.util.*

class SampleGroovyEventResolver {
    def run(final Object... args) {
        def (service,registeredService,authentication,httpRequest,logger) = args
        ...
        return "mfa-duo"
    }
}
```

The parameters passed are as follows:

| Parameter | Description |
| --- | --- |
| `service` | The object representing the incoming service provided in the request, if any. |
| `registeredService` | The object representing the corresponding service definition in the registry. |
| `authentication` | The object representing the established authentication event, containing the principal. |
| `httpRequest` | The object representing the `HttpServletRequest`. |
| `logger` | The object responsible for issuing log messages such as `logger.info(...)`. |

As an example, the following script triggers multifactor authentication via [Duo Security](https://apereo.github.io/cas/development/mfa/DuoSecurity-Authentication.html), if the requesting application is `https://www.example.com` and the authenticated principal contains a `mail` attribute whose values contain `email@example.org`.

```groovy
import java.util.*

class MyExampleScript {
    String run(final Object... args) {
        def (service,registeredService,authentication,httpRequest,logger) = args
        if (service.id == "https://www.example.com") {
            logger.info("Evaluating principal attributes [{}]", authentication.principal.attributes)

            def mail = authentication.principal.attributes['mail']
            if (mail.contains("email@example.org")) {
                logger.info("Found mail attribute with value [{}]", mail)
                return "mfa-duo"
            }
        }
        return null
    }
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.authn.mfa.groovy-script.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).
