---
layout: default
title: CAS - Attribute Release Policies
category: Attributes
---

{% include variables.html %}

# Attribute Release Policy - External Groovy

Identical to inline groovy attribute definitions, except the groovy script can 
also be externalized to a `.groovy` file:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "sample",
  "name" : "sample",
  "id" : 300,
  "attributeReleasePolicy" : {
    "@class" : "org.apereo.cas.services.ReturnMappedAttributeReleasePolicy",
    "allowedAttributes" : {
      "@class" : "java.util.TreeMap",
      "uid" : "file:/etc/cas/sample.groovy"
    }
  }
}
```

The `sample.groovy` script itself may have the following outline:

```groovy
import java.util.*

def run(final Object... args) {
    def attributes = args[0]
    def logger = args[1]
    logger.debug("Current attributes are {}", attributes)
    return []
}
```         

The configuration of this component qualifies to use the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax.

