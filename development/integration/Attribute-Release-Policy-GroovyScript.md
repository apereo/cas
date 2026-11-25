# Attribute Release Policy - Groovy Script

Source: https://apereo.github.io/cas/development/integration/Attribute-Release-Policy-GroovyScript.html

Let an external Groovy script decide how principal attributes should be released. The configuration of this component qualifies to use the [Spring Expression Language](https://apereo.github.io/cas/development/configuration/Configuration-Spring-Expressions.html) syntax.

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
    def (currentAttributes,logger,principal,service) = args
    logger.debug("Current attributes received are {}", currentAttributes)
    return [username:["something"], likes:["cheese", "food"], id:[1234,2,3,4,5], another:"attribute"]
}
```

> **Usage Warning**
>
> Activating this policy is not without cost, as CAS needs to evaluate the inline script, compile and run it for subsequent executions. While the compiled script is cached and should help with execution performance, as a general rule, you should avoid opting for and designing complicated scripts.

The following parameters are passed to the script:

| Parameter | Description |
| --- | --- |
| `currentAttributes` | `Map` of attributes currently resolved and available for release. |
| `logger` | The object responsible for issuing log messages such as `logger.info(...)`. |
| `principal` | The object representing the authenticated principal. |
| `service` | The object representing the corresponding service definition in the registry. |

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html).
