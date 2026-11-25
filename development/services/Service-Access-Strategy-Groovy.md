# Service Access Strategy - Groovy

Source: https://apereo.github.io/cas/development/services/Service-Access-Strategy-Groovy.html

Access strategy and authorization decision can be carried using a Groovy script for all services and applications. This policy is not tied to a specific application and is invoked for all services and integrations.

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).

The following settings and properties are available from the CAS configuration catalog:

- `cas.access-strategy.groovy.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

The outline of the script is as follows:

```groovy
import org.apereo.cas.audit.*
import org.apereo.cas.services.*

def run(Object[] args) {
    def (context,logger) = args
    logger.debug("Checking access for ${context.registeredService}")
    def result = AuditableExecutionResult.builder().build()
    result.setException(UnauthorizedServiceException.denied("Service unauthorized"))
    return result
}
```

The following parameters are passed to the script:

| Parameter | Description |
| --- | --- |
| `context` | An `AuditableContext` object that carries auditable data such as registered services, authentication, etc. |
| `logger` | The object responsible for issuing log messages such as `logger.info(...)`. |

## Groovy Per Service

This strategy delegates to a Groovy script to dynamically decide the access rules requested by CAS at runtime, for a specific service definition:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.GroovyRegisteredServiceAccessStrategy",
    "groovyScript" : "file:///etc/cas/config/access-strategy.groovy"
  }
}
```

The configuration of this component qualifies to use the [Spring Expression Language](https://apereo.github.io/cas/development/configuration/Configuration-Spring-Expressions.html) syntax. Refer to the CAS API documentation to learn more about operations and expected behaviors.

The script itself may be designed as such by overriding the needed operations where necessary:

```groovy
import org.apereo.cas.services.*
import org.apereo.cas.authentication.principal.*
import org.slf4j.*
import groovy.transform.*

@Field final logger = LoggerFactory.getLogger("GroovyServiceAccessStrategy")

def isServiceAccessAllowed(RegisteredService registeredService, Service service) {
    logger.info("Checking access for ${registeredService} and service ${service}")
    registeredService != null
}

def isServiceAccessAllowedForSso(RegisteredService registeredService) {
    logger.info("Checking SSO access for ${registeredService}")
    registeredService != null
}

def authorizeRequest(RegisteredServiceAccessStrategyRequest request) {
    logger.info("Principal ${request.principalId}, attributes ${request.attributes} and service ${request.service}")
    request.service != null
}
```

All operations are seen as optional, and when undefined in the script, the end result of the operation is seen as `false` and access is denied.

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).
