---
layout: default
title: CAS - Authentication Interrupt
category: Webflow Management
---

{% include variables.html %}

# Groovy Authentication Interrupt

This strategy reaches out to a Groovy resource whose job is to dynamically 
calculate whether the authentication flow should be interrupted given the 
provided username and certain number of other parameters.

The script may be defined as:

```groovy
import org.apereo.cas.interrupt.InterruptResponse

def run(final Object... args) {
    def principal = args[0]
    def attributes = args[1]
    def service = args[2]
    def registeredService = args[3]
    def requestContext = args[4]
    def logger = args[5]

    ...
    def block = false
    def ssoEnabled = true

    return new InterruptResponse("Message", [link1:"google.com", link2:"yahoo.com"], block, ssoEnabled)
}
```

{% include_cached casproperties.html properties="cas.interrupt.groovy" %}

The following parameters are passed to the script:

| Parameter             | Description
|------------------------------------------------------------------------------------------------------------------------
| `principal`           | Authenticated principal.
| `attributes`          | A map of type `Map<String, Object>` that contains both principal and authentication attributes. 
| `service`             | The `Service` object representing the requesting application.
| `registeredService`   | The `RegisteredService` object representing the service definition in the registry.
| `requestContext`      | The object representing the Spring Webflow `RequestContext`.
| `logger`              | The object responsible for issuing log messages such as `logger.info(...)`.
