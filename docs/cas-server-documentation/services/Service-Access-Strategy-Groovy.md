---
layout: default
title: CAS - Service Access Strategy - Groovy
category: Services
---

{% include variables.html %}

# Service Access Strategy - Groovy

Access strategy and authorization decision can be carried using a Groovy script for all services and applications. This policy
is not tied to a specific application and is invoked for all services and integrations.

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).

{% include_cached casproperties.html properties="cas.access-strategy.groovy" %}

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

| Parameter | Description                                                                                                |
|-----------|------------------------------------------------------------------------------------------------------------|
| `context` | An `AuditableContext` object that carries auditable data such as registered services, authentication, etc. |
| `logger`  | The object responsible for issuing log messages such as `logger.info(...)`.                                |

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

The configuration of this component qualifies to use the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html)
syntax. Refer to the CAS API documentation to learn more about operations and expected behaviors.

The script itself may be designed as such by overriding the needed operations where necessary:

```groovy
import org.apereo.cas.services.*
import org.apereo.cas.authentication.principal.*
import org.slf4j.*
import groovy.transform.*

@Field def logger = LoggerFactory.getLogger("GroovyServiceAccessStrategy")

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
     
All operations are seen as optional, and when undefined in the script, 
the end result of the operation is seen as `false` and access is denied.

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).
