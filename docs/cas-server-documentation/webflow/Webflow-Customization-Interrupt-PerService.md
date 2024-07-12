---
layout: default
title: CAS - Authentication Interrupt
category: Webflow Management
---

{% include variables.html %}

# Tracking Authentication Interrupts Per Service

Application definitions may be assigned a dedicated webflow interrupt policy. A sample JSON file follows:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "sample service",
  "id" : 100,
  "webflowInterruptPolicy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceWebflowInterruptPolicy",
    "enabled": true,
    "forceExecution": "TRUE"
  }
}
```

<div class="alert alert-info">:information_source: <strong>Protocol Support</strong>
<p>Authentication interrupts should work for all client application types supported by CAS, regardless of authentication protocol.
Whether your application speaks CAS, SAML2, OpenID Connect, etc, the interrupt policy should equally apply and its method of
configuration in the application definition remains the same.</p></div>

The following policy settings are supported:

| Field             | Description                                                                                                       |
|-------------------|-------------------------------------------------------------------------------------------------------------------|
| `enabled`         | Whether interrupt notifications are enabled for this application. Default is `true`.                              |
| `forceExecution`  | Whether execution should proceed anyway, regardless. Accepted values are `TRUE`, `FALSE` or `UNDEFINED`.          |
       
Additional interrupt triggers are listed below.

{% tabs interrupttriggers %}

{% tab interrupttriggers <i class="fa fa-person px-1"></i>Principal Attribute %}
      
Interrupt triggers per application can be triggered based on the principal attributes. If the defined attribute name
exists and can produce a value to match the defined attribute value, interrupt may be triggered.

| Field             | Description                                                                                                       |
|-------------------|-------------------------------------------------------------------------------------------------------------------|
| `attributeName`   | Regular expression pattern to compare against authentication and principal attribute names to trigger interrupt.  |
| `attributeValue`  | Regular expression pattern to compare against authentication and principal attribute values to trigger interrupt. |

The application definition may be defined as:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "sample service",
  "id" : 100,
  "webflowInterruptPolicy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceWebflowInterruptPolicy",
    "attributeName": "mem...of",
    "attributeValue": "^st[a-z]ff$"
  }
}
```

These fields support the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax.

{% endtab %}

{% tab interrupttriggers <i class="fa fa-code px-1"></i>Groovy Script %}

Interrupt triggers per application can be triggered based on the outcome of an external or inline Groovy script. The script
should return a boolean value, i.e. `true` or `false` to indicate whether interrupt should be triggered.

| Field            | Description                                                                                               |
|------------------|-----------------------------------------------------------------------------------------------------------|
| `groovyScript`   | Inline or external groovy script to determine whether interrupt should be triggered for this application. |

This field supports the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax.

The application definition may be defined as:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://.+",
  "name" : "sample service",
  "id" : 100,
  "webflowInterruptPolicy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceWebflowInterruptPolicy",
    "groovyScript": "file:/path/to/script.groovy"
  }
}
```

An external script itself may be designed in Groovy as:

```groovy
def run(Object... args) {
    def (attributes,username,registeredService,service,logger) = args
    logger.debug("Current attributes received are {}", attributes)
    // Determine whether interrupt should be triggered...
    return true
}
```

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).

<div class="alert alert-info">:information_source: <strong>Usage Warning</strong><p>Activating this mode is not without cost,
as CAS needs to evaluate the script, compile and run it for subsequent executions. While the compiled
script is cached and should help with execution performance, as a general rule, you should avoid opting
for and designing complicated scripts.</p></div>

The following parameters are passed to the script:

| Parameter           | Description                                                                   |
|---------------------|-------------------------------------------------------------------------------|
| `attributes`        | `Map` of attributes currently resolved and available for release.             |
| `username`          | The object representing the authenticated username.                           |
| `registeredService` | The object representing the corresponding service definition in the registry. |
| `service`           | The object representing the service requesting authentication.                |
| `logger`            | The object responsible for issuing log messages such as `logger.info(...)`.   |

{% endtab %}

{% endtabs %}



