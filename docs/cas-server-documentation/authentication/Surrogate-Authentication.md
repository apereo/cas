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

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-surrogate-webflow" %}

{% include_cached casproperties.html properties="cas.authn.surrogate.separator,cas.authn.surrogate.tgt" %}

## Account Storage

The following account stores may be configured and used to locate surrogates authorized for a particular user.

### Static

Surrogate accounts may be defined statically in the CAS configuration. 

{% include_cached casproperties.html properties="cas.authn.surrogate.simple.surrogates" %}

### Others

| Storage          | Description                                         
|------------------------------------------------------------------------------------
| JSON             | [See this guide](Surrogate-Authentication-Storage-JSON.html).
| LDAP             | [See this guide](Surrogate-Authentication-Storage-LDAP.html).
| CouchDb          | [See this guide](Surrogate-Authentication-Storage-CouchDb.html).
| JDBC             | [See this guide](Surrogate-Authentication-Storage-JDBC.html).
| REST             | [See this guide](Surrogate-Authentication-Storage-REST.html).
| Custom           | [See this guide](Surrogate-Authentication-Storage-Custom.html).

## Account Selection

Please see [this guide](Surrogate-Authentication-AccountSelection.html).

## Session Expiration

An impersonation session can be assigned a specific expiration policy that would control how long a surrogate session 
may last. This means that the SSO session established as part of impersonation will rightly vanish, once the 
expiration policy dictates as such. It is recommended that you keep the expiration length short (i.e. 30 minutes) to avoid possible security issues.

<div class="alert alert-info"><strong>Remember</strong><p>
The expiration policy assigned to impersonation sessions is expected to be <i>shorter</i> than the <i>normal</i> expiration policy
assigned to non-surrogate sessions. In other words, if the usual expiration policy that controls the single sign-on session is set to last
2 hours, the surrogate session expiration is expected to be a time period less than or equal to 2 hours.
</p></div>

## Surrogate Attributes

Upon a successful surrogate authentication event, the following 
attributes are communicated back to the application in order to detect an impersonation session:

| Attribute            | Instructions                                                                 |
|----------------------|------------------------------------------------------------------------------|
| `surrogateEnabled`   | Boolean to indicate whether session is impersonated.                         |
| `surrogatePrincipal` | The admin user whose credentials are validated and acts as the impersonator. |
| `surrogateUser`      | The surrogate user that is impersonated.                                     |

## Surrogate Access Strategy

Please see [this guide](Surrogate-Authentication-AccessStrategy.html).             

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
     
{% include_cached casproperties.html properties="cas.authn.surrogate.mail,cas.authn.surrogate.sms" %}

To learn more about available options, please [see this guide](../notifications/SMS-Messaging-Configuration.html) 
or [this guide](../notifications/Sending-Email-Configuration.html).
 
## Surrogate Principal Resolution

{% include_cached casproperties.html properties="cas.authn.surrogate.principal" %}

## REST Protocol

The feature extends the [CAS REST API](../protocol/REST-Protocol.html) communication model to surrogate authentication,
allowing REST credentials to specify a substitute and authenticate on behalf of another user. To activate surrogate authentication
for the CAS REST API, you will need to choose one of the following options:

- Format the credential username using the following syntax:

```bash
[surrogate-userid][separator][primary-userid]
```

- Pass along a special request header `X-Surrogate-Principal` that contains the surrogate userid.
