---
layout: default
title: CAS - Attribute Release Policies
category: Attributes
---

{% include variables.html %}

# Attribute Release Policy - Groovy Script

Let an external Groovy script decide how principal attributes should be released. The configuration of this
component qualifies to use the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 300,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.GroovyScriptAttributeReleasePolicy",
    "groovyScript" : "classpath:/script.groovy"
  }
}
```

The script itself may be designed in Groovy as:

```groovy
import java.util.*

def run(final Object... args) {
    def currentAttributes = args[0]
    def logger = args[1]
    def principal = args[2]
    def service = args[3]

    logger.debug("Current attributes received are {}", currentAttributes)
    return [username:["something"], likes:["cheese", "food"], id:[1234,2,3,4,5], another:"attribute"]
}
```

<div class="alert alert-info"><strong>Usage Warning</strong><p>Activating this policy is not without cost,
as CAS needs to evaluate the inline script, compile and run it for subsequent executions. While the compiled
script is cached and should help with execution performance, as a general rule, you should avoid opting
for and designing complicated scripts.</p></div>

The following parameters are passed to the script:

| Parameter           | Description                                                                   |
|---------------------|-------------------------------------------------------------------------------|
| `currentAttributes` | `Map` of attributes currently resolved and available for release.             |
| `logger`            | The object responsible for issuing log messages such as `logger.info(...)`.   |
| `principal`         | The object representing the authenticated principal.                          |
| `service`           | The object representing the corresponding service definition in the registry. |

