---
layout: default
title: CAS - Surrogate Authentication
category: Authentication
---
{% include variables.html %}


# Access Strategy - Surrogate Authentication

Each surrogate account storage is able to determine the list of *impersonatees* to enforce 
authorization rules. Additionally, you may on a per-service level define whether an 
application is authorized to leverage surrogate authentication. The surrogate access 
strategy is only activated if the establish authentication and SSO session is one of impersonation.

See below for the available options.

## Attributes

Decide whether the primary user is tagged with enough attributes and entitlements to 
allow impersonation to execute. In the below example, surrogate access to the 
application matching `testId` is allowed only if the authenticated primary user 
carries an attribute `givenName` which contains a value of `Administrator`.

A sample service definition follows:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.SurrogateRegisteredServiceAccessStrategy",
    "surrogateEnabled" : true,
    "enabled": true,
    "ssoEnabled": true,
    "surrogateRequiredAttributes" : {
      "@class" : "java.util.HashMap",
      "givenName" : [ "java.util.HashSet", [ "Administrator" ] ]
    }
  }
}
```

## Groovy

Decide whether the primary user is allowed to go through impersonation via 
an external Groovy script. A sample service file follows:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.GroovySurrogateRegisteredServiceAccessStrategy",
    "groovyScript": "file:/etc/cas/config/surrogate.groovy"
  }
}
```

The configuration of this component qualifies to use the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax. The Groovy 
script itself may be designed as:

```groovy
import java.util.*

def Object run(final Object... args) {
    def principal = args[0]
    def principalAttributes = args[1]
    def logger = args[2]

    logger.info("Checking for impersonation authz for $principal...")

    // Decide if impersonation is allowed by returning true...
    if (principal.equals("casuser")) {
        return true
    }
    logger.warn("User is not allowed to proceed with impersonation!")
    return false
}
```

The parameters passed are as follows:

| Parameter             | Description
|-----------------------|-------------------------------------------------------------------------------------------
| `principal`             | Primary/Principal user id.
| `principalAttributes`   | Principal attributes collected for the primary user.
| `logger`              | The object responsible for issuing log messages such as `logger.info(...)`.
