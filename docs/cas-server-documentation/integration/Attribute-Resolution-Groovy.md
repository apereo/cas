---
layout: default
title: CAS - Attribute Resolution
category: Attributes
---

{% include variables.html %}

# Groovy Attribute Resolution
     
The following configuration describes how to fetch and retrieve attributes from Groovy attribute repositories.

{% include_cached casproperties.html properties="cas.authn.attribute-repository.groovy" %}

The Groovy script may be designed as:

```groovy
import java.util.*

def run(final Object... args) {
    def (username,attributes,logger,properties,appContext) = args
    logger.debug("[{}]: The received uid is [{}]", this.class.simpleName, username)
    
    // All attribute values must be defined as a collection wrapped in []
    return [username:[username], likes:["cheese", "food"], id:[1234,2,3,4,5], another:["attribute"] ]
}
```

The following parameters are passed to the script:

| Parameter    | Description                                                                         |
|--------------|-------------------------------------------------------------------------------------|
| `username`   | Current principal identifier found from the authentication phase.                   |
| `attributes` | `Map` of query attributes built by CAS to construct the attribute resolution query. |
| `logger`     | The object responsible for issuing log messages such as `logger.info(...)`.         |
| `properties` | CAS configuration properties.                                                       |
| `appContext` | The current and active application context under the type `ApplicationContext`.     |

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).
