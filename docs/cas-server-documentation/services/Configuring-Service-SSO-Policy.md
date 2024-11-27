---
layout: default
title: CAS - Configuring Service SSO Policy
category: Services
---

{% include variables.html %}

# Configuring Service SSO Policy

Single Sign-on participation policies designed on a per-service basis should override the global SSO behavior. Such policies generally are applicable
to participation in single sign-on sessions, creating SSO cookies, etc. 

## Disable Service SSO Access

Participation in existing single sign-on sessions can be disabled on a per-application basis. For example,
the following service will be challenged to present credentials every time, thereby not using SSO:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "...",
  "name" : "...",
  "id" : 1,
  "accessStrategy" : {
    "@class" : "org.apereo.cas.services.DefaultRegisteredServiceAccessStrategy",
    "ssoEnabled" : false
  }
}
```

## Single Sign-on Cookie

CAS adopters may want to allow for a behavior where logging in to a non-SSO-participating application
via CAS either does not create a CAS SSO session and the SSO session it creates is not honored for authenticating subsequently
to an SSO-participating application. This behavior can be defined on a per-service basis.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "...",
  "name" : "...",
  "id" : 1,
  "singleSignOnParticipationPolicy": {
    "@class": "org.apereo.cas.services.DefaultRegisteredServiceSingleSignOnParticipationPolicy",
    "createCookieOnRenewedAuthentication": "TRUE"
  }
}
```         

Acceptable values for `createCookieOnRenewedAuthentication` are `TRUE`, `FALSE` or `UNDEFINED`. 

## Participation Policies

Additional policies can be assigned to each service definition to control participation of an application in an existing single sign-on session.
If conditions hold true, CAS shall honor the existing SSO session and will not challenge the user for credentials. If conditions fail, then
user may be asked for credentials. Such policies can be chained together and executed in order.
        
{% tabs ssoservicepolicy %}

{% tab ssoservicepolicy Authentication Date <i class="fa fa-calendar-days px-1"></i> %}

Honor the existing single sign-on session, if any, if the authentication date is at most `5` seconds old. Otherwise, challenge
the user for credentials and ignore the existing session.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "...",
  "name" : "...",
  "id" : 1,
  "singleSignOnParticipationPolicy":
    {
      "@class": "org.apereo.cas.services.ChainingRegisteredServiceSingleSignOnParticipationPolicy",
      "policies": [ "java.util.ArrayList",
        [
          {
            "@class": "org.apereo.cas.services.AuthenticationDateRegisteredServiceSingleSignOnParticipationPolicy",
            "timeUnit": "SECONDS",
            "timeValue": 5,
            "order": 0
          }
        ]
      ]
    }
}
```

{% endtab %}

{% tab ssoservicepolicy <i class="fa fa-clock px-1"></i>Last Used Time %}

Honor the existing single sign-on session, if any, if the last time an SSO session was used is at most `5` seconds old. Otherwise, challenge the
user for credentials and ignore the existing session.

The policy calculation here typically includes evaluating the last-used-time of the ticket-granting ticket linked to the SSO session to check whether
the ticket continues to actively issue service tickets, etc.

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "...",
  "name" : "...",
  "id" : 1,
  "singleSignOnParticipationPolicy":
    {
      "@class": "org.apereo.cas.services.ChainingRegisteredServiceSingleSignOnParticipationPolicy",
      "policies": [ "java.util.ArrayList",
        [
          {
            "@class": "org.apereo.cas.services.LastUsedTimeRegisteredServiceSingleSignOnParticipationPolicy",
            "timeUnit": "SECONDS",
            "timeValue": 5,
            "order": 0
          }
        ]
      ]
    }
}
```

{% endtab %}

{% tab ssoservicepolicy Attributes <i class="fa fa-id-card px-1"></i> %}

The policy calculation here typically includes evaluating all authentication and principal attributes linked to the SSO session to check whether
the ticket-granting ticket may continue to actively issue service tickets, etc. Each attribute defined in the policy will be examined against each principal
and authentication attribute and for each match, the attribute value is then examined against the defined pattern in the policy.
Successful matches allow for SSO participation. The `requireAllAttributes` flag controls whether *all* attribute conditions defined the policy
must produce successful matches. 

Note that the regular expression matching strategy is defined as *non-eager* and both attribute names
and value patterns defined in the policy are able to support the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html).

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "...",
  "name" : "...",
  "id" : 1,
  "singleSignOnParticipationPolicy":
    {
      "@class": "org.apereo.cas.services.ChainingRegisteredServiceSingleSignOnParticipationPolicy",
      "policies": [ "java.util.ArrayList",
        [
          {
            "@class":"org.apereo.cas.services.AttributeBasedRegisteredServiceSingleSignOnParticipationPolicy",
            "attributes":{
                "@class": "java.util.HashMap",
                "cn": [ "java.util.ArrayList", ["\\d/\\d/\\d"] ]
            },
            "requireAllAttributes": false
          }
        ]
      ]
    }
}
```

{% endtab %}

{% tab ssoservicepolicy <i class="fa fa-code px-1"></i>Groovy %}

SSO participation decisions can be determined using a Groovy script, that may be defined either inline
or outsourced to an external Groovy script.

Below shows the option where you define an external Groovy script:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "...",
  "name" : "...",
  "id" : 1,
  "singleSignOnParticipationPolicy":
    {
      "@class": "org.apereo.cas.services.ChainingRegisteredServiceSingleSignOnParticipationPolicy",
      "policies": [ "java.util.ArrayList",
        [
          {
            "@class":"org.apereo.cas.services.GroovyRegisteredServiceSingleSignOnParticipationPolicy",
            "groovyScript" : "file:///path/to/script.groovy"
          }
        ]
      ]
    }
}
```

The script itself may be designed as:

```groovy
def run(Object[] args) {
    def (registeredService,authentication,logger) = args
    logger.info("Checking SSO participation for ${registeredService.name}")
    
    def principal = authentication.principal
    logger.info("Principal id is ${principal.id}")
    if (principal.id == 'Gandalf') {
        logger.info("User is too powerful; SSO participation is allowed")
        return true
    }
    return false
}
```

The following parameters are passed to the script:

| Parameter           | Description                                                                 |
|---------------------|-----------------------------------------------------------------------------|
| `registeredService` | `RegisteredService` definition that is operating and owning the policy.     |
| `authentication`    | `Authentication` object that carries the authenticated user and attributes. |
| `logger`            | The object responsible for issuing log messages such as `logger.info(...)`. |

  
You may also do the same sort of thing with an inline Groovy script:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "...",
  "name" : "...",
  "id" : 1,
  "singleSignOnParticipationPolicy": {
    "@class":"org.apereo.cas.services.GroovyRegisteredServiceSingleSignOnParticipationPolicy",
    "groovyScript" : "groovy { authentication.principal.id == 'Gandalf' }"
  }
}
```

To prepare CAS to support and integrate with Apache Groovy, please [review this guide](../integration/Apache-Groovy-Scripting.html).

{% endtab %}

{% tab ssoservicepolicy <i class="fa fa-code px-1"></i> Custom %}

Participation in a single sign-on session can be customized and controlled using custom strategies registered with CAS per the below syntax:

```java
@Bean
public SingleSignOnParticipationStrategyConfigurer customSsoConfigurer() {
    return chain -> chain.addStrategy(...);
}
```

[See this guide](../configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.

{% endtab %}

{% endtabs %}
