---
layout: default
title: CAS - Multifactor Authentication Triggers
category: Multifactor Authentication
---

{% include variables.html %}

# Groovy Per Application - Multifactor Authentication Triggers

You may determine the multifactor authentication policy for a registered service using a Groovy script. 

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).

The parameters passed are as follows:

| Parameter            | Description                                                                     |
|----------------------|---------------------------------------------------------------------------------|
| `registeredService`  | The object representing the corresponding service definition in the registry.   |
| `authentication`     | The object representing the `Authentication` object.                            |
| `httpRequest`        | The object representing the HTTP servlet request.                               |
| `service`            | The object representing the service request, associated with this http request. |
| `applicationContext` | The object representing the Spring application context.                         |
| `logger`             | The object responsible for issuing log messages such as `logger.info(...)`.     |

The expected outcome of the script is either `null` in case multifactor authentication should be skipped by this trigger,
or the identifier of the multifactor provider that should be considered for activation.
         
{% tabs groovymfaperapp %}

{% tab groovymfaperapp <i class="fa fa-file-code px-1"></i>File %}

The script may be defined in the service definition using its full path:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^(https|imaps)://.*",
  "id" : 100,
  "name": "test",
  "multifactorPolicy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
    "script" : "file:///etc/cas/config/mfa-policy.groovy"
  }
}
``` 

The script itself may be designed as follows:

```groovy
def run(final Object... args) {
    def (authentication,registeredService,httpRequest,service,applicationContext,logger) = args
    logger.debug("Determine mfa provider for ${registeredService.name} and ${authentication.principal.id}")
    def memberOf = authentication.principal.attributes['memberOf'] as List
    return memberOf.contains('CN=NEED-MFA') ? 'mfa-duo' : null
}
``` 

The `script` attribute supports the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax.

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).

{% endtab %}

{% tab groovymfaperapp <i class="fa fa-pencil px-1"></i>Inline %}

The script may be embedded directly in the service definition, as such:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^(https|imaps)://.*",
  "id" : 100,
  "name": "test",
  "multifactorPolicy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy",
    "script" : "groovy { ... }"
  }
}
```

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).

{% endtab %}

{% endtabs %}
