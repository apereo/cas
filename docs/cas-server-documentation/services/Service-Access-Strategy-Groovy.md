---
layout: default
title: CAS - Configuring Service Access Strategy
category: Services
---

{% include variables.html %}

# Service Access Strategy - Groovy

Access strategy and authorization decision can be carried using a Groovy script for all services and applications. This policy
is not tied to a specific application and is invoked for all services and integrations.

{% include_cached casproperties.html properties="cas.access-strategy.groovy" %}

The outline of the script is as follows:

```groovy
import org.apereo.cas.audit.*
import org.apereo.cas.services.*

def run(Object[] args) {
    def context = args[0] as AuditableContext
    def logger = args[1]
    logger.debug("Checking access for ${context.registeredService}")
    def result = AuditableExecutionResult.builder().build()
    result.setException(new UnauthorizedServiceException("Service unauthorized"))
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

The script itself may be designed as such by overriding the needed operations where necessary:

```groovy
import org.apereo.cas.services.*
import java.util.*

class GroovyRegisteredAccessStrategy implements RegisteredServiceAccessStrategy {
}
```

The configuration of this component qualifies to use the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html)
syntax. Refer to the CAS API documentation to learn more about operations and expected behaviors.
