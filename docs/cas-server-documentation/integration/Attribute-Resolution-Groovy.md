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

def Map<String, List<Object>> run(final Object... args) {
    def username = args[0]
    def attributes = args[1]
    def logger = args[2]
    def properties = args[3]
    def appContext = args[4]

    logger.debug("[{}]: The received uid is [{}]", this.class.simpleName, uid)
    return[username:[uid], likes:["cheese", "food"], id:[1234,2,3,4,5], another:"attribute"]
}
```
