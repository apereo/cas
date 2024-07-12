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

{% tabs impersonationaccess %}

{% tab impersonationaccess Attributes %}

Decide whether the primary user is tagged with enough attributes and entitlements to
allow impersonation to execute. In the below example, surrogate access to the
application matching `testId` is allowed only if the authenticated primary user
carries an attribute `givenName` which contains a value of `Administrator`.

A sample service definition follows:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.SurrogateRegisteredServiceAccessStrategy",
    "surrogateRequiredAttributes" : {
      "@class" : "java.util.HashMap",
      "givenName" : [ "java.util.HashSet", [ "Administrator" ] ]
    }
  }
}
```

{% endtab %}

{% tab impersonationaccess <i class="fa fa-file-code px-1"></i>Groovy %}

Decide whether the primary user is allowed to go through impersonation via
an external Groovy script. A sample service file follows:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "testId",
  "name" : "testId",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.GroovySurrogateRegisteredServiceAccessStrategy",
    "groovyScript": "file:/etc/cas/config/surrogate.groovy"
  }
}
```

The configuration of this component qualifies to use the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) 
syntax. The Groovy script itself may be designed as:

```groovy
import java.util.*

def run(final Object... args) {
    def (principal,principalAttributes,logger) = args
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

| Parameter             | Description                                                                 |
|-----------------------|-----------------------------------------------------------------------------|
| `principal`           | Primary/Principal user id.                                                  |
| `principalAttributes` | Principal attributes collected for the primary user.                        |
| `logger`              | The object responsible for issuing log messages such as `logger.info(...)`. |


To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).

{% endtab %}
          
{% endtabs %}
  
## Surrogate Authentication Per Application

Surrogate authentication can be selectively controlled for specific applications. By default,
all services and applications are eligible for surrogate authentication and impersonation.

```json
{
  "@class": "org.apereo.cas.services.CasRegisteredService",
  "serviceId": "^https://app.example.org",
  "name": "App",
  "id": 1,
  "surrogatePolicy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceSurrogatePolicy",
    "enabled": false
  }
}
```

The following passwordless policy settings are supported:

| Name      | Description                                                                     |
|-----------|---------------------------------------------------------------------------------|
| `enabled` | Boolean to define whether surrogate authentication is allowed for this service. |
