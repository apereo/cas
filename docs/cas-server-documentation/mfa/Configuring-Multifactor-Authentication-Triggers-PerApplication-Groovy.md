---
layout: default
title: CAS - Multifactor Authentication Triggers
category: Multifactor Authentication
---

{% include variables.html %}

# Groovy Per Application - Multifactor Authentication Triggers

You may determine the multifactor authentication policy for a registered service using a Groovy script:

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

The script may also be embedded directly in the service definition, as such:

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

The script itself may be designed as follows:

```groovy
def run(final Object... args) {
    def authentication = args[0]
    def registeredService = args[1]
    def httpRequest = args[2]
    def service = args[3]
    def applicationContext = args[4]
    def logger = args[5]

    logger.debug("Determine mfa provider for ${registeredService} and ${authentication}")
    return "mfa-duo"
}
```  

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
