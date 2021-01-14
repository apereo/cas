---
layout: default
title: CAS - Surrogate Authentication
category: Authentication
---
{% include variables.html %}


# Surrogate Authentication

Surrogate authentication (impersonation), sometimes known as *sudo for the web*, 
is the ability to authenticate on behalf of another user. 

The two actors in this case are:

1. The primary admin user whose credentials are verified upon authentication.
2. The surrogate user, selected by the admin, to which CAS will switch after credential verification and is one that is linked to the single sign-on session.

Example use cases for impersonation include:

1. Logging into an application on behalf of a user to execute and make changes.
2. Troubleshoot a bothersome authentication experience with an application on behalf of another user.

Surrogate authentication is enabled by including the following dependencies in the WAR overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-surrogate-webflow" %}

{% include {{ version }}/surrogate-authentication-configuration.md %}

## Account Storage

The following account stores may be configured and used to locate surrogates authorized for a particular user.

### Static

Surrogate accounts may be defined statically in the CAS configuration. 

{% include {{ version }}/static-accounts-surrogate-authentication-configuration.md %}

### JSON
   
Please [see this guide](Surrogate-Authentication-Storage-JSON.html).

### LDAP

Please [see this guide](Surrogate-Authentication-Storage-LDAP.html).


### CouchDb

Please [see this guide](Surrogate-Authentication-Storage-CouchDb.html).

### JDBC

Please [see this guide](Surrogate-Authentication-Storage-JDBC.html).


### REST

Please [see this guide](Surrogate-Authentication-Storage-REST.html).

### Custom

Please [see this guide](Surrogate-Authentication-Storage-Custom.html).

## Account Selection

The surrogate user selection can happen via the following ways.

### Preselected

This is the case where the surrogate user identity is known 
beforehand and is provided to CAS upon login using a special syntax.
When entering credentials, the following syntax should be used:

```bash
[surrogate-userid][separator][primary-userid]
```

For example, if you are `casuser` and you need to switch to `jsmith` as the 
surrogate user, the credential id provided to CAS would be `jsmith+casuser` where 
the separator is `+` and can be altered via the CAS configuration. You will 
need to provide your own password of course.

### GUI

This is the case where the surrogate user identity is *not* known beforehand, and you wish to choose the account from a pre-populated list. When entering credentials, the following syntax should be used:

```bash
[separator][primary-userid]
```

For example, if you are `casuser` and you need to locate the surrogate account to which you may want to switch, the credential id provided to CAS would be `+casuser` where the separator is `+` and can be altered via the CAS configuration. You will need to provide your own password of course.

## Session Expiration

An impersonation session can be assigned a specific expiration policy that would control how long a surrogate session may last. This means that the SSO session established as part of impersonation will rightly vanish, once the expiration policy dictates as such. It is recommended that you keep the expiration length short (i.e. 30 minutes) to avoid possible security issues.

<div class="alert alert-info"><strong>Remember</strong><p>
The expiration policy assigned to impersonation sessions is expected to be <i>shorter</i> than the <i>normal</i> expiration policy
assigned to non-surrogate sessions. In other words, if the usual expiration policy that controls the single sign-on session is set to last
2 hours, the surrogate session expiration is expected to be a time period less than or equal to 2 hours.
</p></div>

## Surrogate Attributes

Upon a successful surrogate authentication event, the following 
attributes are communicated back to the application in order to detect an impersonation session:

| Attribute             | Instructions
|-----------------------|-------------------------------------------------------------------------------
| `surrogateEnabled`    | Boolean to indicate whether session is impersonated.
| `surrogatePrincipal`  | The admin user whose credentials are validated and acts as the impersonator.
| `surrogateUser`       | The surrogate user that is impersonated.

## Surrogate Access Strategy

Each surrogate account storage is able to determine the list of impersonatees to enforce 
authorization rules. Additionally, you may on a per-service level define whether an 
application is authorized to leverage surrogate authentication. The surrogate access 
strategy is only activated if the establish authentication and SSO session is one of impersonation.

See below for the available options.

### Attributes

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

### Groovy

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

## Surrogate Audits

Surrogate authentication events are by default tracked in the audit logs:

```
=============================================================
WHO: (Primary User: [casuser], Surrogate User: [testuser])
WHAT: ST-1-u_R_SyXJJlENS0fBLwpecNE for https://example.app.edu
ACTION: SERVICE_TICKET_CREATED
APPLICATION: CAS
WHEN: Mon Sep 11 12:55:07 MST 2017
CLIENT IP ADDRESS: 127.0.0.1
SERVER IP ADDRESS: 127.0.0.1
=============================================================
```

Additionally, failure and success events may also communicated via SMS and/or email messages to relevant parties. 
     
{% include casproperties.html properties="cas.authn.surrogate.mail,cas.authn.surrogate.sms" %}

To learn more about available options, please [see this guide](../notifications/SMS-Messaging-Configuration.html) 
or [this guide](../notifications/Sending-Email-Configuration.html).

## REST Protocol

The feature extends the [CAS REST API](../protocol/REST-Protocol.html) communication model to surrogate authentication,
allowing REST credentials to specify a substitute and authenticate on behalf of another user. To activate surrogate authentication
for the CAS REST API, you will need to choose one of the following options:

- Format the credential username using the following syntax:

```bash
[surrogate-userid][separator][primary-userid]
```

- Pass along a special request header `X-Surrogate-Principal` that contains the surrogate userid.
