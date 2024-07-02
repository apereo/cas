---
layout: default
title: CAS - Webflow Decorations
category: Webflow Management
---

{% include variables.html %}

# Groovy Decorators - Webflow Decorations

Groovy login decorators allow one to inject data into the Spring webflow 
context by using an external Groovy script that may take on the following form:

```groovy
def run(Object[] args) {
    def (requestContext,logger) = args
    logger.info("Decorating the webflow...")
    requestContext.flowScope.put("decoration", ...)
 }
``` 

The parameters passed are as follows:

| Parameter            | Description                                                                   |
|----------------------|-------------------------------------------------------------------------------|
| `requestContext`     | The `RequestContext` that carries various types of scopes as data containers. |
| `logger`             | Logger object used to issue log messages where needed.                        |

{% include_cached casproperties.html properties="cas.webflow.login-decorator.groovy" %}

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).
